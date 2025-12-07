package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity;

import java.io.IOException;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class EntityBodyGen extends EntityGen {

  public EntityBodyGen(DataKindEnum xmlFilePostFix, boolean isPkPart) {
    super(xmlFilePostFix);
  }

  @Override
  protected EntityGenKindEnum getEntityGenKindEnum() {
    return EntityGenKindEnum.ENTITY_BODY;
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {

    for (DbOrClassTableInfo tableInfo : getTableList()) {
      sb = new StringBuilder();
      createSource(tableInfo, commonColumnList);
      outputFile(sb, getFilePath("entity"),
          StringUtil.getUpperCamelFromSnake(tableInfo.getTableName()) + ".java");
    }

    appendItemNamesProperties(EntityGenKindEnum.ENTITY_BODY);
  }

  public void createSource(DbOrClassTableInfo tableInfo, List<DbOrClassColumnInfo> commonColumnList)
      throws AppException {

    final String entityNameCp =
        StringUtil.getUpperCamelFromSnake(tableInfo.getTableName());

    // ヘッダ情報定義
    appendPackage(sb);
    appendImport(sb, tableInfo);

    // class定義
    sb.append("@Entity" + RT);
    sb.append(tableInfo.getTableAnnotationString(tableInfo) + RT);

    // group定義
    if (info.groupRootInfo.isDefined()
        && !info.groupRootInfo.getTableNamesWithoutGrouping().contains(tableInfo.getTableName())) {
      if (tableInfo.hasCustomGroupColumn()) {
        DbOrClassColumnInfo customGroupCi = tableInfo.getCustomGroupColumn();
        String filterName = "groupFilter"
            + StringUtil.getUpperCamelFromSnake(tableInfo.getTableName());
        getGroupFilterDefAnnotationString(sb, filterName, customGroupCi.getColumnName(),
            customGroupCi.getDtInfo());
        getGroupFilterAnnotationString(sb, filterName);

      } else {
        getGroupFilterAnnotationString(sb);
      }
    }

    // soft deleteを使用する場合
    getSoftDeleteAnnotationsString(sb, tableInfo);

    sb.append("public final class " + entityNameCp
        + " extends SystemCommonEntity implements Serializable {" + RT2);

    appendSerialVersionUid(sb);

    // 各種field定義
    appendField(sb, tableInfo, tableInfo.columnList);
    appendFieldName(sb, entityNameCp, tableInfo);
    appendFieldNameArr(sb, tableInfo, entityNameCp, false);

    // 各種コンストラクタ定義
    appendDefaultConstructor(sb, entityNameCp);
    appendRecConstructor(sb, tableInfo, entityNameCp);
    if (tableInfo.hasUniqueConstraint()) {
      appendNaturalKeyConstructor(sb, tableInfo, entityNameCp);
    }

    // accessor
    appendAccessor(sb, tableInfo);

    // prePersist
    appendAutoInsertOrUpdateGen(sb, tableInfo, false, false);
    // preUpdate
    appendAutoInsertOrUpdateGen(sb, tableInfo, true, false);
    // uniqueConstraint関連
    appendUniqueConstraintGen(sb, tableInfo);

    // hasSoftDeleteField
    appendHasSoftDeleteFieldGen(sb, tableInfo, false);

    sb.append("}" + RT);
  }

  private void appendUniqueConstraintGen(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    sb.append(T1 + "// getNaturalKeyFieldList()" + RT);
    sb.append(T1 + "public List<String> getNaturalKeyFieldList() {" + RT);
    if (tableInfo.hasUniqueConstraint()) {
      sb.append(T2 + "List<String> rtnList = new ArrayList<>();" + RT);

      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        if (ci.isUniqueConstraint()) {
          sb.append(T2 + "rtnList.add(\""
              + StringUtil.getLowerCamelFromSnake(ci.getColumnName()) + "\");"
              + RT);
        }
      }
      sb.append(T2 + "return rtnList;" + RT);

    } else {
      sb.append(T2 + "return null;" + RT);
    }

    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "// getSetOfUniqueConstraintFieldList()" + RT);
    sb.append(T1 + "// 今は実質naturalKeyしかないのでそれをSetに入れて返す。" + RT);
    sb.append(
        T1 + "// 将来的には他のunique keyも設定できるようにする。（でないとinsert時に論理削除済レコードが残っていた場合の自動削除ができない）" + RT);

    sb.append(T1 + "@Nonnull" + RT);
    sb.append(T1 + "public Set<List<String>> getSetOfUniqueConstraintFieldList() {" + RT);
    sb.append(T2 + "Set<List<String>> rtnSet = new HashSet<>();" + RT);
    sb.append(T2 + "List<String> list = getNaturalKeyFieldList();" + RT);
    sb.append(T2 + "if (list != null) {" + RT);
    sb.append(T3 + "rtnSet.add(list);" + RT);
    sb.append(T2 + "}" + RT2);
    sb.append(T2 + "return rtnSet;" + RT);
    sb.append(T1 + "}" + RT2);
  }
}
