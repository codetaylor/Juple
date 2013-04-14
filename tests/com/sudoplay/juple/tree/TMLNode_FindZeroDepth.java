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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.sudoplay.juple.tree.TMLNode;
import com.sudoplay.juple.tree.TMLNodeTreeParser;

/**
 * This will test the single pattern matching at zero depth. Most of the test
 * cases have been gleaned from the <a
 * href="https://github.com/judnich/TupleMarkup">TupleMarkup</a> repository.
 * 
 * @author Jason Taylor
 * 
 */
public class TMLNode_FindZeroDepth {

  private TMLNodeTreeParser parser;

  public TMLNode_FindZeroDepth() {
    this.parser = new TMLNodeTreeParser();
  }

  @Test
  public void test() {
    /*
     * [a b c] matches [\? \? \?]. It also matches [\*], [\? \*], [\? \? \*],
     * and [\? \? \? \*].
     */
    assertMatch("[a b c]", "[\\? \\? \\?]");
    assertMatch("[a b c]", "[\\*]");
    assertMatch("[a b c]", "[\\? \\*]");
    assertMatch("[a b c]", "[\\? \\? \\*]");
    assertMatch("[a b c]", "[\\? \\? \\? \\*]");

    /*
     * [a b c] matches [\? b c]. [a b c] matches [a \? \?]. [a b c] does not
     * match [\? hello hi].
     */
    assertMatch("[a b c]", "[\\? b c]");
    assertMatch("[a b c]", "[a \\? \\?]");
    assertNoMatch("[a b c]", "[\\? hello hi]");

    /*
     * The pattern [\? \*] expects one or more nodes, which accepts [a], [a b],
     * [a b c], etc.
     */
    assertMatch("[a]", "[\\? \\*]");
    assertMatch("[a b]", "[\\? \\*]");
    assertMatch("[a b c]", "[\\? \\*]");

    /*
     * Pattern [\*] expects zero or more nodes. So this would match [], [a], [a
     * b], [a b c], etc.
     */
    assertMatch("[]", "[\\*]");
    assertMatch("[a]", "[\\*]");
    assertMatch("[a b]", "[\\*]");
    assertMatch("[a b c]", "[\\*]");

    /*
     * [\?] matches [ [a b c] ], because the \? wildcard matches the single
     * nested list.
     */
    assertMatch("[ [a b c] ]", "[\\?]", "[[a b c]]");

    /*
     * Your patterns can be nested as deeply as you like, and can contain
     * non-wildcard items of course to match against. For example, say we want
     * to match TML code like "[[bold] [hello this is a test]]" (or equivalently
     * "[bold | hello this is a test]") in such a way that the right nested list
     * can contain anything at all. So the same pattern should also match
     * "[bold | pattern matching is fun]". The following pattern achieves this:
     * "[bold | \*]". Pretty simple and easy once you understand how it works.
     * In this case "[bold | \*]" (which could also be written "[[bold] [\*]]"
     * with absolutely no functional difference) matches any nodes that contain
     * a nested "[bold]" on the left and a nested list of anything on the right.
     */
    assertMatch("[[bold] [hello this is a test]]", "[[bold] [\\*]]");
    assertMatch("[bold | hello this is a test]", "[[bold] [\\*]]",
        "[[bold] [hello this is a test]]");
    assertMatch("[[bold] [hello this is a test]]", "[bold | \\*]");
    assertMatch("[bold | hello this is a test]", "[bold | \\*]",
        "[[bold] [hello this is a test]]");

    /*
     * Additional test cases.
     */
    assertMatch("[]", "[]");
    assertMatch("[|]", "[[][]]", "[[] []]");
    assertMatch("[a [b] c]", "[a [b] c]");

    assertNoMatch("[|]", "[]");
    assertNoMatch("[a b c]", "[a b c d]");
    assertNoMatch("[a b c d]", "[a b c]");
    assertNoMatch("[a b c]", "[c b a]");
    assertNoMatch("[a b c]", "[a b d]");
    assertNoMatch("[c a b]", "[d a b]");

    assertMatch("[]", "[\\*]");
    assertMatch("[a]", "[\\*]");
    assertMatch("[a b c]", "[\\*]");
    assertMatch("[[a] [b c]]", "[\\*]");

    assertNoMatch("[]", "[\\? \\*]");
    assertMatch("[a b]", "[\\? \\*]");
    assertMatch("[a b]", "[\\? \\? \\*]");
    assertMatch("[a b c]", "[\\? \\? \\*]");
    assertMatch("[a b c]", "[\\? \\*]");
    assertMatch("[a b c d]", "[\\? \\*]");
    assertMatch("[[]]", "[\\?]");

    assertMatch("[this is tml]", "[this is \\?]");
    assertMatch("[this is tml]", "[this \\? tml]");
    assertMatch("[this is tml]", "[\\? is tml]");
    assertMatch("[this is tml]", "[\\? is \\?]");

    assertNoMatch("[test is tml]", "[this is \\?]");
    assertNoMatch("[test is tml]", "[this \\? tml]");

    assertMatch("[[a b] [c d] [e f]]", "[[\\? b] [c \\?] [\\? f]]");
    assertMatch("[[a b] [c d] [e f]]", "[ \\? \\? | \\? \\? | \\? \\? ]");
    assertMatch("[[a b] [c d] [e f]]", "[[\\*] [\\? \\*] [\\? \\? \\*]]");

    assertMatch("[bold | hello, [italic | this] is a test!]", "[bold|\\*]",
        "[[bold] [hello, [[italic] [this]] is a test!]]");
  }

  private void assertMatch(String input, String pattern) {
    assertMatch(input, pattern, input);
  }

  private void assertMatch(String input, String pattern, String expected) {
    TMLNode node = parser.parse(input);
    assertEquals(expected, node.find(pattern, 0).toString());
  }

  private void assertNoMatch(String input, String pattern) {
    TMLNode node = parser.parse(input);
    assertNull(node.find(pattern, 0));
  }

}
