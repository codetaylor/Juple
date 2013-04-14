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

package com.sudoplay.juple.classparser;

import static org.junit.Assert.*;

import org.junit.Test;

public class TMLNumberTest {

  @Test
  public void testNormalNumberRanges() {
    assertNotNull(getNumber(Byte.MIN_VALUE));
    assertNotNull(getNumber(Byte.MAX_VALUE));
    assertNotNull(getNumber(Short.MIN_VALUE));
    assertNotNull(getNumber(Short.MAX_VALUE));
    assertNotNull(getNumber(Integer.MIN_VALUE));
    assertNotNull(getNumber(Integer.MAX_VALUE));
    assertNotNull(getNumber(Long.MIN_VALUE));
    assertNotNull(getNumber(Long.MAX_VALUE));
    assertNotNull(getNumber(Float.MIN_VALUE));
    assertNotNull(getNumber(Float.MAX_VALUE));
    assertNotNull(getNumber(Double.MIN_VALUE));
    assertNotNull(getNumber(Double.MAX_VALUE));
  }

  @Test
  public void testFractional() {
    assertNotNull(getNumber("0.42"));
    assertNotNull(getNumber("-0.42"));
  }

  @Test
  public void testMalformedNumbers() {

    assertNull(getNumber("-"));
    assertNull(getNumber("."));

    // exponent lacks digit
    assertNull(getNumber("e"));
    assertNull(getNumber("0e"));
    assertNull(getNumber(".e"));
    assertNull(getNumber("0.e"));
    assertNull(getNumber("-.0e"));

    // no integer
    assertNull(getNumber("e1"));
    assertNull(getNumber(".e1"));
    assertNull(getNumber("-e1"));

    // trailing characters
    assertNull(getNumber("1x"));
    assertNull(getNumber("1.1x"));
    assertNull(getNumber("1e1x"));
    assertNull(getNumber("1ex"));
    assertNull(getNumber("1.1ex"));
    assertNull(getNumber("1.1e1x"));

    // fraction has no digit
    assertNull(getNumber("0."));
    assertNull(getNumber("-0."));
    assertNull(getNumber("0.e1"));
    assertNull(getNumber("-0.e1"));

    // no leading digit
    assertNull(getNumber(".0"));
    assertNull(getNumber("-.0"));
    assertNull(getNumber(".0e1"));
    assertNull(getNumber("-.0e1"));

    // various
    assertNull(getNumber("--22345"));
    assertNull(getNumber("22345-"));
    assertNull(getNumber("223-45"));
    assertNull(getNumber("-223-45"));
    assertNull(getNumber("..22345"));
    assertNull(getNumber(".223.45"));
    assertNull(getNumber("2.23.45"));
    assertNull(getNumber("2.3-23.45"));
    assertNull(getNumber("22dsf345"));
    assertNull(getNumber("22\n345"));
    assertNull(getNumber("22\t345"));
    assertNull(getNumber("22\r345"));
  }

  private TMLLazilyParsedNumber getNumber(Number number) {
    return TMLLazilyParsedNumber.get(String.valueOf(number));
  }

  private TMLLazilyParsedNumber getNumber(String string) {
    return TMLLazilyParsedNumber.get(string);
  }

}
