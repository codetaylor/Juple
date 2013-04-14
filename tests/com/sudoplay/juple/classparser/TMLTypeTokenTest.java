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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import org.junit.Test;

import com.sudoplay.juple.classparser.TMLTypeToken;

/**
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class TMLTypeTokenTest {

  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  @Test
  @SuppressWarnings("deprecation")
  public void testIsAssignableFromRawTypes() {
    assertTrue(TMLTypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TMLTypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TMLTypeToken.get(RandomAccess.class).isAssignableFrom(
        ArrayList.class));
    assertFalse(TMLTypeToken.get(ArrayList.class).isAssignableFrom(
        RandomAccess.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TMLTypeToken.get(a).isAssignableFrom(a));
    assertTrue(TMLTypeToken.get(b).isAssignableFrom(b));

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertFalse(TMLTypeToken.get(a).isAssignableFrom(b));
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertFalse(TMLTypeToken.get(b).isAssignableFrom(a));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TMLTypeToken.get(a).isAssignableFrom(a));
    assertTrue(TMLTypeToken.get(b).isAssignableFrom(b));

    // listOfString = listOfUnknown // doesn't compile; must be false
    assertFalse(TMLTypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; // compiles; must be true
    // This assertion is too difficult to support reliably, so disabling
    // assertTrue(TMLTypeToken.get(b).isAssignableFrom(a));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TMLTypeToken.get(a).isAssignableFrom(a));
    assertTrue(TMLTypeToken.get(b).isAssignableFrom(b));

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertFalse(TMLTypeToken.get(a).isAssignableFrom(b));
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertFalse(TMLTypeToken.get(b).isAssignableFrom(a));
  }

}
