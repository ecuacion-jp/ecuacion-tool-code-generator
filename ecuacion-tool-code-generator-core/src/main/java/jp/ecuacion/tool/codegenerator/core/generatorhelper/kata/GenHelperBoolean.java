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


/**
 * Code generation helper for {@code BOOLEAN}-type columns, providing a string-based setter
 * that accepts "0"/"1"/"FALSE"/"TRUE".
 */
public class GenHelperBoolean extends GenHelperNoNumberObj {

  /**
   * Generates a setter that accepts a String representation of a boolean and converts it to
   * {@code Boolean}.
   */
  @Override
  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    StringBuilder sb = new StringBuilder();

    sb.append(T1 + "public void set" + columnNameCp + "(String str" + columnNameCp + ") {" + RT);
    sb.append(T3 + "String str = str" + columnNameCp + ".toUpperCase();" + RT);
    sb.append(T3 + "Boolean b = null;" + RT);
    sb.append(T3 + "if (str.equals(\"0\") || str.equals(\"FALSE\")) b = false;" + RT);
    sb.append(T3 + "else if (str.equals(\"1\") || str.equals(\"TRUE\")) b = true;" + RT);
    sb.append(T3 + "else new Violations().add(new BusinessViolation("
        + "\"MSG_ERR_INCORRECT_BOOLEAN_STRING\", str"
        + columnNameCp + ")).throwIfAny();" + RT);
    sb.append(T3 + "set" + columnNameCp + "(b);" + RT);
    sb.append(T1 + "}" + RT);

    return sb.toString();
  }
}
