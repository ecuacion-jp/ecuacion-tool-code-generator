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

import jakarta.validation.Validation;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.TableListRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbCommonReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelEnumReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelGeneralSettingsReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelTableListReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelTemplateLanguageDetector;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableToBeanReader;

/**
 * Reads Excel files and returns parsed data grouped by {@link DataKindEnum}.
 */
public class ReadExcelFilesBlf {

  private DetailLogger detailLog = new DetailLogger(this);

  /**
    * Reads the given Excel file and returns a map from each {@link DataKindEnum} to its
    * corresponding root-info object.
   */
  public Map<DataKindEnum, AbstractRootInfo> execute(File file, CodeGenContext ctx)
      throws Exception {

    detailLog.info("read excel : " + file.getName());

    // The unit here differs from Excel sheets, but we follow the file-split unit from the
    // original XML era for now
    Map<DataKindEnum, AbstractRootInfo> rootInfoMap = new HashMap<>();

    // Detect template language (JA or EN) by inspecting sheet names
    ExcelTemplateLanguage lang = ExcelTemplateLanguageDetector.detect(file.getAbsolutePath());
    ctx.setExcelLang(lang);

    // Read Excel (pure reading and storing into objects only; no data complementation here)
    rootInfoMap.putAll(new ExcelGeneralSettingsReader(lang).readAndGetMap(file.getAbsolutePath()));
    SystemCommonRootInfo sysCmnRootInfo = java.util.Objects.requireNonNull(
        (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON),
        "SYSTEM_COMMON must be populated by ExcelGeneralSettingsReader");

    // dataType
    String dataTypeSheetName =
        lang == ExcelTemplateLanguage.JA ? DataTypeInfo.SHEET_NAME_JA : DataTypeInfo.SHEET_NAME_EN;
    String[] dataTypeHeaders = (lang == ExcelTemplateLanguage.JA ? DataTypeInfo.HEADER_LABELS_JA
        : DataTypeInfo.HEADER_LABELS_EN).toArray(new String[0]);
    rootInfoMap.put(DataKindEnum.DATA_TYPE,
        new DataTypeRootInfo(
            new StringOneLineHeaderExcelTableToBeanReader<DataTypeInfo>(DataTypeInfo.class,
                dataTypeSheetName, dataTypeHeaders).readToBean(file.getAbsolutePath())));

    rootInfoMap
        .putAll(new ExcelEnumReader(sysCmnRootInfo, lang).readAndGetMap(file.getAbsolutePath()));
    rootInfoMap
        .putAll(new ExcelDbReader(sysCmnRootInfo, lang).readAndGetMap(file.getAbsolutePath()));
    rootInfoMap.putAll(
        new ExcelDbCommonReader(sysCmnRootInfo, lang).readAndGetMap(file.getAbsolutePath()));

    try {
      rootInfoMap.putAll(
          new ExcelTableListReader(sysCmnRootInfo, lang).readAndGetMap(file.getAbsolutePath()));
    } catch (Exception e) {
      // テーブル一覧 sheet is absent in older Excel templates; skip silently
    }
    putEmptyRootInfo(rootInfoMap, DataKindEnum.TABLE_LIST, new TableListRootInfo());

    // Batch validation and intra-RootInfo data complementation
    for (AbstractRootInfo rootInfo : rootInfoMap.values()) {
      new Violations()
          .addAll(Validation.buildDefaultValidatorFactory().getValidator().validate(rootInfo))
          .throwIfAny();
      rootInfo.consistencyCheckAndCoplementData();
    }

    // Create rootInfo even when the corresponding file is absent (only for required kinds)
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_REMOVED_DATA, new MiscSoftDeleteRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_GROUP, new MiscGroupRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_OPTIMISTIC_LOCK,
        new MiscOptimisticLockRootInfo());

    return rootInfoMap;
  }

  private void putEmptyRootInfo(Map<DataKindEnum, AbstractRootInfo> fileMap,
      DataKindEnum filePostfix, AbstractRootInfo rootInfo) {
    if (!fileMap.containsKey(filePostfix)) {
      fileMap.put(filePostfix, rootInfo);
    }
  }

}
