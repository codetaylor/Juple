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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.MoreAsserts;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.common.TestTypes.ClassWithObjects;
import com.sudoplay.juple.error.TMLSyntaxException;

public class ArrayTest {

  private Juple juple = new Juple();

  @Test
  public void testTopLevelArrayOfIntsSerialization() {
    int[] target = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    assertEquals("[1 2 3 4 5 6 7 8 9]", juple.toTML(target));
  }

  @Test
  public void testTopLevelArrayOfIntsDeserialization() {
    int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int[] actual = juple.fromTML("[1 2 3 4 5 6 7 8 9]", int[].class);
    MoreAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testTopLevelArrayOfStringsSerialization() {
    String[] target = { "This is a string.", "Another string.", "String three." };
    assertEquals("[[This is a string.][Another string.][String three.]]",
        juple.toTML(target));
  }

  @Test
  public void testTopLevelArrayOfStringsDeserialization() {
    String[] expected = { "This is a string.", "Another string.",
        "String three." };
    String[] actual = juple
        .fromTML("[[This is a string.][Another string.][String three.]]",
            String[].class);
    MoreAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testInvalidArrayDeserialization() {
    String tml = "[1 2 three 4 5]";
    try {
      juple.fromTML(tml, int[].class);
      fail();
    } catch (TMLSyntaxException expected) {}
  }

  @Test
  public void testEmptyArraySerialization() {
    int[] target = {};
    assertEquals("[]", juple.toTML(target));
  }

  @Test
  public void testEmptyArrayDeserialization() {
    int[] actualObject = juple.fromTML("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = juple.fromTML("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);

    actualObject = juple.fromTML("[ ]", int[].class);
    assertTrue(actualObject.length == 0);
  }

  @Test
  public void testNullArraySerialization() {
    int[] target = null;
    assertEquals("[\\2]", juple.toTML(target, int[].class));
  }

  @Test
  public void testNullArrayDeserialization() {
    int[] actualObject = juple.fromTML("[\\2]", int[].class);
    assertNull(actualObject);

    Integer[] actualObject2 = juple.fromTML("[\\2]", Integer[].class);
    assertNull(actualObject2);
  }

  @Test
  public void testNullsInStringArraySerialization() {
    String[] array = { "foo", null, "bar" };
    String expected = "[[foo][\\0][bar]]";
    String json = juple.toTML(array);
    assertEquals(expected, json);
  }

  @Test
  public void testNullsInStringArrayDeserialization() {
    String tml = "[[foo][\\0][bar]]";
    String[] expected = { "foo", null, "bar" };
    String[] target = juple.fromTML(tml, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

  @Test
  public void testNullsInIntegerArraySerialization() {
    Integer[] array = { 42, null, 73 };
    String expected = "[42 \\0 73]";
    String json = juple.toTML(array);
    assertEquals(expected, json);
  }

  @Test
  public void testNullsInIntegerArrayDeserialization() {
    String tml = "[42 \\0 73]";
    Integer[] expected = { 42, null, 73 };
    Integer[] target = juple.fromTML(tml, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

  @Test
  public void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    array[0] = null;
    String json = juple.toTML(array);
    assertEquals("[[\\0]]", json);
  }

  @Test
  public void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = juple.fromTML("[[\\0]]", BagOfPrimitives[].class);
    assertNull(array[0]);
  }

  @Test
  public void testSingleStringArraySerialization() throws Exception {
    String[] s = { "hello" };
    String output = juple.toTML(s);
    assertEquals("[[hello]]", output);
  }

  @Test
  public void testSingleStringArrayDeserialization() throws Exception {
    String json = "[[hello]]";
    String[] arrayType = juple.fromTML(json, String[].class);
    assertEquals(1, arrayType.length);
    assertEquals("hello", arrayType[0]);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testArrayOfCollectionSerialization() throws Exception {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TMLTypeToken<Collection<Integer>[]>() {}
        .getType();
    Collection<Integer>[] arrayOfCollection = new ArrayList[arraySize];
    for (int i = 0; i < arraySize; ++i) {
      int startValue = (3 * i) + 1;
      sb.append('[').append(startValue).append(' ').append(startValue + 1)
          .append(']');
      ArrayList<Integer> tmpList = new ArrayList<Integer>();
      tmpList.add(startValue);
      tmpList.add(startValue + 1);
      arrayOfCollection[i] = tmpList;
    }
    sb.append(']');

    String tml = juple.toTML(arrayOfCollection, typeToSerialize);
    assertEquals(sb.toString(), tml);
  }

  @Test
  public void testArrayOfCollectionDeserialization() throws Exception {
    String tml = "[[1 2][3 4]]";
    Type type = new TMLTypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = juple.fromTML(tml, type);

    assertEquals(2, target.length);

    MoreAsserts.assertEquals(new Integer[] { 1, 2 },
        target[0].toArray(new Integer[0]));

    MoreAsserts.assertEquals(new Integer[] { 3, 4 },
        target[1].toArray(new Integer[0]));
  }

  @Test
  public void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[] { 1, "abc def", 0.3f, 5L };
    String tml = juple.toTML(objs);
    assertTrue(tml.contains("[abc def]"));
    assertTrue(tml.contains("0.3"));
    assertTrue(tml.contains("5"));
    assertEquals("[1[abc def]0.3 5]", tml);
  }

  @Test
  public void testArrayOfPrimitivesAsObjectsWithNullSerialization() throws Exception {
    Object[] objs = new Object[] { 1, "abc def", null, 5L };
    String tml = juple.toTML(objs);
    assertTrue(tml.contains("[abc def]"));
    assertTrue(tml.contains("\\0"));
    assertTrue(tml.contains("5"));
    assertEquals("[1[abc def]\\0 5]", tml);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String tml = "[1 [abc def] 0.3 1.1 5]";
    Object[] objs = juple.fromTML(tml, Object[].class);
    assertEquals(1, (Integer.valueOf((String) objs[0])).intValue());
    assertEquals("abc def", groupStrings((Collection) objs[1]));
    assertEquals(0.3, (Double.valueOf((String) objs[2])).doubleValue(), 1e-15);
    assertEquals(new BigDecimal("1.1"), new BigDecimal(objs[3].toString()));
    assertEquals(5, (Short.valueOf((String) objs[4])).shortValue());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testArrayOfPrimitivesAsObjectsWithNullDeserialization() throws Exception {
    String tml = "[1 [abc def] \\0 1.1 5]";
    Object[] objs = juple.fromTML(tml, Object[].class);
    assertEquals(1, (Integer.valueOf((String) objs[0])).intValue());
    assertEquals("abc def", groupStrings((Collection) objs[1]));
    assertNull(objs[2]);
    assertEquals(new BigDecimal("1.1"), new BigDecimal(objs[3].toString()));
    assertEquals(5, (Short.valueOf((String) objs[4])).shortValue());
  }

  @Test
  public void testObjectArrayWithNonPrimitivesSerialization() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsTML = juple.toTML(classWithObjects);
    String bagOfPrimitivesTML = juple.toTML(bagOfPrimitives);

    Object[] objects = new Object[] { classWithObjects, bagOfPrimitives };
    String tml = juple.toTML(objects);

    assertTrue(tml.contains(classWithObjectsTML));
    assertTrue(tml.contains(bagOfPrimitivesTML));
  }

  @Test
  public void testArrayOfNullObjectsSerialization() {
    Object[] array = new Object[] { null, null };
    String tml = juple.toTML(array);
    assertEquals("[\\0 \\0]", tml);
  }

  @Test
  public void testArrayOfNullObjectsDeserialization() {
    Object[] values = juple.fromTML("[\\0 \\0]", Object[].class);
    assertNull(values[0]);
    assertNull(values[1]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInPrimitiveArrayDeserialization() {
    boolean[] value = juple.fromTML("[ true \\0 false ]", boolean[].class);
    assertTrue(Arrays.equals(new boolean[] { true, true, false }, value));
  }

  @Test(expected = TMLSyntaxException.class)
  public void testNullArrayInPrimitiveArrayDeserialization() {
    boolean[] value = juple.fromTML("[ true \\2 false ]", boolean[].class);
    assertTrue(Arrays.equals(new boolean[] { true, true, false }, value));
  }

  @Test
  public void testPrimitiveBooleanArrayDeserialization() {
    boolean[] value = juple.fromTML("[ true true false ]", boolean[].class);
    assertTrue(Arrays.equals(new boolean[] { true, true, false }, value));
  }

  @Test
  public void testPrimitiveByteArrayDeserialization() {
    byte[] value = juple.fromTML("[ 127 -128 42 ]", byte[].class);
    assertTrue(Arrays.equals(new byte[] { 127, -128, 42 }, value));
  }

  @Test
  public void testPrimitiveCharArrayDeserialization() {
    char[] value = juple.fromTML("[ e f g ]", char[].class);
    assertTrue(Arrays.equals(new char[] { 'e', 'f', 'g' }, value));
  }

  @Test
  public void testPrimitiveCharEscapedArrayDeserialization() {
    char[] value = juple.fromTML("[ \\n \\t \\\\ ]", char[].class);
    assertTrue(Arrays.equals(new char[] { '\n', '\t', '\\' }, value));
  }

  @Test
  public void testPrimitiveDoubleArrayDeserialization() {
    double[] value = juple.fromTML(
        "[ 1.7976931348623157E308 4.9E-324 5.6546568793 ]", double[].class);
    assertArrayEquals(new double[] { Double.MAX_VALUE, Double.MIN_VALUE,
        5.6546568793d }, value, 1e-15);
  }

  @Test
  public void testPrimitiveFloatArrayDeserialization() {
    float[] value = juple.fromTML("[ 3.4028235E38 1.4E-45 3.1415 ]",
        float[].class);
    assertArrayEquals(
        new float[] { Float.MAX_VALUE, Float.MIN_VALUE, 3.1415f }, value,
        1e-15f);
  }

  @Test
  public void testPrimitiveIntArrayDeserialization() {
    int[] value = juple
        .fromTML("[ 2147483647 -2147483648 65482 ]", int[].class);
    assertArrayEquals(
        new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE, 65482 }, value);
  }

  @Test
  public void testPrimitiveLongArrayDeserialization() {
    long[] value = juple.fromTML(
        "[ 9223372036854775807 -9223372036854775808 68165843461654654 ]",
        long[].class);
    assertArrayEquals(new long[] { Long.MAX_VALUE, Long.MIN_VALUE,
        68165843461654654l }, value);
  }

  @Test
  public void testPrimitiveShortArrayDeserialization() {
    short[] value = juple.fromTML("[ 32767 -32768 1523 ]", short[].class);
    assertArrayEquals(new short[] { Short.MAX_VALUE, Short.MIN_VALUE, 1523 },
        value);
  }

  @Test
  public void testMultidimenstionalArraysSerialization() {
    String[][] items = new String[][] {
        { "3m Co", "71.72", "0.02", "0.03", "4/2 12:00am", "Manufacturing" },
        { "Alcoa Inc", "29.01", "0.42", "1.47", "4/1 12:00am", "Manufacturing" } };
    String tml = juple.toTML(items);
    assertTrue(tml.contains("[[[3m Co"));
    assertTrue(tml.contains("Manufacturing]]]"));
  }

  @Test
  public void testMultiDimenstionalObjectArraysSerialization() {
    Object[][] array = new Object[][] { new Object[] { 1, 2 } };
    assertEquals("[[1 2]]", juple.toTML(array));
  }

  @Test
  public void testMixingTypesInObjectArraySerialization() {
    Object[] array = new Object[] { 1, 2, new Object[] { "one", "two", 3 } };
    assertEquals("[1 2[[one][two]3]]", juple.toTML(array));
  }

  @Test
  public void testMultidimenstionalArraysDeserialization() {
    String tml = "[[[3m Co][71.72][0.02][0.03][4/2 12:00am][Manufacturing]][[Alcoa Inc][29.01][0.42][1.47][4/1 12:00am][Manufacturing]]]";
    String[][] items = juple.fromTML(tml, String[][].class);
    assertEquals("3m Co", items[0][0]);
    assertEquals("Manufacturing", items[1][5]);
  }
  
  @Test
  public void testArrayElementsAreArrays() {
    Object[] stringArrays = {
        new String[] {"test1", "test2"},
        new String[] {"test3", "test4"}
    };
    assertEquals("[[[test1][test2]][[test3][test4]]]",
        juple.toTML(stringArrays));
  }

  @SuppressWarnings({ "rawtypes" })
  private String groupStrings(Collection c) {
    StringBuilder sb = new StringBuilder();
    Iterator it = c.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      sb.append((String) o);
      if (it.hasNext()) {
        sb.append(' ');
      }
    }
    return sb.toString();
  }
}
