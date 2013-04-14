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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLInstanceCreator;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.TestTypes;
import com.sudoplay.juple.error.TMLException;
import com.sudoplay.juple.error.TMLSyntaxException;

/**
 * Functional test for TML serialization and deserialization for Maps
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class MapTest {

  private Juple juple = new Juple();

  @Test
  public void testMapSerialization() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    String tml = juple.toTML(map, typeOfMap);
    assertEquals("[[a|1][b|2]]", tml);
  }

  @Test
  public void testMapDeserialization() {
    String tml = "[[a|1][b|2]]";
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> target = juple.fromTML(tml, typeOfMap);
    assertEquals(1, target.get("a").intValue());
    assertEquals(2, target.get("b").intValue());
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testRawMapSerialization() {
    Map map = new LinkedHashMap();
    map.put("a", 1);
    map.put("b", "string with spaces");
    String tml = juple.toTML(map);
    assertEquals("[[a|1][b|string with spaces]]", tml);
  }

  @Test
  public void testMapSerializationEmpty() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    String tml = juple.toTML(map, typeOfMap);
    assertEquals("[]", tml);
  }

  @Test
  public void testMapDeserializationEmpty() {
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = juple.fromTML("[]", typeOfMap);
    assertTrue(map.isEmpty());
  }

  @Test
  public void testMapSerializationWithNullValue() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", null);
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    String tml = juple.toTML(map, typeOfMap);
    assertEquals("[[abc|\\0]]", tml);
  }

  @Test
  public void testSerializeComplexMapWithTypeAdapter() {
    Type type = new TMLTypeToken<Map<Point, String>>() {}.getType();

    Map<Point, String> original = new LinkedHashMap<Point, String>();
    original.put(new Point(5, 5), "a");
    original.put(new Point(8, 8), "b");
    String tml = juple.toTML(original, type);
    assertEquals("[[[[x|5][y|5]]|a][[[x|8][y|8]]|b]]", tml);
    assertEquals(original, juple.<Map<Point, String>> fromTML(tml, type));

    // test that registering a type adapter for one map doesn't interfere with
    // others
    Map<String, Boolean> otherMap = new LinkedHashMap<String, Boolean>();
    otherMap.put("t", true);
    otherMap.put("f", false);
    assertEquals("[[t|true][f|false]]", juple.toTML(otherMap, Map.class));
    assertEquals(
        "[[t|true][f|false]]",
        juple.toTML(otherMap,
            new TMLTypeToken<Map<String, Boolean>>() {}.getType()));
    assertEquals(otherMap, juple.<Object> fromTML("[[t|true][f|false]]",
        new TMLTypeToken<Map<String, Boolean>>() {}.getType()));
  }

  @Test
  public void testMapDeserializationWithNullValue() {
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = juple.fromTML("[[abc|\\0]]", typeOfMap);
    assertEquals(1, map.size());
    assertNull(map.get("abc"));
  }

  @Test(expected = TMLException.class)
  public void testMapSerializationWithNullKey() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put(null, 123);
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    juple.toTML(map, typeOfMap);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testMapDeserializationWithNullKey() {
    Type typeOfMap = new TMLTypeToken<Map<String, Integer>>() {}.getType();
    juple.fromTML("[[\\0|123]]", typeOfMap);
  }

  @Test
  public void testMapSerializationWithIntegerKeys() {
    Map<Integer, String> map = new LinkedHashMap<Integer, String>();
    map.put(123, "456");
    Type typeOfMap = new TMLTypeToken<Map<Integer, String>>() {}.getType();
    String json = juple.toTML(map, typeOfMap);
    assertEquals("[[123|456]]", json);
  }

  @Test
  public void testMapDeserializationWithIntegerKeys() {
    Type typeOfMap = new TMLTypeToken<Map<Integer, String>>() {}.getType();
    Map<Integer, String> map = juple.fromTML("[[123|456]]", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  @Test
  public void testHashMapDeserialization() throws Exception {
    Type typeOfMap = new TMLTypeToken<HashMap<Integer, String>>() {}.getType();
    HashMap<Integer, String> map = juple.fromTML("[[123|456]]", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  @Test
  public void testSortedMap() throws Exception {
    Type typeOfMap = new TMLTypeToken<SortedMap<Integer, String>>() {}
        .getType();
    SortedMap<Integer, String> map = juple.fromTML("[[123|456]]", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

  @Test
  public void testParameterizedMapSubclassSerialization() {
    MyParameterizedMap<String, String> map = new MyParameterizedMap<String, String>(
        10);
    map.put("a", "b");
    Type type = new TMLTypeToken<MyParameterizedMap<String, String>>() {}
        .getType();
    String tml = juple.toTML(map, type);
    assertTrue(tml.contains("[a|b]"));
  }

  @Test
  public void testMapSubclassSerialization() {
    MyMap map = new MyMap();
    map.put("a", "b");
    String tml = juple.toTML(map, MyMap.class);
    assertTrue(tml.contains("[a|b]"));
  }

  @Test
  public void testMapStandardSubclassDeserialization() {
    String tml = "[[a|1][b|2]]";
    Type type = new TMLTypeToken<LinkedHashMap<String, String>>() {}.getType();
    LinkedHashMap<String, Integer> map = juple.fromTML(tml, type);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  @Test
  public void testMapSubclassDeserialization() {
    Juple juple = new JupleBuilder().registerTypeAdapter(MyMap.class,
        new TMLInstanceCreator<MyMap>() {
          public MyMap createInstance(Type type) {
            return new MyMap();
          }
        }).create();
    String tml = "[[a|1][b|2]]";
    MyMap map = juple.fromTML(tml, MyMap.class);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

  @Test
  public void testMapSerializationWithNullValues() {
    ClassWithAMap target = new ClassWithAMap();
    target.map.put("name1", null);
    target.map.put("name2", "value2");
    String tml = juple.toTML(target);
    assertEquals("[[map|[name1|\\0][name2|value2]]]", tml);
  }

  @Test
  public void testMapSerializationWithWildcardValues() {
    Map<String, ? extends Collection<? extends Integer>> map = new LinkedHashMap<String, Collection<Integer>>();
    map.put("test", null);
    Type typeOfMap = new TMLTypeToken<Map<String, ? extends Collection<? extends Integer>>>() {}
        .getType();
    String tml = juple.toTML(map, typeOfMap);
    assertEquals("[[test|[\\2]]]", tml);
  }

  @Test
  public void testMapDeserializationWithWildcardValues() {
    Type typeOfMap = new TMLTypeToken<Map<String, ? extends Long>>() {}
        .getType();
    Map<String, ? extends Long> map = juple.fromTML("[[test|123]]", typeOfMap);
    assertEquals(1, map.size());
    assertEquals(new Long(123L), map.get("test"));
  }

  @Test
  public void testMapDeserializationWithWildcardValuesWithNullCollection() {
    Type typeOfMap = new TMLTypeToken<Map<String, ? extends Collection<? extends Integer>>>() {}
        .getType();
    Map<String, ? extends Long> map = juple
        .fromTML("[[test|[\\2]]]", typeOfMap);
    assertEquals(1, map.size());
    assertNull(map.get("test"));
  }

  @Test
  public void testMapOfMapSerialization() {
    Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
    Map<String, String> nestedMap = new HashMap<String, String>();
    nestedMap.put("1", "1");
    nestedMap.put("2", "2");
    map.put("nestedMap", nestedMap);
    String tml = juple.toTML(map);
    assertEquals("[[nestedMap|[[2|2][1|1]]]]", tml);
  }

  @Test
  public void testMapOfMapDeserialization() {
    String tml = "[[nestedMap|[[2|2][1|1]]]]";
    Type type = new TMLTypeToken<Map<String, Map<String, String>>>() {}
        .getType();
    Map<String, Map<String, String>> map = juple.fromTML(tml, type);
    Map<String, String> nested = map.get("nestedMap");
    assertEquals("1", nested.get("1"));
    assertEquals("2", nested.get("2"));
  }

  @Test
  public void testMapWithQuotes() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("a\"b", "c\"d");
    String tml = juple.toTML(map);
    assertEquals("[[a\"b|c\"d]]", tml);
  }

  @Test
  public void testSerializeMapsWithEmptyStringKey() {
    Map<String, Boolean> map = new HashMap<String, Boolean>();
    map.put("", true);
    assertEquals("[[\\1|true]]", juple.toTML(map));
  }

  @Test
  public void testDeserializeMapsWithEmptyStringKey() {
    Map<String, Boolean> map = juple.fromTML("[[\\1|true]]",
        new TMLTypeToken<Map<String, Boolean>>() {}.getType());
    assertEquals(Boolean.TRUE, map.get(""));
  }

  @Test
  public void testSerializeMaps() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", 12);
    map.put("b", null);

    LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
    innerMap.put("test", 1);
    innerMap.put("TestStringArray", new String[] { "one", "two" });
    map.put("c", innerMap);

    assertEquals("[[a|12][b|\\0][c|[[test|1][TestStringArray|[[one][two]]]]]]",
        new JupleBuilder().create().toTML(map));
    assertEquals("[\n" + "    [a | 12]\n" + "    [b | \\0]\n" + "    [c | \n"
        + "        [\n" + "            [test | 1]\n"
        + "            [TestStringArray | \n" + "                [\n"
        + "                    [one]\n" + "                    [two]\n"
        + "                ]\n" + "            ]\n" + "        ]\n" + "    ]\n"
        + "]", new JupleBuilder().setPrettyPrinting().create().toTML(map));

    innerMap.put("d", "e");
    assertEquals(
        "[[a|12][b|\\0][c|[[test|1][TestStringArray|[[one][two]]][d|e]]]]",
        new Juple().toTML(map));
  }

  @Test
  public final void testInterfaceTypeMapSerialization() {
    MapClass element = new MapClass();
    TestTypes.Sub subType = new TestTypes.Sub();
    element.addBase("Test", subType);
    element.addSub("Test", subType);

    String expected = "[[bases|[Test|[[subName|Sub][baseName|Base][serializerName|\\0]]]][subs|[Test|[[subName|Sub][baseName|Base][serializerName|\\0]]]]]";

    String tml = juple.toTML(element);
    assertEquals(expected, tml);
  }

  @Test
  public void testGeneralMapFieldSerialization() throws Exception {
    MapWithGeneralMapParameters map = new MapWithGeneralMapParameters();
    map.map.put("string", "testString");
    map.map.put("stringArray", new String[] { "one", "two" });
    map.map.put("objectArray", new Object[] { 1, 2L, "three" });

    String expected = "[[map|[string|testString][stringArray|[[one][two]]][objectArray|[1 2[three]]]]]";
    assertEquals(expected, juple.toTML(map));
  }

  @Test
  public void testStringKeyDeserialization() {
    String tml = "[[2,3|a][5,7|b]]";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("2,3", "a");
    map.put("5,7", "b");
    assertEquals(map, juple.fromTML(tml,
        new TMLTypeToken<Map<String, String>>() {}.getType()));
  }

  @Test
  public void testNumberKeyDeserialization() {
    String tml = "[[2.3|a][5.7|b]]";
    Map<Double, String> map = new LinkedHashMap<Double, String>();
    map.put(2.3, "a");
    map.put(5.7, "b");
    assertEquals(map, juple.fromTML(tml,
        new TMLTypeToken<Map<Double, String>>() {}.getType()));
  }

  @Test
  public void testBooleanKeyDeserialization() {
    String tml = "[[true|a][false|b]]";
    Map<Boolean, String> map = new LinkedHashMap<Boolean, String>();
    map.put(true, "a");
    map.put(false, "b");
    assertEquals(
        map,
        juple.fromTML(tml,
            new TMLTypeToken<Map<Boolean, String>>() {}.getType()));
  }

  @Test(expected = TMLSyntaxException.class)
  public void testMapDeserializationWithDuplicateKeys() {
    juple.fromTML("[[a|1][a|2]]",
        new TMLTypeToken<Map<String, Integer>>() {}.getType());
  }

  @Test
  public void testSerializeMapOfMaps() {
    Type type = new TMLTypeToken<Map<String, Map<String, String>>>() {}
        .getType();
    Map<String, Map<String, String>> map = newMap("a",
        newMap("ka1", "va1", "ka2", "va2"), "b",
        newMap("kb1", "vb1", "kb2", "vb2"));
    assertEquals("[[a|[[ka1|va1][ka2|va2]]][b|[[kb1|vb1][kb2|vb2]]]]",
        juple.toTML(map, type));
  }

  @Test
  public void testDeerializeMapOfMaps() {
    Type type = new TMLTypeToken<Map<String, Map<String, String>>>() {}
        .getType();
    Map<String, Map<String, String>> map = newMap("a",
        newMap("ka1", "va1", "ka2", "va2"), "b",
        newMap("kb1", "vb1", "kb2", "vb2"));
    String tml = "[[a|[[ka1|va1][ka2|va2]]][b|[[kb1|vb1][kb2|vb2]]]]";
    assertEquals(map, juple.fromTML(tml, type));
  }

  @Test
  public void testTwoTypesCollapseToOneSerialize() {
    Map<Number, String> original = new LinkedHashMap<Number, String>();
    original.put(new Double(1.0), "a");
    original.put(new Float(1.0), "b");
    String tml = juple.toTML(original,
        new TMLTypeToken<Map<Number, String>>() {}.getType());
    assertEquals("[[1.0|a][1.0|b]]", tml);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializeDuplicateKeyThrows() {
    String s = "[[1.00|a][1.0|b]]";
    juple.fromTML(s, new TMLTypeToken<Map<Double, String>>() {}.getType());
  }

  @Test
  public void testMapWithTypeVariableSerialization() {
    PointWithProperty<Point> map = new PointWithProperty<Point>();
    map.map.put(new Point(2, 3), new Point(4, 5));
    Type type = new TMLTypeToken<PointWithProperty<Point>>() {}.getType();
    String tml = juple.toTML(map, type);
    assertEquals("[[map|[[[x|2][y|3]]|[[x|4][y|5]]]]]", tml);
  }

  static class Point {
    int x;
    int y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    Point() {}

    @Override
    public boolean equals(Object o) {
      return o instanceof Point && ((Point) o).x == x && ((Point) o).y == y;
    }

    @Override
    public int hashCode() {
      return x * 37 + y;
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class PointWithProperty<T> {
    Map<Point, T> map = new HashMap<Point, T>();
  }

  static final class MapClass {
    private final Map<String, TestTypes.Base> bases = new HashMap<String, TestTypes.Base>();
    private final Map<String, TestTypes.Sub> subs = new HashMap<String, TestTypes.Sub>();

    public final void addBase(String name, TestTypes.Base value) {
      bases.put(name, value);
    }

    public final void addSub(String name, TestTypes.Sub value) {
      subs.put(name, value);
    }
  }

  static final class MapWithGeneralMapParameters {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    final Map<String, Object> map = new LinkedHashMap();
  }

  @SuppressWarnings({ "unused", "serial" })
  private static class MyParameterizedMap<K, V> extends LinkedHashMap<K, V> {
    final int foo;

    MyParameterizedMap(int foo) {
      this.foo = foo;
    }
  }

  private static class MyMap extends LinkedHashMap<String, String> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    int foo = 10;
  }

  private static class ClassWithAMap {
    Map<String, String> map = new TreeMap<String, String>();
  }

  private <K, V> Map<K, V> newMap(K key1, V value1, K key2, V value2) {
    Map<K, V> result = new LinkedHashMap<K, V>();
    result.put(key1, value1);
    result.put(key2, value2);
    return result;
  }
}
