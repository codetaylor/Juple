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

/**
 * A pool of string instances. Unlike the {@link String#intern() VM's interned
 * strings}, this pool provides no guarantee of reference equality. It is
 * intended only to save allocations. This class is not thread safe.
 * 
 * <p>
 * This class has been derived from the google-gson source <a
 * href="https://code.google.com/p/google-gson/"
 * >https://code.google.com/p/google-gson/</a>.
 * 
 * <p>
 * This class contains trivial modifications.
 * 
 * @author original unknown
 * @author Jason Taylor (modified for Juple)
 */
final class TMLStringPool {

  /**
   * The maximum length of strings to add to the pool. Strings longer than this
   * don't benefit from pooling because we spend more time on pooling than we
   * save on garbage collection.
   */
  private static final int MAX_LENGTH = 20;
  private final String[] pool = new String[1024];

  /**
   * Returns a string equal to {@code new String(array, start, length)}.
   */
  public String get(char[] array, int start, int length, int hashCode) {
    if (length > TMLStringPool.MAX_LENGTH) {
      return new String(array, start, length);
    }

    // Pick a bucket using Doug Lea's supplemental secondaryHash function (from
    // HashMap)
    hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
    hashCode ^= (hashCode >>> 7) ^ (hashCode >>> 4);
    int index = hashCode & (pool.length - 1);

    String pooled = pool[index];
    if (pooled == null || pooled.length() != length) {
      String result = new String(array, start, length);
      pool[index] = result;
      return result;
    }

    for (int i = 0; i < length; i++) {
      if (pooled.charAt(i) != array[start + i]) {
        String result = new String(array, start, length);
        pool[index] = result;
        return result;
      }
    }

    return pooled;
  }
}
