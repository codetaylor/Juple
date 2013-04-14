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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.common.ParameterizedTypeFixtures.MyParameterizedType;
import com.sudoplay.juple.common.ParameterizedTypeFixtures.MyParameterizedTypeInstanceCreator;
import com.sudoplay.juple.common.TestTypes.BagOfPrimitives;

/**
 * Functional tests for the serialization and deserialization of parameterized
 * types in Juple.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class ParameterizedTypesTest {

  private Juple juple = new Juple();
  @SuppressWarnings("unused")
  private Juple pretty = new JupleBuilder().setPrettyPrinting().create();

  @Test
  public void testParameterizedTypesSerialization() throws Exception {
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TMLTypeToken<MyParameterizedType<Integer>>() {}
        .getType();
    String tml = juple.toTML(src, typeOfSrc);
    assertEquals("[[value|10]]", tml);
  }

  @Test
  public void testParameterizedTypeDeserialization() throws Exception {
    Type expectedType = new TMLTypeToken<MyParameterizedType<BagOfPrimitives>>() {}
        .getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();

    Juple juple = new JupleBuilder().registerTypeAdapter(
        expectedType,
        new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(
            bagDefaultInstance)).create();

    String tml = "[[value|[longValue|0][intValue|0][booleanValue|false][stringValue|\\1]]]";
    MyParameterizedType<BagOfPrimitives> actual = juple.fromTML(tml,
        expectedType);

    assertEquals(0, actual.value.longValue);
    assertEquals(0, actual.value.intValue);
    assertEquals(false, actual.value.booleanValue);
    assertEquals("", actual.value.stringValue);
  }

  @Test
  public void testTypesWithMultipleParametersSerialization() throws Exception {
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src = new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(
        10, 1.0F, 2.1D, "abc", new BagOfPrimitives());
    Type typeOfSrc = new TMLTypeToken<MultiParameters<Integer, Float, Double, String, BagOfPrimitives>>() {}
        .getType();
    String actual = juple.toTML(src, typeOfSrc);
    String expected = "[[a|10][b|1.0][c|2.1][d|abc][e|[longValue|0][intValue|0][booleanValue|false][stringValue|\\1]]]";
    assertEquals(expected, actual);
  }

  @Test
  public void testTypesWithMultipleParametersDeserialization() throws Exception {
    Type typeOfTarget = new TMLTypeToken<MultiParameters<Integer, Float, Double, String, BagOfPrimitives>>() {}
        .getType();
    String tml = "[[a|10][b|1.0][c|2.1][d|abc][e|[longValue|0][intValue|0][booleanValue|false][stringValue|\\1]]]";
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target = juple
        .fromTML(tml, typeOfTarget);
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected = new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(
        10, 1.0F, 2.1D, "abc", new BagOfPrimitives());
    assertEquals(expected, target);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVariableTypeFieldsAndGenericArraysSerialization()
      throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        obj, array, list, arrayOfLists, list, arrayOfLists);
    String actual = juple.toTML(objToSerialize, typeOfSrc);
    String expected = "[[typeParameterObj|0][typeParameterArray|1 2 3][listOfTypeParameters|4 5][arrayOfListOfTypeParameters|[4 5][4 5]][listOfWildcardTypeParameters|4 5][arrayOfListOfWildcardTypeParameters|[4 5][4 5]]]";
    assertEquals(expected, actual);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVariableTypeFieldsAndGenericArraysDeserialization()
      throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        obj, array, list, arrayOfLists, list, arrayOfLists);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = juple.fromTML(
        tml, typeOfSrc);

    assertEquals(objToSerialize, objAfterDeserialization);
  }

  @Test
  public void testVariableTypeSerialization() throws Exception {
    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        0, null, null, null, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    String expected = "[[typeParameterObj|0][typeParameterArray|\\2][listOfTypeParameters|\\2][arrayOfListOfTypeParameters|\\2][listOfWildcardTypeParameters|\\2][arrayOfListOfWildcardTypeParameters|\\2]]";
    assertEquals(expected, tml);
  }

  @Test
  public void testVariableTypeDeserialization() throws Exception {
    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        0, null, null, null, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = juple.fromTML(
        tml, typeOfSrc);

    assertEquals(objToSerialize, objAfterDeserialization);
  }

  @Test
  public void testVariableTypeArrayDeserialization() throws Exception {
    Integer[] array = { 1, 2, 3 };

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        null, array, null, null, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = juple.fromTML(
        tml, typeOfSrc);

    assertEquals(objToSerialize, objAfterDeserialization);
  }

  @Test
  public void testParameterizedTypeWithVariableTypeDeserialization()
      throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        null, null, list, null, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = juple.fromTML(
        tml, typeOfSrc);

    assertEquals(objToSerialize, objAfterDeserialization);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParameterizedTypeGenericArraysSerialization()
      throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        null, null, null, arrayOfLists, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    String expected = "[[typeParameterObj|\\0][typeParameterArray|\\2][listOfTypeParameters|\\2][arrayOfListOfTypeParameters|[1 2][1 2]][listOfWildcardTypeParameters|\\2][arrayOfListOfWildcardTypeParameters|\\2]]";
    assertEquals(expected, tml);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParameterizedTypeGenericArraysDeserialization()
      throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TMLTypeToken<ObjectWithTypeVariables<Integer>>() {}
        .getType();
    ObjectWithTypeVariables<Integer> objToSerialize = new ObjectWithTypeVariables<Integer>(
        null, null, null, arrayOfLists, null, null);
    String tml = juple.toTML(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = juple.fromTML(
        tml, typeOfSrc);

    assertEquals(objToSerialize, objAfterDeserialization);
  }

  @Test
  public void testDeepParameterizedTypeSerialization() {
    Amount<MyQuantity> amount = new Amount<MyQuantity>();
    String tml = juple.toTML(amount);
    assertTrue(tml.contains("value"));
    assertTrue(tml.contains("30"));
  }

  @Test
  public void testDeepParameterizedTypeDeserialization() {
    String tml = "[[value|30]]";
    Type type = new TMLTypeToken<Amount<MyQuantity>>() {}.getType();
    Amount<MyQuantity> amount = juple.fromTML(tml, type);
    assertEquals(30, amount.value);
  }

  /**
   * An test object that has fields that are type variables.
   * 
   * @param <T>
   */
  @SuppressWarnings("unused")
  private static class ObjectWithTypeVariables<T extends Number> {
    private final T typeParameterObj;
    private final T[] typeParameterArray;
    private final List<T> listOfTypeParameters;
    private final List<T>[] arrayOfListOfTypeParameters;
    private final List<? extends T> listOfWildcardTypeParameters;
    private final List<? extends T>[] arrayOfListOfWildcardTypeParameters;

    private ObjectWithTypeVariables() {
      this(null, null, null, null, null, null);
    }

    public ObjectWithTypeVariables(T obj, T[] array, List<T> list,
        List<T>[] arrayOfList, List<? extends T> wildcardList,
        List<? extends T>[] arrayOfWildcardList) {
      this.typeParameterObj = obj;
      this.typeParameterArray = array;
      this.listOfTypeParameters = list;
      this.arrayOfListOfTypeParameters = arrayOfList;
      this.listOfWildcardTypeParameters = wildcardList;
      this.arrayOfListOfWildcardTypeParameters = arrayOfWildcardList;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (this == obj) return true;
      if (!(obj instanceof ObjectWithTypeVariables)) {
        return false;
      }
      ObjectWithTypeVariables<?> o = (ObjectWithTypeVariables<?>) obj;
      if (typeParameterObj == null && o.typeParameterObj != null) {
        return false;
      }
      if (typeParameterObj != null && o.typeParameterObj != null) {
        if (!typeParameterObj.equals(o.typeParameterObj)) {
          return false;
        }
      }
      if (!Arrays.deepEquals(typeParameterArray, o.typeParameterArray)) {
        return false;
      }
      if (listOfTypeParameters == null && o.listOfTypeParameters != null) {
        return false;
      }
      if (listOfTypeParameters != null && o.listOfTypeParameters != null) {
        if (!listOfTypeParameters.equals(o.listOfTypeParameters)) {
          return false;
        }
      }
      if (!Arrays.deepEquals(arrayOfListOfTypeParameters,
          o.arrayOfListOfTypeParameters)) {
        return false;
      }
      if (listOfWildcardTypeParameters == null
          && o.listOfWildcardTypeParameters != null) {
        return false;
      }
      if (listOfWildcardTypeParameters != null
          && o.listOfWildcardTypeParameters != null) {
        if (!listOfWildcardTypeParameters
            .equals(o.listOfWildcardTypeParameters)) {
          return false;
        }
      }
      if (!Arrays.deepEquals(arrayOfListOfWildcardTypeParameters,
          o.arrayOfListOfWildcardTypeParameters)) {
        return false;
      }
      return true;
    }
  }

  private static class MultiParameters<A, B, C, D, E> {
    A a;
    B b;
    C c;
    D d;
    E e;

    // For use by Gson
    @SuppressWarnings("unused")
    private MultiParameters() {}

    MultiParameters(A a, B b, C c, D d, E e) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.e = e;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((a == null) ? 0 : a.hashCode());
      result = prime * result + ((b == null) ? 0 : b.hashCode());
      result = prime * result + ((c == null) ? 0 : c.hashCode());
      result = prime * result + ((d == null) ? 0 : d.hashCode());
      result = prime * result + ((e == null) ? 0 : e.hashCode());
      return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MultiParameters<A, B, C, D, E> other = (MultiParameters<A, B, C, D, E>) obj;
      if (a == null) {
        if (other.a != null) {
          return false;
        }
      } else if (!a.equals(other.a)) {
        return false;
      }
      if (b == null) {
        if (other.b != null) {
          return false;
        }
      } else if (!b.equals(other.b)) {
        return false;
      }
      if (c == null) {
        if (other.c != null) {
          return false;
        }
      } else if (!c.equals(other.c)) {
        return false;
      }
      if (d == null) {
        if (other.d != null) {
          return false;
        }
      } else if (!d.equals(other.d)) {
        return false;
      }
      if (e == null) {
        if (other.e != null) {
          return false;
        }
      } else if (!e.equals(other.e)) {
        return false;
      }
      return true;
    }
  }

  private static class Quantity {
    @SuppressWarnings("unused")
    int q = 10;
  }

  private static class MyQuantity extends Quantity {
    @SuppressWarnings("unused")
    int q2 = 20;
  }

  private interface Measurable<T> {}

  private interface Field<T> {}

  private interface Immutable {}

  public static final class Amount<Q extends Quantity> implements
      Measurable<Q>, Field<Amount<?>>, Serializable, Immutable {
    private static final long serialVersionUID = -7560491093120970437L;

    int value = 30;
  }
}
