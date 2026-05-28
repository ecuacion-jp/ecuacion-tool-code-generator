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

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

/**
 * Orchestrates all preparation steps that run after Excel files are read, including relation
 * back-references and misc-info merging.
 */
public class PrepareManager {

  /**
   * Registers bidirectional relation back-references into the referenced columns, then
   * delegates to each specialized preparer.
   */
  public void prepare() {

    Info info = MainController.tlInfo.get();

    // For entries in DBInfo and DbCommonInfo that have a bidirectional relation,
    // register back-reference info on the referenced side to enable additional generation there.
    List<DbOrClassColumnInfo.RelationRefInfo> relRefInfoList = new ArrayList<>();
    // Create a combined list of DB and DBCommon for iteration
    List<DbOrClassTableInfo> list = new ArrayList<>(info.getDbRootInfo().tableList);
    list.addAll(info.getDbCommonRootInfo().tableList);
    for (DbOrClassTableInfo ti : list) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.isRelation()) {
          jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum relationKind =
              java.util.Objects.requireNonNull(ci.getRelationKind(),
                  "isRelation() guarantees getRelationKind() is non-null");
          relRefInfoList.add(new DbOrClassColumnInfo.RelationRefInfo(ci.isRelationBidirectinal(),
              relationKind.getInverse(), ci.getRelationRefTable(), ci.getRelationRefCol(),
              ci.getRelationRefFieldName(), ti.getName(),
              StringUtil.getLowerCamelFromSnake(ci.getName()), ci.getRelationFieldName()));
        }
      }
    }

    // For bidirectional relations, populate the collected info into the referenced side
    for (DbOrClassColumnInfo.RelationRefInfo bdInfo : relRefInfoList) {
      boolean found = false;

      // It is unlikely that a common table is used as a reference target, so loop only over dbInfo
      for (DbOrClassTableInfo ti : info.getDbRootInfo().tableList) {
        for (DbOrClassColumnInfo ci : ti.columnList) {
          if (ti.getName().equals(bdInfo.getDstTableName())
              && ci.getName().equals(bdInfo.getDstColumnName())) {
            ci.getRelationRefInfoList().add(bdInfo);
            found = true;
          }
        }
      }

      if (!found) {
        throw new RuntimeException("not found : tableName = " + bdInfo.getDstTableName()
            + ", columnName = " + bdInfo.getDstColumnName());
      }
    }

    // DB and DataType:
    new PreparerForDbAndDataType().prepare();

    // miscRemovedData: add info to DbOrClassInfo
    new PreparerForMiscRemovedData().prepare();

    // miscGroupInfo: add info to DbOrClassInfo
    new PreparerForMiscGroup().prepare();

    // miscOptimisticLockInfo: consolidate into DbOrClassInfo
    new PreparerForMiscOptimisticLock().prepare();
  }
}
