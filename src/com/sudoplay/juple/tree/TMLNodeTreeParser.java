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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.error.TMLContract;
import com.sudoplay.juple.error.TMLIOException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;

/**
 * TMLTreeParser contains methods to parse and return a tree of {@link TMLNode}s
 * from a reader.
 * 
 * @author Jason Taylor
 * 
 */
public class TMLNodeTreeParser {

  private final boolean ignoreDividers;

  /**
   * Creates a {@link TMLNodeTreeParser} with default settings.
   * 
   * @see TMLNodeTreeParser#TMLNodeTreeParser(boolean)
   */
  public TMLNodeTreeParser() {
    this.ignoreDividers = false;
  }

  /**
   * Creates a {@link TMLNodeTreeParser}.
   * 
   * <p>
   * Set {@code ignoreDividers} to true to build a node tree that when converted
   * to a string will output TML that is compliant with {@link Juple}s class
   * parsing methods.
   * 
   * <p>
   * If {@code ignoreDividers} is set to false, {@link TMLNodeTreeParser} will
   * build node trees compliant with the original TML specification. When a
   * divider delimiter is encountered, the parser will create a nested tuple out
   * of each section it separates.
   * 
   * @param ignoreDividers
   */
  public TMLNodeTreeParser(boolean ignoreDividers) {
    this.ignoreDividers = ignoreDividers;
  }

  /**
   * Creates a {@link TMLNode} tree from a string.
   * 
   * @param string
   * @param groupStrings
   * @return
   * @see #fromTML(Reader, boolean)
   */
  public TMLNode parse(String string) {
    StringReader reader = new StringReader(string);
    TMLNode node = parse(reader);
    reader.close();
    return node;
  }

  /**
   * Creates a {@link TMLNode} tree from a reader.
   * 
   * <p>
   * NOTE: The caller is responsible for closing the reader.
   * 
   * @param reader
   * @param properties
   * @return
   * @throws TMLIOException
   *           if the underlying reader is unable to read the data
   * @see TMLReader.Property
   */
  public TMLNode parse(Reader reader) throws TMLIOException {
    try {
      TMLReader tmlReader = new TMLReader(reader);
      tmlReader.beginList();
      TMLNode node = parse(tmlReader, false);
      TMLContract.assertFullConsumption(node, tmlReader);
      return node;
    } catch (IOException e) {
      throw new TMLIOException(e);
    }
  }

  /**
   * Parses a stream of TML data into a hierarchy of {@link TMLNode}s.
   * 
   * <p>
   * NOTE: The caller is responsible for closing the reader.
   * 
   * @param reader
   *          the reader to parse data from
   * @param endAtBar
   *          if true, will stop parsing at a divider
   * @return
   * @throws IOException
   */
  private TMLNode parse(TMLReader reader, boolean endAtBar) throws IOException {
    TMLToken token;
    TMLNode node = new TMLNode();
    node.setIgnoreDividers(ignoreDividers);
    parse: while (true) {
      token = reader.peek();
      if (token == TMLToken.BEGIN_LIST) {
        reader.beginList();
        node.add(parse(reader, false));
      } else if (token == TMLToken.DIVIDER) {
        reader.consumeDivider();
        if (ignoreDividers) {
          TMLNode newNode = new TMLNode("|");
          newNode.setIgnoreDividers(ignoreDividers);
          node.add(newNode);
        } else {
          if (endAtBar) break;
          node = new TMLNode(node);
          while (true) {
            node.add(parse(reader, true));
            if (reader.getLastToken() == TMLToken.END_LIST) {
              break parse;
            }
          }
        }
      } else if (token == TMLToken.END_LIST) {
        reader.endList();
        break;
      } else if (token == TMLToken.DATA) {
        String data = reader.nextString();
        if (!data.equals("")) {
          TMLNode newNode = new TMLNode(data);
          newNode.setIgnoreDividers(ignoreDividers);
          node.add(newNode);
        }
      } else if (token == TMLToken.EOF) {
        break;
      } else {
        throw new IllegalStateException();
      }
    }
    return node;
  }

}
