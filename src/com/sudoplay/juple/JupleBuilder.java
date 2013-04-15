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

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sudoplay.juple.classparser.TMLExcluder;
import com.sudoplay.juple.classparser.TMLInstanceCreator;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLDateTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLSqlDateTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTimeTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapterFactory;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapters;
import com.sudoplay.juple.error.TMLContract;
import com.sudoplay.juple.stream.SpaceEscapePolicy;

/**
 * <p>
 * Use this builder to construct a {@link Juple} instance when you need to set
 * configuration options other than the default. For {@link Juple} with default
 * configuration, it is simpler to use {@code new Juple()}. {@code JupleBuilder}
 * is best used by creating it, and then invoking its various configuration
 * methods, and finally calling create.
 * </p>
 * 
 * <p>
 * The following is an example shows how to use the {@code JupleBuilder} to
 * construct a Juple instance:
 * 
 * <pre>
 * Juple juple = new JupleBuilder().register(Id.class, new IdTypeAdapter())
 *     .setGroupStrings(false).setPrettyPrinting().setVersion(1.0).create();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * NOTES:
 * <ul>
 * <li>the order of invocation of configuration methods does not matter.</li>
 * </ul>
 * </p>
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public final class JupleBuilder {

  private boolean prettyPrinting = false;
  private boolean groupStrings = true;
  private List<TMLTypeAdapterFactory> typeAdapterFactories = new ArrayList<TMLTypeAdapterFactory>();
  private TMLExcluder excluder = TMLExcluder.DEFAULT;
  private Map<Type, TMLInstanceCreator<?>> instanceCreators = new HashMap<Type, TMLInstanceCreator<?>>();

  private String datePattern;
  private int dateStyle = DateFormat.DEFAULT;
  private int timeStyle = DateFormat.DEFAULT;

  private String timePattern;

  private SpaceEscapePolicy overrideSpaceEscapePolicy = null;
  private boolean enforceFiniteFloatingPointValues;

  public JupleBuilder setTimeFormat(String timeFormat) {
    this.timePattern = timeFormat;
    return this;
  }

  public JupleBuilder setOverrideSpaceEscapePolicy(SpaceEscapePolicy policy) {
    TMLContract.checkNotNull(policy);
    overrideSpaceEscapePolicy = policy;
    return this;
  }

  /**
   * Configures Juple to serialize {@code Date} objects according to the pattern
   * provided. You can call this method or {@link #setDateFormat(int)} multiple
   * times, but only the last invocation will be used to decide the
   * serialization format.
   * 
   * <p>
   * The date format will be used to serialize and deserialize
   * {@link java.util.Date}, {@link java.sql.Timestamp} and
   * {@link java.sql.Date}.
   * 
   * <p>
   * Note that this pattern must abide by the convention provided by
   * {@code SimpleDateFormat} class. See the documentation in
   * {@link java.text.SimpleDateFormat} for more information on valid date and
   * time patterns.
   * </p>
   * 
   * @param pattern
   *          the pattern that dates will be serialized/deserialized to/from
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setDateFormat(String dateFormat) {
    this.datePattern = dateFormat;
    return this;
  }

  /**
   * Configures Juple to to serialize {@code Date} objects according to the
   * style value provided. You can call this method or
   * {@link #setDateFormat(String)} multiple times, but only the last invocation
   * will be used to decide the serialization format.
   * 
   * <p>
   * Note that this style value should be one of the predefined constants in the
   * {@code DateFormat} class. See the documentation in
   * {@link java.text.DateFormat} for more information on the valid style
   * constants.
   * </p>
   * 
   * @param style
   *          the predefined date style that date objects will be
   *          serialized/deserialized to/from
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setDateFormat(int dateStyle) {
    this.dateStyle = dateStyle;
    this.datePattern = null;
    return this;
  }

  /**
   * Configures Juple to to serialize {@code Date} objects according to the
   * style value provided. You can call this method or
   * {@link #setDateFormat(String)} multiple times, but only the last invocation
   * will be used to decide the serialization format.
   * 
   * <p>
   * Note that this style value should be one of the predefined constants in the
   * {@code DateFormat} class. See the documentation in
   * {@link java.text.DateFormat} for more information on the valid style
   * constants.
   * </p>
   * 
   * @param dateStyle
   *          the predefined date style that date objects will be
   *          serialized/deserialized to/from
   * @param timeStyle
   *          the predefined style for the time portion of the date objects
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setDateFormat(int dateStyle, int timeStyle) {
    this.dateStyle = dateStyle;
    this.timeStyle = timeStyle;
    this.datePattern = null;
    return this;
  }

  /**
   * Juple always accepts special floating point values during serialization and
   * deserialization. If this flag is set, when it encounters a float value
   * {@link Float#NaN}, {@link Float#POSITIVE_INFINITY} ,
   * {@link Float#NEGATIVE_INFINITY}, or a double value {@link Double#NaN},
   * {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, it will
   * throw an {@link IllegalArgumentException}. This method provides a way to
   * override the default behavior when you know that the TML receiver will not
   * be able to handle these special values.
   * 
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder enforceFiniteFloatingPointValues() {
    this.enforceFiniteFloatingPointValues = true;
    return this;
  }

  /**
   * Formats the output TML in a more human readable manner.
   * 
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setPrettyPrinting() {
    prettyPrinting = true;
    return this;
  }

  /**
   * Registers a {@link TMLTypeAdapter} or a {@link TMLInstanceCreator}.
   * 
   * @param typeAdapter
   * @param type
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public JupleBuilder registerTypeAdapter(Type type, Object typeAdapter) {
    TMLContract.checkArgument(typeAdapter instanceof TMLTypeAdapter<?>
        || typeAdapter instanceof TMLInstanceCreator<?>);
    if (typeAdapter instanceof TMLInstanceCreator<?>) {
      registerInstanceCreator((TMLInstanceCreator) typeAdapter, type);
    }
    if (typeAdapter instanceof TMLTypeAdapter<?>) {
      registerTypeAdapterFactory(TMLTypeAdapters.newFactory(
          TMLTypeToken.get(type), (TMLTypeAdapter) typeAdapter));
    }
    return this;
  }

  /**
   * Configures Juple for custom serialization and deserialization for an
   * inheritance type hierarchy. If a type adapter was previously registered for
   * the specified type hierarchy, it is overridden. If a type adapter is
   * registered for a specific type in the type hierarchy, it will be invoked
   * instead of the one registered for the type hierarchy.
   * 
   * @param baseType
   *          the class definition for the type adapter being registered for the
   *          base class or interface
   * @param typeAdapter
   *          This object must extend {@link TMLTypeAdapter}.
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public JupleBuilder registerTypeHierarchyAdapter(Class<?> baseType,
      TMLTypeAdapter<?> adapter) {
    registerTypeAdapterFactory(TMLTypeAdapters.newTypeHierarchyFactory(
        baseType, (TMLTypeAdapter) adapter));
    return this;
  }

  /**
   * If set to true, string lists will be concatenated with a single whitespace.
   * 
   * <p>
   * For example, if you have {@code [ _string  | TML is awesome! ]} and set
   * autoSpaceStrings to true, the string will be parsed as
   * {@code "TML is awesome!"}.
   * 
   * <p>
   * If you have autoSpaceStrings set to false, you would need to specify the
   * escaped space character like so: {@code [ _string  | TML\sis\sawesome! ]}
   * to achieve the same results.
   * 
   * <p>
   * If {@code [ _string  | TML is awesome! ]} is specified and autoSpaceStrings
   * is false, only the first string is captured: {@code "TML"} and all others
   * before the next closing bracket are ignored.
   * 
   * <p>
   * Default = true
   * 
   * @param autoSpaceStrings
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setGroupStrings(boolean groupStrings) {
    this.groupStrings = groupStrings;
    return this;
  }

  /**
   * Add a user defined {@link TMLTypeAdapterFactory} to the list. If the
   * factory is null or already exists in the list, an exception is thrown.
   * 
   * @param factory
   *          the factory to add
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder registerTypeAdapterFactory(TMLTypeAdapterFactory factory) {
    TMLContract.checkNotNull(factory);
    TMLContract.checkArgument(!typeAdapterFactories.contains(factory));
    typeAdapterFactories.add(factory);
    return this;
  }

  /**
   * Configures Juple to enable versioning support.
   * 
   * @param ignoreVersionsAfter
   *          any field or type marked with a version higher than this value are
   *          ignored during serialization or deserialization.
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder setVersion(double ignoreVersionsAfter) {
    excluder = excluder.withVersion(ignoreVersionsAfter);
    return this;
  }

  /**
   * Configures Juple to exclude inner classes during serialization.
   * 
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder disableInnerClassSerialization() {
    excluder = excluder.disableInnerClassSerialization();
    return this;
  }

  /**
   * Configures Juple to exclude all fields from consideration for serialization
   * or deserialization that do not have the
   * {@link com.sudoplay.juple.classparser.annotations.Expose} annotation.
   * 
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder excludeFieldsWithoutExposeAnnotation() {
    excluder = excluder.excludeFieldsWithoutExposeAnnotation();
    return this;
  }

  /**
   * Configures Juple to excludes all class fields that have the specified
   * modifiers. By default, Juple will exclude all fields marked transient or
   * static. This method will override that behavior.
   * 
   * @param modifiers
   *          the field modifiers. You must use the modifiers specified in the
   *          {@link java.lang.reflect.Modifier} class. For example,
   *          {@link java.lang.reflect.Modifier#TRANSIENT},
   *          {@link java.lang.reflect.Modifier#STATIC}.
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public JupleBuilder excludeFieldsWithModifiers(int... modifiers) {
    excluder = excluder.withModifiers(modifiers);
    return this;
  }

  /**
   * 
   * @param instanceCreator
   * @param classOfT
   * @return a reference to this {@code JupleBuilder} object for chaining
   */
  public <T> JupleBuilder registerInstanceCreator(
      TMLInstanceCreator<T> instanceCreator, Type type) {
    instanceCreators.put(type, instanceCreator);
    return this;
  }

  public Juple create() {

    List<TMLTypeAdapterFactory> factories = new ArrayList<TMLTypeAdapterFactory>();
    factories.addAll(this.typeAdapterFactories);
    Collections.reverse(factories);
    addTypeAdaptersForDate(datePattern, dateStyle, timeStyle, factories);
    addTypeAdaptersForTime(timePattern, factories);

    return new Juple(excluder, instanceCreators, prettyPrinting, groupStrings,
        factories, overrideSpaceEscapePolicy, enforceFiniteFloatingPointValues);

  }

  private void addTypeAdaptersForTime(String timePattern,
      List<TMLTypeAdapterFactory> factories) {
    if (timePattern == null) {
      return;
    }
    factories.add(TMLTypeAdapters.newFactory(Time.class,
        new TMLTimeTypeAdapter(timePattern)));
  }

  private void addTypeAdaptersForDate(String datePattern, int dateStyle,
      int timeStyle, List<TMLTypeAdapterFactory> factories) {
    TMLDateTypeAdapter dateTypeAdapter;
    TMLSqlDateTypeAdapter sqlDateTypeAdapter;
    if (datePattern != null && !"".equals(datePattern.trim())) {

      dateTypeAdapter = new TMLDateTypeAdapter(datePattern);
      sqlDateTypeAdapter = new TMLSqlDateTypeAdapter(datePattern);

    } else if (dateStyle != DateFormat.DEFAULT
        && timeStyle != DateFormat.DEFAULT) {

      dateTypeAdapter = new TMLDateTypeAdapter(dateStyle, timeStyle);
      sqlDateTypeAdapter = new TMLSqlDateTypeAdapter(dateStyle, timeStyle);

    } else {
      return;
    }

    factories.add(TMLTypeAdapters.newFactory(Date.class, dateTypeAdapter));
    // factories.add(dateTypeAdapter.getFactory(Timestamp.class));
    factories.add(TMLTypeAdapters.newFactory(java.sql.Date.class,
        sqlDateTypeAdapter));
  }

}
