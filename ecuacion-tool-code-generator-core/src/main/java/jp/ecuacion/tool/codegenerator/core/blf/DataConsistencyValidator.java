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
package jp.ecuacion.tool.codegenerator.core.blf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Validates cross-table and cross-file data consistency for the parsed Excel data.
 *
 * <p>NullAway is suppressed at class level because every value retrieved from the root info
 * objects is treated as present at this stage of the pipeline.</p>
 */
@SuppressWarnings("NullAway")
class DataConsistencyValidator {

  /** Validates that relation references in child-table columns point to existing tables/columns. */
  void checkForChildTable(String sysName, DbOrClassRootInfo dbOrClassRootInfo) {
    List<String> tableNameSet = dbOrClassRootInfo.tableList.stream().map(e -> e.getName()).toList();

    for (DbOrClassTableInfo ti : dbOrClassRootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (StringUtils.isNotEmpty(ci.getRelationRefTable())) {

          // relation: refering to table name existence check
          if (!tableNameSet.contains(ci.getRelationRefTable())) {
            new Violations().add(new BusinessViolation("MSG_ERR_DB_REFER_TO_TABLE_NAME_NOT_FOUND",
                sysName, ti.getName(), ci.getName(), ci.getRelationRefTable())).throwIfAny();
          }

          DbOrClassTableInfo refTi = dbOrClassRootInfo.tableList.stream()
              .collect(Collectors.toMap(e -> e.getName(), e -> e)).get(ci.getRelationRefTable());

          // relation: refering to column name existence check
          List<String> refTiColNameList = refTi.columnList.stream().map(e -> e.getName()).toList();
          if (!refTiColNameList.contains(ci.getRelationRefCol())) {
            new Violations().add(new BusinessViolation("MSG_ERR_DB_REFER_TO_COLUMN_NAME_NOT_FOUND",
                sysName, ti.getName(), ci.getName(), ci.getRelationRefCol())).throwIfAny();
          }
        }
      }
    }
  }

  /**
   * Validates PK/unique-constraint consistency between parent (common) and child tables, and
   * records the result on each table info.
   */
  void checkAndComplementForParentAndChildTable(DbOrClassRootInfo dbCommonRootInfo,
      DbOrClassRootInfo dbRootInfo) {
    // Loop again to add information
    for (DbOrClassTableInfo tableInfo : dbRootInfo.tableList) {
      boolean hasS = false;
      boolean hasU = false;

      // Merge dbInfo and dbCommonInfo columns since judgment requires both
      List<DbOrClassColumnInfo> commonAddedColumnList = new ArrayList<>();
      commonAddedColumnList.addAll(tableInfo.columnList);
      commonAddedColumnList.addAll(dbCommonRootInfo.tableList.get(0).columnList);

      for (DbOrClassColumnInfo colInfo : commonAddedColumnList) {
        if (colInfo.isPk()) {
          // Check because having two surrogate key columns is not allowed
          if (hasS) {
            new Violations().add(new BusinessViolation(
                "MSG_ERR_SURROGATE_KEY_DUPLICATED", tableInfo.getName())).throwIfAny();
          }
          hasS = true;
        }

        if (colInfo.isUniqueConstraint()) {
          hasU = true;
        }
      }

      // PK is required. SystemCommon is treated specially.
      if (!tableInfo.getName().equals("SYSTEM_COMMON") && !hasS) {
        new Violations().add(
            new BusinessViolation("MSG_ERR_PK_REQUIRED", tableInfo.getName())).throwIfAny();
      }

      tableInfo.setHasUniqueConstraint(hasU);
    }
  }

  /**
   * Checks consistency between table definitions and group definitions, and complements
   * group-related data.
   */
  void checkAndComplementForTableAndGroup(String systemName, DbOrClassRootInfo dbCommonRootInfo,
      DbOrClassRootInfo dbRootInfo, MiscGroupRootInfo groupRootInfo) {

    final String colName = groupRootInfo.getColumnName();

    // Exit if group definition is absent
    if (!groupRootInfo.isDefined()) {
      return;
    }

    // Check whether the column defined in groupRootInfo exists in the parent/child tables
    // (the child is considered to have the column if it exists in any child table)
    boolean parentTableHasGroupCol = dbCommonRootInfo.tableList.get(0).hasColumn(colName);
    boolean childTableHasGroupCol = false;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasColumn(colName)) {
        childTableHasGroupCol = true;
        break;
      }
    }

    // Having the column in both parent and child is invalid, but it is caught by the
    // "same column exists in both parent and child" check, so it is not checked here.
    // Do check for the case where it exists in neither parent nor child.
    if (!parentTableHasGroupCol && !childTableHasGroupCol) {
      new Violations().add(
          new BusinessViolation("MSG_ERR_COMMON_GROUP_COL_NOT_FOUND", systemName)).throwIfAny();
    }

    // When "group_id" is used as the common group column name, the master group table may want to
    // hold the column as "id" rather than "group_id". For this purpose each table has a
    // "custom group column" setting. As this background implies, a custom group column can only
    // exist in child tables, at most once.

    // It makes no sense to have a "custom group column" in systemCommon, so treat it as an error.
    // (The common group setting should be used instead.)
    if (dbCommonRootInfo.tableList.get(0).hasCustomGroupColumn()) {
      new Violations().add(new BusinessViolation(
          "MSG_ERR_SYSTEM_COMMON_ENTITY_CANNOT_HAVE_CUSTOM_GROUP_COLUMN", systemName)).throwIfAny();
    }

    // Having more than one "custom group column" in child tables is an error
    int numOfCustomGroupColumns = 0;
    String customGroupTableName = null;
    String customGroupColumnName = null;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasCustomGroupColumn()) {
        numOfCustomGroupColumns++;
        customGroupTableName = ti.getName();
        customGroupColumnName = ti.getCustomGroupColumn().getName();
        if (numOfCustomGroupColumns > 1) {
          new Violations().add(new BusinessViolation(
              "MSG_ERR_MULTIPLE_CUSTOM_GROUP_COLUMN_CANNOT_EXIST", systemName)).throwIfAny();
        }
      }
    }

    // Add customGroup information
    groupRootInfo.setCustomGroupTableName(customGroupTableName);
    groupRootInfo.setCustomGroupColumnName(customGroupColumnName);
  }
}
