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
package jp.ecuacion.tool.codegenerator.core.bl;

/** Prepares DB table information by combining table-level and column-level definitions. */
public class PreparerForDbTable {

  /** Constructs an instance. */
  public PreparerForDbTable() {
    // this.info = CodeGeneratorAction.tlInfo.get();
  }

  /** Combines DB table info into the unified data structure (currently a no-op placeholder). */
  public void combine() {

    // Map<String, AbstractRootInfo> rootInfoMap = info.systemMap.get(info.getSystemName());
    // List<DbOrClassTableInfo> tableListFromTableInfo =
    // ((DbOrClassRootInfo) rootInfoMap.get(Constants.XML_POST_FIX_DB_TABLE)).tableList;
    //
    // // Transfer all data from dbTableInfo into the original list
    // // Convert the table info list obtained from dbTableInfo into a HashMap for easier handling.
    // HashMap<String, DbOrClassTableInfo> tableFromTableInfoMap =
    // new HashMap<String, DbOrClassTableInfo>();
    // for (DbOrClassTableInfo tableInfo : tableListFromTableInfo) {
    // tableFromTableInfoMap.put(tableInfo.getTableName(), tableInfo);
    // }
  }
}
