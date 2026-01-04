package jp.ecuacion.tool.codegenerator.core.blf;

import java.io.File;
import java.util.HashMap;
import jp.ecuacion.lib.core.util.ValidationUtil;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbCommonReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelDbReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelEnumReader;
import jp.ecuacion.tool.codegenerator.core.reader.ExcelGeneralSettingsReader;
import jp.ecuacion.util.poi.excel.table.reader.concrete.StringOneLineHeaderExcelTableToBeanReader;

/**
 * Reads Excel Format and returns read data.
 */
public class ExcelFormatReadBlf {

  /**
   * 戻り値はsystemMap： systemNameをキーとして、systemごとに振り分けられたxmlMapを値に持つリスト。
   * それぞれのxmlMapには、xmlファイル名をキーとして、そのrooList（SuperRootInfo型）がセットで納められている
   * ※SuperRootInfoには複数の実装がある（enumRootInfoなど）
   */
  public HashMap<DataKindEnum, AbstractRootInfo> read(File file) throws Exception {

    // excelのシート単位とは異なるのだが、元々のxml時代のファイル分け単位に一旦沿って実装
    HashMap<DataKindEnum, AbstractRootInfo> rootInfoMap = null;

    // 初期化
    rootInfoMap = new HashMap<>();

    // excel読み込み（ここでは純粋な読み込み、各objectへの格納のみ。データ補完は実施なし）
    rootInfoMap.putAll(new ExcelGeneralSettingsReader().readAndGetMap(file.getAbsolutePath()));
    SystemCommonRootInfo sysCmnRootInfo =
        (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);

    // dataType
    rootInfoMap.put(DataKindEnum.DATA_TYPE,
        new DataTypeRootInfo(new StringOneLineHeaderExcelTableToBeanReader<DataTypeInfo>(
            DataTypeInfo.class, "dataType定義", DataTypeInfo.HEADER_LABELS, null, 1, null)
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

    // ファイルがなくてもrootInfoは作成しておく処理（必要なもののみ）。個別アプリ（base）以外は不要のため作成しない
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
}
