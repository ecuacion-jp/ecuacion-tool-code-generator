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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.splib.core.util.SplibLogUtil;
import jp.ecuacion.tool.codegenerator.core.blf.CheckAndComplementDataBlf;
import jp.ecuacion.tool.codegenerator.core.blf.GenerationBlf;
import jp.ecuacion.tool.codegenerator.core.blf.ReadExcelFilesBlf;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelGeneralSettingsReader;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
   * <p>{@code inputFiles} accepts a comma-separated list of Excel file paths.
   *
   * @param showFileNameInErrorMessage whether error messages should be prefixed with the source
   *     Excel file name. The CLI can process multiple files in one run, so it needs the file
   *     name to tell them apart ({@code true}); the web app only ever handles the single file
   *     the user just uploaded, so the file name would be redundant noise ({@code false}).
   */
  @SuppressWarnings("null")
  public void execute(String inputFiles, String outputDir, boolean showFileNameInErrorMessage)
      throws Exception {

    List<String> inputFilePaths = Arrays.stream(inputFiles.split(",")).map(String::trim)
        .filter(s -> !s.isEmpty()).collect(Collectors.toList());

    try {
      // Prepare
      CodeGenContext info = prepare(inputFilePaths, outputDir, showFileNameInErrorMessage);

      // Build the list of target Excel files.
      // Dedup by canonical path so the same file specified twice in a comma-separated
      // inputFiles does not get processed (and generated) twice.
      List<File> targetFiles = new ArrayList<>();
      Set<String> targetFileCanonicalPaths = new HashSet<>();
      for (String path : inputFilePaths) {
        File file = new File(path);
        validateInputFile(file);
        if (targetFileCanonicalPaths.add(file.getCanonicalPath())) {
          targetFiles.add(file);
        }
      }

      log.info("Per-file code generation started.");

      // Start the excel file unit loop.
      // Tracks which file first declared each system name, so the same system name defined in
      // multiple excel files (which would otherwise generate into the same output path twice) is
      // rejected instead of silently duplicating generated content.
      Map<String, File> systemNameToFileMap = new HashMap<>();
      for (File file : targetFiles) {
        // 1. Read and validate excel formats, and complement data.
        SplibLogUtil.info(log, "Target file : " + file.getName(), 1);
        int logIndents = 2;
        SplibLogUtil.info(log, "Reading excel file.", logIndents);

        Map<DataKindEnum, AbstractRootInfo> rootInfoMap =
            new ReadExcelFilesBlf().execute(file, info);

        // Put data to info.
        String systemName = Objects
            .requireNonNull((SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON),
                "SYSTEM_COMMON must be populated")
            .getSystemName();

        File existingFile = systemNameToFileMap.putIfAbsent(systemName, file);
        if (existingFile != null) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_SYSTEM_NAME_DEFINED_TWICE",
              systemName, existingFile.getName(), file.getName())).throwIfAny();
        }

        info.setRootInfoUnitValues(systemName, rootInfoMap);

        // 2. Check and complement data
        SplibLogUtil.info(log, "Checking data consistency.", logIndents);
        new CheckAndComplementDataBlf().execute(file, info, systemName, rootInfoMap);

        // 3.generate source
        SplibLogUtil.info(log, "Starting source generation.", logIndents);
        new GenerationBlf(info).execute();

        SplibLogUtil.info(log, "Generation for the file finished.", 1);
      }

      log.info("Process finished successfully.");

    } finally {
      // Prevent the CodeGenContext of this request from being held by the (pooled) worker
      // thread beyond this call, which would otherwise leak memory and mix data across
      // requests handled by the same thread.
      tlInfo.remove();
    }
  }

  private CodeGenContext prepare(List<String> inputFilePaths, String outputDir,
      boolean showFileNameInErrorMessage) {
    // Show current directory.
    log.info("Current directory: " + Paths.get("").toAbsolutePath().toString());

    // Delete previously created files.
    log.info("Deleting the previously generated source files.");
    delete(new File(outputDir));

    // Throw an exception if no input file is specified.
    if (inputFilePaths.isEmpty()) {
      new Violations().add(new BusinessViolation("MSG_ERR_INFO_XML_FILE_NOT_SPECIFIED"))
          .throwIfAny();
    }

    // Create and set Info.
    CodeGenContext info = new CodeGenContext();
    tlInfo.set(info);
    info.outputDir = outputDir;
    info.showFileNameInErrorMessage = showFileNameInErrorMessage;
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

  /**
   * Validates that the given input file is a usable Excel file for this tool.
   *
   * <p>Since the caller now specifies each input file explicitly (rather than this tool scanning
   * a directory for candidates), any file that does not qualify is treated as a configuration
   * error rather than silently skipped.</p>
   */
  private static void validateInputFile(File file) {
    if (!file.exists() || !file.isFile()) {
      new Violations()
          .add(new BusinessViolation("MSG_ERR_INFO_XML_FILE_NOT_EXIST", file.getPath()))
          .throwIfAny();

    } else if (!file.getName().endsWith(".xlsx")) {
      new Violations()
          .add(new BusinessViolation("MSG_ERR_INPUT_FILE_NOT_XLSX", file.getPath()))
          .throwIfAny();

    } else if (file.getName().startsWith("~$")) {
      new Violations()
          .add(new BusinessViolation("MSG_ERR_INPUT_FILE_IS_EXCEL_TEMP_FILE", file.getPath()))
          .throwIfAny();

    } else if (!hasGeneralSettingsSheet(file)) {
      new Violations().add(new BusinessViolation(
          "MSG_ERR_INPUT_FILE_NO_GENERAL_SETTINGS_SHEET", file.getPath(),
          ExcelGeneralSettingsReader.SHEET_NAME_JA, ExcelGeneralSettingsReader.SHEET_NAME_EN))
          .throwIfAny();
    }
  }

  /**
   * Checks whether the given excel file contains a general-settings sheet (JA or EN).
   *
   * <p>Files unrelated to this tool are expected to lack this sheet, or to fail to open as a
   * valid workbook. Both cases are treated as "not a target file" here.</p>
   */
  private static boolean hasGeneralSettingsSheet(File file) {
    try (Workbook wb = WorkbookFactory.create(file, null, true)) {
      return wb.getSheet(ExcelGeneralSettingsReader.SHEET_NAME_JA) != null
          || wb.getSheet(ExcelGeneralSettingsReader.SHEET_NAME_EN) != null;

    } catch (Exception e) {
      log.info("Failed to open the excel file. [File name: " + file.getName() + "]");
      return false;
    }
  }
}
