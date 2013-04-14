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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.error.TMLSyntaxException;

public class PrimitiveTest {

  private Juple juple = new Juple();

  @Test
  public void testPrimitiveIntegerAutoboxedSerialization() {
    assertEquals("[1]", juple.toTML(1));
  }

  @Test
  public void testPrimitiveIntegerAutoboxedDeserialization() {
    int expected = 1;
    int actual = juple.fromTML("[1]", int.class);
    assertEquals(expected, actual);

    actual = juple.fromTML("[1]", Integer.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testByteSerialization() {
    assertEquals("[1]", juple.toTML(1, byte.class));
    assertEquals("[1]", juple.toTML(1, Byte.class));
  }

  @Test
  public void testShortSerialization() {
    assertEquals("[1]", juple.toTML(1, short.class));
    assertEquals("[1]", juple.toTML(1, Short.class));
  }

  @Test
  public void testByteDeserialization() {
    Byte target = juple.fromTML("[1]", Byte.class);
    assertEquals(1, (byte) target);
    byte primitive = juple.fromTML("[1]", byte.class);
    assertEquals(1, primitive);
  }

  @Test
  public void testPrimitiveIntegerAutoboxedInASingleElementArraySerialization() {
    int target[] = { -9332 };
    assertEquals("[-9332]", juple.toTML(target));
    assertEquals("[-9332]", juple.toTML(target, int[].class));
    assertEquals("[-9332]", juple.toTML(target, Integer[].class));
  }

  @Test
  public void testReallyLongValuesSerialization() {
    long value = 333961828784581L;
    assertEquals("[333961828784581]", juple.toTML(value));
  }

  @Test
  public void testReallyLongValuesDeserialization() {
    String tml = "[333961828784581]";
    long value = juple.fromTML(tml, Long.class);
    assertEquals(333961828784581L, value);
  }

  @Test
  public void testPrimitiveLongAutoboxedSerialization() {
    assertEquals("[1]", juple.toTML(1L, long.class));
    assertEquals("[1]", juple.toTML(1L, Long.class));
  }

  @Test
  public void testPrimitiveLongAutoboxedDeserialization() {
    long expected = 1L;
    long actual = juple.fromTML("[1]", long.class);
    assertEquals(expected, actual);

    actual = juple.fromTML("[1]", Long.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testPrimitiveLongAutoboxedInASingleElementArraySerialization() {
    long[] target = { -23L };
    assertEquals("[-23]", juple.toTML(target));
    assertEquals("[-23]", juple.toTML(target, long[].class));
    assertEquals("[-23]", juple.toTML(target, Long[].class));
  }

  @Test
  public void testPrimitiveBooleanAutoboxedSerialization() {
    assertEquals("[true]", juple.toTML(true));
    assertEquals("[false]", juple.toTML(false));
  }

  @Test
  public void testBooleanDeserialization() {
    boolean value = juple.fromTML("[false]", boolean.class);
    assertEquals(false, value);
    value = juple.fromTML("[true]", boolean.class);
    assertEquals(true, value);
  }

  @Test
  public void testPrimitiveBooleanAutoboxedInASingleElementArraySerialization() {
    boolean target[] = { false };
    assertEquals("[false]", juple.toTML(target));
    assertEquals("[false]", juple.toTML(target, boolean[].class));
    assertEquals("[false]", juple.toTML(target, Boolean[].class));
  }

  @Test
  public void testNumberSerialization() {
    Number expected = 1L;
    String tml = juple.toTML(expected);
    assertEquals("[1]", tml);

    tml = juple.toTML(expected, Number.class);
    assertEquals("[1]", tml);
  }

  @Test
  public void testNumberDeserialization() {
    String tml = "[1]";
    Number expected = new Integer(1);
    Number actual = juple.fromTML(tml, Number.class);
    assertEquals(expected.intValue(), actual.intValue());

    tml = "[" + String.valueOf(Long.MAX_VALUE) + "]";
    expected = new Long(Long.MAX_VALUE);
    actual = juple.fromTML(tml, Number.class);
    assertEquals(expected.longValue(), actual.longValue());
  }

  @Test
  public void testPrimitiveDoubleAutoboxedSerialization() {
    assertEquals("[-122.08234335]", juple.toTML(-122.08234335));
    assertEquals("[122.08112002]", juple.toTML(new Double(122.08112002)));
  }

  @Test
  public void testPrimitiveDoubleAutoboxedDeserialization() {
    double actual = juple.fromTML("[-122.08858585]", double.class);
    assertEquals(-122.08858585, actual, 1e-15);

    actual = juple.fromTML("[122.023900008000]", Double.class);
    assertEquals(122.023900008, actual, 1e-15);
  }

  @Test
  public void testPrimitiveDoubleAutoboxedInASingleElementArraySerialization() {
    double[] target = { -122.08D };
    assertEquals("[-122.08]", juple.toTML(target));
    assertEquals("[-122.08]", juple.toTML(target, double[].class));
    assertEquals("[-122.08]", juple.toTML(target, Double[].class));
  }

  @Test
  public void testDoubleAsStringRepresentationDeserialization() {
    String doubleValue = "1.0043E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = juple.fromTML('[' + doubleValue + ']', Double.class);
    assertEquals(expected, actual);

    double actual1 = juple.fromTML('[' + doubleValue + ']', double.class);
    assertEquals(expected.doubleValue(), actual1, 1e-15);
  }

  @Test
  public void testDoubleNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "1E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = juple.fromTML('[' + doubleValue + ']', Double.class);
    assertEquals(expected, actual);

    double actual1 = juple.fromTML('[' + doubleValue + ']', double.class);
    assertEquals(expected.doubleValue(), actual1, 1e-15);
  }

  @Test
  public void testLargeDoubleDeserialization() {
    String doubleValue = "1.234567899E8";
    Double expected = Double.valueOf(doubleValue);
    Double actual = juple.fromTML('[' + doubleValue + ']', Double.class);
    assertEquals(expected, actual);

    double actual1 = juple.fromTML('[' + doubleValue + ']', double.class);
    assertEquals(expected.doubleValue(), actual1, 1e-15);
  }

  @Test
  public void testBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String tml = juple.toTML(target);
    assertEquals("[" + target + "]", tml);
  }

  @Test
  public void testBigDecimalDeserialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String tml = "[-122.0e-21]";
    assertEquals(target, juple.fromTML(tml, BigDecimal.class));
  }

  @Test
  public void testBigDecimalInASingleElementArraySerialization() {
    BigDecimal[] target = { new BigDecimal("-122.08e-21") };
    String tml = juple.toTML(target);
    String actual = extractElementFromArray(tml);
    assertEquals(target[0], new BigDecimal(actual));

    tml = juple.toTML(target, BigDecimal[].class);
    actual = extractElementFromArray(tml);
    assertEquals(target[0], new BigDecimal(actual));
  }

  @Test
  public void testSmallValueForBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("1.55");
    String actual = juple.toTML(target);
    assertEquals('[' + target.toString() + ']', actual);
  }

