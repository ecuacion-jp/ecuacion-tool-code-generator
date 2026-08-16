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
package jp.ecuacion.tool.codegenerator.core.blf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.bl.PreparerForDbAndDataType;
import jp.ecuacion.tool.codegenerator.core.bl.PreparerForMiscGroup;
import jp.ecuacion.tool.codegenerator.core.bl.PreparerForMiscOptimisticLock;
import jp.ecuacion.tool.codegenerator.core.bl.PreparerForMiscRemovedData;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
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
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * Validates and complements data parsed from Excel before code generation runs.
 *
 * <p>NullAway is suppressed at class level because every value retrieved from
 *     {@code rootInfoMap} via {@code Map.get(...)} is treated as present at this stage of the
 *     pipeline; missing entries are reported as business validation errors elsewhere.</p>
 */
@SuppressWarnings("NullAway")
public class CheckAndComplementDataBlf {

  /**
    * Validates and complements data in the given rootInfoMap, then returns a map from data-type
    * name to DataTypeInfo.
   */
  @SuppressWarnings("null")
  public Map<String, DataTypeInfo> execute(CodeGenContext info, String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {

    final SystemCommonRootInfo systemCommon =
        (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);

    // Consistency check and complementation for the existence of multiple RootInfos
    checkAndComplementFileLevelConsistencyCheckBl(systemName, rootInfoMap);

    // dataType
    ((DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE)).dataTypeList
        .forEach(dt -> dt.checksAndComplements(systemCommon));

    // DB
    checkForChildTable(systemCommon.getSystemName(),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

    // Between parent and child tables
    checkAndComplementForParentAndChildTable(systemName,
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB));

    // Between table and group
    checkAndComplementForTableAndGroup(systemName,
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON),
        (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB),
        (MiscGroupRootInfo) rootInfoMap.get(DataKindEnum.MISC_GROUP));

    // Store dataTypeInfo into colInfo
    Map<String, DataTypeInfo> dtMap = createDataTypeMap(systemName, rootInfoMap);

    // Put dataTypeInfo to info.
    putDataTypeInfoIntoColInfo(systemName, rootInfoMap, dtMap);

    // Data check and organization across multiple files
    prepareForCodeGeneration(info);

    return dtMap;
  }

