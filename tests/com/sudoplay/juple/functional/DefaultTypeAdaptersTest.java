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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Functional test for TML serialization and deserialization for common classes
 * for which default support is provided in Juple. The tests for Map types are
 * available in {@link MapTest}.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public class DefaultTypeAdaptersTest {

  private static Juple juple;
  private static TimeZone oldTimeZone;

  @BeforeClass
  public static void initialize() {
    oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
    juple = new Juple();
  }

  @AfterClass
  public static void cleanup() {
    TimeZone.setDefault(oldTimeZone);
  }

  // ==========================================================================
  // = Class.class
  // ==========================================================================

  @SuppressWarnings("rawtypes")
  private static class MyClassTypeAdapter extends TMLTypeAdapter<Class> {
    @Override
    public void write(TMLWriter out, Class value) throws IOException {
      out.value(value.getName());
    }

    @Override
    public Class read(TMLReader in) throws IOException {
      String className = in.nextString();
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }
    }
  }

  @Test
  public void testClassSerialization() {
    try {
      juple.toTML(String.class);
      fail();
    } catch (UnsupportedOperationException expected) {}
    Juple juple = new JupleBuilder().registerTypeAdapter(Class.class,
        new MyClassTypeAdapter()).create();
    assertEquals("[java.lang.String]", juple.toTML(String.class));
  }

  @Test
  public void testClassDeserialization() {
    try {
      juple.fromTML("[String.class]", String.class.getClass());
      fail();
    } catch (UnsupportedOperationException expected) {}
    Juple juple = new JupleBuilder().registerTypeAdapter(Class.class,
        new MyClassTypeAdapter()).create();
    assertEquals(String.class, juple.fromTML("[java.lang.String]", Class.class));
  }

  // ==========================================================================
  // = URL.class
  // ==========================================================================

  private static class ClassWithURLField {
    URL url;
  }

  @Test
  public void testURLSerialization() throws MalformedURLException {
    URL url = new URL("http://google.com/");
    assertEquals("[http://google.com/]", juple.toTML(url));
  }

  @Test
  public void testURLDeserialization() {
    String expected = "http://google.com/";
    String tml = "[http://google.com/]";
    URL actual = juple.fromTML(tml, URL.class);
    assertEquals(expected, actual.toExternalForm());
  }

  @Test
  public void testURLAsClassMemberSerialization() throws MalformedURLException {
    ClassWithURLField c = new ClassWithURLField();
    c.url = new URL("http://google.com/");
    assertEquals("[[url|http://google.com/]]", juple.toTML(c));
  }

  @Test
  public void testURLAsClassMemberDeserialization()
      throws MalformedURLException {
    ClassWithURLField c = juple.fromTML("[[url|http://google.com/]]",
        ClassWithURLField.class);
    assertEquals(c.url, new URL("http://google.com/"));
  }

  @Test
  public void testURLAsNullClassMemberSerialization()
      throws MalformedURLException {
    ClassWithURLField c = new ClassWithURLField();
    assertEquals("[[url|\\0]]", juple.toTML(c));
  }

  @Test
  public void testURLAsNullClassMemberDeserialization()
      throws MalformedURLException {
    ClassWithURLField c = juple.fromTML("[[url|\\0]]", ClassWithURLField.class);
    assertNull(c.url);
  }

  // ==========================================================================
  // = URI.class
  // ==========================================================================

  private static class ClassWithURIField {
    URI uri;
  }

  @Test
  public void testURISerialization() throws URISyntaxException {
    URI uri = new URI("http://google.com/");
    assertEquals("[http://google.com/]", juple.toTML(uri));
  }

  @Test
  public void testURIDeserialization() {
    String expected = "http://google.com/";
    String tml = "[http://google.com/]";
    URI actual = juple.fromTML(tml, URI.class);
    assertEquals(expected, actual.toASCIIString());
  }

  @Test
  public void testURIAsClassMemberSerialization() throws URISyntaxException {
    ClassWithURIField c = new ClassWithURIField();
    c.uri = new URI("http://google.com/");
    assertEquals("[[uri|http://google.com/]]", juple.toTML(c));
  }

  @Test
  public void testURIAsClassMemberDeserialization() throws URISyntaxException {
    ClassWithURIField c = juple.fromTML("[[uri|http://google.com/]]",
        ClassWithURIField.class);
    assertEquals(new URI("http://google.com/"), c.uri);
  }

  @Test
  public void testURIAsNullClassMemberSerialization() {
    ClassWithURIField c = new ClassWithURIField();
    assertEquals("[[uri|\\0]]", juple.toTML(c));
  }

  @Test
  public void testURIAsNullClassMemberDeserialization() {
    ClassWithURIField c = juple.fromTML("[[uri|\\0]]", ClassWithURIField.class);
    assertNull(c.uri);
  }

  // ==========================================================================
  // = UUID.class
  // ==========================================================================

  private static class ClassWithUUIDField {
    UUID uuid;
  }

  @Test
  public void testUUIDSerialization() {
    String value = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID o = UUID.fromString(value);
    assertEquals('[' + value + ']', juple.toTML(o));
  }

  @Test
  public void testUUIDDeserialization() {
    String expected = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    String tml = '[' + expected + ']';
    UUID actual = juple.fromTML(tml, UUID.class);
    assertEquals(expected, actual.toString());
  }

  @Test
  public void testUUIDAsClassMemberSerialization() {
    ClassWithUUIDField c = new ClassWithUUIDField();
    String value = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    c.uuid = UUID.fromString(value);
    String expected = "[[uuid|" + value + "]]";
    assertEquals(expected, juple.toTML(c));
  }

  @Test
  public void testUUIDAsClassMemberDeserialization() {
    String value = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID expected = UUID.fromString(value);
    String tml = "[[uuid|" + value + "]]";
    ClassWithUUIDField c = juple.fromTML(tml, ClassWithUUIDField.class);
    assertEquals(expected, c.uuid);
  }

  // ==========================================================================
  // = Locale.class
  // ==========================================================================

  private static class ClassWithLocaleField {
    Locale locale;
  }

  @Test
  public void testLocaleAsClassMemberSerialization() {
    ClassWithLocaleField c = new ClassWithLocaleField();
    c.locale = new Locale("en");
    assertEquals("[[locale|en]]", juple.toTML(c));
  }

  @Test
  public void testLocaleAsClassMemberDeserialization() {
    ClassWithLocaleField c = juple.fromTML("[[locale|en]]",
        ClassWithLocaleField.class);
    assertEquals("en", c.locale.getLanguage());
  }

  @Test
  public void testLocaleSerializationWithLanguage() {
    Locale locale = new Locale("en");
    assertEquals("[en]", juple.toTML(locale));
  }

  @Test
  public void testLocaleDeserializationWithLanguage() {
    String tml = "[en]";
    Locale actual = juple.fromTML(tml, Locale.class);
    assertEquals("en", actual.getLanguage());
  }

  @Test
  public void testLocaleSerializationWithLanguageCountry() {
    Locale locale = Locale.CANADA_FRENCH;
    assertEquals("[fr_CA]", juple.toTML(locale));
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountry() {
    String tml = "[fr_CA]";
    Locale actual = juple.fromTML(tml, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, actual);
  }

  @Test
  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale locale = new Locale("de", "DE", "EURO");
    assertEquals("[de_DE_EURO]", juple.toTML(locale));
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String tml = "[de_DE_EURO]";
    Locale actual = juple.fromTML(tml, Locale.class);
    assertEquals("de", actual.getLanguage());
    assertEquals("DE", actual.getCountry());
    assertEquals("EURO", actual.getVariant());
  }

  // ==========================================================================
  // = BigDecimal.class
  // ==========================================================================

  private static class ClassWithBigDecimalField {
    BigDecimal value;
  }

  @Test
  public void testBigDecimalAsClassMemberSerialization() {
    ClassWithBigDecimalField c = new ClassWithBigDecimalField();
    c.value = new BigDecimal("-122.01e-21");
    assertEquals("[[value|" + c.value.toString() + "]]", juple.toTML(c));
  }

  @Test
  public void testBigDecimalAsClassMemberDeserialization() {
    BigDecimal actual = new BigDecimal("-122.01e-21");
    ClassWithBigDecimalField expected = juple.fromTML(
        "[[value|" + actual.toString() + "]]", ClassWithBigDecimalField.class);
    assertEquals(actual, expected.value);
  }

  @Test
  public void testBigDecimalSerialization() {
    BigDecimal value = new BigDecimal("-122.01e-21");
    assertEquals(("[" + value.toString() + "]"), juple.toTML(value));
  }

  @Test
  public void testBigDecimalDeserialization() {
    BigDecimal expected = new BigDecimal("-122.01e-21");
    BigDecimal actual = juple.fromTML("[" + expected.toString() + "]",
        BigDecimal.class);
    assertEquals(expected.toString(), actual.toString());
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBigDecimalBadValueForDeserialization_TMLSyntaxException() {
    juple.fromTML("[1.5e-1.0031]", BigDecimal.class);
  }

  @Test
  public void testBigDecimalOverrideTypeAdapter() throws Exception {
    Juple juple = new JupleBuilder().registerTypeAdapter(BigDecimal.class,
        new NumberAsStringAdapter(BigDecimal.class)).create();
    assertEquals("[1.1]", juple.toTML(new BigDecimal("1.1"), BigDecimal.class));
    assertEquals(new BigDecimal("1.1"),
        juple.fromTML("[1.1]", BigDecimal.class));
  }

  // ==========================================================================
  // = BigInteger.class
  // ==========================================================================

  private static class ClassWithBigIntegerField {
    BigInteger value;
  }

  @Test
  public void testBigIntegerAsClassMemberSerialization() {
    ClassWithBigIntegerField c = new ClassWithBigIntegerField();
    c.value = new BigInteger("23232323215323234234324324324324324324");
    assertEquals("[[value|" + c.value.toString() + "]]", juple.toTML(c));
  }

  @Test
  public void testBigIntegerAsClassMemberDeserialization() {
    BigInteger actual = new BigInteger("23232323215323234234324324324324324324");
    ClassWithBigIntegerField expected = juple.fromTML(
        "[[value|" + actual.toString() + "]]", ClassWithBigIntegerField.class);
    assertEquals(actual, expected.value);
  }

  @Test
  public void testBigIntegerSerialization() {
    BigInteger value = new BigInteger("879697697697697697697697697697697697");
    assertEquals(("[" + value.toString() + "]"), juple.toTML(value));
  }

  @Test
  public void testBigIntegerDeserialization() {
    BigInteger expected = new BigInteger("879697697697697697697697697697697697");
    BigInteger actual = juple.fromTML("[" + expected.toString() + "]",
        BigInteger.class);
    assertEquals(expected.toString(), actual.toString());
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBigIntegerBadValueForDeserialization_TMLSyntaxException() {
    juple.fromTML("[1.5e-1.0031]", BigInteger.class);
  }

  @Test
  public void testBigIntegerOverrideTypeAdapter() throws Exception {
    Juple juple = new JupleBuilder().registerTypeAdapter(BigInteger.class,
        new NumberAsStringAdapter(BigInteger.class)).create();
    assertEquals("[123]", juple.toTML(new BigInteger("123"), BigInteger.class));
    assertEquals(new BigInteger("123"),
        juple.fromTML("[123]", BigInteger.class));
  }

  // ==========================================================================
  // = Set.class
  // ==========================================================================

  @Test
  public void testSetSerialization() {
    HashSet<String> s = new HashSet<String>();
    s.add("data string");
    assertEquals("[[data string]]", juple.toTML(s));
    assertEquals("[[data string]]", juple.toTML(s, Set.class));
  }

  // ==========================================================================
  // = BitSet.class
  // ==========================================================================

  @Test(expected = TMLSyntaxException.class)
  public void testBitSetInvalidBinaryThrows() {
    String bs = "[0 1 1 0 0 1 0 0 wut? 0 1 1]";
    juple.fromTML(bs, BitSet.class);
  }

  @Test(expected = TMLSyntaxException.class)
  public void testBitSetInvalidBooleanThrows() {
    String bs = "[true false true true true true wut? false true false false]";
    juple.fromTML(bs, BitSet.class);
  }

  @Test
  public void testBitSetSerialization() {
    BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    assertEquals("[0 1 0 1 1 1 0 0 0 1]", juple.toTML(bits));
  }

  @Test
  public void testBitSetDeserialization() {
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);
    String tml = juple.toTML(expected);
    assertEquals(expected, juple.fromTML(tml, BitSet.class));

    tml = "[1 0 1 1 1 1 0 0 1 0 0 0 0 0 0 0]"; // note extra zeros
    assertEquals(expected, juple.fromTML(tml, BitSet.class));

    tml = "[true false true true true true false false true false false]";
    assertEquals(expected, juple.fromTML(tml, BitSet.class));
  }

  @Test
  public void testBitSetDeserializationOfEmptyBitSet() {
    BitSet expected = new BitSet();
    BitSet actual = juple.fromTML("[]", BitSet.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testBitSetSerializationOfEmptyBitSet() {
    BitSet bs = new BitSet();
    assertEquals("[]", juple.toTML(bs));
  }

  // ==========================================================================
  // = Date.class (default date)
  // ==========================================================================

  @Test
  public void testDefaultDateSerialization() {
    Date now = new Date(1315806903103L);
    assertEquals("[Sep 11, 2011 10:55:03 PM]", juple.toTML(now));
  }

  @Test
  public void testDefaultDateDeserialization() {
    String tml = "[Dec 13, 2009 07:18:02 AM]";
    Date extracted = juple.fromTML(tml, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 2);
  }

  /*
   * Date can not directly be compared with another instance since the
   * deserialization loses the millisecond portion.
   */
  @SuppressWarnings("deprecation")
  private void assertEqualsDate(Date date, int year, int month, int day) {
    assertEquals(year - 1900, date.getYear());
    assertEquals(month, date.getMonth());
    assertEquals(day, date.getDate());
  }

  @SuppressWarnings("deprecation")
  private void assertEqualsTime(Date date, int hours, int minutes, int seconds) {
    assertEquals(hours, date.getHours());
    assertEquals(minutes, date.getMinutes());
    assertEquals(seconds, date.getSeconds());
  }

  @Test
  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Juple juple = new JupleBuilder().setDateFormat(DateFormat.FULL)
        .setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String tml = juple.toTML(now);
    assertEquals("[2011-09-11]", tml);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Juple juple = new JupleBuilder().setDateFormat(DateFormat.FULL)
        .setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String tml = juple.toTML(now);
    Date extracted = juple.fromTML(tml, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

  @Test
  public void testDateSerializationInCollection() throws Exception {
    Type listOfDates = new TMLTypeToken<List<Date>>() {}.getType();
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Juple juple = new JupleBuilder().setDateFormat("yyyy-MM-dd").create();
      List<Date> dates = Arrays.asList(new Date(0));
      String tml = juple.toTML(dates, listOfDates);
      assertEquals("[[1970-01-01]]", tml);
      assertEquals(0L, juple
          .<List<Date>> fromTML("[[1970-01-01]]", listOfDates).get(0).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  // ==========================================================================
  // = java.sql.*
  // ==========================================================================

  @Test
  public void testSqlDateSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      java.sql.Date sqlDate = new java.sql.Date(0L);
      Juple juple = new JupleBuilder().setDateFormat("yyyy-MM-dd").create();
      String tml = juple.toTML(sqlDate, java.sql.Date.class);
      assertEquals("[1970-01-01]", tml);
      assertEquals(0, juple.fromTML("[1970-01-01]", java.sql.Date.class)
          .getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testTimestampSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Timestamp timestamp = new Timestamp(0L);
      Juple juple = new JupleBuilder().setDateFormat("yyyy-MM-dd").create();
      String tml = juple.toTML(timestamp, Timestamp.class);
      assertEquals("[1970-01-01]", tml);
      assertEquals(0, juple.fromTML("[1970-01-01]", Timestamp.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testSqlDateSerializationWithPattern() {
    String pattern = "yyyy-MM-dd";
    Juple juple = new JupleBuilder().setDateFormat(DateFormat.FULL)
        .setDateFormat(pattern).create();
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String tml = juple.toTML(instant);
    assertEquals("[2009-12-03]", tml);
  }

  @Test
  public void testSqlDateDeserializationWithPattern() {
    String pattern = "yyyy-MM-dd";
    Juple juple = new JupleBuilder().setDateFormat(DateFormat.FULL)
        .setDateFormat(pattern).create();
    String tml = "[2009-12-03]";
    java.sql.Date extracted = juple.fromTML(tml, java.sql.Date.class);
    assertEqualsDate(extracted, 2009, 11, 3);
  }

  @Test
  public void testDefaultJavaSqlDateSerialization() {
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String tml = juple.toTML(instant);
    assertEquals("[Dec 3, 2009]", tml);
  }

  @Test
  public void testDefaultJavaSqlDateDeserialization() {
    String tml = "[Dec 3, 2009]";
    java.sql.Date extracted = juple.fromTML(tml, java.sql.Date.class);
    assertEqualsDate(extracted, 2009, 11, 3);
  }

  @Test
  public void testDefaultJavaSqlTimestampSerialization() {
    Timestamp now = new java.sql.Timestamp(1259875082000L);
    String tml = juple.toTML(now);
    assertEquals("[Dec 3, 2009 1:18:02 PM]", tml);
  }

  @Test
  public void testDefaultJavaSqlTimestampDeserialization() {
    String tml = "[Dec 3, 2009 1:18:02 PM]";
    Timestamp extracted = juple.fromTML(tml, Timestamp.class);
    assertEqualsDate(extracted, 2009, 11, 3);
    assertEqualsTime(extracted, 13, 18, 2);
  }

  @Test
  public void testDefaultJavaSqlTimeSerialization() {
    Time now = new Time(1259875082000L);
    String tml = juple.toTML(now);
    assertEquals("[01:18:02 PM]", tml);
  }

  @Test
  public void testDefaultJavaSqlTimeDeserialization() {
    String tml = "[1:18:02 PM]";
    Time extracted = juple.fromTML(tml, Time.class);
    assertEqualsTime(extracted, 13, 18, 2);
  }

  @Test
  public void testSqlTimeSerializationWithPattern() {
    String pattern = "a ss:mm:hh";
    Juple juple = new JupleBuilder().setTimeFormat(pattern).create();
    Time now = new Time(1259875082000L);
    String tml = juple.toTML(now);
    assertEquals("[PM 02:18:01]", tml);
  }

  @Test
  public void testSqlTimeDeserializationWithPattern() {
    String pattern = "a ss:mm:hh";
    Juple juple = new JupleBuilder().setTimeFormat(pattern).create();
    String tml = "[PM 02:18:01]";
    Time extracted = juple.fromTML(tml, Time.class);
    assertEqualsTime(extracted, 13, 18, 2);
  }

  // ==========================================================================
  // = Calendar
  // ==========================================================================

  @Test
  public void testDefaultCalendarSerialization() throws Exception {
    String tml = juple.toTML(Calendar.getInstance());
    assertTrue(tml.contains("year"));
    assertTrue(tml.contains("month"));
    assertTrue(tml.contains("dayOfMonth"));
    assertTrue(tml.contains("hourOfDay"));
    assertTrue(tml.contains("minute"));
    assertTrue(tml.contains("second"));
  }

  @Test
  public void testDefaultCalendarDeserialization() throws Exception {
    String tml = "[[year|2013][month|3][dayOfMonth|6][hourOfDay|15][minute|23][second|1]]";
    Calendar cal = juple.fromTML(tml, Calendar.class);
    assertEquals(2013, cal.get(Calendar.YEAR));
    assertEquals(3, cal.get(Calendar.MONTH));
    assertEquals(6, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(23, cal.get(Calendar.MINUTE));
    assertEquals(1, cal.get(Calendar.SECOND));
  }

  @Test
  public void testDefaultGregorianCalendarSerialization() throws Exception {
    GregorianCalendar cal = new GregorianCalendar();
    String tml = juple.toTML(cal);
    assertTrue(tml.contains("year"));
    assertTrue(tml.contains("month"));
    assertTrue(tml.contains("dayOfMonth"));
    assertTrue(tml.contains("hourOfDay"));
    assertTrue(tml.contains("minute"));
    assertTrue(tml.contains("second"));
  }

  @Test
  public void testDefaultGregorianCalendarDeserialization() throws Exception {
    String tml = "[[year|2013][month|3][dayOfMonth|6][hourOfDay|15][minute|23][second|1]]";
    GregorianCalendar cal = juple.fromTML(tml, GregorianCalendar.class);
    assertEquals(2013, cal.get(Calendar.YEAR));
    assertEquals(3, cal.get(Calendar.MONTH));
    assertEquals(6, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(23, cal.get(Calendar.MINUTE));
    assertEquals(1, cal.get(Calendar.SECOND));
  }

  // ==========================================================================
  // = Properties.class
  // ==========================================================================

  @Test
  public void testPropertiesSerialization() {
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    props.setProperty("The meaning of life", "42");
    assertEquals("[[The meaning of life|42][foo|bar]]", juple.toTML(props));
  }

  @Test
  public void testPropertiesDeserialization() {
    String tml = "[[The meaning of life|42][foo|bar]]";
    Properties props = juple.fromTML(tml, Properties.class);
    assertEquals("bar", props.getProperty("foo"));
    assertEquals("42", props.getProperty("The meaning of life"));
  }

  // ==========================================================================
  // = TreeSet.class
  // ==========================================================================

  @Test
  public void testTreeSetSerialization() {
    TreeSet<String> treeSet = new TreeSet<String>();
    Type type = new TMLTypeToken<TreeSet<String>>() {}.getType();
    treeSet.add("Value1");
    assertEquals("[[Value1]]", juple.toTML(treeSet, type));
  }

  @Test
  public void testTreeSetDeserialization() {
    Type type = new TMLTypeToken<TreeSet<String>>() {}.getType();
    TreeSet<String> treeSet = juple.fromTML("[[Value1]]", type);
    assertTrue(treeSet.contains("Value1"));
  }

  // ==========================================================================
  // = StringBuilder.class
  // ==========================================================================

  @Test
  public void testStringBuilderSerialization() {
    StringBuilder sb = new StringBuilder("abc def");
    assertEquals("[abc def]", juple.toTML(sb));
  }

  @Test
  public void testStringBuilderDeserialization() {
    StringBuilder sb = juple.fromTML("[abc def]", StringBuilder.class);
    assertEquals("abc def", sb.toString());
  }

  // ==========================================================================
  // = StringBuffer.class
  // ==========================================================================

  @Test
  public void testStringBufferSerialization() {
    StringBuffer sb = new StringBuffer("abc def");
    assertEquals("[abc def]", juple.toTML(sb));
  }

  @Test
  public void testStringBufferDeserialization() {
    StringBuffer sb = juple.fromTML("[abc def]", StringBuffer.class);
    assertEquals("abc def", sb.toString());
  }

  // ==========================================================================
  // = Nulls
  // ==========================================================================

  @Test
  public void testNullSerialization() throws Exception {
    testNullSerializationAndDeserialization(Boolean.class);
    testNullSerializationAndDeserialization(Byte.class);
    testNullSerializationAndDeserialization(Short.class);
    testNullSerializationAndDeserialization(Integer.class);
    testNullSerializationAndDeserialization(Long.class);
    testNullSerializationAndDeserialization(Double.class);
    testNullSerializationAndDeserialization(Float.class);
    testNullSerializationAndDeserialization(Number.class);
    testNullSerializationAndDeserialization(Character.class);
    testNullSerializationAndDeserialization(String.class);
    testNullSerializationAndDeserialization(StringBuilder.class);
    testNullSerializationAndDeserialization(StringBuffer.class);
    testNullSerializationAndDeserialization(BigDecimal.class);
    testNullSerializationAndDeserialization(BigInteger.class);
    testNullSerializationAndDeserialization(URL.class);
    testNullSerializationAndDeserialization(URI.class);
    testNullSerializationAndDeserialization(UUID.class);
    testNullSerializationAndDeserialization(Locale.class);
    testNullSerializationAndDeserialization(InetAddress.class);
    testNullSerializationAndDeserialization(BitSet.class);
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
    testNullSerializationAndDeserialization(java.sql.Date.class);
    testNullSerializationAndDeserialization(Enum.class);
    testNullSerializationAndDeserialization(Class.class);
    testNullSerializationAndDeserialization(GregorianCalendar.class);
    testNullSerializationAndDeserialization(Calendar.class);
  }

  @Test
  public void testNullArraySerialization() throws Exception {
    testNullArraySerializationAndDeserialization(TreeSet.class);
    testNullArraySerializationAndDeserialization(ArrayList.class);
    testNullArraySerializationAndDeserialization(HashSet.class);
    // map
    testNullArraySerializationAndDeserialization(Properties.class);
  }

  private void testNullSerializationAndDeserialization(Class<?> c) {
    assertEquals("[\\0]", juple.toTML(null, c));
    assertEquals(null, juple.fromTML("[\\0]", c));
  }

  private void testNullArraySerializationAndDeserialization(Class<?> c) {
    assertEquals("[\\2]", juple.toTML(null, c));
    assertEquals(null, juple.fromTML("[\\2]", c));
  }

  static class NumberAsStringAdapter extends TMLTypeAdapter<Number> {
    private final Constructor<? extends Number> constructor;

    NumberAsStringAdapter(Class<? extends Number> type) throws Exception {
      this.constructor = type.getConstructor(String.class);
    }

    @Override
    public void write(TMLWriter out, Number value) throws IOException {
      out.value(value.toString());
    }

    @Override
    public Number read(TMLReader in) throws IOException {
      try {
        return constructor.newInstance(in.nextString());
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    }
  }
}
