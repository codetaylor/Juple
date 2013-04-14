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

package com.sudoplay.juple.tree;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sudoplay.juple.tree.TMLNode;
import com.sudoplay.juple.tree.TMLNodeTreeParser;

@RunWith(Parameterized.class)
public class TMLNodeTreeParser_EmptyTuples {

  private TMLNodeTreeParser parser;

  private String input;
  private String expectedOutput;

  public TMLNodeTreeParser_EmptyTuples(String input, String expectedOutput) {
    this.input = input;
    this.expectedOutput = expectedOutput;
    this.parser = new TMLNodeTreeParser();
  }

  @Parameters
  public static Collection<Object[]> data() {
    ArrayList<Object[]> list = new ArrayList<Object[]>();
    list.add(new Object[] { "[]", "[]" });
    list.add(new Object[] { "[[] [] []]", "[[] [] []]" });
    list.add(new Object[] { "[[] [] [] []]", "[[] [] [] []]" });
    list.add(new Object[] { "[ | ]", "[[] []]" });
    list.add(new Object[] { "[ | | ]", "[[] [] []]" });
    list.add(new Object[] { "[ | | | ]", "[[] [] [] []]" });
    return list;
  }

  @BeforeClass
  public static void initialize() {
    //
  }

  @AfterClass
  public static void cleanup() {
    //
  }

  @Test
  public void testFromTMLBufferedReader() throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(input));
    TMLNode result = parser.parse(reader);
    assertEquals(expectedOutput, result.toString());
  }

  @Test
  public void testFromTMLString() {
    TMLNode result = parser.parse(input);
    assertEquals(expectedOutput, result.toString());
  }
}
