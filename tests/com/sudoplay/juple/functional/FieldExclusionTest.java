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

import static org.junit.Assert.*;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;

public class FieldExclusionTest {

  private static final String VALUE = "blah_1234";

  private Outer outer = new Outer();

  private Juple juple = new Juple();

  @Test
  public void testDefaultInnerClassExclusion() throws Exception {
    Outer.Inner target = outer.new Inner(VALUE);
    String result = juple.toTML(target);
    assertEquals(target.toTML(), result);

    target = outer.new Inner(VALUE);
    result = juple.toTML(target);
    assertEquals(target.toTML(), result);
  }

  @Test
  public void testInnerClassExclusion() throws Exception {
    Juple juple = new JupleBuilder().disableInnerClassSerialization().create();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = juple.toTML(target);
    assertEquals("[\\0]", result);
  }

  @Test
  public void testDefaultNestedStaticClassIncluded() throws Exception {
    Outer.Inner target = outer.new Inner(VALUE);
    String result = juple.toTML(target);
    assertEquals(target.toTML(), result);

    target = outer.new Inner(VALUE);
    result = juple.toTML(target);
    assertEquals(target.toTML(), result);
  }

  private static class Outer {
    private class Inner extends NestedClass {
      public Inner(String value) {
        super(value);
      }
    }

  }

  private static class NestedClass {
    private final String value;

    public NestedClass(String value) {
      this.value = value;
    }

    public String toTML() {
      return "[[value|" + value + "]]";
    }
  }
}
