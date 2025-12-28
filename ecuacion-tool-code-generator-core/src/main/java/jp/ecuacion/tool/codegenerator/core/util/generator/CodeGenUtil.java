package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.util.regex.Pattern;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import org.apache.commons.lang3.StringUtils;

/**
 * Offers utility methods for code generation.
 */
public class CodeGenUtil {

  private Info info;

  public CodeGenUtil() {
    this.info = MainController.tlInfo.get();
  }

  /** Account.mailAddress の形式の文字列を生成. */
  public String classDotField(String tableName, DbOrClassColumnInfo columnInfo) {
    return StringUtil.getUpperCamelFromSnake(tableName) + "."
        + StringUtil.getLowerCamelFromSnake(columnInfo.getColumnName());
  }

  public String softDeleteColCaptalCamel() {
    return StringUtil.getUpperCamelFromSnake(info.removedDataRootInfo.getColumnName());
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

  /**
   * Returns true when the argument string is snake format.
   * 
   * <p>This method supports 4 patterns. </p>
   * <ol>
   * <li>snake with uppercase chars(ex. "ACC_GROUP")</li>
   * <li>snake with lowercase chars(ex. "acc_group")</li>
   * <li>capitalized camel(ex. "AccGroup")</li>
   * <li>uncapitalized camel(ex. "accGroup")</li>
   * </ol>
   *
   * @throws BizLogicAppException BizLogicAppException
   */
  private boolean isSnake(String camelOrSnakeString) throws BizLogicAppException {
    if (camelOrSnakeString.contains("_")) {
      // It's snake if the string contains "_"
      // Throw an exception when a snake case string contains uppercase and lowercase characters.
      boolean isAllUc = Pattern.compile("[A-Z0-9_].*").matcher(camelOrSnakeString).find();
      boolean isAllLc = Pattern.compile("[a-z0-9_].*").matcher(camelOrSnakeString).find();
      if (!isAllUc && !isAllLc) {
        throw new BizLogicAppException("MSG_ERR_STRING_NEITHER_CAMEL_NOR_SNAKE",
            camelOrSnakeString);
      }

      return true;

    } else if (Pattern.compile("[A-Z0-9].*").matcher(camelOrSnakeString).find()) {
      // It's snake if the string doesnt contain lowercase string.
      // Technically we cannot tell whether "A" or "A1" are capitalized camel or uppercase snake,
      // but it doesn't seem to cause any problems if we consider these as snake.
      return true;

    } else if (Pattern.compile("[a-z0-9].*").matcher(camelOrSnakeString).find()) {
      // We cannot tell whether "book" is uncapitalized camel or lowercase snake.
      // It also seems to be okay to consider them as snake.
      return true;
    }

    return false;
  }

  // private String uncapitalizedCamel(String camelOrSnakeString) throws BizLogicAppException {
  // if (isSnake(camelOrSnakeString)) {
  // return StringUtil.getLowerCamelFromSnake(camelOrSnakeString);
  //
  // } else {
  // return StringUtils.uncapitalize(camelOrSnakeString);
  // }
  // }

  private String capitalizedCamel(String camelOrSnakeString) throws BizLogicAppException {
    if (isSnake(camelOrSnakeString)) {
      return StringUtil.getUpperCamelFromSnake(camelOrSnakeString);

    } else {
      return StringUtils.capitalize(camelOrSnakeString);
    }
  }

  public String set(String fieldOrColumnName, String argString) throws BizLogicAppException {
    return "set" + capitalizedCamel(fieldOrColumnName) + "(" + argString + ")";
  }

  public String baseRec(String entityOrTableName) throws BizLogicAppException {
    return capitalizedCamel(entityOrTableName) + "BaseRecord";
  }

  public String baseRecDef(String entityOrTableName) throws BizLogicAppException {
    String uc = capitalizedCamel(entityOrTableName);
    return uc + "BaseRecord rec";
  }

  public String recGet(String fieldOrColumnName) throws BizLogicAppException {
    return "rec.get" + capitalizedCamel(fieldOrColumnName) + "()";
  }

  public String recGetNotNull(String fieldOrColumnName) throws BizLogicAppException {
    return recGet(fieldOrColumnName) + " != null";
  }

  public String recGetIfNotNull(String fieldOrColumnName) throws BizLogicAppException {
    return "if (" + recGet(fieldOrColumnName) + " != null) ";
  }
}
