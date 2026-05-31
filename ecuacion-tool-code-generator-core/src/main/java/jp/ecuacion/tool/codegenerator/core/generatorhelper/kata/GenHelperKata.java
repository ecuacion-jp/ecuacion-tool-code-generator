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
package jp.ecuacion.tool.codegenerator.core.generatorhelper.kata;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractCode;

/** Abstract base class for type-specific entity generation helpers. */
public abstract class GenHelperKata extends AbstractCode {

  /** Returns the import strings needed for the given column; returns an empty array by default. */
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    return new String[] {};
  }

  /** Merges two String arrays and returns a new String array. */
  protected String[] mergeStrings(String[] str1, String... strs) {
    String[] rtnStrs = new String[str1.length + strs.length];
    System.arraycopy(str1, 0, rtnStrs, 0, str1.length);
    System.arraycopy(strs, 0, rtnStrs, str1.length, strs.length);

    return rtnStrs;
  }

  /**
   * The class name follows the pattern "EntityGenHelperXxx", where Xxx directly represents the Java
   * type name. This convenience method extracts Xxx from the class name.
   */
  protected String getJavaKataName() {
    return this.getClass().getSimpleName().replace("EntityGenHelper", "");
  }
}
