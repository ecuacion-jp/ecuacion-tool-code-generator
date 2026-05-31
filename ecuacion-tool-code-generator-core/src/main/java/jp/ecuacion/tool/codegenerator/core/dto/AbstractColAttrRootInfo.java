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

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract root info for definition files that reference a single DB column by name and
 * data type.
 */
@SuppressWarnings("NullAway.Init")
public abstract class AbstractColAttrRootInfo extends AbstractRootInfo {

  @Size(min = 1, max = 30)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String columnName;

  /**
   * Although dataType is already held in the DB item definition and would be a duplicate, this
   * field is intentionally included here to verify that the same dataType is being used.
   */
  @Size(min = 1, max = 30)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String dataTypeName;

  private DataTypeInfo dtInfo;

  /**
   * Constructs an instance with the given file kind, leaving column name and data type name
   * unset.
   */
  @SuppressWarnings("null")
  public AbstractColAttrRootInfo(DataKindEnum fileKind) {
    super(fileKind);
  }

  /** Constructs an instance with the given file kind, column name, and data type name. */
  @SuppressWarnings("null")
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
   * Checks whether the specified column is included in the specified table.
   */
  protected boolean checkIfColIncludedInTable(DbOrClassTableInfo tableInfo, String colName,
      String dataTypeName) {
    for (DbOrClassColumnInfo info : tableInfo.columnList) {
      // Verify that both the column name and dataTypeName match
      if (info.getName().equals(colName) && info.getDataType().equals(dataTypeName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks both dbCommon and the individual table to determine whether the specified column is
   * included. "Included" means both the column name and the data type name match.
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
