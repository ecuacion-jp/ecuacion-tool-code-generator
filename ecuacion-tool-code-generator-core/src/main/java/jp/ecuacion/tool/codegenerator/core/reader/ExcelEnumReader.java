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
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * Reads the enum definition sheet from the Excel file and builds an {@link
 * jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo}.
 */
public class ExcelEnumReader extends StringOneLineHeaderExcelTableReader {

  private static final String SHEET_NAME_JA = "enum定義";
  private static final String SHEET_NAME_EN = "Enum Definition";

  private static int COL_DATA_TYPE_NAME = 0;

  private SystemCommonRootInfo sysCmnRootInfo;

  private static final String[] HEADER_LABELS_JA =
      new String[] {"DataType名", "code", "varName", "javaのみ", "備考", "表示名（デフォルト言語）",
          "表示名（追加言語1）", "表示名（追加言語2）", "表示名（追加言語3）"};

  private static final String[] HEADER_LABELS_EN = new String[] {"DataType Name", "code", "varName",
      "Java Only", "Notes", "Display Name (Default Lang)", "Display Name (Additional Lang 1)",
      "Display Name (Additional Lang 2)", "Display Name (Additional Lang 3)"};

  /** Constructs an instance that targets the enum definition sheet for the given language. */
  public ExcelEnumReader(SystemCommonRootInfo sysCmnRootInfo, ExcelTemplateLanguage lang) {
    super(lang == ExcelTemplateLanguage.JA ? SHEET_NAME_JA : SHEET_NAME_EN,
        lang == ExcelTemplateLanguage.JA ? HEADER_LABELS_JA : HEADER_LABELS_EN);
    this.sysCmnRootInfo = sysCmnRootInfo;
  }

  /** Reads the Excel file at the given path and returns a data-kind-to-root-info map. */
  public Map<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    Map<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    EnumRootInfo rootInfo = new EnumRootInfo();
    rtnMap.put(DataKindEnum.ENUM, rootInfo);

    // Retrieve table data in list form
    List<List<String>> rowList = read(excelPath);

    // Create a map of enumClass entries for easier handling
    Map<String, EnumClassInfo> existingEnumClassMap = new HashMap<>();

    for (List<String> colList : rowList) {
      if (!existingEnumClassMap.containsKey(colList.get(COL_DATA_TYPE_NAME))) {
        existingEnumClassMap.put(colList.get(COL_DATA_TYPE_NAME), new EnumClassInfo(colList));
        rootInfo.enumClassList.add(existingEnumClassMap.get(colList.get(COL_DATA_TYPE_NAME)));
      }

      EnumClassInfo info = java.util.Objects.requireNonNull(
          existingEnumClassMap.get(colList.get(COL_DATA_TYPE_NAME)),
          "EnumClassInfo just inserted into existingEnumClassMap must be present");
      info.enumList.add(new EnumValueInfo(colList, sysCmnRootInfo));
    }

    return rtnMap;
  }
}
