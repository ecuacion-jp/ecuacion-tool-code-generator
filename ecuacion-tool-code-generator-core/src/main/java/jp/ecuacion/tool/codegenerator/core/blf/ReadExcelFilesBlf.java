package jp.ecuacion.tool.codegenerator.core.blf;

import jakarta.validation.Validation;
import java.io.File;
import java.util.HashMap;
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
  public HashMap<DataKindEnum, AbstractRootInfo> execute(File file) throws Exception {

    detailLog.info("read excel : " + file.getName());

    // fileの中身から見てスキップすべきの場合、continue.
    if (shouldSkip(file, "xlsx")) {
      throw new SkipException();
    }

    // excelのシート単位とは異なるのだが、元々のxml時代のファイル分け単位に一旦沿って実装
    HashMap<DataKindEnum, AbstractRootInfo> rootInfoMap = null;

    // 初期化
    rootInfoMap = new HashMap<>();

    // excel読み込み（ここでは純粋な読み込み、各objectへの格納のみ。データ補完は実施なし）
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

    // まとめてvalidation・同一RootInfo内のデータ補完
    for (AbstractRootInfo rootInfo : rootInfoMap.values()) {
      new Violations()
          .addAll(Validation.buildDefaultValidatorFactory().getValidator().validate(rootInfo))
          .throwIfAny();
      rootInfo.consistencyCheckAndCoplementData();
    }

    // ファイルがなくてもrootInfoは作成しておく処理（必要なもののみ）
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_REMOVED_DATA, new MiscSoftDeleteRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_GROUP, new MiscGroupRootInfo());
    putEmptyRootInfo(rootInfoMap, DataKindEnum.MISC_OPTIMISTIC_LOCK,
        new MiscOptimisticLockRootInfo());

    return rootInfoMap;
  }

  private void putEmptyRootInfo(HashMap<DataKindEnum, AbstractRootInfo> fileMap,
      DataKindEnum filePostfix, AbstractRootInfo rootInfo) {
    if (!fileMap.containsKey(filePostfix)) {
      fileMap.put(filePostfix, rootInfo);
    }
  }

  private boolean shouldSkip(File file, String extension) {
    // ディレクトリの場合はスキップ
    if (file.isDirectory()) {
      Logger.log(ReadExcelFilesBlf.class, "MSG_INFO_DIRECTORY_INCLUDED", file.getName());
      return true;

    } else if (!file.getName().endsWith("." + extension)) {
      // xml / excelでない場合はスキップ
      Logger.log(ReadExcelFilesBlf.class, "MSG_INFO_NON_XML_FILE_INCLUDED", file.getName());
      return true;

    } else if (file.getName().startsWith("~$")) {
      // excelの一時ファイルが勝手にこのファイル名でできるのでスキップ
      return true;

    } else {
      return false;
    }
  }
}
