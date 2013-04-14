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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.TestTypes.ArrayOfObjects;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;

public class PrettyPrintingTest {

  private static final boolean DEBUG = false;

  private Juple juple = new JupleBuilder().setPrettyPrinting().create();

  @Test
  public void testPrettyPrintList() {
    BagOfPrimitives b = new BagOfPrimitives();
    List<BagOfPrimitives> listOfB = new LinkedList<BagOfPrimitives>();
    for (int i = 0; i < 15; ++i) {
      listOfB.add(b);
    }
    Type typeOfSrc = new TMLTypeToken<List<BagOfPrimitives>>() {}.getType();
    String tml = juple.toTML(listOfB, typeOfSrc);
    print(tml);
  }

  @Test
  public void testPrettyPrintArrayOfObjects() {
    ArrayOfObjects target = new ArrayOfObjects();
    String tml = juple.toTML(target);
    print(tml);
  }

  @Test
  public void testPrettyPrintArrayOfPrimitives() {
    int[] ints = new int[] { 1, 2, 3, 4, 5 };
    String tml = juple.toTML(ints);
    assertEquals("[1 2 3 4 5]", tml);
  }

  @Test
  public void testPrettyPrintArrayOfPrimitiveArrays() {
    int[][] ints = new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 },
        { 9, 0 }, { 10 } };
    String tml = juple.toTML(ints);
    String expected = "[\n    [1 2]\n    [3 4]\n    [5 6]\n    [7 8]\n    [9 0]\n    [10]\n]";
    assertEquals(expected, tml);
  }

  @Test
  public void testPrettyPrintListOfPrimitiveArrays() {
    List<Integer[]> list = Arrays.asList(new Integer[][] { { 1, 2 }, { 3, 4 },
        { 5, 6 }, { 7, 8 }, { 9, 0 }, { 10 } });
    String tml = juple.toTML(list);
    String expected = "[\n    [1 2]\n    [3 4]\n    [5 6]\n    [7 8]\n    [9 0]\n    [10]\n]";
    assertEquals(expected, tml);
  }

  @Test
  public void testMap() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", 1);
    map.put("def", 5);
    String tml = juple.toTML(map);
    String expected = "[\n    [abc | 1]\n    [def | 5]\n]";
    assertEquals(expected, tml);
  }

  @Test
  public void testEmptyMapField() {
    ClassWithMap obj = new ClassWithMap();
    obj.map = new LinkedHashMap<String, Integer>();
    String tml = juple.toTML(obj);
    String expected = "[\n    [map | ]\n    [value | 2]\n]";
    assertEquals(expected, tml);
  }

  @Test
  public void testMultipleArrays() {
    int[][][] ints = new int[][][] { { { 1 }, { 2 } } };
    String tml = juple.toTML(ints);
    String expected = "[\n    [\n        [1]\n        [2]\n    ]\n]";
    assertEquals(expected, tml);
  }

  @SuppressWarnings("unused")
  private static class ClassWithMap {
    Map<String, Integer> map;
    int value = 2;
  }

  public void print(String msg) {
    if (DEBUG) System.out.println(msg);
  }
}
