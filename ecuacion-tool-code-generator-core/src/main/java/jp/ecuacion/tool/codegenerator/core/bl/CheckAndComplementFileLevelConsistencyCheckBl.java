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
package jp.ecuacion.tool.codegenerator.core.bl;

import java.util.Map;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** Checks that all required Excel definition files are present for the given system. */
public class CheckAndComplementFileLevelConsistencyCheckBl {

  /**
   * Validates that required definition files (data type, system common, etc.) exist in the
   * given map.
   */
  public void check(String systemName, Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {
    checkIfNeededXmlExist(systemName, rootInfoMap);
  }

  private void checkIfNeededXmlExist(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {

    if (!rootInfoMap.containsKey(DataKindEnum.DATA_TYPE)) {
      new Violations().add(new BusinessViolation("MSG_ERR_DT_FILE_EXIST", systemName)).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.DATA_TYPE)
        && rootInfoMap.containsKey(DataKindEnum.ENUM)) {
      new Violations().add(
          new BusinessViolation("MSG_ERR_NO_DT_FILE_THOUGH_ENUM_EXISTS", systemName)).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.DB)
        && rootInfoMap.containsKey(DataKindEnum.DB_COMMON)) {
      new Violations().add(new BusinessViolation(
          "MSG_ERR_DB_NOT_EXIST_ALTHOUGH_DB_COMMON_EXISTS", systemName)).throwIfAny();

    } else if (!rootInfoMap.containsKey(DataKindEnum.SYSTEM_COMMON)) {
      new Violations().add(
          new BusinessViolation("MSG_ERR_SYSTEM_COMMON_INFO_NOT_EXIST", systemName)).throwIfAny();
    }
  }
}
