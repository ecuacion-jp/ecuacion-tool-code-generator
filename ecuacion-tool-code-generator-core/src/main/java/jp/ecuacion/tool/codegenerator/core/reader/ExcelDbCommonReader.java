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
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import org.apache.poi.EncryptedDocumentException;

/**
 * Reads the common DB column definition sheet and converts it into a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public class ExcelDbCommonReader extends ExcelAbstractDbOrClassReader {

  private static final String SHEET_NAME_JA = "DB共通項目定義";
  private static final String SHEET_NAME_EN = "DB Common Item Definition";

  /** Constructs an instance
   *     that targets the common DB item-definition sheet for the given language. */
  public ExcelDbCommonReader(ExcelTemplateLanguage lang) {
    super(lang == ExcelTemplateLanguage.JA ? SHEET_NAME_JA : SHEET_NAME_EN, DataKindEnum.DB_COMMON,
        lang);
  }

  @Override
  protected String resolveTableName(String rawTableName) {
    return (rawTableName == null || rawTableName.isEmpty()) ? "SYSTEM_COMMON" : rawTableName;
  }

  /**
   * Reads the Excel file, guaranteeing the returned {@code DbOrClassRootInfo.tableList} always
   * holds exactly one entry (a column-less "SYSTEM_COMMON" placeholder when the sheet has no
   * rows), so downstream code can always rely on {@code tableList.get(0)} being safe.
   */
  @Override
  public Map<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {
    Map<DataKindEnum, AbstractRootInfo> rtnMap = super.readAndGetMap(excelPath);
    DbOrClassRootInfo rootInfo =
        Objects.requireNonNull((DbOrClassRootInfo) rtnMap.get(DataKindEnum.DB_COMMON));
    if (rootInfo.tableList.isEmpty()) {
      rootInfo.tableList.add(new DbOrClassTableInfo("SYSTEM_COMMON"));
    }

    return rtnMap;
  }
}
