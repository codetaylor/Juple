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

package com.sudoplay.juple.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLInstanceCreator;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.TestTypes.Base;
import com.sudoplay.juple.common.TestTypes.ClassWithBaseField;
import com.sudoplay.juple.common.TestTypes.Sub;

public class InstanceCreatorTest {

  @Test
  public void testInstanceCreatorReturnsBaseType() {
    Juple juple = new JupleBuilder().registerTypeAdapter(Base.class,
        new TMLInstanceCreator<Base>() {
          public Base createInstance(Type type) {
            return new Base();
          }
        }).create();
    String tml = "[[baseName|BaseRevised][subName|Sub]]";
    Base base = juple.fromTML(tml, Base.class);
    assertEquals("BaseRevised", base.baseName);
  }

  @Test
  public void testInstanceCreatorReturnsSubTypeForTopLevelObject() {
    Juple juple = new JupleBuilder().registerTypeAdapter(Base.class,
        new TMLInstanceCreator<Base>() {
          public Base createInstance(Type type) {
            return new Sub();
          }
        }).create();

    String tml = "[[baseName|Base][subName|SubRevised]]";
    Base base = juple.fromTML(tml, Base.class);
    assertTrue(base instanceof Sub);

    Sub sub = (Sub) base;
    assertFalse("SubRevised".equals(sub.subName));
    assertEquals(Sub.SUB_NAME, sub.subName);
  }

  @Test
  public void testInstanceCreatorReturnsSubTypeForField() {
    Juple juple = new JupleBuilder().registerTypeAdapter(Base.class,
        new TMLInstanceCreator<Base>() {
          public Base createInstance(Type type) {
            return new Sub();
          }
        }).create();

    String tml = "[[base|[baseName|Base][subName|SubRevised]]]";
    ClassWithBaseField target = juple.fromTML(tml, ClassWithBaseField.class);
    assertTrue(target.base instanceof Sub);
    assertEquals(Sub.SUB_NAME, ((Sub) target.base).subName);
  }

  @Test
  public void testInstanceCreatorForCollectionType() {
    @SuppressWarnings("serial")
    class SubArrayList<T> extends ArrayList<T> {}
    TMLInstanceCreator<List<String>> listCreator = new TMLInstanceCreator<List<String>>() {
      public List<String> createInstance(Type type) {
        return new SubArrayList<String>();
      }
    };
    Type listOfStringType = new TMLTypeToken<List<String>>() {}.getType();
    Juple juple = new JupleBuilder().registerTypeAdapter(listOfStringType, listCreator)
        .create();
    List<String> list = juple.fromTML("[[a]]", listOfStringType);
    assertEquals(SubArrayList.class, list.getClass());
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
  public void testInstanceCreatorForParametrizedType() throws Exception {
    class SubTreeSet<T> extends TreeSet<T> {}
    TMLInstanceCreator<SortedSet> sortedSetCreator = new TMLInstanceCreator<SortedSet>() {
      public SortedSet createInstance(Type type) {
        return new SubTreeSet();
      }
    };
    Juple juple = new JupleBuilder()
        .registerTypeAdapter(SortedSet.class, sortedSetCreator).create();

    Type sortedSetType = new TMLTypeToken<SortedSet<String>>() {}.getType();
    SortedSet<String> set = juple.fromTML("[[a]]", sortedSetType);
    assertEquals(set.first(), "a");
    assertEquals(SubTreeSet.class, set.getClass());

    set = juple.fromTML("[b]", SortedSet.class);
    assertEquals(set.first(), "b");
    assertEquals(SubTreeSet.class, set.getClass());
  }
}
