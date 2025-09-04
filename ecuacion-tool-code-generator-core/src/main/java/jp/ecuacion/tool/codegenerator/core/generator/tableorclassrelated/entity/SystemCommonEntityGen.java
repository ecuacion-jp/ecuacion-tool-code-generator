package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity;

import java.io.IOException;
import java.util.List;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class SystemCommonEntityGen extends EntityGen {

  // protected EntityGenKindEnum genKind = EntityGenKindEnum.ENTITY_SYSTEM_COMMON;

  public SystemCommonEntityGen() throws AppException {
    super(DataKindEnum.DB_COMMON);
  }

  protected EntityGenKindEnum getEntityGenKindEnum() {
    return EntityGenKindEnum.ENTITY_SYSTEM_COMMON;
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {

    List<DbOrClassTableInfo> tableList = getTableList();

    if (tableList != null) {
      DbOrClassTableInfo tableInfo = tableList.get(0);

      sb = new StringBuilder();
      createSource(tableInfo);

    } else {
      sb = new StringBuilder();

      // ヘッダ情報定義
      appendPackage(sb);
      ImportGenUtil importMgr = new ImportGenUtil();
      importMgr.add(EclibCoreConstants.PKG + ".jpa.entity.EclibEntity");
      importMgr.add("jakarta.persistence.*", "java.io.Serializable");
      importMgr.add(rootBasePackage + ".base.record.SystemCommonBaseRecord");
      sb.append(importMgr.outputStr() + RT);
      // クラス定義
      sb.append("@MappedSuperclass" + RT);
      sb.append("public abstract class SystemCommonEntity "
          + "extends EclibEntity implements Serializable {" + RT2);
      sb.append(T1 + "private static final long serialVersionUID = 1L;" + RT2);
      sb.append(T1 + "public SystemCommonEntity() {}" + RT);
      sb.append(T1 + "public SystemCommonEntity(SystemCommonBaseRecord rec) {super();}" + RT2);
      sb.append(T1 + "@PrePersist" + RT);
      sb.append(T1 + "public void preInsert() {}" + RT2);
      sb.append(T1 + "@PreUpdate" + RT);
      sb.append(T1 + "public void preUpdate() {}" + RT);
      sb.append("}" + RT);
    }

    outputFile(sb, getFilePath("entity"), "SystemCommonEntity.java");

    // // metamodel生成
    // if (tableList != null) {
    // DbOrClassTableInfo tableInfo = tableList.get(0);
    // sb = new StringBuilder();
    // createMetaModelSource(tableInfo);
    // outputFile(sb, getFilePath("entity"),
    // strUtil.getUpperCamelFromSnake(tableInfo.getTableName()) + "_.java");
    // }

    appendItemNamesProperties(EntityGenKindEnum.ENTITY_SYSTEM_COMMON);
  }

  public void createSource(DbOrClassTableInfo tableInfo) throws AppException {

    final String entityNameCp =
        StringUtil.getUpperCamelFromSnake(tableInfo.getTableName());

    // ヘッダ情報定義
    appendPackage(sb);
    appendImport(sb, tableInfo);

    // class定義
    // grouping定義が存在する場合は、systemCommonには必ずfilter定義が記載される。
    if (info.groupRootInfo.isDefined()) {
      getGroupFilterDefAnnotationString(sb);
    }
    if (tableInfo.hasGroupColumn()) {
      getGroupFilterAnnotationString(sb);
    }

    // soft deleteを使用する場合
    getSoftDeleteAnnotationsString(sb, tableInfo);

    sb.append("@MappedSuperclass" + RT);
    sb.append("@EntityListeners(AuditingEntityListener.class)" + RT);
    sb.append(
        "public abstract class SystemCommonEntity extends EclibEntity implements Serializable {"
            + RT2);

    appendSerialVersionUid(sb);

    // 各種field定義
    appendField(sb, tableInfo, tableInfo.columnList);
    appendFieldName(sb, entityNameCp, tableInfo);

    // 各種コンストラクタ定義
    appendDefaultConstructor(sb, entityNameCp);
    appendRecConstructor(sb, tableInfo, entityNameCp);

    // accessor
    appendAccessor(sb, tableInfo);

    // prePersist
    appendAutoInsertOrUpdateGen(sb, tableInfo, false, true);
    // preUpdate
    appendAutoInsertOrUpdateGen(sb, tableInfo, true, true);

    // hasSoftDeleteField
    appendHasSoftDeleteFieldGen(sb, tableInfo, true);

    sb.append("}");
  }

  // public void createMetaModelSource(DbOrClassTableInfo tableInfo) throws AppException {
  // // ヘッダ情報定義
  // appendPackage(sb);
  // appendMetaModelImport(sb, tableInfo);
  // // class定義
  // appendMetaModelClass(sb, tableInfo, EntityGenKindEnum.ENTITY_SYSTEM_COMMON);
  // // field定義
  // appendMetaModelField(sb, tableInfo, EntityGenKindEnum.ENTITY_SYSTEM_COMMON);
  //
  // sb.append("}" + RT);
  // }
}
