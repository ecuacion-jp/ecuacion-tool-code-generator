package jp.ecuacion.tool.codegenerator.core.generator.bl;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil.ColFormat;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil.ColListFormat;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public class BlGen extends AbstractGen {

  private CodeGenUtil code = new CodeGenUtil();

  public BlGen() {
    super(null);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    generateBl(true, info.dbCommonRootInfo.tableList);
    generateBl(false, info.dbRootInfo.tableList);
  }

  private void generateBl(boolean isSystemCommon, @Valid List<DbOrClassTableInfo> tableList)
      throws AppException {

    for (DbOrClassTableInfo ti : tableList) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(ti.getName());
      sb = new StringBuilder();

      generateHeader(isSystemCommon, ti, entityNameCp);

      if (!isSystemCommon) {
        generateFields(ti, entityNameCp);
        getRepositoryForOptimisticLocking(ti, entityNameCp);
        getFindAndOptimisticLockingCheckRec(ti, entityNameCp);
      }

      generateGetVersionForOptimisticLocking(ti, entityNameCp);

      if (!isSystemCommon) {
        generateInsertOrUpdate(ti);
        generateDuplicateCheck(ti);
        generateNaturalKeyDuplicateCheck(ti);
      }

      sb.append("}" + RT);

      outputFile(sb, getFilePath("bl"), entityNameCp + "BaseBl.java");
    }
  }

  public void generateHeader(boolean isSystemCommon, DbOrClassTableInfo ti, String entityNameCp)
      throws AppException {
    sb.append("package " + rootBasePackage + ".base.bl;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();

    importMgr.add(rootBasePackage + ".base.entity.*");

    if (isSystemCommon) {
      importMgr.add("jp.ecuacion.splib.jpa.bl.*");

    } else {
      importMgr.add("org.springframework.beans.factory.annotation.Autowired",
          "jp.ecuacion.splib.jpa.repository.SplibRepository", rootBasePackage + ".base.record.*",
          rootBasePackage + ".base.repository.*", "jp.ecuacion.lib.core.exception.checked.*",
          "java.util.List");

      if (ti.columnList.stream().filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
          || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP).toList().size() > 0) {
        importMgr.add("java.time.*");
      }
    }

    if (ti.hasUniqueConstraint()) {
      importMgr.add("jp.ecuacion.lib.core.exception.checked.*", "java.util.Optional");
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
  }

  private void getRepositoryForOptimisticLocking(DbOrClassTableInfo ti, String entityNameCp) {
    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public SplibRepository<" + entityNameCp
        + ", Long> getRepositoryForOptimisticLocking() {" + RT);
    sb.append(T2 + "return repo;" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void getFindAndOptimisticLockingCheckRec(DbOrClassTableInfo ti, String entityNameCp) {

    sb.append(T1 + "public " + entityNameCp + " findAndOptimisticLockingCheck(" + entityNameCp
        + "BaseRecord rec) throws AppException {" + RT);
    sb.append(T2 + "return findAndOptimisticLockingCheck(rec.get"
        + code.capitalCamel(ti.getPkColumn().getName()) + "OfEntityDataType(), "
        + "rec.getVersionOfEntityDataType());" + RT);
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
    relFieldList.stream().forEach(ci -> relString.append(
        ", " + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getRelationFieldName()));
    sb.append(T1 + "public " + entityName + " insertOrUpdate(" + code.capitalCamel(ti.getName())
        + "BaseRecord rec" + dateTimeString + relString
        + ", String... skipUpdateFields) throws AppException {" + RT);
    sb.append(T2 + entityName + " e = null;" + RT);
    sb.append(T2 + "boolean isInsert = rec.get" + idField + "() == null || rec.get" + idField
        + "().equals(\"\");" + RT2);

    StringBuilder dateTimeString2 = new StringBuilder();
    ti.columnList.stream().filter(ci -> !ci.getIsJavaOnly())
        .filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
            || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP)
        .forEach(ci -> dateTimeString2.append(", " + code.uncapitalCamel(ci.getName())));
    StringBuilder relString2 = new StringBuilder();
    relFieldList.stream().forEach(ci -> relString2.append(", " + ci.getRelationFieldName()));

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
        + "BaseRecord rec, String... targetItemPropertyPaths) throws BizLogicAppException {" + RT);
    sb.append(T2 + "internalDuplicateCheck(isCheckFromAllGroups, entityList, rec, \""
        + ti.getNameCamel() + "\", \"id\", " + "targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheck(List<" + ti.getNameCpCamel() + "> entityList, "
        + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) throws BizLogicAppException {" + RT);
    sb.append(T2 + "duplicateCheck(false, entityList, rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheck(" + ti.getNameCpCamel() + "BaseRecord rec, "
        + "String... targetItemPropertyPaths) throws BizLogicAppException {" + RT);
    sb.append(T2 + "duplicateCheck(repo.findAll(), rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheckFromAllGroups(List<" + ti.getNameCpCamel()
        + "> entityList, " + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) throws BizLogicAppException {" + RT);
    sb.append(T2 + "duplicateCheck(true, entityList, rec, targetItemPropertyPaths);" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void duplicateCheckFromAllGroups(" + ti.getNameCpCamel()
        + "BaseRecord rec, String... targetItemPropertyPaths) throws BizLogicAppException {" + RT);
    sb.append(T2
        + "duplicateCheckFromAllGroups(repo.findAllFromAllGroups(), rec, targetItemPropertyPaths);"
        + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateNaturalKeyDuplicateCheck(DbOrClassTableInfo ti) {
    if (!ti.hasUniqueConstraint()) {
      return;
    }

    String entityName = code.capitalCamel(ti.getName());
    List<String> itemPropertyPathList = ti.columnList.stream().filter(ci -> ci.isUniqueConstraint())
        .map(ci -> code.generateString(ci, ColFormat.ITEM_PROPERTY_PATH)).toList();

    // // args: rec
    // sb.append(T1 + "public void naturalKeyDuplicateCheck(" + entityName
    // + "BaseRecord rec) throws BizLogicAppException {" + RT);
    // sb.append(T2 + "naturalKeyDuplicateCheck(rec, "
    // + StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ", "\"", false) + ");" + RT);
    // sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void naturalKeyDuplicateCheck(" + entityName
        + "BaseRecord rec) throws BizLogicAppException {" + RT);
    final String itemNameKeysStr =
        "new String[] {"
            + StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ", "rec.getItem(\"",
                "\").getItemNameKey(\"" + StringUtils.uncapitalize(entityName) + "\")", false)
            + "}";

    sb.append(T2 + "Optional<" + entityName + "> optional = repo.findBy"
        + StringUtils.capitalize(code.naturalKeyUncapitalCamelAndRelConsidered(ti)) + "("
        + code.generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
            ColListFormat.REC_GET_OF_ENTITY_DATA_TYPE)
        + ");" + RT);

    String pkCapFieldName = code.capitalCamel(ti.getPkColumn().getName());
    sb.append(T2 + "throwExceptionWhenDuplicated(optional.isPresent() && !optional.get().get"
        + pkCapFieldName + "().equals(rec.get" + pkCapFieldName
        + "OfEntityDataType()), false, new String[] {"
        + StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ", "\"", false) + "}, "
        + itemNameKeysStr + ");" + RT);
    sb.append(T1 + "}" + RT);
  }
}
