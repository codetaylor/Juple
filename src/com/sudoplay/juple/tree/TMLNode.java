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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sudoplay.juple.classparser.TMLLazilyParsedNumber;
import com.sudoplay.juple.classparser.TMLNumberUtil;
import com.sudoplay.juple.error.TMLContract;

public class TMLNode implements Cloneable {

  public enum Type {
    LIST, STRING, NUMBER, BOOLEAN, NULL, DIVIDER
  }

  public static final String MATCH_ONE = "\\?";
  public static final String MATCH_ANY = "\\*";

  private String data = "";
  private List<TMLNode> list;
  private Type type = null;
  private TMLLazilyParsedNumber number = null;
  private boolean ignoreDividers = false;

  /**
   * Initialize a new node as an empty list.
   */
  public TMLNode() {
    this.type = Type.LIST;
  }

  /**
   * Initialize a new node as a data node.
   * 
   * @param data
   */
  public TMLNode(String data) {
    if (data == null) {
      this.data = "\\0";
    } else {
      this.data = data;
    }
  }

  /**
   * Initialize a new node as a data node.
   * 
   * @param data
   */
  public TMLNode(long data) {
    this(Long.toString(data));
  }

  /**
   * Initialize a new node as a data node.
   * 
   * @param data
   */
  public TMLNode(double data) {
    this(Double.toString(data));
  }

  /**
   * Initialize a new node as a data node.
   * 
   * @param data
   */
  public TMLNode(boolean data) {
    this(Boolean.toString(data));
  }

  /**
   * @return true if this node contains a string; will return false if the
   *         lowercase version of the string is 'true' or 'false' as it is
   *         recognized as a boolean value instead
   */
  public boolean isString() {
    return getType() == Type.STRING;
  }

  /**
   * @return true if this node contains a number
   * @see TMLNumberUtil#isNumeric(String)
   */
  public boolean isNumber() {
    return getType() == Type.NUMBER;
  }

  /**
   * @return true if this node contains a boolean
   */
  public boolean isBoolean() {
    return getType() == Type.BOOLEAN;
  }

  /**
   * @return true if this node contains a null value
   */
  public boolean isNull() {
    return getType() == Type.NULL;
  }

  /**
   * @return true if this node contains a list of nodes
   */
  public boolean isList() {
    return getType() == Type.LIST;
  }

  /**
   * @return true if this node contains a divider
   */
  public boolean isDivider() {
    return getType() == Type.DIVIDER;
  }

  /**
   * @return the data contained in this node as a string
   */
  public String getAsString() {
    return data;
  }

  /**
   * @return the data contained in this node as a byte
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public byte getAsByte() {
    expectType(Type.NUMBER);
    return new TMLLazilyParsedNumber(data).byteValue();
  }

  /**
   * @return the data contained in this node as a short
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public short getAsShort() {
    expectType(Type.NUMBER);
    return new TMLLazilyParsedNumber(data).shortValue();
  }

  /**
   * @return the data contained in this node as an int
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public int getAsInt() {
    expectType(Type.NUMBER);
    return number.intValue();
  }

  /**
   * @return the data contained in this node as a long
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public long getAsLong() {
    expectType(Type.NUMBER);
    return TMLLazilyParsedNumber.get(data).longValue();
  }

  /**
   * @return the data contained in this node as a float
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public double getAsFloat() {
    expectType(Type.NUMBER);
    return TMLLazilyParsedNumber.get(data).floatValue();
  }

  /**
   * @return the data contained in this node as a double
   * @throws IllegalStateException
   *           if this node's type is not NUMBER
   */
  public double getAsDouble() {
    expectType(Type.NUMBER);
    return TMLLazilyParsedNumber.get(data).doubleValue();
  }

  /**
   * @return the data contained in this node as a boolean
   * @throws IllegalStateException
   *           if this node's type is not BOOLEAN
   */
  public boolean getAsBoolean() {
    expectType(Type.BOOLEAN);
    return Boolean.parseBoolean(data);
  }

  public Iterator<TMLNode> iterator() {
    return list.iterator();
  }

  private void expectType(Type expectedType) {
    if (getType() != expectedType) {
      throw new IllegalStateException("Expected " + expectedType + " but was "
          + type);
    }
  }

