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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;

/**
 * This shows how to use the TMLReader class. The reader class can be used to
 * create parsers for your own TML data structures.
 * 
 * @author Jason Taylor
 * 
 */
public class TMLReaderExample {

  public static void main(String[] args) {
    TMLReaderExample app = new TMLReaderExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  /**
   * This is a class we'll use to stuff our parsed message data into. The member
   * variables were left public for simplicity.
   */
  public class Message {
    public String username;
    public String message;

    @Override
    public String toString() {
      return username + " >> " + message;
    }
  }

  public void run() throws IOException {

    /*
     * Let's say we want to parse some messages that are in this format:
     * 
     * [ [ username | message ] [ username | message ] ... ]
     * 
     * First let's create the example TML string.
     */
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");
    sb.append("  [ Jayne | Testing, testing. Captain, can you hear me? ]\n");
    sb.append("  [ Mal   | I'm standing right here. ]\n");
    sb.append("  [ Jayne | You're coming through good and loud. ]\n");
    sb.append("  [ Mal   | 'Cause I'm standing right here. ]\n");
    sb.append("]\n");

    String tml = sb.toString();
    
    System.out.println("Original TML:\n" + tml);
    
    /*
     * Then, we create a message container to ... contain the messages.
     */
    List<Message> list = new ArrayList<Message>();

    /*
     * Next, we create the TMLReader wrapping a regular java.io.StringReader.
     */
    TMLReader reader = new TMLReader(new StringReader(tml));

    /*
     * After the setup, we assert the first open delimiter and start looping!
     */
    reader.beginList();
    int scope = reader.getScope();
    while (reader.hasNextInScope(scope)) {
      list.add(readMessage(reader));
    }
    reader.endList();

    /*
     * Once we have all our data, we assert that we've reached the EOF and all
     * open delimiters have been resolved, then close the file. The call to
     * assertFullConsumption() can be skipped if it's not important that you've
     * read all the data. It's really just a convenience method.
     */
    reader.assertFullConsumption();
    reader.close();

    System.out.println("Resulting message objects:");
    for (Message m : list) {
      System.out.println(m.toString());
    }
  }

  /**
   * Returns a Message object.
   * 
   * @param reader
   * @return
   * @throws IOException
   */
  private Message readMessage(TMLReader reader) throws IOException {
    Message msg = new Message();
    reader.beginList();
    msg.username = reader.nextString();
    reader.consumeDivider();
    StringBuilder sb = new StringBuilder();
    while(reader.peek() == TMLToken.DATA) {
      sb.append(reader.nextString());
      if (reader.peek() == TMLToken.DATA) {
        sb.append(' ');
      }
    }
    msg.message = sb.toString();
    reader.endList();
    return msg;
  }
}
