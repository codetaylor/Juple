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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.annotations.SerializedName;
import com.sudoplay.juple.common.MoreAsserts;

public class EnumTest {

  private Juple juple = new Juple();

  @Test
  public void testTopLevelEnumSerialization() throws Exception {
    String result = juple.toTML(MyEnum.VALUE1);
    assertEquals('[' + MyEnum.VALUE1.toString() + ']', result);
  }

  @Test
  public void testTopLevelEnumDeserialization() throws Exception {
    MyEnum result = juple.fromTML('[' + MyEnum.VALUE1.toString() + ']',
        MyEnum.class);
    assertEquals(MyEnum.VALUE1, result);
  }

  @Test
  public void testCollectionOfEnumsSerialization() {
    Type type = new TMLTypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<MyEnum>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedTML = "[VALUE1 VALUE2]";
    String actualTML = juple.toTML(target);
    assertEquals(expectedTML, actualTML);
    actualTML = juple.toTML(target, type);
    assertEquals(expectedTML, actualTML);
  }

  @Test
  public void testCollectionOfEnumsDeserialization() {
    Type type = new TMLTypeToken<Collection<MyEnum>>() {}.getType();
    String tml = "[VALUE1 VALUE2]";
    Collection<MyEnum> target = juple.fromTML(tml, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  @Test
  public void testClassWithEnumFieldSerialization() throws Exception {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertEquals(target.getExpectedTML(), juple.toTML(target));
  }

  @Test
  public void testClassWithEnumFieldDeserialization() throws Exception {
    String tml = "[[value1|VALUE1][value2|VALUE2]]";
    ClassWithEnumFields target = juple.fromTML(tml, ClassWithEnumFields.class);
    assertEquals(MyEnum.VALUE1, target.value1);
    assertEquals(MyEnum.VALUE2, target.value2);
  }

  @Test
  public void testEnumSubclass() {
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("[ROCK]", juple.toTML(Roshambo.ROCK));
    assertEquals("[ROCK PAPER SCISSORS]",
        juple.toTML(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, juple.fromTML("[ROCK]", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        juple.fromTML("[ROCK PAPER SCISSORS]",
            new TMLTypeToken<Set<Roshambo>>() {}.getType()));
  }

  @Test
  public void testEnumSubclassAsParameterizedType() {
    Collection<Roshambo> list = new ArrayList<Roshambo>();
    list.add(Roshambo.ROCK);
    list.add(Roshambo.PAPER);

    String tml = juple.toTML(list);
    assertEquals("[ROCK PAPER]", tml);

    Type collectionType = new TMLTypeToken<Collection<Roshambo>>() {}.getType();
    Collection<Roshambo> actualTMLList = juple.fromTML(tml, collectionType);
    MoreAsserts.assertContains(actualTMLList, Roshambo.ROCK);
    MoreAsserts.assertContains(actualTMLList, Roshambo.PAPER);
  }

  @Test
  public void testEnumCaseMapping() {
    assertEquals(Gender.MALE, juple.fromTML("[boy]", Gender.class));
    assertEquals("[boy]", juple.toTML(Gender.MALE, Gender.class));
  }

  @Test
  public void testEnumSet() {
    EnumSet<Roshambo> foo = EnumSet.of(Roshambo.ROCK, Roshambo.PAPER);
    String tml = juple.toTML(foo);
    Type type = new TMLTypeToken<EnumSet<Roshambo>>() {}.getType();
    EnumSet<Roshambo> bar = juple.fromTML(tml, type);
    assertTrue(bar.contains(Roshambo.ROCK));
    assertTrue(bar.contains(Roshambo.PAPER));
    assertFalse(bar.contains(Roshambo.SCISSORS));
  }

  public enum Gender {
    @SerializedName("boy")
    MALE,

    @SerializedName("girl")
    FEMALE
  }

  public enum Roshambo {
    ROCK {
      @Override
      Roshambo defeats() {
        return SCISSORS;
      }
    },
    PAPER {
      @Override
      Roshambo defeats() {
        return ROCK;
      }
    },
    SCISSORS {
      @Override
      Roshambo defeats() {
        return PAPER;
      }
    };

    abstract Roshambo defeats();
  }

  private static enum MyEnum {
    VALUE1, VALUE2
  }

  private static class ClassWithEnumFields {
    private final MyEnum value1 = MyEnum.VALUE1;
    private final MyEnum value2 = MyEnum.VALUE2;

    public String getExpectedTML() {
      return "[[value1|" + value1 + "][value2|" + value2 + "]]";
    }
  }
}
