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
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.jspecify.annotations.Nullable;

/** Holds enum class information read from Excel, including the list of enum values. */
@SuppressWarnings("NullAway.Init")
public class EnumClassInfo extends StringExcelTableBean {

  @Valid
  public List<EnumValueInfo> enumList = new ArrayList<EnumValueInfo>();

  private ColumnGenUtil code = new ColumnGenUtil();

  @NotEmpty
  @Size(min = 1, max = 50)
  @Pattern(regexp = Constants.REG_EX_DT_NAME)
  private String dataTypeName;

  private DataTypeInfo dtInfo;

  //@formatter:off
  @Override
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {
        "dataTypeName", null, null, null, 
        null, null, null, null, null
    };
  }
  //@formatter:on

  /** Constructs an instance from a column list read from an Excel table row. */
  @SuppressWarnings("null")
  public EnumClassInfo(List<String> colList) {
    super(colList);
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  @NotEmpty
  @Size(min = 1, max = 50)
  public String getEnumName() {
    return code.dataTypeNameToCapitalCamel(dataTypeName) + "Enum";
  }

  public DataTypeInfo getDtInfo() {
    return dtInfo;
  }

  public void setDtInfo(DataTypeInfo dtInfo) {
    this.dtInfo = dtInfo;
  }

  @Override
  public void afterReading() {

    for (EnumValueInfo valueInfo : enumList) {
      valueInfo.afterReading();
    }
  }
}
