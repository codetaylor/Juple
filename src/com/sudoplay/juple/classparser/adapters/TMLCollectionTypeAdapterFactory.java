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
import java.util.Collection;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLConstructorConstructor;
import com.sudoplay.juple.classparser.TMLObjectConstructor;
import com.sudoplay.juple.classparser.TMLType;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class TMLCollectionTypeAdapterFactory implements
    TMLTypeAdapterFactory {
  private final TMLConstructorConstructor constructorConstructor;

  public TMLCollectionTypeAdapterFactory(
      TMLConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  public <T> TMLTypeAdapter<T> create(Juple parser, TMLTypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Collection.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type elementType = TMLType.getCollectionElementType(type, rawType);
    TMLTypeAdapter<?> elementTypeAdapter = parser.getAdapter(TMLTypeToken
        .get(elementType));
    TMLObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    // create() doesn't define a type parameter
    TMLTypeAdapter<T> result = new Adapter(parser, elementType,
        elementTypeAdapter, constructor);
    return result;
  }

  private final class Adapter<E> extends TMLTypeAdapter<Collection<E>> {
    private final TMLTypeAdapter<E> elementTypeAdapter;
    private final TMLObjectConstructor<? extends Collection<E>> constructor;

    public Adapter(Juple parser, Type elementType,
        TMLTypeAdapter<E> elementTypeAdapter,
        TMLObjectConstructor<? extends Collection<E>> constructor) {
      this.elementTypeAdapter = new TMLTypeAdapterRuntimeTypeWrapper<E>(parser,
          elementTypeAdapter, elementType);
      this.constructor = constructor;
    }

    public Collection<E> read(TMLReader in) throws IOException {

      boolean encapsulate = !in.getAndClearImmediateFieldChild();
      if (encapsulate) in.beginList();

      TMLToken token = in.peek();
      if (token == TMLToken.NULL_ARRAY) {
        in.nextNullArray();
        if (encapsulate) in.endList();
        return null;
      }

      Collection<E> collection = constructor.construct();

      if (token == TMLToken.END_LIST) {
        if (encapsulate) in.endList();
        return collection;
      }

      int scope = in.getScope();
      while (in.hasNextInScope(scope)) {
        if (elementTypeAdapter.isArrayEncapsulate()) in.beginList();
        collection.add(elementTypeAdapter.read(in));
        if (elementTypeAdapter.isArrayEncapsulate()) in.endList();
      }

      if (encapsulate) in.endList();

      return collection;
    }

    public void write(TMLWriter out, Collection<E> collection)
        throws IOException {

      boolean encapsulate = !out.getAndClearImmediateFieldChild();
      if (encapsulate) out.beginList();

      if (collection == null) {
        out.nullArrayValue();
        if (encapsulate) out.endList();
        return;
      } else if (collection.size() == 0) {
        if (encapsulate) out.endList();
        return;
      }

      for (E element : collection) {
        if (((TMLTypeAdapterRuntimeTypeWrapper<E>) elementTypeAdapter)
            .isArrayEncapsulate(element)) out.beginList();
        elementTypeAdapter.write(out, element);
        if (((TMLTypeAdapterRuntimeTypeWrapper<E>) elementTypeAdapter)
            .isArrayEncapsulate(element)) out.endList();
      }

      if (encapsulate) out.endList();
    }

    @Override
    public boolean isRootEncapsulate() {
      return false;
    }
  }
}
