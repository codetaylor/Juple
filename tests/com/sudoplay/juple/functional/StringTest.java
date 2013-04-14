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

import org.junit.Test;

import com.sudoplay.juple.Juple;

/**
 * Functional tests for Json serialization and deserialization of strings.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class StringTest {

  private Juple juple = new Juple();

  @Test
  public void testStringValueSerialization() throws Exception {
    String value = "someRandomStringValue";
    assertEquals('[' + value + ']', juple.toTML(value));
  }

  @Test
  public void testStringValueDeserialization() throws Exception {
    String value = "someRandomStringValue";
    String actual = juple.fromTML("[" + value + "]", String.class);
    assertEquals(value, actual);
  }

  @Test
  public void testEscapedCtrlNInStringSerialization() throws Exception {
    String value = "a\nb";
    String tml = juple.toTML(value);
    assertEquals("[a\\nb]", tml);
  }

  @Test
  public void testEscapedCtrlNInStringDeserialization() throws Exception {
    String tml = "[a\\nb]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals("a\nb", actual);
  }

  @Test
  public void testEscapedCtrlRInStringSerialization() throws Exception {
    String value = "a\rb";
    String tml = juple.toTML(value);
    assertEquals("[a\\rb]", tml);
  }

  @Test
  public void testEscapedCtrlRInStringDeserialization() throws Exception {
    String tml = "[a\\rb]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals("a\rb", actual);
  }

  @Test
  public void testEscapedBackslashInStringSerialization() throws Exception {
    String value = "a\\b";
    String tml = juple.toTML(value);
    assertEquals("[a\\\\b]", tml);
  }

  @Test
  public void testEscapedBackslashInStringDeserialization() throws Exception {
    String tml = "[a\\\\b]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals("a\\b", actual);
  }
  
  @Test
  public void testEscapedSpaceInStringSerialization() throws Exception {
    String value = "a\\sb";
    String tml = juple.toTML(value);
    assertEquals("[a\\\\sb]", tml);
  }

  @Test
  public void testEscapedSpaceInStringDeserialization() throws Exception {
    String tml = "[a\\sb]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals("a b", actual);
  }
  
  @Test
  public void testStringValueAsSingleElementArraySerialization() throws Exception {
    String[] target = {"abc"};
    assertEquals("[[abc]]", juple.toTML(target));
    assertEquals("[[abc]]", juple.toTML(target, String[].class));
  }

  @Test
  public void testStringValueAsSingleElementArrayDeserialization() throws Exception {
    String[] target = {"abc"};
    assertArrayEquals(target, juple.fromTML("[[abc]]", String[].class));
  }
  
  @Test
  public void testAssignmentCharSerialization() {
    String value = "abc=";
    String tml = juple.toTML(value);
    assertEquals("[abc=]", tml);
  }
  
  @Test
  public void testEscapedKeySequenceInStringSerialization() {
    String value = "\\0";
    String tml = juple.toTML(value);
    assertEquals("[\\\\0]", tml);
  }

  @Test
  public void testEscapedKeySequenceInStringDeserialization() {
    String tml = "[\\\\0]";
    String value = juple.fromTML(tml, String.class);
    assertEquals("\\0", value);
  }
  
  @Test
  public void testEmptyStringSerialization() {
    String value = "";
    assertEquals("[\\1]", juple.toTML(value));
  }

  @Test
  public void testEmptyStringDeserialization() {
    assertEquals("", juple.fromTML("[\\1]", String.class));
  }

}
