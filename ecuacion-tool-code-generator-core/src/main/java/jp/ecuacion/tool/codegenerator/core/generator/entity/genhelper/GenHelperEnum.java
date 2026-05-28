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
package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

/** Code generation helper for {@code ENUM}-type columns. */
public class GenHelperEnum extends GenHelperKata {

//  @Override
//  public String getEntityAccessor(String columnNameCp, String columnNameSm, String dataType) {
//    StringBuilder sb = new StringBuilder();
//
//    sb.append(T1 + "public " + nc.changeExcelDataTypeNameToCamelWithFirstCharUpper(dataType)
//        + "Enum get" + columnNameCp + "() {" + RT);
//    sb.append(T2 + "return " + columnNameSm + ";" + RT);
//    sb.append(T1 + "}" + RT2);
//    sb.append(T1 + "public void set" + columnNameCp + "("
//        + nc.changeExcelDataTypeNameToCamelWithFirstCharUpper(dataType) + "Enum " + columnNameSm
//        + ") throws MultipleAppException {" + RT);
//    sb.append(T2 + "check(" + columnNameSm + ", " + columnNameSm + "Info);" + RT);
//    sb.append(T2 + "this." + columnNameSm + " = " + columnNameSm + ";" + RT);
//    sb.append(T1 + "}" + RT2);
//
//    return sb.toString();
//  }
}
