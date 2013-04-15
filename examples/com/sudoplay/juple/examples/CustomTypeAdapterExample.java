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

package com.sudoplay.juple.examples;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;
import com.sudoplay.juple.classparser.TMLTypeToken;
import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
import com.sudoplay.juple.error.TMLSyntaxException;
import com.sudoplay.juple.stream.TMLReader;
import com.sudoplay.juple.stream.TMLToken;
import com.sudoplay.juple.stream.TMLWriter;

/**
 * This shows how to use the Juple class.
 * 
 * @author Jason Taylor
 */
public class CustomTypeAdapterExample {

  public static void main(String[] args) {
    CustomTypeAdapterExample app = new CustomTypeAdapterExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static class Vector3f {
    public float x, y, z;

    private Vector3f() {}

    public Vector3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  public static class Monster {
    public String name;
    public String type;
    public int hitPoints;
    public boolean flying;
    public Vector3f location;
  }

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

  public void run() throws IOException {

    Monster monster = new Monster();
    monster.name = "Rupert";
    monster.type = "Air Elemental";
    monster.hitPoints = 26;
    monster.flying = true;
    monster.location = new Vector3f(0.9273f, 3.28983f, 9.38474f);

    Juple juple = new JupleBuilder().setPrettyPrinting().create();
    String tml = juple.toTML(monster);

    System.out.println("Default serialization:");
    System.out.println(tml);

    Juple customJuple = new JupleBuilder().setPrettyPrinting()
        .registerTypeAdapter(Vector3f.class, new Vector3fAdapter()).create();
    tml = customJuple.toTML(monster);

    System.out.println("Custom serialization:");
    System.out.println(tml);
    
    System.out.println("Custom deserialization:");
    Monster monster2 = customJuple.fromTML(tml, Monster.class);
    System.out.printf("(x=%f,y=%f,z=%f)\n", monster2.location.x, monster2.location.y, monster2.location.z);
    
    System.out.println("Custom serialization in collection:");
    Type type = new TMLTypeToken<ArrayList<Vector3f>>(){}.getType();
    List<Vector3f> list = new ArrayList<Vector3f>();
    list.add(new Vector3f(1.2f, 3.14f, 42.0001f));
    list.add(new Vector3f(1.2f, 3.14f, 42.0001f));
    list.add(new Vector3f(1.2f, 3.14f, 42.0001f));
    tml = customJuple.toTML(list, type);
    System.out.println(tml);
    
    System.out.println("Custom deserialization from collection:");
    List<Vector3f> list2 = customJuple.fromTML(tml, type);
    Vector3f v = list2.get(1);
    System.out.printf("(x=%f,y=%f,z=%f)\n", v.x, v.y, v.z);
  }

}
