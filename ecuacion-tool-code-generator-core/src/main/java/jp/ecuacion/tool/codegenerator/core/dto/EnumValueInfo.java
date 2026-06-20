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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Holds enum value information including code, variable name, and display names for each
 * language.
 */
@SuppressWarnings("NullAway.Init")
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
  // Holds dispName as a Map to support multiple languages. Key is the language (e.g. "ja").
  private Map<String, String> dispNameMap = new HashMap<String, String>();
  private String dispNameLang1;
  private String dispNameLang2;
  private String dispNameLang3;

  //@formatter:off
  @Override
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {
        null, "code", "varName", "isJavaOnly",
        null, "dispNameDefaultLang", "dispNameLang1", "dispNameLang2", "dispNameLang3"
    };
  }
  //@formatter:on

  /**
   * Constructs an instance from a column list and populates the display name map using
   * language settings from {@code sysCmnRootInfo}.
   */
  @SuppressWarnings("null")
  public EnumValueInfo(List<String> colList, SystemCommonRootInfo sysCmnRootInfo) {
    super(colList);

    // Build the values to store in dispNameMap
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

  /** Returns the display name for the given locale, or {@code null} if absent. */
  public @org.jspecify.annotations.Nullable String getDisplayName(String localeString) {
    return dispNameMap.get(localeString);
  }

  /** Registers the display name for the given locale. */
  public void getDisplayName(String localeString, String displayName) {
    dispNameMap.put(localeString, displayName);
  }

  @Override
  public void afterReading() {}
}
