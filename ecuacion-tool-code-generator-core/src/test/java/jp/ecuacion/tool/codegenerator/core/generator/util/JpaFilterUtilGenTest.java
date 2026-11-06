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
package jp.ecuacion.tool.codegenerator.core.generator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscAdminScopeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("JpaFilterUtilGen")
class JpaFilterUtilGenTest {

  @TempDir
  @SuppressWarnings("null")
  Path outputDir;

  @SuppressWarnings("null")
  private CodeGenContext ctx;

  private DbOrClassRootInfo dbRootInfo = new DbOrClassRootInfo(DataKindEnum.DB);

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    SystemCommonRootInfo sysCmn = mock(SystemCommonRootInfo.class);
    when(sysCmn.getBasePackage()).thenReturn("jp.example.app");
    when(sysCmn.getCharacterEncoding()).thenReturn("UTF-8");
    MiscSoftDeleteRootInfo softDelete = mock(MiscSoftDeleteRootInfo.class);
    when(softDelete.isDefined()).thenReturn(true);

    ctx = mock(CodeGenContext.class);
    ctx.outputDir = outputDir.toString();
    when(ctx.getSystemName()).thenReturn("app");
    when(ctx.getSysCmnRootInfo()).thenReturn(sysCmn);
    when(ctx.getRemovedDataRootInfo()).thenReturn(softDelete);
    when(ctx.getDbRootInfo()).thenReturn(dbRootInfo);
    when(ctx.getAdminScopeRootInfo()).thenReturn(new MiscAdminScopeRootInfo());
    MainController.tlInfo.set(ctx);
  }

  @AfterEach
  void tearDown() {
    MainController.tlInfo.remove();
  }

  @Test
  @DisplayName("passes a filter of every table with a group identifier column")
  void multipleCustomGroupFilters() throws Exception {
    when(ctx.getGroupRootInfo())
        .thenReturn(new MiscGroupRootInfo("ACC_GENERAL_ID", "DT_SERIAL", "TARGET_SYSTEM"));
    dbRootInfo.tableList.add(table("ACC", "ID"));
    dbRootInfo.tableList.add(table("ACC_GENERAL", "ACC_ID"));
    dbRootInfo.tableList.add(table("INQUIRY", null));
    dbRootInfo.tableList.add(table("TARGET_SYSTEM", null));

    assertThat(generate()).contains("import java.util.Map;")
        .contains("super(true, true, \"accGeneralId\", Map.ofEntries(\n"
            + "        Map.entry(\"groupFilterAcc\", \"id\"),\n"
            + "        Map.entry(\"groupFilterAccGeneral\", \"accId\")));");
  }

  @Test
  @DisplayName("passes an empty map when no table has a group identifier column")
  void noCustomGroupFilter() throws Exception {
    when(ctx.getGroupRootInfo())
        .thenReturn(new MiscGroupRootInfo("ACC_GROUP_ID", "DT_SERIAL", ""));
    dbRootInfo.tableList.add(table("INQUIRY", null));

    assertThat(generate()).contains("super(true, true, \"accGroupId\", Map.of());");
  }

  @Test
  @DisplayName("passes null and an empty map when grouping is not defined")
  void groupingUndefined() throws Exception {
    when(ctx.getGroupRootInfo()).thenReturn(new MiscGroupRootInfo());
    dbRootInfo.tableList.add(table("INQUIRY", null));

    assertThat(generate()).contains("super(true, false, null, Map.of());");
  }

  @Test
  @DisplayName("passes the admin scope filters when the admin scope is defined")
  void adminScopeDefined() throws Exception {
    when(ctx.getGroupRootInfo()).thenReturn(new MiscGroupRootInfo());
    when(ctx.getAdminScopeRootInfo())
        .thenReturn(new MiscAdminScopeRootInfo("TARGET_SYSTEM_ID", "DT_SERIAL", "ACC"));
    dbRootInfo.tableList.add(table("INQUIRY", null));
    dbRootInfo.tableList.add(tableWithCustomAdminScopeColumn("TARGET_SYSTEM", "ID"));
    dbRootInfo.tableList.add(tableWithCustomAdminScopeColumn("PARTNER_SYSTEM", "SYSTEM_ID"));

    assertThat(generate()).contains("super(true, false, null, Map.of(), Map.ofEntries(\n"
        + "        Map.entry(\"adminScopeFilter\", \"targetSystemId\"),\n"
        + "        Map.entry(\"adminScopeFilterTargetSystem\", \"id\"),\n"
        + "        Map.entry(\"adminScopeFilterPartnerSystem\", \"systemId\")));");
  }

  private String generate() throws Exception {
    new JpaFilterUtilGen().generate();
    return Files.readString(
        outputDir.resolve("app/src/main/java/jp/example/app/base/util/JpaFilterUtil.java"),
        StandardCharsets.UTF_8).replace("\r\n", "\n");
  }

  /** Returns a table with one column, which is an admin scope identifier column. */
  private DbOrClassTableInfo tableWithCustomAdminScopeColumn(String name, String columnName) {
    DbOrClassTableInfo ti = new DbOrClassTableInfo(name);
    @SuppressWarnings("null")
    DbOrClassColumnInfo ci = mock(DbOrClassColumnInfo.class);
    when(ci.isCustomAdminScopeColumn()).thenReturn(true);
    when(ci.getNameCamel())
        .thenReturn(jp.ecuacion.lib.core.util.StringUtil.getLowerCamelFromSnake(columnName));
    ti.columnList.add(ci);
    return ti;
  }

  /**
   * Returns a table with one column, which is a group identifier column
   *     if {@code customGroupColumnName} is not {@code null}.
   */
  private DbOrClassTableInfo table(String name,
      @org.jspecify.annotations.Nullable String customGroupColumnName) {
    DbOrClassTableInfo ti = new DbOrClassTableInfo(name);
    @SuppressWarnings("null")
    DbOrClassColumnInfo ci = mock(DbOrClassColumnInfo.class);
    if (customGroupColumnName != null) {
      when(ci.isCustomGroupColumn()).thenReturn(true);
      when(ci.getName()).thenReturn(customGroupColumnName);
      when(ci.getNameCamel()).thenReturn(
          jp.ecuacion.lib.core.util.StringUtil.getLowerCamelFromSnake(customGroupColumnName));
    }
    ti.columnList.add(ci);
    return ti;
  }
}
