package jp.ecuacion.tool.codegenerator.core.preparer;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public class PreparerForDbAndDataType {

  private Info info;

  public PreparerForDbAndDataType() {
    this.info = MainController.tlInfo.get();
  }

  public void prepare() throws AppException {
    // 複数xml間のdataType存在整合性
    checkIfDataTypeInEnumExistsInDataTypeInfo();
    checkIfDataTypeInDbOrClassExistsInDataTypeInfo();

    // 一つのファイルの中で、同じキーが複数回出てくることに対するチェック
    checkRepeatedEmerge();

    // ファイルをまたがるdataTypeの型別処理
    checkKataBetsuSyori();
  }

  /**
   * enumに存在するdataType名がdataTypeInfoに存在するかをチェック。
   */
  private void checkIfDataTypeInEnumExistsInDataTypeInfo() throws AppException {

    EnumRootInfo enumRootInfo = ((EnumRootInfo) info.rootInfoMap.get(DataKindEnum.ENUM));

    // enumInfoが存在しない場合はスキップ
    if (enumRootInfo == null) {
      return;
    }

    List<String> dataTypeNameList =
        info.dataTypeRootInfo.dataTypeList.stream().map(dt -> dt.getDataTypeName()).toList();
    enumRootInfo.enumClassList.stream().forEach(en -> {
      if (!dataTypeNameList.contains(en.getDataTypeName())) {
        throw new UncheckedAppException(new BizLogicAppException(
            "MSG_ERR_DESIGNATED_DT_NOT_FOUND_IN_DT_DEFINITION", info.systemName,
            DataKindEnum.ENUM.getLabel(), en.getEnumName(), en.getDataTypeName()));
      }
    });
  }

  /**
   * DbOrClassに存在するdataType名がdataTypeInfoに存在するかをチェック。
   */
  private void checkIfDataTypeInDbOrClassExistsInDataTypeInfo() throws AppException {
    checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum.DB);
    checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum.DB_COMMON);
  }

  /**
   * DbOrClassに存在するdataType名がdataTypeInfoに存在するかをチェックするための共通処理。
   */
  private void checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum dataKind)
      throws AppException {

    DbOrClassRootInfo rootInfo = ((DbOrClassRootInfo) info.rootInfoMap.get(dataKind));
    if (rootInfo == null) {
      return;
    }

    List<String> list =
        info.dataTypeRootInfo.dataTypeList.stream().map(e -> e.getDataTypeName()).toList();
    for (DbOrClassTableInfo ti : rootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (!list.contains(ci.getDataType())) {
          throw new BizLogicAppException("MSG_ERR_DESIGNATED_DT_NOT_FOUND_IN_DT_DEFINITION",
              ((SystemCommonRootInfo) info.rootInfoMap.get(dataKind)).getSystemName(),
              dataKind.getLabel(), ti.getName() + "." + ci.getName(), ci.getDataType());
        }
      }
    }
  }

  private void checkRepeatedEmerge() throws AppException {

    Iterator<DataKindEnum> it = info.rootInfoMap.keySet().iterator();
    while (it.hasNext()) {
      AbstractRootInfo rootInfo = info.rootInfoMap.get(it.next());
      if (rootInfo instanceof EnumRootInfo) {
        checkRepeatedEmergeEnum((EnumRootInfo) rootInfo);
      }

      if (rootInfo instanceof DataTypeRootInfo) {
        checkRepeatedEmergeDataType();
      }

      if (rootInfo instanceof DbOrClassRootInfo) {
        checkRepeatedEmergeDbOrClass();
      }
    }
  }

  private void checkRepeatedEmergeEnum(EnumRootInfo rootInfo) throws AppException {

    HashSet<String> clsNameSet = new HashSet<String>();

    for (EnumClassInfo ci : rootInfo.enumClassList) {
      // classレベルの重複チェック
      if (clsNameSet.contains(ci.getEnumName())) {
        throw new BizLogicAppException("MSG_ERR_SAME_ENUM_DEFINED_TWICE", info.systemName,
            ci.getEnumName());
      }

      clsNameSet.add(ci.getEnumName());

      HashSet<String> valCodeSet = new HashSet<String>();
      HashSet<String> valVarNameSet = new HashSet<String>();
      // dispNameは複数言語分存在するので、言語をkeyにして言語別のsetを持つ
      HashMap<String, HashSet<String>> valDispNameDuplicateCheckMap =
          new HashMap<String, HashSet<String>>();

      for (EnumValueInfo vi : ci.enumList) {
        // code
        if (valCodeSet.contains(vi.getCode())) {
          throw new BizLogicAppException("MSG_ERR_SAME_CODE_DEFINED_TWICE_IN_ENUM", info.systemName,
              ci.getEnumName(), vi.getCode());
        }

        valCodeSet.add(vi.getCode());
        // varName
        if (valVarNameSet.contains(vi.getVarName())) {
          throw new BizLogicAppException("MSG_ERR_SAME_VAR_NAME_DEFINED_TWICE_IN_ENUM",
              info.systemName, ci.getEnumName(), vi.getVarName());
        }

        valVarNameSet.add(vi.getVarName());

        // dispName
        // 複数言語を持っているので、言語ごとにチェックを行う
        Iterator<String> dispNameLangIt = vi.getDisplayNameMap().keySet().iterator();

        while (dispNameLangIt.hasNext()) {
          String lang = dispNameLangIt.next();
          String dispName = vi.getDisplayNameMap().get(lang);

          // dispNameが空欄の場合はエラー
          if (dispName == null || dispName.equals("")) {
            throw new BizLogicAppException("MSG_ERR_ENUM_DISP_NAME_EMPTY", info.systemName,
                ci.getEnumName(), vi.getCode(), lang);
          }

          // setが存在しない場合はsetを新規作成
          if (!valDispNameDuplicateCheckMap.containsKey(lang)) {
            valDispNameDuplicateCheckMap.put(lang, new HashSet<String>());
          }

          if (valDispNameDuplicateCheckMap.get(lang).contains(dispName)) {
            throw new BizLogicAppException("MSG_ERR_SAME_DISP_NAME_DEFINED_TWICE_IN_ENUM",
                info.systemName, ci.getEnumName(), dispName);
          }

          valDispNameDuplicateCheckMap.get(lang).add(dispName);
        }
      }
    }
  }

  private void checkRepeatedEmergeDataType() throws AppException {

    HashSet<String> dtNameSet = new HashSet<String>();

    DataTypeRootInfo dtRootInfo = (DataTypeRootInfo) info.rootInfoMap.get(DataKindEnum.DATA_TYPE);

    // dataType自身の中に2回出現がないかを確認
    if (dtRootInfo != null) {
      for (DataTypeInfo dtInfo : dtRootInfo.dataTypeList) {
        if (dtNameSet.contains(dtInfo.getDataTypeName())) {
          throw new BizLogicAppException("MSG_ERR_SAME_DT_DEFINED_TWICE", info.systemName,
              dtInfo.getDataTypeName());
        }

        dtNameSet.add(dtInfo.getDataTypeName());
      }
    }
  }

  private void checkRepeatedEmergeDbOrClass() throws AppException {

    HashSet<String> dbCommonColSet = new HashSet<String>();
    // HashSet<String> clsTableSet = null;

    DbOrClassRootInfo dbRootInfo = (DbOrClassRootInfo) info.rootInfoMap.get(DataKindEnum.DB);
    DbOrClassRootInfo dbCommonRootInfo =
        (DbOrClassRootInfo) info.rootInfoMap.get(DataKindEnum.DB_COMMON);
    // DbOrClassRootInfo clsRootInfo = (DbOrClassRootInfo)
    // rootInfoMap.get(DataKindEnum.XML_POST_FIX_CLS);

    // dbCommon自身の中に2回出現がないか
    // 存在しない場合もあるので、件数を確認
    if (dbCommonRootInfo != null && dbCommonRootInfo.tableList.size() > 0) {
      for (DbOrClassColumnInfo col : dbCommonRootInfo.tableList.get(0).columnList) {
        if (dbCommonColSet.contains(col.getName())) {
          throw new BizLogicAppException("MSG_ERR_SAME_COL_DEFINED_TWICE", info.systemName,
              DataKindEnum.DB_COMMON.getLabel(), "（なし）", col.getName());
        }
        dbCommonColSet.add(col.getName());
      }
    }

    // DBのtable重複チェック処理
    checkDuplicatedDefinitionOfDbOrClassAndCreateTableSet(info.systemName, dbRootInfo,
        dbCommonColSet, DataKindEnum.DB);
  }

  private HashSet<String> checkDuplicatedDefinitionOfDbOrClassAndCreateTableSet(String systemName,
      DbOrClassRootInfo rootInfo, HashSet<String> dbCommonColSet, DataKindEnum dataKind)
      throws AppException {
    HashSet<String> tableSet = new HashSet<String>();

    if (rootInfo != null) {
      for (DbOrClassTableInfo ti : rootInfo.tableList) {
        // tableレベルの重複チェック
        if (tableSet.contains(ti.getName())) {
          throw new BizLogicAppException("MSG_ERR_SAME_TABLE_DEFINED_TWICE",
              systemName + dataKind.getLabel(), ti.getName());
        }

        tableSet.add(ti.getName());

        HashSet<String> dbColSet = new HashSet<String>();

        for (DbOrClassColumnInfo col : ti.columnList) {
          if (dbColSet.contains(col.getName())) {
            throw new BizLogicAppException("MSG_ERR_SAME_COL_DEFINED_TWICE", systemName,
                dataKind.getLabel(), ti.getName(), col.getName());
          }

          // dbCommonに存在する項目の場合はエラー
          if (dbCommonColSet.contains(col.getName())) {
            throw new BizLogicAppException("MSG_ERR_COL_CONTAINED_IN_DB_COMMON",
                systemName + dataKind.getLabel(), ti.getName(), col.getName());
          }

          dbColSet.add(col.getName());
        }
      }
    }
    return tableSet;
  }

  private void checkKataBetsuSyori() throws AppException {

    DbOrClassRootInfo dbRootInfo = (DbOrClassRootInfo) info.rootInfoMap.get(DataKindEnum.DB);
    DbOrClassRootInfo dbCommonRootInfo =
        (DbOrClassRootInfo) info.rootInfoMap.get(DataKindEnum.DB_COMMON);

    // 自動採番
    if (dbRootInfo != null) {
      for (DbOrClassTableInfo tab : dbRootInfo.tableList) {
        checkKataBetsuShoriAutoIncrement(tab, info.systemName, DataKindEnum.DB);
      }
    }

    // dbCommonはカラムのみのためループなし
    if (dbCommonRootInfo != null && dbCommonRootInfo.tableList.size() > 0) {
      checkKataBetsuShoriAutoIncrement(dbCommonRootInfo.tableList.get(0), info.systemName,
          DataKindEnum.DB_COMMON);
    }
  }

  private void checkKataBetsuShoriAutoIncrement(DbOrClassTableInfo tableInfo, String systemName,
      DataKindEnum postFix) throws AppException {

    for (DbOrClassColumnInfo col : tableInfo.columnList) {
      // 自動採番が設定されていない場合はチェック不要のためスキップ
      if (!col.isAutoIncrement()) {
        continue;
      }

      // dataType名を取得
      DataTypeInfo dataType = col.getDtInfo();
      DataTypeKataEnum en = dataType.getKata();
      if (en != INTEGER && en != LONG && en != TIMESTAMP && en != DATE_TIME
          && en != DataTypeKataEnum.ENUM && en != DataTypeKataEnum.BOOLEAN) {
        throw new BizLogicAppException(
            "MSG_ERR_AUTO_INCREMENT_CAN_BE_ON_ONLY_WHEN_KATA_IS_EITHER_INT_OR_LONG_OR_TIMESTAMP",
            systemName + postFix, tableInfo.getName(), col.getName());
      }
    }
  }
}
