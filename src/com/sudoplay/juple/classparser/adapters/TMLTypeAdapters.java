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

package com.sudoplay.juple.classparser.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLLazilyParsedNumber;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.annotations.SerializedName;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

public class TMLTypeAdapters {

  private TMLTypeAdapters() {
    //
  }

  // ==========================================================================
  // = Number
  // ==========================================================================

  public static final TMLTypeAdapter<Number> NUMBER = new TMLTypeAdapter<Number>() {
    @Override
    public Number read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      } else if (p == TMLToken.END_LIST) {
        return null;
      }
      String str = in.nextString();
      TMLLazilyParsedNumber number = TMLLazilyParsedNumber.get(str);
      if (number == null) {
        throw new TMLSyntaxException("Expecting number but was: " + str);
      }
      return number;
    }

    @Override
    public void write(TMLWriter out, Number value)
        throws IOException {
      out.value(value);
    }
  };

  public static final TMLTypeAdapterFactory NUMBER_FACTORY = newFactory(
      Number.class, NUMBER);

  // ==========================================================================
  // = StringBuilder
  // ==========================================================================

  public static final TMLTypeAdapter<StringBuilder> STRING_BUILDER = new TMLTypeAdapter<StringBuilder>() {
    @Override
    public StringBuilder read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      } else if (p == TMLToken.END_LIST) {
        return new StringBuilder();
      }

      StringBuilder sb = new StringBuilder();
      while (p == TMLToken.DATA) {
        sb.append(in.nextString());
        if ((p = in.peek()) == TMLToken.DATA) {
          sb.append(' ');
        }
      }
      return sb;
    }

    @Override
    public void write(TMLWriter out, StringBuilder value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TMLTypeAdapterFactory STRING_BUILDER_FACTORY = newFactory(
      StringBuilder.class, STRING_BUILDER);

  // ==========================================================================
  // = BigDecimal
  // ==========================================================================

  public static final TMLTypeAdapter<BigDecimal> BIG_DECIMAL = new TMLTypeAdapter<BigDecimal>() {
    @Override
    public BigDecimal read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return new BigDecimal(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, BigDecimal value) throws IOException {
      out.value(value);
    }
  };

  public static final TMLTypeAdapterFactory BIG_DECIMAL_FACTORY = newFactory(
      BigDecimal.class, BIG_DECIMAL);

  // ==========================================================================
  // = BigInteger
  // ==========================================================================

  public static final TMLTypeAdapter<BigInteger> BIG_INTEGER = new TMLTypeAdapter<BigInteger>() {
    @Override
    public BigInteger read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return new BigInteger(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, BigInteger value) throws IOException {
      out.value(value);
    }
  };

  public static final TMLTypeAdapterFactory BIG_INTEGER_FACTORY = newFactory(
      BigInteger.class, BIG_INTEGER);

  // ==========================================================================
  // = StringBuffer
  // ==========================================================================

  public static final TMLTypeAdapter<StringBuffer> STRING_BUFFER = new TMLTypeAdapter<StringBuffer>() {
    @Override
    public StringBuffer read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      } else if (p == TMLToken.END_LIST) {
        return new StringBuffer();
      }

      StringBuffer sb = new StringBuffer();
      while (p == TMLToken.DATA) {
        sb.append(in.nextString());
        if ((p = in.peek()) == TMLToken.DATA) {
          sb.append(' ');
        }
      }
      return sb;
    }

    @Override
    public void write(TMLWriter out, StringBuffer value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TMLTypeAdapterFactory STRING_BUFFER_FACTORY = newFactory(
      StringBuffer.class, STRING_BUFFER);

  // ==========================================================================
  // = URL
  // ==========================================================================

  public static final TMLTypeAdapter<URL> URL = new TMLTypeAdapter<URL>() {
    @Override
    public URL read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      String nextString = in.nextString();
      return "null".equals(nextString) ? null : new URL(nextString);
    }

    @Override
    public void write(TMLWriter out, URL value)
        throws IOException {
      out.value(value == null ? (String) null : value.toExternalForm());
    }
  };

  public static final TMLTypeAdapterFactory URL_FACTORY = newFactory(URL.class,
      URL);

  // ==========================================================================
  // = URI
  // ==========================================================================

  public static final TMLTypeAdapter<URI> URI = new TMLTypeAdapter<URI>() {
    @Override
    public URI read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        String nextString = in.nextString();
        return "null".equals(nextString) ? null : new URI(nextString);
      } catch (URISyntaxException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, URI value)
        throws IOException {
      out.value(value == null ? null : value.toASCIIString());
    }
  };

  public static final TMLTypeAdapterFactory URI_FACTORY = newFactory(URI.class,
      URI);

  // ==========================================================================
  // = UUID
  // ==========================================================================

  public static final TMLTypeAdapter<UUID> UUID = new TMLTypeAdapter<UUID>() {
    @Override
    public UUID read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      return java.util.UUID.fromString(in.nextString());
    }

    @Override
    public void write(TMLWriter out, UUID value)
        throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TMLTypeAdapterFactory UUID_FACTORY = newFactory(
      UUID.class, UUID);

  // ==========================================================================
  // = Locale
  // ==========================================================================

  public static final TMLTypeAdapter<Locale> LOCALE = new TMLTypeAdapter<Locale>() {
    @Override
    public Locale read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      String locale = in.nextString();
      StringTokenizer tokenizer = new StringTokenizer(locale, "_");
      String language = null;
      String country = null;
      String variant = null;
      if (tokenizer.hasMoreElements()) {
        language = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        country = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        variant = tokenizer.nextToken();
      }
      if (country == null && variant == null) {
        return new Locale(language);
      } else if (variant == null) {
        return new Locale(language, country);
      } else {
        return new Locale(language, country, variant);
      }
    }

    @Override
    public void write(TMLWriter out, Locale value)
        throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TMLTypeAdapterFactory LOCALE_FACTORY = newFactory(
      Locale.class, LOCALE);

  // ==========================================================================
  // = InetAddress
  // ==========================================================================

  public static final TMLTypeAdapter<InetAddress> INET_ADDRESS = new TMLTypeAdapter<InetAddress>() {
    @Override
    public InetAddress read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      // regrettably, this should have included both the host name and the host
      // address
      return InetAddress.getByName(in.nextString());
    }

    @Override
    public void write(TMLWriter out, InetAddress value) throws IOException {
      out.value(value == null ? null : value.getHostAddress());
    }
  };

  public static final TMLTypeAdapterFactory INET_ADDRESS_FACTORY = newTypeHierarchyFactory(
      InetAddress.class, INET_ADDRESS);

  // ==========================================================================
  // = BitSet
  // ==========================================================================

  public static final TMLTypeAdapter<BitSet> BIT_SET = new TMLTypeAdapter<BitSet>() {
    public BitSet read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      }

      BitSet bitset = new BitSet();
      int i = 0;
      int scope = in.getScope();
      while (in.hasNextInScope(scope)) {
        boolean set;
        String stringValue = in.nextString();
        try {
          set = Integer.parseInt(stringValue) != 0;
        } catch (NumberFormatException e) {
          String bool = stringValue.toLowerCase();
          if (bool.equals("true") || bool.equals("false")) {
            set = Boolean.parseBoolean(bool);
          } else {
            throw new TMLSyntaxException(
                "Expecting bitset number value (1, 0) or (true, false), found: "
                    + stringValue);
          }
        }

        if (set) bitset.set(i);
        i++;
      }
      return bitset;
    }

    public void write(TMLWriter out, BitSet src)
        throws IOException {
      if (src == null) {
        out.nullValue();
        return;
      }

      for (int i = 0; i < src.length(); i++) {
        int value = (src.get(i)) ? 1 : 0;
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory BIT_SET_FACTORY = newFactory(
      BitSet.class, BIT_SET);

  // ==========================================================================
  // = Calendar
  // ==========================================================================

  public static final TMLTypeAdapter<Calendar> CALENDAR = new TMLTypeAdapter<Calendar>() {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String HOUR_OF_DAY = "hourOfDay";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    @Override
    public Calendar read(TMLReader in)
        throws IOException {

      boolean encapsulate = in.getLastToken() != TMLToken.DIVIDER;
      if (encapsulate) in.beginList();

      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }

      try {
        int year = 0;
        int month = 0;
        int dayOfMonth = 0;
        int hourOfDay = 0;
        int minute = 0;
        int second = 0;
        int scope = in.getScope();
        while (in.hasNextInScope(scope)) {
          in.beginList();
          String name = in.nextString();
          in.consumeDivider();
          int value = Integer.valueOf(in.nextString());
          if (YEAR.equals(name)) {
            year = value;
          } else if (MONTH.equals(name)) {
            month = value;
          } else if (DAY_OF_MONTH.equals(name)) {
            dayOfMonth = value;
          } else if (HOUR_OF_DAY.equals(name)) {
            hourOfDay = value;
          } else if (MINUTE.equals(name)) {
            minute = value;
          } else if (SECOND.equals(name)) {
            second = value;
          }
          in.endList();
        }
        if (encapsulate) in.endList();
        return new GregorianCalendar(year, month, dayOfMonth, hourOfDay,
            minute, second);
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Calendar value)
        throws IOException {

      boolean encapsulate = !out.getAndClearImmediateFieldChild();
      if (encapsulate) out.beginList();

      if (value == null) {
        out.nullValue();
        if (encapsulate) out.endList();
        return;
      }

      out.beginList();
      out.name(YEAR).value(value.get(Calendar.YEAR));
      out.endList();
      out.beginList();
      out.name(MONTH).value(value.get(Calendar.MONTH));
      out.endList();
      out.beginList();
      out.name(DAY_OF_MONTH).value(value.get(Calendar.DAY_OF_MONTH));
      out.endList();
      out.beginList();
      out.name(HOUR_OF_DAY).value(value.get(Calendar.HOUR_OF_DAY));
      out.endList();
      out.beginList();
      out.name(MINUTE).value(value.get(Calendar.MINUTE));
      out.endList();
      out.beginList();
      out.name(SECOND).value(value.get(Calendar.SECOND));
      out.endList();

      if (encapsulate) out.endList();
    }

    public boolean isRootEncapsulate() {
      return false;
    };
  };

  public static final TMLTypeAdapterFactory CALENDAR_FACTORY = newFactoryForMultipleTypes(
      Calendar.class, GregorianCalendar.class, CALENDAR);

  // ==========================================================================
  // = Class
  // ==========================================================================

  @SuppressWarnings("rawtypes")
  public static final TMLTypeAdapter<Class> CLASS = new TMLTypeAdapter<Class>() {
    @Override
    public Class read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.END_LIST) {
        return null;
      } else if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      throw new UnsupportedOperationException(
          "Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
    }

    @Override
    public void write(TMLWriter out, Class value)
        throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      throw new UnsupportedOperationException(
          "Attempted to serialize java.lang.Class: " + value.getName()
              + ". Forgot to register a type adapter?");
    }
  };

  public static final TMLTypeAdapterFactory CLASS_FACTORY = newFactory(
      Class.class, CLASS);

  // ==========================================================================
  // = Boolean
  // ==========================================================================

  public static final TMLTypeAdapter<Boolean> BOOLEAN = new TMLTypeAdapter<Boolean>() {
    @Override
    public Boolean read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      String bool = in.nextString().toLowerCase();
      if (bool.equals("true") || bool.equals("false")) {
        return Boolean.parseBoolean(bool);
      }
      throw new TMLSyntaxException("Expecting boolean (true, false), found: "
          + bool);
    }

    @Override
    public void write(TMLWriter out, Boolean value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory BOOLEAN_FACTORY = newFactory(
      boolean.class, Boolean.class, BOOLEAN);

  /**
   * Writes a boolean as a string. Useful for map keys, where booleans aren't
   * otherwise permitted.
   */
  public static final TMLTypeAdapter<Boolean> BOOLEAN_AS_STRING = new TMLTypeAdapter<Boolean>() {
    @Override
    public Boolean read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      String bool = in.nextString().toLowerCase();
      if (bool.equals("true") || bool.equals("false")) {
        return Boolean.parseBoolean(bool);
      }
      throw new TMLSyntaxException("Expecting boolean (true, false), found: "
          + bool);
    }

    @Override
    public void write(TMLWriter out, Boolean value)
        throws IOException {
      out.value(value == null ? "\\0" : value.toString());
    }
  };

  // ==========================================================================
  // = Integer
  // ==========================================================================

  public static final TMLTypeAdapter<Integer> INTEGER = new TMLTypeAdapter<Integer>() {
    @Override
    public Integer read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }

      try {
        return Integer.parseInt(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Integer value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory INTEGER_FACTORY = newFactory(
      int.class, Integer.class, INTEGER);

  // ==========================================================================
  // = Byte
  // ==========================================================================

  public static final TMLTypeAdapter<Number> BYTE = new TMLTypeAdapter<Number>() {
    @Override
    public Number read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return (byte) Integer.parseInt(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Number value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory BYTE_FACTORY = newFactory(
      byte.class, Byte.class, BYTE);

  // ==========================================================================
  // = Character
  // ==========================================================================

  public static final TMLTypeAdapter<Character> CHARACTER = new TMLTypeAdapter<Character>() {
    @Override
    public Character read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      String value = in.nextString();
      if (value.length() != 1) {
        throw new TMLSyntaxException("Expecting character, got: [" + value
            + "]");
      }
      return value.charAt(0);
    }

    @Override
    public void write(TMLWriter out, Character value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.toString());
      }
    }
  };

  public static final TMLTypeAdapterFactory CHARACTER_FACTORY = newFactory(
      char.class, Character.class, CHARACTER);

  // ==========================================================================
  // = Short
  // ==========================================================================

  public static final TMLTypeAdapter<Number> SHORT = new TMLTypeAdapter<Number>() {
    @Override
    public Number read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return (short) Integer.parseInt(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Number value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory SHORT_FACTORY = newFactory(
      short.class, Short.class, SHORT);

  // ==========================================================================
  // = Long
  // ==========================================================================

  public static final TMLTypeAdapter<Long> LONG = new TMLTypeAdapter<Long>() {
    @Override
    public Long read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return Long.parseLong(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Long value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory LONG_FACTORY = newFactory(
      long.class, Long.class, LONG);

  // ==========================================================================
  // = Float
  // ==========================================================================

  public static final TMLTypeAdapter<Float> FLOAT = new TMLTypeAdapter<Float>() {
    @Override
    public Float read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return Float.parseFloat(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Float value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory FLOAT_FACTORY = newFactory(
      float.class, Float.class, FLOAT);

  // ==========================================================================
  // = Double
  // ==========================================================================

  public static final TMLTypeAdapter<Double> DOUBLE = new TMLTypeAdapter<Double>() {
    @Override
    public Double read(TMLReader in)
        throws IOException {
      if (in.peek() == TMLToken.END_LIST) {
        return null;
      } else if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return Double.parseDouble(in.nextString());
      } catch (NumberFormatException e) {
        throw new TMLSyntaxException(e);
      }
    }

    @Override
    public void write(TMLWriter out, Double value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }
  };

  public static final TMLTypeAdapterFactory DOUBLE_FACTORY = newFactory(
      double.class, Double.class, DOUBLE);

  // ==========================================================================
  // = String
  // ==========================================================================

  public static final TMLTypeAdapter<String> STRING = new TMLTypeAdapter<String>() {
    @Override
    public String read(TMLReader in)
        throws IOException {
      TMLToken p = in.peek();
      if (p == TMLToken.END_LIST) {
        return null;
      } else if (p == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      return getAllStringsInScope(in);
    }

    @Override
    public void write(TMLWriter out, String value)
        throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value);
      }
    }

    public boolean isArrayEncapsulate() {
      return true;
    };
  };

  public static final TMLTypeAdapterFactory STRING_FACTORY = newFactory(
      String.class, STRING);

  // ==========================================================================
  // = Timestamp
  // ==========================================================================

  public static final TMLTypeAdapterFactory TIMESTAMP_FACTORY = new TMLTypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    // we use a runtime check to make sure the 'T's equal
    public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
      if (typeToken.getRawType() != Timestamp.class) {
        return null;
      }

      final TMLTypeAdapter<Date> dateTypeAdapter = juple.getAdapter(Date.class);
      return (TMLTypeAdapter<T>) new TMLTypeAdapter<Timestamp>() {
        @Override
        public Timestamp read(TMLReader in)
            throws IOException {
          Date date = dateTypeAdapter.read(in);
          return date != null ? new Timestamp(date.getTime()) : null;
        }

        @Override
        public void write(TMLWriter out, Timestamp value) throws IOException {
          dateTypeAdapter.write(out, value);
        }
      };
    }
  };

  // ==========================================================================
  // = Enum
  // ==========================================================================

  private static final class TMLEnumTypeAdapter<T extends Enum<T>> extends
      TMLTypeAdapter<T> {
    private final Map<String, T> nameToConstant = new HashMap<String, T>();
    private final Map<T, String> constantToName = new HashMap<T, String>();

    public TMLEnumTypeAdapter(Class<T> classOfT) {
      try {
        for (T constant : classOfT.getEnumConstants()) {
          String name = constant.name();
          SerializedName annotation = classOfT.getField(name).getAnnotation(
              SerializedName.class);
          if (annotation != null) {
            name = annotation.value();
          }
          nameToConstant.put(name, constant);
          constantToName.put(constant, name);
        }
      } catch (NoSuchFieldException e) {
        throw new AssertionError();
      }
    }

    public T read(TMLReader in) throws IOException {
      if (in.peek() == TMLToken.NULL) {
        in.nextNull();
        return null;
      }
      return nameToConstant.get(in.nextString());
    }

    public void write(TMLWriter out, T value)
        throws IOException {
      out.value(value == null ? null : constantToName.get(value));
    }
  }

  public static final TMLTypeAdapterFactory ENUM_FACTORY = newEnumTypeHierarchyFactory();

  // ==========================================================================
  // = Factory creation methods
  // ==========================================================================

  public static <TT> TMLTypeAdapterFactory newFactory(
      final TMLTypeToken<TT> type, final TMLTypeAdapter<TT> typeAdapter) {
    return new TMLTypeAdapterFactory() {
      // we use a runtime check to make sure the 'T's equal
      @SuppressWarnings("unchecked")
      public <T> TMLTypeAdapter<T> create(Juple parser,
          TMLTypeToken<T> typeToken) {
        return typeToken.equals(type) ? (TMLTypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TMLTypeAdapterFactory newFactory(final Class<TT> type,
      final TMLTypeAdapter<TT> typeAdapter) {
    return new TMLTypeAdapterFactory() {
      // we use a runtime check to make sure the 'T's equal
      @SuppressWarnings("unchecked")
      public <T> TMLTypeAdapter<T> create(Juple parser,
          TMLTypeToken<T> typeToken) {
        if (typeToken.getRawType() == type) {
          return (TMLTypeAdapter<T>) typeAdapter;
        }
        return null;
      }

      @Override
      public String toString() {
        return "Factory[type=" + type.getName() + ", adapter=" + typeAdapter
            + "]";
      }
    };
  }

  public static <TT> TMLTypeAdapterFactory newFactory(final Class<TT> unboxed,
      final Class<TT> boxed, final TMLTypeAdapter<? super TT> typeAdapter) {
    return new TMLTypeAdapterFactory() {
      // we use a runtime check to make sure the 'T's equal
      @SuppressWarnings("unchecked")
      public <T> TMLTypeAdapter<T> create(Juple parser,
          TMLTypeToken<T> typeToken) {
        Class<?> type = typeToken.getRawType();
        if (type == unboxed || type == boxed) {
          return (TMLTypeAdapter<T>) typeAdapter;
        }
        return null;
      }

      @Override
      public String toString() {
        return "Factory[type=" + boxed.getName() + "+" + unboxed.getName()
            + ", adapter=" + typeAdapter + "]";
      }
    };
  }

  public static TMLTypeAdapterFactory newEnumTypeHierarchyFactory() {
    return new TMLTypeAdapterFactory() {
      @SuppressWarnings({ "rawtypes", "unchecked" })
      public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
          return null;
        }
        if (!rawType.isEnum()) {
          rawType = rawType.getSuperclass(); // handle anonymous subclasses
        }
        return (TMLTypeAdapter<T>) new TMLEnumTypeAdapter(rawType);
      }
    };
  }

  public static <TT> TMLTypeAdapterFactory newFactoryForMultipleTypes(
      final Class<TT> base, final Class<? extends TT> sub,
      final TMLTypeAdapter<? super TT> typeAdapter) {
    return new TMLTypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      // we use a runtime check to make sure the 'T's equal
      public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == base || rawType == sub) ? (TMLTypeAdapter<T>) typeAdapter
            : null;
      }

      @Override
      public String toString() {
        return "Factory[type=" + base.getName() + "+" + sub.getName()
            + ",adapter=" + typeAdapter + "]";
      }
    };
  }

  public static <TT> TMLTypeAdapterFactory newTypeHierarchyFactory(
      final Class<TT> clazz, final TMLTypeAdapter<TT> typeAdapter) {
    return new TMLTypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
        return clazz.isAssignableFrom(typeToken.getRawType()) ? (TMLTypeAdapter<T>) typeAdapter
            : null;
      }

      @Override
      public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter="
            + typeAdapter + "]";
      }
    };
  }

}
