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
package jp.ecuacion.tool.codegenerator.core.bl;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/**
 * Performs cross-file consistency checks between DB/class definitions and data type
 * definitions, and sets up cross-cutting data.
 */
public class PreparerForDbAndDataType {

  private CodeGenContext getInfo() {
    return MainController.tlInfo.get();
  }

  /**
   * Runs all inter-file consistency checks: data type existence, duplicate names, and
   * kind-specific processing.
   */
  public void prepare() {
    // Check data type existence consistency across multiple XML/Excel files
    checkIfDataTypeInEnumExistsInDataTypeInfo();
    checkIfDataTypeInDbOrClassExistsInDataTypeInfo();

    // Check for duplicate keys appearing more than once within a single file
    checkRepeatedEmerge();

    // Process data types by kind, across files
    checkKataBetsuSyori();
  }

  /**
   * Checks whether the data type names referenced in enum definitions exist in dataTypeInfo.
   */
  @SuppressWarnings("null")
  private void checkIfDataTypeInEnumExistsInDataTypeInfo() {

    EnumRootInfo enumRootInfo = ((EnumRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.ENUM));

    // Skip if enumInfo is absent
    if (enumRootInfo == null) {
      return;
    }

    List<String> dataTypeNameList =
        getInfo().getDataTypeRootInfo().dataTypeList.stream()
            .map(dt -> dt.getDataTypeName()).toList();
    enumRootInfo.enumClassList.stream().forEach(en -> {
      if (!dataTypeNameList.contains(en.getDataTypeName())) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_DESIGNATED_DT_NOT_FOUND_IN_DT_DEFINITION", getInfo().getSystemName(),
            DataKindEnum.ENUM.getLabel(), en.getEnumName(), en.getDataTypeName())).throwIfAny();
      }
    });
  }

  /**
   * Checks whether the data type names referenced in DbOrClass definitions exist in dataTypeInfo.
   */
  private void checkIfDataTypeInDbOrClassExistsInDataTypeInfo() {
    checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum.DB);
    checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum.DB_COMMON);
  }

  /**
   * Common processing to check whether data type names in DbOrClass exist in dataTypeInfo.
   */
  @SuppressWarnings("null")
  private void checkIfDataTypeInDbOrClassExistsInDataTypeInfoCommon(DataKindEnum dataKind) {

    DbOrClassRootInfo rootInfo = ((DbOrClassRootInfo) getInfo().getRootInfoMap().get(dataKind));
    if (rootInfo == null) {
      return;
    }

    List<String> list =
        getInfo().getDataTypeRootInfo().dataTypeList.stream()
            .map(e -> e.getDataTypeName()).toList();
    for (DbOrClassTableInfo ti : rootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (!list.contains(ci.getDataType())) {
          new Violations().add(new BusinessViolation(
              "MSG_ERR_DESIGNATED_DT_NOT_FOUND_IN_DT_DEFINITION",
              getInfo().getSystemName(),
              dataKind.getLabel(), ti.getName() + "." + ci.getName(), ci.getDataType()))
              .throwIfAny();
        }
      }
    }
  }

  private void checkRepeatedEmerge() {

    Iterator<DataKindEnum> it = getInfo().getRootInfoMap().keySet().iterator();
    while (it.hasNext()) {
      AbstractRootInfo rootInfo = getInfo().getRootInfoMap().get(it.next());
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

  private void checkRepeatedEmergeEnum(EnumRootInfo rootInfo) {

    HashSet<String> clsNameSet = new HashSet<String>();

    for (EnumClassInfo ci : rootInfo.enumClassList) {
      // Class-level duplicate check
      if (clsNameSet.contains(ci.getEnumName())) {
        new Violations().add(new BusinessViolation("MSG_ERR_SAME_ENUM_DEFINED_TWICE",
            getInfo().getSystemName(), ci.getEnumName())).throwIfAny();
      }

      clsNameSet.add(ci.getEnumName());

      HashSet<String> valCodeSet = new HashSet<String>();
      HashSet<String> valVarNameSet = new HashSet<String>();
      // dispName exists for multiple languages, so hold a per-language set keyed by language
      HashMap<String, HashSet<String>> valDispNameDuplicateCheckMap =
          new HashMap<String, HashSet<String>>();

      for (EnumValueInfo vi : ci.enumList) {
        // code
        if (valCodeSet.contains(vi.getCode())) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_CODE_DEFINED_TWICE_IN_ENUM",
              getInfo().getSystemName(), ci.getEnumName(), vi.getCode())).throwIfAny();
        }

        valCodeSet.add(vi.getCode());
        // varName
        if (valVarNameSet.contains(vi.getVarName())) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_VAR_NAME_DEFINED_TWICE_IN_ENUM",
              getInfo().getSystemName(), ci.getEnumName(), vi.getVarName())).throwIfAny();
        }

        valVarNameSet.add(vi.getVarName());

        // dispName
        // Since multiple languages are supported, perform a check for each language
        Iterator<String> dispNameLangIt = vi.getDisplayNameMap().keySet().iterator();

        while (dispNameLangIt.hasNext()) {
          String lang = dispNameLangIt.next();
          String dispName = vi.getDisplayNameMap().get(lang);

          // Error if dispName is blank
          if (dispName == null || dispName.equals("")) {
            new Violations().add(new BusinessViolation("MSG_ERR_ENUM_DISP_NAME_EMPTY",
                getInfo().getSystemName(), ci.getEnumName(), vi.getCode(), lang)).throwIfAny();
          }

          // Create a new set if one does not exist yet
          if (!valDispNameDuplicateCheckMap.containsKey(lang)) {
            valDispNameDuplicateCheckMap.put(lang, new HashSet<String>());
          }

          if (valDispNameDuplicateCheckMap.get(lang).contains(dispName)) {
            new Violations().add(new BusinessViolation(
                "MSG_ERR_SAME_DISP_NAME_DEFINED_TWICE_IN_ENUM",
                getInfo().getSystemName(), ci.getEnumName(), dispName)).throwIfAny();
          }

          valDispNameDuplicateCheckMap.get(lang).add(dispName);
        }
      }
    }
  }

  private void checkRepeatedEmergeDataType() {

    HashSet<String> dtNameSet = new HashSet<String>();

    DataTypeRootInfo dtRootInfo =
        (DataTypeRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DATA_TYPE);

    // Verify that there are no duplicate entries within the dataType definitions themselves
    if (dtRootInfo != null) {
      for (DataTypeInfo dtInfo : dtRootInfo.dataTypeList) {
        if (dtNameSet.contains(dtInfo.getDataTypeName())) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_DT_DEFINED_TWICE",
              getInfo().getSystemName(), dtInfo.getDataTypeName())).throwIfAny();
        }

        dtNameSet.add(dtInfo.getDataTypeName());
      }
    }
  }

  private void checkRepeatedEmergeDbOrClass() {

    HashSet<String> dbCommonColSet = new HashSet<String>();
    // HashSet<String> clsTableSet = null;

    DbOrClassRootInfo dbRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DB);
    DbOrClassRootInfo dbCommonRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DB_COMMON);
    // DbOrClassRootInfo clsRootInfo = (DbOrClassRootInfo)
    // rootInfoMap.get(DataKindEnum.XML_POST_FIX_CLS);

    // Check for duplicate entries within dbCommon itself
    // Verify item count since it may not exist
    if (dbCommonRootInfo != null && dbCommonRootInfo.tableList.size() > 0) {
      for (DbOrClassColumnInfo col : dbCommonRootInfo.tableList.get(0).columnList) {
        if (dbCommonColSet.contains(col.getName())) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_COL_DEFINED_TWICE",
              getInfo().getSystemName(), DataKindEnum.DB_COMMON.getLabel(), "(none)",
              col.getName()))
              .throwIfAny();
        }
        dbCommonColSet.add(col.getName());
      }
    }

    // Duplicate check for DB tables
    checkDuplicatedDefinitionOfDbOrClassAndCreateTableSet(getInfo().getSystemName(), dbRootInfo,
        dbCommonColSet, DataKindEnum.DB);
  }

  private HashSet<String> checkDuplicatedDefinitionOfDbOrClassAndCreateTableSet(String systemName,
      @org.jspecify.annotations.Nullable DbOrClassRootInfo rootInfo, HashSet<String> dbCommonColSet,
      DataKindEnum dataKind) {
    HashSet<String> tableSet = new HashSet<String>();

    if (rootInfo != null) {
      for (DbOrClassTableInfo ti : rootInfo.tableList) {
        // Table-level duplicate check
        if (tableSet.contains(ti.getName())) {
          new Violations().add(new BusinessViolation("MSG_ERR_SAME_TABLE_DEFINED_TWICE",
              systemName + dataKind.getLabel(), ti.getName())).throwIfAny();
        }

        tableSet.add(ti.getName());

        HashSet<String> dbColSet = new HashSet<String>();

        for (DbOrClassColumnInfo col : ti.columnList) {
          if (dbColSet.contains(col.getName())) {
            new Violations().add(new BusinessViolation("MSG_ERR_SAME_COL_DEFINED_TWICE", systemName,
                dataKind.getLabel(), ti.getName(), col.getName())).throwIfAny();
          }

          // Error if the item already exists in dbCommon
          if (dbCommonColSet.contains(col.getName())) {
            new Violations().add(new BusinessViolation("MSG_ERR_COL_CONTAINED_IN_DB_COMMON",
                systemName + dataKind.getLabel(), ti.getName(), col.getName())).throwIfAny();
          }

          dbColSet.add(col.getName());
        }
      }
    }
    return tableSet;
  }

  private void checkKataBetsuSyori() {

    DbOrClassRootInfo dbRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DB);
    DbOrClassRootInfo dbCommonRootInfo =
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DB_COMMON);

    // Auto-increment
    if (dbRootInfo != null) {
      for (DbOrClassTableInfo tab : dbRootInfo.tableList) {
        checkKataBetsuShoriAutoIncrement(tab, getInfo().getSystemName(), DataKindEnum.DB);
      }
    }

    // dbCommon contains only columns so no loop needed
    if (dbCommonRootInfo != null && dbCommonRootInfo.tableList.size() > 0) {
      checkKataBetsuShoriAutoIncrement(dbCommonRootInfo.tableList.get(0), getInfo().getSystemName(),
          DataKindEnum.DB_COMMON);
    }
  }

  private void checkKataBetsuShoriAutoIncrement(DbOrClassTableInfo tableInfo, String systemName,
      DataKindEnum postFix) {

    for (DbOrClassColumnInfo col : tableInfo.columnList) {
      // Skip if auto-increment is not configured
      if (!col.isAutoIncrement()) {
        continue;
      }

      // Get the dataType name
      DataTypeInfo dataType = col.getDtInfo();
      DataTypeKataEnum en = dataType.getKata();
      if (en != INTEGER && en != LONG && en != TIMESTAMP && en != DATE_TIME
          && en != DataTypeKataEnum.ENUM && en != DataTypeKataEnum.BOOLEAN) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_AUTO_INCREMENT_CAN_BE_ON_ONLY_WHEN_KATA_IS_EITHER_INT_OR_LONG_OR_TIMESTAMP",
            systemName + postFix, tableInfo.getName(), col.getName())).throwIfAny();
      }
    }
  }
}
