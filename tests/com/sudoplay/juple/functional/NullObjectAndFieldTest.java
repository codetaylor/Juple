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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.TestTypes.ClassWithObjects;

/**
 * Functional tests for the different cases for serializing null fields and
 * objects.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class NullObjectAndFieldTest {

  private Juple juple = new Juple();

  @Test(expected = NullPointerException.class)
  public void testTopLevelNullObjectSerializationWithoutTypeThrows() {
    String actual = juple.toTML(null);
    assertEquals("[\\0]", actual);
  }

  @Test(expected = NullPointerException.class)
  public void testTopLevelNullArraySerializationWithoutTypeThrows() {
    String actual = juple.toTML((Integer[]) null);
    assertEquals("[\\0]", actual);
  }

  @Test
  public void testTopLevelNullObjectSerialization() {
    String actual = juple.toTML(null, String.class);
    assertEquals("[\\0]", actual);
  }

  @Test
  public void testTopLevelNullObjectDeserialization() {
    String actual = juple.fromTML("[\\0]", String.class);
    assertNull(actual);
  }

  @Test
  public void testTopLevelNullStringArraySerialization() {
    String actual = juple.toTML(null, String[].class);
    assertEquals("[\\2]", actual);
  }

  @Test
  public void testTopLevelNullStringArrayDeserialization() {
    String[] actual = juple.fromTML("[\\2]", String[].class);
    assertNull(actual);
  }

  @Test
  public void testTopLevelNullsInStringArraySerialization() {
    String[] s = new String[] { null, "String!", null };
    String actual = juple.toTML(s, String[].class);
    assertEquals("[[\\0][String!][\\0]]", actual);
  }

  @Test
  public void testTopLevelNullsInStringArrayDeserialization() {
    String[] actual = juple.fromTML("[[\\0][String!][\\0]]", String[].class);
    assertNull(actual[0]);
    assertEquals("String!", actual[1]);
    assertNull(actual[2]);
  }

  @Test
  public void testTopLevelNullsInIntegerArraySerialization() {
    Integer[] i = new Integer[] { null, 42, null };
    String actual = juple.toTML(i, Integer[].class);
    assertEquals("[\\0 42 \\0]", actual);
  }

  @Test
  public void testTopLevelNullsInIntegerArrayDeserialization() {
    Integer[] actual = juple.fromTML("[\\0 42 \\0]", Integer[].class);
    assertNull(actual[0]);
    assertEquals(42, (int) actual[1]);
    assertNull(actual[2]);
  }

  @Test
  public void testTopLevelNullStringCollectionSerialization() {
    String actual = juple.toTML(null, Collection.class);
    assertEquals("[\\2]", actual);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testTopLevelNullStringCollectionDeserialization() {
    Collection actual = juple.fromTML("[\\2]", Collection.class);
    assertNull(actual);
  }

  @Test
  public void testTopLevelNullStringParameterizedCollectionSerialization() {
    Type type = new TMLTypeToken<Collection<String>>() {}.getType();
    String actual = juple.toTML(null, type);
    assertEquals("[\\2]", actual);
  }

  @Test
  public void testTopLevelNullStringParameterizedCollectionDeserialization() {
    Type type = new TMLTypeToken<Collection<String>>() {}.getType();
    Collection<String> actual = juple.fromTML("[\\2]", type);
    assertNull(actual);
  }

  @Test
  public void testTopLevelNullMapSerialization() {
    String actual = juple.toTML(null, Map.class);
    assertEquals("[\\2]", actual);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testTopLevelNullMapDeserialization() {
    Map actual = juple.fromTML("[\\2]", Map.class);
    assertNull(actual);
  }

  @Test
  public void testTopLevelNullStringParameterizedMapSerialization() {
    Type type = new TMLTypeToken<Map<String, String>>() {}.getType();
    String actual = juple.toTML(null, type);
    assertEquals("[\\2]", actual);
  }

  @Test
  public void testTopLevelNullStringParameterizedMapDeserialization() {
    Type type = new TMLTypeToken<Map<String, String>>() {}.getType();
    Map<String, String> actual = juple.fromTML("[\\2]", type);
    assertNull(actual);
  }

  @Test
  public void testExplicitSerializationOfNulls() {
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = juple.toTML(target);
    String expected = "[[bag|\\0]]";
    assertEquals(expected, actual);
  }

  @Test
  public void testExplicitDeserializationOfNulls() throws Exception {
    ClassWithObjects target = juple.fromTML("[[bag|\\0]]",
        ClassWithObjects.class);
    assertNull(target.bag);
  }

  @Test
  public void testExplicitSerializationOfNullArrayMembers() {
    ClassWithMembers target = new ClassWithMembers();
    String tml = juple.toTML(target);
    assertEquals("[[str|\\0][array|\\2][col|\\2]]", tml);
  }

  @Test
  public void testNullWrappedPrimitiveMemberSerialization() {
    ClassWithNullWrappedPrimitive target = new ClassWithNullWrappedPrimitive();
    String tml = juple.toTML(target);
    assertEquals("[[value|\\0]]", tml);
  }

  @Test
  public void testNullWrappedPrimitiveMemberDeserialization() {
    String tml = "[[value|\\0]]";
    ClassWithNullWrappedPrimitive target = juple.fromTML(tml,
        ClassWithNullWrappedPrimitive.class);
    assertNull(target.value);
  }

  @Test
  public void testExplicitSerializationOfNullCollectionMembers() {
    ClassWithMembers target = new ClassWithMembers();
    String tml = juple.toTML(target);
    assertEquals("[[str|\\0][array|\\2][col|\\2]]", tml);
  }

  @Test
  public void testExplicitSerializationOfNullStringMembers() {
    ClassWithMembers target = new ClassWithMembers();
    String tml = juple.toTML(target);
    assertEquals("[[str|\\0][array|\\2][col|\\2]]", tml);
  }

  @Test
  public void testAbsentJsonElementsAreSetToNull() {
    ClassWithInitializedMembers target = juple.fromTML("[[array|1 2 3]]",
        ClassWithInitializedMembers.class);
    assertTrue(target.array.length == 3 && target.array[1] == 2);
    assertEquals(ClassWithInitializedMembers.MY_STRING_DEFAULT, target.str1);
    assertNull(target.str2);
    assertEquals(ClassWithInitializedMembers.MY_INT_DEFAULT, target.int1);
    assertEquals(0, target.int2); // test the default value of a primitive int
                                  // field per JVM spec
    assertEquals(ClassWithInitializedMembers.MY_BOOLEAN_DEFAULT, target.bool1);
    assertFalse(target.bool2); // test the default value of a primitive boolean
                               // field per JVM spec
  }

  public static class ClassWithInitializedMembers {
    // Using a mix of no-args constructor and field initializers
    // Also, some fields are intialized and some are not (so initialized per JVM
    // spec)
    public static final String MY_STRING_DEFAULT = "string";
    private static final int MY_INT_DEFAULT = 2;
    private static final boolean MY_BOOLEAN_DEFAULT = true;
    int[] array;
    String str1, str2;
    int int1 = MY_INT_DEFAULT;
    int int2;
    boolean bool1 = MY_BOOLEAN_DEFAULT;
    boolean bool2;

    public ClassWithInitializedMembers() {
      str1 = MY_STRING_DEFAULT;
    }
  }

  private static class ClassWithNullWrappedPrimitive {
    private Long value;
  }

  @SuppressWarnings("unused")
  private static class ClassWithMembers {
    String str;
    int[] array;
    Collection<String> col;
  }

}
