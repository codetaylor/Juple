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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.TestTypes.ArrayOfObjects;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitiveWrappers;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.common.TestTypes.ClassWithArray;
import com.sudoplay.juple.common.TestTypes.ClassWithNoFields;
import com.sudoplay.juple.common.TestTypes.ClassWithObjects;
import com.sudoplay.juple.common.TestTypes.ClassWithTransientFields;
import com.sudoplay.juple.common.TestTypes.Nested;
import com.sudoplay.juple.common.TestTypes.PrimitiveArray;
import com.sudoplay.juple.error.TMLIOException;

/**
 * Functional tests for TML serialization and deserialization of regular
 * classes.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class ObjectTest {

  private Juple juple = new Juple();
  @SuppressWarnings("unused")
  private Juple pretty = new JupleBuilder().setPrettyPrinting().create();
  private static TimeZone oldTimeZone = TimeZone.getDefault();

  @BeforeClass
  public static void initialize() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
  }

  @AfterClass
  public static void cleanup() {
    TimeZone.setDefault(oldTimeZone);
  }

  @Test
  public void testBagOfPrimitivesSerialization() throws Exception {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testBagOfPrimitivesDeserialization() throws Exception {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String tml = src.getExpectedTML();
    BagOfPrimitives target = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals(tml, target.getExpectedTML());
  }

  @Test
  public void testBagOfPrimitiveWrappersSerialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testBagOfPrimitiveWrappersDeserialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String tml = target.getExpectedTML();
    target = juple.fromTML(tml, BagOfPrimitiveWrappers.class);
    assertEquals(tml, target.getExpectedTML());
  }

  @Test
  public void testClassWithTransientFieldsSerialization() throws Exception {
    ClassWithTransientFields<Long> target = new ClassWithTransientFields<Long>(
        1L);
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testClassWithTransientFieldsDeserialization() throws Exception {
    String tml = "[[longValue|1]]";
    ClassWithTransientFields target = juple.fromTML(tml,
        ClassWithTransientFields.class);
    assertEquals(tml, target.getExpectedTML());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInAreIgnored()
      throws Exception {
    String tml = "[[transientLongValue|1][longValue|1]]";
    ClassWithTransientFields target = juple.fromTML(tml,
        ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

  @Test
  public void testClassWithNoFieldsSerialization() throws Exception {
    assertEquals("[]", juple.toTML(new ClassWithNoFields()));
  }

  @Test
  public void testClassWithNoFieldsDeserialization() throws Exception {
    String tml = "[]";
    ClassWithNoFields target = juple.fromTML(tml, ClassWithNoFields.class);
    ClassWithNoFields expected = new ClassWithNoFields();
    assertEquals(expected, target);
  }

  @Test
  public void testNestedSerialization() throws Exception {
    Nested target = new Nested(
        new BagOfPrimitives(10, 20, false, "stringValue"), new BagOfPrimitives(
            30, 40, true, "stringValue"));
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testNestedDeserialization() throws Exception {
    String expected = "[[primitive1|[longValue|10][intValue|20][booleanValue|false][stringValue|stringValue]][primitive2|[longValue|30][intValue|40][booleanValue|true][stringValue|stringValue]]]";
    Nested target = juple.fromTML(expected, Nested.class);
    assertEquals(expected, target.getExpectedTML());
  }

  @Test(expected = NullPointerException.class)
  public void testNullSerializationThrows() throws Exception {
    juple.toTML(null);
  }

  @Test(expected = TMLIOException.class)
  public void testEmptyTMLDeserializationThrows() throws Exception {
    juple.fromTML("", String.class);
  }

  @Test
  public void testEmptyStringDeserialization() throws Exception {
    String s = juple.fromTML("[\\1]", String.class);
    assertEquals("", s);
  }

  @Test(expected = TMLIOException.class)
  public void testTruncatedDeserializationThrows() {
    juple.fromTML("[[a][b ", new TMLTypeToken<List<String>>() {}.getType());
  }

  @Test
  public void testNullDeserialization() throws Exception {
    String myNullObject = null;
    Object object = juple.fromTML(myNullObject, Object.class);
    assertNull(object);
  }

  @Test
  public void testNullFieldsSerialization() throws Exception {
    Nested target = new Nested(
        new BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testNullFieldsDeserialization() throws Exception {
    String tml = "[[primitive1|[longValue|10][intValue|20][booleanValue|false][stringValue|stringValue]][primitive2|\\0]]";
    Nested target = juple.fromTML(tml, Nested.class);
    assertEquals(tml, target.getExpectedTML());
  }

  @Test
  public void testArrayOfObjectsSerialization() throws Exception {
    ArrayOfObjects target = new ArrayOfObjects();
    String expected = "[[elements|[[longValue|0][intValue|2][booleanValue|false][stringValue|i0]][[longValue|1][intValue|3][booleanValue|false][stringValue|i1]][[longValue|2][intValue|4][booleanValue|false][stringValue|i2]]]]";
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testArrayOfObjectsDeserialization() throws Exception {
    String tml = "[[elements|[[longValue|0][intValue|2][booleanValue|false][stringValue|i0]][[longValue|1][intValue|3][booleanValue|false][stringValue|i1]][[longValue|2][intValue|4][booleanValue|false][stringValue|i2]]]]";
    ArrayOfObjects target = juple.fromTML(tml, ArrayOfObjects.class);
    assertEquals(tml, juple.toTML(target));
  }

  @Test
  public void testArrayOfArraysSerialization() throws Exception {
    ArrayOfArrays target = new ArrayOfArrays();
    String expected = "[[elements|[[[longValue|0][intValue|0][booleanValue|false][stringValue|0_0]][[longValue|1][intValue|0][booleanValue|false][stringValue|0_1]]][[[longValue|1][intValue|0][booleanValue|false][stringValue|1_0]][[longValue|2][intValue|1][booleanValue|false][stringValue|1_1]]][[[longValue|2][intValue|0][booleanValue|false][stringValue|2_0]][[longValue|3][intValue|2][booleanValue|false][stringValue|2_1]]]]]";
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testArrayOfArraysDeserialization() throws Exception {
    String tml = "[[elements|[[[longValue|0][intValue|0][booleanValue|false][stringValue|0_0]][[longValue|1][intValue|0][booleanValue|false][stringValue|0_1]]][[[longValue|1][intValue|0][booleanValue|false][stringValue|1_0]][[longValue|2][intValue|1][booleanValue|false][stringValue|1_1]]][[[longValue|2][intValue|0][booleanValue|false][stringValue|2_0]][[longValue|3][intValue|2][booleanValue|false][stringValue|2_1]]]]]";
    ArrayOfArrays target = juple.fromTML(tml, ArrayOfArrays.class);
    assertEquals(tml, juple.toTML(target));
  }

  @Test
  public void testArrayOfObjectsAsFields() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String stringValue = "someStringValueInArray";
    String classWithObjectsJson = juple.toTML(classWithObjects);
    String bagOfPrimitivesJson = juple.toTML(bagOfPrimitives);

    ClassWithArray classWithArray = new ClassWithArray(new Object[] {
        stringValue, classWithObjects, bagOfPrimitives });
    String json = juple.toTML(classWithArray);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
    assertTrue(json.contains("[" + stringValue + "]"));
  }

  @Test
  public void testNullArraysDeserialization() throws Exception {
    String tml = "[[array|\\2]]";
    ClassWithArray target = juple.fromTML(tml, ClassWithArray.class);
    assertNull(target.array);
  }

  @Test
  public void testNullObjectFieldsDeserialization() throws Exception {
    String tml = "[[bag|\\0]]";
    ClassWithObjects target = juple.fromTML(tml, ClassWithObjects.class);
    assertNull(target.bag);
  }

  @Test
  public void testEmptyCollectionInAnObjectSerialization() throws Exception {
    String expected = "[[children|]]";
    ClassWithCollectionField target = new ClassWithCollectionField();
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testEmptyCollectionInAnObjectDeserialization() throws Exception {
    String tml = "[[children|]]";
    ClassWithCollectionField target = juple.fromTML(tml,
        ClassWithCollectionField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  @Test
  public void testEmptyArrayInAnObjectSerialization() throws Exception {
    String expected = "[[children|]]";
    ClassWithArrayField target = new ClassWithArrayField();
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testEmptyArrayInAnObjectDeserialization() throws Exception {
    String tml = "[[children|]]";
    ClassWithArrayField target = juple.fromTML(tml, ClassWithArrayField.class);
    assertNotNull(target);
    assertTrue(target.children.length == 0);
  }

  @Test
  public void testEmptyMapInAnObjectSerialization() throws Exception {
    String expected = "[[children|]]";
    ClassWithMapField target = new ClassWithMapField();
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testEmptyMapInAnObjectDeserialization() throws Exception {
    String tml = "[[children|]]";
    ClassWithMapField target = juple.fromTML(tml, ClassWithMapField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  @Test
  public void testPrimitiveArrayInAnObjectDeserialization() throws Exception {
    long[] expected = new long[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    String tml = "[[longArray|0 1 2 3 4 5 6 7 8 9]]";
    PrimitiveArray target = juple.fromTML(tml, PrimitiveArray.class);
    assertArrayEquals(expected, target.getArray());
  }

  @Test
  public void testNullPrimitiveFieldsDeserialization() throws Exception {
    String tml = "[[longValue|\\0]]";
    BagOfPrimitives target = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals(BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

  @Test
  public void testPrivateNoArgConstructorDeserialization() throws Exception {
    ClassWithPrivateNoArgsConstructor target = juple.fromTML("[[a|20]]",
        ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

  @Test
  public void testAnonymousLocalClassesSerialization() throws Exception {
    assertEquals("[\\0]", juple.toTML(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  @Test
  public void testPrimitiveArrayFieldSerialization() {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals("[[longArray|1 2 3]]", juple.toTML(target));
  }

  @Test
  public void testClassWithObjectFieldSerialization() {
    ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    String tml = juple.toTML(obj);
    assertTrue(tml.contains("abc"));
  }

  @Test
  public void testInnerClassSerialization() {
    Parent p = new Parent();
    Parent.Child c = p.new Child();
    String tml = juple.toTML(c);
    assertTrue(tml.contains("value2"));
    assertFalse(tml.contains("value1"));
  }

  @Test
  public void testStringFieldWithNumberValueDeserialization() {
    String tml = "[[stringValue|1]]";
    BagOfPrimitives bag = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals("1", bag.stringValue);

    tml = "[[stringValue|1.5E+6]]";
    bag = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals("1.5E+6", bag.stringValue);

    tml = "[[stringValue|true]]";
    bag = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals("true", bag.stringValue);
  }

  @Test
  public void testStringFieldWithEmptyValueSerialization() {
    ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    String tml = juple.toTML(target);
    assertTrue(tml.contains("[a|5794749]"));
    assertTrue(tml.contains("[b|\\1]"));
    assertTrue(tml.contains("[c|\\1]"));
  }

  @Test
  public void testSingletonLists() {
    Product product = new Product();
    assertEquals("[[attributes|][departments|]]", juple.toTML(product));
    juple.fromTML(juple.toTML(product), Product.class);

    product.departments.add(new Department());
    assertEquals(
        "[[attributes|][departments|[[name|abc][code|123]]]]",
        juple.toTML(product));
    juple.fromTML(juple.toTML(product), Product.class);

    product.attributes.add("456");
    assertEquals(
        "[[attributes|[456]][departments|[[name|abc][code|123]]]]",
        juple.toTML(product));
    juple.fromTML(juple.toTML(product), Product.class);
  }
  
  @Test
  public void testDateAsMapObjectFieldSerialization() {
    HasObjectMap a = new HasObjectMap();
    a.map.put("date", new Date(0));
    assertEquals("[[map|[date|Dec 31, 1969 4:00:00 PM]]]", juple.toTML(a));
  }

  public class HasObjectMap {
    Map<String, Object> map = new HashMap<String, Object>();
  }

  static final class Department {
    public String name = "abc";
    public String code = "123";
  }

  static final class Product {
    private List<String> attributes = new ArrayList<String>();
    private List<Department> departments = new ArrayList<Department>();
  }

  @SuppressWarnings("unused")
  private static class ClassWithEmptyStringFields {
    String a = "";
    String b = "";
    String c = "";
  }

  @SuppressWarnings("unused")
  private static class Parent {
    int value1 = 1;

    private class Child {
      int value2 = 2;
    }
  }

  private static class ClassWithObjectField {
    @SuppressWarnings("unused")
    Object member;
  }

  private static class ClassWithPrivateNoArgsConstructor {
    public int a;

    private ClassWithPrivateNoArgsConstructor() {
      a = 10;
    }
  }

  private static class ClassWithCollectionField {
    Collection<String> children = new ArrayList<String>();
  }

  private static class ClassWithArrayField {
    String[] children = new String[0];
  }

  private static class ClassWithMapField {
    Map<String, String> children = new HashMap<String, String>();
  }

  private static class ArrayOfArrays {
    private final BagOfPrimitives[][] elements;

    public ArrayOfArrays() {
      elements = new BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new BagOfPrimitives(i + j, i * j, false, i + "_" + j);
        }
      }
    }
  }

}
