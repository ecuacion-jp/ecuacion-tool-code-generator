package jp.ecuacion.tool.codegenerator.core.preparer;

import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.controller.CodeGeneratorAction;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public class PreparerForMiscOptimisticLock {
  
  private Info info;
  
  public PreparerForMiscOptimisticLock() {
    this.info = CodeGeneratorAction.tlInfo.get();
  }

  public void prepare() throws BizLogicAppException {
    MiscOptimisticLockRootInfo lockInfo = (MiscOptimisticLockRootInfo) info.systemMap
        .get(info.systemName).get(DataKindEnum.MISC_OPTIMISTIC_LOCK);
    if (lockInfo.isDefined()) {
      // 楽観的排他制御のカラムの情報を、個別のcolumnに設定する
      // （本項目はデータ保持項目が少なく単純なのでこの持ち方に出来たが、削除フラグ等は複雑になるためEntity生成時にまとめて処理している）
      setOptLock(lockInfo, DataKindEnum.DB);
      setOptLock(lockInfo, DataKindEnum.DB_COMMON);
      // setOptLock(systemName, systemMap, lockInfo, Dict.XML_POST_FIX_CLS);

      // 混乱しないため、元データは破棄しておく
      info.systemMap.get(info.systemName).remove(DataKindEnum.MISC_OPTIMISTIC_LOCK);
    }
  }

  private void setOptLock(MiscOptimisticLockRootInfo lockInfo, DataKindEnum dataKind)
      throws BizLogicAppException {
    DbOrClassRootInfo dbRootInfo =
        (DbOrClassRootInfo) info.systemMap.get(info.systemName).get(dataKind);
    if (dbRootInfo != null) {
      for (DbOrClassTableInfo ti : dbRootInfo.tableList) {
        for (DbOrClassColumnInfo ci : ti.columnList) {
          if (ci.getColumnName() == null) {
            System.out.println("here!");
          }
          if (ci.getColumnName().equals(lockInfo.getColumnName())) {
            if (ci.getDataType().equals(lockInfo.getDataTypeName())) {
              ci.setOptLock(true);
            } else {
              // カラム名が一緒なのにDataTypeが異なる場合はエラー扱いとする
              throw new BizLogicAppException("MSG_ERR_DT_OF_COL_FOR_OPT_LOCK_DIFFER",
                  info.systemName, ti.getTableName(), ci.getColumnName(), ci.getDataType(),
                  lockInfo.getDataTypeName());
            }
          }
        }
      }
    }
  }
}
