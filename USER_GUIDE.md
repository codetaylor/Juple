#Juple User Guide
Juple is a Java library that can be used to convert Java objects to and from a Tuple Markup Language (TML) representation.

##Disclaimer
Juple uses code adapted from [Google's Gson](https://code.google.com/p/google-gson/) and the content in this guide was derived from the [Gson User Guide](https://sites.google.com/site/gson/gson-user-guide). The [Tuple Markup Language](https://github.com/judnich/TupleMarkup) specification was originally written by John Judnich. Juple is in no way endorsed by Google, the authors of Gson and the Gson User Guide, or John Judnich.

Please refer to the LICENSE file.

##Goals for Juple
* Allow parsing TML into a pattern searchable node-tree
* Provide easy to use methods to convert Java to TML and vice-versa
* Allow pre-existing unmodifiable objects to be converted to and from TML
* Allow custom representations for objects
* Support arbitrarily complex object
* Generate both compact and human-readable TML output
* Adhere to the original TML specification as closely as possible

##Using Juple
The main class to use is Juple which can be created by simply calling ```new Juple()```. Juple also provides a ```JupleBuilder``` class that can be used to create a Juple instance with user provided settings.

The Juple instance does not maintain any state while invoking TML operations, so you are free to reuse the same object for multiple serialization and deserialization operations, even across multiple threads.

###Primitive Examples
######Serialization
```java
Juple juple = new Juple();
juple.toTML(1);            //prints [1]
juple.toTML("abcd");       //prints [abcd]
juple.toTML(new Long(10)); //prints [10]
int[] values = { 1, 2, 3 };
juple.toTML(values);       //prints [1 2 3]
String[] strValues = { "Hello world!", "...again." };
juple.toTML(strValues);    //prints [[Hello world!][...again]]
```
######Deserialization
```java
int one = juple.fromTML("[1]", int.class);
Integer one = juple.fromTML("[1]", Integer.class);
Long one = juple.fromTML("[1]", Long.class);
Boolean false = juple.fromTML("[false]", Boolean.class);
String str = juple.fromTML("[abc]", String.class);
```
###Object Examples
######Object
```java
class BagOfPrimitives {
    private int value1 = 1;
    private String value2 = "abc";
    private transient int value3 = 3;
    BagOfPrimitives() {
        // no-args constructor
    }
}
```
######Serialization
```java
BagOfPrimitives obj = new BagOfPrimitives();
Juple juple = new Juple();
String tml = juple.toTML(obj);
// tml is [[value1|1][value2|abc]]
```
**NOTE:** Serializing objects with a circular reference will result in infinite recursion and produce an exception.

######Deserialization
```java
BagOfPrimitives obj2 = juple.fromTML(tml, BagOfPrimitives.class);
// obj2 is just like obj1
```

######Notes on Objects
* It is perfectly fine (and recommended) to use private fields
* There is no need to use any annotations to indicate a field is to be included for serialization and deserialization. All fields in the current class, and from all super classes, are included by default.
* If a field is marked transient, by default it is ignored and not included in the TML serialization or deserialization.
* Juple will handle nulls:
 * During serialization, a null field will be included in the output either as a null object ```\0``` or a null list ```\2```.
 * During deserialization, a null or missing entry in TML results in setting the corresponding field in the object to null.
* If a field is *synthetic*, it is ignored and neither serialized nor deserialized.
* Fields corresponding to the outer classes in inner classes, anonymous classes, and local classes are ignored and not included in serialization or deserialization.

###Nested Classes (including Inner Classes)
Juple can serialize static nested classes quite easily.

Juple can also deserialize static nested classes. However, Juple cannot automatically deserialize the **pure inner classes since their no-args constructor also need a reference to the containing Object**, which is not available at the time of deserialization. You can address this problem by either making the inner class static or by providing a custom TMLInstanceCreator for it. Here is an example:
```java
public class A { 
  public String a; 

  class B { 

    public String b; 

    public B() {
      // No args constructor for B
    }
  } 
}
```
**NOTE:** The above class B can not (by default) be serialized with Juple.

Juple can not deserialize `[[b|abc]]` into an instance of B since the class B is an inner class. If it was defined as `static class B` then Juple would have been able to deserialize the string. Another solution is to write a custom instance creator for B.
```java
public class InstanceCreatorForB implements TMLInstanceCreator<A.B> {
  private final A a;
  public InstanceCreatorForB(A a)  {
    this.a = a;
  }
  public A.B createInstance(Type type) {
    return a.new B();
  }
}
```
The above is possible, but not recommended.

###Array Examples
```java
Juple juple = new Juple();
int[] ints = {1, 2, 3, 4, 5};
String[] strings = {"abc", "def", "ghi"};
```
######Serialization
```java
juple.toTML(ints);    // prints [1 2 3 4 5]
juple.toTML(strings); // prints [[abc][def][ghi]]
```
######Deserialization
```java
int[] ints2 = juple.fromTML([1 2 3 4 5], int[].class);
// ints2 will be the same as ints
```
Juple also supports multi-dimensional arrays with arbitrarily complex element types.

###Collections Examples
```java
Juple juple = new Juple();
Collection<Integer> ints = Lists.immutableList(1,2,3,4,5);
```
######Serialization
```java
String tml = juple.toTML(ints); // prints [1 2 3 4 5]
```
######Deserialization
```java
Type collectionType = new TMLTypeToken<Collection<Integer>>(){}.getType();
Collection<Integer> ints2 = juple.fromTML(tml, collectionType);
// ints2 will be the same as ints
```
**NOTE:** Take note of how the collection type is defined through the use of the TMLTypeToken class.

######Collections Limitations
* Juple can serialize a collection of arbitrary objects, but cannot deserialize from it because there is no way for the user to indicate the type of the resulting object.
* When deserializing, `Collection` must be of a specific generic type.

###Serializing and Deserializing Generic Types
When you call `toTML(Object obj)`, Juple calls `obj.getClass()` to get information on the fields to serialize. Similarly, you can typically pass `MyClass.class` object in either the `fromTML(String tml, Class<T> classOfT)` or the `fromTML(Reader reader, Class<T> classOfT)` method. This works if the object is a non-generic type. However, if the object is of a generic type, then the generic type information is lost because of Java Type Erasure. Here is an example illustrating the point:
```java
class Foo<T> {
  T value;
}
Juple juple = new Juple();
Foo<Bar> foo = new Foo<Bar>();

// May not serialize foo.value correctly
juple.toTML(foo);

// Fails to deserialize foo.value as Bar
juple.fromTML(tml, foo.getClass());
```
The above code fails to interpret `value` as type `Bar` because Juple invokes `list.getClass()` to get its class information and this method returns a raw class, `Foo.class`. This means that Juple has no way of knowing that this is an object of type `Foo<Bar>`, and not just plain `Foo`.

You can solve this problem by specifying the correct parameterized type for your generic type. This is done by using the **TMLTypeToken** class.
```java
Type fooType = new TMLTypeToken<Foo<Bar>>() {}.getType();
juple.toTML(foo, fooType);

juple.fromJson(tml, fooType);
```
The idiom used to get `fooType` actually defines an anonymous local inner class containing a method `getType()` that returns the fully parameterized type.

###Serializing and Deserializing Collection with Objects of Arbitrary Types
Sometimes you might encounter TML that contains mixed types. For example:
```
[[hello] 5 [[name|GREETINGS][source|guest]]]
```
The equivalent Collection containing this is:
```java
Collection collection = new ArrayList();
collection.add("hello");
collection.add(5);
collection.add(new Event("GREETINGS", "guest"));
```
Where the Event class is defined as:
```java
class Event {
  private String name;
  private String source;
  private Event(String name, String source) {
    this.name = name;
    this.source = source;
  }
}
```
You can serialize the collection with Juple without doing anything specific: `toTML(collection)` would write the desired output.

However, deserialization with `fromTML(tml, Collection.class)` will not work since Juple has no way of knowing how to map the input to the types. Juple requires that you provide a genericised version of collection type in `fromTML`. So, you have three options:

1. Use Juple's `toTMLNode(String string)` to parse the TML into a `TMLNode` tree and then use `fromTML()` on each of the elements. See [the example](https://github.com/codetaylor/Juple/blob/master/examples/com/sudoplay/juple/examples/RawCollectionsExample.java).
2. Register a type adapter for Collection.class that looks at each of the array members and maps them to appropriate objects. The disadvantage of this approach is that it will screw up deserialization of other collection types in Juple.
3. Register a type adapter for `MyCollectionMemberType` and use `fromTML()` with `Collection<MyCollectionMemberType>`.
This approach is only practical if the array appears as a top-level element or if you can change the field type holding the collection to be of type `Collection<MyCollectionMemberType>`.
