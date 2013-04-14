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

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.annotations.Since;
import com.sudoplay.juple.classparser.annotations.Until;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;

/**
 * Functional tests for versioning support.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class VersioningTest {

  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 2;
  private static final int D = 3;

  private JupleBuilder builder = new JupleBuilder();

  @Test
  public void testVersionedUntilSerialization() {
    Version1 target = new Version1();
    Juple juple = builder.setVersion(1.29).create();
    String tml = juple.toTML(target);
    assertTrue(tml.contains("[a|" + A));

    juple = builder.setVersion(1.3).create();
    tml = juple.toTML(target);
    assertFalse(tml.contains("[a|" + A));
  }

  @Test
  public void testVersionedUntilDeserialization() {
    Juple juple = builder.setVersion(1.3).create();
    String tml = "[[a|3][b|4][c|5]]";
    Version1 version1 = juple.fromTML(tml, Version1.class);
    assertEquals(A, version1.a);
  }

  @Test
  public void testVersionedClassesSerialization() {
    Juple juple = builder.setVersion(1.0).create();
    String tml1 = juple.toTML(new Version1());
    String tml2 = juple.toTML(new Version1_1());
    assertEquals(tml1, tml2);
  }

  @Test
  public void testVersionedClassesDeserialization() {
    Juple juple = builder.setVersion(1.0).create();
    String tml = "[[a|3][b|4][c|5]]";
    Version1 version1 = juple.fromTML(tml, Version1.class);
    assertEquals(3, version1.a);
    assertEquals(4, version1.b);
    Version1_1 version1_1 = juple.fromTML(tml, Version1_1.class);
    assertEquals(3, version1_1.a);
    assertEquals(4, version1_1.b);
    assertEquals(C, version1_1.c);
  }

  @Test
  public void testIgnoreLaterVersionClassSerialization() {
    Juple juple = builder.setVersion(1.0).create();
    assertEquals("[\\0]", juple.toTML(new Version1_2()));
  }

  @Test
  public void testIgnoreLaterVersionClassDeserialization() {
    Juple juple = builder.setVersion(1.0).create();
    String tml = "[[a|3][b|4][c|5][d|6]]";
    Version1_2 version1_2 = juple.fromTML(tml, Version1_2.class);
    // Since the class is versioned to be after 1.0, we expect null
    assertNull(version1_2);
  }

  @Test
  public void testVersionedJupleWithUnversionedClassesSerialization() {
    Juple juple = builder.setVersion(1.0).create();
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testVersionedJupleWithUnversionedClassesDeserialization() {
    Juple juple = builder.setVersion(1.0).create();
    String tml = "[[longValue|10][intValue|20][booleanValue|false]]";

    BagOfPrimitives expected = new BagOfPrimitives();
    expected.longValue = 10;
    expected.intValue = 20;
    expected.booleanValue = false;
    BagOfPrimitives actual = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilSerialization() {
    Juple juple = builder.setVersion(1.0).create();
    SinceUntilMixing target = new SinceUntilMixing();
    String tml = juple.toTML(target);
    assertFalse(tml.contains("[b|" + B));

    juple = builder.setVersion(1.2).create();
    tml = juple.toTML(target);
    assertTrue(tml.contains("[b|" + B));

    juple = builder.setVersion(1.3).create();
    tml = juple.toTML(target);
    assertFalse(tml.contains("[b|" + B));
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilDeserialization() {
    String tml = "[[a|5][b|6]]";
    Juple juple = builder.setVersion(1.0).create();
    SinceUntilMixing result = juple.fromTML(tml, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);

    juple = builder.setVersion(1.2).create();
    result = juple.fromTML(tml, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(6, result.b);

    juple = builder.setVersion(1.3).create();
    result = juple.fromTML(tml, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);
  }

  @Test
  public void testVersionedArraySerialization() {
    Juple juple = builder.setVersion(1.0).create();
    String tml = juple.toTML(new ArrayVersion1());
    assertEquals("[[b|4 5 6]]", tml);
  }

  private static class ArrayVersion1 {
    @Since(1.2)
    int[] a = { 1, 2, 3 };
    @SuppressWarnings("unused")
    int[] b = { 4, 5, 6 };
  }

  private static class Version1 {
    @Until(1.3)
    int a = A;
    @Since(1.0)
    int b = B;
  }

  private static class Version1_1 extends Version1 {
    @Since(1.1)
    int c = C;
  }

  @Since(1.2)
  private static class Version1_2 extends Version1_1 {
    @SuppressWarnings("unused")
    int d = D;
  }

  private static class SinceUntilMixing {
    int a = A;

    @Since(1.1)
    @Until(1.3)
    int b = B;
  }
}
