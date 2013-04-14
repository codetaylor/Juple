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

package com.sudoplay.juple.common;

import java.util.Arrays;
import java.util.Collection;

import com.sudoplay.juple.classparser.annotations.SerializedName;

/**
 * Types used for testing TML serialization and deserialization
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class TestTypes {

  public static class Base {
    public static final String BASE_NAME = Base.class.getSimpleName();
    public static final String BASE_FIELD_KEY = "baseName";
    public static final String SERIALIZER_KEY = "serializerName";
    public String baseName = BASE_NAME;
    public String serializerName;

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (!(obj instanceof Base)) {
        return false;
      }
      Base o = (Base) obj;
      if (serializerName == null && o.serializerName != null) {
        return false;
      }
      if (!baseName.equals(o.baseName)) {
        return false;
      }
      return true;
    }
  }

  public static class Sub extends Base {
    public static final String SUB_NAME = Sub.class.getSimpleName();
    public static final String SUB_FIELD_KEY = "subName";
    public final String subName = SUB_NAME;

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }
  }

  public static class ClassWithBaseField {
    public static final String FIELD_KEY = "base";
    public final Base base;

    public ClassWithBaseField(Base base) {
      this.base = base;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (!(obj instanceof ClassWithBaseField)) {
        return false;
      }
      ClassWithBaseField o = (ClassWithBaseField) obj;
      return o.base.equals(base);
    }
  }

  public static class ClassWithBaseArrayField {
    public static final String FIELD_KEY = "base";
    public final Base[] base;

    public ClassWithBaseArrayField(Base[] base) {
      this.base = base;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (!(obj instanceof ClassWithBaseArrayField)) {
        return false;
      }
      ClassWithBaseArrayField o = (ClassWithBaseArrayField) obj;
      return Arrays.deepEquals(base, o.base);
    }
  }

  public static class ClassWithBaseCollectionField {
    public static final String FIELD_KEY = "base";
    public final Collection<Base> base;

    public ClassWithBaseCollectionField(Collection<Base> base) {
      this.base = base;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (!(obj instanceof ClassWithBaseCollectionField)) {
        return false;
      }
      ClassWithBaseCollectionField o = (ClassWithBaseCollectionField) obj;
      return base.equals(o.base);
    }
  }

  public static class StringWrapper {
    public final String someConstantStringInstanceField;

    public StringWrapper(String value) {
      someConstantStringInstanceField = value;
    }
  }

  public static class BagOfPrimitives {
    public static final long DEFAULT_VALUE = 0;
    public long longValue;
    public int intValue;
    public boolean booleanValue;
    public String stringValue;

    public BagOfPrimitives() {
      this(DEFAULT_VALUE, 0, false, "");
    }

    public BagOfPrimitives(long longValue, int intValue, boolean booleanValue,
        String stringValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
      this.stringValue = stringValue;
    }

    public int getIntValue() {
      return intValue;
    }

    public String getExpectedTML() {
      return getExpectedTML(false);
    }

    public String getExpectedTML(boolean isImmediateFieldValue) {
      StringBuilder sb = new StringBuilder();
      if (!isImmediateFieldValue) sb.append("[");
      sb.append("[longValue|").append(longValue).append("]");
      sb.append("[intValue|").append(intValue).append("]");
      sb.append("[booleanValue|").append(booleanValue).append("]");
      if (stringValue.equals("")) {
        sb.append("[stringValue|").append("\\1").append("]");
      } else {
        sb.append("[stringValue|").append(stringValue).append("]");
      }
      if (!isImmediateFieldValue) sb.append("]");
      return sb.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (booleanValue ? 1231 : 1237);
      result = prime * result + intValue;
      result = prime * result + (int) (longValue ^ (longValue >>> 32));
      result = prime * result
          + ((stringValue == null) ? 0 : stringValue.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      BagOfPrimitives other = (BagOfPrimitives) obj;
      if (booleanValue != other.booleanValue) return false;
      if (intValue != other.intValue) return false;
      if (longValue != other.longValue) return false;
      if (stringValue == null) {
        if (other.stringValue != null) return false;
      } else if (!stringValue.equals(other.stringValue)) return false;
      return true;
    }

    @Override
    public String toString() {
      return String.format(
          "(longValue=%d,intValue=%d,booleanValue=%b,stringValue=%s)",
          longValue, intValue, booleanValue, stringValue);
    }
  }

  public static class BagOfPrimitiveWrappers {
    private final Long longValue;
    private final Integer intValue;
    private final Boolean booleanValue;

    public BagOfPrimitiveWrappers(Long longValue, Integer intValue,
        Boolean booleanValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
    }

    public String getExpectedTML() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      sb.append("[longValue|").append(longValue).append("]");
      sb.append("[intValue|").append(intValue).append("]");
      sb.append("[booleanValue|").append(booleanValue).append("]");
      sb.append("]");
      return sb.toString();
    }
  }

  public static class PrimitiveArray {
    private final long[] longArray;

    public PrimitiveArray() {
      this(new long[0]);
    }

    public PrimitiveArray(long[] longArray) {
      this.longArray = longArray;
    }
    
    public long[] getArray() {
      return this.longArray;
    }

  }

  public static class ClassWithNoFields {
    // Nothing here.. .
    @Override
    public boolean equals(Object other) {
      return other.getClass() == ClassWithNoFields.class;
    }
  }

  public static class Nested {
    private final BagOfPrimitives primitive1;
    private final BagOfPrimitives primitive2;

    public Nested() {
      this(null, null);
    }

    public Nested(BagOfPrimitives primitive1, BagOfPrimitives primitive2) {
      this.primitive1 = primitive1;
      this.primitive2 = primitive2;
    }

    public String getExpectedTML() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      appendFields(sb);
      sb.append("]");
      return sb.toString();
    }

    public void appendFields(StringBuilder sb) {
      if (primitive1 != null) {
        sb.append("[primitive1|").append(primitive1.getExpectedTML(true))
            .append("]");
      } else {
        sb.append("[primitive12|\\0]");
      }
      if (primitive2 != null) {
        sb.append("[primitive2|").append(primitive2.getExpectedTML(true))
            .append("]");
      } else {
        sb.append("[primitive2|\\0]");
      }
    }
  }

  public static class ClassWithTransientFields<T> {
    public transient T transientT;
    public final transient long transientLongValue;
    private final long[] longValue;

    public ClassWithTransientFields() {
      this(0L);
    }

    public ClassWithTransientFields(long value) {
      longValue = new long[] { value };
      transientLongValue = value + 1;
    }

    public String getExpectedTML() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      sb.append("[longValue|").append(longValue[0]).append("]");
      sb.append("]");
      return sb.toString();
    }
  }

  public static class ClassWithCustomTypeConverter {
    private final BagOfPrimitives bag;
    private final int value;

    public ClassWithCustomTypeConverter() {
      this(new BagOfPrimitives(), 10);
    }

    public ClassWithCustomTypeConverter(int value) {
      this(new BagOfPrimitives(value, value, false, ""), value);
    }

    public ClassWithCustomTypeConverter(BagOfPrimitives bag, int value) {
      this.bag = bag;
      this.value = value;
    }

    public BagOfPrimitives getBag() {
      return bag;
    }

    public int getValue() {
      return value;
    }
  }

  public static class ArrayOfObjects {
    private final BagOfPrimitives[] elements;

    public ArrayOfObjects() {
      elements = new BagOfPrimitives[3];
      for (int i = 0; i < elements.length; ++i) {
        elements[i] = new BagOfPrimitives(i, i + 2, false, "i" + i);
      }
    }

  }

  public static class ClassOverridingEquals {
    public ClassOverridingEquals ref;

    @Override
    public boolean equals(Object obj) {
      return true;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }

  public static class ClassWithArray {
    public final Object[] array;

    public ClassWithArray() {
      array = null;
    }

    public ClassWithArray(Object[] array) {
      this.array = array;
    }
  }

  public static class ClassWithObjects {
    public final BagOfPrimitives bag;

    public ClassWithObjects() {
      this(new BagOfPrimitives());
    }

    public ClassWithObjects(BagOfPrimitives bag) {
      this.bag = bag;
    }
  }

  public static class ClassWithSerializedNameFields {
    @SerializedName("fooBar")
    public final int f;
    @SerializedName("Another Foo")
    public final int g;

    public ClassWithSerializedNameFields() {
      this(1, 4);
    }

    public ClassWithSerializedNameFields(int f, int g) {
      this.f = f;
      this.g = g;
    }

  }

}
