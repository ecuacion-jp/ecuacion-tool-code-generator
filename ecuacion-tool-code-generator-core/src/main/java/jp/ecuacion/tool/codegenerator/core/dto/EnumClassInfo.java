package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.util.generator.StringGenUtil;
import jp.ecuacion.util.poi.excel.table.bean.StringExcelTableBean;

public class EnumClassInfo extends StringExcelTableBean {

  @Valid
  public List<EnumValueInfo> enumList = new ArrayList<EnumValueInfo>();

  @NotEmpty
  @Size(min = 1, max = 50)
  @Pattern(regexp = Constants.REG_EX_DT_NAME)
  private String dataTypeName;

  @Size(min = 1, max = 10000)
  private String javadocClass;

  private DataTypeInfo dtInfo;

  //@formatter:off
  @Override
  protected @Nonnull String[] getFieldNameArray() {
    return new String[] {
        "dataTypeName", null, null, null, 
        null, null, null, null, null
    };
  }
  //@formatter:on

  public EnumClassInfo(List<String> colList) {
    super(colList);
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  @NotEmpty
  @Size(min = 1, max = 50)
  public String getEnumName() {
    return StringGenUtil.dataTypeNameToUppperCamel(dataTypeName) + "Enum";
  }

  // javadocClass
  public String getJavadocClass() {
    return javadocClass;
  }

  public DataTypeInfo getDtInfo() {
    return dtInfo;
  }

  public void setDtInfo(DataTypeInfo dtInfo) {
    this.dtInfo = dtInfo;
  }

  @Override
  public void afterReading() throws AppException {

    for (EnumValueInfo valueInfo : enumList) {
      valueInfo.afterReading();
    }
  }
}
