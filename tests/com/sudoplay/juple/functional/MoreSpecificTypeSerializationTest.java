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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;

/**
 * Tests for TML serialization of a sub-class object while encountering a
 * base-class type
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
@SuppressWarnings("unused")
public class MoreSpecificTypeSerializationTest {

  private Juple juple = new Juple();

  @Test
  public void testSubclassFields() {
    ClassWithBaseFields target = new ClassWithBaseFields(new Sub(1, 2));
    String tml = juple.toTML(target);
    assertEquals("[[b|[s|2][b|1]]]", tml);
  }

  @Test
  public void testListOfSubclassFields() {
    Collection<Base> list = new ArrayList<Base>();
    list.add(new Base(1));
    list.add(new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(
        list, null);
    String tml = juple.toTML(target);
    assertTrue(tml, tml.contains("[b|1]"));
    assertTrue(tml, tml.contains("[s|3][b|2]"));
  }

  @Test
  public void testMapOfSubclassFields() {
    Map<String, Base> map = new HashMap<String, Base>();
    map.put("base", new Base(1));
    map.put("sub", new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(
        null, map);
    assertEquals("[[collection|\\2][map|[sub|[[s|3][b|2]]][base|[[b|1]]]]]",
        juple.toTML(target));
  }

  /**
   * For parameterized type, Gson ignores the more-specific type and sticks to
   * the declared type
   */
  @Test
  public void testParameterizedSubclassFields() {
    ClassWithParameterizedBaseFields target = new ClassWithParameterizedBaseFields(
        new ParameterizedSub<String>("one", "two"));
    String tml = juple.toTML(target);
    assertEquals("[[b|[t|one]]]", tml);
    assertTrue(tml.contains("[t|one]"));
    assertFalse(tml.contains("[s|"));
  }

  /**
   * For parameterized type in a List, Gson ignores the more-specific type and
   * sticks to the declared type
   */
  @Test
  public void testListOfParameterizedSubclassFields() {
    Collection<ParameterizedBase<String>> list = new ArrayList<ParameterizedBase<String>>();
    list.add(new ParameterizedBase<String>("one"));
    list.add(new ParameterizedSub<String>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target = new ClassWithContainersOfParameterizedBaseFields(
        list, null);
    String tml = juple.toTML(target);
    assertEquals("[[collection|[[t|one]][[t|two]]][map|\\2]]", tml);
    assertTrue(tml, tml.contains("[t|one]"));
    assertFalse(tml, tml.contains("[s|"));
  }

  /**
   * For parameterized type in a map, Gson ignores the more-specific type and
   * sticks to the declared type
   */
  @Test
  public void testMapOfParameterizedSubclassFields() {
    Map<String, ParameterizedBase<String>> map = new HashMap<String, ParameterizedBase<String>>();
    map.put("base", new ParameterizedBase<String>("one"));
    map.put("sub", new ParameterizedSub<String>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target = new ClassWithContainersOfParameterizedBaseFields(
        null, map);
    assertEquals("[[collection|\\2][map|[sub|[[t|two]]][base|[[t|one]]]]]",
        juple.toTML(target));
  }

  private static class Base {
    int b;

    Base(int b) {
      this.b = b;
    }
  }

  private static class Sub extends Base {
    int s;

    Sub(int b, int s) {
      super(b);
      this.s = s;
    }
  }

  private static class ClassWithBaseFields {
    Base b;

    ClassWithBaseFields(Base b) {
      this.b = b;
    }
  }

  private static class ClassWithContainersOfBaseFields {
    Collection<Base> collection;
    Map<String, Base> map;

    ClassWithContainersOfBaseFields(Collection<Base> collection,
        Map<String, Base> map) {
      this.collection = collection;
      this.map = map;
    }
  }

  private static class ParameterizedBase<T> {
    T t;

    ParameterizedBase(T t) {
      this.t = t;
    }
  }

  private static class ParameterizedSub<T> extends ParameterizedBase<T> {
    T s;

    ParameterizedSub(T t, T s) {
      super(t);
      this.s = s;
    }
  }

  private static class ClassWithParameterizedBaseFields {
    ParameterizedBase<String> b;

    ClassWithParameterizedBaseFields(ParameterizedBase<String> b) {
      this.b = b;
    }
  }

  private static class ClassWithContainersOfParameterizedBaseFields {
    Collection<ParameterizedBase<String>> collection;
    Map<String, ParameterizedBase<String>> map;

    ClassWithContainersOfParameterizedBaseFields(
        Collection<ParameterizedBase<String>> collection,
        Map<String, ParameterizedBase<String>> map) {
      this.collection = collection;
      this.map = map;
    }
  }
}
