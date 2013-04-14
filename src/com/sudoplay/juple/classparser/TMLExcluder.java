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

package com.sudoplay.juple.classparser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapterFactory;
import com.sudoplay.juple.classparser.annotations.Expose;
import com.sudoplay.juple.classparser.annotations.Since;
import com.sudoplay.juple.classparser.annotations.Until;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * This class selects which fields and types to omit. It is configurable,
 * supporting version attributes {@link Since} and {@link Until}, modifiers,
 * synthetic fields, anonymous and local classes, inner classes, and fields with
 * the {@link Expose} annotation.
 * 
 * <p>
 * This class is a type adapter factory; types that are excluded will be adapted
 * to null. It may delegate to another type adapter if only one direction is
 * excluded.
 * 
 * @author Joel Leitch (original for gson)
 * @author Jesse Wilson (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public final class TMLExcluder implements TMLTypeAdapterFactory, Cloneable {
  private static final double IGNORE_VERSIONS = -1.0d;
  public static final TMLExcluder DEFAULT = new TMLExcluder();

  private double version = IGNORE_VERSIONS;
  private int modifiers = Modifier.TRANSIENT | Modifier.STATIC;
  private boolean serializeInnerClasses = true;
  private boolean requireExpose;

  @Override
  public TMLExcluder clone() {
    try {
      return (TMLExcluder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  public TMLExcluder withVersion(double ignoreVersionsAfter) {
    TMLExcluder result = clone();
    result.version = ignoreVersionsAfter;
    return result;
  }

  public TMLExcluder withModifiers(int... modifiers) {
    TMLExcluder result = clone();
    result.modifiers = 0;
    for (int modifier : modifiers) {
      result.modifiers |= modifier;
    }
    return result;
  }

  public TMLExcluder disableInnerClassSerialization() {
    TMLExcluder result = clone();
    result.serializeInnerClasses = false;
    return result;
  }

  public TMLExcluder excludeFieldsWithoutExposeAnnotation() {
    TMLExcluder result = clone();
    result.requireExpose = true;
    return result;
  }

  public <T> TMLTypeAdapter<T> create(final Juple parser,
      final TMLTypeToken<T> type) {
    Class<?> rawType = type.getRawType();
    final boolean skipSerialize = excludeClass(rawType, true);
    final boolean skipDeserialize = excludeClass(rawType, false);

    if (!skipSerialize && !skipDeserialize) {
      return null;
    }

    return new TMLTypeAdapter<T>() {
      /**
       * The delegate is lazily created because it may not be needed, and
       * creating it may fail.
       */
      private TMLTypeAdapter<T> delegate;

      @Override
      public T read(TMLReader in) throws IOException {
        if (skipDeserialize) {
          int scope = in.getScope();
          while (in.hasNextInScope(scope)) {
            in.skipNext();
          }
          return null;
        }
        return delegate().read(in);
      }

      @Override
      public void write(TMLWriter out, T value) throws IOException {
        if (skipSerialize) {
          out.nullValue();
          return;
        }
        delegate().write(out, value);
      }

      private TMLTypeAdapter<T> delegate() {
        TMLTypeAdapter<T> d = delegate;
        return d != null ? d : (delegate = parser.getDelegateAdapter(
            TMLExcluder.this, type));
      }
    };
  }

  public boolean excludeField(Field field, boolean serialize) {
    if ((modifiers & field.getModifiers()) != 0) {
      return true;
    }

    if (version != TMLExcluder.IGNORE_VERSIONS
        && !isValidVersion(field.getAnnotation(Since.class),
            field.getAnnotation(Until.class))) {
      return true;
    }

    if (field.isSynthetic()) {
      return true;
    }

    if (requireExpose) {
      Expose annotation = field.getAnnotation(Expose.class);
      if (annotation == null
          || (serialize ? !annotation.serialize() : !annotation.deserialize())) {
        return true;
      }
    }

    if (!serializeInnerClasses && isInnerClass(field.getType())) {
      return true;
    }

    if (isAnonymousOrLocal(field.getType())) {
      return true;
    }

    return false;
  }

  public boolean excludeClass(Class<?> clazz, boolean serialize) {
    if (version != TMLExcluder.IGNORE_VERSIONS
        && !isValidVersion(clazz.getAnnotation(Since.class),
            clazz.getAnnotation(Until.class))) {
      return true;
    }

    if (!serializeInnerClasses && isInnerClass(clazz)) {
      return true;
    }

    if (isAnonymousOrLocal(clazz)) {
      return true;
    }

    return false;
  }

  private boolean isAnonymousOrLocal(Class<?> clazz) {
    return !Enum.class.isAssignableFrom(clazz)
        && (clazz.isAnonymousClass() || clazz.isLocalClass());
  }

  private boolean isInnerClass(Class<?> clazz) {
    return clazz.isMemberClass() && !isStatic(clazz);
  }

  private boolean isStatic(Class<?> clazz) {
    return (clazz.getModifiers() & Modifier.STATIC) != 0;
  }

  private boolean isValidVersion(Since since, Until until) {
    return isValidSince(since) && isValidUntil(until);
  }

  private boolean isValidSince(Since annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion > version) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidUntil(Until annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion <= version) {
        return false;
      }
    }
    return true;
  }
}
