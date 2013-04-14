## Juple
Juple is a Java library that can be used to convert Java objects into a Tuple Markup Language (TML) representation. It can also be used to convert a TML string to an equivalent Java object. Juple can also convert TML to node trees that are searchable by patterns.

The [Tuple Markup Language](https://github.com/judnich/TupleMarkup) specification was originally written by John Judnich.

Juple uses modified code from [Google's Gson](https://code.google.com/p/google-gson/) which is released as open-source under the [Apache Liscense, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

### Why Juple?
Juple was created to combine the simplicity and elegance of John Judnich's Tuple Markup Language with the versatility and power of Google's Gson.

The initial idea for Juple spawned during development of the PC game, [Lodestar: Stygian Skies](https://lodestargame.com/home). After trying out JSON for defining game entities it was clear that we wanted something simpler than JSON to represent the entity data in text. We wanted something simple and elegant, something that modders could easily edit without worrying about syntax. We also wanted something versatile and powerful on the other end, something we could use to easily load data into arbitrary Java classes. TML was recommended as an alternative to JSON and Juple was born.

### What is in this repository?

* *Juple* parses TML data into a Java object.
* *JupleBuilder* builds a Juple instance with user defined settings.
* *TMLNodeTreeParser* is used to parse TML into a pattern searchable node tree consistent with the original TML spec.
* *TMLReader* is a wrapper for a java.io.Reader that contains methods to assert syntax and read data.
* *TMLWriter* is a wrapper for a java.io.Writer that contains methods to assert syntax and write data.

## Example

It is very simple to create a Java object from its TML representation using Juple.

#####TML

    [
        [ name | Ethereal Filcher ]
        [ life | 22 ]
        [ location |
            [ x | 6.2 ]
            [ y | 5.4 ]
            [ z | -3.14 ]
        ]
        [ ethereal | true ]
    ]

#####Java Objects

    public class Vector3f {
        public float x, y, z;
        
        public Vector3f() {}
    }
    
    public class Monster {
        private String name;
        private int life;
        private Vector3f location;
        private boolean ethereal;
        
        public Monster() {}
    }

#####Java

    TMLClassParser parser = new TMLClassParser();
    Monster grue = parser.fromTML(reader, Monster.class);

For more examples, check out the examples folder.

##Liscense

Copyright (C) 2013 Jason Taylor. Released as open-source under [Apache Liscense, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).