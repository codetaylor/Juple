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

package com.sudoplay.juple.classparser.adapters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLConstructorConstructor;
import com.sudoplay.juple.classparser.TMLExcluder;
import com.sudoplay.juple.classparser.TMLObjectConstructor;
import com.sudoplay.juple.classparser.TMLPrimitives;
import com.sudoplay.juple.classparser.TMLType;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.annotations.SerializedName;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 * 
 * <p>
 * Portions of this class have been derived from the google-gson source <a
 * href="https://code.google.com/p/google-gson/"
 * >https://code.google.com/p/google-gson/</a>.
 * 
 * @author original unknown
 * @author Jason Taylor (modified for Juple)
 */
public final class TMLReflectiveTypeAdapterFactory implements
    TMLTypeAdapterFactory {
  private final TMLConstructorConstructor constructorConstructor;
  private final TMLExcluder excluder;

  public TMLReflectiveTypeAdapterFactory(
      TMLConstructorConstructor constructorConstructor, TMLExcluder excluder) {
    this.constructorConstructor = constructorConstructor;
    this.excluder = excluder;
  }

  public boolean excludeField(Field f, boolean serialize) {
    return !excluder.excludeClass(f.getType(), serialize)
        && !excluder.excludeField(f, serialize);
  }

  private String getFieldName(Field f) {
    SerializedName serializedName = f.getAnnotation(SerializedName.class);
    return serializedName == null ? f.getName() : serializedName.value();
  }

  public <T> TMLTypeAdapter<T> create(Juple juple, final TMLTypeToken<T> type) {

    Class<? super T> raw = type.getRawType();
    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    TMLObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(constructor, getBoundFields(juple, type, raw));
  }

  private TMLReflectiveTypeAdapterFactory.BoundField createBoundField(
      final Juple context, final Field field, final String name,
      final TMLTypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = TMLPrimitives.isPrimitive(fieldType
        .getRawType());

    // special casing primitives here saves ~5% on Android...
    return new TMLReflectiveTypeAdapterFactory.BoundField(name, serialize,
        deserialize) {
      final TMLTypeAdapter<?> typeAdapter = context.getAdapter(fieldType);

      @SuppressWarnings({ "unchecked", "rawtypes" })
      // the type adapter and field type always agree
      @Override
      void write(TMLWriter writer, Object value) throws IOException,
          IllegalAccessException {
        Object fieldValue = field.get(value);
        TMLTypeAdapter t = new TMLTypeAdapterRuntimeTypeWrapper(context,
            this.typeAdapter, fieldType.getType());
        if (this.typeAdapter.isFieldEncapsulate()) writer.beginList();
        t.write(writer, fieldValue);
        if (this.typeAdapter.isFieldEncapsulate()) writer.endList();
      }

      @Override
      void read(TMLReader reader, Object value) throws IOException,
          IllegalAccessException {
        if (this.typeAdapter.isFieldEncapsulate()) reader.beginList();
        Object fieldValue = typeAdapter.read(reader);
        if (this.typeAdapter.isFieldEncapsulate()) reader.endList();
        if (fieldValue != null || !isPrimitive) {
          field.set(value, fieldValue);
        }
      }
    };
  }

  private Map<String, BoundField> getBoundFields(Juple context,
      TMLTypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        boolean serialize = excludeField(field, true);
        boolean deserialize = excludeField(field, false);
        if (!serialize && !deserialize) {
          continue;
        }
        field.setAccessible(true);
        Type fieldType = TMLType.resolve(type.getType(), raw,
            field.getGenericType());
        BoundField boundField = createBoundField(context, field,
            getFieldName(field), TMLTypeToken.get(fieldType), serialize,
            deserialize);
        BoundField previous = result.put(boundField.name, boundField);
        if (previous != null) {
          throw new IllegalArgumentException(declaredType
              + " declares multiple fields named " + previous.name);
        }
      }
      type = TMLTypeToken.get(TMLType.resolve(type.getType(), raw,
          raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return result;
  }

  static abstract class BoundField {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }

    abstract void write(TMLWriter writer, Object value) throws IOException,
        IllegalAccessException;

    abstract void read(TMLReader reader, Object value) throws IOException,
        IllegalAccessException;
  }

  public final class Adapter<T> extends TMLTypeAdapter<T> {
    private final TMLObjectConstructor<T> constructor;
    private final Map<String, BoundField> boundFields;

    private Adapter(TMLObjectConstructor<T> constructor,
        Map<String, BoundField> boundFields) {
      this.constructor = constructor;
      this.boundFields = boundFields;
    }

    @Override
    public T read(TMLReader in) throws IOException {

      boolean encapsulate = !in.getAndClearImmediateFieldChild();
      if (encapsulate) in.beginList();

      TMLToken p = in.peek();
      if (p == TMLToken.NULL) {
        in.nextNull();
        if (encapsulate) in.endList();
        return null;
      }

      T instance = constructor.construct();

      if (in.peek() == TMLToken.END_LIST) {
        if (encapsulate) in.endList();
        return instance;
      }

      try {
        int scope = in.getScope();
        while (in.hasNextInScope(scope)) {
          in.beginList();
          String name = in.nextName();
          BoundField field = boundFields.get(name);
          if (field == null || !field.deserialized) {
            in.skipRemaining();
            continue;
          } else {
            field.read(in, instance);
          }
          in.endList();
        }
        if (encapsulate) in.endList();
      } catch (IllegalStateException e) {
        throw new TMLSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
      return instance;
    }

    @Override
    public void write(TMLWriter out, T value) throws IOException {

      boolean encapsulate = !out.getAndClearImmediateFieldChild();
      if (encapsulate) out.beginList();

      if (value == null) {
        out.nullValue();
        if (encapsulate) out.endList();
        return;
      }

      try {
        for (BoundField boundField : boundFields.values()) {
          if (boundField.serialized) {
            out.beginList();
            out.name(boundField.name);
            boundField.write(out, value);
            out.endList();
          }
        }
        if (encapsulate) out.endList();
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
    }

    @Override
    public boolean isRootEncapsulate() {
      return false;
    }
  }
}
