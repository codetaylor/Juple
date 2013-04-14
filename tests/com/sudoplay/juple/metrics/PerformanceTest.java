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

package com.sudoplay.juple.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.annotations.Expose;

/**
 * Tests to measure performance for Juple. All tests in this file will be
 * disabled in code. To run them remove set DISABLED to false and run them.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class PerformanceTest {

  private static final boolean DISABLED = true;
  private static final int COLLECTION_SIZE = 5000;
  private static final int NUM_ITERATIONS = 100;

  private Juple juple = new Juple();

  @Test
  public void testStringDeserialization() {
    if (DISABLED) return;

    StringBuilder sb = new StringBuilder(8096);
    sb.append("Error Yippie");

    while (true) {
      try {
        String stackTrace = sb.toString();
        sb.append(stackTrace);
        String tml = "[[message|Error message.][stackTrace|" + stackTrace
            + "]]";
        parseLongTML(tml);
        System.out.println("Juple could handle a string of size: "
            + stackTrace.length());
      } catch (Exception expected) {
        System.out.println("...after which threw: " + expected.getClass());
        break;
      } catch (Error expected) {
        System.out.println("...after which threw: " + expected.getClass());
        break;
      }
    }
  }

  @Test
  public void testLargeCollectionSerialization() {
    if (DISABLED) return;

    int count = 1400000;
    List<CollectionEntry> list = new ArrayList<CollectionEntry>(count);
    for (int i = 0; i < count; ++i) {
      list.add(new CollectionEntry("name" + i, "value" + i));
    }
    juple.toTML(list);
    System.out.println("Juple could handle serializing a collection of "
        + count + " objects");
  }

  @Test
  public void testLargeCollectionDeserialization() {
    if (DISABLED) return;

    StringBuilder sb = new StringBuilder();
    int count = 1400000;// 87000;
    sb.append('[');
    for (int i = 0; i < count; ++i) {
      sb.append("[[name|name").append(i).append("][value|value").append(i)
          .append("]]");
    }
    sb.append(']');
    String tml = sb.toString();
    Type collectionType = new TMLTypeToken<ArrayList<CollectionEntry>>() {}
        .getType();
    List<CollectionEntry> list = juple.fromTML(tml, collectionType);
    assertEquals(count, list.size());
    System.out.println("Juple could handle deserializing a collection of "
        + count + " objects");
  }

  @Test
  public void testByteArraySerialization() {
    if (DISABLED) return;

    for (int size = 4145152; true; size += 1036288) {
      byte[] ba = new byte[size];
      for (int i = 0; i < size; ++i) {
        ba[i] = 0x05;
      }
      juple.toTML(ba);
      // last test size = 51814400 before manual stop
      System.out.printf("Juple could serialize a byte array of size: %d\n",
          size);
    }
  }

  @Test
  public void testByteArrayDeserialization() {
    if (DISABLED) return;

    for (int numElements = 10639296; true; numElements += 16384) {
      StringBuilder sb = new StringBuilder(numElements * 2);
      sb.append("[");
      boolean first = true;
      for (int i = 0; i < numElements; ++i) {
        if (first) {
          first = false;
        } else {
          sb.append(" ");
        }
        sb.append("5");
      }
      sb.append("]");
      String tml = sb.toString();
      byte[] ba = juple.fromTML(tml, byte[].class);
      // last test size = 14047168 before manual stop
      System.out.printf("Juple could deserialize a byte array of size: %d\n",
          ba.length);
    }
  }

  // ==========================================================================
  // = Timed tests
  // ==========================================================================

  @Test
  public void testRunTimedTests() {
    if (DISABLED) return;

    testSerializeClasses();
    testDeserializeClasses();
    testLargeObjectSerializationAndDeserialization();
    testSerializeExposedClasses();
    testDeserializeExposedClasses();
    testLargeMapRoundTrip();
  }

  private void testSerializeClasses() {
    ClassWithList c = new ClassWithList("str");
    for (int i = 0; i < COLLECTION_SIZE; ++i) {
      c.list.add(new ClassWithField("element-" + i));
    }
    StringWriter w = new StringWriter();
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      juple.toTML(c, w);
    }
    long t2 = System.currentTimeMillis();
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Serialize classes avg time: %d ms\n", avg);
  }

  private void testDeserializeClasses() {
    String tml = buildTMLForClassWithList();
    ClassWithList[] target = new ClassWithList[NUM_ITERATIONS];
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      target[i] = juple.fromTML(tml, ClassWithList.class);
    }
    long t2 = System.currentTimeMillis();
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Deserialize classes avg time: %d ms\n", avg);
  }

  private void testLargeObjectSerializationAndDeserialization() {
    Map<String, Long> largeObject = new HashMap<String, Long>();
    for (long l = 0; l < 100000; l++) {
      largeObject.put("field" + l, l);
    }

    long t1 = System.currentTimeMillis();
    String tml = juple.toTML(largeObject);
    long t2 = System.currentTimeMillis();
    System.out.printf("Large object serialized in: %d ms\n", (t2 - t1));

    t1 = System.currentTimeMillis();
    juple.fromTML(tml, new TMLTypeToken<Map<String, Long>>() {}.getType());
    t2 = System.currentTimeMillis();
    System.out.printf("Large object deserialized in: %d ms\n", (t2 - t1));
  }

  private void testSerializeExposedClasses() {
    ClassWithListOfObjects c1 = new ClassWithListOfObjects("str");
    for (int i1 = 0; i1 < COLLECTION_SIZE; ++i1) {
      c1.list.add(new ClassWithExposedField("element-" + i1));
    }
    ClassWithListOfObjects c = c1;
    StringWriter w = new StringWriter();
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      juple.toTML(c, w);
    }
    long t2 = System.currentTimeMillis();
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Serialize exposed classes avg time: %d ms\n", avg);
  }

  private void testDeserializeExposedClasses() {
    String tml = buildTMLForClassWithList();
    ClassWithListOfObjects[] target = new ClassWithListOfObjects[NUM_ITERATIONS];
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < NUM_ITERATIONS; ++i) {
      target[i] = juple.fromTML(tml, ClassWithListOfObjects.class);
    }
    long t2 = System.currentTimeMillis();
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Deserialize exposed classes avg time: %d ms\n", avg);
  }

  private void testLargeMapRoundTrip() {
    Type longToLong = new TMLTypeToken<Map<Long, Long>>() {}.getType();
    Map<Long, Long> original = new HashMap<Long, Long>();
    for (long i = 0; i < 1000000; i++) {
      original.put(i, i + 1);
    }

    long t1 = System.currentTimeMillis();
    String tml = juple.toTML(original);
    long t2 = System.currentTimeMillis();
    long avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Serialize large map avg time: %d ms\n", avg);

    t1 = System.currentTimeMillis();
    juple.fromTML(tml, longToLong);
    t2 = System.currentTimeMillis();
    avg = (t2 - t1) / NUM_ITERATIONS;
    System.out.printf("Deserialize large map avg time: %d ms\n", avg);
  }

  @SuppressWarnings("unused")
  private static class CollectionEntry {
    final String name;
    final String value;

    private CollectionEntry() {
      this(null, null);
    }

    CollectionEntry(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  private void parseLongTML(String tml) {
    ExceptionHolder target = juple.fromTML(tml, ExceptionHolder.class);
    assertTrue(target.message.contains("Error"));
    assertTrue(target.stackTrace.contains("Yippie"));
  }

  private static class ExceptionHolder {
    public final String message;
    public final String stackTrace;

    // For use by Juple
    @SuppressWarnings("unused")
    private ExceptionHolder() {
      this("", "");
    }

    public ExceptionHolder(String message, String stackTrace) {
      this.message = message;
      this.stackTrace = stackTrace;
    }
  }

  @SuppressWarnings("unused")
  private static final class ClassWithList {
    final String field;
    final List<ClassWithField> list = new ArrayList<ClassWithField>(
        COLLECTION_SIZE);

    ClassWithList() {
      this(null);
    }

    ClassWithList(String field) {
      this.field = field;
    }
  }

  @SuppressWarnings("unused")
  private static final class ClassWithField {
    final String field;

    ClassWithField() {
      this("");
    }

    public ClassWithField(String field) {
      this.field = field;
    }
  }

  @SuppressWarnings("unused")
  private static final class ClassWithListOfObjects {
    @Expose
    final String field;
    @Expose
    final List<ClassWithExposedField> list = new ArrayList<ClassWithExposedField>(
        COLLECTION_SIZE);

    ClassWithListOfObjects() {
      this(null);
    }

    ClassWithListOfObjects(String field) {
      this.field = field;
    }
  }

  @SuppressWarnings("unused")
  private static final class ClassWithExposedField {
    @Expose
    final String field;

    ClassWithExposedField() {
      this("");
    }

    ClassWithExposedField(String field) {
      this.field = field;
    }
  }

  private String buildTMLForClassWithList() {
    StringBuilder sb = new StringBuilder("[");
    sb.append("[field|").append("str").append("]");
    sb.append("[list|[");
    boolean first = true;
    for (int i = 0; i < COLLECTION_SIZE; ++i) {
      if (first) {
        first = false;
      } else {
        sb.append(" ");
      }
      sb.append("[field|element-" + i + "]");
    }
    sb.append("]");
    sb.append("]");
    sb.append("]");
    return sb.toString();
  }

}
