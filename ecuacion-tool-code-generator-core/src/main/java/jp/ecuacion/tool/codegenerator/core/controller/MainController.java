package jp.ecuacion.tool.codegenerator.core.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.blf.CheckAndComplementDataBlf;
import jp.ecuacion.tool.codegenerator.core.blf.GenerationBlf;
import jp.ecuacion.tool.codegenerator.core.blf.ReadExcelFilesBlf;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;

public class MainController {

  /** 
   * Store Info as threadLocal to adapt to multithread accesses.
   */
  public static ThreadLocal<Info> tlInfo = new ThreadLocal<>();

  /**
   * Is the entrypoint of the core module.
   */
  public void execute(String inputDir, String outputDir) throws Exception {

    // Prepare
    Info info = prepare(inputDir, outputDir);

    // Start the excel file unit loop.
    File[] listFiles = new File(inputDir).listFiles();
    for (File file : listFiles) {
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap = null;

      // 1. Read and validate excel formats, and complement data.
      try {
        Logger.log(this, "READ_EXCELS");
        rootInfoMap = new ReadExcelFilesBlf().execute(file);

      } catch (SkipException ex) {
        continue;
      }

      // Put data to info.
      String systemName =
          ((SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON)).getSystemName();
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

  private Info prepare(String inputDir, String outputDir) throws BizLogicAppException {
    // Delete previously created files.
    Logger.log(this, "DELETE_LAST_TIME_FILE");
    delete(new File(outputDir));

    // Throw an exception if the directory does not exist.
    new File(inputDir).mkdirs();
    if (!new File(inputDir).exists() || !new File(inputDir).isDirectory()) {
      throw new BizLogicAppException("MSG_ERR_INFO_XML_DIR_NOT_EXIST", inputDir);
    }

    // Create and set Info.
    Info info = new Info();
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

  public static class SkipException extends Exception {
    private static final long serialVersionUID = 1L;
  }
}
