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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;

/**
 * Functional test for serialization and deserialization of classes with type
 * variables.
 * 
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class TypeVariableTest {

  private Juple juple = new Juple();

  @Test
  public void testAdvancedTypeVariables() throws Exception {
    Bar bar1 = new Bar("someString", 1, true);
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);
    arrayList.add(2);
    arrayList.add(3);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String tml = juple.toTML(bar1);

    Bar bar2 = juple.fromTML(tml, Bar.class);
    assertEquals(bar1, bar2);
  }

  @Test
  public void testTypeVariablesViaTypeParameter() throws Exception {
    Foo<String, Integer> original = new Foo<String, Integer>("e", 5, false);
    original.map.put("f", Arrays.asList(6, 7));
    Type type = new TMLTypeToken<Foo<String, Integer>>() {}.getType();
    String tml = juple.toTML(original, type);
    assertEquals(
        "[[someSField|e][someTField|5][map|[f|[6 7]]][redField|false]]", tml);
    assertEquals(original, juple.<Foo<String, Integer>> fromTML(tml, type));
  }

  @Test
  public void testBasicTypeVariables() throws Exception {
    Blue blue1 = new Blue(true);
    String tml = juple.toTML(blue1);

    Blue blue2 = juple.fromTML(tml, Blue.class);
    assertEquals(blue1, blue2);
  }

  public static class Blue extends Red<Boolean> {
    public Blue() {
      super(false);
    }

    public Blue(boolean value) {
      super(value);
    }

    // Technically, we should implement hashcode too
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Blue)) {
        return false;
      }
      Blue blue = (Blue) o;
      return redField.equals(blue.redField);
    }
  }

  public static class Red<S> {
    protected S redField;

    public Red() {}

    public Red(S redField) {
      this.redField = redField;
    }
  }

  public static class Foo<S, T> extends Red<Boolean> {
    private S someSField;
    private T someTField;
    public final Map<S, List<T>> map = new HashMap<S, List<T>>();

    public Foo() {}

    public Foo(S sValue, T tValue, Boolean redField) {
      super(redField);
      this.someSField = sValue;
      this.someTField = tValue;
    }

    // Technically, we should implement hashcode too
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
      if (!(o instanceof Foo<?, ?>)) {
        return false;
      }
      Foo<S, T> realFoo = (Foo<S, T>) o;
      return redField.equals(realFoo.redField)
          && someTField.equals(realFoo.someTField)
          && someSField.equals(realFoo.someSField) && map.equals(realFoo.map);
    }
  }

  public static class Bar extends Foo<String, Integer> {
    public Bar() {
      this("", 0, false);
    }

    public Bar(String s, Integer i, boolean b) {
      super(s, i, b);
    }
  }

}
