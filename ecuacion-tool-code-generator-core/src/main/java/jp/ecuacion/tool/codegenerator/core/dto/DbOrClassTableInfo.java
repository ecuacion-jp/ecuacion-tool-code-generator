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
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.NormalSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithMultipleValues;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class DbOrClassTableInfo extends AbstractInfo {
  @Valid
  public List<DbOrClassColumnInfo> columnList = new ArrayList<>();

  @NotEmpty
  @Size(min = 1, max = 50)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String name;

  // private boolean isSurrogateKeyStorategy = false;
  private boolean hasUniqueConstraint = false;

  public DbOrClassTableInfo() {

  }

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

  public void setTableName(String tableName) throws AppException {
    this.name = tableName;
  }

  /*
   * column
   */

  public boolean hasColumn(String colName) {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getName().equals(colName)) {
        return true;
      }
    }

    return false;
  }

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

  public List<DbOrClassColumnInfo> getColumnListIncludingSystemCommon() {
    List<DbOrClassColumnInfo> list = new ArrayList<>(columnList);
    list.addAll(info.dbCommonRootInfo.tableList.get(0).columnList);

    return list;
  }

  /*
   * kata
   */

  public boolean hasColumnWithKata(DataTypeKataEnum kata) {
    return columnList.stream().map(ci -> ci.getDtInfo().getKata()).toList().contains(kata);
  }

  public List<DbOrClassColumnInfo> getColumnListWithKata(DataTypeKataEnum kata) {
    return columnList.stream().filter(ci -> ci.getDtInfo().getKata() == kata).toList();
  }

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

  public List<DbOrClassColumnInfo> getColumnListWithAnyOfKatas(DataTypeKataEnum... katas) {
    return columnList.stream().filter(ci -> Arrays.asList(katas).contains(ci.getDtInfo().getKata()))
        .toList();
  }

  /*
   * pk
   */

  public DbOrClassColumnInfo getPkColumn() {
    List<DbOrClassColumnInfo> list = columnList.stream().filter(ci -> ci.isPk()).toList();
    return list.size() == 0 ? null : list.get(0);
  }

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

  public boolean hasGroupColumn() {
    return getGroupColumn() != null;
  }

  public DbOrClassColumnInfo getGroupColumn() {
    // commonを含めないので、ここでは整合性は細かく確認せず単純にあればtrueを返して終了。
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.isGroupColumn()) {
        return ci;
      }
    }

    return null;
  }

  public boolean hasGroupColumnIncludingSystemCommon() {
    return getGroupColumnIncludingSystemCommon() != null;
  }

  public DbOrClassColumnInfo getGroupColumnIncludingSystemCommon() {

    // group定義がされていない場合はnullを返して終了。
    if (!info.groupRootInfo.isDefined()) {
      return null;
    }

    // 後続のチェックのためにListで保持
    List<DbOrClassColumnInfo> groupCiList = new ArrayList<>();

    for (DbOrClassColumnInfo ci : getColumnListIncludingSystemCommon()) {
      if (ci.isGroupColumn()) {
        groupCiList.add(ci);
      }
    }

    if (info.groupRootInfo.getTableNamesWithoutGrouping().contains(name)) {
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

  public boolean hasCustomGroupColumn() {
    return getCustomGroupColumn() != null;
  }

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

  public boolean hasSoftDeleteFieldExcludingSystemCommon() throws BizLogicAppException {
    return softDeleteExistenceCheck(columnList, getName());
  }

  public boolean hasSoftDeleteFieldInSystemCommon() throws BizLogicAppException {
    List<DbOrClassColumnInfo> dbCommonCi = info.dbCommonRootInfo.tableList.get(0).columnList;
    return softDeleteExistenceCheck(dbCommonCi, getName());
  }

  /**
   * これは前述の2つの値から決まるので、個別にfieldは持たずmethodのみ用意しておく.
   */
  public boolean hasSoftDeleteFieldInludingSystemCommon() throws BizLogicAppException {
    return hasSoftDeleteFieldExcludingSystemCommon() || hasSoftDeleteFieldInSystemCommon();
  }

  private boolean softDeleteExistenceCheck(List<DbOrClassColumnInfo> columnList, String tableName)
      throws BizLogicAppException {

    MiscSoftDeleteRootInfo removedDataInfo = info.removedDataRootInfo;

    boolean hasRemovedDataColumn = false;
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getName().equals(removedDataInfo.getColumnName())) {
        if (ci.getDataType().equals(removedDataInfo.getDataTypeName())) {
          hasRemovedDataColumn = true;

        } else {
          // カラム名が一緒なのにDataTypeが異なる場合はエラー扱いとする
          throw new BizLogicAppException("MSG_ERR_DT_OF_COL_FOR_REMOVED_DATA_COL_DIFFER",
              info.systemName, tableName, ci.getName(), ci.getDataType(),
              removedDataInfo.getDataTypeName());
        }
      }
    }

    return hasRemovedDataColumn;
  }

  /*
   * version
   */

  private DbOrClassColumnInfo getVersionColumn(List<DbOrClassColumnInfo> columnList) {
    List<DbOrClassColumnInfo> versionColList = columnList.stream()
        .filter(ci -> ci.getName().equals(info.optimisticLockRootInfo.getColumnName())).toList();

    return versionColList.size() == 0 ? null : versionColList.get(0);
  }

  public DbOrClassColumnInfo getVersionColumn() {
    return getVersionColumn(columnList);
  }

  public boolean hasVersionColumn() {
    return getVersionColumn() != null;
  }

  public DbOrClassColumnInfo getVersionColumnIncludingSystemCommon() {
    return getVersionColumn(getColumnListIncludingSystemCommon());
  }

  public boolean hasVersionColumnIncludingSystemCommon() {
    return getGroupColumnIncludingSystemCommon() != null;
  }

  /*
   * unique constraint
   */

  public void setHasUniqueConstraint(boolean hasUniqueConstraint) {
    this.hasUniqueConstraint = hasUniqueConstraint;
  }

  public boolean hasUniqueConstraint() {
    return hasUniqueConstraint;
  }

  public String getTableAnnotationString(DbOrClassTableInfo tableInfo) throws BizLogicAppException {
    ParamListGen paramGenList = new ParamListGen();
    // name
    paramGenList.add(new ParamGenWithSingleValue("name", tableInfo.getName(), true));

    // uniqueConstraints
    if (tableInfo.hasUniqueConstraint) {
      // uniqueKeyのcolumnNameのリストを作成
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
        // indexNameを設定
        StringBuilder indexNameColList = new StringBuilder();
        for (String col : index) {
          indexNameColList.append("_" + col);
        }

        String indexName = "IDX" + indexNameColList.toString();
        // columnListを設定。columnListは、valueが複数項目存在する場合のannotation標準である{}で括る方式ではなく"col1, col2"という形式。
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

  public List<DbOrClassColumnInfo> getRelationColumnList() {
    return columnList.stream().filter(ci -> ci.isRelation()).toList();
  }

  public boolean hasRelationColumns() {
    return getRelationColumnList().size() > 0;
  }

  public List<DbOrClassColumnInfo> getRelationColumnWithoutGroupList() {
    return columnList.stream().filter(ci -> ci.isRelation())
        .filter(ci -> !ci.getName().equals(info.groupRootInfo.getColumnName())).toList();
  }

  public boolean hasBidirectionalRelation() {
    return columnList.stream().filter(col -> col.isRelation() && col.isRelationBidirectinal())
        .toList().size() > 0;
  }

  public boolean hasBidirectionalRelationRef() {
    return columnList.stream().filter(col -> col.hasBidirectionalRelationRef()).toList().size() > 0;
  }

  public boolean hasAnyRelationsOrRefs() {
    return hasRelationColumns() || hasBidirectionalRelationRef();
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
        throw new RuntimeException(new BizLogicAppException(
            "MSG_ERR_INDEX_NUMBER_NOT_CONTINUOUS_FROM_1", "", name, Integer.toString(indexSerial)));
      }

      index.add(indexMap.get(i).getName());
    }

    return index.toArray(new String[index.size()]);
  }

  public void dataConsistencyCheck() throws AppException {
    for (DbOrClassColumnInfo info : columnList) {
      info.afterReading();
    }
  }
}
