package jp.ecuacion.tool.codegenerator.core.bl;

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public class PrepareManager {

  public void prepare() throws AppException {

    Info info = MainController.tlInfo.get();

    // DBInfo、DbCommonInfoの中で、bidirectionalなrelationがあるものについて、参照先側での追加実装のために参照先側の情報として登録しておく
    List<DbOrClassColumnInfo.RelationRefInfo> relRefInfoList = new ArrayList<>();
    // ループのためDBとDBCommonをまとめたlistを作成
    List<DbOrClassTableInfo> list = new ArrayList<>(info.dbRootInfo.tableList);
    list.addAll(info.dbCommonRootInfo.tableList);
    for (DbOrClassTableInfo ti : list) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.isRelation()) {
          relRefInfoList.add(new DbOrClassColumnInfo.RelationRefInfo(ci.isRelationBidirectinal(),
              ci.getRelationKind().getInverse(), ci.getRelationRefTable(), ci.getRelationRefCol(),
              ci.getRelationRefFieldName(), ti.getName(),
              StringUtil.getLowerCamelFromSnake(ci.getName()), ci.getRelationFieldName()));
        }
      }
    }

    // bidirectionalの場合は取得した結果を参照先側に埋める
    for (DbOrClassColumnInfo.RelationRefInfo bdInfo : relRefInfoList) {
      boolean found = false;

      // 参照先にcommonを使うことは流石にないと思われるのでdbInfoでloop
      for (DbOrClassTableInfo ti : info.dbRootInfo.tableList) {
        for (DbOrClassColumnInfo ci : ti.columnList) {
          if (ti.getName().equals(bdInfo.getDstTableName())
              && ci.getName().equals(bdInfo.getDstColumnName())) {
            ci.getRelationRefInfoList().add(bdInfo);
            found = true;
          }
        }
      }

      if (!found) {
        throw new RuntimeException("not found : tableName = " + bdInfo.getDstTableName()
            + ", columnName = " + bdInfo.getDstColumnName());
      }
    }

    // DbとDataType:
    new PreparerForDbAndDataType().prepare();

    // miscRemovevdData:DbOrClassInfoに情報追加
    new PreparerForMiscRemovedData().prepare();

    // miscGroupInfo:DbOrClassInfoに情報追加
    new PreparerForMiscGroup().prepare();

    // miscOptimisticLockInfo:DbOrClassInfoにまとめる
    new PreparerForMiscOptimisticLock().prepare();
  }
}
