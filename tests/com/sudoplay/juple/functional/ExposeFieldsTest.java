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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Type;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLInstanceCreator;
import com.sudoplay.juple.classparser.annotations.Expose;

public class ExposeFieldsTest {

  private Juple juple = new JupleBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .registerTypeAdapter(SomeInterface.class, new SomeInterfaceInstanceCreator())
      .create();

  @Test
  public void testNullExposeFieldSerialization() throws Exception {
    ClassWithExposedFields object = new ClassWithExposedFields(null, 1);
    String tml = juple.toTML(object);
    assertEquals(object.getExpectedTML(), tml);
  }

  @Test
  public void testArrayWithOneNullExposeFieldObjectSerialization()
      throws Exception {
    ClassWithExposedFields object1 = new ClassWithExposedFields(1, 1);
    ClassWithExposedFields object2 = new ClassWithExposedFields(null, 1);
    ClassWithExposedFields object3 = new ClassWithExposedFields(2, 2);
    ClassWithExposedFields[] objects = { object1, object2, object3 };

    String tml = juple.toTML(objects);
    String expected = new StringBuilder().append('[')
        .append(object1.getExpectedTML()).append(object2.getExpectedTML())
        .append(object3.getExpectedTML()).append(']').toString();

    assertEquals(expected, tml);
  }

  @Test
  public void testExposeAnnotationSerialization() throws Exception {
    ClassWithExposedFields target = new ClassWithExposedFields(1, 2);
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testExposeAnnotationDeserialization() throws Exception {
    String tml = "[[a|3][b|4][d|20.0]]";
    ClassWithExposedFields target = juple.fromTML(tml,
        ClassWithExposedFields.class);

    assertEquals(3, (int) target.a);
    assertNull(target.b);
    assertFalse(target.d == 20);
  }

  @Test
  public void testNoExposedFieldDeserialization() throws Exception {
    String tml = "[[a|4][b|5]]";
    ClassWithNoExposedFields obj = juple.fromTML(tml,
        ClassWithNoExposedFields.class);

    assertEquals(0, obj.a);
    assertEquals(1, obj.b);
  }

  @Test
  public void testExposedInterfaceFieldSerialization() throws Exception {
    String expected = "[[interfaceField|]]";
    ClassWithInterfaceField target = new ClassWithInterfaceField(
        new SomeObject());
    String actual = juple.toTML(target);

    assertEquals(expected, actual);
  }

  @Test
  public void testExposedInterfaceFieldDeserialization() throws Exception {
    String tml = "[[interfaceField|]]";
    ClassWithInterfaceField obj = juple.fromTML(tml,
        ClassWithInterfaceField.class);

    assertNotNull(obj.interfaceField);
  }

  @Test
  public void testNoExposedFieldSerialization() throws Exception {
    ClassWithNoExposedFields obj = new ClassWithNoExposedFields();
    String tml = juple.toTML(obj);

    assertEquals("[]", tml);
  }

  private static class ClassWithExposedFields {
    @Expose
    private final Integer a;
    private final Integer b;
    @Expose(serialize = false)
    final long c;
    @Expose(deserialize = false)
    final double d;
    @Expose(serialize = false, deserialize = false)
    final char e;

    public ClassWithExposedFields(Integer a, Integer b) {
      this(a, b, 1L, 2.0, 'a');
    }

    public ClassWithExposedFields(Integer a, Integer b, long c, double d, char e) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.e = e;
    }

    public String getExpectedTML() {
      StringBuilder sb = new StringBuilder("[");
      if (a == null) {
        sb.append("[a|").append("\\0").append("]");
      } else {
        sb.append("[a|").append(a).append("]");
      }
      sb.append("[d|").append(d).append("]");
      sb.append("]");
      return sb.toString();
    }
  }

  private static class ClassWithNoExposedFields {
    private final int a = 0;
    private final int b = 1;
  }

  private static interface SomeInterface {
    // Empty interface
  }

  private static class SomeObject implements SomeInterface {
    // Do nothing
  }

  private static class SomeInterfaceInstanceCreator implements
      TMLInstanceCreator<SomeInterface> {
    public SomeInterface createInstance(Type type) {
      return new SomeObject();
    }
  }

  private static class ClassWithInterfaceField {
    @Expose
    private final SomeInterface interfaceField;

    public ClassWithInterfaceField(SomeInterface interfaceField) {
      this.interfaceField = interfaceField;
    }
  }
}
