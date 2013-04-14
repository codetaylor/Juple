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

import java.math.BigInteger;

/**
 * This class holds a number value that is lazily converted to a specific number
 * type. Use {@link #get(String)} to get a new instance of
 * {@link TMLLazilyParsedNumber}. If the string passed to {@link #get(String)}
 * is not a valid number, null is returned.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
@SuppressWarnings("serial")
public final class TMLLazilyParsedNumber extends Number {
  private final String value;

  public TMLLazilyParsedNumber(String value) {
    this.value = value;
  }

  @Override
  public byte byteValue() {
    return Byte.parseByte(value);
  }
  
  @Override
  public short shortValue() {
    return Short.parseShort(value);
  }
  
  @Override
  public int intValue() {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      try {
        return (int) Long.parseLong(value);
      } catch (NumberFormatException nfe) {
        return new BigInteger(value).intValue();
      }
    }
  }

  @Override
  public long longValue() {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return new BigInteger(value).longValue();
    }
  }

  @Override
  public float floatValue() {
    return Float.parseFloat(value);
  }

  @Override
  public double doubleValue() {
    return Double.parseDouble(value);
  }

  @Override
  public String toString() {
    return value;
  }

  /**
   * @param str
   * @return a new {@link TMLLazilyParsedNumber} or null if str is an invalid
   *         number
   */
  public static TMLLazilyParsedNumber get(String str) {
    if (TMLNumberUtil.isNumeric(str)) {
      return new TMLLazilyParsedNumber(str);
    }
    return null;
  }

}
