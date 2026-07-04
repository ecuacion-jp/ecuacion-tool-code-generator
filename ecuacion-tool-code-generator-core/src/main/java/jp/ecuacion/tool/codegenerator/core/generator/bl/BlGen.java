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
package jp.ecuacion.tool.codegenerator.core.generator.bl;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.RelationRefInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil.ColFormat;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil.ColListFormat;
import org.apache.commons.lang3.StringUtils;

/** Generates the base business logic classes for each DB table entity. */
public class BlGen extends AbstractGen {

  private ColumnGenUtil code = new ColumnGenUtil();

  /** Constructs an instance with no specific table target (generates for all tables). */
  public BlGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    generateBl(true, getInfo().getDbCommonRootInfo().tableList);
    generateBl(false, getInfo().getDbRootInfo().tableList);
  }

  private void generateBl(boolean isSystemCommon, @Valid List<DbOrClassTableInfo> tableList) {

    for (DbOrClassTableInfo ti : tableList) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(ti.getName());
      sb = new StringBuilder();

      generateHeader(isSystemCommon, ti, entityNameCp);

      if (!isSystemCommon) {
        generateFields(ti, entityNameCp);
        getRepositoryForOptimisticLocking(entityNameCp);
        getFindAndOptimisticLockingCheckRec(ti, entityNameCp);
      }

      generateGetVersionForOptimisticLocking(ti, entityNameCp);

      if (!isSystemCommon) {
        generateInsertOrUpdate(ti);
        generateDuplicateCheck(ti);
        generateNaturalKeyDuplicateCheck(ti);
        generateChildExistenceCheck(ti);
        allChildrenExistenceChecks(ti);
      }

      sb.append("}" + RT);

      outputFile(sb, getFilePath("bl"), entityNameCp + "BaseBl.java");
    }
  }

  /**
   * Generates the package declaration, import statements, and class declaration for the base
   * BL class.
   */
  public void generateHeader(boolean isSystemCommon, DbOrClassTableInfo ti, String entityNameCp) {
    sb.append("package " + rootBasePackage + ".base.bl;" + RT2);

    ImportBlock importMgr = new ImportBlock();

    importMgr.add(rootBasePackage + ".base.entity.*");

    if (isSystemCommon) {
      importMgr.add("jp.ecuacion.splib.jpa.bl.*");

    } else {
      importMgr.add("org.springframework.beans.factory.annotation.Autowired",
          "jp.ecuacion.splib.jpa.repository.SplibRepository", rootBasePackage + ".base.record.*",
          rootBasePackage + ".base.repository.*", "java.util.List");

      if (ti.columnList.stream().filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
          || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP).toList().size() > 0) {
        importMgr.add("java.time.*");
      }
    }

    if (ti.hasUniqueConstraint()) {
      importMgr.add("java.util.Optional");
    }

    if (!isSystemCommon && ti.getPkColumn().getRelationRefInfoList().size() > 0) {
      importMgr.add("java.util.Arrays");
    }

    sb.append(importMgr.outputStr() + RT);

    String extendsStr = isSystemCommon
        ? "<E extends SystemCommon" + (ti.hasPkColumn() ? "" : ", I") + "> extends SplibJpaBl<E, "
            + (ti.hasPkColumn() ? code.getJavaKata(ti.getPkColumn()) : "I") + ", "
            + code.getJavaKata(ti.getVersionColumnIncludingSystemCommon()) + ">"
        : " extends SystemCommonBaseBl<" + entityNameCp + ", " + code.getJavaKata(ti.getPkColumn())
            + ">";
    sb.append("public abstract class " + entityNameCp + "BaseBl" + extendsStr + " {" + RT2);
  }

  private void generateFields(DbOrClassTableInfo ti, String entityNameCp) {
    sb.append(T1 + "@Autowired" + RT);
    sb.append(T1 + "protected " + entityNameCp + "BaseRepository repo;" + RT2);

    // Deduplicate by orgTableName: when multiple FK columns from the same source table
    // all reference this table, only one repository field per source table is needed.
    ti.columnList.stream().map(ci -> ci.getRelationRefInfoList()).flatMap(l -> l.stream())
        .map(RelationRefInfo::getOrgTableName).distinct().forEach(orgTableName -> {
          sb.append(T1 + "@Autowired" + RT);
          sb.append(T1 + "protected " + code.capitalCamel(orgTableName) + "BaseRepository "
              + code.uncapitalCamel(orgTableName) + "Repo;" + RT2);
        });
  }

  private void getRepositoryForOptimisticLocking(String entityNameCp) {
    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public SplibRepository<" + entityNameCp
        + ", Long> getRepositoryForOptimisticLocking() {" + RT);
    sb.append(T2 + "return repo;" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void getFindAndOptimisticLockingCheckRec(DbOrClassTableInfo ti, String entityNameCp) {

    sb.append(T1 + "public " + entityNameCp + " findAndOptimisticLockingCheck(" + entityNameCp
        + "BaseRecord rec) {" + RT);
    sb.append(T2 + "return findAndOptimisticLockingCheck(rec.get"
        + code.capitalCamel(ti.getPkColumn().getName()) + "OfEntityDataType(), " + "rec.get"
        + ti.getVersionColumnIncludingSystemCommon().getNameCpCamel() + "OfEntityDataType());"
        + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateGetVersionForOptimisticLocking(DbOrClassTableInfo ti, String entityNameCp) {
    if (!ti.hasVersionColumn()) {
      return;
    }

    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public " + code.getJavaKata(ti.getVersionColumnIncludingSystemCommon())
        + " getVersionForOptimisticLocking(" + entityNameCp + " e) {" + RT);
    sb.append(
        T2 + "return e.get" + code.capitalCamel(ti.getVersionColumn().getName()) + "();" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateInsertOrUpdate(DbOrClassTableInfo ti) {

    final List<DbOrClassColumnInfo> relFieldList =
        ti.columnList.stream().filter(e -> !e.getIsJavaOnly()).filter(e -> e.isRelation()).toList();

    final String entityName = code.capitalCamel(ti.getName());
    final String idField = code.capitalCamel(ti.getPkColumn().getName());

    sb.append(T1 + "/** Is a utility to insert or update an entity. */" + RT);
    StringBuilder dateTimeString = new StringBuilder();
    ti.columnList.stream().filter(ci -> !ci.getIsJavaOnly())
        .filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
            || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP)
        .forEach(ci -> dateTimeString
            .append(", " + code.getJavaKata(ci) + " " + code.uncapitalCamel(ci.getName())));
    StringBuilder relString = new StringBuilder();
    relFieldList.stream().forEach(ci -> relString.append(", "
        + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getEffectiveRelationObjVarName()));
    sb.append(T1 + "public " + entityName + " insertOrUpdate(" + code.capitalCamel(ti.getName())
        + "BaseRecord rec" + dateTimeString + relString + ", String... skipUpdateFields) {" + RT);
    sb.append(T2 + entityName + " e = null;" + RT);
    sb.append(T2 + "boolean isInsert = rec.get" + idField + "() == null || rec.get" + idField
        + "().equals(\"\");" + RT2);

    StringBuilder dateTimeString2 = new StringBuilder();
    ti.columnList.stream().filter(ci -> !ci.getIsJavaOnly())
        .filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
            || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP)
        .forEach(ci -> dateTimeString2.append(", " + code.uncapitalCamel(ci.getName())));
    StringBuilder relString2 = new StringBuilder();
    relFieldList.stream()
        .forEach(ci -> relString2.append(", " + ci.getEffectiveRelationObjVarName()));

    sb.append(T2 + "if (isInsert) {" + RT);
    sb.append(T3 + "e = new " + entityName + "(rec" + dateTimeString2 + relString2 + ");" + RT);
    // relFieldList.stream()
    // .forEach(ci -> sb.append(T3 + "e.set" + code.capitalCamel(ci.getRelationFieldName()) + "("
    // + code.uncapitalCamel(ci.getRelationFieldName()) + ");" + RT));
    sb.append(RT);
    sb.append(T2 + "} else {" + RT);
    sb.append(T3 + "e = findAndOptimisticLockingCheck(rec);" + RT);
    sb.append(T3 + "e.update(rec" + dateTimeString2 + relString2 + ", skipUpdateFields);" + RT);
    sb.append(T2 + "}" + RT2);
    sb.append(T2 + "return repo.save(e);" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateDuplicateCheck(DbOrClassTableInfo ti) {
    sb.append(T1 + "private void duplicateCheck(boolean isCheckFromAllGroups, List<"
        + ti.getNameCpCamel() + "> entityList, " + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) {" + RT);
    sb.append(T2 + "internalDuplicateCheck(isCheckFromAllGroups, entityList, rec, \""
        + ti.getNameCamel() + "\", \"id\", " + "targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheck(List<" + ti.getNameCpCamel() + "> entityList, "
        + ti.getNameCpCamel() + "BaseRecord rec, String... targetItemPropertyPaths) {" + RT);
    sb.append(T2 + "duplicateCheck(false, entityList, rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheck(" + ti.getNameCpCamel() + "BaseRecord rec, "
        + "String... targetItemPropertyPaths) {" + RT);
    sb.append(T2 + "duplicateCheck(repo.findAll(), rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheckFromAllGroups(List<" + ti.getNameCpCamel()
        + "> entityList, " + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) {" + RT);
    sb.append(T2 + "duplicateCheck(true, entityList, rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheckFromAllGroups(" + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) {" + RT);
    sb.append(T2
        + "duplicateCheckFromAllGroups(repo.findAllFromAllGroups(), rec, targetItemPropertyPaths);"
        + RT);
    sb.append(T1 + "}" + RT2);
  }

  @SuppressWarnings("null")
  private void generateNaturalKeyDuplicateCheck(DbOrClassTableInfo ti) {
    if (!ti.hasUniqueConstraint()) {
      return;
    }

    String entityName = code.capitalCamel(ti.getName());
    List<String> itemPropertyPathList = ti.columnList.stream().filter(ci -> ci.isUniqueConstraint())
        .map(ci -> code.generateString(ci, ColFormat.ITEM_PROPERTY_PATH)).toList();

    sb.append(T1 + "public void naturalKeyDuplicateCheck(" + entityName + "BaseRecord rec) {" + RT);
    final String itemNameKeysStr =
        "new String[] {" + StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ",
            "rec.getItem(\"", "\").getItemNameKey()") + "}";

    sb.append(T2 + "Optional<" + entityName + "> optional = repo.findBy"
        + StringUtils.capitalize(code.naturalKeyUncapitalCamelAndRelConsidered(ti)) + "("
        + code.generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
            ColListFormat.REC_GET_OF_ENTITY_DATA_TYPE)
        + ");" + RT);

    String pkCapFieldName = code.capitalCamel(ti.getPkColumn().getName());
    sb.append(T2 + "throwExceptionWhenDuplicated(optional.isPresent() && !optional.get().get"
        + pkCapFieldName + "().equals(rec.get" + pkCapFieldName
        + "OfEntityDataType()), false, new String[] {"
        + StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ", "\"") + "}, "
        + itemNameKeysStr + ");" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateChildExistenceCheck(DbOrClassTableInfo ti) {
    String methodDefArgPkCol =
        code.getJavaKata(ti.getPkColumn()) + " " + ti.getPkColumn().getNameCamel();
    String methodDefArgRec =
        ti.getNameCpCamel() + "BaseRecord rec, ChildExistenceCheckConditionBean[] conditions, "
            + "String referingRecordDataLabel, String recordSpecifyingFieldName";
    String checkMethodAdditionalArgs =
        ", conditions, referingRecordDataLabel, recordSpecifyingFieldName";
    String repoArgPkCol = ti.getPkColumn().getNameCamel();
    String repoArgRec = "rec.get" + ti.getPkColumn().getNameCpCamel() + "OfEntityDataType()";

    // When multiple FK columns from the same source table all reference this table's PK,
    // generate one set of methods per source table (not one per FK) to avoid duplicate
    // method signatures. Each method body calls internalChildExistenceCheck once per FK.
    @SuppressWarnings("null")
    List<String> uniqueOrgTableNames = ti.getPkColumn().getRelationRefInfoList().stream()
        .map(RelationRefInfo::getOrgTableName).distinct().toList();

    for (String orgTableName : uniqueOrgTableNames) {
      List<RelationRefInfo> refInfosForOrgTable = ti.getPkColumn().getRelationRefInfoList()
          .stream().filter(r -> r.getOrgTableName().equals(orgTableName)).toList();
      RelationRefInfo first = refInfosForOrgTable.get(0);

      String methodDefPrefix = "public void childExistenceCheck" + first.getOrgTableNameCpCamel()
          + "(";
      String methodDefPostfix = ") {";
      String msgId = "jp.ecuacion.splib.core.entity." + first.getOrgTableNameCamel();

      // method (args : pk)
      sb.append(T1 + methodDefPrefix + methodDefArgPkCol + methodDefPostfix + RT);
      sb.append(T2 + "childExistenceCheck" + first.getOrgTableNameCpCamel() + "(" + repoArgPkCol
          + ", (String) null);" + RT);
      sb.append(T1 + "}" + RT2);

      // method (args : pk, messageId)
      sb.append(
          T1 + methodDefPrefix + methodDefArgPkCol + ", String messageId" + methodDefPostfix + RT);
      sb.append(T2 + "String entityMsgIdPart = \"" + msgId + "\";" + RT);
      for (RelationRefInfo refInfo : refInfosForOrgTable) {
        DbOrClassTableInfo relOrgTi = getInfo().getTableInfo(refInfo.getOrgTableName());
        String orgFieldNameUpper =
            StringUtil.getLowerSnakeFromCamel(refInfo.getOrgFieldName()).toUpperCase(Locale.ROOT);
        DbOrClassColumnInfo relOrgCi = relOrgTi.getColumn(orgFieldNameUpper);
        sb.append(T2 + "internalChildExistenceCheck(" + refInfo.getOrgTableNameCamel()
            + "Repo.findBy" + code.generateString(relOrgCi, ColFormat.QUERY_METHOD) + "("
            + repoArgPkCol + "), messageId, entityMsgIdPart);" + RT);
      }
      sb.append(T1 + "}" + RT2);

      // method (args : pk, ChildExistenceCheckConditionBean...)
      sb.append(T1 + methodDefPrefix + methodDefArgPkCol
          + ", ChildExistenceCheckConditionBean... conditions" + methodDefPostfix + RT);
      sb.append(T2 + "String entityMsgIdPart = \"" + msgId + "\";" + RT);
      for (RelationRefInfo refInfo : refInfosForOrgTable) {
        DbOrClassTableInfo relOrgTi = getInfo().getTableInfo(refInfo.getOrgTableName());
        String orgFieldNameUpper =
            StringUtil.getLowerSnakeFromCamel(refInfo.getOrgFieldName()).toUpperCase(Locale.ROOT);
        DbOrClassColumnInfo relOrgCi = relOrgTi.getColumn(orgFieldNameUpper);
        sb.append(T2 + "internalChildExistenceCheck(" + refInfo.getOrgTableNameCamel()
            + "Repo.findBy" + code.generateString(relOrgCi, ColFormat.QUERY_METHOD) + "("
            + repoArgPkCol + "), entityMsgIdPart, conditions);" + RT);
      }
      sb.append(T1 + "}" + RT2);

      // method (args : rec)
      sb.append(T1 + methodDefPrefix + methodDefArgRec + methodDefPostfix + RT);
      sb.append(T2 + "String entityMsgIdPart = \"" + msgId + "\";" + RT);
      for (RelationRefInfo refInfo : refInfosForOrgTable) {
        DbOrClassTableInfo relOrgTi = getInfo().getTableInfo(refInfo.getOrgTableName());
        String orgFieldNameUpper =
            StringUtil.getLowerSnakeFromCamel(refInfo.getOrgFieldName()).toUpperCase(Locale.ROOT);
        DbOrClassColumnInfo relOrgCi = relOrgTi.getColumn(orgFieldNameUpper);
        sb.append(T2 + "internalChildExistenceCheck(" + refInfo.getOrgTableNameCamel()
            + "Repo.findBy" + code.generateString(relOrgCi, ColFormat.QUERY_METHOD) + "("
            + repoArgRec + "), null, entityMsgIdPart" + checkMethodAdditionalArgs + ");" + RT);
      }
      sb.append(T1 + "}" + RT2);
    }
  }

  private void allChildrenExistenceChecks(DbOrClassTableInfo ti) {
    if (ti.getPkColumn().getRelationRefInfoList().size() > 0) {

      String fiName = ti.getPkColumn().getNameCamel();
      String mtdArg = code.getJavaKata(ti.getPkColumn()) + " " + fiName;

      sb.append(T1 + "public void allChildrenExistenceChecks(" + mtdArg + ") {" + RT);
      sb.append(T2 + "allChildrenExistenceChecks(" + fiName + ", null);" + RT);
      sb.append(T1 + "}" + RT2);

      sb.append(T1 + "public void allChildrenExistenceChecks(" + mtdArg
          + ", String messageId, Class<?>... clses) {" + RT);
      sb.append(T2 + "List<Class<?>> skipList = Arrays.asList(clses);" + RT2);

      // Deduplicate by orgTableName to avoid calling the same check method twice when
      // multiple FK columns from the same source table reference this table.
      ti.getPkColumn().getRelationRefInfoList().stream()
          .map(RelationRefInfo::getOrgTableName).distinct().forEach(orgTableName -> {
            String orgNameCpCamel = code.capitalCamel(orgTableName);
            sb.append(T2 + "if (!skipList.contains(" + orgNameCpCamel + ".class)) "
                + "childExistenceCheck" + orgNameCpCamel + "(" + fiName + ", messageId);" + RT);
          });

      sb.append(T1 + "}" + RT);
    }
  }
}
