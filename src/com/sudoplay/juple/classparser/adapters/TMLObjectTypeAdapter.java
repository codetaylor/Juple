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
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 * 
 * @author unknown (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public final class TMLObjectTypeAdapter extends TMLTypeAdapter<Object> {
  public static final TMLTypeAdapterFactory FACTORY = new TMLTypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> type) {
      if (type.getRawType() == Object.class) {
        return (TMLTypeAdapter<T>) new TMLObjectTypeAdapter(juple);
      }
      return null;
    }
  };

  private final Juple juple;

  private TMLObjectTypeAdapter(Juple juple) {
    this.juple = juple;
  }

  @Override
  public Object read(TMLReader in) throws IOException {
    TMLToken token = in.peek();
    switch (token) {
    case BEGIN_LIST:
      List<Object> list = new ArrayList<Object>();
      in.beginList();
      int scope = in.getScope();
      while (in.hasNextInScope(scope)) {
        list.add(read(in));
      }
      in.endList();
      return list;

    case DIVIDER:
      in.consumeDivider();
      return read(in);

    case DATA:
      return in.nextString();

    case NULL:
      in.nextNull();
      return null;

    case NULL_ARRAY:
      in.nextNullArray();
      return null;

    default:
      // fall out
    }
    throw new IllegalStateException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(TMLWriter out, Object value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    TMLTypeAdapter<Object> typeAdapter = (TMLTypeAdapter<Object>) juple
        .getAdapter(value.getClass());
    if (typeAdapter instanceof TMLObjectTypeAdapter) {
      out.beginList();
      out.endList();
      return;
    }

    typeAdapter.write(out, value);
  }

}
