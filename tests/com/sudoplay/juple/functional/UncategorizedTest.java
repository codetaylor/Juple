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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;
import com.sudoplay.juple.common.TestTypes.ClassOverridingEquals;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Functional tests that do not fall neatly into any of the existing
 * classification.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class UncategorizedTest {

  private Juple juple = new Juple();

  @Test
  public void testInvalidTMLDeserializationFails() throws Exception {
    try {
      juple.fromTML("[adfasdf1112,,,\":]", BagOfPrimitives.class);
      fail("Bad TML should throw a TMLSyntaxException");
    } catch (TMLSyntaxException expected) {}

    try {
      juple.fromTML("[adfasdf1112,,,\":]", BagOfPrimitives.class);
      fail("Bad TML should throw a TMLSyntaxException");
    } catch (TMLSyntaxException expected) {}
  }

  @Test
  public void testObjectEqualButNotSameSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;
    String tml = juple.toTML(objB);
    assertEquals("[[ref|[ref|\\0]]]", tml);
  }

  @Test
  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertFalse(juple.toTML(target).contains("DEFAULT_VALUE"));
  }

  @Test
  public void testJupleInstanceReusableForSerializationAndDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    String tml = juple.toTML(bag);
    BagOfPrimitives deserialized = juple.fromTML(tml, BagOfPrimitives.class);
    assertEquals(bag, deserialized);
  }

  @Test
  public void testReturningDerivedClassesDuringDeserialization() {
    Juple juple = new JupleBuilder().registerTypeAdapter(Base.class,
        new BaseTypeAdapter()).create();
    String tml = "[[opType|OP1]]";
    Base base = juple.fromTML(tml, Base.class);
    assertTrue(base instanceof Derived1);
    assertEquals(OperationType.OP1, base.opType);

    tml = "[[opType|OP2]]";
    base = juple.fromTML(tml, Base.class);
    assertTrue(base instanceof Derived2);
    assertEquals(OperationType.OP2, base.opType);
  }

  private enum OperationType {
    OP1, OP2
  }

  private static class Base {
    OperationType opType;
  }

  private static class Derived1 extends Base {
    Derived1() {
      opType = OperationType.OP1;
    }
  }

  private static class Derived2 extends Base {
    Derived2() {
      opType = OperationType.OP2;
    }
  }

  private static class BaseTypeAdapter extends TMLTypeAdapter<Base> {

    @Override
    public Base read(TMLReader in) throws IOException {
      in.beginList();
      in.nextName();
      String opTypeStr = in.nextString();
      OperationType opType = OperationType.valueOf(opTypeStr);
      in.endList();
      switch (opType) {
      case OP1:
        return new Derived1();
      case OP2:
        return new Derived2();
      }
      throw new TMLSyntaxException("unknown type: " + opType);
    }

    @Override
    public void write(TMLWriter out, Base value) throws IOException {
      // do nothing
    }

  }

}
