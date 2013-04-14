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
import java.lang.reflect.TypeVariable;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * @author unknown (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
final class TMLTypeAdapterRuntimeTypeWrapper<T> extends TMLTypeAdapter<T> {
  private final Juple context;
  private final TMLTypeAdapter<T> delegate;
  private final Type type;

  TMLTypeAdapterRuntimeTypeWrapper(Juple context, TMLTypeAdapter<T> delegate,
      Type type) {
    this.context = context;
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public boolean isArrayEncapsulate() {
    return delegate.isArrayEncapsulate();
  }

  @Override
  public boolean isFieldEncapsulate() {
    return delegate.isFieldEncapsulate();
  }

  @Override
  public boolean isRootEncapsulate() {
    return delegate.isRootEncapsulate();
  }

  public boolean isArrayEncapsulate(T value) {
    return getBestAdapter(value).isArrayEncapsulate();
  }

  public boolean isFieldEncapsulate(T value) {
    return getBestAdapter(value).isFieldEncapsulate();
  }

  public boolean isRootEncapsulate(T value) {
    return getBestAdapter(value).isRootEncapsulate();
  }

  @Override
  public T read(TMLReader in) throws IOException {
    return delegate.read(in);
  }

  @SuppressWarnings({ "unchecked" })
  @Override
  public void write(TMLWriter out, T value) throws IOException {
    getBestAdapter(value).write(out, value);
  }

  @SuppressWarnings("rawtypes")
  private TMLTypeAdapter getBestAdapter(T value) {
    // Order of preference for choosing type adapters
    // First preference: a type adapter registered for the runtime type
    // Second preference: a type adapter registered for the declared type
    // Third preference: reflective type adapter for the runtime type (if it is
    // a sub class of the declared type)
    // Fourth preference: reflective type adapter for the declared type
    TMLTypeAdapter chosen = delegate;
    Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
    if (runtimeType != type) {
      TMLTypeAdapter runtimeTypeAdapter = context.getAdapter(TMLTypeToken
          .get(runtimeType));
      if (!(runtimeTypeAdapter instanceof TMLReflectiveTypeAdapterFactory.Adapter)) {
        // The user registered a type adapter for the runtime type, so we will
        // use that
        chosen = runtimeTypeAdapter;
      } else if (!(delegate instanceof TMLReflectiveTypeAdapterFactory.Adapter)) {
        // The user registered a type adapter for Base class, so we prefer it
        // over the
        // reflective type adapter for the runtime type
        chosen = delegate;
      } else {
        // Use the type adapter for runtime type
        chosen = runtimeTypeAdapter;
      }
    }
    return chosen;
  }

  /**
   * Finds a compatible runtime type if it is more specific
   */
  private Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
    if (value != null
        && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
      type = value.getClass();
    }
    return type;
  }
}
