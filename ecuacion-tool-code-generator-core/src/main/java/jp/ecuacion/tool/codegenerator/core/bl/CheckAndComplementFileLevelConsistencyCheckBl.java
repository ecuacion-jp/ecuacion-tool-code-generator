package jp.ecuacion.tool.codegenerator.core.bl;

import java.util.Map;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class CheckAndComplementFileLevelConsistencyCheckBl {

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
