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
package jp.ecuacion.tool.codegenerator.core.generator.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Generates the source from the actual Excel template filled in with a small definition.
 *
 * <p>It's run on the template to find a gap between the columns of the template and the reader.
 *     </p>
 */
@DisplayName("Generation of admin scope filters from the Excel template")
class AdminScopeGenerationTest {

  private static final Path TEMPLATE =
      Path.of("../excel-format/db-column-definitions_fmt-v5.0.0-ja_my-app.xlsx");

  private static final String HEADER_TABLE = "テーブル名";
  private static final String HEADER_COLUMN = "カラム名";
  private static final String HEADER_DATA_TYPE = "dataType";
  private static final String HEADER_SURROGATE_KEY = "Surrogate key ";
  private static final String HEADER_AUTO_NUMBER = "自動採番";
  private static final String HEADER_ADMIN_SCOPE_IDENTIFIER = "範囲識別項目";
  private static final String HEADER_DISPLAY_NAME = "カラム表示名（デフォルト言語）";
  private static final String DT_ID = "DT_SERIAL";
  private static final String MARK = "○";

  @TempDir
  @SuppressWarnings("null")
  Path dir;

  @Test
  @DisplayName("generates a filter for each kind of table")
  void scopeUsed() throws Exception {
    generate(true);

    assertThat(entity("AppCommon"))
        .contains("@FilterDef(name = \"adminScopeFilter\"")
        .contains("defaultCondition = \"TARGET_SYSTEM_ID in (:targetSystemId)\"");
    assertThat(entity("Inquiry")).contains("@Filter(name = \"adminScopeFilter\")")
        .doesNotContain("@FilterDef(name = \"adminScopeFilter");
    assertThat(entity("TargetSystem"))
        .contains("@FilterDef(name = \"adminScopeFilterTargetSystem\"")
        .contains("defaultCondition = \"ID in (:id)\"")
        .contains("@Filter(name = \"adminScopeFilterTargetSystem\")");
    assertThat(entity("Acc")).doesNotContain("adminScopeFilter");

    // The child list of TARGET_SYSTEM is narrowed by the filter of the child.
    String targetSystem = entity("TargetSystem");
    assertThat(targetSystem.substring(targetSystem.indexOf("@OneToMany")))
        .contains("@Filter(name = \"adminScopeFilter\")");

    assertThat(generated("util/JpaFilterUtil.java"))
        .contains("Map.entry(\"adminScopeFilter\", \"targetSystemId\")")
        .contains("Map.entry(\"adminScopeFilterTargetSystem\", \"id\")");
  }

