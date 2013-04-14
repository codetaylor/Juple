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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapter for Date. Although this class appears stateless, it is not.
 * DateFormat captures its time zone and locale when it is created, which gives
 * this class state. DateFormat isn't thread safe either, so this class has to
 * synchronize its read and write methods.
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (origianl for gson)
 * @author Jason Taylor (modified for Juple)
 */
public final class TMLDateTypeAdapter extends TMLTypeAdapter<Date> {

  private final DateFormat enUsFormat;
  private final DateFormat localFormat;
  private final DateFormat iso8601Format;

  public TMLDateTypeAdapter() {
    this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
        Locale.US), DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
        DateFormat.DEFAULT));
  }

  public TMLDateTypeAdapter(String datePattern) {
    this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(
        datePattern));
  }

  public TMLDateTypeAdapter(int style) {
    this(DateFormat.getDateInstance(style, Locale.US), DateFormat
        .getDateInstance(style));
  }

  public TMLDateTypeAdapter(int dateStyle, int timeStyle) {
    this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
        DateFormat.getDateTimeInstance(dateStyle, timeStyle));
  }

  public TMLDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat) {
    this.enUsFormat = enUsFormat;
    this.localFormat = localFormat;
    this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
        Locale.US);
    this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public Date read(TMLReader in) throws IOException {
    if (in.peek() == TMLToken.NULL) {
      in.nextNull();
      return null;
    }
    Date date = deserializeToDate(getAllStringsInScope(in));
    return date;
  }

  private synchronized Date deserializeToDate(String tml) {
    try {
      return localFormat.parse(tml);
    } catch (ParseException ignored) {}
    try {
      return enUsFormat.parse(tml);
    } catch (ParseException ignored) {}
    try {
      return iso8601Format.parse(tml);
    } catch (ParseException e) {
      throw new TMLSyntaxException(tml, e);
    }
  }

  @Override
  public synchronized void write(TMLWriter out, Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String dateFormatAsString = enUsFormat.format(value);
    out.value(dateFormatAsString);
  }

  @Override
  public boolean isArrayEncapsulate() {
    return true;
  }
}
