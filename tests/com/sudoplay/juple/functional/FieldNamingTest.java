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
import com.sudoplay.juple.classparser.annotations.SerializedName;

public class FieldNamingTest {

  private Juple juple = new Juple();

  @Test
  public void testAnnotatedNameSerialization() {
    TestNames testClass = new TestNames();
    assertEquals(TestNames.getExpectedTML(), juple.toTML(testClass));
  }

  @Test
  public void testAnnotatedNameDeserialization() {
    String tml = "[[lowerCamel|1][UpperCamel|2][_lowerCamelLeadingUnderscore|3][_UpperCamelLeadingUnderscore|4][lower_words|5][UPPER_WORDS|6][annotatedName|42]]";
    TestNames testClass = juple.fromTML(tml, TestNames.class);
    assertEquals(testClass.annotated, 42);
  }

  @SuppressWarnings("unused")
  // fields are used reflectively
  private static class TestNames {
    int lowerCamel = 1;
    int UpperCamel = 2;
    int _lowerCamelLeadingUnderscore = 3;
    int _UpperCamelLeadingUnderscore = 4;
    int lower_words = 5;
    int UPPER_WORDS = 6;
    @SerializedName("annotatedName")
    int annotated = 7;

    public static String getExpectedTML() {
      return "[[lowerCamel|1][UpperCamel|2][_lowerCamelLeadingUnderscore|3][_UpperCamelLeadingUnderscore|4][lower_words|5][UPPER_WORDS|6][annotatedName|7]]";
    }
  }
}
