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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;

/**
 * Unit tests to validate serialization of parameterized types without explicit
 * types
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class RawSerializationTest {

  private Juple juple = new Juple();

  @Test
  public void testCollectionOfPrimitives() {
    Collection<Integer> ints = Arrays.asList(1, 2, 3, 4, 5);
    String tml = juple.toTML(ints);
    assertEquals("[1 2 3 4 5]", tml);
  }

  @Test
  public void testCollectionOfObjects() {
    Collection<Foo> foos = Arrays.asList(new Foo(1), new Foo(2));
    String tml = juple.toTML(foos);
    assertEquals("[[[b|1]][[b|2]]]", tml);
  }
  
  @Test
  public void testParameterizedObject() {
    Bar<Foo> bar = new Bar<Foo>(new Foo(1));
    String expectedJson = "[[t|[b|1]]]";
    // Ensure that serialization works without specifying the type explicitly
    String tml = juple.toTML(bar);
    assertEquals(expectedJson, tml);
    // Ensure that serialization also works when the type is specified explicitly
    tml = juple.toTML(bar, new TMLTypeToken<Bar<Foo>>(){}.getType());
    assertEquals(expectedJson, tml);
  }

  @Test
  public void testTwoLevelParameterizedObject() {
    Bar<Bar<Foo>> bar = new Bar<Bar<Foo>>(new Bar<Foo>(new Foo(1)));
    String expectedJson = "[[t|[t|[b|1]]]]";
    // Ensure that serialization works without specifying the type explicitly
    String tml = juple.toTML(bar);
    assertEquals(expectedJson, tml);
    // Ensure that serialization also works when the type is specified explicitly
    tml = juple.toTML(bar, new TMLTypeToken<Bar<Bar<Foo>>>(){}.getType());
    assertEquals(expectedJson, tml);
  }

  @Test
  public void testThreeLevelParameterizedObject() {
    Bar<Bar<Bar<Foo>>> bar = new Bar<Bar<Bar<Foo>>>(new Bar<Bar<Foo>>(new Bar<Foo>(new Foo(1))));
    String expectedJson = "[[t|[t|[t|[b|1]]]]]";
    // Ensure that serialization works without specifying the type explicitly
    String tml = juple.toTML(bar);
    assertEquals(expectedJson, tml);
    // Ensure that serialization also works when the type is specified explicitly
    tml = juple.toTML(bar, new TMLTypeToken<Bar<Bar<Bar<Foo>>>>(){}.getType());
    assertEquals(expectedJson, tml);
  }

  private static class Foo {
    @SuppressWarnings("unused")
    int b;

    Foo(int b) {
      this.b = b;
    }
  }

  @SuppressWarnings("unused")
  private static class Bar<T> {
    T t;

    Bar(T t) {
      this.t = t;
    }
  }
}
