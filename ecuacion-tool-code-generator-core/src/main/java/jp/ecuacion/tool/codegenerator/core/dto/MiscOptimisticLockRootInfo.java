package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * 本クラスは、対応するxmlファイルが配置されていない場合でも、オブジェクトは生成されsystemMapに設定される。
 * ユーザが本雪堤を使用するか否かは、isThisSettingValid()にて取得する。
 *
 * @author yosuke.tanaka
 */
public class MiscOptimisticLockRootInfo extends AbstractColAttrRootInfo {

  public MiscOptimisticLockRootInfo() {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK);
  }

  public MiscOptimisticLockRootInfo(String columnName, String dataTypeName) {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK, columnName, dataTypeName);
  }

  @Override
  public void consistencyCheckAndCoplementData() throws BizLogicAppException {
  }
}
