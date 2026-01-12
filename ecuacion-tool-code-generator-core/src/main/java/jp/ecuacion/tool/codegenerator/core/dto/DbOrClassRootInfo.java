package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.item.EclibItem;
import jp.ecuacion.lib.core.item.EclibItemContainer;
import jp.ecuacion.lib.core.util.ValidationUtil;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;


public class DbOrClassRootInfo extends AbstractRootInfo implements EclibItemContainer {
  @Override
  public EclibItem[] getItems() {
    return new EclibItem[] {};
  }
  
  @Valid
  public List<DbOrClassTableInfo> tableList = new ArrayList<DbOrClassTableInfo>();

  public DbOrClassRootInfo(DataKindEnum fileKind) {
    super(fileKind);
  }

  @Override
  public boolean isDefined() {
    return tableList.size() > 0;
  }

  public void consistencyCheckAndCoplementData() throws AppException {
    ValidationUtil.validateThenThrow(this);

    for (DbOrClassTableInfo tbl : tableList) {
      tbl.dataConsistencyCheck();
    }
    
    // 子のentityの場合のみのチェック
    childEntityCheck();

    // systemCommonの場合のみのチェック
    systemCommonCheck();
  }

  private void childEntityCheck() throws BizLogicAppException {

    for (DbOrClassTableInfo ti : tableList) {

      // 項目単位のチェック
      for (DbOrClassColumnInfo ci : ti.columnList) {

        // relationを持つ場合（SystemCommonEntityではrelationを持たないのでこちらにチェックを実装）
        if (ci.getRelationKind() != null) {

          // nullableな場合はエラー（redmine#463参照）
          if (ci.isNullable()) {
            throw new BizLogicAppException(
                "MSG_ERR_CONSISTENCY_CHECK_NULLABLE_ENTITY_COLUMN_CANNOT_HAVE_RELATIONS",
                ti.getName(), ci.getName());
          }
        }
      }
    }
  }

  private void systemCommonCheck() throws AppException {
    
    if (fileKind.equals(DataKindEnum.DB_COMMON)) {

      // そもそもtableはあって一つ
      if (tableList.size() > 1) {
        throw new BizLogicAppException(
            "MSG_ERR_CONSISTENCY_CHECK_SYSTEM_COMMON_ENTITY_MUST_BE_0_OR_1");
      }

      if (tableList.size() == 0) {
        return;
      }

      // 以下は親entityが存在する場合
      DbOrClassTableInfo ti = tableList.get(0);

      // 名称はSystemCommon
      if (!ti.getName().equals("SYSTEM_COMMON")) {
        throw new BizLogicAppException(
            "MSG_ERR_CONSISTENCY_CHECK_NAME_OF_SYSTEM_COMMON_ENTITY_CANNOT_BE_CHANGED");
      }

      // SystemCommonEntityにはrelationを保持しないルール。（redmine#465）
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.getRelationKind() != null) {
          throw new BizLogicAppException(
              "MSG_ERR_CONSISTENCY_CHECK_SYSTEM_COMMON_ENTITY_CANNOT_HAVE_RELATIONS");
        }
      }
    }
  }
}
