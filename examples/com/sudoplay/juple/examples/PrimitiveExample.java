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

package com.sudoplay.juple.examples;

import java.io.IOException;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;

/**
 * Primitive examples.
 * 
 * @author Jason Taylor
 */
public class PrimitiveExample {

  public static void main(String[] args) {
    PrimitiveExample app = new PrimitiveExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public void run() throws IOException {

    Object value;
    String tml;

    Juple juple = new JupleBuilder().setPrettyPrinting().create();

    tml = juple.toTML(42);
    System.out.println("Serialized int: " + tml);

    value = juple.fromTML(tml, int.class);
    System.out.println("Deserialized int: " + value);

    /*
     * You can serialize a primitive as null...
     */

    tml = juple.toTML(null, int.class);
    System.out.println("Serialized a null: " + tml);

    /*
     * ... but cannot deserialize a null as primitive ...
     */

    try {
      @SuppressWarnings("unused")
      int i = juple.fromTML(tml, int.class);
    } catch (NullPointerException e) {
      System.out.println("Deserializing a null as a primitive throws: "
          + e.getClass().getSimpleName());
      System.out
          .println("... because null values can't be assigned to primitives.");
    }

    /*
     * You can, however, deserialize a null value as a primitive wrapper like
     * Integer or Boolean.
     */

    Integer nullInt = juple.fromTML(tml, Integer.class);
    System.out.println("Deserialized null as primitive wrapper: " + nullInt);

  }

}
