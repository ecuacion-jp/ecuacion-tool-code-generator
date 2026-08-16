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
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.VersionGen;

/**
 * Propagates optimistic-lock column settings from {@code MiscOptimisticLockRootInfo} into
 * individual column definitions.
 */
public class PreparerForMiscOptimisticLock {

  private CodeGenContext getInfo() {
    return MainController.tlInfo.get();
  }

  /**
   * Marks each column that matches the optimistic-lock column as a version column, then
   * removes the lock info from the map.
   */
  public void prepare() {
    MiscOptimisticLockRootInfo lockInfo =
        (MiscOptimisticLockRootInfo) getInfo().getRootInfoMap()
            .get(DataKindEnum.MISC_OPTIMISTIC_LOCK);
    if (lockInfo != null && lockInfo.isDefined()) {
      // Set the optimistic-lock column info into individual columns
      // (This item is simple with few data fields so it can be held this way,
      //  but delete flags etc. are more complex and are handled together during entity generation)
      setOptLock(lockInfo, DataKindEnum.DB);
      setOptLock(lockInfo, DataKindEnum.DB_COMMON);
      // setOptLock(systemName, systemMap, lockInfo, Dict.XML_POST_FIX_CLS);

      // Discard the original data to avoid confusion
      getInfo().getRootInfoMap().remove(DataKindEnum.MISC_OPTIMISTIC_LOCK);
    }
  }

  private void setOptLock(MiscOptimisticLockRootInfo lockInfo, DataKindEnum dataKind) {
    DbOrClassRootInfo dbRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(dataKind);
    if (dbRootInfo != null) {
      for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
        for (DbOrClassColumnInfo ci : ti.columnList) {
          if (ci.getName().equals(lockInfo.getColumnName())) {
            // JPA's @Version only supports short/integer/long/Timestamp, independent of whichever
            // concrete dataType each table happens to use for its own version column.
            if (!VersionGen.isKataAllowed(ci.getDtInfo().getKata())) {
              new Violations()
                  .add(new BusinessViolation("MSG_ERR_OPTIMISTIC_LOCK_COLUMN_KATA_NOT_ALLOWED",
                      getInfo().getSystemName(), ti.getName(), ci.getName(),
                      ci.getDtInfo().getKata().toString()))
                  .throwIfAny();
            }

            ci.setOptLock(true);
          }
        }
      }
    }
  }
}
