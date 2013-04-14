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

package com.sudoplay.juple.examples.stream;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.stream.TMLWriter;

/**
 * This shows how to use the TMLWriter class. The writer class can be used to
 * create exporters for your own TML data structures.
 * 
 * @author Jason Taylor
 * 
 */
public class TMLWriterExample {

  public static void main(String[] args) {
    TMLWriterExample app = new TMLWriterExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  /**
   * This is the class we want to use for source data.
   */
  public class Message {
    public String username;
    public String message;

    public Message(String username, String message) {
      this.username = username;
      this.message = message;
    }

    @Override
    public String toString() {
      return username + " >> " + message;
    }
  }

  public void run() throws IOException {

    /*
     * First, we'll create the same messages that we parsed in the TMLReader
     * example.
     */
    List<Message> list = new ArrayList<Message>();
    list.add(new Message("Jayne", "Testing, testing. Captain, can you hear me?"));
    list.add(new Message("Mal", "I'm standing right here."));
    list.add(new Message("Jayne", "You're coming through good and loud."));
    list.add(new Message("Mal", "'Cause I'm standing right here."));

    /*
     * Next, we create the TMLWriter and wrap a java.io.StringWriter.
     */
    Writer sw = new StringWriter();
    TMLWriter writer = new TMLWriter(sw);

    /*
     * Setting the indent string to anything other than null or empty string
     * will enable indented 'pretty' formatting on the writer. Try commenting
     * this out and see the difference.
     */
    writer.setIndent("  ");

    /*
     * After the setup, we write the first open delimiter and start looping!
     */
    writer.beginList();
    for (Message m : list) {
      writer.beginList();
      writer.value(m.username);
      writer.divider();
      writer.value(m.message);
      writer.endList();
    }
    writer.endList();

    /*
     * Closing the writer also asserts that all open delimiters have been
     * resolved.
     */
    writer.close();

    System.out.println(sw.toString());

  }

}
