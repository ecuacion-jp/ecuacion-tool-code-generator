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

import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** Prepares group-related settings by merging group info into the DB table definitions. */
public class PreparerForMiscGroup {

  private CodeGenContext getInfo() {
    return MainController.tlInfo.get();
  }

  /** Checks that the group column's DataType matches the declaration in all relevant tables. */
  public void prepare() {
    MiscGroupRootInfo groupInfo =
        (MiscGroupRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.MISC_GROUP);
    if (groupInfo == null || !groupInfo.isDefined()) {
      return;
    }
    checkGroupColDataType(groupInfo, DataKindEnum.DB);
    checkGroupColDataType(groupInfo, DataKindEnum.DB_COMMON);
  }

  private void checkGroupColDataType(MiscGroupRootInfo groupInfo, DataKindEnum dataKind) {
    DbOrClassRootInfo dbRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(dataKind);
    if (dbRootInfo == null) {
      return;
    }
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.getName().equals(groupInfo.getColumnName())) {
          if (!ci.getDataType().equals(groupInfo.getDataTypeName())) {
            new Violations().add(new BusinessViolation("MSG_ERR_DT_OF_COL_FOR_GRP_COL_DIFFER",
                getInfo().getSystemName(), ti.getName(), ci.getName(), ci.getDataType(),
                groupInfo.getDataTypeName())).throwIfAny();
          }
        }
      }
    }
  }
}
