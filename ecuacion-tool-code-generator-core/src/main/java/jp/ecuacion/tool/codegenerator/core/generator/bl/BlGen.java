package jp.ecuacion.tool.codegenerator.core.generator.bl;

import java.io.IOException;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil.ColFormat;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public class BlGen extends AbstractGen {

  private CodeGenUtil code = new CodeGenUtil();

  public BlGen() {
    super(null);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    for (DbOrClassTableInfo ti : info.dbRootInfo.tableList) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(ti.getName());
      sb = new StringBuilder();

      final List<DbOrClassColumnInfo> relFieldList =
          ti.columnList.stream().filter(e -> !e.getIsJavaOnly()).filter(e -> !e.isPk())
              .filter(e -> e.isRelationColumn()).toList();

      generateHeader(ti, entityNameCp, relFieldList);
      generateFields(ti, entityNameCp);
      generateInsertOrUpdate(ti, relFieldList);
      generateNaturalKeyDuplicatedCheck(ti);

      sb.append("}" + RT);

      outputFile(sb, getFilePath("bl"), entityNameCp + "BaseBl.java");
    }
  }

  public void generateHeader(DbOrClassTableInfo tableInfo, String entityNameCp,
      List<DbOrClassColumnInfo> relFieldList) throws AppException {
    sb.append("package " + rootBasePackage + ".base.bl;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();

    importMgr.add(rootBasePackage + ".base.entity.*", rootBasePackage + ".base.record.*",
        rootBasePackage + ".base.repository.*",
        "org.springframework.beans.factory.annotation.Autowired");

    if (tableInfo.hasUniqueConstraint()) {
      importMgr.add("jp.ecuacion.lib.core.exception.checked.BizLogicAppException",
          "java.util.Optional", "java.util.List", "jp.ecuacion.lib.core.util.StringUtil",
          "java.util.Arrays", "jp.ecuacion.lib.core.util.PropertyFileUtil.Arg");
    }

    relFieldList.stream().forEach(ci -> importMgr
        .add(rootBasePackage + ".base.entity." + code.capitalCamel(ci.getRelationRefTable())));

    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class " + entityNameCp + "BaseBl {" + RT2);
  }

  private void generateFields(DbOrClassTableInfo ti, String entityNameCp) {
    sb.append(T1 + "@Autowired" + RT);
    sb.append(T1 + "private " + entityNameCp + "BaseRepository repo;" + RT2);
  }

  private void generateInsertOrUpdate(DbOrClassTableInfo ti,
      List<DbOrClassColumnInfo> relFieldList) {

    final String idField = code.capitalCamel(ti.getPkColumn().getName());

    sb.append(T1 + "/** Is a utility to insert or update an entity. */" + RT);
    StringBuilder relString = new StringBuilder();
    relFieldList.stream().forEach(ci -> relString.append(
        ", " + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getRelationFieldName()));
    sb.append(T1 + "public void insertOrUpdate(" + code.capitalCamel(ti.getName())
        + "BaseRecord rec, " + code.capitalCamel(ti.getName()) + " entityForUpdate" + relString
        + ", String... skipUpdateFields) {" + RT);
    sb.append(T2 + code.capitalCamel(ti.getName()) + " e = null;" + RT);
    sb.append(T2 + "boolean isInsert = rec.get" + idField + "() == null || rec.get" + idField
        + "().equals(\"\");" + RT2);

    sb.append(T2 + "if (isInsert) {" + RT);
    sb.append(T3 + "e = new " + code.capitalCamel(ti.getName()) + "(rec);" + RT);
    relFieldList.stream()
        .forEach(ci -> sb.append(T3 + "e.set" + code.capitalCamel(ci.getRelationFieldName()) + "("
            + code.uncapitalCamel(ci.getRelationFieldName()) + ");" + RT));
    sb.append(RT);
    sb.append(T2 + "} else {" + RT);
    sb.append(T3 + "e = entityForUpdate;" + RT);
    StringBuilder relString2 = new StringBuilder();
    relFieldList.stream().filter(ci -> !ci.isGroupColumn())
        .forEach(ci -> relString2.append(", " + ci.getRelationFieldName()));
    sb.append(T3 + "e.update(rec" + relString2 + ");" + RT);
    sb.append(T2 + "}" + RT2);
    sb.append(T2 + "repo.save(e);" + RT);
    sb.append(T1 + "}" + RT2);
  }

  private void generateNaturalKeyDuplicatedCheck(DbOrClassTableInfo ti) {
    if (!ti.hasUniqueConstraint()) {
      return;
    }

    String entityName = code.capitalCamel(ti.getName());
    sb.append(T1 + "public void naturalKeyDuplicatedCheck(" + entityName
        + "BaseRecord rec) throws BizLogicAppException {" + RT);

    List<String> itemPropertyPathList = ti.columnList.stream().filter(ci -> ci.isUniqueConstraint())
        .map(ci -> code.generateString(ci, ColFormat.ITEM_PROPERTY_PATH)).toList();
    String itemPropertyPathsStr =
        StringUtil.getSeparatedValuesString(itemPropertyPathList, ", ", "\"", false);

    sb.append(
        T2 + "String[] itemPropertyPaths = new String[] {" + itemPropertyPathsStr + "};" + RT);
    sb.append(T2 + "List<String> itemNameKeyList = Arrays.asList(itemPropertyPaths).stream()"
        + ".map(path -> rec.getItem(path).getItemNameKey()).toList();" + RT2);

    sb.append(T2 + "Optional<" + entityName + "> optional = repo.findBy"
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
    sb.append(T3
        + "String msgId = \"jp.ecuacion.splib.web.jpa.service.SplibEditJpaService.message.\""
        + " + (itemPropertyPaths.length == 1 ? \"duplicated\" : \"combinationDuplicated\");" + RT);
    sb.append(T3 + "String str = StringUtil.getSeparatedValuesString(itemNameKeyList, \"、\", "
        + "\"「${+item_names:\", \"}」\", false);" + RT);

    sb.append(T3 + "throw new BizLogicAppException("
        + "itemPropertyPaths, msgId, new Arg[] {Arg.formattedString(str)});" + RT);
    sb.append(T2 + "}" + RT);
    sb.append(T1 + "}" + RT);
  }

  // String[] itemPropertyPaths = new String[] {"accGeneral.acc.id", "app.id"};
  // List<String> itemNameKeyList = Arrays.asList(itemPropertyPaths).stream().map(path ->
  // rec.getItem(path).getItemNameKey()).toList();
  //
  // Optional<AccApp> optional =
  // repo.findByAccGeneral_AccIdAndApp_Id(rec.getAccGeneral().getAcc().getIdOfEntityDataType(),
  // rec.getApp().getIdOfEntityDataType());
  //
  // if (optional.isPresent()) {
  // String msgId = "jp.ecuacion.splib.web.jpa.service.SplibEditJpaService.message." +
  // (itemPropertyPaths.length == 1 ? "duplicated" : "combinationDuplicated");
  // String str = StringUtil.getSeparatedValuesString(itemNameKeyList, "、", "「${+item_names:", "}」",
  // false);
  // throw new BizLogicAppException(itemPropertyPaths, msgId, new Arg[] {Arg.formattedString(str)});
  // }

}
