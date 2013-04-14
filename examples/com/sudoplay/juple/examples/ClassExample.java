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
import java.util.ArrayList;
import java.util.List;

import com.sudoplay.juple.Juple;
import com.sudoplay.juple.JupleBuilder;

/**
 * This shows how to use the Juple class.
 * 
 * @author Jason Taylor
 */
public class ClassExample {

  public static void main(String[] args) {
    ClassExample app = new ClassExample();
    try {
      app.run();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static class Ability {

    private static final Ability DODGE = new Ability(0, "Dodge");
    private static final Ability VANISH = new Ability(1, "Vanish");

    public int id;
    public String name;

    @SuppressWarnings("unused")
    private Ability() {} // constructor for Juple

    public Ability(int id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  public static class Monster {

    public String name;
    public String type;

    public int hitPoints;
    public Armor armor;

    public boolean flying;

    public List<Ability> abilities = new ArrayList<Ability>();

  }

  public static class Armor {
    public String name;
    public int value;
  }

  public void run() throws IOException {

    Monster monster = new Monster();
    monster.name = "Rupert";
    monster.type = "Air Elemental";
    monster.hitPoints = 26;
    monster.armor = new Armor();
    monster.armor.name = "Glass Plate";
    monster.armor.value = 12;
    monster.flying = true;
    monster.abilities.add(Ability.DODGE);
    monster.abilities.add(Ability.VANISH);

    Juple parser = new JupleBuilder().setPrettyPrinting().create();
    String tml = parser.toTML(monster, Monster.class);

    System.out.println(tml);

  }

}
