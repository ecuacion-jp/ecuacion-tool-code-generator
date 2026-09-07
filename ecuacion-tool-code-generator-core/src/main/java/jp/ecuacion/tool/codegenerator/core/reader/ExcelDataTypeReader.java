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
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableToBeanReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * Reads the dataType definition sheet and builds a {@link DataTypeRootInfo}.
 *
 * <p>Unlike the other readers in this package, dataType rows map one-to-one onto {@link
 * DataTypeInfo} beans with no grouping, so this simply wraps the generic
 * {@link StringOneLineHeaderExcelTableToBeanReader}.
 */
public class ExcelDataTypeReader implements ExcelDataKindReader {

  private final StringOneLineHeaderExcelTableToBeanReader<DataTypeInfo> reader;

  /** Constructs an instance that targets the dataType definition sheet for the given language. */
  public ExcelDataTypeReader(ExcelTemplateLanguage lang) {
    String sheetName =
        lang == ExcelTemplateLanguage.JA ? DataTypeInfo.SHEET_NAME_JA : DataTypeInfo.SHEET_NAME_EN;
    String[] headerLabels = (lang == ExcelTemplateLanguage.JA ? DataTypeInfo.HEADER_LABELS_JA
        : DataTypeInfo.HEADER_LABELS_EN).toArray(new String[0]);
    reader = new StringOneLineHeaderExcelTableToBeanReader<>(DataTypeInfo.class, sheetName,
        headerLabels);
  }

  /** Reads the Excel file at the given path and returns a data-kind-to-root-info map. */
  @Override
  public Map<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    Map<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    rtnMap.put(DataKindEnum.DATA_TYPE, new DataTypeRootInfo(reader.readToBean(excelPath, false)));
    return rtnMap;
  }

  @Override
  public String getSheetName() {
    return reader.getSheetName();
  }
}
