package jp.ecuacion.tool.codegenerator.core.util.generator;

import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.CodeGeneratorAction;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public class StringGenUtil {

  private Info info;

  public StringGenUtil() {
    this.info = CodeGeneratorAction.tlInfo.get();
  }

  /** Account.mailAddress の形式の文字列を生成. */
  public String classDotField(String tableName, DbOrClassColumnInfo columnInfo) {
    return StringUtil.getUpperCamelFromSnake(tableName) + "."
        + StringUtil.getLowerCamelFromSnake(columnInfo.getColumnName());
  }

  public String softDeleteColCaptalCamel() {
    return StringUtil
        .getUpperCamelFromSnake(info.removedDataRootInfo.getColumnName());
  }

  public String softDeleteColUpperSnake() {
    return info.removedDataRootInfo.getColumnName().toUpperCase();
  }

  public String softDeleteColLowerSnake() {
    return info.removedDataRootInfo.getColumnName().toLowerCase();
  }

  /**
   * changeForDataTypeについて すべて"DT_"で始まるが、そこは不要なので、ひとまずはずし、あとはchangiInitCaptalと同じ。
   */
  public static String dataTypeNameToUppperCamel(String str) {
    return StringUtil.getUpperCamelFromSnake(str.substring(3));
  }
}
