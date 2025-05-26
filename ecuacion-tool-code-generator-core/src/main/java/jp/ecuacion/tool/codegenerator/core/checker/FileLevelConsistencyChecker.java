package jp.ecuacion.tool.codegenerator.core.checker;

import java.util.HashMap;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class FileLevelConsistencyChecker {

  public void check(String systemName,
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap) throws AppException {
    checkIfNeededXmlExist(systemName, systemMap.get(systemName));
  }

  private void checkIfNeededXmlExist(String systemName,
      HashMap<DataKindEnum, AbstractRootInfo> xmlMap) throws AppException {
    if (!xmlMap.containsKey(DataKindEnum.DATA_TYPE)) {
      throw new BizLogicAppException("MSG_ERR_DT_FILE_EXIST", systemName);

    } else if (!xmlMap.containsKey(DataKindEnum.DATA_TYPE)
        && xmlMap.containsKey(DataKindEnum.ENUM)) {
      // enumがあるのにdataTypeがない、はあり得ない
      throw new BizLogicAppException("MSG_ERR_NO_DT_FILE_THOUGH_ENUM_EXISTS", systemName);

    } else if (!xmlMap.containsKey(DataKindEnum.DB)
        && xmlMap.containsKey(DataKindEnum.DB_COMMON)) {
      // dbCommonがあるのにdbがないことはあり得ない
      throw new BizLogicAppException("MSG_ERR_DB_NOT_EXIST_ALTHOUGH_DB_COMMON_EXISTS", systemName);

    } else if (!xmlMap.containsKey(DataKindEnum.SYSTEM_COMMON)) {
      // systemCommonは必ず存在しなければならない
      throw new BizLogicAppException("MSG_ERR_SYSTEM_COMMON_INFO_NOT_EXIST", systemName);
    }


  }

}
