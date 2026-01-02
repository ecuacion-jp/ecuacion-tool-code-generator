package jp.ecuacion.tool.codegenerator.core.util.generator;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Offers utility methods for code generation.
 */
public class CodeGenUtil {

  public static final List<DataTypeKataEnum> numberDataTypeList =
      Arrays.asList(new DataTypeKataEnum[] {DataTypeKataEnum.BYTE, DataTypeKataEnum.SHORT,
          DataTypeKataEnum.INTEGER, DataTypeKataEnum.LONG});

  public static final List<DataTypeKataEnum> dateTimeDataTypeList =
      Arrays.asList(new DataTypeKataEnum[] {DataTypeKataEnum.TIMESTAMP, DataTypeKataEnum.DATE,
          DataTypeKataEnum.TIME, DataTypeKataEnum.DATE_TIME});

  public static final List<DataTypeKataEnum> ofEntityTypeMethodAvailableDataTypeList =
      ListUtils.union(ListUtils.union(numberDataTypeList, dateTimeDataTypeList),
          Arrays.asList(new DataTypeKataEnum[] {DataTypeKataEnum.ENUM}));

  private Info info = MainController.tlInfo.get();

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
  private boolean isSnake(String camelOrSnakeString) {
    if (camelOrSnakeString.contains("_")) {
      // It's snake if the string contains "_"
      // Throw an exception when a snake case string contains uppercase and lowercase characters.
      boolean isAllUc = Pattern.compile("[A-Z0-9_].*").matcher(camelOrSnakeString).find();
      boolean isAllLc = Pattern.compile("[a-z0-9_].*").matcher(camelOrSnakeString).find();
      if (!isAllUc && !isAllLc) {
        throw new UncheckedAppException(
            new BizLogicAppException("MSG_ERR_STRING_NEITHER_CAMEL_NOR_SNAKE", camelOrSnakeString));
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

  /*
   * cases
   */

  public String uncapitalCamel(String camelOrSnakeString) {
    if (isSnake(camelOrSnakeString)) {
      return StringUtil.getLowerCamelFromSnake(camelOrSnakeString);

    } else {
      return StringUtils.uncapitalize(camelOrSnakeString);
    }
  }

  public String capitalCamel(String camelOrSnakeString) {
    if (isSnake(camelOrSnakeString)) {
      return StringUtil.getUpperCamelFromSnake(camelOrSnakeString);

    } else {
      return StringUtils.capitalize(camelOrSnakeString);
    }
  }

  /**
   * Changes data type name format: "DT_XXX" to capitalized camel case.
   */
  public static String dataTypeNameToUppperCamel(String str) {
    return StringUtil.getUpperCamelFromSnake(str.substring(3));
  }

  public String getFromEntityToRec(DbOrClassColumnInfo ci) {
    if (ci.getDtInfo().getKata() == DataTypeKataEnum.ENUM) {
      return "get" + capitalCamel(ci.getName()) + "()" + ".getCode()";

    } else if (numberDataTypeList.contains(ci.getDtInfo().getKata())) {
      String getter = "get" + capitalCamel(ci.getName()) + "()";
      return getter + " == null ? null : " + getter + ".toString()";

    } else {
      return "get" + capitalCamel(ci.getName()) + "()";
    }
  }

  /**
   * Record: ofEntityDataType
   */
  public String getOfEntityDataType(DbOrClassColumnInfo ci) {
    return createStringFromColumnInfo(ci, FormatType.GET_CONNECTED_OF_ENTITY_DATA_TYPE);
  }

  public String createStringFromColumnInfo(DbOrClassColumnInfo ci, FormatType formatType) {
    StringBuilder sb = new StringBuilder();
    boolean is1st = true;

    DbOrClassColumnInfo currentCi = ci;
    while (true) {
      if (is1st) {
        is1st = false;

      } else {
        sb.append(".");
      }

      if (formatType == FormatType.GET_CONNECTED
          || formatType == FormatType.GET_CONNECTED_OF_ENTITY_DATA_TYPE) {
        if (currentCi.isRelationColumn()) {
          sb.append("get" + capitalCamel(currentCi.getRelationFieldName()) + "()");

        } else {
          String postfix =
              ofEntityTypeMethodAvailableDataTypeList.contains(ci.getDtInfo().getKata())
                  ? "OfEntityDataType"
                  : "";
          sb.append("get" + capitalCamel(currentCi.getName()) + postfix + "()");
        }
      }

      if (currentCi.isRelationColumn()) {
        String tab = currentCi.getRelationRefTable();
        String col = currentCi.getRelationRefCol();
        currentCi = info.dbRootInfo.tableList.stream().filter(e -> e.getName().equals(tab)).toList()
            .get(0).columnList.stream().filter(e -> e.getName().equals(col)).toList().get(0);

      } else {
        break;
      }
    }

    return sb.toString();
  }

  private static enum FormatType {
    GET_CONNECTED, GET_CONNECTED_OF_ENTITY_DATA_TYPE
  }


  /*
   * var
   */

  public String varIsNotNull(String formattedString) throws BizLogicAppException {
    return formattedString + " != null";
  }

  public String ifVarIsNotNull(String formattedString) throws BizLogicAppException {
    return "if (" + formattedString + " != null) ";
  }

  public String set(String fieldOrColumnName, String argString) throws BizLogicAppException {
    return "set" + capitalCamel(fieldOrColumnName) + "(" + argString + ")";
  }

  public String baseRec(String entityOrTableName) throws BizLogicAppException {
    return capitalCamel(entityOrTableName) + "BaseRecord";
  }

  public String baseRecDef(String entityOrTableName) throws BizLogicAppException {
    String uc = capitalCamel(entityOrTableName);
    return uc + "BaseRecord rec";
  }

  /*
   * recGet
   */

  public String recGet(String fieldOrColumnName) throws BizLogicAppException {
    return "rec.get" + capitalCamel(fieldOrColumnName) + "()";
  }

  public String recGetIsNull(String fieldOrColumnName) throws BizLogicAppException {
    return recGet(fieldOrColumnName) + " == null";
  }

  public String recGetIsNotNull(String fieldOrColumnName) throws BizLogicAppException {
    return varIsNotNull(recGet(fieldOrColumnName));
  }

  public String ifRecGetIsNotNull(String fieldOrColumnName) throws BizLogicAppException {
    return "if (" + recGet(fieldOrColumnName) + " != null) ";
  }

  /** Account.mailAddress の形式の文字列を生成. */
  public String classDotField(String tableName, DbOrClassColumnInfo columnInfo) {
    return StringUtil.getUpperCamelFromSnake(tableName) + "."
        + StringUtil.getLowerCamelFromSnake(columnInfo.getName());
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
}
