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

package com.sudoplay.juple.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

@SuppressWarnings("resource")
public class TMLWriterTest {

  @Test(expected = NullPointerException.class)
  public void testNullAsWriterParameter_NullPointerException() {
    new TMLWriter(null);
  }

  @Test
  public void testWrongTopLevelType() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    try {
      writer.value("data");
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(true);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(1L);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(1.0);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.name("data");
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.nullValue();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.endList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.divider();
      fail();
    } catch (IllegalStateException expected) {}
  }

  @Test
  public void testNameWithoutValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name("name");
    writer.endList();
    assertEquals("[name|]", stringWriter.toString());
  }

  @Test
  public void testNameWithNullValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name("name");
    writer.nullValue();
    writer.endList();
    assertEquals("[name|\\0]", stringWriter.toString());
  }

  @Test
  public void testNameAfterNotBeginList() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.beginList();
    writer.divider();
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
    writer.endList();
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
    writer.nullValue();
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
    writer.value(true);
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
    writer.endList();
    writer.close();
  }

  @Test
  public void testInvalidAfterName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.beginList();
    writer.name("name");
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.divider();
      fail();
    } catch (IllegalStateException expected) {}
    writer.value(true);
    writer.endList();
    writer.endList();
    writer.close();
  }

  @Test
  public void testValueWithoutName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("data");
    writer.endList();
    assertEquals("[data]", stringWriter.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void testMultipleTopLevelLists_IllegalStateException()
      throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList().endList();
    writer.beginList();
  }

  @Test(expected = IllegalStateException.class)
  public void testCloseIncompleteDocument_IllegalStateException()
      throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList().beginList().endList().close();
  }

  @Test(expected = IllegalStateException.class)
  public void testCloseEmptyDocument_IllegalStateException() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.close();
  }

  @Test(expected = NullPointerException.class)
  public void testNullName_IOException() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name(null);
  }

  @Test
  public void testNullNamedStringValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name("data");
    writer.value((String) null);
    writer.endList();
    writer.close();
    assertEquals("[data|\\0]", stringWriter.toString());
  }

  @Test
  public void testNullNamedArrayValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name("data");
    writer.nullArrayValue();
    writer.endList();
    writer.close();
    assertEquals("[data|\\2]", stringWriter.toString());
  }

  @Test
  public void testNonFiniteDoubleExclusion() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.setEnforceFiniteFloatingPointValues(true);
    writer.beginList();
    try {
      writer.value(Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      writer.value(Double.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      writer.value(Double.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {}

  }

  @Test
  public void testNonFiniteBoxedDoubleExclusion() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.setEnforceFiniteFloatingPointValues(true);
    writer.beginList();
    try {
      writer.value(new Double(Double.NaN));
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      writer.value(new Double(Double.NEGATIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {}
    try {
      writer.value(new Double(Double.POSITIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  public void testDoubles() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value(-0.0);
    writer.value(1.0);
    writer.value(Double.MAX_VALUE);
    writer.value(Double.MIN_VALUE);
    writer.value(0.0);
    writer.value(-0.5);
    writer.value(2.2250738585072014E-308);
    writer.value(Math.PI);
    writer.value(Math.E);
    writer.endList();
    writer.close();
    assertEquals(
        "[-0.0 1.0 1.7976931348623157E308 4.9E-324 0.0 -0.5 2.2250738585072014E-308 3.141592653589793 2.718281828459045]",
        stringWriter.toString());
  }

  @Test
  public void testLongs() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value(0);
    writer.value(1);
    writer.value(-1);
    writer.value(Long.MIN_VALUE);
    writer.value(Long.MAX_VALUE);
    writer.endList();
    writer.close();
    assertEquals("[0 1 -1 -9223372036854775808 9223372036854775807]",
        stringWriter.toString());
  }

  @Test
  public void testNumbers() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value(new BigInteger("0"));
    writer.value(new BigInteger("9223372036854775808"));
    writer.value(new BigInteger("-9223372036854775809"));
    writer.value(new BigDecimal("3.141592653589793238462643383"));
    writer.endList();
    writer.close();
    assertEquals(
        "[0 9223372036854775808 -9223372036854775809 3.141592653589793238462643383]",
        stringWriter.toString());
  }

  @Test
  public void testBooleans() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value(true);
    writer.value(false);
    writer.endList();
    writer.close();
    assertEquals("[true false]", stringWriter.toString());
  }

  @Test
  public void testNulls() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value((String) null);
    writer.nullValue();
    writer.endList();
    writer.close();
    assertEquals("[\\0 \\0]", stringWriter.toString());
  }

  @Test
  public void testNullArrays() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.nullArrayValue();
    writer.nullArrayValue();
    writer.endList();
    writer.close();
    assertEquals("[\\2 \\2]", stringWriter.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNameWithSpaces_IllegalArgumentException() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.name("space case");
  }

  @Test
  public void testStrings() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("a");
    writer.value("a\"");
    writer.value("\"");
    writer.value("|");
    writer.value(",");
    writer.value("\n");
    writer.value("\r");
    writer.value("\t");
    writer.value(" ", SpaceEscapePolicy.FORCE_ESCAPE);
    writer.value("\\");
    writer.value("\\\\");
    writer.value("[");
    writer.value("]");
    writer.value("\0");
    writer.value("\u0019");
    writer.endList();
    writer.close();
    assertEquals(
        "[a a\" \" \\| , \\n \\r \\t \\s \\\\ \\\\\\\\ \\[ \\] \\u0000 \\u0019]",
        stringWriter.toString());
  }

  @Test
  public void testAutoEscapeStringSpaces() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("  This is   a   test. ", SpaceEscapePolicy.AUTO);
    writer.endList();
    writer.close();
    assertEquals("[\\s\\sThis\\sis\\s\\s\\sa\\s\\s\\stest.\\s]",
        stringWriter.toString());
  }

  @Test
  public void testEscapeStringSpaces() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("\\s");
    writer.endList();
    writer.close();
    assertEquals("[\\\\s]", stringWriter.toString());
  }

  @Test
  public void testAutoEscapeStringSpacesSingleSpaces() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("This is a test.", SpaceEscapePolicy.AUTO);
    writer.endList();
    writer.close();
    assertEquals("[This is a test.]", stringWriter.toString());
  }

  @Test
  public void testForceEscapeStringSpaces() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("This is  a  test.", SpaceEscapePolicy.FORCE_ESCAPE);
    writer.endList();
    writer.close();
    assertEquals("[This\\sis\\s\\sa\\s\\stest.]", stringWriter.toString());
  }

  @Test
  public void testNoEscapeStringSpaces() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("This is  a  test.", SpaceEscapePolicy.NO_ESCAPE);
    writer.endList();
    writer.close();
    assertEquals("[This is  a  test.]", stringWriter.toString());
  }

  @Test
  public void testUnicodeLineBreaksEscaped() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.value("\u2028 \u2029");
    writer.endList();
    writer.close();
    assertEquals("[\\u2028 \\u2029]", stringWriter.toString());
  }

  @Test
  public void testEmptyList() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    assertEquals("[]", stringWriter.toString());
  }

  @Test
  public void testDeepNestingLists() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      writer.beginList();
      sb.append("[");
    }
    for (int i = 0; i < 40; i++) {
      writer.endList();
      sb.append("]");
    }
    writer.close();
    assertEquals(sb.toString(), stringWriter.toString());
  }

  @Test
  public void testDuplicateNames() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.beginList();
    writer.name("duplicate").value(true);
    writer.endList();
    writer.beginList();
    writer.name("duplicate").value(false);
    writer.endList();
    writer.endList();
    writer.close();
    // TMLWriter does not attempt to detect duplicate names
    assertEquals("[[duplicate|true][duplicate|false]]", stringWriter.toString());
  }

  @Test
  public void testPrettyPrintWithClassStructure() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.setIndent("    ");
    writer.beginList();
    writer.beginList().name("a").value(true).endList();
    writer.beginList().name("b").value(false).endList();
    writer.beginList().name("c").value(9.43).endList();

    writer.beginList().name("d");
    writer.beginList().value("data").endList();
    writer.beginList().value("data").endList();
    writer.beginList().value("data").endList();
    writer.endList();

    writer.beginList().name("e").beginList();
    writer.beginList().name("a").value("data").endList();
    writer.beginList().name("b").value("data").endList();
    writer.beginList().name("c").value("data").endList();
    writer.endList().endList();

    writer.endList();
    writer.close();

    String expected = "[\n" + "    [a | true]\n" + "    [b | false]\n"
        + "    [c | 9.43]\n" + "    [d | \n" + "        [data]\n"
        + "        [data]\n" + "        [data]\n" + "    ]\n" + "    [e | \n"
        + "        [\n" + "            [a | data]\n"
        + "            [b | data]\n" + "            [c | data]\n"
        + "        ]\n" + "    ]\n" + "]";
    assertEquals(expected, stringWriter.toString());
  }

  @Test
  public void testPrettyPrintWithArrayStructure() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.setIndent("    ");

    writer.beginList();
    writer.beginList().value("data").endList();
    writer.beginList().value("data").endList();
    writer.beginList().value("data").endList();
    writer.endList();

    writer.close();

    String expected = "[\n" + "    [data]\n" + "    [data]\n" + "    [data]\n"
        + "]";
    assertEquals(expected, stringWriter.toString());
  }

  @Test
  public void testClosedWriterThrowsOnStructure() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    try {
      writer.beginList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.endList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.divider();
      fail();
    } catch (IllegalStateException expected) {}
  }

  @Test
  public void testClosedWriterThrowsOnName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    try {
      writer.name("name");
      fail();
    } catch (IllegalStateException expected) {}
  }

  @Test
  public void testClosedWriterThrowsOnValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    try {
      writer.value(true);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value("value");
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(1);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(1.0);
      fail();
    } catch (IllegalStateException expected) {}
    try {
      writer.value(new BigInteger("500"));
      fail();
    } catch (IllegalStateException expected) {}
  }

  @Test
  public void testClosedWriterThrowsOnFlush() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    try {
      writer.flush();
      fail();
    } catch (IllegalStateException expected) {}
  }

  @Test
  public void testCloseIsIdempotent() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.endList();
    writer.close();
    writer.close();
  }

  @Test
  public void testDivider() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TMLWriter writer = new TMLWriter(stringWriter);
    writer.beginList();
    writer.divider();
    writer.divider();
    writer.divider();
    writer.endList();
    writer.close();
    assertEquals("[|||]", stringWriter.toString());
  }

  /**
   * If {@link TMLWriter#beginList()} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#BEGIN_LIST}.
   * <p>
   * If {@link TMLWriter#value(double)} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLWriter#value(long)} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLWriter#value(boolean)} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLWriter#value(String)} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLWriter#value(Number)} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLWriter#divider()} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#DIVIDER}.
   * <p>
   * If {@link TMLWriter#endList()} is successful and in root scope,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#EOF}.
   * <p>
   * If {@link TMLWriter#close()} is successful,
   * {@link TMLWriter#getLastToken()} returns {@link TMLToken#EOF}.
   * 
   * @throws IOException
   */
  @Test
  public void testGetLastToken() throws IOException {
    TMLWriter writer = new TMLWriter(new StringWriter());
    writer.beginList();
    assertEquals(TMLToken.BEGIN_LIST, writer.getLastToken());
    writer.value("name");
    assertEquals(TMLToken.DATA, writer.getLastToken());
    writer.divider();
    assertEquals(TMLToken.DIVIDER, writer.getLastToken());
    writer.value(Double.MAX_VALUE);
    assertEquals(TMLToken.DATA, writer.getLastToken());
    writer.value(Long.MAX_VALUE);
    assertEquals(TMLToken.DATA, writer.getLastToken());
    writer.value(true);
    assertEquals(TMLToken.DATA, writer.getLastToken());
    writer.value((Number) 42);
    assertEquals(TMLToken.DATA, writer.getLastToken());
    writer.endList();
    assertEquals(TMLToken.EOF, writer.getLastToken());
    writer.close();
    assertEquals(TMLToken.EOF, writer.getLastToken());
  }

  /**
   * The writer starts with a scope of 0. For each open delimiter written by
   * {@link TMLWriter#beginList()}, the scope is incremented by one. For each
   * close delimiter written by {@link TMLWritten#endList()}, the scope is
   * decremented by one. A closed writer has a scope of 0.
   * 
   * @throws IOException
   */
  @Test
  public void testGetScope() throws IOException {
    TMLWriter writer = new TMLWriter(new StringWriter());
    assertEquals(0, writer.getScope());
    for (int i = 0; i < 32; i++) {
      writer.beginList();
      assertEquals(i + 1, writer.getScope());
    }
    for (int i = 0; i < 32; i++) {
      writer.endList();
      assertEquals(31 - i, writer.getScope());
    }
    writer.close();
    assertEquals(0, writer.getScope());
  }

}
