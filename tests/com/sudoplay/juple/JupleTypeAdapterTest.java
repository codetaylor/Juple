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

package com.sudoplay.juple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

public class JupleTypeAdapterTest {

  private Juple juple;

  public JupleTypeAdapterTest() {
    juple = new JupleBuilder()
        .registerTypeAdapter(AtomicLong.class, new ExceptionTypeAdapter())
        .registerTypeAdapter(AtomicInteger.class,
            new AtomicIntegerTypeAdapter()).create();
  }

  @Test(expected = TMLSyntaxException.class)
  public void testDefaultTypeAdapterThrowsSyntaxException() throws Exception {
    juple.fromTML("[abc|123]", BigInteger.class);
  }

  @Test
  public void testTypeAdapterThrowsException() throws Exception {
    try {
      juple.toTML(new AtomicLong(0));
      fail("Type Adapter should have thrown an exception");
    } catch (IllegalStateException expected) {}

    try {
      juple.fromTML("[123]", AtomicLong.class);
      fail("Type Adapter should have thrown an exception");
    } catch (TMLSyntaxException expected) {}
  }

  @Test
  public void testTypeAdapterProperlyConvertsTypes() throws Exception {
    int intialValue = 1;
    AtomicInteger atomicInt = new AtomicInteger(intialValue);
    String tml = juple.toTML(atomicInt);
    assertEquals("[2]", tml);

    atomicInt = juple.fromTML(tml, AtomicInteger.class);
    assertEquals(intialValue, atomicInt.get());
  }

  @Test
  public void testTypeAdapterDoesNotAffectNonAdaptedTypes() throws Exception {
    String expected = "blah";
    String actual = juple.toTML(expected);
    assertEquals("[" + expected + "]", actual);

    actual = juple.fromTML(actual, String.class);
    assertEquals(expected, actual);
  }

  private static class ExceptionTypeAdapter extends TMLTypeAdapter<AtomicLong> {
    @Override
    public AtomicLong read(TMLReader in) throws IOException {
      throw new IllegalStateException();
    }

    @Override
    public void write(TMLWriter out, AtomicLong value) throws IOException {
      throw new IllegalStateException();
    }
  }

  private static class AtomicIntegerTypeAdapter extends
      TMLTypeAdapter<AtomicInteger> {
    @Override
    public AtomicInteger read(TMLReader in) throws IOException {
      int intValue = Integer.parseInt(in.nextString());
      return new AtomicInteger(--intValue);
    }

    @Override
    public void write(TMLWriter out, AtomicInteger value) throws IOException {
      out.value(value.incrementAndGet());
    }
  }

}
