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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLConstructorConstructor;
import com.sudoplay.juple.classparser.TMLObjectConstructor;
import com.sudoplay.juple.classparser.TMLType;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.error.TMLException;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapts maps to TML lists.
 * 
 * <p>
 * A map like:
 * 
 * <pre>
 * Map&lt;Point, String&gt; map = new HashMap&lt;Point, String&gt;();
 * map.put(new Point(5, 6), &quot;a&quot;);
 * map.put(new Point(8, 8), &quot;b&quot;);
 * </pre>
 * 
 * <p>
 * Would serialize like so:
 * 
 * <pre>
 *   [
 *     [
 *       [
 *         [x | 5]
 *         [y | 6]
 *       ]
 *       a
 *     ]
 *     [
 *       [
 *         [x | 8]
 *         [y | 8]
 *       ]
 *       b
 *     ]
 *   ]
 * </pre>
 */
public final class TMLMapTypeAdapterFactory implements TMLTypeAdapterFactory {
  private final TMLConstructorConstructor constructorConstructor;

  public TMLMapTypeAdapterFactory(
      TMLConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Map.class.isAssignableFrom(rawType)) {
      return null;
    }

    Class<?> rawTypeOfSrc = TMLType.getRawType(type);
    Type[] keyAndValueTypes = TMLType
        .getMapKeyAndValueTypes(type, rawTypeOfSrc);
    TMLTypeAdapter<?> keyAdapter = getKeyAdapter(juple, keyAndValueTypes[0]);
    TMLTypeAdapter<?> valueAdapter = juple.getAdapter(TMLTypeToken
        .get(keyAndValueTypes[1]));
    TMLObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    // we don't define a type parameter for the key or value types
    TMLTypeAdapter<T> result = new Adapter(juple, keyAndValueTypes[0],
        keyAdapter, keyAndValueTypes[1], valueAdapter, constructor);
    return result;
  }

  /**
   * Returns a type adapter that writes the value as a string.
   */
  private TMLTypeAdapter<?> getKeyAdapter(Juple context, Type keyType) {
    return (keyType == boolean.class || keyType == Boolean.class) ? TMLTypeAdapters.BOOLEAN_AS_STRING
        : context.getAdapter(TMLTypeToken.get(keyType));
  }

  private final class Adapter<K, V> extends TMLTypeAdapter<Map<K, V>> {
    private final TMLTypeAdapter<K> keyTypeAdapter;
    private final TMLTypeAdapter<V> valueTypeAdapter;
    private final TMLObjectConstructor<? extends Map<K, V>> constructor;

    public Adapter(Juple parser, Type keyType,
        TMLTypeAdapter<K> keyTypeAdapter, Type valueType,
        TMLTypeAdapter<V> valueTypeAdapter,
        TMLObjectConstructor<? extends Map<K, V>> constructor) {
      this.keyTypeAdapter = new TMLTypeAdapterRuntimeTypeWrapper<K>(parser,
          keyTypeAdapter, keyType);
      this.valueTypeAdapter = new TMLTypeAdapterRuntimeTypeWrapper<V>(parser,
          valueTypeAdapter, valueType);
      this.constructor = constructor;
    }

    public Map<K, V> read(TMLReader in) throws IOException {

      boolean encapsulate = !in.getAndClearImmediateFieldChild();
      if (encapsulate) in.beginList();

      TMLToken token = in.peek();
      if (token == TMLToken.NULL_ARRAY) {
        in.nextNullArray();
        return null;
      }

      Map<K, V> map = constructor.construct();

      if (token == TMLToken.END_LIST) {
        if (encapsulate) in.endList();
        return map;
      }

      int scope = in.getScope();
      while (in.hasNextInScope(scope)) {
        in.beginList(); // entry array
        K key = keyTypeAdapter.read(in);
        if (key == null) {
          throw new TMLSyntaxException(
              "Juple cannot serialize maps with null keys.");
        }
        in.consumeDivider();
        V value = valueTypeAdapter.read(in);
        V replaced = map.put(key, value);
        if (replaced != null) {
          throw new TMLSyntaxException("duplicate key: " + key);
        }
        in.endList();
      }
      if (encapsulate) in.endList();
      return map;
    }

    public void write(TMLWriter out, Map<K, V> map) throws IOException {

      boolean encapsulate = !out.getAndClearImmediateFieldChild();
      if (encapsulate) out.beginList();

      if (map == null) {
        out.nullArrayValue();
        if (encapsulate) out.endList();
        return;
      } else if (map.isEmpty()) {
        if (encapsulate) out.endList();
        return;
      }

      List<K> keys = new ArrayList<K>(map.size());
      List<V> values = new ArrayList<V>(map.size());

      for (Map.Entry<K, V> entry : map.entrySet()) {
        if (entry.getKey() == null) {
          throw new TMLException(
              "Juple cannot deserialize maps with null keys.");
        }
        keys.add(entry.getKey());
        values.add(entry.getValue());
      }

      for (int i = 0; i < keys.size(); i++) {
        out.beginList(); // entry array
        keyTypeAdapter.write(out, keys.get(i));
        out.divider();
        valueTypeAdapter.write(out, values.get(i));
        out.endList();
      }
      if (encapsulate) out.endList();
    }

    @Override
    public boolean isRootEncapsulate() {
      return false;
    }
  }
}
