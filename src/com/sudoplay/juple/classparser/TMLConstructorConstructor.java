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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sudoplay.juple.error.TMLIOException;

/**
 * Returns a function that can construct an instance of a requested type.
 */
public final class TMLConstructorConstructor {
  private final Map<Type, TMLInstanceCreator<?>> instanceCreators;

  public TMLConstructorConstructor(
      Map<Type, TMLInstanceCreator<?>> instanceCreators) {
    this.instanceCreators = instanceCreators;
  }

  public TMLConstructorConstructor() {
    this(Collections.<Type, TMLInstanceCreator<?>> emptyMap());
  }

  public <T> TMLObjectConstructor<T> get(TMLTypeToken<T> typeToken) {
    final Type type = typeToken.getType();
    final Class<? super T> rawType = typeToken.getRawType();

    // first try an instance creator

    @SuppressWarnings("unchecked")
    // types must agree
    final TMLInstanceCreator<T> creator = (TMLInstanceCreator<T>) instanceCreators
        .get(type);
    if (creator != null) {
      return new TMLObjectConstructor<T>() {
        public T construct() {
          return creator.createInstance(type);
        }
      };
    }

    // Next try raw type match for instance creators
    @SuppressWarnings("unchecked")
    // types must agree
    final TMLInstanceCreator<T> rawTypeCreator = (TMLInstanceCreator<T>) instanceCreators
        .get(rawType);
    if (rawTypeCreator != null) {
      return new TMLObjectConstructor<T>() {
        public T construct() {
          return rawTypeCreator.createInstance(type);
        }
      };
    }

    TMLObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
    if (defaultConstructor != null) {
      return defaultConstructor;
    }

    TMLObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(
        type, rawType);
    if (defaultImplementation != null) {
      return defaultImplementation;
    }

    // finally try unsafe
    return newUnsafeAllocator(type, rawType);
  }

  private <T> TMLObjectConstructor<T> newDefaultConstructor(
      Class<? super T> rawType) {
    try {
      final Constructor<? super T> constructor = rawType
          .getDeclaredConstructor();
      if (!constructor.isAccessible()) {
        constructor.setAccessible(true);
      }
      return new TMLObjectConstructor<T>() {
        @SuppressWarnings("unchecked")
        // T is the same raw type as is requested
        public T construct() {
          try {
            Object[] args = null;
            return (T) constructor.newInstance(args);
          } catch (InstantiationException e) {
            // TODO: JsonParseException ?
            throw new RuntimeException("Failed to invoke " + constructor
                + " with no args", e);
          } catch (InvocationTargetException e) {
            // TODO: don't wrap if cause is unchecked!
            // TODO: JsonParseException ?
            throw new RuntimeException("Failed to invoke " + constructor
                + " with no args", e.getTargetException());
          } catch (IllegalAccessException e) {
            throw new AssertionError(e);
          }
        }
      };
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Constructors for common interface types like Map and List and their
   * subytpes.
   */
  @SuppressWarnings("unchecked")
  // use runtime checks to guarantee that 'T' is what it is
  private <T> TMLObjectConstructor<T> newDefaultImplementationConstructor(
      final Type type, Class<? super T> rawType) {
    if (Collection.class.isAssignableFrom(rawType)) {
      if (SortedSet.class.isAssignableFrom(rawType)) {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new TreeSet<Object>();
          }
        };
      } else if (EnumSet.class.isAssignableFrom(rawType)) {
        return new TMLObjectConstructor<T>() {
          @SuppressWarnings("rawtypes")
          public T construct() {
            if (type instanceof ParameterizedType) {
              Type elementType = ((ParameterizedType) type)
                  .getActualTypeArguments()[0];
              if (elementType instanceof Class) {
                return (T) EnumSet.noneOf((Class) elementType);
              } else {
                throw new TMLIOException("Invalid EnumSet type: "
                    + type.toString());
              }
            } else {
              throw new TMLIOException("Invalid EnumSet type: "
                  + type.toString());
            }
          }
        };
      } else if (Set.class.isAssignableFrom(rawType)) {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new LinkedHashSet<Object>();
          }
        };
      } else if (Queue.class.isAssignableFrom(rawType)) {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new LinkedList<Object>();
          }
        };
      } else {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new ArrayList<Object>();
          }
        };
      }
    }

    if (Map.class.isAssignableFrom(rawType)) {
      if (SortedMap.class.isAssignableFrom(rawType)) {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new TreeMap<Object, Object>();
          }
        };
      } else if (type instanceof ParameterizedType
          && !(String.class.isAssignableFrom(TMLTypeToken.get(
              ((ParameterizedType) type).getActualTypeArguments()[0])
              .getRawType()))) {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new LinkedHashMap<Object, Object>();
          }
        };
      } else {
        return new TMLObjectConstructor<T>() {
          public T construct() {
            return (T) new TMLLinkedHashTreeMap<String, Object>();
          }
        };
      }
    }

    return null;
  }

  private <T> TMLObjectConstructor<T> newUnsafeAllocator(final Type type,
      final Class<? super T> rawType) {
    return new TMLObjectConstructor<T>() {
      private final TMLUnsafeAllocator unsafeAllocator = TMLUnsafeAllocator
          .create();

      @SuppressWarnings("unchecked")
      public T construct() {
        try {
          Object newInstance = unsafeAllocator.newInstance(rawType);
          return (T) newInstance;
        } catch (Exception e) {
          throw new RuntimeException(
              ("Unable to invoke no-args constructor for " + type + ". " + "Registering a TMLInstanceCreator for this type may fix this problem."),
              e);
        }
      }
    };
  }

  @Override
  public String toString() {
    return instanceCreators.toString();
  }
}
