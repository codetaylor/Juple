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
 * Functional tests involving interfaces.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class InterfaceTest {

  private static final String OBJ_TML = "[[someStringValue|string value]]";
  private static final String OBJ_FIELD_TML = "[someStringValue|string value]";

  private Juple juple = new Juple();
  private TestObject obj = new TestObject("string value");

  @Test
  public void testSerializingObjectImplementingInterface() throws Exception {
    assertEquals(OBJ_TML, juple.toTML(obj));
  }

  @Test
  public void testSerializingInterfaceObjectField() throws Exception {
    TestObjectWrapper objWrapper = new TestObjectWrapper(obj);
    assertEquals("[[obj|" + OBJ_FIELD_TML + "]]", juple.toTML(objWrapper));
  }

  private static interface TestObjectInterface {
    // Holder
  }

  private static class TestObject implements TestObjectInterface {
    @SuppressWarnings("unused")
    private String someStringValue;

    private TestObject(String value) {
      this.someStringValue = value;
    }
  }

  private static class TestObjectWrapper {
    @SuppressWarnings("unused")
    private TestObjectInterface obj;

    private TestObjectWrapper(TestObjectInterface obj) {
      this.obj = obj;
    }
  }
}
