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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.common.TestTypes.Base;
import com.sudoplay.juple.common.TestTypes.ClassWithBaseArrayField;
import com.sudoplay.juple.common.TestTypes.ClassWithBaseCollectionField;
import com.sudoplay.juple.common.TestTypes.ClassWithBaseField;
import com.sudoplay.juple.common.TestTypes.Nested;
import com.sudoplay.juple.common.TestTypes.Sub;

public class InheritanceTest {

  private Juple juple = new Juple();

  @Test
  public void testSubClassSerialization() throws Exception {
    SubTypeOfNested target = new SubTypeOfNested(new BagOfPrimitives(10, 20,
        false, "stringValue"), new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testSubClassDeserialization() throws Exception {
    String tml = "[[value|5][primitive1|[longValue|10][intValue|20][booleanValue|false][stringValue|stringValue]][primitive2|[longValue|30][intValue|40][booleanValue|true][stringValue|stringValue]]]";
    SubTypeOfNested target = juple.fromTML(tml, SubTypeOfNested.class);
    assertEquals(tml, target.getExpectedTML());
  }

  @Test
  public void testClassWithBaseFieldSerialization() {
    ClassWithBaseField sub = new ClassWithBaseField(new Sub());
    assertEquals("[[base|[subName|Sub][baseName|Base][serializerName|\\0]]]",
        juple.toTML(sub));
  }

  @Test
  public void testClassWithBaseFieldDeserialization() {
    String tml = "[[base|[subName|Sub][baseName|Base][serializerName|\\0]]]";
    ClassWithBaseField sub = new ClassWithBaseField(new Sub());
    assertEquals(sub, juple.fromTML(tml, ClassWithBaseField.class));
  }

  @Test
  public void testClassWithBaseArrayFieldSerialization() {
    Base[] baseClasses = new Base[] { new Sub(), new Sub() };
    ClassWithBaseArrayField sub = new ClassWithBaseArrayField(baseClasses);
    String expected = "[[base|[[subName|Sub][baseName|Base][serializerName|\\0]][[subName|Sub][baseName|Base][serializerName|\\0]]]]";
    assertEquals(expected, juple.toTML(sub));
  }

  @Test
  public void testClassWithBaseArrayFieldDeserialization() {
    String tml = "[[base|[[subName|Sub][baseName|Base][serializerName|\\0]][[subName|Sub][baseName|Base][serializerName|\\0]]]]";
    Base[] baseClasses = new Base[] { new Sub(), new Sub() };
    ClassWithBaseArrayField sub = new ClassWithBaseArrayField(baseClasses);
    assertEquals(sub, juple.fromTML(tml, ClassWithBaseArrayField.class));
  }

  @Test
  public void testClassWithBaseCollectionFieldSerialization() {
    Collection<Base> baseClasses = new ArrayList<Base>();
    baseClasses.add(new Sub());
    baseClasses.add(new Sub());
    ClassWithBaseCollectionField sub = new ClassWithBaseCollectionField(
        baseClasses);
    String expected = "[[base|[[subName|Sub][baseName|Base][serializerName|\\0]][[subName|Sub][baseName|Base][serializerName|\\0]]]]";
    assertEquals(expected, juple.toTML(sub));
  }

  @Test
  public void testClassWithBaseCollectionFieldDeserialization() {
    String tml = "[[base|[[subName|Sub][baseName|Base][serializerName|\\0]][[subName|Sub][baseName|Base][serializerName|\\0]]]]";
    Collection<Base> baseClasses = new ArrayList<Base>();
    baseClasses.add(new Sub());
    baseClasses.add(new Sub());
    ClassWithBaseCollectionField sub = new ClassWithBaseCollectionField(
        baseClasses);
    assertEquals(sub, juple.fromTML(tml, ClassWithBaseCollectionField.class));
  }

  @Test
  public void testBaseSerializedAsSub() {
    Base base = new Sub();
    String expected = "[[subName|Sub][baseName|Base][serializerName|\\0]]";
    assertEquals(expected, juple.toTML(base));
  }

  @Test
  public void testBaseDeserializedAsSub() {
    String tml = "[[subName|Sub][baseName|Base][serializerName|\\0]]";
    Base base = new Sub();
    assertEquals(base, juple.fromTML(tml, Sub.class));
  }

  @Test
  public void testBaseSerializedAsBaseWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    String expected = "[[baseName|Base][serializerName|\\0]]";
    assertEquals(expected, juple.toTML(base, Base.class));
  }

  @Test
  public void testBaseDeserializedAsBaseWhenSpecifiedWithExplicitType() {
    String tml = "[[baseName|Base][serializerName|\\0]]";
    Base base = new Base();
    Base actual = juple.fromTML(tml, Base.class);
    assertEquals(base, actual);
    assertTrue(actual instanceof Base);
    assertFalse(actual instanceof Sub);
  }

  @Test
  public void testBaseSerializedAsSubWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    String expected = "[[subName|Sub][baseName|Base][serializerName|\\0]]";
    assertEquals(expected, juple.toTML(base, Sub.class));
  }

  private static class SubTypeOfNested extends Nested {
    private final long value = 5;

    public SubTypeOfNested(BagOfPrimitives primitive1,
        BagOfPrimitives primitive2) {
      super(primitive1, primitive2);
    }

    @Override
    public void appendFields(StringBuilder sb) {
      sb.append("[value|").append(value).append("]");
      super.appendFields(sb);
    }
  }
}
