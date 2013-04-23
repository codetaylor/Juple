##Juple Specific TML Syntax
Juple's interpretation of TML remains pretty close to the [official TML specification](https://github.com/judnich/TupleMarkup) written by John Judnich. 

There are, however, some subtle differences in the way Juple interprets TML to serialize and deserialize Java objects.

###Basic Tuples
Tuples are still lists represented with an opening `[` and closing `]` delimiter:

    [1 2 3 4]

In the context of a Juple <-> Java object, the above represents a primitive array.

Strings are represented as a list of characters separated by a space:

    [Hello world!]

Arrays or collections of strings are encapsulated within list delimiters like so:

    [
        [Hello world!]
        [...yet again.]
    ]

###Nesting Delimiter

When serializing and deserializing, Juple uses the nesting delimiter `|` to denote a separation between either a class field and field value, or a map key and value.

######A class field string

    [
        [field | A single string.]
    ]

######A class field collection of strings

    [
        [field |
            [Element one.]
            [Element two.]
            [Element three.]
            ...
        ]
    ]

######A class field object

    [
        [field |
            [fieldA | 45.9]
            [fieldB | Element]
            [fieldC | true]
            ...
        ]
    ]

######A class field collection of objects

    [
        [field |
            [
                [fieldA | 45.9]
                [fieldB | Element]
                [fieldC | true]
            ]
            [
                [fieldA | 19.3]
                [fieldB | Property]
                [fieldC | false]
            ]
            ...
        ]
    ]

######A map

    [
        [1 | a]
        [2 | b]
    ]


**NOTE:** While Juple strays somewhat from the official specification in how it treats the nesting delimiter, the *TMLNodeTreeParser* class does not.

###Comments
As per the TML specification, any text placed after `||` on a line will be ignored as a comment.

    [
        [name | Jayne Cobb] || This will be ignored as a comment.
        [occupation | Thug]
    ]

Header text is a deviation from the official TML specification. Any text placed before the initial opening delimiter, `[`, will also be ignored.

    Any text placed here will be ignored.
    This is a good place to put lengthy information about the data, if such verbosity is required.
    [
        [name | Hoban Washburne]
        [occupation | Pilot]
    ]

###Escape Codes

All of the escape codes defined in the TML specification are supported:

    \n \r \t \s \\ \[ \] \| \? \*

Juple also uses the following escape codes:

    \0 \1 \2

These respectively evaluate to `null object`, `empty string`, and `null list`. The `null object` represents a single null object. The `null list` can represent an array, collection, or map with a null pointer.

For example: `[[ array | \0 ]]` represents a field with a null value, if the field is an object. If the field is a collection or array, it represents a collection or array with a single null value in the list.

`[[ array | [\1] [\1] [\1] ]]` represents a field with an array or collection containing three empty strings, `""`.

`[[ array | \2 ]]` represents field containing a null array, collection, or map.

##License

Copyright (C) 2013 Jason Taylor. Released as open-source under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
