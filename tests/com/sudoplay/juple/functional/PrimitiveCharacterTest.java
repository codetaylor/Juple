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

/**
 * Functional tests for Java Character values.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class PrimitiveCharacterTest {

  private Juple juple = new Juple();

  @Test
  public void testPrimitiveCharacterAutoboxedSerialization() {
    assertEquals("[A]", juple.toTML('A'));
    assertEquals("[A]", juple.toTML('A', char.class));
    assertEquals("[A]", juple.toTML('A', Character.class));
  }

  @Test
  public void testPrimitiveCharacterAutoboxedDeserialization() {
    char expected = 'a';
    char actual = juple.fromTML("[a]", char.class);
    assertEquals(expected, actual);

    actual = juple.fromTML("[a]", char.class);
    assertEquals(expected, actual);

    actual = juple.fromTML("[a]", Character.class);
    assertEquals(expected, actual);
  }

}
