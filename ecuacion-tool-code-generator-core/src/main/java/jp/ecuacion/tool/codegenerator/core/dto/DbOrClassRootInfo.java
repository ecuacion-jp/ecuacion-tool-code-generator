package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;


public class DbOrClassRootInfo extends AbstractRootInfo implements ItemContainer {
  @Override
  public Item[] customizedItems() {
    return new Item[] {};
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

  public void consistencyCheckAndCoplementData() {
    new Violations()
        .addAll(Validation.buildDefaultValidatorFactory().getValidator().validate(this))
        .throwIfAny();

    for (DbOrClassTableInfo tbl : tableList) {
      tbl.dataConsistencyCheck();
    }
    
    // 子のentityの場合のみのチェック
    // childEntityCheck();

    // systemCommonの場合のみのチェック
    systemCommonCheck();
  }

  // 将来のチェック追加用に残しているが現時点では内容なし（redmine#463参照）
  // private void childEntityCheck() {
  //   for (DbOrClassTableInfo ti : tableList) {
  //     for (DbOrClassColumnInfo ci : ti.columnList) {
  //       if (ci.getRelationKind() != null && ci.isNullable()) {
  //         throw new BizLogicAppException(
  //             "MSG_ERR_CONSISTENCY_CHECK_NULLABLE_ENTITY_COLUMN_CANNOT_HAVE_RELATIONS",
  //             ti.getName(), ci.getName());
  //       }
  //     }
  //   }
  // }

  private void systemCommonCheck() {

    if (fileKind.equals(DataKindEnum.DB_COMMON)) {

      // そもそもtableはあって一つ
      if (tableList.size() > 1) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_CONSISTENCY_CHECK_SYSTEM_COMMON_ENTITY_MUST_BE_0_OR_1")).throwIfAny();
      }

      if (tableList.size() == 0) {
        return;
      }

      // 以下は親entityが存在する場合
      DbOrClassTableInfo ti = tableList.get(0);

      // 名称はSystemCommon
      if (!ti.getName().equals("SYSTEM_COMMON")) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_CONSISTENCY_CHECK_NAME_OF_SYSTEM_COMMON_ENTITY_CANNOT_BE_CHANGED"))
            .throwIfAny();
      }

      // SystemCommonにはrelationを保持しないルール。（redmine#465）
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.getRelationKind() != null) {
          new Violations().add(new BusinessViolation(
              "MSG_ERR_CONSISTENCY_CHECK_SYSTEM_COMMON_ENTITY_CANNOT_HAVE_RELATIONS")).throwIfAny();
        }
      }
    }
  }
}
