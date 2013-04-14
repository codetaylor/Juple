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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.common.MoreAsserts;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class CollectionTest {

  private Juple juple = new Juple();

  @Test
  public void testTopLevelCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type targetType = new TMLTypeToken<Collection<Integer>>() {}.getType();
    String tml = juple.toTML(target, targetType);
    assertEquals("[1 2 3 4 5 6 7 8 9]", tml);
  }

  @Test
  public void testTopLevelCollectionOfIntegersDeserialization() {
    String tml = "[0 1 2 3 4 5 6 7 8 9]";
    Type collectionType = new TMLTypeToken<Collection<Integer>>() {}.getType();
    Collection<Integer> target = juple.fromTML(tml, collectionType);
    int[] expected = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    MoreAsserts.assertEquals(expected, toIntArray(target));
  }

  @Test
  public void testTopLevelListOfIntegerCollectionsDeserialization()
      throws Exception {
    String tml = "[[1 2 3][4 5 6][7 8 9]]";
    Type collectionType = new TMLTypeToken<Collection<Collection<Integer>>>() {}
        .getType();
    List<Collection<Integer>> target = juple.fromTML(tml, collectionType);
    int[][] expected = new int[3][3];
    for (int i = 0; i < 3; ++i) {
      int start = (3 * i) + 1;
      for (int j = 0; j < 3; ++j) {
        expected[i][j] = start + j;
      }
    }

    for (int i = 0; i < 3; i++) {
      MoreAsserts.assertEquals(expected[i], toIntArray(target.get(i)));
    }
  }

  @Test
  public void testLinkedListSerialization() {
    List<String> list = new LinkedList<String>();
    list.add("a1");
    list.add("a2");
    Type linkedListType = new TMLTypeToken<LinkedList<String>>() {}.getType();
    String tml = juple.toTML(list, linkedListType);
    assertTrue(tml.contains("a1"));
    assertTrue(tml.contains("a2"));
  }

  @Test
  public void testLinkedListDeserialization() {
    String tml = "[[a1][a2]]";
    Type linkedListType = new TMLTypeToken<LinkedList<String>>() {}.getType();
    List<String> list = juple.fromTML(tml, linkedListType);
    assertEquals("a1", list.get(0));
    assertEquals("a2", list.get(1));
  }

  @Test
  public void testQueueSerialization() {
    Queue<String> queue = new LinkedList<String>();
    queue.add("a1");
    queue.add("a2");
    Type queueType = new TMLTypeToken<Queue<String>>() {}.getType();
    String tml = juple.toTML(queue, queueType);
    assertTrue(tml.contains("a1"));
    assertTrue(tml.contains("a2"));
  }

  @Test
  public void testQueueDeserialization() {
    String tml = "[[a1][a2]]";
    Type queueType = new TMLTypeToken<Queue<String>>() {}.getType();
    Queue<String> queue = juple.fromTML(tml, queueType);
    assertEquals("a1", queue.element());
    queue.remove();
    assertEquals("a2", queue.element());
  }

  @Test
  public void testNullsInListSerialization() {
    List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    String expected = "[[foo][\\0][bar]]";
    Type typeOfList = new TMLTypeToken<List<String>>() {}.getType();
    String tml = juple.toTML(list, typeOfList);
    assertEquals(expected, tml);
  }

  @Test
  public void testNullsInListDeserialization() {
    List<String> expected = new ArrayList<String>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    String tml = "[[foo][\\0][bar]]";
    Type expectedType = new TMLTypeToken<List<String>>() {}.getType();
    List<String> target = juple.fromTML(tml, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertEquals(expected.get(i), target.get(i));
    }
  }

  @Test
  public void testCollectionOfObjectSerialization() {
    List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add("World");
    assertEquals("[[Hello][World]]", juple.toTML(target));

    Type type = new TMLTypeToken<List<Object>>() {}.getType();
    assertEquals("[[Hello][World]]", juple.toTML(target, type));
  }

  @Test
  public void testCollectionOfObjectWithNullSerialization() {
    List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add(null);
    target.add("World");
    assertEquals("[[Hello]\\0[World]]", juple.toTML(target));

    Type type = new TMLTypeToken<List<Object>>() {}.getType();
    assertEquals("[[Hello]\\0[World]]", juple.toTML(target, type));
  }

  @Test
  public void testCollectionOfStringsSerialization() {
    List<String> target = new ArrayList<String>();
    target.add("Hello");
    target.add("World");
    assertEquals("[[Hello][World]]", juple.toTML(target));
  }

  @Test
  public void testCollectionOfBagOfPrimitivesSerialization() {
    List<BagOfPrimitives> target = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    String result = juple.toTML(target);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    for (BagOfPrimitives obj : target) {
      assertTrue(result.contains(obj.getExpectedTML()));
    }
  }

  @Test
  public void testCollectionOfStringsDeserialization() {
    String tml = "[[Hello][World]]";
    Type collectionType = new TMLTypeToken<Collection<String>>() {}.getType();
    Collection<String> target = juple.fromTML(tml, collectionType);

    assertTrue(target.contains("Hello"));
    assertTrue(target.contains("World"));
  }

  @Test
  public void testRawCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals("[1 2 3 4 5 6 7 8 9]", juple.toTML(target));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testRawCollectionSerialization() {
    BagOfPrimitives bag1 = new BagOfPrimitives();
    Collection target = Arrays.asList(bag1, bag1);
    String tml = juple.toTML(target);
    assertTrue(tml.contains(bag1.getExpectedTML()));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testRawCollectionDeserializationNotAllowed() {
    String tml = "[0 1 2 3 4 5 6 7 8 9]";
    Collection integers = juple.fromTML(tml, Collection.class);

    // everything is read raw as strings
    MoreAsserts.assertContains(
        Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
        integers);

    // raw strings are returned as lists of lists of strings
    tml = "[[Hello][World]]";
    Collection strings = juple.fromTML(tml, Collection.class);
    Iterator it = strings.iterator();
    ArrayList list = new ArrayList();
    while (it.hasNext()) {
      String s = (String) groupStrings((Collection) it.next());
      list.add(s);
    }
    assertTrue(list.contains("Hello"));
    assertTrue(list.contains("World"));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
    String tml = '[' + bag.getExpectedTML() + bag.getExpectedTML() + ']';
    ArrayList list = juple.fromTML(tml, ArrayList.class);
    String expected = "[[longValue, 10], [intValue, 20], [booleanValue, false], [stringValue, stringValue]]";
    assertEquals(expected, list.get(0).toString());
    assertEquals(expected, list.get(1).toString());
  }

  @Test
  public void testWildcardPrimitiveCollectionSerilaization() throws Exception {
    Collection<? extends Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7,
        8, 9);
    Type collectionType = new TMLTypeToken<Collection<? extends Integer>>() {}
        .getType();
    String tml = juple.toTML(target, collectionType);
    assertEquals("[1 2 3 4 5 6 7 8 9]", tml);

    tml = juple.toTML(target);
    assertEquals("[1 2 3 4 5 6 7 8 9]", tml);
  }

  @Test
  public void testWildcardPrimitiveCollectionDeserilaization() throws Exception {
    String tml = "[1 2 3 4 5 6 7 8 9]";
    Type collectionType = new TMLTypeToken<Collection<? extends Integer>>() {}
        .getType();
    Collection<? extends Integer> target = juple.fromTML(tml, collectionType);
    assertEquals(9, target.size());
    assertTrue(target.contains(1));
    assertTrue(target.contains(9));
  }

  @Test
  public void testWildcardCollectionField() throws Exception {
    Collection<BagOfPrimitives> collection = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    collection.add(objA);
    collection.add(objB);

    ObjectWithWildcardCollection target = new ObjectWithWildcardCollection(
        collection);
    String tml = juple.toTML(target);
    assertTrue(tml.contains(objA.getExpectedTML()));
    assertTrue(tml.contains(objB.getExpectedTML()));

    target = juple.fromTML(tml, ObjectWithWildcardCollection.class);
    Collection<? extends BagOfPrimitives> deserializedCollection = target
        .getCollection();
    assertEquals(2, deserializedCollection.size());
    assertTrue(deserializedCollection.contains(objA));
    assertTrue(deserializedCollection.contains(objB));
  }

  @Test
  public void testFieldIsArrayList() {
    HasArrayListField object = new HasArrayListField();
    object.longs.add(1L);
    object.longs.add(3L);
    String tml = juple.toTML(object, HasArrayListField.class);
    assertEquals("[[longs|1 3]]", tml);
    HasArrayListField copy = juple.fromTML("[[longs|1 3]]",
        HasArrayListField.class);
    assertEquals(Arrays.asList(1L, 3L), copy.longs);
  }

  @Test
  public void testSetSerialization() {
    Set<Entry> set = new HashSet<Entry>();
    set.add(new Entry(1));
    set.add(new Entry(2));
    String json = juple.toTML(set);
    assertTrue(json.contains("1"));
    assertTrue(json.contains("2"));
  }

  @Test
  public void testSetDeserialization() {
    String json = "[[[value|1]][[value|2]]]";
    Type type = new TMLTypeToken<Set<Entry>>() {}.getType();
    Set<Entry> set = juple.fromTML(json, type);
    assertEquals(2, set.size());
    for (Entry entry : set) {
      assertTrue(entry.value == 1 || entry.value == 2);
    }
  }

  @Test
  public void testUserCollectionTypeAdapter() {
    Type listOfString = new TMLTypeToken<List<String>>() {}.getType();
    Object stringListSerializer = new TMLTypeAdapter<List<String>>() {

      @Override
      public List<String> read(TMLReader in) throws IOException {
        List<String> list = new ArrayList<String>();
        int scope = in.getScope();
        while (in.hasNextInScope(scope)) {
          in.beginList();
          list.add(in.nextString());
          in.endList();
        }
        return list;
      }

      @Override
      public void write(TMLWriter out, List<String> value) throws IOException {
        for (String s : value) {
          out.beginList();
          out.value(s);
          out.endList();
        }
      }
    };
    Juple juple = new JupleBuilder().registerTypeAdapter(listOfString,
        stringListSerializer).create();

    assertEquals("[[ab][cd]]",
        juple.toTML(Arrays.asList("ab", "cd"), listOfString));

    assertEquals(Arrays.asList("ab", "cd"),
        juple.fromTML("[[ab][cd]]", listOfString));
  }

  private static class ObjectWithWildcardCollection {
    private final Collection<? extends BagOfPrimitives> collection;

    public ObjectWithWildcardCollection(
        Collection<? extends BagOfPrimitives> collection) {
      this.collection = collection;
    }

    public Collection<? extends BagOfPrimitives> getCollection() {
      return collection;
    }
  }

  private static class Entry {
    int value;

    Entry(int value) {
      this.value = value;
    }
  }

  static class HasArrayListField {
    ArrayList<Long> longs = new ArrayList<Long>();
  }

  @SuppressWarnings("rawtypes")
  private static int[] toIntArray(Collection collection) {
    int[] ints = new int[collection.size()];
    int i = 0;
    for (Iterator iterator = collection.iterator(); iterator.hasNext(); ++i) {
      Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = ((Integer) obj).intValue();
      } else if (obj instanceof Long) {
        ints[i] = ((Long) obj).intValue();
      }
    }
    return ints;
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