  @Test
  @DisplayName("generates nothing for the admin scope when it's not used")
  void scopeNotUsed() throws Exception {
    generate(false);

    try (Stream<Path> files = Files.walk(dir.resolve("out"))) {
      for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).doesNotContain("adminScope");
      }
    }
  }

  private void generate(boolean usesAdminScope) throws Exception {
    Path excel = dir.resolve("db-column-definitions_fmt-v5.0.0-ja_app.xlsx");
    try (InputStream in = Files.newInputStream(TEMPLATE);
        Workbook wb = WorkbookFactory.create(in);
        OutputStream out = Files.newOutputStream(excel)) {
      fill(wb, usesAdminScope);
      wb.write(out);
    }

    new MainController().execute(excel.toString(), dir.resolve("out").toString(), false);
  }

  private void fill(Workbook wb, boolean usesAdminScope) {
    Sheet settings = wb.getSheet("各種設定");
    setting(settings, "SYSTEM_COMMON", "SYSTEM_NAME", "app");
    setting(settings, "SYSTEM_COMMON", "BASE_PACKAGE", "jp.example.app");
    setting(settings, "SYSTEM_COMMON", "FRAMEWORK_KIND", "Spring Framework");
    setting(settings, "SYSTEM_COMMON", "CHARSET", "UTF-8");
    setting(settings, "SYSTEM_COMMON", "LANG_DEFAULT", "en");
    setting(settings, "OPTIMISTIC_LOCKING", "COLUMN_NAME", "VERSION");
    setting(settings, "OPTIMISTIC_LOCKING", "DATA_TYPE_NAME", DT_ID);
    if (usesAdminScope) {
      setting(settings, "ADMIN_SCOPE", "COLUMN_NAME", "TARGET_SYSTEM_ID");
      setting(settings, "ADMIN_SCOPE", "DATA_TYPE_NAME", DT_ID);
      setting(settings, "ADMIN_SCOPE", "TABLE_NAMES_WITHOUT_ADMIN_SCOPE", "ACC");
    }

    Sheet dataTypes = wb.getSheet("dataType定義");
    set(dataTypes, 7, 0, DT_ID);
    set(dataTypes, 7, 1, "LONG");

    fillRows(wb.getSheet("DB共通項目定義"), List.of(Map.of(HEADER_COLUMN, "VERSION",
        HEADER_DATA_TYPE, DT_ID, HEADER_DISPLAY_NAME, "version")));

    List<Map<String, String>> rows = new ArrayList<>();
    rows.add(idColumn("TARGET_SYSTEM", MARK));
    rows.add(idColumn("INQUIRY", ""));
    rows.add(Map.of(HEADER_TABLE, "INQUIRY", HEADER_COLUMN, "TARGET_SYSTEM_ID", HEADER_DATA_TYPE,
        DT_ID, HEADER_DISPLAY_NAME, "target system ID", "関連：種類", "@ManyToOne", "関連：direction",
        "bidirectional", "関連：参照元変数名", "targetSystem", "関連：参照先テーブル", "TARGET_SYSTEM",
        "関連：参照先カラム", "ID"));
    rows.add(idColumn("ACC", ""));
    fillRows(wb.getSheet("DB項目定義"), rows);
    wb.getSheet("DB項目定義").getRow(7).getCell(header(wb.getSheet("DB項目定義"), "関連：参照先変数名"),
        Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("inquiryList");

    fillTableList(wb.getSheet("テーブル一覧"), List.of("TARGET_SYSTEM", "INQUIRY", "ACC"));
  }

  private Map<String, String> idColumn(String table, String adminScopeIdentifier) {
    return Map.of(HEADER_TABLE, table, HEADER_COLUMN, "ID", HEADER_DATA_TYPE, DT_ID,
        HEADER_DISPLAY_NAME, "ID", HEADER_SURROGATE_KEY, MARK, HEADER_AUTO_NUMBER, MARK,
        HEADER_ADMIN_SCOPE_IDENTIFIER, adminScopeIdentifier);
  }

  private void fillRows(Sheet sheet, List<Map<String, String>> rows) {
    int rowIndex = 5;
    for (Map<String, String> row : rows) {
      for (Map.Entry<String, String> entry : row.entrySet()) {
        set(sheet, rowIndex, header(sheet, entry.getKey()), entry.getValue());
      }
      rowIndex++;
    }
  }

  /** Replaces the dynamic array formula, as POI doesn't recalculate it. */
  private void fillTableList(Sheet sheet, List<String> tables) {
    for (int i = 5; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      Cell cell = row == null ? null : row.getCell(0);
      if (cell != null) {
        if (cell.isPartOfArrayFormulaGroup()) {
          sheet.removeArrayFormula(cell);
        }
        cell.setBlank();
      }
    }
    for (int i = 0; i < tables.size(); i++) {
      set(sheet, 5 + i, 0, tables.get(i));
      set(sheet, 5 + i, header(sheet, "テーブル表示名（デフォルト言語）"), tables.get(i));
    }
  }

  private int header(Sheet sheet, String label) {
    for (Cell cell : sheet.getRow(4)) {
      if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equals(label)) {
        return cell.getColumnIndex();
      }
    }
    throw new IllegalArgumentException("Header not found: " + label);
  }

  private void setting(Sheet sheet, String category, String item, String value) {
    for (Row row : sheet) {
      if (category.equals(string(row.getCell(1))) && item.equals(string(row.getCell(3)))) {
        set(sheet, row.getRowNum(), 6, value);
        return;
      }
    }
    throw new IllegalArgumentException("Setting not found: " + category + "." + item);
  }

  private String string(Cell cell) {
    return cell != null && cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : "";
  }

  private void set(Sheet sheet, int rowIndex, int columnIndex, String value) {
    Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
    row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(value);
  }

  private String entity(String name) throws IOException {
    return generated("entity/" + name + ".java");
  }

  private String generated(String relativePath) throws IOException {
    return Files.readString(
        dir.resolve("out/app/src/main/java/jp/example/app/base/" + relativePath),
        StandardCharsets.UTF_8);
  }
}