  private Type getType() {
    if (type != null) return type;

    if ("".equals(data)) {
      return Type.LIST;
    } else if ("\\0".equals(data)) {
      return Type.NULL;
    } else if (data.toLowerCase().equals("true")
        || data.toLowerCase().equals("false")) {
      return Type.BOOLEAN;
    } else if (TMLNumberUtil.isNumeric(data)) {
      number = new TMLLazilyParsedNumber(data);
      return Type.NUMBER;
    } else if ("|".equals(data)) {
      return Type.DIVIDER;
    } else {
      return Type.STRING;
    }
  }

  /**
   * Initialize a new node. The supplied node will be added to the child list.
   * If the supplied node is null, an exception is thrown.
   * 
   * @param node
   */
  public TMLNode(TMLNode node) {
    add(node);
  }

  /**
   * @return the value of this node
   */
  public String getValue() {
    return this.data;
  }

  /**
   * Adds the supplied node to this node's child list. If the supplied node is
   * null, an exception is thrown.
   * 
   * @param node
   */
  public void add(TMLNode node) {
    TMLContract.checkNotNull(node);
    if (list == null) {
      this.data = "";
      list = new ArrayList<TMLNode>(2);
    }
    list.add(node);
  }

  /**
   * @param index
   * @return the child node at index
   */
  public TMLNode getNode(int index) {
    if (list == null) return null;
    return list.get(index);
  }

  /**
   * This node node is wrapped in a new list and returned. Any changes made to
   * the node in the returned list also affect this node as it is the same node.
   * 
   * @return this node wrapped in a new list
   */
  public TMLNode getAsNewList() {
    return new TMLNode(this);
  }

  /**
   * @return the size of this node's child list
   */
  public int getListSize() {
    if (list == null) return 0;
    return list.size();
  }

  /**
   * Convenience method that simply calls {@link #find(String, int)} with a
   * depth of -1, indicating that the entire tree should be searched beginning
   * with this node.
   * 
   * @param pattern
   *          string to parse and use as matching pattern
   * @return first {@link TMLNode} found that matches pattern
   * @see #find(TMLNode, int)
   */
  public TMLNode findGreedy(String pattern) {
    return find(pattern, -1);
  }

  /**
   * Convenience method that parses an input string into a TML tree and calls
   * {@link #find(TMLNode)}.
   * 
   * @param pattern
   *          string to parse and use as matching pattern
   * @param depth
   *          how many recursions to perform while searching the tree
   * @return first {@link TMLNode} found that matches pattern
   * @see #find(TMLNode, int)
   */
  public TMLNode find(String pattern, int depth) {
    TMLNodeTreeParser parser = new TMLNodeTreeParser(ignoreDividers);
    StringReader reader = new StringReader(pattern);
    return find(parser.parse(reader), depth);
  }

  /**
   * Convenience method that simply calls {@link #find(TMLNode, int)} with a
   * depth of -1, indicating that the entire tree should be searched beginning
   * with this node.
   * 
   * @param pattern
   *          {@link TMLNode} tree to use as matching pattern
   * @return first {@link TMLNode} found that matches pattern
   * @see #find(TMLNode, int)
   */
  public TMLNode findGreedy(TMLNode pattern) {
    return find(pattern, -1);
  }

  /**
   * Returns the first {@link TMLNode} found that matches the supplied pattern.
   * The supplied depth controls how many recursions to perform while searching.
   * Passing a depth of -1 will cause the search to traverse the entire tree,
   * attempting to match the pattern to every node.
   * 
   * @param pattern
   *          {@link TMLNode} tree to use as matching pattern
   * @param depth
   *          how many recursions to perform while searching the tree
   * @return first {@link TMLNode} found that matches pattern
   */
  public TMLNode find(TMLNode pattern, int depth) {

    if (equals(pattern)) {
      return this;
    }

    if (depth > 0) {
      depth -= 1;
    } else if (depth == 0) {
      return null;
    }

    TMLNode node;
    int len = getListSize();
    for (int i = 0; i < len; i++) {
      node = getNode(i).find(pattern, depth);
      if (node != null) {
        return node;
      }
    }

    return null;
  }

