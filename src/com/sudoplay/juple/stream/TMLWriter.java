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

package com.sudoplay.juple.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import com.sudoplay.juple.error.TMLContract;

public class TMLWriter implements Closeable, Flushable {

  private static final String[] REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['['] = "\\[";
    REPLACEMENT_CHARS['|'] = "\\|";
    REPLACEMENT_CHARS[']'] = "\\]";
    REPLACEMENT_CHARS['\\'] = "\\\\";
  }

  private TMLToken lastToken = null;

  private String indent;
  private String deferredName;
  private String divider = "|";

  private final Writer out;

  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    push(TMLScope.EMPTY_DOCUMENT);
  }

  private boolean immediateFieldChild = false;

  private SpaceEscapePolicy overrideSpaceEscapePolicy = null;
  private boolean enforceFiniteFloatingPointValues = false;

  /**
   * Creates a new instance that writes a TML-encoded stream to {@code out}. For
   * best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   */
  public TMLWriter(Writer out) {
    TMLContract.checkNotNull(out);
    this.out = out;
  }

  /**
   * Sets the indentation string to be repeated for each level of indentation in
   * the encoded document. If {@code indent.isEmpty()} the encoded document will
   * be compact. Otherwise the encoded document will be more human-readable.
   * 
   * @param indent
   *          a string containing only whitespace.
   */
  public final void setIndent(String indent) {
    if (indent.length() == 0) {
      this.indent = null;
      this.divider = "|";
    } else {
      this.indent = indent;
      this.divider = " | ";
    }
  }

  /**
   * Begins encoding a new list. Each call to this method must be paired with a
   * call to {@link #endList}.
   * 
   * @return this writer
   */
  public TMLWriter beginList() throws IOException {
    return open(TMLScope.EMPTY_LIST, "[");
  }

  /**
   * Ends encoding the current list.
   * 
   * @return this writer
   */
  public TMLWriter endList() throws IOException {
    return close(TMLScope.EMPTY_LIST, TMLScope.NONEMPTY_LIST, "]");
  }

  /**
   * Writes a divider.
   * 
   * @return
   * @throws IOException
   */
  public TMLWriter divider() throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    if (deferredName != null) {
      throw new IllegalStateException(
          "Expected value, open delimiter or close delimiter");
    }
    if (peek() == TMLScope.EMPTY_DOCUMENT) {
      throw new IllegalStateException("Expected initial open delimiter");
    }
    out.write(divider);
    lastToken = TMLToken.DIVIDER;
    return this;
  }

  /**
   * Writes a property name followed by a divider. Used when serializing class
   * member variables.
   * 
   * @param name
   *          the name of the forthcoming value. May not be null.
   * @return this writer.
   */
  public TMLWriter name(String name) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    assertNoSpaces(name);
    if (lastToken != TMLToken.BEGIN_LIST) {
      throw new IllegalStateException(
          "Expected open delimiter before name, was " + lastToken);
    }
    TMLContract.checkNotNull(name);
    if (stackSize == 0) {
      throw new IllegalStateException("TMLWriter is closed.");
    }
    if (peek() != TMLScope.EMPTY_LIST || lastToken != TMLToken.BEGIN_LIST
        || deferredName != null) {
      throw new IllegalStateException("Expected open delimiter");
    }
    deferredName = name;
    immediateFieldChild = true;
    return this;
  }

  /**
   * Convenience method that calls {@link #value(String, SpaceEscapePolicy)}
   * with the AUTO option. Strings written with this method will only have their
   * spaces escaped if more than one space occurs in succession.
   * 
   * @param value
   * @return
   * @throws IOException
   */
  public TMLWriter value(String value) throws IOException {
    return value(value, SpaceEscapePolicy.AUTO);
  }

  /**
   * Encodes {@code value}.
   * 
   * @param value
   *          the literal string value, or null to encode a null literal.
   * @return this writer
   */
  public TMLWriter value(String value, SpaceEscapePolicy escapeSpaces)
      throws IOException {
    if (overrideSpaceEscapePolicy != null)
      escapeSpaces = overrideSpaceEscapePolicy;
    assertNotClosed();
    assertScopeGreaterThanZero();
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue(false);
    string(value, escapeSpaces);
    return this;
  }

  /**
   * Encodes {@code null}.
   * 
   * @return this writer
   */
  public TMLWriter nullValue() throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    writeDeferredName();
    beforeValue(false);
    out.write("\\0");
    lastToken = TMLToken.NULL;
    return this;
  }

  /**
   * Encodes {@code nullArray}.
   * 
   * @return this writer
   */
  public TMLWriter nullArrayValue() throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    writeDeferredName();
    beforeValue(false);
    out.write("\\2");
    lastToken = TMLToken.NULL_ARRAY;
    return this;
  }

  /**
   * Encodes {@code value}.
   * 
   * @return this writer
   */
  public TMLWriter value(boolean value) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    writeDeferredName();
    beforeValue(false);
    out.write(value ? "true" : "false");
    lastToken = TMLToken.DATA;
    return this;
  }

  /**
   * Encodes {@code value}.
   * 
   * @param value
   *          a finite value. May not be {@link Double#isNaN() NaNs} or
   *          {@link Double#isInfinite() infinities}
   * @return this writer
   */
  public TMLWriter value(double value) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    if (enforceFiniteFloatingPointValues) {
      if (Double.isNaN(value) || Double.isInfinite(value)) {
        throw new IllegalArgumentException(
            "Numeric values must be finite, but was " + value);
      }
    }
    writeDeferredName();
    beforeValue(false);
    out.write(Double.toString(value));
    lastToken = TMLToken.DATA;
    return this;
  }

  /**
   * Encodes {@code value}.
   * 
   * @param value
   *          a finite value
   * @return this writer
   */
  public TMLWriter value(float value) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    if (enforceFiniteFloatingPointValues) {
      if (Double.isNaN(value) || Double.isInfinite(value)) {
        throw new IllegalArgumentException(
            "Numeric values must be finite, but was " + value);
      }
    }
    writeDeferredName();
    beforeValue(false);
    out.write(Float.toString(value));
    lastToken = TMLToken.DATA;
    return this;
  }

  /**
   * Encodes {@code value}.
   * 
   * @return this writer
   */
  public TMLWriter value(long value) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    writeDeferredName();
    beforeValue(false);
    out.write(Long.toString(value));
    lastToken = TMLToken.DATA;
    return this;
  }

  /**
   * Encodes {@code value}.
   * 
   * @param value
   *          a finite value. May not be {@link Double#isNaN() NaNs} or
   *          {@link Double#isInfinite() infinities}
   * @return this writer
   */
  public TMLWriter value(Number value) throws IOException {
    assertNotClosed();
    assertScopeGreaterThanZero();
    if (value == null) {
      return nullValue();
    }

    writeDeferredName();
    String string = value.toString();
    if (enforceFiniteFloatingPointValues) {
      if ((string.equals("-Infinity") || string.equals("Infinity") || string
          .equals("NaN"))) {
        throw new IllegalArgumentException(
            "Numeric values must be finite, but was " + value);
      }
    }
    beforeValue(false);
    out.append(string);
    lastToken = TMLToken.DATA;
    return this;
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Writer} and
   * flushes that writer.
   */
  @Override
  public void flush() throws IOException {
    assertNotClosed();
    out.flush();
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   * 
   * @throws IOException
   *           if the document is incomplete
   */
  @Override
  public void close() throws IOException {
    out.close();
    int size = stackSize;
    if (size > 1) {
      throw new IllegalStateException("Document scope not resolved, scope="
          + getScope());
    }
    if (size == 1 && stack[0] != TMLScope.CLOSED_DOCUMENT) {
      throw new IllegalStateException("Document empty");
    }
    stack[0] = TMLScope.CLOSED_DOCUMENT;
    stackSize = 1;
    lastToken = TMLToken.EOF;
    immediateFieldChild = false;
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private TMLWriter open(int scope, String openDelimiter) throws IOException {
    assertNotClosed();
    writeDeferredName();
    beforeValue(true);
    push(scope);
    out.write(openDelimiter);
    lastToken = TMLToken.BEGIN_LIST;
    return this;
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private TMLWriter close(int empty, int nonempty, String closeBracket)
      throws IOException {
    assertNotClosed();
    writeDeferredName();
    int context = peek();
    if (context != nonempty && context != empty) {
      throw new IllegalStateException("Nesting problem.");
    }

    stackSize--;
    if (context == nonempty && lastToken == TMLToken.END_LIST) {
      newline();
    }
    out.write(closeBracket);
    immediateFieldChild = false;
    if (stackSize == 1) {
      stack[0] = TMLScope.CLOSED_DOCUMENT;
      stackSize = 1;
      lastToken = TMLToken.EOF;
      return this;
    }
    lastToken = TMLToken.END_LIST;
    return this;
  }

  /**
   * Returns the value on the top of the stack.
   */
  private int peek() {
    if (stackSize == 0) {
      throw new IllegalStateException("TMLWriter is closed.");
    }
    return stack[stackSize - 1];
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(int topOfStack) {
    stack[stackSize - 1] = topOfStack;
  }

  private void push(int newTop) {
    if (stackSize == stack.length) {
      int[] newStack = new int[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      stack = newStack;
    }
    stack[stackSize++] = newTop;
  }

  /**
   * Inserts any necessary separators and whitespace before a literal value,
   * inline array, or inline object. Also adjusts the stack to expect either a
   * closing bracket or another element.
   * 
   * @param root
   *          true if the value is a new list
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue(boolean newList) throws IOException {
    switch (peek()) {
    case TMLScope.EMPTY_DOCUMENT: // first in document
      replaceTop(TMLScope.NONEMPTY_DOCUMENT);
    case TMLScope.NONEMPTY_DOCUMENT:
      // fall-through
      break;

    case TMLScope.EMPTY_LIST: // first in list
      replaceTop(TMLScope.NONEMPTY_LIST);
      if (newList) newline();
      break;

    case TMLScope.NONEMPTY_LIST: // another in list
      if ((lastToken == TMLToken.DATA || lastToken == TMLToken.NULL || lastToken == TMLToken.NULL_ARRAY)
          && !newList) out.append(' ');
      if (newList) newline();
      break;

    default:
      throw new IllegalStateException("Nesting problem.");
    }
  }

  private void string(String value, SpaceEscapePolicy escapeSpaces)
      throws IOException {
    if (value.length() == 0) {
      out.write("\\1");
      lastToken = TMLToken.DATA;
      return;
    }
    boolean encodeSpaces = escapeSpaces == SpaceEscapePolicy.FORCE_ESCAPE
        || (value.contains("  ") && escapeSpaces == SpaceEscapePolicy.AUTO);
    String[] replacements = REPLACEMENT_CHARS;
    int last = 0;
    int length = value.length();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      String replacement;
      if (c == ' ' && encodeSpaces) {
        replacement = "\\s";
      } else if (c < 128) {
        replacement = replacements[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      } else {
        continue;
      }
      if (last < i) {
        out.write(value, last, i - last);
      }
      out.write(replacement);
      last = i + 1;
    }
    if (last < length) {
      out.write(value, last, length - last);
    }
    lastToken = TMLToken.DATA;
  }

  private void newline() throws IOException {
    if (indent == null) return;
    out.write("\n");
    for (int i = 1, size = stackSize; i < size; i++) {
      out.write(indent);
    }
  }

  private void writeDeferredName() throws IOException {
    if (deferredName != null) {
      beforeName();
      string(deferredName, SpaceEscapePolicy.NO_ESCAPE);
      deferredName = null;
      divider();
    }
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also adjusts
   * the stack to expect the name's value.
   */
  private void beforeName() throws IOException {
    int context = peek();
    if (context == TMLScope.NONEMPTY_LIST) { // not first in list
      out.write(' ');
    } else if (context != TMLScope.EMPTY_LIST) { // not in a list!
      throw new IllegalStateException("Nesting problem.");
    }
  }

  /**
   * @return the current scope depth of the writer
   */
  public int getScope() {
    return stackSize - 1;
  }

  /**
   * @return the last token written
   */
  public TMLToken getLastToken() {
    return lastToken;
  }

  /**
   * @return true if {@link #name(String)} has been called, but no value has
   *         been written
   */
  public boolean hasUnresolvedName() {
    return deferredName != null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void assertNoSpaces(String string) {
    if (string.indexOf(' ') > -1) {
      throw new IllegalArgumentException("Expected no spaces in string: "
          + string);
    }
  }

  private void assertScopeGreaterThanZero() {
    if (getScope() > 0) return;
    throw new IllegalStateException(
        "Invalid top level token, expected BEGIN_LIST");
  }

  private void assertNotClosed() {
    if (stack[0] == TMLScope.CLOSED_DOCUMENT) {
      throw new IllegalStateException("Document is closed");
    }
  }

  public boolean isImmediateFieldChild() {
    return immediateFieldChild;
  }

  public void clearImmediateFieldChild() {
    immediateFieldChild = false;
  }

  public boolean getAndClearImmediateFieldChild() {
    boolean is = immediateFieldChild;
    immediateFieldChild = false;
    return is;
  }

  public void setOverrideSpaceEscapePolicy(SpaceEscapePolicy policy) {
    overrideSpaceEscapePolicy = policy;
  }

  public void clearOverrideSpaceEscapePolicy() {
    overrideSpaceEscapePolicy = null;
  }

  public void setEnforceFiniteFloatingPointValues(boolean flag) {
    enforceFiniteFloatingPointValues = flag;
  }

}
