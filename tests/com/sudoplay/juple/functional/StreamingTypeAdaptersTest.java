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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class StreamingTypeAdaptersTest {

  private Juple miniJuple = new Juple();
  private TMLTypeAdapter<Truck> truckAdapter = miniJuple
      .getAdapter(Truck.class);
  private TMLTypeAdapter<Map<String, Double>> mapAdapter = miniJuple
      .getAdapter(new TMLTypeToken<Map<String, Double>>() {});

  @Test
  public void testSerialize() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person(
        "Jodie", 29));
    truck.horsePower = 300;

    assertEquals(
        "[[horsePower|300.0][passengers|[[age|29][name|Jesse]][[age|29][name|Jodie]]]]",
        toTML(truckAdapter, truck));
  }

  @Test
  public void testDeserialize() throws IOException {
    String tml = "[[horsePower|300.0][passengers|[[age|29][name|Jesse]][[age|29][name|Jodie]]]]";
    Truck truck = fromTML(truckAdapter, tml);
    assertEquals(300.0, truck.horsePower, 1e-15);
    assertEquals(
        Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29)),
        truck.passengers);
  }

  @Test
  public void testSerializeNullField() throws IOException {
    Truck truck = new Truck();
    truck.passengers = null;
    assertEquals("[[horsePower|0.0][passengers|\\2]]",
        toTML(truckAdapter, truck));
  }

  @Test
  public void testDeserializeNullField() throws IOException {
    Truck truck = fromTML(truckAdapter, "[[horsePower|0.0][passengers|\\2]]");
    assertNull(truck.passengers);
  }

  @Test
  public void testSerializeNullObject() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList((Person) null);
    assertEquals("[[horsePower|0.0][passengers|[\\0]]]",
        toTML(truckAdapter, truck));
  }

  @Test
  public void testDeserializeNullObject() throws IOException {
    Truck truck = fromTML(truckAdapter, "[[horsePower|0.0][passengers|[\\0]]]");
    assertEquals(Arrays.asList((Person) null), truck.passengers);
  }

  @Test
  public void testSerializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person(
        "Jodie", 29));
    assertEquals("[[horsePower|0.0][passengers|Jesse Jodie]]",
        toTML(truckAdapter, truck));
  }

  @Test
  public void testDeserializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = fromTML(truckAdapter,
        "[[horsePower|0.0][passengers|Jesse Jodie]]");
    assertEquals(
        Arrays.asList(new Person("Jesse", -1), new Person("Jodie", -1)),
        truck.passengers);
  }

  @Test
  public void testSerializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals("[[a|5.0][b|10.0]]", toTML(mapAdapter, map));
  }

  @Test
  public void testDeserializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals(map, fromTML(mapAdapter, "[[a|5.0][b|10.0]]"));
  }

  @Test
  public void testSerialize1dArray() throws IOException {
    TMLTypeAdapter<double[]> arrayAdapter = miniJuple
        .getAdapter(new TMLTypeToken<double[]>() {});
    assertEquals("[1.0 2.0 3.0]",
        toTML(arrayAdapter, new double[] { 1.0, 2.0, 3.0 }));
  }

  @Test
  public void testDeserialize1dArray() throws IOException {
    TMLTypeAdapter<double[]> arrayAdapter = miniJuple
        .getAdapter(new TMLTypeToken<double[]>() {});
    double[] array = fromTML(arrayAdapter, "[1.0 2.0 3.0]");
    assertTrue(Arrays.toString(array),
        Arrays.equals(new double[] { 1.0, 2.0, 3.0 }, array));
  }

  @Test
  public void testSerialize2dArray() throws IOException {
    TMLTypeAdapter<double[][]> arrayAdapter = miniJuple
        .getAdapter(new TMLTypeToken<double[][]>() {});
    double[][] array = { { 1.0, 2.0 }, { 3.0 } };
    assertEquals("[[1.0 2.0][3.0]]", toTML(arrayAdapter, array));
  }

  @Test
  public void testDeserialize2dArray() throws IOException {
    TMLTypeAdapter<double[][]> arrayAdapter = miniJuple
        .getAdapter(new TMLTypeToken<double[][]>() {});
    double[][] array = fromTML(arrayAdapter, "[[1.0 2.0][3.0]]");
    double[][] expected = { { 1.0, 2.0 }, { 3.0 } };
    assertTrue(Arrays.toString(array), Arrays.deepEquals(expected, array));
  }

  @Test
  public void testSerializeRecursive() throws IOException {
    TMLTypeAdapter<Node> nodeAdapter = miniJuple.getAdapter(Node.class);
    Node root = new Node("root");
    root.left = new Node("left");
    root.right = new Node("right");
    assertEquals(
        "[[label|root][left|[label|left][left|\\0][right|\\0]][right|[label|right][left|\\0][right|\\0]]]",
        toTML(nodeAdapter, root));
  }

  private void usePersonNameAdapter() {
    TMLTypeAdapter<Person> personNameAdapter = new TMLTypeAdapter<Person>() {
      @Override
      public Person read(TMLReader in) throws IOException {
        String name = in.nextString();
        return new Person(name, -1);
      }

      @Override
      public void write(TMLWriter out, Person value) throws IOException {
        out.value(value.name);
      }
    };
    miniJuple = new JupleBuilder().registerTypeAdapter(Person.class,
        personNameAdapter).create();
    truckAdapter = miniJuple.getAdapter(Truck.class);
  }

  static class Truck {
    double horsePower;
    List<Person> passengers = Collections.emptyList();
  }

  static class Person {
    int age;
    String name;

    Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Person && ((Person) o).name.equals(name)
          && ((Person) o).age == age;
    }

    @Override
    public int hashCode() {
      return name.hashCode() ^ age;
    }
  }

  static class Node {
    String label;
    Node left;
    Node right;

    Node(String label) {
      this.label = label;
    }
  }

  // TODO: remove this when TypeAdapter.toJson() is public
  private static <T> String toTML(TMLTypeAdapter<T> typeAdapter, T value)
      throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    typeAdapter.write(writer, value);
    return stringWriter.toString();
  }

  // TODO: remove this when TypeAdapter.fromJson() is public
  private <T> T fromTML(TMLTypeAdapter<T> typeAdapter, String json)
      throws IOException {
    TMLReader reader = new TMLReader(new StringReader(json));
    return typeAdapter.read(reader);
  }

}
