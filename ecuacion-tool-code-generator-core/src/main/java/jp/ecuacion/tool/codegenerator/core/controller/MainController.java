package jp.ecuacion.tool.codegenerator.core.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.blf.ExcelFormatReadBlf;
import jp.ecuacion.tool.codegenerator.core.blf.GenerationBlf;
import jp.ecuacion.tool.codegenerator.core.checker.FileLevelConsistencyChecker;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;
import jp.ecuacion.tool.codegenerator.core.preparer.PrepareManager;
import org.apache.commons.lang3.StringUtils;

public class MainController {

  /** 
   * Store Info as threadLocal to adapt to multithread accesses.
   */
  public static ThreadLocal<Info> tlInfo = new ThreadLocal<>();

  /**
   * Is the entrypoint of the core module.
   * 
   * <p>The flow of it is as follows:<br>
   * 1. Read and validate excel formats, and complement data.<br>
   * 2. Check data by compare multiple RootInfos.<br>
   * 3. Generate source.
   * </p>
   */
  public void execute(String inputDir, String outputDir) throws Exception {

    // Delete previously created files.
    Logger.log(this, "DELETE_LAST_TIME_FILE");
    delete(new File(outputDir));

    // Create and set Info.
    Info info = new Info();
    tlInfo.set(info);
    info.inputDir = inputDir;
    info.outputDir = outputDir;

    // 1. Read and validate excel formats, and complement data.
    Logger.log(this, "READ_EXCELS");
    HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap =
        new ExcelFormatReadBlf().read(info.inputDir);
    info.setCommonUnitValues(systemMap);

    // 2. Check data
    Logger.log(this, "CHECK_AND_COMPLEMENT_DATA");
    checksAndComplements(systemMap);

    // dataTypeInfoのcolInfoへの詰め込み
    HashMap<String, HashMap<String, DataTypeInfo>> allDtMap = createAllDataTypeMap(systemMap);
    // dataTypeInfoのcolInfoへの詰め込み（指定したdataTypeNameに対するdataTypeInfoの存在チェックも合わせて実施）
    putDataTypeInfoIntoColInfo(info, systemMap, allDtMap);

    // 3.generate source
    Logger.log(this, "GEN_SOURCE_START");
    // システム別にループを回して生成
    generateSource(info, systemMap, outputDir);
  }

  private void generateSource(Info info,
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap, String outputDir)
      throws Exception {

    for (String sysName : systemMap.keySet()) {
      final GenerationBlf genCon = new GenerationBlf(info, outputDir);

      // Infoに値を格納
      info.setRootInfoUnitValues(sysName);

      // 複数ファイル間でのデータチェックとデータ整理
      new PrepareManager().prepare();

      // 1システムについても複数パターンの生成が必要な場合があるので、パターンを配列で持ち、それをループで実行する形をとる
      List<GeneratePtnEnum> arr = new ArrayList<>();

      if (shouldMakeNoGroupQuery(info)) {
        if (shouldMakeNoGroupQueryForDaoOnly(info)) {
          arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NORMAL);
          arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NO_GROUP_QUERY);

        } else {
          // グループ指定なしqueryパターンで生成
          arr.add(GeneratePtnEnum.NORMAL);
          arr.add(GeneratePtnEnum.NO_GROUP_QUERY);
        }

      } else {
        arr.add(GeneratePtnEnum.NORMAL);
      }