  @Test
  public void testSmallValueForBigDecimalDeserialization() {
    BigDecimal expected = new BigDecimal("1.55");
    BigDecimal actual = juple.fromTML("[1.55]", BigDecimal.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testBigDecimalPreservePrecisionSerialization() {
    String expectedValue = "1.000";
    BigDecimal obj = new BigDecimal(expectedValue);
    String actualValue = juple.toTML(obj);

    assertEquals('[' + expectedValue + ']', actualValue);
  }

  @Test
  public void testBigDecimalPreservePrecisionDeserialization() {
    String tml = "[1.000]";
    BigDecimal expected = new BigDecimal("1.000");
    BigDecimal actual = juple.fromTML(tml, BigDecimal.class);

    assertEquals(expected, actual);
  }

  @Test
  public void testBigDecimalAsStringRepresentationDeserialization() {
    String doubleValue = "[0.05E+5]";
    BigDecimal expected = new BigDecimal("0.05E+5");
    BigDecimal actual = juple.fromTML(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testBigDecimalNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "[5E+5]";
    BigDecimal expected = new BigDecimal("5E+5");
    BigDecimal actual = juple.fromTML(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testBigIntegerSerialization() {
    BigInteger target = new BigInteger(
        "12121211243123245845384534687435634558945453489543985435");
    String expected = "[" + target.toString() + "]";
    assertEquals(expected, juple.toTML(target));
  }

  @Test
  public void testBigIntegerDeserialization() {
    String tml = "12121211243123245845384534687435634558945453489543985435";
    BigInteger target = new BigInteger(tml);
    tml = "[" + tml + "]";
    assertEquals(target, juple.fromTML(tml, BigInteger.class));
  }

  @Test
  public void testBigIntegerInASingleElementArraySerialization() {
    BigInteger[] target = { new BigInteger(
        "1212121243434324323254365345367456456456465464564564") };
    String tml = juple.toTML(target);
    String actual = extractElementFromArray(tml);
    assertEquals(target[0], new BigInteger(actual));

    tml = juple.toTML(target, BigInteger[].class);
    actual = extractElementFromArray(tml);
    assertEquals(target[0], new BigInteger(actual));
  }

  @Test
  public void testSmallValueForBigIntegerSerialization() {
    BigInteger target = new BigInteger("15");
    String actual = juple.toTML(target);
    String expected = "[" + target.toString() + "]";
    assertEquals(expected, actual);
  }

  @Test
  public void testSmallValueForBigIntegerDeserialization() {
    BigInteger expected = new BigInteger("15");
    BigInteger actual = juple.fromTML("[15]", BigInteger.class);
    assertEquals(expected, actual);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBadValueForBigIntegerDeserialization() {
    juple.fromTML("[15.099]", BigInteger.class);
  }

  @Test
  public void testMoreSpecificSerialization() {
    String expected = "This is a string";
    String expectedJson = juple.toTML(expected);

    Serializable serializableString = expected;
    String actualJson = juple.toTML(serializableString, Serializable.class);
    assertFalse(expectedJson.equals(actualJson));
  }

  @Test
  public void testDoubleNaNSerializationExclusion() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      double nan = Double.NaN;
      juple.toTML(nan);
      fail("Juple should not accept NaN when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Double.NaN);
      fail("Juple should not accept NaN when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testDoubleNaNSerialization() {
    double nan = Double.NaN;
    assertEquals("[NaN]", juple.toTML(nan));
    assertEquals("[NaN]", juple.toTML(Double.NaN));
  }

  @Test
  public void testDoubleNaNDeserialization() {
    assertTrue(Double.isNaN(juple.fromTML("[NaN]", Double.class)));
    assertTrue(Double.isNaN(juple.fromTML("[NaN]", double.class)));
  }

  @Test
  public void testFloatNaNSerializationNotSupportedByDefault() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      float nan = Float.NaN;
      juple.toTML(nan);
      fail("Juple should not accept NaN when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Float.NaN);
      fail("Juple should not accept NaN when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testFloatNaNSerialization() {
    float nan = Float.NaN;
    assertEquals("[NaN]", juple.toTML(nan));
    assertEquals("[NaN]", juple.toTML(Float.NaN));
  }

  @Test
  public void testFloatNaNDeserialization() {
    assertTrue(Float.isNaN(juple.fromTML("[NaN]", Float.class)));
    assertTrue(Float.isNaN(juple.fromTML("[NaN]", float.class)));
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBigDecimalNaNDeserializationNotSupported() {
    juple.fromTML("[NaN]", BigDecimal.class);
  }

  @Test
  public void testDoubleInfinitySerializationExclusion() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      double infinity = Double.POSITIVE_INFINITY;
      juple.toTML(infinity);
      fail("Juple should not accept positive infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Double.POSITIVE_INFINITY);
      fail("Juple should not accept positive infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testDoubleInfinitySerialization() {
    double infinity = Double.POSITIVE_INFINITY;
    assertEquals("[Infinity]", juple.toTML(infinity));
    assertEquals("[Infinity]", juple.toTML(Double.POSITIVE_INFINITY));
  }

  @Test
  public void testDoubleInfinityDeserialization() {
    assertTrue(Double.isInfinite(juple.fromTML("[Infinity]", Double.class)));
    assertTrue(Double.isInfinite(juple.fromTML("[Infinity]", double.class)));
  }

  @Test
  public void testFloatInfinitySerializationNotSupportedByDefault() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      float infinity = Float.POSITIVE_INFINITY;
      juple.toTML(infinity);
      fail("Juple should not accept positive infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Float.POSITIVE_INFINITY);
      fail("Juple should not accept positive infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testFloatInfinitySerialization() {
    float infinity = Float.POSITIVE_INFINITY;
    assertEquals("[Infinity]", juple.toTML(infinity));
    assertEquals("[Infinity]", juple.toTML(Float.POSITIVE_INFINITY));
  }

  @Test
  public void testFloatInfinityDeserialization() {
    assertTrue(Float.isInfinite(juple.fromTML("[Infinity]", Float.class)));
    assertTrue(Float.isInfinite(juple.fromTML("[Infinity]", float.class)));
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBigDecimalInfinityDeserializationNotSupported() {
    juple.fromTML("[Infinity]", BigDecimal.class);
  }

  @Test
  public void testNegativeInfinitySerializationExclusion() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      double negativeInfinity = Double.NEGATIVE_INFINITY;
      juple.toTML(negativeInfinity);
      fail("Gson should not accept negative infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Double.NEGATIVE_INFINITY);
      fail("Gson should not accept negative infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testNegativeInfinitySerialization() {
    double negativeInfinity = Double.NEGATIVE_INFINITY;
    assertEquals("[-Infinity]", juple.toTML(negativeInfinity));
    assertEquals("[-Infinity]", juple.toTML(Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testNegativeInfinityDeserialization() {
    assertTrue(Double.isInfinite(juple.fromTML("[-Infinity]", double.class)));
    assertTrue(Double.isInfinite(juple.fromTML("[-Infinity]", Double.class)));
  }

  @Test
  public void testNegativeInfinityFloatSerializationExclusion() {
    Juple juple = new JupleBuilder().enforceFiniteFloatingPointValues()
        .create();
    try {
      float negativeInfinity = Float.NEGATIVE_INFINITY;
      juple.toTML(negativeInfinity);
      fail("Gson should not accept negative infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
    try {
      juple.toTML(Float.NEGATIVE_INFINITY);
      fail("Gson should not accept negative infinity when enforceFiniteFloatingPointValues");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testNegativeInfinityFloatSerialization() {
    float negativeInfinity = Float.NEGATIVE_INFINITY;
    assertEquals("[-Infinity]", juple.toTML(negativeInfinity));
    assertEquals("[-Infinity]", juple.toTML(Float.NEGATIVE_INFINITY));
  }

  @Test
  public void testNegativeInfinityFloatDeserialization() {
    assertTrue(Float.isInfinite(juple.fromTML("[-Infinity]", float.class)));
    assertTrue(Float.isInfinite(juple.fromTML("[-Infinity]", Float.class)));
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBigDecimalNegativeInfinityDeserializationNotSupported() {
    juple.fromTML("[-Infinity]", BigDecimal.class);
  }

  @Test
  public void testDeserializePrimitiveWrapperAsObjectField() {
    String tml = "[[i|10]]";
    ClassWithIntegerField target = juple.fromTML(tml,
        ClassWithIntegerField.class);
    assertEquals(10, target.i.intValue());
  }

  @Test
  public void testPrimitiveClassLiteral() {
    assertEquals(1, juple.fromTML("[1]", int.class).intValue());
    assertEquals(1,
        (juple.fromTML(new StringReader("[1]"), int.class)).intValue());
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializingDecimalPointValueZeroFails() {
    assertEquals(1, (int) juple.fromTML("[1.0]", Integer.class));
  }

  @Test
  public void testDeserializingNonZeroDecimalPointValuesAsIntegerFails() {
    try {
      juple.fromTML("[1.02]", Byte.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", Short.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", Integer.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", Long.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", byte.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", short.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", int.class);
      fail();
    } catch (TMLSyntaxException expected) {}
    try {
      juple.fromTML("[1.02]", long.class);
      fail();
    } catch (TMLSyntaxException expected) {}
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializingBigDecimalAsIntegerFails() {
    juple.fromTML("[-122.08e-213]", Integer.class);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializingBigIntegerAsIntegerFails() {
    juple.fromTML("[12121211243123245845384534687435634558945453489543985435]",
        Integer.class);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializingBigIntegerAsLongFails() {
    juple.fromTML("[12121211243123245845384534687435634558945453489543985435]",
        Long.class);
  }

  @Test
  public void testValueVeryCloseToZeroIsZero() {
    assertEquals(-0.0f, juple.fromTML("[-122.08e-2132]", float.class), 1e-15);
    assertEquals(-0.0, juple.fromTML("[-122.08e-2132]", double.class), 1e-15);
    assertEquals(0.0f, juple.fromTML("[122.08e-2132]", float.class), 1e-15);
    assertEquals(0.0, juple.fromTML("[122.08e-2132]", double.class), 1e-15);
  }

  @Test
  public void testDeserializingBigDecimalAsFloat() {
    String tml = "[-122.08e-2132332]";
    float actual = juple.fromTML(tml, float.class);
    assertEquals(-0.0f, actual, 1e-15);
  }

  @Test
  public void testDeserializingBigDecimalAsDouble() {
    String tml = "[-122.08e-2132332]";
    double actual = juple.fromTML(tml, double.class);
    assertEquals(-0.0d, actual, 1e-15);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDeserializingBigDecimalAsBigIntegerFails() {
    juple.fromTML("[-122.08e-213]", BigInteger.class);
  }

  @Test
  public void testDeserializingBigIntegerAsBigDecimal() {
    BigDecimal actual = juple.fromTML(
        "[12121211243123245845384534687435634558945453489543985435]",
        BigDecimal.class);
    assertEquals("12121211243123245845384534687435634558945453489543985435",
        actual.toPlainString());
  }

  private static class ClassWithIntegerField {
    Integer i;
  }

  private String extractElementFromArray(String tml) {
    return tml.substring(tml.indexOf('[') + 1, tml.indexOf(']'));
  }
}
