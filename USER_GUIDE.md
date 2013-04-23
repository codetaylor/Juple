#Juple User Guide
Juple is a Java library that can be used to convert Java objects to and from a Tuple Markup Language (TML) representation.

##Contents
- [Disclaimer](#disclaimer)
- [Goals for Juple](#goals-for-juple)
- [Using Juple](#using-juple)
    - [Primitive Examples](#primitive-examples)
	- [Object Examples](#object-examples)
		- [Notes on Objects](#notes-on-objects)
	- [Nested Classes (including Inner Classes)](#nested-classes-including-inner-classes)
	- [Array Examples](#array-examples)
	- [Collections Examples](#collections-examples)
		- [Collections Limitations](#collections-limitations)
	- [Serializing and Deserializing Generic Types](#serializing-and-deserializing-generic-types)
	- [Serializing and Deserializing Collection with Objects of Arbitrary Types](#serializing-and-deserializing-collection-with-objects-of-arbitrary-types)
    - [Custom Type Adapters](#custom-type-adapters)
        - [Writing a Type Adapter](#writing-a-type-adapter)
        - [Controlling Encapsulation Behavior](#controlling-encapsulation-behavior)
        - [Registering a Single Type Adapter for Multiple Generic Types](#registering-a-single-type-adapter-for-multiple-generic-types)
	- [Writing an Instance Creator](#writing-an-instance-creator)
		- [Instance Creator Example](#instance-creator-example)
		- [Instance Creator for a Parameterized Type](#instance-creator-for-a-parameterized-type)
    - [Compact Vs. Pretty Printing](#compact-vs-pretty-printing)
- [License](#license)

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
####Serialization
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
####Deserialization
```java
int one = juple.fromTML("[1]", int.class);
Integer one = juple.fromTML("[1]", Integer.class);
Long one = juple.fromTML("[1]", Long.class);
Boolean false = juple.fromTML("[false]", Boolean.class);
String str = juple.fromTML("[abc]", String.class);
```
###Object Examples
####Object
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
####Serialization
```java
BagOfPrimitives obj = new BagOfPrimitives();
Juple juple = new Juple();
String tml = juple.toTML(obj);
// tml is [[value1|1][value2|abc]]
```
**NOTE:** Serializing objects with a circular reference will result in infinite recursion and produce an exception.

####Deserialization
```java
BagOfPrimitives obj2 = juple.fromTML(tml, BagOfPrimitives.class);
// obj2 is just like obj1
```

####Notes on Objects
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
####Serialization
```java
juple.toTML(ints);    // prints [1 2 3 4 5]
juple.toTML(strings); // prints [[abc][def][ghi]]
```
####Deserialization
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
####Serialization
```java
String tml = juple.toTML(ints); // prints [1 2 3 4 5]
```
####Deserialization
```java
Type collectionType = new TMLTypeToken<Collection<Integer>>(){}.getType();
Collection<Integer> ints2 = juple.fromTML(tml, collectionType);
// ints2 will be the same as ints
```
**NOTE:** Take note of how the collection type is defined through the use of the TMLTypeToken class.

####Collections Limitations
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

###Custom Type Adapters
Sometimes default representation is not what you want. Juple allows you to register your own type adapters:
```java
JupleBuilder builder = new JupleBuilder();
builder.registerTypeAdapter(MyType.class, new MyTypeAdapter());
```
####Writing a Type Adapter
Let's use a typical three point vector as an example class:
```java
public class Vector3f {
    public float x, y, z;

    @SuppressWarnings("unused")
    private Vector3f() {}
    
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
```
The default output of this object in TML looks like this:
```
[
    [x | 1.2]
    [y | 3.14]
    [z | 42.0001]
]
```
This is rather excessive, however, because we know that a `Vector3f` object will always contain just three floats. Here is an example of how to write a custom type adapter for the `Vector3f` class:
```java
public static class Vector3fAdapter extends TMLTypeAdapter<Vector3f> {
    @Override
    public Vector3f read(TMLReader in) throws IOException {
        if (in.peek() == TMLToken.NULL) {
            in.nextNull();
            return null;
        }
        try {
            Vector3f v = new Vector3f();
            v.x = Float.parseFloat(in.nextString());
            v.y = Float.parseFloat(in.nextString());
            v.z = Float.parseFloat(in.nextString());
            return v;
        } catch (NumberFormatException e) {
            throw new TMLSyntaxException(e);
        }
    }

    @Override
    public void write(TMLWriter out, Vector3f value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.x);
        out.value(value.y);
        out.value(value.z);
    }
}
```
The `Vector3f` class will now serialize to and deserialize from TML that looks like this:
```
[1.2 3.14 42.0001]
```
Arrays or collections of `Vector3f` objects will now look like this:
```
[1.2 3.14 42.0001 1.2 3.14 42.0001 1.2 3.14 42.0001 1.2 3.14 42.0001 1.2 3.14 42.0001]
```
####Controlling Encapsulation Behavior
The `TMLTypeAdapter` class has three methods that can be overridden to better control open and close delimiter encapsulation behavior:

**isRootEncapsulate()** - default `true`
```java
public boolean isRootEncapsulate() {
    return true;
}
```
If this method returns `true`, Juple will force encapsulation at the root level. If it returns `false`, encapsulation at the root level will not be enforced and will be left up to the type adapter. For example, the custom `Vector3f` type adapter above defaults to `true`, producing `[1.2 3.14 42.0001]`. If we override `isRootEncapsulate()` to return `false`, the TML would look like this `1.2 3.14 42.0001` and fail because the type adapter does not perform any encapsulation.

**isFieldEncapsulate()** - default `false`
```java
public boolean isFieldEncapsulate() {
    return false;
}
```
If this method returns `true`, Juple will force encapsulation when the object is the value of a class field:
```
[
    [field | [1.2 3.14 42.0001]]
]
```
If it instead returns its default value of `false`:
```
[
    [field | 1.2 3.14 42.0001]
]
```
**isArrayEncapsulate()** - default `false`
```java
public boolean isArrayEncapsulate() {
    return false;
}
```
Sometimes you might want to encapsulate values inside an array or collection. If `isArrayEncapsulate()` returns `true` and the type adapter's type appears in an array or collection, Juple will force encapsulation. For example, the string type adapter overrides and returns true to produce serialized array or collection of strings that looks like:
```
[
    [Hello world!]
    [This is a string.]
]
```
If the string type adapter did not override this behavior, the array or collection of strings would look like:
```
[Hello world! This is a string]
```
... and Juple would not know where the strings were separated.

####Registering a Single Type Adapter for Multiple Generic Types
Often you want to register a single handler for all generic types corresponding to a raw type. For example, suppose you have an `Id` class for Id representation/translation (i.e. an internal vs. external representation). The `Id<T>` type would have the same serialization for all generic types so the type adapter would just write the value in its `write()` method. Deserialization in the adapter's `read()` method would be a little different. You would need to call `new Id(Class<T>, String)` which needs to return an instance of `Id<T>`.

Juple supports registering a single handler for this. You can also register a specific handler for a specific generic type (say `Id<RequiresSpecialHandling>` needed special handling). The `Type` parameter for `toTML()` and `fromTML()` contains the generic type information to help you write a single handler for all generic types corresponding to the same raw type.

###Writing an Instance Creator
While deserializing an object, Juple needs to create a default instance of the class. Well-behaved classes that are meant for serialization and deserialization should have either a **public or private** no-argument constructor.

Typically, instance creators are needed when you are dealing with a library class that **does not** define a no-argument constructor.

**NOTE:** A custom instance creator is not needed if a custom type adapter is registered for the type, as instance creation will typically be handled in the `read()` method of the adapter.

####Instance Creator Example
```java
private class MoneyInstanceCreator implements TMLInstanceCreator<Money> {
    public Money createInstance(Type type) {
        return new Money("1000000", CurrencyCode.USD);
    }
}
```
Type could be of a corresponding generic type. This is useful to invoke constructors which need specific generic type information, for example, if the `Id` class stores the class for which the `Id` is being created.
###Instance Creator for a Parameterized Type
Sometimes that the type that you are trying to instantiate is a parameterized type. Generally, this is not a problem since the actual instance is of raw type. Here is an example:
```java
class MyList<T> extends ArrayList<T> {
}

class MyListInstanceCreator implements TMLInstanceCreator<MyList<?>> {
    @SuppressWarnings("unchecked")
    public MyList<?> createInstance(Type type) {
        // No need to use a parameterized list since the actual instance will have the raw type anyway.
        return new MyList();
    }
}
```
However, sometimes you do need to create an instance based on the actual parameterized type. In this case, you can use the type parameter being passed to the `createInstance` method. Here is an example:
```java
public class Id<T> {
    private final Class<T> classOfId;
    private final long value;
    public Id(Class<T> classOfId, long value) {
        this.classOfId = classOfId;
        this.value = value;
    }
}

class IdInstanceCreator implements TMLInstanceCreator<Id<?>> {
    public Id<?> createInstance(Type type) {
        Type[] typeParameters = ((ParameterizedType)type).getActualTypeArguments();
        Type idType = typeParameters[0]; // Id has only one parameterized type T
        return Id.get((Class)idType, 0L);
    }
}
```
In the above example, an instance of the `Id` class cannot be created without actually passing in the actual type for the parameterized type. We solve this problem by using the passed method parameter, `type`. The `type` object in this case is the Java parameterized type representation of `Id<Foo>` where the actual instance should be bound to `Id<foo>`. Since the `Id` class has just one parameterized type parameter, `T`, we use the zero element of the type array returned by `getActualTypeArgument()`, which will hold `Foo.class` in this case.

###Compact Vs. Pretty Printing

The default TML output that is provided by Juple is a compact TML format: there will not be any unnecessary whitespace in the output. When the pretty print feature is enabled, Juple will output a more human-readable TML format using line breaks and indents.

To enable the pretty print feature, you must configure your 'Juple' instance using the 'JupleBuilder':
```java
Juple juple = new JupleBuilder().setPrettyPrinting().create();
String output = juple.toTML(someObject);
```

##License

Copyright (C) 2013 Jason Taylor. Released as open-source under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
