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

package com.sudoplay.juple.classparser.adapters;

import java.io.IOException;

import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Converts Java objects to and from TML.
 * 
 * <h3>Defining a type's TML form</h3> By default Juple converts application
 * classes to TML using its built-in type adapters. If Juple's default TML
 * conversion isn't appropriate for a type, extend this class to customize the
 * conversion.
 * 
 * @author unknown (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public abstract class TMLTypeAdapter<T> {

  /**
   * Reads one TML value (a list, string, number, boolean or null) and converts
   * it to a Java object. Returns the converted object.
   * 
   * @return the converted Java object. May be null.
   */
  public abstract T read(TMLReader in)
      throws IOException;

  /**
   * Writes one TML value (a list, string, number, boolean or null) for
   * {@code value}.
   * 
   * @param value
   *          the Java object to write. May be null.
   */
  public abstract void write(TMLWriter out, T value)
      throws IOException;

  /**
   * @return true if this type adapter should encapsulate the data in a list at
   *         the root level, defaults to true
   */
  public boolean isRootEncapsulate() {
    return true;
  }

  /**
   * @return true if this type adapter should force encapsulation immediately
   *         after a field name, defaults to false
   */
  public boolean isFieldEncapsulate() {
    return false;
  }

  /**
   * @return true if this type adapter should encapsulate data elements inside
   *         of a collection or array, defaults to false
   */
  public boolean isArrayEncapsulate() {
    return false;
  }

  /**
   * Returns the remaining strings in the current scope, concatenated with a
   * single space. If a non-DATA token is encountered, a
   * {@link TMLSyntaxException} is thrown.
   * 
   * @param in
   * @return
   * @throws IOException
   */
  public static String getAllStringsInScope(TMLReader in) throws IOException {
    StringBuilder sb = new StringBuilder();
    int scope = in.getScope();
    stringGroup: while (in.hasNextInScope(scope)) {
      sb.append(in.nextString());
      switch (in.peek()) {
      case DATA:
        sb.append(' ');
        // fall through
      case END_LIST:
        continue;
      case EOF:
      case DIVIDER:
      case BEGIN_LIST:
      case NULL:
      case NULL_ARRAY:
        break stringGroup;
        //throw new TMLSyntaxException("Expecting DATA but was " + in.peek());
      }
    }
    return sb.toString();
  }

}
