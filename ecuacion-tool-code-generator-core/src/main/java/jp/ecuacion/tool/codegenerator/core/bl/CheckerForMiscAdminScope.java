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
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscAdminScopeRootInfo;

/**
 * Checks the admin scope settings against the table definitions.
 *
 * <p>Every table must be classified as a table with the scope column, a table with a custom admin
 *     scope column or a table without admin scope, so that a table added without setting
 *     is found on generation.</p>
 */
public class CheckerForMiscAdminScope {

  /** Checks the settings. Does nothing if the admin scope is not used. */
  public void check(String systemName, DbOrClassRootInfo dbCommonRootInfo,
      DbOrClassRootInfo dbRootInfo, MiscAdminScopeRootInfo info) {

    if (!info.isDefined()) {
      return;
    }

    final String colName = info.getColumnName();
    final DbOrClassTableInfo commonTable = dbCommonRootInfo.tableList.get(0);
    Violations violations = new Violations();

    // It makes no sense to have a custom admin scope column in appCommon, as with grouping.
    if (commonTable.hasCustomAdminScopeColumn()) {
      violations.add(new BusinessViolation(
          "MSG_ERR_APP_COMMON_ENTITY_CANNOT_HAVE_CUSTOM_ADMIN_SCOPE_COLUMN", systemName));
    }

    checkDataType(violations, systemName, commonTable, info);

    boolean existsScopeColumn = commonTable.hasColumn(colName);
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      existsScopeColumn = existsScopeColumn || ti.hasColumn(colName);
      checkDataType(violations, systemName, ti, info);
      checkClassification(violations, systemName, commonTable, ti, info);

      if (ti.columnList.stream().filter(DbOrClassColumnInfo::isCustomAdminScopeColumn)
          .count() > 1) {
        violations.add(new BusinessViolation("MSG_ERR_MULTIPLE_CUSTOM_ADMIN_SCOPE_COLUMNS_IN_TABLE",
            systemName, ti.getName()));
      }
    }

    if (!existsScopeColumn) {
      violations.add(new BusinessViolation("MSG_ERR_ADMIN_SCOPE_COL_NOT_FOUND", systemName));
    }

    violations.throwIfAny();
  }

  private void checkClassification(Violations violations, String systemName,
      DbOrClassTableInfo commonTable, DbOrClassTableInfo ti, MiscAdminScopeRootInfo info) {

    final String colName = info.getColumnName();
    final boolean hasScopeColumn = ti.hasColumn(colName) || commonTable.hasColumn(colName);
    final boolean hasCustomColumn = ti.hasCustomAdminScopeColumn();

    if (info.getTableNamesWithoutAdminScope().contains(ti.getName())) {
      if (hasScopeColumn || hasCustomColumn) {
        violations.add(new BusinessViolation("MSG_ERR_TABLE_WITHOUT_ADMIN_SCOPE_HAS_SCOPE_COLUMN",
            systemName, ti.getName()));
      }

    } else if (hasScopeColumn && hasCustomColumn) {
      violations.add(new BusinessViolation("MSG_ERR_ADMIN_SCOPE_COL_AND_CUSTOM_COL_IN_TABLE",
          systemName, ti.getName()));

    } else if (!hasScopeColumn && !hasCustomColumn) {
      violations.add(new BusinessViolation("MSG_ERR_TABLE_NOT_CLASSIFIED_FOR_ADMIN_SCOPE",
          systemName, ti.getName()));
    }
  }

  private void checkDataType(Violations violations, String systemName, DbOrClassTableInfo ti,
      MiscAdminScopeRootInfo info) {

    for (DbOrClassColumnInfo ci : ti.columnList) {
      if ((ci.getName().equals(info.getColumnName()) || ci.isCustomAdminScopeColumn())
          && !ci.getDataType().equals(info.getDataTypeName())) {
        violations.add(new BusinessViolation("MSG_ERR_DT_OF_COL_FOR_ADMIN_SCOPE_COL_DIFFER",
            systemName, ti.getName(), ci.getName(), ci.getDataType(), info.getDataTypeName()));
      }
    }
  }
}
