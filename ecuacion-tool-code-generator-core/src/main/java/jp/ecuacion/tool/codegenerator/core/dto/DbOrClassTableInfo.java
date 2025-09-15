package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
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
  private String tableName;

  // private boolean isSurrogateKeyStorategy = false;
  private boolean hasUniqueConstraint = false;

  public DbOrClassTableInfo() {

  }

  public DbOrClassTableInfo(String tableName) {
    this.tableName = tableName;
  }

  // tableName
  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) throws AppException {
    this.tableName = tableName;
  }

  public boolean hasColumn(String colName) {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getColumnName().equals(colName)) {
        return true;
      }
    }

    return false;
  }

  public DbOrClassColumnInfo getColumn(String colName) {
    for (DbOrClassColumnInfo ci : columnList) {
      if (ci.getColumnName().equals(colName)) {
        return ci;
      }
    }

    return null;
  }

  public DbOrClassColumnInfo getPkColumn() {
    // pk（surrogate key）は必ず存在するのでこの書き方でOK
    return columnList.stream().filter(col -> col.isPk()).toList().get(0);
  }

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

    // こちらはループを回す対象のカラムリスト。systemCommonも含める。
    List<DbOrClassColumnInfo> list = new ArrayList<>(columnList);
    list.addAll(info.dbCommonRootInfo.tableList.get(0).columnList);

    for (DbOrClassColumnInfo ci : list) {
      if (ci.isGroupColumn()) {
        groupCiList.add(ci);
      }
    }

    if (info.groupRootInfo.getTableNamesWithoutGrouping().contains(tableName)) {
      if (groupCiList.size() > 0) {
        throw new RuntimeException("The table is listed in 'TABLE_NAMES_WITHOUT_GROUPING'"
            + " but has group column: table_name = " + tableName);
      }

    } else {
      if (groupCiList.size() == 0) {
        throw new RuntimeException("Table '" + tableName
            + "' is not listed in 'TABLE_NAMES_WITHOUT_GROUPING', but it has no group column.");

      } else if (groupCiList.size() != 1) {
        throw new RuntimeException("Number of Group columns in a table must be 1: " + tableName);
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

  // soft delete関連
  public boolean hasSoftDeleteFieldExcludingSystemCommon() throws BizLogicAppException {
    return softDeleteExistenceCheck(columnList, getTableName());
  }

  public boolean hasSoftDeleteFieldInSystemCommon() throws BizLogicAppException {
    List<DbOrClassColumnInfo> dbCommonCi = info.dbCommonRootInfo.tableList.get(0).columnList;
    return softDeleteExistenceCheck(dbCommonCi, getTableName());
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
      if (ci.getColumnName().equals(removedDataInfo.getColumnName())) {
        if (ci.getDataType().equals(removedDataInfo.getDataTypeName())) {
          hasRemovedDataColumn = true;

        } else {
          // カラム名が一緒なのにDataTypeが異なる場合はエラー扱いとする
          throw new BizLogicAppException("MSG_ERR_DT_OF_COL_FOR_REMOVED_DATA_COL_DIFFER",
              info.systemName, tableName, ci.getColumnName(), ci.getDataType(),
              removedDataInfo.getDataTypeName());
        }
      }
    }

    return hasRemovedDataColumn;
  }

  public void setHasUniqueConstraint(boolean hasUniqueConstraint) {
    this.hasUniqueConstraint = hasUniqueConstraint;
  }

  public boolean hasUniqueConstraint() {
    return hasUniqueConstraint;
  }

  public String getTableAnnotationString(DbOrClassTableInfo tableInfo) throws BizLogicAppException {
    ParamListGen paramGenList = new ParamListGen();
    // name
    paramGenList.add(new ParamGenWithSingleValue("name", tableInfo.getTableName(), true));

    // uniqueConstraints
    if (tableInfo.hasUniqueConstraint) {
      // uniqueKeyのcolumnNameのリストを作成
      List<String> uniqueKeyColumns = new ArrayList<>();
      for (DbOrClassColumnInfo col : tableInfo.columnList) {
        if (col.isUniqueConstraint()) {
          uniqueKeyColumns.add(col.getColumnName());
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
        throw new RuntimeException(
            new BizLogicAppException("MSG_ERR_INDEX_NUMBER_NOT_CONTINUOUS_FROM_1", "", tableName,
                Integer.toString(indexSerial)));
      }

      index.add(indexMap.get(i).getColumnName());
    }

    return index.toArray(new String[index.size()]);
  }

  public boolean hasRelation() {
    return columnList.stream().filter(col -> col.isRelationColumn()).toList().size() > 0;
  }

  public boolean hasBidirectionalRelation() {
    return columnList.stream().filter(col -> col.isRelationColumn() && col.isRelationBidirectinal())
        .toList().size() > 0;
  }

  public boolean hasBidirectionalRelationRef() {
    return columnList.stream().filter(col -> col.hasBidirectionalInfo()).toList().size() > 0;
  }
  
  public boolean hasAnyRelationsOrRefs() {
    return hasRelation() || hasBidirectionalRelationRef();
  }

  public void dataConsistencyCheck() throws AppException {
    for (DbOrClassColumnInfo info : columnList) {
      info.afterReading();
    }
  }
}
