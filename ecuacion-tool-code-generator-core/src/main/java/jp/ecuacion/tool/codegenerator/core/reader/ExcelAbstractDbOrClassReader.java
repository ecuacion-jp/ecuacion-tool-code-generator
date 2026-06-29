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
package jp.ecuacion.tool.codegenerator.core.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * Abstract reader that parses a DB or class specification sheet and builds a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public abstract class ExcelAbstractDbOrClassReader extends StringOneLineHeaderExcelTableReader {

  private static int COL_TABLE_NAME = 0;

  private SystemCommonRootInfo sysCmnRootInfo;
  // private StringUtil strUtil = new StringUtil();
  private DataKindEnum fileKind;

  private static final String[] HEADER_LABELS_JA =
      new String[] {"テーブル名", "カラム名", "dataType", "dataType存在確認", "javaのみ", "Surrogate key ",
          "natural key (unique key)", "nullable",
          "自動採番", "強制採番", "自動更新", "強制更新", "グループ識別項目", "SPRING監査", "関連：種類", "関連：direction",
          "関連：参照元変数名", "関連：参照元オブジェクト変数名", "関連：参照先テーブル", "関連：参照先カラム", "関連：参照先変数名", "関連：eager",
          "index1", "index2", "index3", "index4", "index5", "index6", "index7", "index8", "index9",
          "index10", "備考", "カラム表示名（デフォルト言語）", "カラム表示名（追加言語1）", "カラム表示名（追加言語2）",
          "カラム表示名（追加言語3）"};

  private static final String[] HEADER_LABELS_EN =
      new String[] {"Table Name", "Column Name", "dataType", "dataType Check", "Java Only",
          "Surrogate key ", "natural key (unique key)",
          "nullable", "Auto Numbering", "Force Numbering", "Auto Update", "Force Update",
          "Group Identifier", "Spring Audit", "Relation: Type", "Relation: Direction",
          "Relation: Source Var Name", "Relation: Source Object Var Name", "Relation: Target Table",
          "Relation: Target Column", "Relation: Target Var Name", "Relation: Eager", "index1",
          "index2", "index3", "index4", "index5", "index6", "index7", "index8", "index9", "index10",
          "Notes", "Column Display Name (Default Lang)", "Column Display Name (Additional Lang 1)",
          "Column Display Name (Additional Lang 2)", "Column Display Name (Additional Lang 3)"};

  /** Constructs an instance for the given sheet name, data kind, and system-common root info. */
  public ExcelAbstractDbOrClassReader(String sheetName, DataKindEnum fileKind,
      SystemCommonRootInfo systemCommonRootInfo, ExcelTemplateLanguage lang) {
    super(sheetName, lang == ExcelTemplateLanguage.JA ? HEADER_LABELS_JA : HEADER_LABELS_EN);
    this.fileKind = fileKind;
    sysCmnRootInfo = systemCommonRootInfo;
  }

  /** Reads the Excel file at the given path and returns a data-kind-to-root-info map. */
  public Map<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    Map<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    DbOrClassRootInfo rootInfo = new DbOrClassRootInfo(fileKind);
    rtnMap.put(fileKind, rootInfo);

    // Retrieve table data in list form
    List<List<String>> rowList = read(excelPath);

    // Create a map of enumClass entries for easier handling
    Map<String, DbOrClassTableInfo> existingTableMap = new HashMap<>();

    for (List<String> colList : rowList) {
      String tableName = resolveTableName(colList.get(COL_TABLE_NAME));
      if (!existingTableMap.containsKey(tableName)) {
        existingTableMap.put(tableName, new DbOrClassTableInfo(tableName));
        rootInfo.tableList.add(existingTableMap.get(tableName));
      }

      DbOrClassTableInfo info = java.util.Objects.requireNonNull(existingTableMap.get(tableName),
          "Table info just inserted into existingTableMap must be present");
      info.columnList.add(new DbOrClassColumnInfo(colList, sysCmnRootInfo.getDefaultLang(),
          sysCmnRootInfo.getSupportLang1(), sysCmnRootInfo.getSupportLang2(),
          sysCmnRootInfo.getSupportLang3()));
    }

    return rtnMap;
  }

  /** 
   * Returns the table name to use for grouping rows. 
   *     Subclasses may override to supply defaults.
   */
  protected String resolveTableName(String rawTableName) {
    return rawTableName;
  }
}
