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

import static org.junit.Assert.*;
import static com.sudoplay.juple.stream.TMLToken.BEGIN_LIST;
import static com.sudoplay.juple.stream.TMLToken.END_LIST;
import static com.sudoplay.juple.stream.TMLToken.DIVIDER;
import static com.sudoplay.juple.stream.TMLToken.DATA;
import static com.sudoplay.juple.stream.TMLToken.NULL;
import static com.sudoplay.juple.stream.TMLToken.EOF;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

@SuppressWarnings("resource")
public class TMLReaderTest {

  @Test
  public void testReadList() throws IOException {
    TMLReader reader = new TMLReader(reader("[true true]"));
    reader.beginList();
    reader.nextString();
    reader.nextString();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testReadEmptyList() throws IOException {
    TMLReader reader = new TMLReader(reader("[]"));
    reader.beginList();
    assertFalse(reader.hasNextInScope(1));
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextList() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    reader.skipNext();
    assertEquals("g", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextListAfterPeek() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    assertEquals(BEGIN_LIST, reader.peek());
    reader.skipNext();
    assertEquals("g", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipRemainderOfList() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    reader.beginList();
    assertEquals("c", reader.nextString());
    reader.skipRemaining();
    assertEquals("g", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextTopLevelList() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.skipNext();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = IllegalStateException.class)
  public void testSkipRemainderOfTopLevelList_IllegalStateException()
      throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.skipRemaining();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextString() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b c d]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    reader.skipNext();
    assertEquals("d", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextDivider() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b | c d]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    reader.skipNext();
    assertEquals("c", reader.nextString());
    assertEquals("d", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextStringAfterPeek() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b c d]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    assertEquals(DATA, reader.peek());
    reader.skipNext();
    assertEquals("d", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextDataLoop() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b [c d e f] g]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    reader.beginList();
    assertEquals("c", reader.nextString());
    while (reader.peek() == DATA) {
      reader.skipNext();
    }
    reader.endList();
    assertEquals("g", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testSkipNextNull() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b \\0 g]"));
    reader.beginList();
    assertEquals("a", reader.nextString());
    assertEquals("b", reader.nextString());
    assertEquals(NULL, reader.peek());
    reader.skipNext();
    assertEquals("g", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testHelloWorld() throws IOException {
    String tml = "[\n[hello|true]\n[foo|world]\n]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.beginList();
    assertEquals("hello", reader.nextString());
    reader.consumeDivider();
    assertEquals("true", reader.nextString());
    reader.endList();
    reader.beginList();
    assertEquals("foo", reader.nextString());
    reader.consumeDivider();
    assertEquals("world", reader.nextString());
    reader.endList();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = NullPointerException.class)
  public void testNullParameterAsReader_NullPointerException() {
    new TMLReader(null);
  }

  @Test(expected = IOException.class)
  public void testEmptyTMLString_IOException() throws IOException {
    new TMLReader(reader("")).beginList();
  }

  @Test(expected = IOException.class)
  public void testNoTopLevelList_IOException() throws IOException {
    new TMLReader(reader("true")).nextString();
  }

  @Test
  public void testCharacterUnescaping() throws IOException {
    String tml = "[a\" \\\\ a\\\\\" \\\" \\| \\n \\r \\t \\s \\u0000 \\u0019 \\u20AC]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("a\"", reader.nextString());
    assertEquals("\\", reader.nextString());
    assertEquals("a\\\"", reader.nextString());
    assertEquals("\"", reader.nextString());
    assertEquals("|", reader.nextString());
    assertEquals("\n", reader.nextString());
    assertEquals("\r", reader.nextString());
    assertEquals("\t", reader.nextString());
    assertEquals(" ", reader.nextString());
    assertEquals("\0", reader.nextString());
    assertEquals("\u0019", reader.nextString());
    assertEquals("\u20AC", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = NumberFormatException.class)
  public void testUnescapingInvalidCharacters_NumberFormatException()
      throws IOException {
    TMLReader reader = new TMLReader(reader("[\\u000g]"));
    reader.beginList();
    reader.nextString();
  }

  @Test(expected = IOException.class)
  public void testUnescapingTruncatedCharacters_IOException()
      throws IOException {
    TMLReader reader = new TMLReader(reader("[\\u000"));
    reader.beginList();
    reader.nextString();
  }

  @Test(expected = IOException.class)
  public void testUnescapingTruncatedSequence_IOException() throws IOException {
    TMLReader reader = new TMLReader(reader("[\"\\"));
    reader.beginList();
    reader.nextString();
  }

  @Test
  public void testPeekingStringsPrefixedWithNull() throws IOException {
    TMLReader reader = new TMLReader(reader("[nully]"));
    reader.beginList();
    assertEquals(DATA, reader.peek());
    assertEquals("nully", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testVeryLongString() throws IOException {
    String tml = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus a enim quis augue adipiscing tincidunt a quis quam. Sed aliquam tellus sed dui suscipit sed hendrerit turpis varius. In euismod tempor tellus ac imperdiet. Quisque pellentesque orci elit. Nam nisl tortor, vestibulum vitae luctus quis, convallis laoreet nulla. Aenean massa neque, suscipit sit amet pharetra sit amet, euismod quis ipsum. Aliquam metus risus, congue ut tristique vitae, faucibus vitae ligula. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Integer elementum tortor in neque pellentesque eget imperdiet purus convallis. Curabitur odio quam, interdum eu ornare quis, condimentum quis augue. Cras porttitor massa accumsan risus ultrices ultricies. Proin ac sem augue. Vestibulum mattis justo id metus condimentum id aliquet sem vulputate. Donec nibh dui, facilisis a blandit id, viverra eget sapien. Ut elit dolor, eleifend aliquet porttitor sit amet, accumsan eu ipsum. Pellentesque nec quam odio. Etiam suscipit tincidunt purus quis euismod. Cras molestie, dui nec accumsan viverra, sapien nisl scelerisque eros, et convallis nisl urna pretium nibh. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non dui augue, eu venenatis libero. Nam facilisis libero in quam ultricies accumsan. Sed risus mauris, sodales a euismod sit amet, volutpat venenatis nulla. Suspendisse dapibus libero nec sapien vestibulum vel hendrerit velit tincidunt. Vivamus ut mattis est. Curabitur et tortor nec velit pretium mollis. Sed ut ligula leo, ut pulvinar purus. Proin adipiscing vehicula leo. Vivamus massa ipsum, ultrices eu tempus at, interdum imperdiet orci. Pellentesque pretium, ipsum vel blandit hendrerit, arcu risus mollis mi, ut mollis lorem lacus mollis enim. Curabitur sed enim vel lorem aliquam volutpat ut at odio. Etiam sit amet arcu odio. Morbi risus augue, lacinia vel elementum nec, hendrerit id est. Integer sollicitudin justo magna. Sed quis lacus turpis. Vestibulum at ligula vel quam consectetur rutrum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean facilisis imperdiet euismod. Vestibulum nibh ligula, posuere sed ornare non, suscipit adipiscing urna. Etiam ut risus sed lorem adipiscing sagittis. Quisque condimentum, turpis nec mollis tempor, tortor dolor pellentesque quam, vel fermentum mauris nulla non urna. Ut pulvinar, lectus eu sodales facilisis, velit metus lacinia justo, in tempus justo neque et lectus. Donec id ipsum libero. Nunc commodo laoreet turpis nec egestas. Phasellus lobortis adipiscing pulvinar. Donec eget leo quis velit dapibus aliquam non ac sapien. Suspendisse dapibus nunc sed quam interdum tincidunt et et odio. Morbi augue justo, semper eu vestibulum at, cursus et est. Sed accumsan ante non neque euismod a placerat elit ultrices. Morbi lectus leo, blandit sed vestibulum sed, aliquam vitae sem. Curabitur eu fringilla enim. Vivamus consectetur elementum risus non ullamcorper. Aliquam erat volutpat. Maecenas tellus erat, tincidunt vitae fringilla ac, facilisis eu urna. Cras dolor nulla, blandit a mollis at, gravida nec est. Fusce risus neque, tempor at volutpat eu, pulvinar vitae purus. Nunc vel est risus. Donec nunc erat, tincidunt quis mollis eget, auctor et risus. Donec mollis elit a urna condimentum feugiat. Etiam eget placerat elit. Cras interdum pellentesque ornare. In ullamcorper, orci ut eleifend aliquam, dolor elit consectetur risus, et malesuada massa eros ut turpis. Duis eget elit tortor, eget suscipit augue. Vivamus dolor diam, vehicula id rutrum eget, sodales in nisi. Quisque sit amet ante eget tellus pellentesque mattis. Ut ornare consequat libero, nec pretium lectus dignissim varius. In quis odio diam, in ullamcorper leo. Nam sit amet purus ligula. Nunc eget felis a est tristique ultricies. Phasellus ullamcorper euismod erat, ac eleifend orci tempus in. Integer ac lorem dolor. Integer cursus, eros a luctus facilisis, nisl lacus pellentesque turpis, sed tincidunt enim nulla in felis. Donec augue quam, vulputate in hendrerit ut, pellentesque sodales ipsum. Donec ac est id eros suscipit vestibulum id at nisl. Mauris ultrices suscipit nisi, in fringilla est luctus id. Morbi id nulla eget neque interdum auctor sed sed diam. Pellentesque congue molestie viverra. Vestibulum dolor tortor, facilisis quis imperdiet eget, faucibus vitae tortor. Integer at urna eget turpis dictum adipiscing nec at felis. Donec laoreet ipsum nec justo congue eleifend. Nulla aliquet viverra arcu sed semper. Cras egestas metus in justo commodo non aliquam sapien suscipit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut id fringilla tellus. Aenean et urna magna. Aenean consectetur aliquam justo. Etiam ornare convallis luctus. Sed id faucibus neque. Nulla facilisi. Nulla facilisi. Curabitur iaculis scelerisque velit, nec interdum enim pulvinar vel. Donec id nisi nunc. Proin dolor nisl, ultrices ac venenatis mollis, tempor a enim. Aliquam nunc mauris, tincidunt ac sodales at, condimentum non magna. Aenean non dui leo, ut venenatis turpis. Fusce sapien ligula, consequat ut molestie ac, ultrices quis sem. Proin diam tellus, viverra at euismod a, tristique in velit. Integer pharetra dolor et felis euismod vitae vulputate lorem mattis. Duis pretium nunc sed nisi egestas sollicitudin lacinia urna bibendum. Aenean molestie laoreet bibendum. Praesent sit amet nibh orci. Nunc eros nunc, gravida sed condimentum at, euismod nec sem. Mauris at placerat est. Mauris consequat sem ac ipsum sagittis venenatis. Phasellus elit ante, eleifend nec mattis aliquet, ultricies id orci. Suspendisse fermentum ante vel dolor convallis hendrerit. Sed tincidunt eros a purus viverra in fringilla velit commodo. Nam posuere lobortis libero vitae ornare. Nullam lacus metus, blandit eu rhoncus sed, vestibulum eget odio. Proin rhoncus condimentum quam, eget vehicula sapien accumsan vitae. Integer condimentum, nunc sit amet tincidunt rhoncus, leo nibh scelerisque mauris, ut sagittis nisi nibh eget felis. Sed sollicitudin vehicula vehicula. Curabitur id est leo. Morbi pulvinar vulputate venenatis. Sed vitae tortor mauris, id euismod magna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla enim lorem, ultrices et dapibus vel, semper semper nunc. Phasellus varius dapibus neque, in ultricies massa porttitor eu. Nunc vestibulum facilisis dui, quis commodo tortor dignissim a. Pellentesque quis mauris non est lacinia pretium sit amet nec massa. Cras sagittis, sapien ut ullamcorper ultrices, risus mauris ultricies odio, ut commodo neque neque ac libero. Donec ornare, ipsum euismod luctus commodo, erat enim porta justo, in viverra justo lorem id magna. Morbi faucibus velit sed turpis ornare eget pellentesque tellus consectetur. Phasellus pulvinar, diam non ullamcorper elementum, erat diam fringilla lorem, non sodales magna quam et est. Maecenas ut ipsum eget leo tristique malesuada vel at augue. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus tempor, est a venenatis porta, metus sapien sagittis nisl, sed blandit nunc dolor quis libero. Vivamus eu turpis non nisi fringilla dapibus accumsan eu urna. Mauris eleifend laoreet blandit. Praesent vitae augue id turpis blandit ornare. Integer ut massa nec nulla vulputate volutpat at ut orci. Fusce non quam elit, sed tempus erat. Sed ac felis nisi, non tincidunt lacus. Sed semper accumsan nibh et aliquam. Mauris aliquet pretium bibendum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Phasellus ultricies auctor nisi, sit amet hendrerit libero euismod a. Suspendisse dui arcu, facilisis non dictum sed, ornare vel eros. Suspendisse a eros id ipsum fermentum rutrum vel sit amet urna. Morbi sodales volutpat elit. Integer auctor eros at tellus vulputate tincidunt. Vivamus dapibus vulputate nunc vel accumsan. Sed vel neque aliquet mauris aliquam posuere vitae quis magna. Nam venenatis pellentesque metus at pulvinar. Vestibulum sem metus, lobortis id luctus nec, consectetur rhoncus risus. Donec ullamcorper consequat lacus et dapibus. Sed tempus aliquet eleifend. Sed posuere porta semper. Suspendisse potenti. Vestibulum vel congue odio. Sed adipiscing pulvinar enim a eleifend. In non mi odio. Morbi id nibh eget ligula aliquam ultrices. Aenean elementum aliquam dapibus. Etiam pellentesque volutpat diam eu scelerisque. Etiam blandit erat a nisl ornare a sagittis metus tristique. Nulla quis augue ut justo faucibus pharetra et tempus felis. Nullam eget lectus purus, quis porta leo. Fusce sit amet quam vitae tortor bibendum aliquam. Nam convallis sapien id risus rhoncus ac tincidunt arcu iaculis. Fusce eleifend tempor augue vel sagittis. Cras ac arcu vitae neque semper euismod quis a dui. Proin adipiscing leo vitae nulla elementum mollis. Pellentesque risus nisl, convallis id placerat dictum, ultricies et purus. Etiam ut orci orci. Sed sed ante in nulla rutrum facilisis vel sit amet velit. In ac risus a nisi scelerisque aliquet. Nullam enim neque, molestie a placerat at, auctor quis mi. Duis venenatis tempor eleifend. Fusce viverra lacinia quam quis egestas. Praesent eget feugiat magna. Cras a justo eu velit aliquet posuere. Donec scelerisque nibh a tortor interdum scelerisque sit amet lacinia diam. Fusce arcu sapien, scelerisque sit amet posuere ut, lacinia in lorem. Vivamus eu ante metus, quis suscipit nulla. Nullam et massa arcu, vel porttitor magna. Curabitur aliquam, risus nec dapibus elementum, elit mauris dictum tellus, a semper diam tellus sed nunc. Mauris id nulla nec lorem placerat commodo. Nunc hendrerit nunc eget turpis sodales a consectetur eros mollis. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In nisl justo, tempor id iaculis in, rhoncus non tortor. Quisque sapien sapien, rutrum ut dignissim et, viverra vitae ligula. Curabitur ornare orci sit amet massa ultricies at aliquet diam ultrices. Suspendisse potenti. Curabitur at ultrices nibh. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam at lorem ante, ac egestas ipsum. Nam nec orci ac magna semper luctus id id tellus. Nam aliquet pharetra orci quis condimentum. Aenean metus elit, volutpat in iaculis sed, imperdiet imperdiet libero. Nunc quis augue ut lacus aliquam semper eu non erat. Aenean et justo vel mauris varius consequat sit amet eget ante. Suspendisse potenti. Suspendisse potenti. Proin at libero nec nulla rhoncus volutpat. Vivamus dignissim aliquam felis non rutrum. Quisque pulvinar mattis sodales. Etiam semper massa sed elit laoreet vestibulum. Nulla facilisi. Curabitur faucibus vulputate lacus, sit amet adipiscing lorem convallis in. Quisque a tortor libero. Donec molestie scelerisque volutpat. Cras enim nisi, eleifend vel placerat et, ornare ut dolor. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Vivamus aliquet consequat tincidunt. Proin at luctus orci. In consequat erat at neque vestibulum et malesuada nisi rutrum. Maecenas lacus est, ullamcorper ut faucibus nec, dignissim eu turpis. In fermentum eros sit amet risus pulvinar in ullamcorper orci ullamcorper. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ante tellus, pretium a malesuada ac, posuere eget leo. Donec pretium dictum metus, vitae rutrum mi tempor rutrum. Aliquam mollis, orci vulputate pharetra interdum, arcu nisl fermentum nulla, vitae mollis ligula nibh sed libero. Praesent fringilla elementum tellus eu auctor. Aliquam nisl nulla, consectetur eget molestie ac, rhoncus nec augue. In hac habitasse platea dictumst. Maecenas semper ullamcorper nibh vitae bibendum. Suspendisse congue laoreet eros a rutrum. Vivamus tempor luctus magna eu euismod. Aenean vel arcu ipsum. Donec eget ligula ac nisl accumsan interdum quis nec arcu. Suspendisse ut volutpat velit. Mauris eget nibh elit, et volutpat sem. Quisque bibendum magna eget lacus gravida vitae egestas felis condimentum. Nam eu orci eu nunc posuere convallis. In eget tortor ipsum. Proin ultricies bibendum viverra. Aenean sed erat libero, vel elementum mauris. Sed sed sagittis sapien. Proin malesuada faucibus est a pharetra. Sed in sem felis, ut facilisis lectus. Praesent dui urna, dapibus eget convallis quis, aliquet ut mi. Vivamus luctus blandit dolor nec scelerisque. Praesent sed cursus mauris. Cras eu laoreet justo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed tempus consectetur nisi, quis lobortis est elementum pellentesque. Morbi bibendum sem nec metus posuere porta sit amet nec justo. Nulla mauris dui, vestibulum quis gravida sed, egestas ut tortor. Sed eget neque tellus. Suspendisse nec urna in sem condimentum ultrices ac ut augue. Maecenas iaculis elit et eros pulvinar feugiat. Nullam et sapien sem. Aenean facilisis, elit in rutrum mattis, nunc odio volutpat turpis, id mollis nisi turpis eget lacus. Morbi dignissim tempor venenatis. Aenean consectetur tincidunt purus, non convallis ligula cursus vitae. Pellentesque et mauris vel ante pellentesque congue sit amet et nulla. Donec quis metus eu nunc condimentum semper. Fusce auctor scelerisque risus eu molestie. Curabitur id tellus velit, vitae congue dolor. Donec placerat felis congue nisi egestas fermentum at quis mauris. Pellentesque vehicula dictum nunc non porta. Cras.";
    TMLReader reader = new TMLReader(reader("[" + tml + "]"));
    reader.beginList();
    StringBuilder sb = new StringBuilder();
    while (reader.peek() == DATA) {
      sb.append(reader.nextString());
      if (reader.peek() == DATA) {
        sb.append(' ');
      }
    }
    reader.endList();
    assertEquals(EOF, reader.peek());
    reader.close();
    assertEquals(tml, sb.toString());
  }

  @Test(expected = IOException.class)
  public void testPrematureEndOfInput() throws IOException {
    String tml = "[premature";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.nextString();
  }

  @Test
  public void testNextFailuresDoNotAdvance() throws IOException {
    TMLReader reader = new TMLReader(reader("[a b]"));
    assertEquals(BEGIN_LIST, reader.peek());
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.consumeDivider();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.endList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals(BEGIN_LIST, reader.peek());
    reader.beginList();
    assertEquals(DATA, reader.peek());
    try {
      reader.beginList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.consumeDivider();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.endList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals(DATA, reader.peek());
    reader.nextString();
    assertEquals(DATA, reader.peek());
    try {
      reader.beginList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.consumeDivider();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.endList();
      fail();
    } catch (IllegalStateException expected) {}
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals(DATA, reader.peek());
    reader.nextString();
    assertEquals(END_LIST, reader.peek());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }
  
  @Test
  public void testNullIsNotNullArray() throws IOException {
    String tml = "[\\0]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextNullArray();
      fail();
    } catch (IllegalStateException expected) {}
    reader.nextNull();
    reader.endList();
    assertEquals(EOF, reader.peek());
    reader.close();
  }

  @Test
  public void testNullIsNotString() throws IOException {
    String tml = "[\\0]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {}
    reader.nextNull();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullArrayIsNotString() throws IOException {
    String tml = "[\\2]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {}
    reader.nextNullArray();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullSequenceSeries() throws IOException {
    String tml = "[\\0 \\0 \\0 \\0]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.nextNull();
    reader.nextNull();
    reader.nextNull();
    reader.nextNull();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullArraySequenceSeries() throws IOException {
    String tml = "[\\2 \\2 \\2 \\2]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.nextNullArray();
    reader.nextNullArray();
    reader.nextNullArray();
    reader.nextNullArray();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullSequenceTerminatedByNullSequence() throws IOException {
    String tml = "[\\0\\0]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals("00", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullArraySequenceTerminatedByNullArraySequence() throws IOException {
    String tml = "[\\2\\2]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextNullArray();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals("22", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullSequenceTerminatedByLiteral() throws IOException {
    String tml = "[\\0null]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals("0null", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testNullArraySequenceTerminatedByLiteral() throws IOException {
    String tml = "[\\2null]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.nextNullArray();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals("2null", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testDivider() throws IOException {
    String tml = "[divide|this]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("divide", reader.nextString());
    assertEquals(DIVIDER, reader.peek());
    reader.consumeDivider();
    assertEquals("this", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testCommentIsNotDivider() throws IOException {
    String tml = "[divide||this is a comment\nthis]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("divide", reader.nextString());
    try {
      reader.consumeDivider();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals("this", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = IOException.class)
  public void testUnterminatedComment() throws IOException {
    String tml = "[divide||this is a comment]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("divide", reader.nextString());
    reader.consumeDivider();
  }

  @Test
  public void testCommentWithSkipNext() throws IOException {
    String tml = "[divide||this is a comment\nthis]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("divide", reader.nextString());
    reader.skipNext();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testCommentWithSkipRemainder() throws IOException {
    String tml = "[divide||this is a comment\nthis small group]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("divide", reader.nextString());
    reader.skipRemaining();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testCommentAtEOF() throws IOException {
    String tml = "[comment test]||this is a comment";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.nextString();
    reader.nextString();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = IOException.class)
  public void testCommentAtEOFWithMoreTopLevelLists() throws IOException {
    String tml = "[comment test]||this is a comment\n[][][][]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.nextString();
    reader.nextString();
    reader.endList();
  }

  @Test
  public void testEmptyString() throws IOException {
    String tml = "[empty \\1 string]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    assertEquals("empty", reader.nextString());
    assertEquals("", reader.nextString());
    assertEquals("string", reader.nextString());
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = IOException.class)
  public void testMultipleTopLevelLists() throws IOException {
    String tml = "[][]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    reader.endList();
  }

  @Test
  public void testMultipleTopLevelListsWithSkip() throws IOException {
    String tml = "[][]";
    TMLReader reader = new TMLReader(reader(tml));
    reader.beginList();
    try {
      reader.endList();
      fail();
    } catch (IOException expected) {}
    try {
      reader.skipNext();
      fail();
    } catch (IOException expected) {}
    try {
      reader.skipRemaining();
      fail();
    } catch (IllegalStateException expected) {}
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testHeaderIgnored() throws IOException {
    String tml = "this is a header[]";
    TMLReader reader = new TMLReader(reader(tml));
    assertEquals(BEGIN_LIST, reader.peek());
    reader.beginList();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testBomIgnoredInHeader() throws IOException {
    String tml = "\ufeffheader[]";
    TMLReader reader = new TMLReader(reader(tml));
    assertEquals(BEGIN_LIST, reader.peek());
    reader.beginList();
    reader.endList();
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testFailWithPosition() throws IOException {
    testFailWithPosition("Expected DATA but was END_LIST at line 6 column 20",
        "[data \n\n\n\n\ndata data data data]");
  }

  @Test
  public void testFailWithPositionGreaterThanBufferSize() throws IOException {
    String spaces = repeat(' ', 8192);
    testFailWithPosition("Expected DATA but was END_LIST at line 6 column 20",
        "[data \n\n" + spaces + "\n\n\ndata data data data]");
  }

  @Test
  public void testFailWithPositionOverLineComment() throws IOException {
    testFailWithPosition(
        "Expected DATA but was END_LIST at line 6 column 20",
        "[data \n\n||this is a comment\n\n || ... and another comment!\ndata data data data]");
  }

  @Test
  public void testFailWithEscapedNewlineCharacter() throws IOException {
    testFailWithPosition("Expected DATA but was END_LIST at line 6 column 22",
        "[data \n\n\n\n\ndata data\\\n data data]");
  }

  @Test
  public void testFailWithEmptyStrings() throws IOException {
    testFailWithPosition("Expected DATA but was END_LIST at line 6 column 12",
        "[\\1 \n\n\n\n\n\\1 \\1 \\1 \\1]");
  }

  @Test
  public void testFailWithHeader() throws IOException {
    testFailWithPosition("Expected DATA but was END_LIST at line 7 column 20",
        "this is a header!\n[data \n\n\n\n\ndata data data data]");
  }

  @Test
  public void testDeeplyNestedArrays() throws IOException {
    /*
     * this is nested 40 levels deep; Juple is tuned for nesting 30 levels deep
     * or fewer
     */
    TMLReader reader = new TMLReader(reader(repeat('[', 40) + repeat(']', 40)));
    for (int i = 0; i < 40; i++) {
      reader.beginList();
    }
    for (int i = 0; i < 40; i++) {
      reader.endList();
    }
    assertEquals(EOF, reader.peek());
  }

  @Test
  public void testVeryDeeplyNestedArrays() throws IOException {
    /*
     * this is nested 400 levels deep; Juple is tuned for nesting 30 levels deep
     * or fewer
     */
    TMLReader reader = new TMLReader(
        reader(repeat('[', 400) + repeat(']', 400)));
    for (int i = 0; i < 400; i++) {
      reader.beginList();
    }
    for (int i = 0; i < 400; i++) {
      reader.endList();
    }
    assertEquals(EOF, reader.peek());
  }

  @Test(expected = IOException.class)
  public void testUnterminatedList_IOException() throws IOException {
    String tml = "[unterminated lis";
    TMLReader reader = new TMLReader(reader(tml));
    assertEquals(BEGIN_LIST, reader.peek());
    reader.beginList();
    assertEquals("unterminated", reader.nextString());
    reader.nextString();
  }

  @Test(expected = IOException.class)
  public void testNoOpenDelimiter_IOException() throws IOException {
    TMLReader reader = new TMLReader(reader("no] open| \n delimiter"));
    reader.beginList();
    reader.close();
  }

  @Test
  public void testNextConsumableTokenOpenDelimiter() throws IOException {
    TMLReader reader = new TMLReader(reader("["));
    reader.beginList();
    reader.close();
  }

  @Test
  public void testMultipleOpenDelimiterTokensSeparatedNonLiteral()
      throws IOException {
    TMLReader reader = new TMLReader(reader("[ \r[ [ \t [ \n   [ \r\n ["));
    reader.beginList();
    reader.beginList();
    reader.beginList();
    reader.beginList();
    reader.beginList();
    reader.beginList();
    reader.close();
  }

  @Test
  public void testEscapedChars() throws IOException {
    TMLReader reader = new TMLReader(
        reader("[\\n \\r \\t \\s \\0 \\1 \\* \\?]"));
    reader.beginList();
    assertEquals("\n", reader.nextString());
    assertEquals("\r", reader.nextString());
    assertEquals("\t", reader.nextString());
    assertEquals(" ", reader.nextString());
    assertEquals(NULL, reader.peek());
    reader.nextNull();
    assertEquals("", reader.nextString());
    assertEquals("\\*", reader.nextString());
    assertEquals("\\?", reader.nextString());
    reader.close();
  }

  @Test
  public void testEscapedSpace() throws IOException {
    TMLReader reader = new TMLReader(reader("[a\\sb \\s\\s c\\sd]"));
    reader.beginList();
    assertEquals("a b", reader.nextString());
    assertEquals("  ", reader.nextString());
    assertEquals("c d", reader.nextString());
    reader.close();
  }

  /**
   * Calling {@link TMLReader#assertFullConsumption()} will assert that the
   * scope has been resolved. An IOException will be thrown if the stream has
   * unresolved open delimiters.
   * 
   * @throws IOException
   */
  @Test(expected = IOException.class)
  public void testStrictCloseOutOfScope_IOException() throws IOException {
    TMLReader reader = new TMLReader(reader("[ [ close before ] "));
    reader.beginList();
    reader.beginList();
    assertEquals("close", reader.nextString());
    assertEquals("before", reader.nextString());
    reader.endList();
    reader.assertFullConsumption();
    reader.close();
  }

  /**
   * Calling {@link TMLReader#assertFullConsumption()} will assert that the
   * stream is at EOF. An IOException will be thrown if the stream has
   * unconsumed data remaining.
   * 
   * @throws IOException
   */
  @Test(expected = IOException.class)
  public void testStrictCloseBeforeEOF_IOException() throws IOException {
    TMLReader reader = new TMLReader(reader("[ [ close before ] ]"));
    reader.beginList();
    reader.beginList();
    assertEquals("close", reader.nextString());
    reader.assertFullConsumption();
    reader.close();
  }

  /**
   * Calling {@link TMLReader#assertFullConsumption()} will assert that the
   * stream is at EOF and all open delimiters have been resolved. An IOException
   * will be thrown if the stream has unconsumed data remaining.
   * 
   * @throws IOException
   */
  @Test
  public void testStrictClose() throws IOException {
    TMLReader reader = new TMLReader(reader("[ [ close before ] ]"));
    reader.beginList();
    reader.beginList();
    assertEquals("close", reader.nextString());
    assertEquals("before", reader.nextString());
    reader.endList();
    reader.endList();
    reader.assertFullConsumption();
    reader.close();
  }

  /**
   * If {@link TMLReader#beginList()} is successful,
   * {@link TMLReader#getLastToken()} returns {@link TMLToken#BEGIN_LIST}.
   * <p>
   * If {@link TMLReader#nextString()} is successful,
   * {@link TMLReader#getLastToken()} returns {@link TMLToken#DATA}.
   * <p>
   * If {@link TMLReader#consumeDivider()} is successful,
   * {@link TMLReader#getLastToken()} returns {@link TMLToken#DIVIDER}.
   * <p>
   * If {@link TMLReader#endList()} is successful,
   * {@link TMLReader#getLastToken()} returns {@link TMLToken#END_LIST}.
   * <p>
   * If {@link TMLReader#close()} is successful,
   * {@link TMLReader#getLastToken()} returns {@link TMLToken#EOF}.
   * 
   * @throws IOException
   */
  @Test
  public void testGetLastToken() throws IOException {
    TMLReader reader = new TMLReader(reader("[ 1 | data \\0 \\1 ]"));
    reader.beginList();
    assertEquals(TMLToken.BEGIN_LIST, reader.getLastToken());

    assertEquals("1", reader.nextString());
    assertEquals(TMLToken.DATA, reader.getLastToken());

    reader.consumeDivider();
    assertEquals(TMLToken.DIVIDER, reader.getLastToken());

    assertEquals("data", reader.nextString());
    assertEquals(TMLToken.DATA, reader.getLastToken());

    reader.nextNull();
    assertEquals(TMLToken.NULL, reader.getLastToken());

    assertEquals("", reader.nextString());
    assertEquals(TMLToken.DATA, reader.getLastToken());

    reader.endList();
    assertEquals(TMLToken.END_LIST, reader.getLastToken());

    reader.close();
    assertEquals(TMLToken.EOF, reader.getLastToken());
  }

  /**
   * The reader starts with a scope of 0. For each open delimiter consumed by
   * {@link TMLReader#beginList()}, the scope is incremented by one. For each
   * close delimiter consumed by {@link TMLReader#endList()}, the scope is
   * decremented by one.
   * 
   * @throws IOException
   */
  @Test
  public void testGetScope() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 32; i++) {
      sb.append("[");
    }
    for (int i = 0; i < 32; i++) {
      sb.append("]");
    }
    TMLReader reader = new TMLReader(reader(sb.toString()));
    assertEquals(0, reader.getScope());
    for (int i = 0; i < 32; i++) {
      reader.beginList();
      assertEquals(i + 1, reader.getScope());
    }
    for (int i = 0; i < 32; i++) {
      reader.endList();
      assertEquals(31 - i, reader.getScope());
    }
    reader.close();
    assertEquals(0, reader.getScope());
  }

  @Test
  public void testHasNext() throws IOException {
    TMLReader reader = new TMLReader(reader("[ | data ]"));
    assertEquals(true, reader.hasNext());
    reader.beginList();
    assertEquals(true, reader.hasNext());
    reader.consumeDivider();
    assertEquals(true, reader.hasNext());
    reader.nextString();
    assertEquals(true, reader.hasNext());
    reader.endList();
    assertEquals(false, reader.hasNext());
    reader.close();
  }

  @Test
  public void testHasNextInScope() throws IOException {
    TMLReader reader = new TMLReader(
        reader("scope zero [ scope one [ scope two [1][2][3] ] ]"));

    // scope zero
    reader.beginList(); // scope one
    reader.nextString();
    reader.nextString();
    reader.beginList(); // scope two
    reader.nextString();
    reader.nextString();

    int i = 1;
    while (reader.hasNextInScope(2)) {
      reader.beginList();
      assertEquals(String.valueOf(i++), reader.nextString());
      reader.endList();
    }

    reader.endList();
    reader.endList();

    reader.close();
  }

  @Test
  public void testHasNextInScopeNotTerminatedByNull() throws IOException {
    TMLReader reader = new TMLReader(
        reader("scope zero [ scope one [ scope \\0 two [1][2][3] ] ]"));

    // scope zero
    reader.beginList(); // scope one
    reader.nextString();
    reader.nextString();
    
    reader.beginList(); // scope two
    while (reader.hasNextInScope(2)) {
      reader.skipNext();
    }

    reader.endList();
    reader.endList();

    reader.close();

  }

  @Test
  public void testHasNextInScopeNotTerminatedByDivider() throws IOException {
    TMLReader reader = new TMLReader(
        reader("scope zero [ scope one [ scope | two [1][2][3] ] ]"));

    // scope zero
    reader.beginList(); // scope one
    reader.nextString();
    reader.nextString();
    
    reader.beginList(); // scope two
    while (reader.hasNextInScope(2)) {
      reader.skipNext();
    }

    reader.endList();
    reader.endList();

    reader.close();

  }

  @Test
  public void testPeek() throws IOException {
    TMLReader reader = new TMLReader(reader("[ peek | ]"));
    assertEquals(TMLToken.BEGIN_LIST, reader.peek());
    reader.beginList();
    assertEquals(TMLToken.DATA, reader.peek());
    reader.nextString();
    assertEquals(TMLToken.DIVIDER, reader.peek());
    reader.consumeDivider();
    assertEquals(TMLToken.END_LIST, reader.peek());
    reader.endList();
    assertEquals(TMLToken.EOF, reader.peek());
    reader.close();
  }

  private void testFailWithPosition(String message, String tml)
      throws IOException {
    TMLReader reader1 = new TMLReader(reader(tml));
    reader1.beginList();
    reader1.nextString();
    reader1.nextString();
    reader1.nextString();
    reader1.nextString();
    reader1.nextString();
    try {
      reader1.nextString();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals(message, expected.getMessage());
    }
  }

  private String repeat(char c, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(c);
    }
    return sb.toString();
  }

  private StringReader reader(String string) {
    return new StringReader(string);
  }

}
