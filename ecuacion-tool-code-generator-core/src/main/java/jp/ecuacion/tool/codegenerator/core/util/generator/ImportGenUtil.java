/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;

/**
 * Manages a sorted set of import strings and produces the final import block, eliminating redundant
 * class imports already covered by a wildcard.
 */
public class ImportGenUtil extends ToolForCodeGen {

  private TreeSet<String> importSet = new TreeSet<>();

  /** Adds each of the given fully-qualified class names to the import set. */
  public void add(String... strings) {
    for (String str : strings) {
      importSet.add(str);
    }
  }

  /** Removes the given string from the import set if it is present. */
  public void removeIfContains(String string) {
    if (importSet.contains(string)) {
      importSet.remove(string);
    }
  }

  /**
   * Builds and returns the import block string, removing any class-level imports superseded by a
   * wildcard import from the same package.
   */
  public String outputStr() {
    StringBuilder sb = new StringBuilder();

    // If one entry is a.b.c and another is a.b.*, remove a.b.c
    List<String> asteriskList = new ArrayList<>();
    List<String> noAsteriskList = new ArrayList<>();
    for (String str : importSet) {
      if (str.substring(str.lastIndexOf(".") + 1).equals("*")) {
        asteriskList.add(str);

      } else {
        noAsteriskList.add(str);
      }
    }

    for (String asteriskPkg : asteriskList) {
      String pkg = asteriskPkg.substring(0, asteriskPkg.lastIndexOf("."));

      for (String str : noAsteriskList) {
        if (str.substring(0, str.lastIndexOf(".")).equals(pkg)) {
          importSet.remove(str);
        }
      }
    }

    for (String str : importSet) {
      sb.append("import " + str + ";" + RT);
    }

    return sb.toString();
  }
}
