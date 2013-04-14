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
import java.util.ArrayList;
import java.util.Collection;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.tree.TMLNode;
import com.sudoplay.juple.tree.TMLNodeTreeParser;
import com.sudoplay.juple.tree.TMLNodeTreeParserBuilder;

public class RawCollectionsExample {

  static class Event {
    private String name;
    private String source;

    private Event(String name, String source) {
      this.name = name;
      this.source = source;
    }

    @Override
    public String toString() {
      return String.format("(name=%s, source=%s)", name, source);
    }
  }

  public static void main(String[] args) {
    RawCollectionsExample app = new RawCollectionsExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void run() throws IOException {

    Collection collection = new ArrayList();
    collection.add("Good news everyone!");
    collection.add(5);
    collection.add(new Event("GREETINGS", "guest"));

    Juple juple = new Juple();
    String tml = juple.toTML(collection);
    System.out.println("Using toTML(Object) on a raw collection: " + tml);

    TMLNodeTreeParser parser = new TMLNodeTreeParserBuilder()
        .setIgnoreDividers().create();
    TMLNode node = parser.parse(tml);

    String tmlMessage = node.getNode(0).toString();
    String tmlNumber = node.getNode(1).toString(true);
    String tmlEvent = node.getNode(2).toString();

    String message = juple.fromTML(tmlMessage, String.class);
    int number = juple.fromTML(tmlNumber, Integer.class);
    Event event = juple.fromTML(tmlEvent, Event.class);

    System.out.printf("Using Juple.fromTML(String, Class) to get: %s, %d, %s",
        message, number, event);

  }
}
