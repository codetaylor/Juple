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
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * Adapter for Time. Although this class appears stateless, it is not.
 * DateFormat captures its time zone and locale when it is created, which gives
 * this class state. DateFormat isn't thread safe either, so this class has to
 * synchronize its read and write methods.
 * 
 * @author unknown (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public final class TMLTimeTypeAdapter extends TMLTypeAdapter<Time> {
  public static final TMLTypeAdapterFactory FACTORY = new TMLTypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    // we use a runtime check to make sure the 'T's equal
    public <T> TMLTypeAdapter<T> create(Juple juple, TMLTypeToken<T> typeToken) {
      return typeToken.getRawType() == Time.class ? (TMLTypeAdapter<T>) new TMLTimeTypeAdapter()
          : null;
    }
  };

  private final DateFormat format;

  public TMLTimeTypeAdapter() {
    this("hh:mm:ss a");
  }

  public TMLTimeTypeAdapter(String timePattern) {
    this.format = new SimpleDateFormat(timePattern);
  }

  @Override
  public synchronized Time read(TMLReader in) throws IOException {
    if (in.peek() == TMLToken.NULL) {
      in.nextNull();
      return null;
    }
    try {
      Date date = format.parse(getAllStringsInScope(in));
      return new Time(date.getTime());
    } catch (ParseException e) {
      throw new TMLSyntaxException(e);
    }
  }

  @Override
  public synchronized void write(TMLWriter out, Time value) throws IOException {
    out.value(value == null ? null : format.format(value));
  }

  @Override
  public boolean isArrayEncapsulate() {
    return true;
  }
}
