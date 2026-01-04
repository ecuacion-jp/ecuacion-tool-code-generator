package jp.ecuacion.tool.codegenerator.core.blf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.bl.CheckAndComplementFileLevelConsistencyCheckBl;
import jp.ecuacion.tool.codegenerator.core.bl.PrepareManager;
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
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import org.apache.commons.lang3.StringUtils;

public class CheckAndComplementDataBlf {

  public Map<String, DataTypeInfo> execute(Info info, String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) throws AppException {

    final SystemCommonRootInfo systemCommon =
        (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);

    // 複数RootInfoの中で、RootInfoの存在有無の整合性チェックと補完
    new CheckAndComplementFileLevelConsistencyCheckBl().check(systemName, rootInfoMap);

    // dataType
    ((DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE)).dataTypeList
        .forEach(dt -> dt.checksAndComplements(systemCommon));

    // inside tables
    checkForChildTable(systemCommon.getSystemName(),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

    // tableの親子間
    checkAndComplementForParentAndChildTable(
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

    // tableとgroup間
    checkAndComplementForTableAndGroup(systemName,
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB),
        (MiscGroupRootInfo) rootInfoMap.get(DataKindEnum.MISC_GROUP));

    // dataTypeInfoのcolInfoへの詰め込み
    Map<String, DataTypeInfo> dtMap = createDataTypeMap(systemName, rootInfoMap);

    // Put dataTypeInfo to info.
    putDataTypeInfoIntoColInfo(systemName, rootInfoMap, dtMap);

    // 複数ファイル間でのデータチェックとデータ整理
    new PrepareManager().prepare();

    return dtMap;
  }

  private void checkForChildTable(String sysName, DbOrClassRootInfo dbOrClassRootInfo)
      throws BizLogicAppException {
    List<String> tableNameSet = dbOrClassRootInfo.tableList.stream().map(e -> e.getName()).toList();

    for (DbOrClassTableInfo ti : dbOrClassRootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (StringUtils.isNotEmpty(ci.getRelationRefTable())) {

          // relation: refering to table name existence check
          if (!tableNameSet.contains(ci.getRelationRefTable())) {
            throw new BizLogicAppException("MSG_ERR_DB_REFER_TO_TABLE_NAME_NOT_FOUND", sysName,
                ti.getName(), ci.getName(), ci.getRelationRefTable());
          }

          DbOrClassTableInfo refTi = dbOrClassRootInfo.tableList.stream()
              .collect(Collectors.toMap(e -> e.getName(), e -> e)).get(ci.getRelationRefTable());

          // relation: refering to column name existence check
          List<String> refTiColNameList = refTi.columnList.stream().map(e -> e.getName()).toList();
          if (!refTiColNameList.contains(ci.getRelationRefCol())) {
            throw new BizLogicAppException("MSG_ERR_DB_REFER_TO_COLUMN_NAME_NOT_FOUND", sysName,
                ti.getName(), ci.getName(), ci.getRelationRefCol());
          }
        }
      }
    }
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

  // dtMapを作成
  public Map<String, DataTypeInfo> createDataTypeMap(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {
    // 一つ目のStringはシステム名、2つ目はdataType名。全てのdataTypeInfoをこれに詰める
    Map<String, DataTypeInfo> dtMap = new HashMap<String, DataTypeInfo>();

    // データをdtMapに詰める
    // 仕様上、dataTypeInfoなしで、dataTypeRefInfoのみを使用しシステムを構築することも可能としているので、存在チェックをかけておく
    if (rootInfoMap.get(DataKindEnum.DATA_TYPE) != null) {
      // Mapを生成

      DataTypeRootInfo dtRootInfo = (DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE);
      // dataTypeListのdataType情報をMapに詰める。
      for (DataTypeInfo dtInfo : dtRootInfo.dataTypeList) {
        dtMap.put(dtInfo.getDataTypeName(), dtInfo);
      }
    }

    return dtMap;
  }

  private void putDataTypeInfoIntoColInfo(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap, Map<String, DataTypeInfo> dtMap)
      throws BizLogicAppException {

    for (AbstractRootInfo rootInfo : rootInfoMap.values()) {

      if (rootInfo instanceof DbOrClassRootInfo) {
        // DbOrClassRootInfo
        for (DbOrClassTableInfo ti : ((DbOrClassRootInfo) rootInfo).tableList) {
          for (DbOrClassColumnInfo ci : ti.columnList) {
            ci.setDtInfo(checkAndGetDataTypeInfo(dtMap, ci.getDataType(), systemName,
                "tableName = " + ti.getName() + ", columnName = " + ci.getName()));
          }
        }

      } else if (rootInfo instanceof EnumRootInfo) {
        // EnumRootInfo
        for (EnumClassInfo ei : ((EnumRootInfo) rootInfo).enumClassList) {
          ei.setDtInfo(checkAndGetDataTypeInfo(dtMap, ei.getDataTypeName(), systemName,
              "enumName = " + ei.getEnumName()));
        }

      } else if (rootInfo instanceof MiscGroupRootInfo) {
        // MiscGroupRootInfo
        MiscGroupRootInfo grpInfo = (MiscGroupRootInfo) rootInfo;

        if (grpInfo.isDefined()) {
          grpInfo.setDtInfo(checkAndGetDataTypeInfo(dtMap, grpInfo.getDataTypeName(), systemName,
              "grouping column: " + grpInfo.getColumnName()));
        }
      }
    }
  }

  private DataTypeInfo checkAndGetDataTypeInfo(Map<String, DataTypeInfo> dtMap, String dataTypeName,
      String systemName, String placeInfo) throws BizLogicAppException {
    DataTypeInfo dtInfo = dtMap.get(dataTypeName);
    if (dtInfo == null) {
      throw new BizLogicAppException("MSG_ERR_DESIGNATED_DT_NOT_EXIST_IN_DT_INFO", dataTypeName,
          systemName, placeInfo);
    }

    return dtInfo;
  }
}