  /**
   * Validates that required definition files (data type, system common, etc.) exist in the
   * given map.
   */
  private void checkAndComplementFileLevelConsistencyCheckBl(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {
    if (!rootInfoMap.containsKey(DataKindEnum.DATA_TYPE)) {
      new Violations().add("MSG_ERR_DT_FILE_EXIST", systemName).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.DATA_TYPE)
        && rootInfoMap.containsKey(DataKindEnum.ENUM)) {
      new Violations().add("MSG_ERR_NO_DT_FILE_THOUGH_ENUM_EXISTS", systemName).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.DB)
        && rootInfoMap.containsKey(DataKindEnum.DB_COMMON)) {
      new Violations().add("MSG_ERR_DB_NOT_EXIST_ALTHOUGH_DB_COMMON_EXISTS", systemName)
          .throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.DB)) {
      new Violations().add("MSG_ERR_DB_FILE_NOT_EXIST", systemName).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.SYSTEM_COMMON)) {
      new Violations().add("MSG_ERR_SYSTEM_COMMON_INFO_NOT_EXIST", systemName).throwIfAny();
    }
  }

  /**
   * Orchestrates all preparation steps that run after Excel files are read, including relation
   * back-references and misc-info merging.
   */
  private void prepareForCodeGeneration(CodeGenContext info) {

    // For entries in DBInfo and DbCommonInfo that have a bidirectional relation,
    // register back-reference info on the referenced side to enable additional generation there.
    List<DbOrClassColumnInfo.RelationRefInfo> relRefInfoList = new ArrayList<>();
    // Create a combined list of DB and DBCommon for iteration
    List<DbOrClassTableInfo> list = new ArrayList<>(info.getDbRootInfo().tableList);
    list.addAll(info.getDbCommonRootInfo().tableList);
    for (DbOrClassTableInfo ti : list) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.isRelation()) {
          RelationKindEnum relationKind = ci.getRelationKind();
          if (relationKind == null) {
            throw new IllegalStateException(
                "isRelation() guarantees getRelationKind() is non-null");
          }
          relRefInfoList.add(new DbOrClassColumnInfo.RelationRefInfo(ci.isRelationBidirectinal(),
              relationKind.getInverse(), ci.getRelationRefTable(), ci.getRelationRefCol(),
              ci.getRelationRefFieldName(), ti.getName(),
              StringUtil.getLowerCamelFromSnake(ci.getName()),
              ci.getEffectiveRelationObjVarName()));
        }
      }
    }

    // For bidirectional relations, populate the collected info into the referenced side
    for (DbOrClassColumnInfo.RelationRefInfo bdInfo : relRefInfoList) {
      boolean found = false;

      // It is unlikely that a common table is used as a reference target, so loop only over dbInfo
      for (DbOrClassTableInfo ti : info.getDbRootInfo().tableList) {
        for (DbOrClassColumnInfo ci : ti.columnList) {
          if (ti.getName().equals(bdInfo.getDstTableName())
              && ci.getName().equals(bdInfo.getDstColumnName())) {
            ci.getRelationRefInfoList().add(bdInfo);
            found = true;
          }
        }
      }

      if (!found) {
        throw new RuntimeException("not found : tableName = " + bdInfo.getDstTableName()
            + ", columnName = " + bdInfo.getDstColumnName());
      }
    }

    // DB and DataType:
    new PreparerForDbAndDataType().prepare();

    // miscRemovedData: add info to DbOrClassInfo
    new PreparerForMiscRemovedData().prepare();

    // miscGroupInfo: add info to DbOrClassInfo
    new PreparerForMiscGroup().prepare();

    // miscOptimisticLockInfo: consolidate into DbOrClassInfo
    new PreparerForMiscOptimisticLock().prepare();
  }

  @SuppressWarnings("null")
  private void checkForChildTable(String sysName, DbOrClassRootInfo dbOrClassRootInfo) {
    if (dbOrClassRootInfo.tableList.size() == 0) {
      new Violations().add(new BusinessViolation("MSG_ERR_DB_NO_DATA_FOUND", sysName)).throwIfAny();
    }

    List<String> tableNameSet = dbOrClassRootInfo.tableList.stream().map(e -> e.getName()).toList();

    for (DbOrClassTableInfo ti : dbOrClassRootInfo.tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (StringUtils.isNotEmpty(ci.getRelationRefTable())) {

          // relation: refering to table name existence check
          if (!tableNameSet.contains(ci.getRelationRefTable())) {
            new Violations().add(new BusinessViolation("MSG_ERR_DB_REFER_TO_TABLE_NAME_NOT_FOUND",
                sysName, ti.getName(), ci.getName(), ci.getRelationRefTable())).throwIfAny();
          }

          DbOrClassTableInfo refTi = dbOrClassRootInfo.tableList.stream()
              .collect(Collectors.toMap(e -> e.getName(), e -> e)).get(ci.getRelationRefTable());

          // relation: refering to column name existence check
          List<String> refTiColNameList = refTi.columnList.stream().map(e -> e.getName()).toList();
          if (!refTiColNameList.contains(ci.getRelationRefCol())) {
            new Violations().add(new BusinessViolation("MSG_ERR_DB_REFER_TO_COLUMN_NAME_NOT_FOUND",
                sysName, ti.getName(), ci.getName(), ci.getRelationRefCol())).throwIfAny();
          }
        }
      }
    }
  }

  private void checkAndComplementForParentAndChildTable(String systemName,
      DbOrClassRootInfo dbCommonRootInfo, DbOrClassRootInfo dbRootInfo) {
    // Loop again to add information
    for (DbOrClassTableInfo tableInfo : dbRootInfo.tableList) {
      boolean hasS = false;
      boolean hasU = false;

      // Merge dbInfo and dbCommonInfo columns since judgment requires both
      List<DbOrClassColumnInfo> commonAddedColumnList = new ArrayList<>();
      commonAddedColumnList.addAll(tableInfo.columnList);
      if (dbCommonRootInfo.tableList.size() > 0) {
        commonAddedColumnList.addAll(dbCommonRootInfo.tableList.get(0).columnList);
      }

      for (DbOrClassColumnInfo colInfo : commonAddedColumnList) {
        if (colInfo.isPk()) {
          if (colInfo.isNullable()) {
            new Violations().add(new BusinessViolation("MSG_ERR_PK_MUST_NOT_BE_NULLABLE",
                systemName, colInfo.getDataType(), tableInfo.getName(), colInfo.getName()))
                .throwIfAny();
          }

          // Check because having two surrogate key columns is not allowed
          if (hasS) {
            new Violations()
                .add(new BusinessViolation("MSG_ERR_SURROGATE_KEY_DUPLICATED", tableInfo.getName()))
                .throwIfAny();
          }

          hasS = true;
        }

        if (colInfo.isUniqueConstraint()) {
          hasU = true;
        }
      }

      // PK is required. SystemCommon is treated specially.
      if (!tableInfo.getName().equals("SYSTEM_COMMON") && !hasS) {
        new Violations().add(new BusinessViolation("MSG_ERR_PK_REQUIRED", tableInfo.getName()))
            .throwIfAny();
      }

      tableInfo.setHasUniqueConstraint(hasU);
    }
  }

  /**
   * Checks consistency between table definitions and group definitions, and complements
   * group-related data.
   */
  private void checkAndComplementForTableAndGroup(String systemName,
      DbOrClassRootInfo dbCommonRootInfo, DbOrClassRootInfo dbRootInfo,
      MiscGroupRootInfo groupRootInfo) {

    final String colName = groupRootInfo.getColumnName();

    // Exit if group definition is absent
    if (!groupRootInfo.isDefined()) {
      return;
    }

    // Check whether the column defined in groupRootInfo exists in the parent/child tables
    // (the child is considered to have the column if it exists in any child table)
    boolean parentTableHasGroupCol = dbCommonRootInfo.tableList.get(0).hasColumn(colName);
    boolean childTableHasGroupCol = false;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasColumn(colName)) {
        childTableHasGroupCol = true;
        break;
      }
    }

    // Having the column in both parent and child is invalid, but it is caught by the
    // "same column exists in both parent and child" check, so it is not checked here.
    // Do check for the case where it exists in neither parent nor child.
    if (!parentTableHasGroupCol && !childTableHasGroupCol) {
      new Violations().add(new BusinessViolation("MSG_ERR_COMMON_GROUP_COL_NOT_FOUND", systemName))
          .throwIfAny();
    }

    // When "group_id" is used as the common group column name, the master group table may want to
    // hold the column as "id" rather than "group_id". For this purpose each table has a
    // "custom group column" setting. As this background implies, a custom group column can only
    // exist in child tables, at most once.

    // It makes no sense to have a "custom group column" in systemCommon, so treat it as an error.
    // (The common group setting should be used instead.)
    if (dbCommonRootInfo.tableList.get(0).hasCustomGroupColumn()) {
      new Violations()
          .add(new BusinessViolation("MSG_ERR_SYSTEM_COMMON_ENTITY_CANNOT_HAVE_CUSTOM_GROUP_COLUMN",
              systemName))
          .throwIfAny();
    }

    // Having more than one "custom group column" in child tables is an error
    int numOfCustomGroupColumns = 0;
    String customGroupTableName = null;
    String customGroupColumnName = null;
    for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
      if (ti.hasCustomGroupColumn()) {
        numOfCustomGroupColumns++;
        customGroupTableName = ti.getName();
        customGroupColumnName = ti.getCustomGroupColumn().getName();
        if (numOfCustomGroupColumns > 1) {
          new Violations()
              .add(new BusinessViolation("MSG_ERR_MULTIPLE_CUSTOM_GROUP_COLUMN_CANNOT_EXIST",
                  systemName))
              .throwIfAny();
        }
      }
    }

    // Add customGroup information
    groupRootInfo.setCustomGroupTableName(customGroupTableName);
    groupRootInfo.setCustomGroupColumnName(customGroupColumnName);
  }

  /**
   * Creates and returns a map keyed by DataTypeInfo name, built from the data-type entries in
   * rootInfoMap.
   */
  public Map<String, DataTypeInfo> createDataTypeMap(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {
    // First String is system name, second is dataType name. All dataTypeInfo is stored here.
    Map<String, DataTypeInfo> dtMap = new HashMap<String, DataTypeInfo>();

    // Fill dtMap with data
    // By design it is possible to build a system using only dataTypeRefInfo without dataTypeInfo,
    // so perform an existence check here.
    if (rootInfoMap.get(DataKindEnum.DATA_TYPE) != null) {
      // Build the map

      DataTypeRootInfo dtRootInfo = (DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE);
      // Store the dataType information from dataTypeList into the map.
      for (DataTypeInfo dtInfo : Objects.requireNonNull(dtRootInfo).dataTypeList) {
        dtMap.put(dtInfo.getDataTypeName(), dtInfo);
      }
    }

    return dtMap;
  }

  private void putDataTypeInfoIntoColInfo(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap, Map<String, DataTypeInfo> dtMap) {

    for (AbstractRootInfo rootInfo : rootInfoMap.values()) {

      if (rootInfo instanceof DbOrClassRootInfo dbOrClassRootInfo) {
        // DbOrClassRootInfo
        for (DbOrClassTableInfo ti : dbOrClassRootInfo.tableList) {
          for (DbOrClassColumnInfo ci : ti.columnList) {
            ci.setDtInfo(checkAndGetDataTypeInfo(dtMap, ci.getDataType(), systemName,
                "tableName = " + ti.getName() + ", columnName = " + ci.getName()));
          }
        }

      } else if (rootInfo instanceof EnumRootInfo enumRootInfo) {
        // EnumRootInfo
        for (EnumClassInfo ei : enumRootInfo.enumClassList) {
          ei.setDtInfo(checkAndGetDataTypeInfo(dtMap, ei.getDataTypeName(), systemName,
              "enumName = " + ei.getEnumName()));
        }

      } else if (rootInfo instanceof MiscGroupRootInfo grpInfo) {
        // MiscGroupRootInfo

        if (grpInfo.isDefined()) {
          grpInfo.setDtInfo(checkAndGetDataTypeInfo(dtMap, grpInfo.getDataTypeName(), systemName,
              "grouping column: " + grpInfo.getColumnName()));
        }
      }
    }
  }

  @SuppressWarnings("null")
  private DataTypeInfo checkAndGetDataTypeInfo(Map<String, DataTypeInfo> dtMap, String dataTypeName,
      String systemName, String placeInfo) {
    DataTypeInfo dtInfo = dtMap.get(dataTypeName);
    if (dtInfo == null) {
      new Violations().add(new BusinessViolation("MSG_ERR_DESIGNATED_DT_NOT_EXIST_IN_DT_INFO",
          dataTypeName, systemName, placeInfo)).throwIfAny();
    }

    return dtInfo;
  }
}
