package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.record;

import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;


/**
 * @author 庸介
 *
 */
public class SystemCommonBaseRecordGen extends BaseRecordGen {

  public SystemCommonBaseRecordGen() {
    super(DataKindEnum.DB_COMMON);
  }

  @Override
  public void generate() throws AppException {

    DbOrClassTableInfo tableInfo = info.getCommonTableInfo();
    if (tableInfo == null) {
      return;
    }
    
    final String tableNameCp =
        StringUtil.getUpperCamelFromSnake(tableInfo.getName());
    sb = new StringBuilder();

    createHeader(tableInfo);
    createConstA(tableInfo);
    createConstB(tableInfo);
    createConstC(tableInfo);
    createAccessor(tableInfo, tableNameCp);
    // createLengthGetter(tableInfo, tableNameCp);
    createListsForHtmlSelect(tableInfo);

    sb.append("}" + RT);

    outputFile(sb, getFilePath("record"),
        "SystemCommonBaseRecord.java");
  }

  /**
   */
  public void createHeader(DbOrClassTableInfo tableInfo) {
    sb.append("package " + rootBasePackage + ".base.record;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();

    createHeaderCommon(importMgr, tableInfo);

    importMgr.add("jp.ecuacion.splib.core.record.SplibRecord");
    importMgr.add(rootBasePackage + ".base.entity.SystemCommonEntity");
    importMgr.add("jp.ecuacion.splib.core.container.*");

    sb.append(importMgr.outputStr() + RT);

    genJavadocClass(new String[] {"AbstractDTO", "@author Yosuke Tanaka"});
    sb.append("public abstract class SystemCommonBaseRecord extends SplibRecord {" + RT2);
    // sb.append(T1 +"protected HttpServletRequestUtil ru;" + RT2);

    for (DbOrClassColumnInfo columnInfo : tableInfo.columnList) {
      fieldDefinition(tableInfo.getName(), columnInfo);
    }

    sb.append(RT);
  }

  public void createConstA(DbOrClassTableInfo tableInfo) {
    sb.append(T1 + JD_ST + RT);
    sb.append(T1 + " * デフォルトコンストラクタ。" + RT);
    sb.append(T1 + " * 共通項目の値の初期化を実施" + RT);
    sb.append(T1 + JD_END + RT);
    sb.append(T1 + "public SystemCommonBaseRecord() {" + RT);
    sb.append(T2 + "super();" + RT);
    insideConstA(tableInfo);
    sb.append(T1 + "}" + RT2);
  }

  public void createConstB(DbOrClassTableInfo tableInfo) throws AppException {
    sb.append(T1 + JD_ST + RT);
    sb.append(T1 + " * エンティティを引数にした場合のデフォルトコンストラクタ。" + RT);
    sb.append(T1 + " * 共通項目の値の代入を実施" + RT);
    sb.append(T1 + JD_END + RT);
    sb.append(T1
        + "public SystemCommonBaseRecord(SystemCommonEntity e, DatetimeFormatParameters params) {"
        + RT);
    sb.append(T2 + "super(params);" + RT);
    insideConstB(tableInfo, false);
    sb.append(T1 + "}" + RT2);
  }

  public void createConstC(DbOrClassTableInfo tableInfo) throws AppException {
    sb.append(T1 + JD_ST + RT);
    sb.append(T1 + " * clone目的で使用するconstructor。" + RT);
    sb.append(T1
        + " * JSFで一覧からデータ選択した際、選択した行のrecが、recListの中の要素をそのまま渡されるので、cloneしないとrecList側が書き換わってしまうので使用。"
        + RT);
    sb.append(T1 + " * abstractクラスにはclone()を実装できないので、代わりにコンストラクタでの実装とした。" + RT);
    sb.append(T1 + JD_END + RT);
    sb.append(T1 + "public SystemCommonBaseRecord(SystemCommonBaseRecord rec) {" + RT);
    sb.append(T2 + "super(rec.getDateTimeFormatParams());" + RT);
    insideConstC(tableInfo);
    sb.append(T1 + "}" + RT2);
  }
}
