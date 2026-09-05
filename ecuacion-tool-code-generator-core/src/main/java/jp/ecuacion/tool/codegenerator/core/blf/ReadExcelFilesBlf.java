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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDataKindReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDataTypeReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbCommonReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelEnumReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelGeneralSettingsReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelTableListReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelTemplateLanguageDetector;
import org.apache.poi.EncryptedDocumentException;
import org.jspecify.annotations.Nullable;

/**
 * Reads Excel files and returns parsed data grouped by {@link DataKindEnum}.
 */
public class ReadExcelFilesBlf {

  /**
    * Reads the given Excel file and returns a map from each {@link DataKindEnum} to its
    * corresponding root-info object.
   */
  public Map<DataKindEnum, AbstractRootInfo> execute(File file, CodeGenContext ctx)
      throws Exception {

    // The unit here differs from Excel sheets, but we follow the file-split unit from the
    // original XML era for now
    Map<DataKindEnum, AbstractRootInfo> rootInfoMap = new HashMap<>();

    // Detect template language (JA or EN) by inspecting sheet names
    ExcelTemplateLanguage lang = ExcelTemplateLanguageDetector.detect(file.getAbsolutePath());
    ctx.setExcelLang(lang);

    // Read excel sheets
    List<ExcelDataKindReader> list = List.of(new ExcelGeneralSettingsReader(lang),
        new ExcelDataTypeReader(lang), new ExcelEnumReader(lang), new ExcelDbReader(lang),
        new ExcelDbCommonReader(lang), new ExcelTableListReader(lang));
    for (ExcelDataKindReader reader : list) {
      try {
        putAllWithSheetName(rootInfoMap, reader, file.getAbsolutePath());

      } catch (ViolationException ex) {
        // Messages here come from ecuacion-util-excel-table and already embed the sheet name
        // (see e.g. jp.ecuacion.util.excel.reader.HeaderCellIsBlank), so unlike the other
        // prefixes in this class, only the file name is added here to avoid showing the sheet
        // name twice.
        @Nullable
        Arg prefix =
            ctx.showFileNameInErrorMessage ? Arg.message("MSG_ERR_ABOUT_EXCEL_FILE", file.getName())
                : null;
        ex.getViolations().withMessageParameters(p -> p.messagePrefix(prefix)).throwIfAny();
      }
    }

    // Create rootInfo even when the corresponding file is absent (only for required kinds)
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_REMOVED_DATA, new MiscSoftDeleteRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_GROUP, new MiscGroupRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_OPTIMISTIC_LOCK,
        new MiscOptimisticLockRootInfo());

    // Batch validation and intra-RootInfo data complementation
    for (AbstractRootInfo rootInfo : rootInfoMap.values()) {
      Arg prefix = ctx.excelErrorMessagePrefix(file, rootInfo.getSheetName());
      new Violations().validate(rootInfo).withMessageParameters(
          p -> p.messagePrefix(prefix).representativePropertyPath("fileToUpload")).throwIfAny();

      rootInfo.consistencyCheckAndComplementData();
    }

    return rootInfoMap;
  }

  private void putEmptyRootInfo(Map<DataKindEnum, AbstractRootInfo> fileMap,
      DataKindEnum filePostfix, AbstractRootInfo rootInfo) {
    if (!fileMap.containsKey(filePostfix)) {
      fileMap.put(filePostfix, rootInfo);
    }
  }

  /**
   * Reads {@code reader}, tags every resulting root info with its sheet name, then merges it into
   * {@code target}.
   */
  private void putAllWithSheetName(Map<DataKindEnum, AbstractRootInfo> target,
      ExcelDataKindReader reader, String excelPath) throws EncryptedDocumentException, IOException {
    Map<DataKindEnum, AbstractRootInfo> source = reader.readAndGetMap(excelPath);
    for (AbstractRootInfo rootInfo : source.values()) {
      rootInfo.setSheetName(reader.getSheetName());
    }
    target.putAll(source);
  }

}
