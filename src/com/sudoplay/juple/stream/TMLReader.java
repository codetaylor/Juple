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
import java.io.IOException;
import java.io.Reader;

import com.sudoplay.juple.classparser.adapters.TMLReflectiveTypeAdapterFactory;
import com.sudoplay.juple.error.TMLContract;

/**
 * Reads a TML encoded value as a stream of tokens.
 * 
 * <p>
 * Portions of this code have been derived from the google-gson source <a
 * href="https://code.google.com/p/google-gson/"
 * >https://code.google.com/p/google-gson/</a>.
 * 
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 * 
 */
public class TMLReader implements Closeable {

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_LIST = 1;
  private static final int PEEKED_END_LIST = 2;
  private static final int PEEKED_DIVIDER = 3;
  private static final int PEEKED_DATA = 4;
  private static final int PEEKED_EOF = 5;
  private static final int PEEKED_NULL = 6;
  private static final int PEEKED_NULL_ARRAY = 7;

  private final char[] buffer = new char[1024];
  private int pos = 0;
  private int lim = 0;

  private int lineNumber = 0;
  private int lineStart = 0;

  private int peeked = PEEKED_NONE;
  private int lastToken = 0;

  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    stack[stackSize++] = TMLScope.EMPTY_DOCUMENT;
  }

  private final TMLStringPool stringPool = new TMLStringPool();

  private final Reader in;

  private boolean immediateFieldChild = false;

  public TMLReader(Reader in) {
    TMLContract.checkNotNull(in);
    this.in = in;
  }

  /**
   * Consumes the next token from the TML stream and asserts that it is the
   * beginning of a new list.
   * 
   * @throws IOException
   * @throws IllegalStateException
   *           if the next token is not an open delimiter or if this reader is
   *           closed
   */
  public void beginList() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_LIST) {
      push(TMLScope.EMPTY_LIST);
      peeked = PEEKED_NONE;
      lastToken = PEEKED_BEGIN_LIST;
    } else {
      throw new IllegalStateException("Expected BEGIN_LIST but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
  }

  /**
   * Consumes the next token from the TML stream and asserts that it is the end
   * of the current list.
   * 
   * @throws IOException
   * @throws IllegalStateException
   *           if the next token is not a close delimiter or if this reader is
   *           closed
   */
  public void endList() throws IOException {
    if (stackSize < 2) {
      throw new IllegalStateException("END_LIST scope out of range at line "
          + getLineNumber() + " column " + getColumnNumber());
    }
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_LIST) {
      stackSize--;
      peeked = PEEKED_NONE;
      lastToken = PEEKED_END_LIST;
      immediateFieldChild = false;
      if (stackSize < 2 && doPeek() != PEEKED_EOF) {
        peeked = PEEKED_EOF;
        throw new IOException("Data remaining after document close at line "
            + getLineNumber() + " column " + getColumnNumber());
      }
    } else {
      throw new IllegalStateException("Expected END_LIST but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
  }

  /**
   * Consumes the next token from the TML stream and asserts that it is a
   * divider.
   * 
   * @throws IOException
   * @throws IllegalStateException
   *           if the next token is not a divider or if this reader is closed
   */
  public void consumeDivider() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_DIVIDER) {
      peeked = PEEKED_NONE;
      lastToken = PEEKED_DIVIDER;
    } else {
      throw new IllegalStateException("Expected DIVIDER but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
  }

  /**
   * Consumes the next token from the TML stream and asserts that it is a
   * literal null.
   * 
   * @throws IllegalStateException
   *           if the next token is not null or if this reader is closed
   */
  public void nextNull() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_NULL) {
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException("Expected NULL but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
    lastToken = PEEKED_NULL;
  }

  /**
   * Consumes the next token from the TML stream and asserts that it is a
   * literal null array.
   * 
   * @throws IllegalStateException
   *           if the next token is not null or if this reader is closed
   */
  public void nextNullArray() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_NULL_ARRAY) {
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException("Expected NULL_ARRAY but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
    lastToken = PEEKED_NULL_ARRAY;
  }

  /**
   * Returns the next string and consumes a divider. Used by the
   * {@link TMLReflectiveTypeAdapterFactory}.
   * 
   * @return the next field name
   * @throws IOException
   */
  public String nextName() throws IOException {
    String name = nextString();
    consumeDivider();
    immediateFieldChild = true;
    return name;
  }

  /**
   * Consumes and returns the next string.
   * 
   * @return
   * @throws IOException
   */
  public String nextString() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p != PEEKED_DATA) {
      throw new IllegalStateException("Expected DATA but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    peeked = PEEKED_NONE;
    lastToken = PEEKED_DATA;
    return _nextString();
  }

  /**
   * Skips the next data token.
   * 
   * @throws IOException
   */
  private void skipString() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p != PEEKED_DATA) {
      throw new IllegalStateException("Expected DATA but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    peeked = PEEKED_NONE;
    lastToken = PEEKED_DATA;
    _skipString();
    return;
  }

  /**
   * Consumes and returns the next token from the TML stream and asserts that it
   * is data.
   * 
   * @return the next string data
   * @throws IOException
   */
  private String _nextString() throws IOException {

    char[] buffer = this.buffer;
    StringBuilder builder = null;
    int hashCode = 0;
    while (true) {
      int p = pos;
      int l = lim;
      int start = p;
      while (p < l) {
        int c = buffer[p++];

        switch (c) {
        case '\\':
          pos = p;
          if (builder == null) {
            builder = new StringBuilder();
          }
          builder.append(buffer, start, p - start - 1);
          builder.append(readEscapeCharacter());
          p = pos;
          l = lim;
          start = p;
          break;
        case ' ':
        case '[':
        case ']':
        case '|':
          pos = p - 1;
          if (builder == null) {
            return stringPool.get(buffer, start, p - start - 1, hashCode);
          } else {
            builder.append(buffer, start, p - start - 1);
            return builder.toString();
          }
        case '\n':
          hashCode = (hashCode * 31) + c;
          lineNumber++;
          lineStart = p;
        case '\r':
        case '\t':
          pos = p;
          if (builder == null) {
            builder = new StringBuilder();
          }
          builder.append(buffer, start, p - start - 1);
          p = pos;
          l = lim;
          start = p;
          break;
        default:
          hashCode = (hashCode * 31) + c;
        }
      }

      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, start, p - start);
      pos = p;
      if (!fillBuffer(1)) {
        throw new IOException("End of input" + " at line " + getLineNumber()
            + " column " + getColumnNumber());
      }
    }
  }

  /**
   * Skips the next data token.
   * 
   * @throws IOException
   */
  private void _skipString() throws IOException {

    char[] buffer = this.buffer;
    int hashCode = 0;
    while (true) {
      int p = pos;
      int l = lim;
      while (p < l) {
        int c = buffer[p++];

        switch (c) {
        case '\\':
          pos = p;
          skipEscapeCharacter();
          p = pos;
          l = lim;
          break;
        case ' ':
        case '[':
        case ']':
        case '|':
          pos = p - 1;
          return;
        case '\n':
          hashCode = (hashCode * 31) + c;
          lineNumber++;
          lineStart = p;
        case '\r':
        case '\t':
          pos = p;
          p = pos;
          l = lim;
          break;
        default:
          hashCode = (hashCode * 31) + c;
        }
      }

      pos = p;
      if (!fillBuffer(1)) {
        throw new IOException("End of input" + " at line " + getLineNumber()
            + " column " + getColumnNumber());
      }
    }
  }

  /**
   * @return the current scope of the reader stack
   */
  public int getScope() {
    return stackSize - 1;
  }

  /**
   * @return true if the current list has additional data
   * @throws IOException
   */
  public boolean hasNextInScope(int scope) throws IOException {
    if (scope < 1) {
      throw new IllegalArgumentException("Expected scope > 0 but was scope="
          + scope);
    }
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    return stackSize - 1 == scope && p != PEEKED_END_LIST && p != PEEKED_EOF;
  }

  /**
   * @return true if the reader has additional data
   * @throws IOException
   */
  public boolean hasNext() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    return p != PEEKED_EOF;
  }

  /**
   * Skips the remainder of the current list, consuming the last END_LIST token
   * in the current scope.
   * 
   * @throws IOException
   */
  public void skipRemaining() throws IOException {
    if (getScope() == 0) {
      throw new IllegalStateException("Expected scope > 0");
    }
    skipList(false);
  }

  /**
   * Skips a single string, list, null or divider.
   * 
   * <ul>
   * <li>If the next token is DATA, a single DATA is skipped.</li>
   * <li>If the next token is BEGIN_LIST, the entire list is skipped.</li>
   * <li>If the next token is NULL, a single NULL is skipped.</li>
   * <li>If the next token is DIVIDER, a single DIVIDER is skipped.</li>
   * </ul>
   * 
   * @throws IOException
   */
  public void skipNext() throws IOException {

    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    switch (p) {
    case PEEKED_BEGIN_LIST:
      skipList(true);
      break;
    case PEEKED_END_LIST:
      endList();
      break;
    case PEEKED_NULL:
      nextNull();
      break;
    case PEEKED_DATA:
      skipString();
      break;
    case PEEKED_DIVIDER:
      consumeDivider();
      break;
    case PEEKED_EOF:
      throw new IOException("End of input at line " + getLineNumber()
          + " column " + getColumnNumber());
    default:
      throw new AssertionError();
    }
  }

  /**
   * @return the last consumed {@link TMLToken} or null if none
   */
  public TMLToken getLastToken() {
    switch (lastToken) {
    case PEEKED_BEGIN_LIST:
      return TMLToken.BEGIN_LIST;
    case PEEKED_END_LIST:
      return TMLToken.END_LIST;
    case PEEKED_DIVIDER:
      return TMLToken.DIVIDER;
    case PEEKED_DATA:
      return TMLToken.DATA;
    case PEEKED_EOF:
      return TMLToken.EOF;
    case PEEKED_NULL:
      return TMLToken.NULL;
    case PEEKED_NONE:
      return null;
    default:
      throw new AssertionError();
    }
  }

  /**
   * @return the type of the next token without consuming it
   * @throws IOException
   */
  public TMLToken peek() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    switch (p) {
    case PEEKED_BEGIN_LIST:
      return TMLToken.BEGIN_LIST;
    case PEEKED_END_LIST:
      return TMLToken.END_LIST;
    case PEEKED_DIVIDER:
      return TMLToken.DIVIDER;
    case PEEKED_DATA:
      return TMLToken.DATA;
    case PEEKED_EOF:
      return TMLToken.EOF;
    case PEEKED_NULL:
      return TMLToken.NULL;
    case PEEKED_NULL_ARRAY:
      return TMLToken.NULL_ARRAY;
    default:
      throw new AssertionError();
    }
  }

  private int doPeek() throws IOException {

    int peekStack = stack[stackSize - 1];
    if (peekStack == TMLScope.EMPTY_LIST) {

      stack[stackSize - 1] = TMLScope.NONEMPTY_LIST;

    } else if (peekStack == TMLScope.EMPTY_DOCUMENT) {

      ignoreHeader();
      stack[stackSize - 1] = TMLScope.NONEMPTY_DOCUMENT;

    } else if (peekStack == TMLScope.NONEMPTY_DOCUMENT) {

      int c = nextNonWhitespace(false);
      if (c == -1) {
        return peeked = PEEKED_EOF;
      } else {
        pos--;
      }

    } else if (peekStack == TMLScope.CLOSED_DOCUMENT) {

      throw new IllegalStateException("TMLReader is closed");

    }

    int c = nextNonWhitespace(true);
    switch (c) {
    case '[':
      return peeked = PEEKED_BEGIN_LIST;
    case ']':
      return peeked = PEEKED_END_LIST;
    case '|':
      return peeked = PEEKED_DIVIDER;
    default:
      pos--;
    }

    int keyseq = peekKeySequence();
    if (keyseq != PEEKED_NONE) {
      return keyseq;
    }

    return peeked = PEEKED_DATA;

  }

  private int peekKeySequence() throws IOException {
    char c = buffer[pos];
    int peeking;
    if (c != '\\') {
      return PEEKED_NONE;
    }

    if (pos + 1 >= lim && !fillBuffer(1)) {
      return PEEKED_NONE;
    }

    c = buffer[pos + 1];
    if (c == '0') {
      peeking = PEEKED_NULL;
    } else if (c == '2') {
      peeking = PEEKED_NULL_ARRAY;
    } else {
      return PEEKED_NONE;
    }

    // check for actual non-literal sequence terminating char
    if ((pos + 2 < lim || fillBuffer(3)) && isLiteral(buffer[pos + 2])) {
      return PEEKED_NONE;
    }

    pos += 2;
    return peeked = peeking;
  }

  private boolean isLiteral(char c) throws IOException {
    switch (c) {
    case '[':
    case ']':
    case '|':
    case ' ':
    case '\t':
    case '\f':
    case '\r':
    case '\n':
      return false;
    default:
      return true;
    }
  }

  /**
   * Returns the next character in the stream that is neither whitespace nor a
   * part of a comment. When this returns, the returned character is always at
   * {@code buffer[pos-1]}; this means the caller can always push back the
   * returned character by decrementing {@code pos}.
   */
  private int nextNonWhitespace(boolean throwOnEof) throws IOException {
    char[] buffer = this.buffer;
    int p = pos;
    int l = lim;
    while (true) {
      if (p == l) {
        pos = p;
        if (!fillBuffer(1)) {
          break;
        }
        p = pos;
        l = lim;
      }

      int c = buffer[p++];
      switch (c) {
      case '\n':
        lineNumber++;
        lineStart = p;
        continue;
      case ' ':
      case '\r':
      case '\t':
        continue;
      case '|':
        pos = p;
        if (p == l) {
          pos--;
          boolean charsLoaded = fillBuffer(2);
          pos++;
          if (!charsLoaded) {
            return c;
          }
        }
        char peek = buffer[pos];
        if (peek == '|') {
          // skip a || line comment
          pos++;
          skipToEndOfLine();
          p = pos;
          l = lim;
          continue;
        }
        return c;
      default:
        pos = p;
        return c;
      }
    }
    if (throwOnEof) {
      throw new IOException("End of input" + " at line " + getLineNumber()
          + " column " + getColumnNumber());
    } else {
      return -1;
    }
  }

  /**
   * Unescapes the character identified by the character or characters that
   * immediately follow a backslash. The backslash '\' should have already been
   * read. This supports both unicode escapes "u000A" and two-character escapes
   * "\n".
   * 
   * @throws NumberFormatException
   *           if any unicode escape sequences are malformed
   */
  private String readEscapeCharacter() throws IOException {
    if (pos == lim && !fillBuffer(1)) {
      throw new IOException("End of input" + " at line " + getLineNumber()
          + " column " + getColumnNumber());
    }

    char escaped = buffer[pos++];
    switch (escaped) {
    case 'u':
      if (pos + 4 > lim && !fillBuffer(4)) {
        throw new IOException("End of input" + " at line " + getLineNumber()
            + " column " + getColumnNumber());
      }
      // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
      char result = 0;
      for (int i = pos, end = i + 4; i < end; i++) {
        char c = buffer[i];
        result <<= 4;
        if (c >= '0' && c <= '9') {
          result += (c - '0');
        } else if (c >= 'a' && c <= 'f') {
          result += (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
          result += (c - 'A' + 10);
        } else {
          throw new NumberFormatException("\\u" + new String(buffer, pos, 4));
        }
      }
      pos += 4;
      return Character.toString(result);

    case '1':
      return "";

    case '?':
      return "\\?";

    case '*':
      return "\\*";

    case 's':
      return " ";

    case 't':
      return "\t";

    case 'n':
      return "\n";

    case 'r':
      return "\r";

    case '\\':
    default:
      return Character.toString(escaped);
    }
  }

  /**
   * Skips the character identified by the character or characters that
   * immediately follow a backslash. The backslash '\' should have already been
   * read. This supports both unicode escapes "u000A" and two-character escapes
   * "\n".
   * 
   * @throws IOException
   */
  private void skipEscapeCharacter() throws IOException {
    if (pos == lim && !fillBuffer(1)) {
      throw new IOException("End of input" + " at line " + getLineNumber()
          + " column " + getColumnNumber());
    }

    char escaped = buffer[pos++];
    switch (escaped) {
    case 'u':
      if (pos + 4 > lim && !fillBuffer(4)) {
        throw new IOException("End of input" + " at line " + getLineNumber()
            + " column " + getColumnNumber());
      }
      pos += 4;
      return;

    default:
      return;
    }
  }

  /**
   * Advances the position until after the next newline character. If the line
   * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
   * caller.
   */
  private void skipToEndOfLine() throws IOException {
    while (pos < lim || fillBuffer(1)) {
      char c = buffer[pos++];
      if (c == '\n') {
        lineNumber++;
        lineStart = pos;
        break;
      } else if (c == '\r') {
        break;
      }
    }
  }

  /**
   * Returns true once {@code limit - pos >= minimum}. If the data is exhausted
   * before that many characters are available, this returns false.
   */
  private boolean fillBuffer(int minimum) throws IOException {
    char[] buffer = this.buffer;
    lineStart -= pos;
    if (lim != pos) {
      lim -= pos;
      System.arraycopy(buffer, pos, buffer, 0, lim);
    } else {
      lim = 0;
    }

    pos = 0;
    int total;
    while ((total = in.read(buffer, lim, buffer.length - lim)) != -1) {
      lim += total;

      if (lineNumber == 0 && lineStart == 0 && lim > 0 && buffer[0] == '\ufeff') {
        pos++;
        lineStart++;
        minimum++;
      }

      if (lim >= minimum) {
        return true;
      }
    }
    return false;
  }

  /**
   * Ignores all data before the initial opening delimiter.
   * 
   * @throws IOException
   *           if EOF is reached before an open delimiter
   */
  private void ignoreHeader() throws IOException {
    while (nextNonWhitespace(true) != '[') {
      //
    }
    pos--;
  }

  /**
   * Pushes a new scope onto the scope stack. If the stack is at capacity, the
   * stack size is doubled.
   * 
   * @param scope
   */
  private void push(int scope) {
    if (stackSize == stack.length) {
      int[] newStack = new int[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      stack = newStack;
    }
    stack[stackSize++] = scope;
  }

  /**
   * Closes this TML reader and the underlying {@link java.io.Reader}.
   */
  public void close() throws IOException {
    lastToken = PEEKED_EOF;
    peeked = PEEKED_NONE;
    stack[0] = TMLScope.CLOSED_DOCUMENT;
    stackSize = 1;
    immediateFieldChild = false;
    in.close();
  }

  /**
   * Asserts that the stream is at EOF and that the scope has been resolved.
   * 
   * @return this reader for chaining
   */
  public void assertFullConsumption() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = nextNonWhitespace(false);
    }
    if (p != -1 && p != PEEKED_EOF) {
      throw new IOException("Expected EOF but was " + peek()
          + " on assertFullConsumption() at at line " + getLineNumber()
          + " column " + getColumnNumber());
    }
    if (stackSize > 1) {
      throw new IOException("TML stream closed while still inside scope: "
          + stackSize);
    }
  }

  /**
   * Skips the remainder of the list in the current scope, ignoring all nested
   * lists and consuming the final closing delimiter.
   * 
   * Pass true if the skipList call was made while peeked == PEEKED_BEGIN_LIST.
   * 
   * @param atBeginList
   * @throws IOException
   */
  private void skipList(boolean atBeginList) throws IOException {

    int count = atBeginList ? 0 : 1;

    do {

      int p = peeked;
      if (p == PEEKED_NONE) {
        p = doPeek();
      }

      if (p == PEEKED_BEGIN_LIST) {
        push(TMLScope.EMPTY_LIST);
        lastToken = PEEKED_BEGIN_LIST;
        count++;
      } else if (p == PEEKED_END_LIST) {
        stackSize--;
        lastToken = PEEKED_END_LIST;
        count--;
      } else {
        findNonLiteralCharacter: do {
          int i = 0;
          for (; pos + i < lim; i++) {
            switch (buffer[pos + i]) {
            case '[':
            case ']':
              if (buffer[pos + i - 1] == '\\') {
                continue; // ignore escaped control char
              }
              pos += i;
              break findNonLiteralCharacter;
            }
          }
          pos += i;
        } while (fillBuffer(1));
      }
      peeked = PEEKED_NONE;
    } while (count > 0);
  }

  /**
   * @return the current line number
   */
  private int getLineNumber() {
    return lineNumber + 1;
  }

  /**
   * @return the current line position
   */
  private int getColumnNumber() {
    return pos - lineStart;
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

}
