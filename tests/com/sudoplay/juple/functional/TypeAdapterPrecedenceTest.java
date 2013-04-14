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

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class TypeAdapterPrecedenceTest {

  @Test
  public void testStreamingFollowedByStreaming() {
    Juple juple = new JupleBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 1"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 2"))
        .create();
    assertEquals("[foo via type adapter 2]", juple.toTML(new Foo("foo")));
    assertEquals("foo via type adapter 2",
        juple.fromTML("[foo]", Foo.class).name);
  }

  @Test
  public void testStreamingHierarchicalFollowedByStreaming() {
    Juple juple = new JupleBuilder()
        .registerTypeHierarchyAdapter(Foo.class,
            newTypeAdapter("type adapter 1"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 2"))
        .create();
    assertEquals("[foo via type adapter 2]", juple.toTML(new Foo("foo")));
    assertEquals("foo via type adapter 2",
        juple.fromTML("[foo]", Foo.class).name);
  }

  @Test
  public void testStreamingFollowedByStreamingHierarchical() {
    Juple juple = new JupleBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 1"))
        .registerTypeHierarchyAdapter(Foo.class,
            newTypeAdapter("type adapter 2")).create();
    assertEquals("[foo via type adapter 2]", juple.toTML(new Foo("foo")));
    assertEquals("foo via type adapter 2",
        juple.fromTML("[foo]", Foo.class).name);
  }

  private TMLTypeAdapter<Foo> newTypeAdapter(final String name) {
    return new TMLTypeAdapter<Foo>() {
      @Override
      public Foo read(TMLReader in) throws IOException {
        return new Foo(in.nextString() + " via " + name);
      }

      @Override
      public void write(TMLWriter out, Foo value) throws IOException {
        out.value(value.name + " via " + name);
      }
    };
  }

  private static class Foo {
    final String name;

    private Foo(String name) {
      this.name = name;
    }
  }
}
