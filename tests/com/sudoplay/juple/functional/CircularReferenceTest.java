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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.common.TestTypes.ClassOverridingEquals;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class CircularReferenceTest {

  private Juple juple = new Juple();

  @Test
  public void testCircularSerialization() throws Exception {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    a.children.add(b);
    b.children.add(a);
    try {
      juple.toTML(a);
      fail("Circular types should not get printed!");
    } catch (StackOverflowError expected) {}
  }

  @Test
  public void testSelfReferenceSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    objA.ref = objA;

    try {
      juple.toTML(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (StackOverflowError expected) {}
  }

  @Test
  public void testSelfReferenceArrayFieldSerialization() throws Exception {
    ClassWithSelfReferenceArray objA = new ClassWithSelfReferenceArray();
    objA.children = new ClassWithSelfReferenceArray[] { objA };

    try {
      juple.toTML(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (StackOverflowError expected) {}
  }

  @Test
  public void testSelfReferenceCustomAdapterSerialization() throws Exception {
    ClassWithSelfReference obj = new ClassWithSelfReference();
    obj.child = obj;
    Juple juple = new JupleBuilder().registerTypeAdapter(
        ClassWithSelfReference.class,
        new TMLTypeAdapter<ClassWithSelfReference>() {

          @Override
          public ClassWithSelfReference read(TMLReader in) throws IOException {
            // TODO Auto-generated method stub
            return (ClassWithSelfReference) null;
          }

          @Override
          public void write(TMLWriter out, ClassWithSelfReference value)
              throws IOException {
            out.beginList();
            out.name("property");
            out.value("value");
            out.endList();
            out.beginList();
            out.name("child");
            write(out, value.child);
            out.endList();
          }

        }).create();
    try {
      juple.toTML(obj);
      fail("Circular reference to self can not be serialized!");
    } catch (StackOverflowError expected) {}
  }

  @Test
  public void testDirectedAcyclicGraphSerialization() throws Exception {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType c = new ContainsReferenceToSelfType();
    a.children.add(b);
    a.children.add(c);
    b.children.add(c);
    assertNotNull(juple.toTML(a));
  }

  @Test
  public void testDirectedAcyclicGraphDeserialization() throws Exception {
    String tml = "[[children|[[children|[[children|]]]][[children|]]]]";
    ContainsReferenceToSelfType target = juple.fromTML(tml,
        ContainsReferenceToSelfType.class);
    assertNotNull(target);
    assertEquals(2, target.children.size());
  }

  private static class ContainsReferenceToSelfType {
    Collection<ContainsReferenceToSelfType> children = new ArrayList<ContainsReferenceToSelfType>();
  }

  private static class ClassWithSelfReference {
    ClassWithSelfReference child;
  }

  private static class ClassWithSelfReferenceArray {
    @SuppressWarnings("unused")
    ClassWithSelfReferenceArray[] children;
  }
}
