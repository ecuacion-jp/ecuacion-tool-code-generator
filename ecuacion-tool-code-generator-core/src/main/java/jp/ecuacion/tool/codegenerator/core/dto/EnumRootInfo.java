package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.ValidationUtil;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class EnumRootInfo extends AbstractRootInfo {

  private String dataTypeNamePrefix;
  private List<String> dispNameLangArr = new ArrayList<String>();

  @Valid
  public List<EnumClassInfo> enumClassList = new ArrayList<EnumClassInfo>();

  public EnumRootInfo() {
    super(DataKindEnum.ENUM);
  }

  // dataTypeNamePrefix
  public String getDataTypeNamePrefix() {
    return dataTypeNamePrefix;
  }

  public void setDataTypeNamePrefix(String dataTypeNamePrefix) {
    this.dataTypeNamePrefix = dataTypeNamePrefix;
  }

  // dispNameLang
  public List<String> getDisplayNameLangArr() {
    return dispNameLangArr;
  }

  public void setUserFriendlyNameLangArr(List<String> dispNameLangArr) {
    this.dispNameLangArr = dispNameLangArr;
  }

  public void addDispNameLang(String lang) {
    dispNameLangArr.add(lang);
  }
  
  @Override
  public boolean isDefined() {
    return enumClassList.size() > 0;
  }

  @Override
  public void consistencyCheckAndCoplementData() throws AppException {
    ValidationUtil.validateThenThrow(this);
    
    for (EnumClassInfo info : enumClassList) {
      info.afterReading();
    }
  }
}
