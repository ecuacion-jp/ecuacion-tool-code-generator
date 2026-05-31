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

/**
 * Code generation helper for {@code DATE_TIME}-type columns, providing a timestamp-parsing
 * string setter.
 */
public class GenHelperDateTime extends GenHelperNoNumberObj {
  @Override
  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    StringBuilder sb = new StringBuilder();

    sb.append(T1 + "public void set" + columnNameCp + "(String str" + columnNameCp + ") {" + RT);
    sb.append(T2 + "try {" + RT);
    sb.append(
        T4 + "SimpleDateFormat sdf = new SimpleDateFormat(\"yyyy'/'MM'/'dd' 'HH':'mm':'ss'.'SSS\");"
            + RT);
    sb.append(T4 + "set" + columnNameCp + "(new Timestamp(sdf.parse(str" + columnNameCp
        + ").getTime()));" + RT);
    sb.append(T2 + "} catch (ParseException pe) {" + RT);
    sb.append(T3 + "new Violations().add(new BusinessViolation(\"MSG_ERR_TIMESTAMP_FORMAT_WRONG\", "
        + columnNameSm + "Info.getDisplayName(), str" + columnNameCp + ")).throwIfAny();" + RT);
    sb.append(T2 + "}" + RT);
    sb.append(T1 + "}" + RT2);
    return sb.toString();
  }

  @Override
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    String[] rtnStrings = mergeStrings(super.getNeededImports(columnInfo), "java.time.*");

    return rtnStrings;
  }
}
