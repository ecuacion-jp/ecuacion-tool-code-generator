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
package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.NormalSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithMultipleValues;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
 * Holds DB or class table information including its columns, unique constraints, and relation
 * definitions.
 */
@SuppressWarnings("NullAway.Init")
public class DbOrClassTableInfo extends AbstractInfo {
  @Valid
  public List<DbOrClassColumnInfo> columnList = new ArrayList<>();

  @NotEmpty
  @Size(min = 1, max = 50)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String name;

  // private boolean isSurrogateKeyStorategy = false;
  private boolean hasUniqueConstraint = false;

  /** Constructs an empty instance for later population. */
  @SuppressWarnings("null")
  public DbOrClassTableInfo() {

  }

  /** Constructs an instance with the given table name. */
  public DbOrClassTableInfo(String tableName) {
    this.name = tableName;
  }

  // name

  public String getName() {
    return name.equals("SYSTEM_COMMON_ENTITY") ? "SYSTEM_COMMON" : name;
  }

  public String getNameCpCamel() {
    return StringUtil.getUpperCamelFromSnake(getName());
  }

  public String getNameCamel() {
    return StringUtil.getLowerCamelFromSnake(getName());
  }

  public void setTableName(String tableName) {
    this.name = tableName;
  }

  /*
   * column
   */

