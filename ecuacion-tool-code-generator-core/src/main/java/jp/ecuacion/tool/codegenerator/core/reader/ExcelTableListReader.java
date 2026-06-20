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
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.TableListInfo;
import jp.ecuacion.tool.codegenerator.core.dto.TableListRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * Reads the table-list sheet and builds a {@link TableListRootInfo}.
 */
public class ExcelTableListReader extends StringOneLineHeaderExcelTableReader {

  private static final String SHEET_NAME_JA = "テーブル一覧";
  private static final String SHEET_NAME_EN = "Table List";

  private static final String[] HEADER_LABELS_JA =
      new String[] {"テーブル名", "テーブル表示名（デフォルト言語）", "テーブル表示名（追加言語1）",
          "テーブル表示名（追加言語2）", "テーブル表示名（追加言語3）"};

  private static final String[] HEADER_LABELS_EN =
      new String[] {"Table Name", "Table Display Name (Default Lang)",
          "Table Display Name (Additional Lang 1)", "Table Display Name (Additional Lang 2)",
          "Table Display Name (Additional Lang 3)"};

  private final SystemCommonRootInfo sysCmnRootInfo;

  /** Constructs an instance that targets the table-list sheet for the given language. */
  public ExcelTableListReader(SystemCommonRootInfo sysCmnRootInfo, ExcelTemplateLanguage lang) {
    super(lang == ExcelTemplateLanguage.JA ? SHEET_NAME_JA : SHEET_NAME_EN,
        lang == ExcelTemplateLanguage.JA ? HEADER_LABELS_JA : HEADER_LABELS_EN);
    this.sysCmnRootInfo = sysCmnRootInfo;
  }

  /** Reads the Excel file at the given path and returns a data-kind-to-root-info map. */
  public Map<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    Map<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    TableListRootInfo rootInfo = new TableListRootInfo();
    rtnMap.put(DataKindEnum.TABLE_LIST, rootInfo);

    List<List<String>> rowList = read(excelPath);
    for (List<String> colList : rowList) {
      rootInfo.tableList.add(new TableListInfo(colList, sysCmnRootInfo));
    }

    return rtnMap;
  }
}
