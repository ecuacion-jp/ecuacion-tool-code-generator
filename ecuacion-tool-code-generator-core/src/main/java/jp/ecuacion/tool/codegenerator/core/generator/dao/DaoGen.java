/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.core.generator.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractTableGen;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil.ColFormat;

/** Generates base DAO and Spring Data JPA repository source files for each entity. */
public class DaoGen extends AbstractTableGen {

  private ColumnGenUtil code = new ColumnGenUtil();

  /** Constructs an instance for the specified data kind. */
  public DaoGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);
  }

  @Override
  public void generate() {

    // Create baseDao / baseRepositoryImpl per entity
    for (DbOrClassTableInfo tableInfo : getInfo().getDbRootInfo().tableList) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

      // super.makePkList(tableInfo);
      if (getInfo().getSysCmnRootInfo().getUsesUtilJpa()) {
        createBaseDaos(tableInfo, entityNameCp);
      }

      // Generate baseRepository when using Spring
      if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()) {
        createBaseRepository(tableInfo, entityNameCp);
      }
    }

    // Create SystemCommonDao
    if (getInfo().getSysCmnRootInfo().getUsesUtilJpa()) {
      createSystemCommonBaseDao();
    }

    // Generate baseRepository when using Spring
    if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()) {
      createSystemCommonBaseRepository();
    }
  }

  /** Creates base DAO source files for the given table. */
  public void createBaseDaos(DbOrClassTableInfo ti, String entityNameCp) {
    sb = new StringBuilder();


    // Declaration and constructor
    sb.append("package " + rootBasePackage + ".base." + postfixSm + ";" + RT2);

    createBaseDaoImport(ti, entityNameCp);

    MiscGroupRootInfo groupInfo = getInfo().getGroupRootInfo();

    sb.append("public abstract class " + entityNameCp + "Base" + postfixCp
        + " extends SystemCommonBase" + postfixCp + "<" + entityNameCp + "> {" + RT2);

    // Branch constructor generation depending on whether Spring is used
    if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()) {
      // Generate a simple no-argument constructor
      sb.append(T1 + "public " + entityNameCp + "Base" + postfixCp + "() {" + RT);
      sb.append(T2 + "super(new " + entityNameCp + "[0]);" + RT);

    } else {
      // Pass the concrete class to AbstractDao as a workaround
      sb.append(T1 + "public " + entityNameCp + "Base" + postfixCp + "("
          + ((groupInfo.isDefined() && ti.hasGroupColumnIncludingSystemCommon())
              ? groupInfo.getKata() + " " + groupInfo.getLwFieldName()
              : "")
          + ") {" + RT);
      sb.append(T2 + "super("
          + (groupInfo.isDefined()
              ? (ti.hasGroupColumnIncludingSystemCommon() ? groupInfo.getLwFieldName() : "null")
                  + ", "
              : "")
          + "new " + entityNameCp + "[0]);" + RT);
    }

    sb.append(T1 + "}" + RT2);

    // Returns whether this entity has a grpMeta field as bool
    sb.append(T1 + "protected boolean hasGroupFieldInTable() {" + RT);
    sb.append(T2 + "return " + ti.hasGroupColumnIncludingSystemCommon() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    // getSetOfUniqueConstraintFieldList()
    // sb.append(T1 + "@Override" + RT);
    // sb.append(T1 + "protected Set<List<FieldInfo>> getSetOfUniqueConstraintFieldList() {" + RT);
    // sb.append(T2 + "return " + entityNameCp + ".getSetOfUniqueConstraintFieldList();" + RT);
    // sb.append(T1 + "}" + RT2);

    // selectEntityByPk. When a delete flag is specified, include it in the where clause.
    sb.append(T1 + "/** " + RT);
    sb.append(T1 + " * Selects by pk." + RT);
    sb.append(T1 + " */" + RT);
    // Build the string portion that varies depending on whether a group is specified
    sb.append(T1 + "public " + entityNameCp + " selectEntityByPk"
        + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectEntityByPkForBaseDao(em, \"selectEntityByPk" + "\", \""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue, false);"
        + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** " + RT);
    sb.append(T1 + " * Selects by pk with FOR UPDATE." + RT);
    sb.append(T1 + " */" + RT);
    sb.append(T1 + "public " + entityNameCp + " selectEntityByPkForUpdate"
        + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectEntityByPkForBaseDao(em, \"selectEntityByPkForUpdate" + "\", \""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue, true);"
        + RT);
    sb.append(T1 + "}" + RT2);

    if (ti.hasUniqueConstraint()) {
      sb.append(T1 + "/** " + RT);
      sb.append(T1 + " * Selects by natural key when surrogate key is used." + RT);
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
    sb.append(T1 + " * Selects all records (within the group if a group is defined)." + RT);
    sb.append(T1 + " */" + RT);
    sb.append(T1 + "public List<" + entityNameCp + "> selectEntityList" + "(EntityManager em"
        + ") {" + RT);
    sb.append(T2 + "return selectEntityListForBaseDao(em, \"selectEntityList" + "\", false);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** Gets count by pk. */" + RT);
    sb.append(
        T1 + "public Long selectCountByPk" + "(EntityManager em, Object pkValue" + ") {" + RT);
    sb.append(T2 + "return selectCountForBaseDao(em, \"selectCountByPk" + "\", getParamMapFromPk(\""
        + StringUtil.getLowerCamelFromSnake(ti.getPkColumn().getName()) + "\", pkValue));" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "/** Counts all records (within the group if a group is defined). */" + RT);
    sb.append(T1 + "public Long selectCountAll" + "(EntityManager em" + ") {" + RT);
    sb.append(T2 + "return selectCountForBaseDao(em, \"selectCountAll" + "\", null);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath(postfixSm), entityNameCp + "Base" + postfixCp + ".java");
  }

  private void createBaseDaoImport(DbOrClassTableInfo ti, String entityNameCp) {
    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.util.*", "jakarta.persistence.EntityManager",
        rootBasePackage + ".base.entity." + entityNameCp);
    if (ti.hasUniqueConstraint()) {
      importMgr.add("jp.ecuacion.util.jpa.dao.query.QueryCondition");
      for (DbOrClassColumnInfo ci : ti.columnList) {
        DataTypeInfo dtInfo = ci.getDtInfo();
        importMgr.add(code.getHelper(dtInfo.getKata()).getNeededImports(ci));
      }
    }

    // Import enum classes used
    for (DbOrClassColumnInfo colInfo : ti.columnList) {
      if (colInfo.isUniqueConstraint() || colInfo.isPk()) {
        String dataType = colInfo.getDataType();
        DataTypeInfo dtInfo = colInfo.getDtInfo();
        if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
          String importClassStr =
              rootBasePackage + ".base.enums." + code.dataTypeNameToCapitalCamel(dataType) + "Enum";
          importMgr.add(importClassStr);
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);
  }

  private void createBaseRepository(DbOrClassTableInfo tableInfo, String tableNameCp) {

    sb = new StringBuilder();

    List<DbOrClassColumnInfo> list = new ArrayList<>(tableInfo.columnList);
    list.addAll(getInfo().getDbCommonRootInfo().tableList.get(0).columnList);
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

    // Declaration and constructor
    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    // import
    createBaseRepositoryImport(tableInfo, tableNameCp);

    sb.append("public interface " + tableNameCp + "BaseRepository"
        + " extends SystemCommonBaseRepository<" + tableNameCp
        + ", Long>, JpaSpecificationExecutor<" + tableNameCp + "> {" + RT2);

    sb.append(T1 + "/** Is defined with jpql because hibernate filter "
        + "does not take effect to spring data jpa standard 'findById'. */" + RT);
    sb.append(
        T1 + "@Query(value = \"from " + tableNameCp + " where " + idFieldName + " = :id\")" + RT);
    sb.append(T1 + "Optional<" + tableNameCp + "> findById(Long id);" + RT2);

    // findBy<NaturalKey>
    if (tableInfo.hasUniqueConstraint()) {
      sb.append(T1 + "/** Finds by natural key. */" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findBy"
          + code.naturalKeyUncapitalCamelAndRelConsidered(tableInfo) + "(" + RT);
      sb.append(T3 + code.naturalKeyDefine(tableInfo) + ");" + RT2);
    }

    // findBy<IdOfRelation>
    for (DbOrClassColumnInfo ci : tableInfo.getRelationColumnList()) {
      String rtnType = ci.getRelationKind() == RelationKindEnum.ONE_TO_ONE ? "Optional" : "List";
      sb.append(
          T1 + "/** Is generated for existence check when a parent record is deleted. */" + RT);
      sb.append(T1 + "public " + rtnType + "<" + tableInfo.getNameCpCamel() + "> findBy"
          + code.generateString(ci, ColFormat.QUERY_METHOD) + "(" + code.getJavaKata(ci) + " "
          + getInfo().getTableInfo(ci.getRelationRefTable())
              .getPkColumn().getNameCamel() + ");" + RT2);
    }

    if (tableInfo.hasSoftDeleteFieldInludingSystemCommon()) {

      // findAllFromAllGroups
      sb.append(T1 + "@Query(nativeQuery = true, value = "
          + "\"select * from Instance where del_flg = false\")" + RT);
      sb.append(T1 + "public List<" + tableNameCp + "> findAllFromAllGroups();" + RT2);

      String commonComment = "/** Used for procedures in libraries. "
          + "Native query is used to search soft deleted records.";
      sb.append(T1 + commonComment + " */" + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findByIdAndSoftDeleteFieldTrueFromAllGroups"
          + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      String noNaturalKeyMsg = T1 + "The entity doesn't have a natural key. "
          + "Unsatisfied condition is used in the where clause. It not called from library. */";
      sb.append(T1 + commonComment
          + (tableInfo.hasUniqueConstraint() ? " */" : RT + noNaturalKeyMsg) + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where "
          + (tableInfo.hasUniqueConstraint() ? code.naturalKeySqlParams(tableInfo) : "1 = 2")
          + " and " + code.softDeleteColLowerSnake() + " = true\")" + RT);
      sb.append(
          T1 + "Optional<" + tableNameCp + "> findByNaturalKeyAndSoftDeleteFieldTrueFromAllGroups"
              + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      sb.append(T1 + commonComment + " */" + RT);
      sb.append(T1 + "@Modifying" + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"delete from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "void deleteByIdAndSoftDeleteFieldTrueFromAllGroups(@Param(\"entity\") "
          + tableNameCp + " entity);" + RT2);
    }

    sb.append("}" + RT);

    outputFile(sb, getFilePath("repository"), tableNameCp + "BaseRepository.java");
  }

  private void createBaseRepositoryImport(DbOrClassTableInfo tableInfo, String tableNameCp) {
    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.util.*", rootBasePackage + ".base.entity." + tableNameCp,
        "org.springframework.data.jpa.repository.*",
        "org.springframework.data.repository.query.Param");

    if (tableInfo.hasUniqueConstraint()) {
      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        if (ci.isPk() || ci.isUniqueConstraint()) {
          DataTypeInfo dtInfo = ci.getDtInfo();
          importMgr.add(code.getHelper(dtInfo.getKata()).getNeededImports(ci));
        }
      }
    }

    // Import enum classes used
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      if (colInfo.isUniqueConstraint() || colInfo.isPk()) {
        String dataType = colInfo.getDataType();
        DataTypeInfo dtInfo = colInfo.getDtInfo();
        if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
          String importClassStr =
              rootBasePackage + ".base.enums." + code.dataTypeNameToCapitalCamel(dataType) + "Enum";
          importMgr.add(importClassStr);
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);
  }

  private void createSystemCommonBaseDao() {
    final MiscSoftDeleteRootInfo delFlgInfo = getInfo().getRemovedDataRootInfo();
    final MiscGroupRootInfo groupInfo = getInfo().getGroupRootInfo();

    sb = new StringBuilder();
    // Flag indicating whether to add the group column as a constructor argument
    final boolean shouldAddGroupArg =
        groupInfo.isDefined() && !getInfo().getSysCmnRootInfo().isFrameworkKindSpring();


    sb.append("package " + rootBasePackage + ".base." + postfixSm + ";" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("jp.ecuacion.util.jpa.dao.AbstractDao",
        rootBasePackage + ".base.entity.SystemCommon");
    importMgr.add("org.jspecify.annotations.NonNull");

    // Check enum usage and add required enum imports
    if (getInfo().getDbCommonRootInfo() != null) {
      for (DbOrClassColumnInfo colInfo :
          getInfo().getDbCommonRootInfo().tableList.get(0).columnList) {
        if (colInfo.getDtInfo().getKata() == DataTypeKataEnum.ENUM) {
          // Ideally we would determine which project the enum belongs to here, but since
          // SystemCommon has only been used with the framework, it is handled that way for now.
          importMgr.add(EclibCoreConstants.PKG + ".base.enums.*");
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class SystemCommonBase" + postfixCp + "<T extends SystemCommon> "
        + "extends AbstractDao<T> {" + RT2);

    sb.append(T1 + "protected static final String COL_NAME_GRP = \""
        + ((groupInfo == null || groupInfo.getColumnName() == null) ? null
            : groupInfo.getLwFieldName())
        + "\";" + RT);
    sb.append(T1 + "protected static final String COL_NAME_DEL_FLG = \"remFlg\";" + RT2);

    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "protected boolean isSpringJpa() {" + RT);
    sb.append(T2 + "return " + getInfo().getSysCmnRootInfo().isFrameworkKindSpring() + ";" + RT);
    sb.append(T1 + "}" + RT2);


    // Group-related definitions
    Objects.requireNonNull(groupInfo);
    sb.append(T1 + "protected boolean isGroupDefined() {" + RT);
    sb.append(T2 + "return " + Boolean.valueOf(groupInfo.isDefined()).toString() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected boolean confinesQueryToGroup() {" + RT);
    sb.append(T2 + "return " + Boolean.valueOf(shouldAddGroupArg).toString() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected String getGroupFieldName() {" + RT);
    sb.append(T2 + "return " + (shouldAddGroupArg ? "COL_NAME_GRP" : "null") + ";" + RT);
    sb.append(T1 + "}" + RT2);

    // Declare hasGroupFieldInTable() as abstract
    sb.append(T1 + "protected abstract boolean hasGroupFieldInTable();" + RT2);

    sb.append(T1 + "protected Object getGroupFieldValue() {" + RT);
    sb.append(
        T2 + "return " + (shouldAddGroupArg ? groupInfo.getLwFieldName() : "null") + ";" + RT);
    sb.append(T1 + "}" + RT2);

    if (shouldAddGroupArg) {
      sb.append(
          T1 + "protected " + groupInfo.getKata() + " " + groupInfo.getLwFieldName() + ";" + RT2);
    }

    // Delete-flag-related definitions
    sb.append(T1 + "protected boolean isLogicalDeleteFlagDefined() {" + RT);
    sb.append(T2 + "return " + delFlgInfo.isDefined() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "@NonNull");
    sb.append(T1 + "protected String getLogicalDeleteFlagFieldInfo() {" + RT);
    sb.append(T2 + "return \""
        + (delFlgInfo.isDefined()
            ? StringUtil.getLowerCamelFromSnake(delFlgInfo.getColumnName()) + "\""
            : "null")
        + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "protected boolean hasLogicalDeleteFlagFieldInTable() {" + RT);
    sb.append(T2 + "return " + delFlgInfo.isDefined() + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "@SafeVarargs" + RT);
    sb.append(T1 + "public SystemCommonBase" + postfixCp + "("
        + (shouldAddGroupArg ? groupInfo.getKata() + " " + groupInfo.getLwFieldName() + ", " : "")
        + "T... e) {" + RT);
    sb.append(T2 + "super(e);" + RT);
    if (shouldAddGroupArg) {
      sb.append(T2 + "this." + groupInfo.getLwFieldName() + " = " + groupInfo.getLwFieldName() + ";"
          + RT);
    }

    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath(postfixSm), "SystemCommonBase" + postfixCp + ".java");
  }

  private void createSystemCommonBaseRepository() {

    sb = new StringBuilder();

    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    ImportBlock importMgr = new ImportBlock();
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
