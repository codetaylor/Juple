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

public class TMLNumberUtil {

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

  private TMLNumberUtil() {}

  public static boolean isNumeric(String str) {
    int last = NUMBER_CHAR_NONE;

    for (int i = 0; i < str.length(); i++) {

      char c = str.charAt(i);
      switch (c) {
      case '-':
        switch (last) {
        case NUMBER_CHAR_NONE:
          last = NUMBER_CHAR_SIGN;
          continue;
        case NUMBER_CHAR_EXP_E:
          last = NUMBER_CHAR_EXP_SIGN;
          continue;
        default:
          return false;
        }

      case '+':
        switch (last) {
        case NUMBER_CHAR_EXP_E:
          last = NUMBER_CHAR_EXP_SIGN;
          continue;
        default:
          return false;
        }

      case 'e':
      case 'E':
        switch (last) {
        case NUMBER_CHAR_DIGIT:
        case NUMBER_CHAR_FRACTION_DIGIT:
          last = NUMBER_CHAR_EXP_E;
          continue;
        default:
          return false;
        }

      case '.':
        switch (last) {
        case NUMBER_CHAR_DIGIT:
          last = NUMBER_CHAR_DECIMAL;
          continue;
        default:
          return false;
        }

      default:
        if (c < '0' || c > '9') {
          return false;
        }
        switch (last) {
        case NUMBER_CHAR_SIGN:
        case NUMBER_CHAR_NONE:
          last = NUMBER_CHAR_DIGIT;
          continue;
        case NUMBER_CHAR_DECIMAL:
          last = NUMBER_CHAR_FRACTION_DIGIT;
          continue;
        case NUMBER_CHAR_EXP_E:
        case NUMBER_CHAR_EXP_SIGN:
          last = NUMBER_CHAR_EXP_DIGIT;
          continue;
        }
      }
    }

    // We've read a complete number.
    switch (last) {
    case NUMBER_CHAR_DIGIT:
    case NUMBER_CHAR_FRACTION_DIGIT:
    case NUMBER_CHAR_EXP_DIGIT:
      return true;
    default:
      return false;
    }

  }

}
