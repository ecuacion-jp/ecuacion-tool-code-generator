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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Holds admin scope settings such as the scope column and tables excluded from the scope.
 */
@SuppressWarnings("NullAway.Init")
public class MiscAdminScopeRootInfo extends AbstractColAttrRootInfo {

  private List<String> tableNamesWithoutAdminScope = new ArrayList<>();

  /** Constructs an empty instance when the settings are not used. */
  @SuppressWarnings("null")
  public MiscAdminScopeRootInfo() {
    super(DataKindEnum.MISC_ADMIN_SCOPE);
  }

  /**
   * Constructs an instance with the settings.
   *
   * @param columnName the scope column name
   * @param dataTypeName the data type name of the scope column
   * @param tableNamesWithoutAdminScope tables not narrowed by the scope, in CSV
   */
  @SuppressWarnings("null")
  public MiscAdminScopeRootInfo(String columnName, String dataTypeName,
      String tableNamesWithoutAdminScope) {

    super(DataKindEnum.MISC_ADMIN_SCOPE, columnName, dataTypeName);

    if (tableNamesWithoutAdminScope != null && !tableNamesWithoutAdminScope.equals("")) {
      this.tableNamesWithoutAdminScope = Arrays.asList(tableNamesWithoutAdminScope.split(","));
    }
  }

  public List<String> getTableNamesWithoutAdminScope() {
    return tableNamesWithoutAdminScope;
  }

  @Override
  public void consistencyCheckAndComplementData() {

  }
}
