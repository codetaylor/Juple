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

package com.sudoplay.juple.functional;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapterFactory;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class DelegateTypeAdapterTest {

  private StatsTypeAdapterFactory stats;
  private Juple juple;

  public DelegateTypeAdapterTest() {
    stats = new StatsTypeAdapterFactory();
    juple = new JupleBuilder().register(stats).create();
  }

  @Test
  public void testDelegateInvoked() {
    List<BagOfPrimitives> bags = new ArrayList<BagOfPrimitives>();
    for (int i = 0; i < 10; ++i) {
      bags.add(new BagOfPrimitives(i, i, i % 2 == 0, String.valueOf(i)));
    }
    String tml = juple.toTML(bags);
    bags = juple.fromTML(tml,
        new TMLTypeToken<List<BagOfPrimitives>>() {}.getType());
    // 11: 1 list object, and 10 entries. stats invoked on all 5 fields
    assertEquals(51, stats.numReads);
    assertEquals(51, stats.numWrites);
  }

  @Test
  public void testDelegateInvokedOnStrings() {
    String[] bags = new String[] { "1", "2", "3", "4" };
    String tml = juple.toTML(bags);
    bags = juple.fromTML(tml, String[].class);
    // 1 array object with 4 elements.
    assertEquals(5, stats.numReads);
    assertEquals(5, stats.numWrites);
  }

  private static class StatsTypeAdapterFactory implements TMLTypeAdapterFactory {
    public int numReads = 0;
    public int numWrites = 0;

    public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> type) {
      final TMLTypeAdapter<T> delegate = juple.getDelegateAdapter(this, type);
      return new TMLTypeAdapter<T>() {
        @Override
        public void write(TMLWriter out, T value) throws IOException {
          ++numWrites;
          delegate.write(out, value);
        }

        @Override
        public T read(TMLReader in) throws IOException {
          ++numReads;
          return delegate.read(in);
        }

        @Override
        public boolean isArrayEncapsulate() {
          return delegate.isArrayEncapsulate();
        }

        @Override
        public boolean isRootEncapsulate() {
          return delegate.isRootEncapsulate();
        }

        @Override
        public boolean isFieldEncapsulate() {
          return delegate.isFieldEncapsulate();
        }
      };

    }
  }
}
