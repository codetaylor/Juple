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

import java.util.List;

import org.junit.Test;

import com.sudoplay.juple.tree.TMLNode;
import com.sudoplay.juple.tree.TMLNodeTreeParser;

/**
 * This will test the multiple pattern matching at all depths.
 * 
 * @author Jason Taylor
 * 
 */
public class TMLNode_FindAllGreedy {

  private TMLNodeTreeParser parser;

  public TMLNode_FindAllGreedy() {
    this.parser = new TMLNodeTreeParser();
  }

  @Test
  public void test() {

    assertMatch("[[a b] [c d] [e f]]", "[c d]", new String[] { "[c d]" });
    assertMatch("[[a b] [c d] [e f]]", "[ \\? \\? ]", new String[] { "[a b]",
        "[c d]", "[e f]" });
    assertMatch("[[a b] [c d] [e f]]", "[ \\? f ]", new String[] { "[e f]" });

    assertMatch("[bold | hello, [italic | this] is a test!]", "[italic|\\*]",
        new String[] { "[[italic] [this]]" });
  }

  private void assertMatch(String input, String pattern, String[] expected) {
    TMLNode node = parser.parse(input);
    List<TMLNode> nodes = node.findAllGreedy(pattern);
    for (int i = 0; i < nodes.size(); i++) {
      TMLNode n = nodes.get(i);
      assertEquals(expected[i], n.toString());
    }
  }

}
