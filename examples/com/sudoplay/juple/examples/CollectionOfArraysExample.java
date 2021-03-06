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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;

/**
 * {@link Collection} of {@link Array}s example.
 * 
 * @author Jason Taylor
 */
public class CollectionOfArraysExample {

  public static void main(String[] args) {
    CollectionOfArraysExample app = new CollectionOfArraysExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public void run() throws IOException {

    Juple juple = new JupleBuilder().setPrettyPrinting().create();

    Type type = new TMLTypeToken<ArrayList<String[]>>() {}.getType();
    ArrayList<String[]> l = new ArrayList<String[]>();
    l.add(new String[] { "Amazing\\sstring", "test!" });
    l.add(new String[] { "Another", "trial", "run!" });
    String tml = juple.toTML(l, type);
    System.out.println(tml);

    ArrayList<String[]> actual = juple.fromTML(tml, type);
    for (String[] array : actual) {
      System.out.println("===");
      for (String s : array) {
        System.out.println(s);
      }
    }

  }

}
