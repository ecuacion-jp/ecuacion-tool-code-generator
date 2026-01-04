package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.AbstractTableOrClassRelatedGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public class DaoGen extends AbstractTableOrClassRelatedGen {

  private CodeGenUtil code = new CodeGenUtil();

  public DaoGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);
  }

  @Override
  public void generate() throws AppException {

    // Entity別のbaseDao / baseRepositoryImplを作成
    for (DbOrClassTableInfo tableInfo : getTableList()) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

      // super.makePkList(tableInfo);

      if (info.sysCmnRootInfo.getUsesUtilJpa()) {
        createBaseDaos(tableInfo, entityNameCp);
      }

      // springの場合はbaseRepositoryを生成
      if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
        createBaseRepository(tableInfo, entityNameCp, info.groupRootInfo);
      }
    }

    // SystemCommonDaoを作成
    if (info.sysCmnRootInfo.getUsesUtilJpa()) {
      createSystemCommonBaseDao();
    }

    // springの場合はbaseRepositoryを生成
    if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
      createSystemCommonBaseRepository();
    }
  }

  public void createBaseDaos(DbOrClassTableInfo ti, String entityNameCp) throws AppException {
    sb = new StringBuilder();

    final boolean isNoGroupQuery = info.getGenPtn() == GeneratePtnEnum.NO_GROUP_QUERY
        || info.getGenPtn() == GeneratePtnEnum.DAO_ONLY_GROUP_NO_GROUP_QUERY;

    // 宣言とコンストラクタ
    sb.append("package " + rootBasePackage + ".base." + postfixSm + ";" + RT2);

    createBaseDaoImport(ti, entityNameCp);

    MiscGroupRootInfo groupInfo = info.groupRootInfo;

    sb.append("public abstract class " + entityNameCp + "Base" + postfixCp
        + " extends SystemCommonBase" + postfixCp + "<" + entityNameCp + "> {" + RT2);

    // springか否かで生成するコンストラクタを分岐
    if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
      // 単純な引数なしコンストラクタを生成
      sb.append(T1 + "public " + entityNameCp + "Base" + postfixCp + "() {" + RT);
      sb.append(T2 + "super(new " + entityNameCp + "[0]);" + RT);

    } else {
      // 裏技的にAbstractDaoに具体的なクラスを渡す
      sb.append(T1 + "public " + entityNameCp + "Base" + postfixCp + "("
          + ((groupInfo.isDefined() && !isNoGroupQuery && ti.hasGroupColumnIncludingSystemCommon())
              ? groupInfo.getKata() + " " + groupInfo.getLwFieldName()
              : "")
          + ") {" + RT);
      sb.append(T2 + "super("
          + ((!isNoGroupQuery && groupInfo.isDefined())
              ? ((ti.hasGroupColumnIncludingSystemCommon()) ? groupInfo.getLwFieldName() : "null")
                  + ", "
              : "")
          + "new " + entityNameCp + "[0]);" + RT);
    }

    sb.append(T1 + "}" + RT2);

    // 本entityがgrpMetaのfieldを持つかをboolで返す
    sb.append(T1 + "protected boolean hasGroupFieldInTable() {" + RT);
    sb.append(T2 + "return " + ti.hasGroupColumnIncludingSystemCommon() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    // getSetOfUniqueConstraintFieldList()
    // sb.append(T1 + "@Override" + RT);
    // sb.append(T1 + "protected Set<List<FieldInfo>> getSetOfUniqueConstraintFieldList() {" + RT);
    // sb.append(T2 + "return " + entityNameCp + ".getSetOfUniqueConstraintFieldList();" + RT);
    // sb.append(T1 + "}" + RT2);

    // selectEntityByPk。削除フラグを指定している場合はwhereに入れる
    sb.append(T1 + "/** " + RT);
    sb.append(T1 + " * pkでselect。" + RT);
    sb.append(T1 + " */" + RT);
    // グループ指定ありなしで変化する部分の文字列を作成
    sb.append(T1 + "public " + entityNameCp + " selectEntityByPk"
        + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectEntityByPkForBaseDao(em, \"selectEntityByPk" + "\", \""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue, false);"
        + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** " + RT);
    sb.append(T1 + " * pkでselect for update。" + RT);
    sb.append(T1 + " */" + RT);
    sb.append(T1 + "public " + entityNameCp + " selectEntityByPkForUpdate"
        + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectEntityByPkForBaseDao(em, \"selectEntityByPkForUpdate" + "\", \""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue, true);"
        + RT);
    sb.append(T1 + "}" + RT2);

    if (ti.hasUniqueConstraint()) {
      sb.append(T1 + "/** " + RT);
      sb.append(T1 + " * surrogateKeyを使用している場合に、naturalKeyでselect。" + RT);
      sb.append(T1 + " */" + RT);
      sb.append(T1 + "public " + entityNameCp + " selectEntityBy"
          + code.naturalKeyUncapitalCamelAnd(ti) + "(EntityManager em," + RT);
      sb.append(T3 + code.naturalKeyDefine(ti) + ") {" + RT);
      sb.append(T2 + "QueryCondition[] queryConditions = new QueryCondition[] {" + RT);
      boolean is1st = true;
      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.isUniqueConstraint()) {
          String comma = "";
          if (is1st) {
            is1st = false;

          } else {
            comma = ", ";
          }

          sb.append(T4 + comma + "new QueryCondition(" + "\""
              + StringUtil.getLowerCamelFromSnake(ci.getName()) + "\"" + ", "
              + StringUtil.getLowerCamelFromSnake(ci.getName()) + ")" + RT);
        }
      }
      sb.append(T2 + "};" + RT);
      sb.append(
          T2 + "return selectEntityByConditions(em, \"selectEntityByNaturalKey\", queryConditions);"
              + RT);
      sb.append(T1 + "}" + RT2);
    }

    sb.append(T1 + "/** " + RT);
    sb.append(T1 + " * （グループが定義されていればグループ内で）全件select。" + RT);
    sb.append(T1 + " */" + RT);
    sb.append(T1 + "public List<" + entityNameCp + "> selectEntityList" + "(EntityManager em"
        + ") {" + RT);
    sb.append(T2 + "return selectEntityListForBaseDao(em, \"selectEntityList" + "\", false);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** pkでcount取得。 */" + RT);
    sb.append(
        T1 + "public Long selectCountByPk" + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectCountForBaseDao(em, \"selectCountByPk" + "\", getParamMapFromPk(\""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue));" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** （グループが定義されていればグループ内で）全件カウント。*/" + RT);
    sb.append(T1 + "public Long selectCountAll" + "(EntityManager em" + ") {" + RT);
    sb.append(T2 + "return selectCountForBaseDao(em, \"selectCountAll" + "\", null);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath(postfixSm), entityNameCp + "Base" + postfixCp + ".java");
  }

  private void createBaseDaoImport(DbOrClassTableInfo ti, String entityNameCp) throws AppException {
    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("java.util.*", "jakarta.persistence.EntityManager",
        rootBasePackage + ".base.entity." + entityNameCp);
    if (ti.hasUniqueConstraint()) {
      importMgr.add("jp.ecuacion.util.jpa.dao.query.QueryCondition");
      for (DbOrClassColumnInfo ci : ti.columnList) {
        DataTypeInfo dtInfo = ci.getDtInfo();
        importMgr.add(getHelper(dtInfo.getKata()).getNeededImports(ci));
      }
    }

    // 使用するenumクラスをimport
    for (DbOrClassColumnInfo colInfo : ti.columnList) {
      if (colInfo.isUniqueConstraint() || colInfo.isPk()) {
        String dataType = colInfo.getDataType();
        DataTypeInfo dtInfo = colInfo.getDtInfo();
        if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
          String importClassStr = getRootBasePackageOfDataTypeFromAllSystem(colInfo.getDataType())
              + ".base.enums." + CodeGenUtil.dataTypeNameToCapitalCamel(dataType) + "Enum";
          importMgr.add(importClassStr);
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);
  }

  private void createBaseRepository(DbOrClassTableInfo tableInfo, String tableNameCp,
      MiscGroupRootInfo groupInfo) throws AppException {

    sb = new StringBuilder();

    final List<DbOrClassColumnInfo> relFieldList =
        tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly()).filter(e -> !e.isPk())
            .filter(e -> e.isRelationColumn()).toList();

    List<DbOrClassColumnInfo> list = new ArrayList<>(tableInfo.columnList);
    list.addAll(info.dbCommonRootInfo.tableList.get(0).columnList);
    String idColumnName = null;
    for (DbOrClassColumnInfo ci : list) {
      if (ci.isPk()) {
        idColumnName = ci.getName();
      }
    }

    if (idColumnName == null) {
      throw new RuntimeException("idColumnName is null: " + tableInfo.getName());
    }

    final String idFieldName = StringUtil.getLowerCamelFromSnake(idColumnName);

    // 宣言とコンストラクタ
    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    // import
    createBaseRepositoryImport(tableInfo, tableNameCp, relFieldList);

    sb.append("public interface " + tableNameCp + "BaseRepository"
        + " extends SystemCommonBaseRepository<" + tableNameCp
        + ", Long>, JpaSpecificationExecutor<" + tableNameCp + "> {" + RT2);

    sb.append(T1 + "/** spring data jpa標準の基本crud（findById）ではfilterが動作しないためjpql形式で定義. */" + RT);
    sb.append(
        T1 + "@Query(value = \"from " + tableNameCp + " where " + idFieldName + " = :id\")" + RT);
    sb.append(T1 + "Optional<" + tableNameCp + "> findById(Long id);" + RT2);

    if (tableInfo.hasUniqueConstraint()) {
      sb.append(T1 + "/** natural keyのqueryを自動生成 */" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findBy"
          + code.naturalKeyUncapitalCamelAndRelConsidered(tableInfo) + "(" + RT);
      sb.append(T3 + code.naturalKeyDefine(tableInfo) + ");" + RT2);
    }

    if (tableInfo.hasSoftDeleteFieldInludingSystemCommon()) {
      String commonComment = "/** Used for procedures in libraries. "
          + "Native query is used to search soft deleted records. */";
      sb.append(T1 + commonComment + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findByIdAndSoftDeleteFieldTrueFromAllGroups"
          + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      sb.append(T1 + commonComment + RT);
      if (!tableInfo.hasUniqueConstraint()) {
        sb.append(T1 + "/** The entity doesn't have a natural key. "
            + "Unsatisfied condition is used in the where clause. It not called from library. */"
            + RT);
      }
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where "
          + (tableInfo.hasUniqueConstraint() ? code.naturalKeySqlParams(tableInfo) : "1 = 2")
          + " and " + code.softDeleteColLowerSnake() + " = true\")" + RT);
      sb.append(
          T1 + "Optional<" + tableNameCp + "> findByNaturalKeyAndSoftDeleteFieldTrueFromAllGroups"
              + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      sb.append(T1 + commonComment + RT);
      sb.append(T1 + "@Modifying" + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"delete from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "void deleteByIdAndSoftDeleteFieldTrueFromAllGroups(@Param(\"entity\") "
          + tableNameCp + " entity);" + RT2);
    }

    createInsertOrUpdate(sb, tableInfo, relFieldList);

    naturalKeyDuplicatedCheck(sb, tableInfo, relFieldList);

    sb.append("}" + RT);

    outputFile(sb, getFilePath("repository"), tableNameCp + "BaseRepository.java");
  }

  private void createBaseRepositoryImport(DbOrClassTableInfo tableInfo, String tableNameCp,
      List<DbOrClassColumnInfo> relFieldList) throws AppException {
    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("java.util.*", rootBasePackage + ".base.entity." + tableNameCp,
        "org.springframework.data.jpa.repository.*",
        "org.springframework.data.repository.query.Param");

    if (tableInfo.hasUniqueConstraint()) {
      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        DataTypeInfo dtInfo = ci.getDtInfo();
        // dateは対象外（java.time.*をimportするが未使用）
        if (dtInfo.getKata() != DataTypeKataEnum.DATE) {
          importMgr.add(getHelper(dtInfo.getKata()).getNeededImports(ci));
        }
      }
    }

    // 使用するenumクラスをimport
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      if (colInfo.isUniqueConstraint() || colInfo.isPk()) {
        String dataType = colInfo.getDataType();
        DataTypeInfo dtInfo = colInfo.getDtInfo();
        if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
          String importClassStr = getRootBasePackageOfDataTypeFromAllSystem(colInfo.getDataType())
              + ".base.enums." + CodeGenUtil.dataTypeNameToCapitalCamel(dataType) + "Enum";
          importMgr.add(importClassStr);
        }
      }
    }

    importMgr.add(
        rootBasePackage + ".base.record." + code.capitalCamel(tableInfo.getName()) + "BaseRecord");
    relFieldList.stream().forEach(ci -> importMgr
        .add(rootBasePackage + ".base.entity." + code.capitalCamel(ci.getRelationRefTable())));
    if (tableInfo.hasUniqueConstraint()) {
      importMgr.add("jp.ecuacion.lib.core.exception.checked.BizLogicAppException");
    }

    sb.append(importMgr.outputStr() + RT);
  }

  private void createInsertOrUpdate(StringBuilder sb, DbOrClassTableInfo ti,
      List<DbOrClassColumnInfo> relFieldList) {

    final String idField = code.capitalCamel(ti.getPkColumn().getName());

    sb.append(T1 + "/** Is a utility to insert or update an entity. */" + RT);
    StringBuilder relString = new StringBuilder();
    relFieldList.stream().forEach(ci -> relString.append(
        ", " + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getRelationFieldName()));
    sb.append(T1 + "default void insertOrUpdate(" + code.capitalCamel(ti.getName())
        + "BaseRecord recForInsert, " + code.capitalCamel(ti.getName()) + " entityForUpdate"
        + relString + ", String... skipUpdateFields) {" + RT);
    sb.append(T2 + code.capitalCamel(ti.getName()) + " e = null;" + RT);
    sb.append(T2 + "boolean isInsert = recForInsert.get" + idField
        + "() == null || recForInsert.get" + idField + "().equals(\"\");" + RT2);

    sb.append(T2 + "if (isInsert) {" + RT);
    sb.append(T3 + "e = new " + code.capitalCamel(ti.getName()) + "(recForInsert);" + RT);
    relFieldList.stream()
        .forEach(ci -> sb.append(T3 + "e.set" + code.capitalCamel(ci.getRelationFieldName()) + "("
            + code.uncapitalCamel(ci.getRelationFieldName()) + ");" + RT));
    sb.append(RT);
    sb.append(T2 + "} else {" + RT);
    sb.append(T3 + "e = entityForUpdate;" + RT);
    StringBuilder relString2 = new StringBuilder();
    relFieldList.stream().filter(ci -> !ci.isGroupColumn())
        .forEach(ci -> relString2.append(", " + ci.getRelationFieldName()));
    sb.append(T3 + "e.update(recForInsert" + relString2 + ");" + RT);
    sb.append(T2 + "}" + RT2);
    sb.append(T2 + "save(e);" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void naturalKeyDuplicatedCheck(StringBuilder sb, DbOrClassTableInfo ti,
      List<DbOrClassColumnInfo> relFieldList) {
    if (!ti.hasUniqueConstraint()) {
      return;
    }

    String entityName = code.capitalCamel(ti.getName());
    sb.append(T1 + "default void naturalKeyDuplicatedCheck(" + entityName
        + "BaseRecord rec) throws BizLogicAppException {" + RT);
    sb.append(T2 + "Optional<" + entityName + "> optional = findBy"
        + StringUtils.capitalize(code.naturalKeyUncapitalCamelAndRelConsidered(ti)) + "(");

    boolean is1st = true;
    for (DbOrClassColumnInfo ci : ti.columnList) {
      if (ci.isUniqueConstraint()) {
        if (is1st) {
          is1st = false;

        } else {
          sb.append(", ");
        }

        sb.append("rec." + code.getOfEntityDataType(ci));
      }
    }

    sb.append(");" + RT2);
    sb.append(T2 + "if (optional.isPresent()) {" + RT);
    sb.append(T3 + "throw new BizLogicAppException(\"jp\");" + RT);
    sb.append(T2 + "}" + RT);
    sb.append(T1 + "}" + RT);
  }

  private void createSystemCommonBaseDao() {
    final MiscSoftDeleteRootInfo delFlgInfo = info.removedDataRootInfo;
    final MiscGroupRootInfo groupInfo = info.groupRootInfo;

    sb = new StringBuilder();
    // コンストラクタにgroupのカラムを引数としてつけるか否かのフラグ
    final boolean shouldAddGroupArg = (info.getGenPtn() != GeneratePtnEnum.NO_GROUP_QUERY
        && info.getGenPtn() != GeneratePtnEnum.DAO_ONLY_GROUP_NO_GROUP_QUERY)
        && groupInfo.isDefined() && !info.sysCmnRootInfo.isFrameworkKindSpring();


    sb.append("package " + rootBasePackage + ".base." + postfixSm + ";" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("jp.ecuacion.util.jpa.dao.AbstractDao",
        rootBasePackage + ".base.entity.SystemCommonEntity");
    importMgr.add("jakarta.annotation.Nonnull");

    if (delFlgInfo.isDefined()) {
      importMgr.add("jakarta.persistence.EntityManager");
    }

    // enumの使用を確認し、必要なenumのimportを追加
    if (info.dbCommonRootInfo != null) {
      for (DbOrClassColumnInfo colInfo : info.dbCommonRootInfo.tableList.get(0).columnList) {
        if (colInfo.getDtInfo().getKata() == DataTypeKataEnum.ENUM) {
          // 本当はここでどのprojectに属するenumなのかを判断する必要があるが、
          // SystemCommonについてはframeworkの場合のみしか実績がないのでいったんそうしておく。
          importMgr.add(EclibCoreConstants.PKG + ".base.enums.*");
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class SystemCommonBase" + postfixCp
        + "<T extends SystemCommonEntity> " + "extends AbstractDao<T> {" + RT2);

    sb.append(T1 + "protected static final String COL_NAME_GRP = \""
        + ((groupInfo == null || groupInfo.getColumnName() == null) ? null
            : groupInfo.getLwFieldName())
        + "\";" + RT);
    sb.append(T1 + "protected static final String COL_NAME_DEL_FLG = \"remFlg\";" + RT2);

    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "protected boolean isSpringJpa() {" + RT);
    sb.append(T2 + "return " + info.sysCmnRootInfo.isFrameworkKindSpring() + ";" + RT);
    sb.append(T1 + "}" + RT2);


    // group関連の定義
    Objects.requireNonNull(groupInfo);
    sb.append(T1 + "protected boolean isGroupDefined() {" + RT);
    sb.append(T2 + "return " + Boolean.valueOf(groupInfo.isDefined()).toString() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected boolean confinesQueryToGroup() {" + RT);
    sb.append(T2 + "return " + Boolean.valueOf(shouldAddGroupArg).toString() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected String getGroupFieldName() {" + RT);
    sb.append(T2 + "return " + ((shouldAddGroupArg) ? "COL_NAME_GRP" : "null") + ";" + RT);
    sb.append(T1 + "}" + RT2);

    // hasGroupFieldInTable()をabstract定義
    sb.append(T1 + "protected abstract boolean hasGroupFieldInTable();" + RT2);

    sb.append(T1 + "protected Object getGroupFieldValue() {" + RT);
    sb.append(
        T2 + "return " + ((shouldAddGroupArg) ? groupInfo.getLwFieldName() : "null") + ";" + RT);
    sb.append(T1 + "}" + RT2);

    if (shouldAddGroupArg) {
      sb.append(
          T1 + "protected " + groupInfo.getKata() + " " + groupInfo.getLwFieldName() + ";" + RT2);
    }

    // 削除フラグ関連の定義
    sb.append(T1 + "protected boolean isLogicalDeleteFlagDefined() {" + RT);
    sb.append(T2 + "return " + delFlgInfo.isDefined() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "@Nonnull");
    sb.append(T1 + "protected String getLogicalDeleteFlagFieldInfo() {" + RT);
    sb.append(T2 + "return \""
        + ((delFlgInfo.isDefined())
            ? StringUtil.getLowerCamelFromSnake(delFlgInfo.getColumnName()) + "\""
            : "null")
        + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected boolean hasLogicalDeleteFlagFieldInTable() {" + RT);
    sb.append(T2 + "return " + delFlgInfo.isDefined() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "@SafeVarargs" + RT);
    sb.append(T1 + "public SystemCommonBase" + postfixCp + "("
        + ((shouldAddGroupArg) ? groupInfo.getKata() + " " + groupInfo.getLwFieldName() + ", " : "")
        + "T... e) {" + RT);
    sb.append(T2 + "super(e);" + RT);
    if (shouldAddGroupArg) {
      sb.append(T2 + "this." + groupInfo.getLwFieldName() + " = " + groupInfo.getLwFieldName() + ";"
          + RT);
    }

    sb.append(T1 + "}" + RT2);

    if (delFlgInfo.isDefined()) {
      sb.append(T1 + "public void " + delFlgInfo.getRemoveMethodName() + "(EntityManager em, T e) {"
          + RT);
      sb.append(T2 + "try {" + RT);
      sb.append(T3 + "e.set" + StringUtil.getUpperCamelFromSnake(delFlgInfo.getColumnName())
          + "(true);" + RT);
      sb.append(T3 + "super.logicalDeleteByPk(em, e);" + RT);
      sb.append(T2 + "} catch (Exception ex) {" + RT);
      sb.append(T3 + "throw new RuntimeException(ex);" + RT);
      sb.append(T2 + "}" + RT);
      sb.append(T1 + "}" + RT2);
    }

    sb.append("}" + RT);

    outputFile(sb, getFilePath(postfixSm), "SystemCommonBase" + postfixCp + ".java");
  }

  private void createSystemCommonBaseRepository() {

    sb = new StringBuilder();

    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("jp.ecuacion.splib.jpa.repository.SplibRepository",
        "org.springframework.data.repository.NoRepositoryBean");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@NoRepositoryBean" + RT);
    sb.append(
        "public interface SystemCommonBaseRepository<T, I> extends SplibRepository<T, I> {" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath("repository"), "SystemCommonBaseRepository.java");
  }
}
