/*
 * Copyright (C) 2013 Jason Taylor.
 * Released as open-source under the Apache License, Version 2.0.
 * 
 * ============================================================================
 * | Juple
 * ============================================================================
 * 
 * Copyright (C) 2013 Jason Taylor
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============================================================================
 * | Gson
 * | --------------------------------------------------------------------------
 * | Juple is a derivative work based on Google's Gson library:
 * | https://code.google.com/p/google-gson/
 * ============================================================================
 * 
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sudoplay.juple;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sudoplay.juple.classparser.TMLConstructorConstructor;
import com.sudoplay.juple.classparser.TMLExcluder;
import com.sudoplay.juple.classparser.TMLInstanceCreator;
import com.sudoplay.juple.classparser.TMLPrimitives;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLArrayTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLCollectionTypeAdapterFactory;
import com.sudoplay.juple.classparser.adapters.TMLDateTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLMapTypeAdapterFactory;
import com.sudoplay.juple.classparser.adapters.TMLObjectTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLReflectiveTypeAdapterFactory;
import com.sudoplay.juple.classparser.adapters.TMLSqlDateTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTimeTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapterFactory;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapters;
import com.sudoplay.juple.error.TMLContract;
import com.sudoplay.juple.error.TMLIOException;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.SpaceEscapePolicy;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;
import com.sudoplay.juple.tree.TMLNode;
import com.sudoplay.juple.tree.TMLNodeTreeParser;

/**
 * This is the main class for using Juple. Juple is typically used by first
 * constructing a Juple instance and then invoking {@link #toTML(Object)} or
 * {@link #fromTML(String, Class)}.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class Juple {

  /**
   * This thread local guards against reentrant calls to getAdapter(). In
   * certain object graphs, creating an adapter for a type may recursively
   * require an adapter for the same type! Without intervention, the recursive
   * lookup would stack overflow. We cheat by returning a proxy type adapter.
   * The proxy is wired up once the initial adapter has been created.
   */
  private final ThreadLocal<Map<TMLTypeToken<?>, FutureTypeAdapter<?>>> calls = new ThreadLocal<Map<TMLTypeToken<?>, FutureTypeAdapter<?>>>() {
    @Override
    protected Map<TMLTypeToken<?>, FutureTypeAdapter<?>> initialValue() {
      return new HashMap<TMLTypeToken<?>, FutureTypeAdapter<?>>();
    }
  };

  private final Map<TMLTypeToken<?>, TMLTypeAdapter<?>> adapterCache = Collections
      .synchronizedMap(new HashMap<TMLTypeToken<?>, TMLTypeAdapter<?>>());

  private final List<TMLTypeAdapterFactory> factories;
  private final TMLConstructorConstructor constructorConstructor;
  private final boolean prettyPrinting;

  private final SpaceEscapePolicy overrideSpaceEscapePolicy;
  private final boolean enforceFiniteFloatingPointValues;

  private final TMLNodeTreeParser nodeTreeParser = new TMLNodeTreeParser(true);

  /**
   * Constructs a new Juple instance with default settings.
   */
  public Juple() {
    this(TMLExcluder.DEFAULT, Collections
        .<Type, TMLInstanceCreator<?>> emptyMap(), false, true, Collections
        .<TMLTypeAdapterFactory> emptyList(), null, false);
  }

  /**
   * Constructs a new Juple instance and registers the default
   * {@link TMLTypeAdapterFactory}s and user supplied factories.
   * 
   * @param overrideSpaceEscapePolicy
   * @param disableSerializeSpecialFloatingPointValues
   */
  public Juple(final TMLExcluder excluder,
      final Map<Type, TMLInstanceCreator<?>> instanceCreators,
      boolean prettyPrinting, boolean groupStrings,
      List<TMLTypeAdapterFactory> typeAdapterFactories,
      SpaceEscapePolicy overrideSpaceEscapePolicy,
      boolean enforceFiniteFloatingPointValues) {

    this.enforceFiniteFloatingPointValues = enforceFiniteFloatingPointValues;
    this.overrideSpaceEscapePolicy = overrideSpaceEscapePolicy;
    this.prettyPrinting = prettyPrinting;

    this.constructorConstructor = new TMLConstructorConstructor(
        instanceCreators);

    List<TMLTypeAdapterFactory> factories = new ArrayList<TMLTypeAdapterFactory>();

    // built-in
    factories.add(TMLObjectTypeAdapter.FACTORY);

    factories.addAll(typeAdapterFactories);

    factories.add(excluder);

    factories.add(TMLTypeAdapters.STRING_FACTORY);
    factories.add(TMLTypeAdapters.INTEGER_FACTORY);
    factories.add(TMLTypeAdapters.BOOLEAN_FACTORY);
    factories.add(TMLTypeAdapters.BYTE_FACTORY);
    factories.add(TMLTypeAdapters.SHORT_FACTORY);
    factories.add(TMLTypeAdapters.LONG_FACTORY);
    factories.add(TMLTypeAdapters.DOUBLE_FACTORY);
    factories.add(TMLTypeAdapters.FLOAT_FACTORY);
    factories.add(TMLTypeAdapters.NUMBER_FACTORY);
    factories.add(TMLTypeAdapters.CHARACTER_FACTORY);
    factories.add(TMLTypeAdapters.STRING_BUILDER_FACTORY);
    factories.add(TMLTypeAdapters.STRING_BUFFER_FACTORY);
    factories.add(TMLTypeAdapters.BIG_DECIMAL_FACTORY);
    factories.add(TMLTypeAdapters.BIG_INTEGER_FACTORY);
    factories.add(TMLTypeAdapters.URL_FACTORY);
    factories.add(TMLTypeAdapters.URI_FACTORY);
    factories.add(TMLTypeAdapters.UUID_FACTORY);
    factories.add(TMLTypeAdapters.LOCALE_FACTORY);
    factories.add(TMLTypeAdapters.INET_ADDRESS_FACTORY);
    factories.add(TMLTypeAdapters.BIT_SET_FACTORY);

    factories.add(TMLTypeAdapters.newFactory(Date.class,
        new TMLDateTypeAdapter()));

    factories.add(TMLTypeAdapters.CALENDAR_FACTORY);
    factories.add(TMLTimeTypeAdapter.FACTORY);
    factories.add(TMLTypeAdapters.newFactory(java.sql.Date.class,
        new TMLSqlDateTypeAdapter()));
    factories.add(TMLTypeAdapters.TIMESTAMP_FACTORY);
    factories.add(TMLArrayTypeAdapter.FACTORY);
    factories.add(TMLTypeAdapters.ENUM_FACTORY);
    factories.add(TMLTypeAdapters.CLASS_FACTORY);

    factories.add(new TMLCollectionTypeAdapterFactory(constructorConstructor));
    factories.add(new TMLMapTypeAdapterFactory(constructorConstructor));
    factories.add(new TMLReflectiveTypeAdapterFactory(constructorConstructor,
        excluder));

    this.factories = Collections.unmodifiableList(factories);
  }

  /**
   * This method deserializes the specified TML into an object of the specified
   * class. It is not suitable to use if the specified class is a generic type
   * since it will not have the generic type information because of the Type
   * Erasure feature of Java. Therefore, this method should not be used if the
   * desired type is a generic type. Note that this method works fine if the any
   * of the fields of the specified object are generics, just the object itself
   * should not be a generic type. For the cases when the object is of generic
   * type, invoke {@link #fromTML(String, Type)}. If you have the TML in a
   * {@link Reader} instead of a String, use {@link #fromTML(Reader, Class)}
   * instead.
   * 
   * @param <T>
   *          the type of the desired object
   * @param string
   *          the string from which the object is to be deserialized
   * @param classOfT
   *          the class of T
   * @return an object of type T from the string
   */
  public <T> T fromTML(String string, Class<T> classOfT) {
    if (string == null) return null;
    Object object = fromTML(string, (Type) classOfT);
    return TMLPrimitives.wrap(classOfT).cast(object);
  }

  /**
   * This method deserializes the specified TML into an object of the specified
   * type. This method is useful if the specified object is a generic type. For
   * non-generic objects, use {@link #fromTML(String, Class)} instead. If you
   * have the TML in a {@link Reader} instead of a String, use
   * {@link #fromTML(Reader, Type)} instead.
   * 
   * <p>
   * You can obtain the type by using the
   * {@link com.sudoplay.juple.classparser.TMLTypeToken} class. For example, to
   * get the type for {@code Collection<Foo>}, you should use:
   * 
   * <pre>
   * Type typeOfObj = new TMLTypeToken&lt;Collection&lt;Foo&gt;&gt;() {}.getType();
   * </pre>
   * 
   * @param <T>
   *          the type of the desired object
   * @param string
   *          the string from which the object is to be deserialized
   * @param typeOfT
   *          The specific genericized type of src
   * @return an object of type T from the string
   */
  @SuppressWarnings("unchecked")
  public <T> T fromTML(String string, Type type) {
    if (string == null) return null;
    StringReader reader = new StringReader(string);
    T object = (T) fromTML(reader, type);
    reader.close();
    return object;
  }

  /**
   * This method deserializes the TML read from the specified reader into an
   * object of the specified class. It is not suitable to use if the specified
   * class is a generic type since it will not have the generic type information
   * because of the Type Erasure feature of Java. Therefore, this method should
   * not be used if the desired type is a generic type. Note that this method
   * works fine if the any of the fields of the specified object are generics,
   * just the object itself should not be a generic type. For the cases when the
   * object is of generic type, invoke {@link #fromTML(Reader, Type)}. If you
   * have the TML in a String form instead of a {@link Reader}, use
   * {@link #fromTML(String, Class)} instead.
   * 
   * @param <T>
   *          the type of the desired object
   * @param reader
   *          the reader producing the TML from which the object is to be
   *          deserialized
   * @param classOfT
   *          the class of T
   * @return an object of type T from the string
   */
  public <T> T fromTML(Reader reader, Class<T> classOfT) {
    Object object = fromTML(reader, (Type) classOfT);
    return TMLPrimitives.wrap(classOfT).cast(object);
  }

  /**
   * This method deserializes the TML read from the specified reader into an
   * object of the specified type. This method is useful if the specified object
   * is a generic type. For non-generic objects, use
   * {@link #fromTML(Reader, Class)} instead. If you have the Json in a String
   * form instead of a {@link Reader}, use {@link #fromTML(String, Type)}
   * instead.
   * 
   * <p>
   * You can obtain the type by using the
   * {@link com.sudoplay.juple.classparser.TMLTypeToken} class. For example, to
   * get the type for {@code Collection<Foo>}, you should use:
   * 
   * <pre>
   * Type typeOfObj = new TMLTypeToken&lt;Collection&lt;Foo&gt;&gt;() {}.getType();
   * </pre>
   * 
   * @param <T>
   *          the type of the desired object
   * @param reader
   *          the reader producing TML from which the object is to be
   *          deserialized
   * @param typeOfT
   *          The specific genericized type of src
   * @return an object of type T from the tml
   */
  @SuppressWarnings("unchecked")
  public <T> T fromTML(Reader reader, Type type) throws TMLSyntaxException,
      TMLIOException {
    TMLReader tmlReader = new TMLReader(reader);
    T object = (T) fromTML(tmlReader, type);
    TMLContract.assertFullConsumption(object, tmlReader);
    return object;
  }

  /**
   * Reads the next TML value from {@code reader} and converts it to an object
   * of type {@code typeOfT}.
   * 
   * <p>
   * Since Type is not parameterized by T, this method is type unsafe and should
   * be used carefully
   * 
   * @param reader
   *          the TMLReader to read from
   * @param typeOfT
   *          type of object to return
   * @return
   * @throws TMLSyntaxException
   *           if the source TML is not a valid for an object of type
   * @throws TMLIOException
   *           if there was a problem reading from the reader
   */
  @SuppressWarnings("unchecked")
  private <T> T fromTML(TMLReader reader, Type typeOfT)
      throws TMLSyntaxException, TMLIOException {
    try {
      TMLTypeAdapter<T> adapter = (TMLTypeAdapter<T>) getAdapter(TMLTypeToken
          .get(typeOfT));
      if (adapter.isRootEncapsulate()) reader.beginList();
      T result = adapter.read(reader);
      if (adapter.isRootEncapsulate()) reader.endList();
      return result;
    } catch (EOFException e) {
      throw new TMLSyntaxException(e);
    } catch (IllegalStateException e) {
      throw new TMLSyntaxException(e);
    } catch (IOException e) {
      throw new TMLIOException(e);
    }
  }

  /**
   * This method parses TML data from a reader into a {@link TMLNode} tree in
   * the proper format for Juple. When the resulting node is converted to a
   * string using {@link TMLNode#toString()} or
   * {@link TMLNode#toString(boolean)}, the TML produced is in the proper format
   * for Juple's class conversion methods: {@link #fromTML(String, Class)} and
   * {@link #fromTML(String, Type)} .
   * 
   * @param reader
   * @return
   */
  public TMLNode toTMLNode(Reader reader) {
    return nodeTreeParser.parse(reader);
  }

  /**
   * This method parses a TML string into a {@link TMLNode} tree in the proper
   * format for Juple. When the resulting node is converted to a string using
   * {@link TMLNode#toString()} or {@link TMLNode#toString(boolean)}, the TML
   * produced is in the proper format for Juple's class conversion methods:
   * {@link #fromTML(String, Class)} and {@link #fromTML(String, Type)} .
   * 
   * @param string
   * @return
   */
  public TMLNode toTMLNode(String string) {
    return nodeTreeParser.parse(string);
  }

  /**
   * This method serializes the specified object into its equivalent TML
   * representation. This method should be used when the specified object is not
   * a generic type. This method uses {@link Class#getClass()} to get the type
   * for the specified object, but the {@code getClass()} loses the generic type
   * information because of the Type Erasure feature of Java.
   * 
   * <p>
   * Note that this method works fine if the any of the object fields are of
   * generic type, just the object itself should not be of a generic type. If
   * the object is of generic type, use {@link #toTML(Object, Type)} instead. If
   * you want to write out the object to a {@link Writer}, use
   * {@link #toTML(Object, Writer)} instead.
   * 
   * @param obj
   *          the object for which TML representation is to be created
   * @return TML representation of {@code obj}
   */
  public String toTML(Object obj) {
    if (obj == null) {
      throw new NullPointerException(
          "Use toTML(Object, Type) to ensure correct null value serialization");
    }
    return toTML(obj, obj.getClass());
  }

  /**
   * This method serializes the specified object, including those of generic
   * types, into its equivalent TML representation. This method must be used if
   * the specified object is a generic type. For non-generic objects, use
   * {@link #toTML(Object)} instead. If you want to write out the object to a
   * {@link Writer}, use {@link #toTML(Object, Type, Writer)} instead.
   * 
   * <p>
   * You can obtain the type by using the
   * {@link com.sudoplay.juple.classparser.TMLTypeToken} class. For example, to
   * get the type for {@code Collection<Foo>}, you should use:
   * 
   * <pre>
   * Type typeOfObj = new TMLTypeToken&lt;Collection&lt;Foo&gt;&gt;() {}.getType();
   * </pre>
   * 
   * @param obj
   *          the object for which TML representation is to be created
   * @param typeOfObj
   *          the specific generic type of obj
   * @return TML representation of {@code obj}
   */
  public String toTML(Object obj, Type typeOfObj) {
    try {
      StringWriter writer = new StringWriter();
      toTML(obj, typeOfObj, writer);
      writer.close();
      return writer.toString();
    } catch (IOException e) {
      throw new TMLIOException(e);
    }
  }

  /**
   * This method serializes the specified object into its equivalent TML
   * representation. This method should be used when the specified object is not
   * a generic type. This method uses {@link Class#getClass()} to get the type
   * for the specified object, but the {@code getClass()} loses the generic type
   * information because of the Type Erasure feature of Java.
   * 
   * <p>
   * Note that this method works fine if the any of the object fields are of
   * generic type, just the object itself should not be of a generic type. If
   * the object is of generic type, use {@link #toTML(Object, Type, Writer)}
   * instead.
   * 
   * @param src
   *          the object for which TML representation is to be created
   * @param writer
   *          Writer to which the TML representation needs to be written
   * @throws TMLIOException
   *           if there was a problem writing to the writer
   */
  public void toTML(Object obj, Writer writer) {
    if (obj == null) {
      throw new NullPointerException(
          "Use toTML(Object, Type) to ensure correct null value serialization");
    }
    toTML(obj, obj.getClass(), writer);
  }

  /**
   * This method serializes the specified object, including those of generic
   * types, into its equivalent TML representation. This method must be used if
   * the specified object is a generic type. For non-generic objects, use
   * {@link #toTML(Object, Writer)} instead.
   * 
   * <p>
   * You can obtain the type by using the
   * {@link com.sudoplay.juple.classparser.TMLTypeToken} class. For example, to
   * get the type for {@code Collection<Foo>}, you should use:
   * 
   * <pre>
   * Type typeOfSrc = new TMLTypeToken&lt;Collection&lt;Foo&gt;&gt;() {}.getType();
   * </pre>
   * 
   * @param src
   *          the object for which TML representation is to be created
   * @param typeOfSrc
   *          The specific genericized type of src. You can obtain this type by
   *          using the {@link com.sudoplay.juple.classparser.TMLTypeToken}
   *          class. See above.
   * @param writer
   *          Writer to which the TML representation of src needs to be written.
   * @throws TMLIOException
   *           if there was a problem writing to the writer
   */
  public void toTML(Object obj, Type typeOfObj, Writer writer) {
    TMLWriter tmlWriter = new TMLWriter(writer);
    toTML(obj, typeOfObj, tmlWriter);
  }

  @SuppressWarnings("unchecked")
  private void toTML(Object obj, Type typeOfObj, TMLWriter writer) {
    writer.setOverrideSpaceEscapePolicy(overrideSpaceEscapePolicy);
    writer
        .setEnforceFiniteFloatingPointValues(enforceFiniteFloatingPointValues);
    try {
      if (prettyPrinting) writer.setIndent("    ");
      TMLTypeAdapter<?> adapter = getAdapter(TMLTypeToken.get(typeOfObj));
      if (adapter.isRootEncapsulate()) writer.beginList();
      ((TMLTypeAdapter<Object>) adapter).write(writer, obj);
      if (adapter.isRootEncapsulate()) writer.endList();
    } catch (IOException e) {
      throw new TMLIOException(e);
    }
  }

  /**
   * Convenience method that simply calls {@link #getAdapter(TMLTypeToken)}.
   * 
   * @param type
   * @return
   */
  public <T> TMLTypeAdapter<T> getAdapter(Class<T> type) {
    return getAdapter(TMLTypeToken.get(type));
  }

  /**
   * Returns an adapter appropriate for parsing the type either by retrieving
   * the adapter from the cache, or creating a new one.
   * 
   * @param <T>
   * 
   * @param type
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> TMLTypeAdapter<T> getAdapter(TMLTypeToken<T> type) {
    TMLTypeAdapter<?> cached = adapterCache.get(type);
    if (cached != null) {
      return (TMLTypeAdapter<T>) cached;
    }

    Map<TMLTypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
    // the key and value type parameters always agree
    FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls
        .get(type);
    if (ongoingCall != null) {
      return ongoingCall;
    }

    FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
    threadCalls.put(type, call);
    try {
      for (TMLTypeAdapterFactory factory : factories) {
        TMLTypeAdapter<T> candidate = factory.create(this, type);
        if (candidate != null) {
          call.setDelegate(candidate);
          // System.out.println(type + " -> " + candidate);
          adapterCache.put(type, candidate);
          return candidate;
        }
      }
      throw new IllegalArgumentException("Juple cannot handle " + type);
    } finally {
      threadCalls.remove(type);
    }
  }

  /**
   * This method is used to get an alternate type adapter for the specified
   * type. This is used to access a type adapter that is overridden by a
   * {@link TMLTypeAdapterFactory} that you may have registered. This features
   * is typically used when you want to register a type adapter that does a
   * little bit of work but then delegates further processing to the default
   * type adapter.
   */
  public <T> TMLTypeAdapter<T> getDelegateAdapter(
      TMLTypeAdapterFactory skipPast, TMLTypeToken<T> type) {
    boolean skipPastFound = false;

    for (TMLTypeAdapterFactory factory : factories) {
      if (!skipPastFound) {
        if (factory == skipPast) {
          skipPastFound = true;
        }
        continue;
      }

      TMLTypeAdapter<T> candidate = factory.create(this, type);
      if (candidate != null) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Cannot serialize " + type);
  }

  static class FutureTypeAdapter<T> extends TMLTypeAdapter<T> {
    private TMLTypeAdapter<T> delegate;

    public void setDelegate(TMLTypeAdapter<T> typeAdapter) {
      if (delegate != null) {
        throw new AssertionError();
      }
      delegate = typeAdapter;
    }

    @Override
    public T read(TMLReader in) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      return delegate.read(in);
    }

    @Override
    public void write(TMLWriter out, T value) throws IOException {
      if (delegate == null) {
        throw new IllegalStateException();
      }
      delegate.write(out, value);
    }
  }

}
