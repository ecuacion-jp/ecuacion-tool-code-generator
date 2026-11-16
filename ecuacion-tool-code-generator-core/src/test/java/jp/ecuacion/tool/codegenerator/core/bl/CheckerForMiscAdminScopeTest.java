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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscAdminScopeRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckerForMiscAdminScope")
class CheckerForMiscAdminScopeTest {

  private static final String COL = "TARGET_SYSTEM_ID";

  private final DbOrClassRootInfo common = new DbOrClassRootInfo(DataKindEnum.DB_COMMON);
  private final DbOrClassRootInfo db = new DbOrClassRootInfo(DataKindEnum.DB);

  @BeforeEach
  void setUp() {
    common.tableList.add(table("APP_COMMON", "VERSION"));
    DbOrClassTableInfo targetSystem = table("TARGET_SYSTEM");
    targetSystem.columnList.add(customColumn("ID", "DT_SERIAL"));
    db.tableList.add(targetSystem);
    db.tableList.add(table("INQUIRY", "ID", COL));
    db.tableList.add(table("ACC", "ID"));
  }

  private DbOrClassTableInfo table(String name, String... columnNames) {
    DbOrClassTableInfo ti = new DbOrClassTableInfo(name);
    for (String columnName : columnNames) {
      ti.columnList.add(column(columnName, "DT_SERIAL"));
    }
    return ti;
  }

  private DbOrClassColumnInfo column(String name, String dataType) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo ci = mock(DbOrClassColumnInfo.class);
    when(ci.getName()).thenReturn(name);
    when(ci.getDataType()).thenReturn(dataType);
    return ci;
  }

  private DbOrClassColumnInfo customColumn(String name, String dataType) {
    DbOrClassColumnInfo ci = column(name, dataType);
    when(ci.isCustomAdminScopeColumn()).thenReturn(true);
    return ci;
  }

  private void check(String without) {
    new CheckerForMiscAdminScope().check("app", common, db,
        new MiscAdminScopeRootInfo(COL, "DT_SERIAL", without));
  }

  private void assertViolation(String without, String messageId) {
    assertThatThrownBy(() -> check(without)).isInstanceOfSatisfying(ViolationException.class,
        e -> assertThat(e.getViolations().getBusinessViolations())
            .extracting(BusinessViolation::getMessageId).contains(messageId));
  }

  @Test
  @DisplayName("does nothing when the admin scope is not used, even with unclassified tables")
  void notDefined() {
    assertThatCode(() -> new CheckerForMiscAdminScope().check("app", common, db,
        new MiscAdminScopeRootInfo())).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("passes when every table is classified")
  void allClassified() {
    assertThatCode(() -> check("ACC")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("custom columns can be set in multiple tables")
  void customColumnsInMultipleTables() {
    db.tableList.get(2).columnList.add(customColumn("SYSTEM_ID", "DT_SERIAL"));

    assertThatCode(() -> check("")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("every table is classified when APP_COMMON has the scope column")
  void scopeColumnInCommon() {
    common.tableList.get(0).columnList.add(column(COL, "DT_SERIAL"));
    db.tableList.get(0).columnList.clear();
    db.tableList.get(1).columnList.removeIf(ci -> ci.getName().equals(COL));

    assertThatCode(() -> check("")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("an unclassified table is an error")
  void unclassified() {
    assertViolation("", "MSG_ERR_TABLE_NOT_CLASSIFIED_FOR_ADMIN_SCOPE");
  }

  @Test
  @DisplayName("a table without admin scope having the scope column is an error")
  void withoutAdminScopeHasColumn() {
    assertViolation("ACC,INQUIRY", "MSG_ERR_TABLE_WITHOUT_ADMIN_SCOPE_HAS_SCOPE_COLUMN");
  }

  @Test
  @DisplayName("a table without admin scope having a custom column is an error")
  void withoutAdminScopeHasCustomColumn() {
    assertViolation("ACC,TARGET_SYSTEM", "MSG_ERR_TABLE_WITHOUT_ADMIN_SCOPE_HAS_SCOPE_COLUMN");
  }

  @Test
  @DisplayName("a table with both the scope column and a custom column is an error")
  void scopeColumnAndCustomColumn() {
    db.tableList.get(1).columnList.add(customColumn("OTHER_ID", "DT_SERIAL"));

    assertViolation("ACC", "MSG_ERR_ADMIN_SCOPE_COL_AND_CUSTOM_COL_IN_TABLE");
  }

  @Test
  @DisplayName("multiple custom columns in a table is an error")
  void multipleCustomColumns() {
    db.tableList.get(0).columnList.add(customColumn("SYSTEM_ID", "DT_SERIAL"));

    assertViolation("ACC", "MSG_ERR_MULTIPLE_CUSTOM_ADMIN_SCOPE_COLUMNS_IN_TABLE");
  }

  @Test
  @DisplayName("a custom column in APP_COMMON is an error")
  void customColumnInCommon() {
    common.tableList.get(0).columnList.add(customColumn("SYSTEM_ID", "DT_SERIAL"));

    assertViolation("ACC", "MSG_ERR_APP_COMMON_ENTITY_CANNOT_HAVE_CUSTOM_ADMIN_SCOPE_COLUMN");
  }

  @Test
  @DisplayName("no table with the scope column is an error")
  void scopeColumnNotFound() {
    db.tableList.get(1).columnList.removeIf(ci -> ci.getName().equals(COL));

    assertViolation("ACC,INQUIRY", "MSG_ERR_ADMIN_SCOPE_COL_NOT_FOUND");
  }

  @Test
  @DisplayName("the scope column with a different dataType is an error")
  void differentDataType() {
    db.tableList.get(1).columnList.removeIf(ci -> ci.getName().equals(COL));
    db.tableList.get(1).columnList.add(column(COL, "DT_STRING"));

    assertViolation("ACC", "MSG_ERR_DT_OF_COL_FOR_ADMIN_SCOPE_COL_DIFFER");
  }

  @Test
  @DisplayName("a custom column with a different dataType is an error")
  void customColumnDifferentDataType() {
    db.tableList.get(0).columnList.clear();
    db.tableList.get(0).columnList.add(customColumn("ID", "DT_STRING"));

    assertViolation("ACC", "MSG_ERR_DT_OF_COL_FOR_ADMIN_SCOPE_COL_DIFFER");
  }
}
