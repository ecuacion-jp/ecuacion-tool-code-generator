package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class MiscSoftDeleteRootInfo extends AbstractColAttrRootInfo {

  @Pattern(regexp = Constants.REG_EX_AL_NUM_DOT)
  private String initialValue;
  @Size(min = 1, max = 30)
  @Pattern(regexp = Constants.REG_EX_AL_NUM_US_CM_DOT)
  private String removeMethodName;
  @Pattern(regexp = Constants.REG_EX_AL_NUM_DOT)
  private String updatedValue;
  @Pattern(regexp = Constants.REG_EX_AL_NUM_US_CM_DOT)
  private String additionalMethodArgs;

  public MiscSoftDeleteRootInfo() {
    super(DataKindEnum.MISC_REMOVED_DATA);
  }

  public MiscSoftDeleteRootInfo(String columnName, String dataTypeName, String initialValue,
      String removeMethodName, String updatedValue, String additionalMethodArgs) {

    super(DataKindEnum.MISC_REMOVED_DATA, columnName, dataTypeName);

    this.initialValue = initialValue;
    this.removeMethodName = removeMethodName;
    this.updatedValue = updatedValue;
    this.additionalMethodArgs = additionalMethodArgs;
  }

  public String getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(String initialValue) {
    this.initialValue = initialValue;
  }

  public String getRemoveMethodName() {
    return removeMethodName;
  }

  public void setRemoveMethodName(String removeMethodName) {
    this.removeMethodName = removeMethodName;
  }

  public String getUpdatedValue() {
    return updatedValue;
  }

  public void setUpdatedValue(String updatedValue) {
    this.updatedValue = updatedValue;
  }

  public String getAdditionalMethodArgs() {
    return additionalMethodArgs;
  }

  public void setAdditionalMethodArgs(String additionalMethodArgs) {
    this.additionalMethodArgs = additionalMethodArgs;
  }

  @Override
  public void consistencyCheckAndCoplementData() throws BizLogicAppException {}
}
