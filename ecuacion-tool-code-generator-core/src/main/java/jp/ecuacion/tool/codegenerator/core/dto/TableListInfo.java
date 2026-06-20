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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/** Holds table display name information for each language, as read from the table-list sheet. */
@SuppressWarnings("NullAway.Init")
public class TableListInfo extends StringExcelTableBean {

  private String tableName;
  private String dispNameDefaultLang;
  private String dispNameLang1;
  private String dispNameLang2;
  private String dispNameLang3;
  private Map<String, String> dispNameMap = new HashMap<>();

  @Override
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {"tableName", "dispNameDefaultLang", "dispNameLang1", "dispNameLang2",
        "dispNameLang3"};
  }

  /** Constructs an instance from a column list and builds the display name map per language. */
  @SuppressWarnings("null")
  public TableListInfo(List<String> colList, SystemCommonRootInfo sysCmnRootInfo) {
    super(colList);

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