  /** Returns {@code true} if this table has a column with the given name. */
  public boolean hasColumn(String colName) {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getName().equals(colName)) {
        return true;
      }
    }

    return false;
  }

  /** Returns the column matching the given name, or {@code null} if absent. */
  @SuppressWarnings({"NullAway", "null"})
  public DbOrClassColumnInfo getColumn(String colName) {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getName().equals(colName)) {
        return ci;
      }
    }

    return null;
  }

  /*
   * all columns
   */

  /** Returns all columns of this table combined with the SYSTEM_COMMON columns. */
  public List<DbOrClassColumnInfo> getColumnListIncludingSystemCommon() {
    List<DbOrClassColumnInfo> list = new ArrayList<>(columnList);
    list.addAll(getInfo().getDbCommonRootInfo().tableList.get(0).columnList);

    return list;
  }

  /** Returns all columns where {@code isJavaOnly} is false. */
  public List<DbOrClassColumnInfo> getNonJavaOnlyColumns() {
    return columnList.stream().filter(ci -> !ci.getIsJavaOnly()).toList();
  }

  /*
   * kata
   */

  /** Returns {@code true} if this table has at least one column of the given data type kind. */
  public boolean hasColumnWithKata(DataTypeKataEnum kata) {
    return columnList.stream().map(ci -> ci.getDtInfo().getKata()).toList().contains(kata);
  }

  /** Returns all columns of the given data type kind. */
  public List<DbOrClassColumnInfo> getColumnListWithKata(DataTypeKataEnum kata) {
    return columnList.stream().filter(ci -> ci.getDtInfo().getKata() == kata).toList();
  }

  /**
   * Returns {@code true} if this table has at least one column whose data type kind matches
   * any of the given kinds.
   */
  @SuppressWarnings("null")
  public boolean hasColumnWithAnyOfKatas(DataTypeKataEnum... katas) {
    List<DataTypeKataEnum> tableKataList =
        columnList.stream().map(ci -> ci.getDtInfo().getKata()).toList();

    for (DataTypeKataEnum argKata : katas) {
      if (tableKataList.contains(argKata)) {
        return true;
      }
    }

    return false;
  }

  /** Returns all columns whose data type kind matches any of the given kinds. */
  public List<DbOrClassColumnInfo> getColumnListWithAnyOfKatas(DataTypeKataEnum... katas) {
    return columnList.stream().filter(ci -> Arrays.asList(katas).contains(ci.getDtInfo().getKata()))
        .toList();
  }

  /*
   * pk
   */

  /** Returns the primary-key column, or {@code null} if no column is marked as PK. */
  @SuppressWarnings({"NullAway", "null"})
  public DbOrClassColumnInfo getPkColumn() {
    List<DbOrClassColumnInfo> list = columnList.stream().filter(ci -> ci.isPk()).toList();
    return list.size() == 0 ? null : list.get(0);
  }

  /** Returns {@code true} if this table has a primary-key column. */
  public boolean hasPkColumn() {
    return getPkColumn() != null;
  }

  public DbOrClassColumnInfo getPkColumnIncludingSystemCommon() {
    // pk (surrogate key) always exists.
    return getColumnListIncludingSystemCommon().stream().filter(ci -> ci.isPk()).toList().get(0);
  }

  /*
   * group
   */

  /** Returns {@code true} if this table has a group column (excluding SYSTEM_COMMON). */
  public boolean hasGroupColumn() {
    return getGroupColumn() != null;
  }

  /** Returns the group column for this table, or {@code null} if absent. */
  @SuppressWarnings({"NullAway", "null"})
  public DbOrClassColumnInfo getGroupColumn() {
    // Common columns are not included, so no detailed consistency check here — just return true if
    // found.
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.isGroupColumn()) {
        return ci;
      }
    }

    return null;
  }

  /** Returns {@code true} if this table has a group column, considering SYSTEM_COMMON columns. */
  public boolean hasGroupColumnIncludingSystemCommon() {
    return getGroupColumnIncludingSystemCommon() != null;
  }

  /** Returns the group column considering SYSTEM_COMMON, or {@code null} if not applicable. */
  @SuppressWarnings({"NullAway", "null"})
  public DbOrClassColumnInfo getGroupColumnIncludingSystemCommon() {

    // Return null immediately if group is not defined
    if (!getInfo().getGroupRootInfo().isDefined()) {
      return null;
    }

    // Hold in a List for subsequent checks
    List<DbOrClassColumnInfo> groupCiList = new ArrayList<>();

    for (DbOrClassColumnInfo ci : getColumnListIncludingSystemCommon()) {
      if (ci.isGroupColumn()) {
        groupCiList.add(ci);
      }
    }

    if (getInfo().getGroupRootInfo().getTableNamesWithoutGrouping().contains(name)) {
      if (groupCiList.size() > 0) {
        throw new RuntimeException("The table is listed in 'TABLE_NAMES_WITHOUT_GROUPING'"
            + " but has group column: table_name = " + name);
      }

    } else {
      if (groupCiList.size() == 0) {
        throw new RuntimeException("Table '" + name
            + "' is not listed in 'TABLE_NAMES_WITHOUT_GROUPING', but it has no group column.");

      } else if (groupCiList.size() != 1) {
        throw new RuntimeException("Number of Group columns in a table must be 1: " + name);
      }
    }

    return groupCiList.size() == 0 ? null : groupCiList.get(0);
  }

  /** Returns {@code true} if this table has a custom group column. */
  public boolean hasCustomGroupColumn() {
    return getCustomGroupColumn() != null;
  }

  /** Returns the custom group column, or {@code null} if absent. */
  @SuppressWarnings({"NullAway", "null"})
  public DbOrClassColumnInfo getCustomGroupColumn() {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.isCustomGroupColumn()) {
        return ci;
      }
    }

    return null;
  }

  /*
   * soft delete
   */

  /** Returns {@code true} if this table's own columns contain the soft-delete flag column. */
  public boolean hasSoftDeleteFieldExcludingSystemCommon() {
    return softDeleteExistenceCheck(columnList, getName());
  }

  /** Returns {@code true} if the SYSTEM_COMMON columns contain the soft-delete flag column. */
  public boolean hasSoftDeleteFieldInSystemCommon() {
    List<DbOrClassColumnInfo> dbCommonCi =
        getInfo().getDbCommonRootInfo().tableList.get(0).columnList;
    return softDeleteExistenceCheck(dbCommonCi, getName());
  }

  /**
   * This value is derived from the two methods above, so no dedicated field is held — only a
   * method is provided.
   */
  public boolean hasSoftDeleteFieldInludingSystemCommon() {
    return hasSoftDeleteFieldExcludingSystemCommon() || hasSoftDeleteFieldInSystemCommon();
  }

  private boolean softDeleteExistenceCheck(List<DbOrClassColumnInfo> columnList, String tableName) {

    MiscSoftDeleteRootInfo removedDataInfo = getInfo().getRemovedDataRootInfo();

    boolean hasRemovedDataColumn = false;
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getName().equals(removedDataInfo.getColumnName())) {
        if (ci.getDataType().equals(removedDataInfo.getDataTypeName())) {
          hasRemovedDataColumn = true;

        } else {
          // Treat as an error if the column name matches but the DataType differs
          new Violations()
              .add(new BusinessViolation("MSG_ERR_DT_OF_COL_FOR_REMOVED_DATA_COL_DIFFER",
                  getInfo().getSystemName(), tableName, ci.getName(), ci.getDataType(),
                  removedDataInfo.getDataTypeName()))
              .throwIfAny();
        }
      }
    }

    return hasRemovedDataColumn;
  }

  /*
   * version
   */

  @SuppressWarnings({"NullAway", "null"})
  private DbOrClassColumnInfo getVersionColumn(List<DbOrClassColumnInfo> columnList) {
    List<DbOrClassColumnInfo> versionColList = columnList.stream()
        .filter(ci -> ci.getName().equals(getInfo().getOptimisticLockRootInfo().getColumnName()))
        .toList();

    return versionColList.size() == 0 ? null : versionColList.get(0);
  }

  /** Returns the version column for optimistic locking, or {@code null} if absent. */
  @SuppressWarnings("NullAway")
  public DbOrClassColumnInfo getVersionColumn() {
    return getVersionColumn(columnList);
  }

  /** Returns {@code true} if this table has an optimistic-lock version column. */
  public boolean hasVersionColumn() {
    return getVersionColumn() != null;
  }

  /**
   * Returns the optimistic-lock version column, searching both this table's columns and
   * SYSTEM_COMMON columns.
   */
  public DbOrClassColumnInfo getVersionColumnIncludingSystemCommon() {
    return getVersionColumn(getColumnListIncludingSystemCommon());
  }

  /** Returns {@code true} if a group column exists considering SYSTEM_COMMON columns. */
  public boolean hasVersionColumnIncludingSystemCommon() {
    return getGroupColumnIncludingSystemCommon() != null;
  }

  /*
   * unique constraint
   */

  /** Sets whether this table has a unique constraint. */
  public void setHasUniqueConstraint(boolean hasUniqueConstraint) {
    this.hasUniqueConstraint = hasUniqueConstraint;
  }

  /** Returns {@code true} if this table has at least one unique constraint. */
  public boolean hasUniqueConstraint() {
    return hasUniqueConstraint;
  }

  /**
   * Generates the {@code @Table} annotation string, including unique constraints and index
   * definitions.
   */
  public String getTableAnnotationString(DbOrClassTableInfo tableInfo) {
    ParamListGen paramGenList = new ParamListGen();
    // name
    paramGenList.add(new ParamGenWithSingleValue("name", tableInfo.getName(), true));

    // uniqueConstraints
    if (tableInfo.hasUniqueConstraint) {
      // Build the list of unique key column names
      List<String> uniqueKeyColumns = new ArrayList<>();
      for (DbOrClassColumnInfo col : tableInfo.columnList) {
        if (col.isUniqueConstraint()) {
          uniqueKeyColumns.add(col.getName());
        }
      }
      // columnNames = {"id_1" , "id_2"})
      ParamGen columnNamesParam = new ParamGenWithMultipleValues("columnNames",
          uniqueKeyColumns.toArray(new String[uniqueKeyColumns.size()]), true);
      // uniqueConstraints={@UniqueConstraint(columnNames = {"id_1" , "id_2"})}
      paramGenList.add(new ParamGenWithMultipleValues("uniqueConstraints", new AnnotationGen[] {
          new NormalSingleAnnotationGen("UniqueConstraint", ElementType.TYPE, columnNamesParam)}));
    }

    // index
    List<String[]> indexList = getIndexList();
    if (indexList.size() > 0) {
      List<NormalSingleAnnotationGen> indexAnnotationList = new ArrayList<>();
      for (String[] index : indexList) {
        // Set indexName
        StringBuilder indexNameColList = new StringBuilder();
        for (String col : index) {
          indexNameColList.append("_" + col);
        }

        String indexName = "IDX" + indexNameColList.toString();
        // Set columnList. Unlike the {} bracket style used for multi-value annotations,
        // columnList uses the "col1, col2" format.
        boolean is1stTime = true;
        StringBuilder columnList = new StringBuilder();
        for (String colName : index) {
          if (is1stTime) {
            is1stTime = false;

          } else {
            columnList.append(", ");
          }

          columnList.append(colName);
        }

        ParamListGen paramList =
            new ParamListGen(new ParamGenWithSingleValue("name", indexName, true),
                new ParamGenWithSingleValue("columnList", columnList.toString(), true));
        indexAnnotationList
            .add(new NormalSingleAnnotationGen("Index", ElementType.TYPE, paramList));
      }
      // indexes={@Index(name = "TEST_TABLE", columnList ="...", ...}
      paramGenList.add(new ParamGenWithMultipleValues("indexes",
          indexAnnotationList.toArray(new NormalSingleAnnotationGen[indexAnnotationList.size()])));
    }

    // @Table
    NormalSingleAnnotationGen table =
        new NormalSingleAnnotationGen("Table", ElementType.TYPE, paramGenList);
    return table.generateString(ElementType.TYPE);
  }

  /*
   * relation
   */

  /** Returns all columns that have a relation definition. */
  public List<DbOrClassColumnInfo> getRelationColumnList() {
    return columnList.stream().filter(ci -> ci.isRelation()).toList();
  }

  /** Returns all relation columns excluding the group column. */
  public List<DbOrClassColumnInfo> getRelationColumnWithoutGroupList() {
    return columnList.stream().filter(ci -> ci.isRelation())
        .filter(ci -> !ci.getName().equals(getInfo().getGroupRootInfo().getColumnName())).toList();
  }

  /** Returns {@code true} if this table has at least one relation column. */
  public boolean hasRelationColumn() {
    return getRelationColumnList().size() > 0;
  }

  /** Returns {@code true} if this table has at least one bidirectional relation column. */
  public boolean hasBidirectionalRelation() {
    return columnList.stream().filter(col -> col.isRelation() && col.isRelationBidirectinal())
        .toList().size() > 0;
  }

  /**
   * Returns all columns that have a bidirectional relation reference registered from another
   * table.
   */
  public List<DbOrClassColumnInfo> getBidirectionalRelationRefColumnList() {
    return columnList.stream().filter(col -> col.hasBidirectionalRelationRef()).toList();
  }

  /** Returns {@code true} if this table has any bidirectional relation reference columns. */
  public boolean hasBidirectionalRelationRefColumn() {
    return getBidirectionalRelationRefColumnList().size() > 0;
  }

  /**
   * Returns {@code true} if this table has any relation columns or bidirectional relation
   * references.
   */
  public boolean hasAnyRelationsOrRefs() {
    return hasRelationColumn() || hasBidirectionalRelationRefColumn();
  }

  private List<String[]> getIndexList() {

    Map<Integer, DbOrClassColumnInfo> index1Map = new HashMap<>();
    Map<Integer, DbOrClassColumnInfo> index2Map = new HashMap<>();
    Map<Integer, DbOrClassColumnInfo> index3Map = new HashMap<>();

    for (DbOrClassColumnInfo colInfo : columnList) {
      if (colInfo.getIndex1() != null) {
        index1Map.put(colInfo.getIndex1(), colInfo);
      }
    }

    for (DbOrClassColumnInfo colInfo : columnList) {
      if (colInfo.getIndex2() != null) {
        index2Map.put(colInfo.getIndex2(), colInfo);
      }
    }

    for (DbOrClassColumnInfo colInfo : columnList) {
      if (colInfo.getIndex3() != null) {
        index3Map.put(colInfo.getIndex3(), colInfo);
      }
    }

    List<String[]> list = new ArrayList<>();
    if (getIndex(index1Map, 1) != null && getIndex(index1Map, 1).length != 0) {
      list.add(getIndex(index1Map, 1));
    }

    if (getIndex(index2Map, 2) != null && getIndex(index1Map, 2).length != 0) {
      list.add(getIndex(index2Map, 2));
    }

    if (getIndex(index3Map, 3) != null && getIndex(index1Map, 3).length != 0) {
      list.add(getIndex(index3Map, 3));
    }

    return list;
  }

  private String[] getIndex(Map<Integer, DbOrClassColumnInfo> indexMap, int indexSerial) {

    if (indexMap.size() == 0) {
      return new String[] {};
    }

    List<String> index = new ArrayList<>();
    for (int i = 1; i <= indexMap.size(); i++) {
      if (!indexMap.containsKey(i)) {
        new Violations().add(new BusinessViolation("MSG_ERR_INDEX_NUMBER_NOT_CONTINUOUS_FROM_1", "",
            name, Integer.toString(indexSerial))).throwIfAny();
      }

      DbOrClassColumnInfo indexedCol = indexMap.get(i);
      if (indexedCol == null) {
        throw new IllegalStateException(
            "Index column missing for index serial " + indexSerial + " on table " + name);
      }
      index.add(indexedCol.getName());
    }

    return index.toArray(new String[index.size()]);
  }

  /** Runs the {@code afterReading} consistency check for all columns in this table. */
  public void dataConsistencyCheck() {
    for (DbOrClassColumnInfo info : columnList) {
      info.afterReading();
    }
  }
}