  /**
   * Convenience method that simply calls {@link #findAll(String, int)} with a
   * depth of -1, indicating that the entire tree should be searched beginning
   * with this node.
   * 
   * @param pattern
   *          string to parse and use as matching pattern
   * @return all {@link TMLNode}s found that matches pattern
   * @see #findAll(TMLNode, int)
   */
  public List<TMLNode> findAllGreedy(String pattern) {
    return findAll(pattern, -1);
  }

  /**
   * Convenience method that parses an input string into a TML tree and calls
   * {@link #findAll(TMLNode)}.
   * 
   * @param pattern
   *          string to parse and use as matching pattern
   * @return a {@link List} of all matching nodes
   * @see #findAll(TMLNode, int)
   */
  public List<TMLNode> findAll(String pattern, int depth) {
    TMLNodeTreeParser parser = new TMLNodeTreeParser(ignoreDividers);
    StringReader reader = new StringReader(pattern);
    return findAll(parser.parse(reader), depth);
  }

  /**
   * Convenience method that simply calls {@link #findAll(TMLNode, int)} with a
   * depth of -1, indicating that the entire tree should be searched beginning
   * with this node.
   * 
   * @param pattern
   *          {@link TMLNode} tree to use as matching pattern
   * @return all {@link TMLNode}s found that matches pattern
   * @see #findAll(TMLNode, int)
   */
  public List<TMLNode> findAllGreedy(TMLNode pattern) {
    return findAll(pattern, -1);
  }

  /**
   * Returns a {@link List} of all nodes that match the supplied pattern.
   * 
   * @param pattern
   *          {@link TMLNode} tree to use as matching pattern
   * @return all {@link TMLNode}s found that matches pattern
   */
  public List<TMLNode> findAll(TMLNode pattern, int depth) {

    List<TMLNode> results = new ArrayList<TMLNode>();

    if (equals(pattern)) {
      results.add(this);
    }

    if (depth > 0) {
      depth -= 1;
    } else if (depth == 0) {
      return results;
    }

    int len = getListSize();
    for (int i = 0; i < len; i++) {
      results.addAll(getNode(i).findAll(pattern, depth));
    }

    return results;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TMLNode)) {
      return false;
    }
    TMLNode o = (TMLNode) obj;
    if (o.data.equals(MATCH_ONE)) {
      return true;
    }
    if (getListSize() == 0 && o.getListSize() == 0) {
      return data.equals(o.data);
    }
    if (o.getListSize() == 0) {
      return false;
    }

    int clen = getListSize();
    int plen = o.getListSize();
    int i;

    for (i = 0; i < clen; i++) {
      if (i >= plen) {
        return false;
      }
      if (o.getNode(i).data.equals(MATCH_ANY)) {
        return true;
      }
      if (!getNode(i).equals(o.getNode(i))) {
        return false;
      }
    }

    if (i < plen) {
      if (o.getNode(i).data.equals(MATCH_ANY)) {
        return true;
      }
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return toString(false);
  }

  /**
   * Converts this node to its TML string equivalent.
   * 
   * <p>
   * Setting {@code encapsulate} to true will return the string with an extra
   * open delimiter at the beginning and an extra close delimiter at the end.
   * 
   * @param encapsulate
   * @return
   */
  public String toString(boolean encapsulate) {
    StringBuilder sb = new StringBuilder();
    if (encapsulate) sb.append('[');
    if (isList()) {
      sb.append('[');
      for (int i = 0; i < getListSize(); i++) {
        sb.append(list.get(i).toString());
        if (i < list.size() - 1) {
          //if (!list.get(i + 1).isDivider() && !list.get(i).isDivider()) {
            sb.append(' ');
          //}
        }
      }
      sb.append(']');
    } else {
      sb.append(data);
    }
    if (encapsulate) sb.append(']');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = data.hashCode();
    result = 73 * result + list.hashCode();
    return result;
  }

  @Override
  public TMLNode clone() {
    TMLNode clone = new TMLNode();
    clone.data = data;
    if (list != null) {
      for (TMLNode node : list) {
        clone.add(node.clone());
      }
    }
    return clone;
  }

  /**
   * Exposed internally.
   */
  void setIgnoreDividers(boolean ignore) {
    this.ignoreDividers = ignore;
  }

}
