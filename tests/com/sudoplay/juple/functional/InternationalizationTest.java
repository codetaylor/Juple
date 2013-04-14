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

import org.junit.Test;

import com.sudoplay.juple.Juple;

public class InternationalizationTest {

  private Juple juple = new Juple();

  @Test
  public void testStringsWithRawChineseCharactersDeserialization()
      throws Exception {
    String expected = "好好好";
    String tml = "[" + expected + "]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersSerialization()
      throws Exception {
    String target = "\u597d\u597d\u597d";
    String tml = juple.toTML(target);
    String expected = "[\u597d\u597d\u597d]";
    assertEquals(expected, tml);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersDeserialization()
      throws Exception {
    String expected = "\u597d\u597d\u597d";
    String tml = "[" + expected + "]";
    String actual = juple.fromTML(tml, String.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersEscapedDeserialization()
      throws Exception {
    String actual = juple.fromTML("[\\u597d\\u597d\\u597d]", String.class);
    assertEquals("\u597d\u597d\u597d", actual);
  }

}
