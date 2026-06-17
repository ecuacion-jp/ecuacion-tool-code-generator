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
package jp.ecuacion.tool.codegenerator.core.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.blf.CheckAndComplementDataBlf;
import jp.ecuacion.tool.codegenerator.core.blf.GenerationBlf;
import jp.ecuacion.tool.codegenerator.core.blf.ReadExcelFilesBlf;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;

/**
 * Entry controller that drives the code generation pipeline: reads Excel files, validates,
 * complements, and generates source code.
 */
public class MainController {

  /** 
   * Store Info as threadLocal to adapt to multithread accesses.
   */
  public static ThreadLocal<CodeGenContext> tlInfo = new ThreadLocal<>();

  /**
   * Is the entrypoint of the core module.
   */
  public void execute(String inputDir, String outputDir) throws Exception {

    // Prepare
    CodeGenContext info = prepare(inputDir, outputDir);

    // Build the list of target Excel files, logging skipped files along the way.
    List<File> targetFiles = new ArrayList<>();
    for (File file : new File(inputDir).listFiles()) {
      if (!shouldSkip(file, "xlsx")) {
        targetFiles.add(file);
      }
    }

    if (targetFiles.isEmpty()) {
      Logger.log(this, "MSG_WRN_NO_TARGET_EXCEL_FILE", inputDir);
      return;
    }

    // Start the excel file unit loop.
    for (File file : targetFiles) {
      // 1. Read and validate excel formats, and complement data.
      Logger.log(this, "READ_EXCELS");
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap = new ReadExcelFilesBlf().execute(file, info);

      // Put data to info.
      String systemName =
          java.util.Objects.requireNonNull(
              (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON),
              "SYSTEM_COMMON must be populated").getSystemName();
      info.setRootInfoUnitValues(systemName, rootInfoMap);

      // 2. Check and complement data
      Logger.log(this, "CHECK_AND_COMPLEMENT_DATA");
      // Map<String, DataTypeInfo> dtMap =
      new CheckAndComplementDataBlf().execute(info, systemName, rootInfoMap);

      // 3.generate source
      Logger.log(this, "GEN_SOURCE_START");
      new GenerationBlf(info).execute();
    }
  }

  private CodeGenContext prepare(String inputDir, String outputDir) {
    // Delete previously created files.
    Logger.log(this, "DELETE_LAST_TIME_FILE");
    delete(new File(outputDir));

    // Throw an exception if the directory does not exist.
    new File(inputDir).mkdirs();
    if (!new File(inputDir).exists() || !new File(inputDir).isDirectory()) {
      new Violations().add(new BusinessViolation("MSG_ERR_INFO_XML_DIR_NOT_EXIST", inputDir))
          .throwIfAny();
    }

    // Create and set Info.
    CodeGenContext info = new CodeGenContext();
    tlInfo.set(info);
    info.outputDir = outputDir;
    return info;
  }

  /**
   * Delete all the directories using recursive procedure.
   */
  public static void delete(File f) {
    // Exit if file or directory do not exist.
    if (f.exists() == false) {
      return;
    }

    // Delete if file exists.
    if (f.isFile()) {
      f.delete();
    }

    // Delete all the files and directories if directory exists.
    if (f.isDirectory()) {
      Arrays.asList(f.listFiles()).forEach(file -> delete(file));
      // Delete self finally.
      f.delete();
    }
  }

  private static boolean shouldSkip(File file, String extension) {
    if (file.isDirectory()) {
      Logger.log(MainController.class, "MSG_INFO_DIRECTORY_INCLUDED", file.getName());
      return true;
    } else if (!file.getName().endsWith("." + extension)) {
      Logger.log(MainController.class, "MSG_INFO_NON_XML_FILE_INCLUDED", file.getName());
      return true;
    } else if (file.getName().startsWith("~$")) {
      return true;
    } else {
      return false;
    }
  }
}
