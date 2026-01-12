package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractColAttrRootInfo extends AbstractRootInfo {

  @Size(min = 1, max = 30)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String columnName;

  /**
   * dataTypeはDB項目定義で持っているので重複提議にはなるのだが、同一のdataTypeを使用していることを確認する為に敢えて項目を設けている。
   */
  @Size(min = 1, max = 30)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String dataTypeName;

  private DataTypeInfo dtInfo;

  public AbstractColAttrRootInfo(DataKindEnum fileKind) {
    super(fileKind);
  }

  public AbstractColAttrRootInfo(DataKindEnum fileKind, String columnName, String dataTypeName) {
    super(fileKind);
    this.columnName = columnName;
    this.dataTypeName = dataTypeName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  public void setDataTypeName(String dataTypeName) {
    this.dataTypeName = dataTypeName;
  }

  public void setDtInfo(DataTypeInfo dataTypeInfo) {
    this.dtInfo = dataTypeInfo;
  }

  public DataTypeInfo getDtInfo() {
    return dtInfo;
  }

  public String getLwFieldName() {
    return StringUtil.getLowerCamelFromSnake(columnName);
  }

  public String getCpFieldName() {
    return StringUtil.getUpperCamelFromSnake(columnName);
  }

  public String getKata() {
    return StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
  }

  /**
   * 指定のtableに指定の項目が含まれるかをチェックする。
   */
  protected boolean checkIfColIncludedInTable(DbOrClassTableInfo tableInfo, String colName,
      String dataTypeName) {
    for (DbOrClassColumnInfo info : tableInfo.columnList) {
      // カラム名と、dataTypeNameが同一であることを確認
      if (info.getName().equals(colName) && info.getDataType().equals(dataTypeName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * dbCommonと個別tableの両方を見て、指定のカラムがテーブルに含まれているかを確認する。 「含まれている」の定義は、「カラム名が同一」かつ「データタイプ名が同一」。
   */
  public boolean hasColInTable(DbOrClassTableInfo tableInfo, DbOrClassTableInfo cmnTableInfo) {
    return (checkIfColIncludedInTable(tableInfo, columnName, dataTypeName)
        || checkIfColIncludedInTable(cmnTableInfo, columnName, dataTypeName));
  }

  @Override
  public boolean isDefined() {
    return !StringUtils.isEmpty(getColumnName());
  }
}
