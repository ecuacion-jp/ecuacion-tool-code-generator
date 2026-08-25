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

import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.NOT_EMPTY;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.validation.constraints.NotEmptyWhen;
import jp.ecuacion.lib.validation.constraints.PatternWithDescription;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.validation.CrossSheetConsistencyCheckGroup;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/** Holds table display name information for each language, as read from the table-list sheet. */
@NotEmptyWhen(propertyPath = "dispNameLang1", conditionPropertyPath = "sysCmnRootInfo.supportLang1",
    conditionValue = NOT_EMPTY, conditionOperator = EQUAL_TO, emptyWhenConditionNotSatisfied = true,
    groups = CrossSheetConsistencyCheckGroup.class)
@NotEmptyWhen(propertyPath = "dispNameLang2", conditionPropertyPath = "sysCmnRootInfo.supportLang2",
    conditionValue = NOT_EMPTY, conditionOperator = EQUAL_TO, emptyWhenConditionNotSatisfied = true,
    groups = CrossSheetConsistencyCheckGroup.class)
@NotEmptyWhen(propertyPath = "dispNameLang3", conditionPropertyPath = "sysCmnRootInfo.supportLang3",
    conditionValue = NOT_EMPTY, conditionOperator = EQUAL_TO, emptyWhenConditionNotSatisfied = true,
    groups = CrossSheetConsistencyCheckGroup.class)
@SuppressWarnings("NullAway.Init")
public class TableListInfo extends StringExcelTableBean implements LangsHolder {

  @NotEmpty
  @Size(min = 1, max = 50)
  @PatternWithDescription(regexp = Constants.REG_EX_UP_NUM_US, description = "upperSnakeCase")
  private String tableName;
  @NotEmpty
  @Size(min = 1, max = 50)
  private String dispNameDefaultLang;
  @Size(min = 1, max = 50)
  private String dispNameLang1;
  @Size(min = 1, max = 50)
  private String dispNameLang2;
  @Size(min = 1, max = 50)
  private String dispNameLang3;
  private Map<String, String> dispNameMap = new HashMap<>();

  /** Held for {@code @NotEmptyWhen}'s conditionPropertyPath; 
   * not re-validated (not {@code @Valid}). */
  @SuppressWarnings("unused")
  private SystemCommonRootInfo sysCmnRootInfo;

  @Override
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {"tableName", "dispNameDefaultLang", "dispNameLang1", "dispNameLang2",
        "dispNameLang3"};
  }

  /** Constructs an instance by parsing the given raw column value list. */
  @SuppressWarnings("null")
  public TableListInfo(List<String> colList) {
    super(colList);
  }

  /**
   * Sets the system-common root info, needed both as the condition source for the
   * {@code @NotEmptyWhen} constraints above (validated under {@link
   * CrossSheetConsistencyCheckGroup}) and to build the display-name map.
   *
   * <p>Called from {@code CheckAndComplementDataBlf} once all sheets have been read; this info
   * is intentionally unavailable while this sheet's own data is being parsed.</p>
   */
  public void setSysCmnRootInfo(SystemCommonRootInfo sysCmnRootInfo) {
    this.sysCmnRootInfo = sysCmnRootInfo;
  }

  /**
   * Builds the display-name map using the language settings from {@code sysCmnRootInfo}.
   *
   * <p>Must be called after {@link #setSysCmnRootInfo} and after the
   * {@link CrossSheetConsistencyCheckGroup} validation has passed.</p>
   */
  public void buildDisplayNameMap() {
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

  public String getTableName() {
    return tableName;
  }

  public Map<String, String> getDisplayNameMap() {
    return dispNameMap;
  }

  @Override
  public void afterReading() {}
}