      // 通常は1システム1パターンだが、複数になる場合は複数に分けて生成
      for (GeneratePtnEnum anEnum : arr) {
        info.setGenPtn(anEnum);
        genCon.controlGenerators();
      }
    }
  }

  private void checksAndComplements(
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap) throws AppException {

    for (Entry<String, HashMap<DataKindEnum, AbstractRootInfo>> entry : systemMap.entrySet()) {
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap = systemMap.get(entry.getKey());
      final SystemCommonRootInfo systemCommon =
          (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);

      // 複数RootInfoの中で、RootInfoの存在有無の整合性チェックと補完
      new FileLevelConsistencyChecker().check(entry.getKey(), systemMap);

      // inside tables
      checkForChildTable(systemCommon.getSystemName(),
          (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

      // tableの親子間
      checkAndComplementForParentAndChildTable(
          (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
          (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

      // tableとgroup間
      checkAndComplementForTableAndGroup(entry.getKey(),
          (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
          (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB),
          (MiscGroupRootInfo) rootInfoMap.get(DataKindEnum.MISC_GROUP));

      // dataType
      ((DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE)).dataTypeList
          .forEach(dt -> dt.checksAndComplements(systemCommon));
    }
  }

  private void putDataTypeInfoIntoColInfo(Info info,
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap,
      HashMap<String, HashMap<String, DataTypeInfo>> allDtMap) throws BizLogicAppException {

    for (Entry<String, HashMap<DataKindEnum, AbstractRootInfo>> entry : systemMap.entrySet()) {
      HashMap<DataKindEnum, AbstractRootInfo> rootInfoMap = systemMap.get(entry.getKey());
      HashMap<String, DataTypeInfo> dtMap = allDtMap.get(entry.getKey());

      for (AbstractRootInfo rootInfo : rootInfoMap.values()) {

        if (rootInfo instanceof DbOrClassRootInfo) {
          // DbOrClassRootInfo
          for (DbOrClassTableInfo ti : ((DbOrClassRootInfo) rootInfo).tableList) {
            for (DbOrClassColumnInfo ci : ti.columnList) {
              ci.setDtInfo(checkAndGetDataTypeInfo(info, dtMap, ci.getDataType(), entry.getKey(),
                  "tableName = " + ti.getName() + ", columnName = " + ci.getName()));
            }
          }

        } else if (rootInfo instanceof EnumRootInfo) {
          // EnumRootInfo
          for (EnumClassInfo ei : ((EnumRootInfo) rootInfo).enumClassList) {
            ei.setDtInfo(checkAndGetDataTypeInfo(info, dtMap, ei.getDataTypeName(), entry.getKey(),
                "enumName = " + ei.getEnumName()));
          }

        } else if (rootInfo instanceof MiscGroupRootInfo) {
          // MiscGroupRootInfo
          MiscGroupRootInfo grpInfo = (MiscGroupRootInfo) rootInfo;

          if (grpInfo.isDefined()) {
            grpInfo.setDtInfo(checkAndGetDataTypeInfo(info, dtMap, grpInfo.getDataTypeName(),
                entry.getKey(), "grouping column: " + grpInfo.getColumnName()));
          }
        }
      }
    }
  }

  private void checkForChildTable(String sysName, DbOrClassRootInfo dbOrClassRootInfo)
      throws BizLogicAppException {
    List<String> tableNameSet =
        dbOrClassRootInfo.tableList.stream().map(e -> e.getName()).toList();

    for (DbOrClassTableInfo ti : dbOrClassRootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (StringUtils.isNotEmpty(ci.getRelationRefTable())) {

          // relation: refering to table name existence check
          if (!tableNameSet.contains(ci.getRelationRefTable())) {
            throw new BizLogicAppException("MSG_ERR_DB_REFER_TO_TABLE_NAME_NOT_FOUND", sysName,
                ti.getName(), ci.getName(), ci.getRelationRefTable());
          }

          DbOrClassTableInfo refTi = dbOrClassRootInfo.tableList.stream()
              .collect(Collectors.toMap(e -> e.getName(), e -> e))
              .get(ci.getRelationRefTable());

          // relation: refering to column name existence check
          List<String> refTiColNameList =
              refTi.columnList.stream().map(e -> e.getName()).toList();
          if (!refTiColNameList.contains(ci.getRelationRefCol())) {
            throw new BizLogicAppException("MSG_ERR_DB_REFER_TO_COLUMN_NAME_NOT_FOUND", sysName,
                ti.getName(), ci.getName(), ci.getRelationRefCol());
          }
        }
      }
    }
  }

  private DataTypeInfo checkAndGetDataTypeInfo(Info info, HashMap<String, DataTypeInfo> dtMap,
      String dataTypeName, String systemName, String placeInfo) throws BizLogicAppException {
    DataTypeInfo dtInfo = dtMap.get(dataTypeName);
    if (dtInfo == null) {
      throw new BizLogicAppException("MSG_ERR_DESIGNATED_DT_NOT_EXIST_IN_DT_INFO", dataTypeName,
          systemName, placeInfo);
    }

    return dtInfo;
  }

  private void checkAndComplementForParentAndChildTable(DbOrClassRootInfo dbCommonRootInfo,
      DbOrClassRootInfo dbRootInfo) throws BizLogicAppException {
    // 情報付加のため再度ループ
    for (DbOrClassTableInfo tableInfo : dbRootInfo.tableList) {
      // テーブル単位の情報取得
      boolean hasS = false;
      boolean hasU = false;

      // dbInfoとdbCommonInfoのカラムを合わせないと正しく判断できないためマージ
      List<DbOrClassColumnInfo> commonAddedColumnList = new ArrayList<>();
      commonAddedColumnList.addAll(tableInfo.columnList);
      commonAddedColumnList.addAll(dbCommonRootInfo.tableList.get(0).columnList);

      for (DbOrClassColumnInfo colInfo : commonAddedColumnList) {
        // commonのcolumnも追加

        if (colInfo.isPk()) {
          // Surrogate Keyが2項目あるのはNGなのでチェックしておく
          if (hasS) {
            throw new BizLogicAppException("MSG_ERR_SURROGATE_KEY_DUPLICATED");
          }

          hasS = true;
        }

        if (colInfo.isUniqueConstraint()) {
          hasU = true;
        }
      }

      // PKは必須。SystemCommonだけは特別扱い。
      if (!tableInfo.getName().equals("SYSTEM_COMMON") && !hasS) {
        throw new BizLogicAppException("MSG_ERR_PK_REQUIRED", tableInfo.getName());
      }

      tableInfo.setHasUniqueConstraint(hasU);
    }
  }

  /** tableとgroupの間のチェック。 */
  private void checkAndComplementForTableAndGroup(String systemName,
      DbOrClassRootInfo dbCommonRootInfo, DbOrClassRootInfo dbRootInfo,
      MiscGroupRootInfo groupRootInfo) throws BizLogicAppException {

    final String colName = groupRootInfo.getColumnName();

    // groupの定義がない場合は終了
    if (!groupRootInfo.isDefined()) {
      return;
    }

    // groupRootInfoで定義された項目が親子のtableでそれぞれ保持有無の取得（子は任意のtableに存在すればありとみなす）
    boolean parentTableHasGroupCol = dbCommonRootInfo.tableList.get(0).hasColumn(colName);
    boolean childTableHasGroupCol = false;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasColumn(colName)) {
        childTableHasGroupCol = true;
        break;
      }
    }

    // 親にも子にも存在するのはNGだが、「親にも子にも同一のカラム存在チェック」で引っかかるためここではチェックしない。
    // 親にも子にも存在しないチェックはしておく。
    if (!parentTableHasGroupCol && !childTableHasGroupCol) {
      throw new BizLogicAppException("MSG_ERR_COMMON_GROUP_COL_NOT_FOUND", systemName);
    }

    // 共通設定として「group_id」をgroupを表すカラム名としている場合に、そのマスタとなるgroupテーブル上では、group_idではなくidというカラム名で持ちたいことがある。
    // そのために、各テーブルの設定で「グループ識別項目」という列を持っている。この背景からわかるように、「グループ識別項目」は子テーブルで、あってもひとつのみ存在。

    // systemCommonに「グループ識別項目」を持つのは意味がわからないのでエラー。（共通設定のgroupを使用すべき）
    if (dbCommonRootInfo.tableList.get(0).hasCustomGroupColumn()) {
      throw new BizLogicAppException("MSG_ERR_SYSTEM_COMMON_ENTITY_CANNOT_HAVE_CUSTOM_GROUP_COLUMN",
          systemName);
    }

    // 子テーブルで「グループ識別項目」が2つあるのはエラーとする
    int numOfCustomGroupColumns = 0;
    String customGroupTableName = null;
    String customGroupColumnName = null;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasCustomGroupColumn()) {
        numOfCustomGroupColumns++;
        customGroupTableName = ti.getName();
        customGroupColumnName = ti.getCustomGroupColumn().getName();
        if (numOfCustomGroupColumns > 1) {
          throw new BizLogicAppException("MSG_ERR_MULTIPLE_CUSTOM_GROUP_COLUMN_CANNOT_EXIST",
              systemName);
        }
      }
    }

    // customGroupの情報を追加
    groupRootInfo.setCustomGroupTableName(customGroupTableName);
    groupRootInfo.setCustomGroupColumnName(customGroupColumnName);
  }

  private boolean shouldMakeNoGroupQuery(Info info) {
    if (info.groupRootInfo == null) {
      return false;
    }

    return info.groupRootInfo.getNeedsUngroupedSource();
  }

  private boolean shouldMakeNoGroupQueryForDaoOnly(Info info) {
    if (info.groupRootInfo == null) {
      return false;
    }

    return info.groupRootInfo.getDevidesDaoIntoOtherProject();
  }

  /**
   * 再帰処理で指定のディレクトリをまるごと削除。
   */
  public static void delete(File f) {
    // ファイル・ディレクトリが存在しなければ終了
    if (f.exists() == false) {
      return;
    }

    // ファイルがあれば削除
    if (f.isFile()) {
      f.delete();
    }

    // ディレクトリがある場合は配下のファイルを全て削除
    if (f.isDirectory()) {
      Arrays.asList(f.listFiles()).forEach(file -> delete(file));
      // 最後に自分を削除
      f.delete();
    }
  }

  // allDtMapを作成
  private static HashMap<String, HashMap<String, DataTypeInfo>> createAllDataTypeMap(
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap) {
    // 一つ目のStringはシステム名、2つ目はdataType名。全てのdataTypeInfoをこれに詰める
    HashMap<String, HashMap<String, DataTypeInfo>> allDtMap =
        new HashMap<String, HashMap<String, DataTypeInfo>>();

    // データをdtMapに詰める
    systemMap.keySet().forEach(systemName -> {
      // 仕様上、dataTypeInfoなしで、dataTypeRefInfoのみを使用しシステムを構築することも可能としているので、存在チェックをかけておく
      if (systemMap.get(systemName).get(DataKindEnum.DATA_TYPE) != null) {
        // Mapを生成
        allDtMap.put(systemName, new HashMap<String, DataTypeInfo>());

        DataTypeRootInfo dtRootInfo =
            (DataTypeRootInfo) systemMap.get(systemName).get(DataKindEnum.DATA_TYPE);
        // dataTypeListのdataType情報をMapに詰める。
        for (DataTypeInfo dtInfo : dtRootInfo.dataTypeList) {
          allDtMap.get(systemName).put(dtInfo.getDataTypeName(), dtInfo);
        }
      }
    });

    return allDtMap;
  }
}
