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

package com.sudoplay.juple.error;

import java.io.IOException;
import java.lang.reflect.Type;

import com.sudoplay.juple.stream.TMLReader;

/**
 * This class contains static methods for asserting various conditions and
 * throwing various exceptions.
 * 
 * @author Jason Taylor
 */
public class TMLContract {

  private TMLContract() {}

  /**
   * Throws a {@link NullPointerException} if the supplied object is null.
   * 
   * @param o
   * @return the supplied object
   */
  public static <T> T checkNotNull(T o) {
    if (o == null) throw new NullPointerException();
    return o;
  }

  /**
   * Throws an {@link IllegalArgumentException} if the supplied condition is
   * false.
   * 
   * @param condition
   */
  public static void checkArgument(boolean condition) {
    if (false == condition) throw new IllegalArgumentException();
  }

  /**
   * Throws an {@link IllegalStateException} if the supplied condition is false.
   * 
   * @param condition
   */
  public static void checkState(boolean condition) {
    if (false == condition) throw new IllegalStateException();
  }

  /**
   * Throws a {@link TMLSyntaxException} if the supplied condition is false.
   * 
   * @param condition
   */
  public static void checkSyntax(boolean condition) {
    if (false == condition) throw new TMLSyntaxException();
  }

  /**
   * Asserts that the supplied type is not a primitive type.
   * 
   * @param type
   */
  public static void checkNotPrimitive(Type type) {
    checkArgument(!(type instanceof Class<?>)
        || !((Class<?>) type).isPrimitive());
  }

  /**
   * Asserts that if {@code obj} is not null, the {@code reader} has reached
   * EOF.
   * 
   * @param obj
   * @param reader
   */
  public static void assertFullConsumption(Object obj, TMLReader reader) {
    try {
      if (obj != null) reader.assertFullConsumption();
    } catch (IOException e) {
      throw new TMLIOException(e);
    }
  }

}
