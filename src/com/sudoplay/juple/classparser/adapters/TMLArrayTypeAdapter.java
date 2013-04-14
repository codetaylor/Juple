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
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLType;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapter for an array of objects.
 * 
 * @author unknown (original for gson)
 * @author Jason Taylor (modified for Juple)
 * 
 * @param <E>
 */
public final class TMLArrayTypeAdapter<E> extends TMLTypeAdapter<Object> {

  public static final TMLTypeAdapterFactory FACTORY = new TMLTypeAdapterFactory() {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
      Type type = typeToken.getType();
      if (!(type instanceof GenericArrayType || type instanceof Class
          && ((Class<?>) type).isArray())) {
        return null;
      }
      Type componentType = TMLType.getArrayComponentType(type);
      TMLTypeAdapter<?> componentTypeAdapter = juple.getAdapter(TMLTypeToken
          .get(componentType));

      return new TMLArrayTypeAdapter(juple, componentTypeAdapter,
          TMLType.getRawType(componentType));
    }
  };

  private final TMLTypeAdapter<E> componentTypeAdapter;
  private final Class<E> componentType;

  public TMLArrayTypeAdapter(Juple juple,
      TMLTypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
    this.componentTypeAdapter = new TMLTypeAdapterRuntimeTypeWrapper<E>(juple,
        componentTypeAdapter, componentType);
    this.componentType = componentType;
  }

  @Override
  public Object read(TMLReader in) throws IOException {

    boolean encapsulate = !in.getAndClearImmediateFieldChild();
    if (encapsulate) in.beginList();

    TMLToken token = in.peek();
    if (token == TMLToken.NULL_ARRAY) {
      in.nextNullArray();
      if (encapsulate) in.endList();
      return null;
    }

    List<E> list = new ArrayList<E>();

    if (token == TMLToken.END_LIST) {
      if (encapsulate) in.endList();
      return Array.newInstance(componentType, 0);
    }

    // properties.saveGroupStrings(false);
    int scope = in.getScope();
    while (in.hasNextInScope(scope)) {
      if (componentTypeAdapter.isArrayEncapsulate()) in.beginList();
      list.add(componentTypeAdapter.read(in));
      if (componentTypeAdapter.isArrayEncapsulate()) in.endList();
    }
    // properties.resetGroupStrings();

    if (encapsulate) in.endList();

    Object array = Array.newInstance(componentType, list.size());
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return array;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(TMLWriter out, Object value) throws IOException {

    boolean encapsulate = !out.getAndClearImmediateFieldChild();
    if (encapsulate) out.beginList();

    if (value == null) {
      out.nullArrayValue();
      if (encapsulate) out.endList();
      return;
    }

    for (int i = 0, length = Array.getLength(value); i < length; i++) {
      E element = (E) Array.get(value, i);
      if (((TMLTypeAdapterRuntimeTypeWrapper<E>) componentTypeAdapter)
          .isArrayEncapsulate(element)) out.beginList();
      componentTypeAdapter.write(out, element);
      if (((TMLTypeAdapterRuntimeTypeWrapper<E>) componentTypeAdapter)
          .isArrayEncapsulate(element)) out.endList();
    }

    if (encapsulate) out.endList();
  }

  @Override
  public boolean isRootEncapsulate() {
    return false;
  }

}
