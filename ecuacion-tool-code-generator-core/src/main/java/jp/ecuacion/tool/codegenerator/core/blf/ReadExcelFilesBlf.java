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
import jp.ecuacion.tool.codegenerator.core.controller.MainController.SkipException;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbCommonReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelEnumReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelGeneralSettingsReader;
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
  public Map<DataKindEnum, AbstractRootInfo> execute(File file) throws Exception {

    detailLog.info("read excel : " + file.getName());

    // Skip if the file content indicates it should be skipped
    if (shouldSkip(file, "xlsx")) {
      throw new SkipException();
    }

    // The unit here differs from Excel sheets, but we follow the file-split unit from the
    // original XML era for now
    Map<DataKindEnum, AbstractRootInfo> rootInfoMap = new HashMap<>();

    // Read Excel (pure reading and storing into objects only; no data complementation here)
    rootInfoMap.putAll(new ExcelGeneralSettingsReader().readAndGetMap(file.getAbsolutePath()));
    SystemCommonRootInfo sysCmnRootInfo =
        java.util.Objects.requireNonNull(
            (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON),
            "SYSTEM_COMMON must be populated by ExcelGeneralSettingsReader");

    // dataType
    rootInfoMap.put(DataKindEnum.DATA_TYPE,
        new DataTypeRootInfo(new StringOneLineHeaderExcelTableToBeanReader<DataTypeInfo>(
            DataTypeInfo.class, "dataType定義", DataTypeInfo.HEADER_LABELS)
                .readToBean(file.getAbsolutePath())));

    rootInfoMap.putAll(new ExcelEnumReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));
    rootInfoMap.putAll(new ExcelDbReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));
    rootInfoMap
        .putAll(new ExcelDbCommonReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));

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

  private boolean shouldSkip(File file, String extension) {
    // Skip directories
    if (file.isDirectory()) {
      Logger.log(ReadExcelFilesBlf.class, "MSG_INFO_DIRECTORY_INCLUDED", file.getName());
      return true;

    } else if (!file.getName().endsWith("." + extension)) {
      // Skip files that are not xml / excel
      Logger.log(ReadExcelFilesBlf.class, "MSG_INFO_NON_XML_FILE_INCLUDED", file.getName());
      return true;

    } else if (file.getName().startsWith("~$")) {
      // Skip Excel temporary files that are automatically created with this naming pattern
      return true;

    } else {
      return false;
    }
  }
}
