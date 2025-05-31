package jp.ecuacion.tool.codegenerator.core.blf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.ValidationUtil;
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
import jp.ecuacion.util.poi.excel.table.reader.concrete.StringOneLineHeaderExcelTableToBeanReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * @author 庸介
 */
public class InputFileReadBlf {
  private DetailLogger detailLog = new DetailLogger(this);

  /**
   * 戻り値はsystemMap： systemNameをキーとして、systemごとに振り分けられたxmlMapを値に持つリスト。
   * それぞれのxmlMapには、xmlファイル名をキーとして、そのrooList（SuperRootInfo型）がセットで納められている
   * ※SuperRootInfoには複数の実装がある（enumRootInfoなど）
   */
  public HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> makeFileList(String infoExcelDir)
      throws Exception {

    HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap = new HashMap<>();

    // infoExcelDirが存在しない場合はエラー
    if (!new File(infoExcelDir).exists() || !new File(infoExcelDir).isDirectory()) {
      throw new BizLogicAppException("MSG_ERR_INFO_XML_DIR_NOT_EXIST", infoExcelDir);
    }

    readExcel(infoExcelDir, systemMap);

    // ファイルがなくてもrootInfoは作成しておく処理（必要なもののみ）。個別アプリ（base）以外は不要のため作成しない
    for (Entry<String, HashMap<DataKindEnum, AbstractRootInfo>> entry : systemMap.entrySet()) {
      // boolean isFw =
      // !((SystemCommonRootInfo) systemMap.get(system).get(Constants.XML_POST_FIX_SYSTEM_COMMON))
      // .getProjectType().equals("base");
      // if (isFw) {
      // continue;
      // }

      HashMap<DataKindEnum, AbstractRootInfo> fileMap = systemMap.get(entry.getKey());
      putEmptyRootInfo(fileMap, DataKindEnum.MISC_REMOVED_DATA, new MiscSoftDeleteRootInfo());
      putEmptyRootInfo(fileMap, DataKindEnum.MISC_GROUP, new MiscGroupRootInfo());
      putEmptyRootInfo(fileMap, DataKindEnum.MISC_OPTIMISTIC_LOCK,
          new MiscOptimisticLockRootInfo());
    }

    return systemMap;
  }

  private void readExcel(String infoExcelDir,
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap)
      throws EncryptedDocumentException, IOException, AppException {

    // excelのシート単位とは異なるのだが、元々のxml時代のファイル分け単位に一旦沿って実装
    HashMap<DataKindEnum, AbstractRootInfo> rootInfoMap = null;

    File[] listFiles = new File(infoExcelDir).listFiles();

    for (File file : listFiles) {
      detailLog.info("read excel : " + file.getName());

      // 初期化
      rootInfoMap = new HashMap<>();

      // fileの中身から見てスキップすべきの場合、continue.
      if (shouldSkip(file, "xlsx")) {
        continue;
      }

      // excel読み込み（ここでは純粋な読み込み、各objectへの格納のみ。データ補完は実施なし）
      rootInfoMap.putAll(new ExcelGeneralSettingsReader().readAndGetMap(file.getAbsolutePath()));
      SystemCommonRootInfo sysCmnRootInfo =
          (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);

      // dataType
      String[] headerLabels = new String[] {"DataType名", "型", "長さ最小", "長さ最大", "データパターン（日本語）",
          "データパターン", "禁則文字チェック除外", "正規表現", "パターン説明（デフォルト言語）", "パターン説明（追加言語1）", "パターン説明（追加言語2）",
          "パターン説明（追加言語3）", "最小値", "最大値", "整数部桁数", "小数部桁数", "コードの長さ", "timezoneなし", "備考"};
      rootInfoMap
          .put(DataKindEnum.DATA_TYPE,
              new DataTypeRootInfo(new StringOneLineHeaderExcelTableToBeanReader<DataTypeInfo>(
                  DataTypeInfo.class, "dataType定義", headerLabels, null, 1, null)
                      .readToBean(file.getAbsolutePath())));

      rootInfoMap.putAll(new ExcelEnumReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));
      rootInfoMap.putAll(new ExcelDbReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));
      rootInfoMap
          .putAll(new ExcelDbCommonReader(sysCmnRootInfo).readAndGetMap(file.getAbsolutePath()));

      // まとめてvalidation・同一RootInfo内のデータ補完
      for (AbstractRootInfo rootInfo : rootInfoMap.values()) {
        ValidationUtil.validateThenThrow(rootInfo);
        rootInfo.consistencyCheckAndCoplementData();
      }

      systemMap.put(sysCmnRootInfo.getSystemName(), rootInfoMap);
    }
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
      Logger.log(InputFileReadBlf.class, "MSG_INFO_DIRECTORY_INCLUDED", file.getName());
      return true;

    } else if (!file.getName().endsWith("." + extension)) {
      // xml / excelでない場合はスキップ
      Logger.log(InputFileReadBlf.class, "MSG_INFO_NON_XML_FILE_INCLUDED", file.getName());
      return true;

    } else if (file.getName().startsWith("~$")) {
      // excelの一時ファイルが勝手にこのファイル名でできるのでスキップ
      return true;

    } else {
      return false;
    }
  }
}
