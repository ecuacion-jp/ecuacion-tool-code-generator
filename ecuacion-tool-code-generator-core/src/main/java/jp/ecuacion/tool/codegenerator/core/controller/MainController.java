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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.blf.CheckAndComplementDataBlf;
import jp.ecuacion.tool.codegenerator.core.blf.GenerationBlf;
import jp.ecuacion.tool.codegenerator.core.blf.ReadExcelFilesBlf;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Entry controller that drives the code generation pipeline: reads Excel files, validates,
 * complements, and generates source code.
 */
public class MainController {

  private static final DetailLogger log = new DetailLogger(MainController.class);

  /**
   * Store Info as threadLocal to adapt to multithread accesses.
   */
  public static ThreadLocal<CodeGenContext> tlInfo = new ThreadLocal<>();

  /**
   * Is the entrypoint of the core module.
   *
   * <p>{@code inputDir} accepts a comma-separated list of directories.
   */
  public void execute(String inputDir, String outputDir) throws Exception {

    List<String> inputDirs = Arrays.stream(inputDir.split(",")).map(String::trim)
        .filter(s -> !s.isEmpty()).collect(Collectors.toList());

    // Prepare
    CodeGenContext info = prepare(inputDirs, outputDir);

    // Build the list of target Excel files from all input directories.
    List<File> targetFiles = new ArrayList<>();
    for (String dir : inputDirs) {
      for (File file : new File(dir).listFiles()) {
        if (!shouldSkip(file, "xlsx")) {
          targetFiles.add(file);
        }
      }
    }

    if (targetFiles.isEmpty()) {
      log.info("Warning: No target Excel files found in the input directory. [Directory: "
          + inputDir + "]");
      return;
    }

    // Start the excel file unit loop.
    for (File file : targetFiles) {
      // 1. Read and validate excel formats, and complement data.

      log.info("==========");
      log.info("[" + file.getName() + "]");
      log.info("Reading excel file.");
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap = new ReadExcelFilesBlf().execute(file, info);

      // Put data to info.
      String systemName =
          Objects.requireNonNull((SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON),
              "SYSTEM_COMMON must be populated").getSystemName();
      info.setRootInfoUnitValues(systemName, rootInfoMap);

      // 2. Check and complement data
      log.info("Checking data consistency.");
      // Map<String, DataTypeInfo> dtMap =
      new CheckAndComplementDataBlf().execute(info, systemName, rootInfoMap);

      // 3.generate source
      log.info("Starting source generation.");
      new GenerationBlf(info).execute();
    }
  }

  private CodeGenContext prepare(List<String> inputDirs, String outputDir) {
    // Show current directory.
    log.info("Current directory: " + Paths.get("").toAbsolutePath().toString());

    // Delete previously created files.
    log.info("Deleting the previously generated source files.");
    delete(new File(outputDir));

    // Throw an exception if any directory does not exist.
    for (String dir : inputDirs) {
      if (!new File(dir).exists() || !new File(dir).isDirectory()) {
        new Violations().add(new BusinessViolation("MSG_ERR_INFO_XML_DIR_NOT_EXIST", dir))
            .throwIfAny();
      }
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
      log.info("A directory is included in the XML directory. Skipping. [Directory name: "
          + file.getName() + "]");
      return true;
    } else if (!file.getName().endsWith("." + extension)) {
      log.info("A non-XML file is included in the XML directory. Skipping. [File name: "
          + file.getName() + "]");
      return true;
    } else if (file.getName().startsWith("~$")) {
      return true;
    } else {
      return false;
    }
  }
}
