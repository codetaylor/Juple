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

import java.lang.reflect.Type;

/**
 * This interface is implemented to create instances of a class that does not
 * define a no-args constructor. If you can modify the class, you should instead
 * add a private, or public no-args constructor. However, that is not possible
 * for library classes, such as JDK classes, or a third-party library that you
 * do not have source-code of. In such cases, you should define an instance
 * creator for the class.
 * <p>
 * Let us look at an example where defining an InstanceCreator might be useful.
 * The {@code Id} class defined below does not have a default no-args
 * constructor.
 * </p>
 * 
 * <pre>
 * public class Id&lt;T&gt; {
 *   private final Class&lt;T&gt; clazz;
 *   private final long value;
 * 
 *   public Id(Class&lt;T&gt; clazz, long value) {
 *     this.clazz = clazz;
 *     this.value = value;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * If Juple encounters an object of type {@code Id} during deserialization, it
 * will throw an exception. The easiest way to solve this problem will be to add
 * a (public or private) no-args constructor as follows:
 * </p>
 * 
 * <pre>
 * private Id() {
 *   this(Object.class, 0L);
 * }
 * </pre>
 * 
 * <p>
 * However, let us assume that the developer does not have access to the
 * source-code of the {@code Id} class, or does not want to define a no-args
 * constructor for it. The developer can solve this problem by defining an
 * {@code InstanceCreator} for {@code Id}:
 * </p>
 * 
 * <pre>
 * class IdInstanceCreator implements InstanceCreator&lt;Id&gt; {
 *   public Id createInstance(Type type) {
 *     return new Id(Object.class, 0L);
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Note that it does not matter what the fields of the created instance contain
 * since Juple will overwrite them with the deserialized values specified in
 * TML. You should also ensure that a <i>new</i> object is returned, not a
 * common object since its fields will be overwritten. The developer will need
 * to register {@code IdInstanceCreator} with TML as follows:
 * </p>
 * 
 * <pre>
 * TMLProperties properties = new TMLProperties().registerInstanceCreator(
 *     Id.class, new IdInstanceCreator()).create();
 * TMLClassParser parser = new TMLClassParser(properties);
 * </pre>
 * 
 * This class has been derived from the google-gson source <a
 * href="https://code.google.com/p/google-gson/"
 * >https://code.google.com/p/google-gson/</a>.
 * 
 * <p>
 * This class contains trivial modifications.
 * 
 * @param <T>
 *          the type of object that will be created by this implementation
 * 
 * @author Inderjeet Singh (original for gson)
 * @author Joel Leitch (original for gson)
 * @author Jason Taylor (modified for Juple)
 */
public interface TMLInstanceCreator<T> {

  public T createInstance(Type type);

}
