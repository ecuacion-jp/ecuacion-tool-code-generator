package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.util.poi.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;

public class EnumValueInfo extends StringExcelTableBean {

  @StrBoolean
  private String isJavaOnly;
  @NotEmpty
  @Size(min = 1, max = 10)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String code;
  @NotEmpty
  @Size(min = 1, max = 50)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String varName;
  private String dispNameDefaultLang;
  // 多言語に対応するため、dispNameをMapで持つ。キーは言語（jaなど）。
  private Map<String, String> dispNameMap = new HashMap<String, String>();
  private String dispNameLang1;
  private String dispNameLang2;
  private String dispNameLang3;

  //@formatter:off
  @Override
  protected @Nonnull String[] getFieldNameArray() {
    return new String[] {
        null, "isJavaOnly", "code", "varName", 
        "dispNameDefaultLang", null, "dispNameLang1", "dispNameLang2", "dispNameLang3"
    };
  }
  //@formatter:on

  public EnumValueInfo(List<String> colList, SystemCommonRootInfo sysCmnRootInfo) {
    super(colList);

    // dispNameMapの値に代入するものを作成
    Map<String, String> map = new HashMap<>();
    map.put(sysCmnRootInfo.getDefaultLang(), dispNameDefaultLang);
    if (!StringUtils.isEmpty(sysCmnRootInfo.getSupportLang1())) {
      map.put(sysCmnRootInfo.getSupportLang1(), dispNameLang1);
    }
    if (!StringUtils.isEmpty(sysCmnRootInfo.getSupportLang2())) {
      map.put(sysCmnRootInfo.getSupportLang2(), dispNameLang2);
    }
    if (!StringUtils.isEmpty(sysCmnRootInfo.getSupportLang3())) {
      map.put(sysCmnRootInfo.getSupportLang3(), dispNameLang3);
    }

    dispNameMap = map;
  }

  public String getIsJavaOnly() {
    return isJavaOnly;
  }

  public String getCode() {
    return code;
  }

  public String getVarName() {
    return varName;
  }

  public Map<String, String> getDisplayNameMap() {
    return dispNameMap;
  }

  public String getDisplayName(String localeString) {
    return dispNameMap.get(localeString);
  }

  public void getDisplayName(String localeString, String displayName) {
    dispNameMap.put(localeString, displayName);
  }

  @Override
  public void afterReading() {}
}
