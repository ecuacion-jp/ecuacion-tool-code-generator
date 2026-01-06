package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper.GenHelperKata;
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
   * cases / formats
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
  public String dataTypeNameToCapitalCamel(String str) {
    return StringUtil.getUpperCamelFromSnake(str.substring(3));
  }

  /**
   * Returns the datatype when you use the definition of the field in entities.
   */
  public String getEnumConsideredKata(DataTypeInfo dtInfo) {
    String rtn = null;

    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      rtn = dataTypeNameToCapitalCamel(dtInfo.getDataTypeName())
          + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());

    } else if (dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
        || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME) {
      rtn = (dtInfo.getNotNeedsTimezone()) ? "LocalDateTime" : "OffsetDateTime";

    } else if (dtInfo.getKata() == DataTypeKataEnum.DATE) {
      rtn = "LocalDate";

    } else if (dtInfo.getKata() == DataTypeKataEnum.TIME) {
      rtn = "LocalTime";

    } else {
      rtn = StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
    }

    return rtn;
  }

  /*
   * columns related common
   */

  private HashMap<DataTypeKataEnum, GenHelperKata> helperMap =
      new HashMap<DataTypeKataEnum, GenHelperKata>();

  /**
   * Gets Helper.
   */
  public GenHelperKata getHelper(DataTypeKataEnum kata) {
    if (!helperMap.containsKey(kata)) {
      try {
        @SuppressWarnings("unchecked")
        Class<GenHelperKata> cls = (Class<GenHelperKata>) Class
            .forName(Constants.STR_PACKAGE_HOME + ".core.generator.entity.genhelper.GenHelper"
                + StringUtil.getUpperCamelFromSnake(kata.getName()));
        Constructor<GenHelperKata> con = cls.getConstructor();
        GenHelperKata helper = con.newInstance();
        helperMap.put(kata, helper);

      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }

    return helperMap.get(kata);
  }

  /**
   * Generatos column related string like getter.
   * 
   * <p>The feature of this method is for relation to be considered.</p>
   */
  public String generateString(DbOrClassColumnInfo ci, ColFormat formatType) {
    StringBuilder sb = new StringBuilder();
    boolean is1st = true;

    DbOrClassColumnInfo currentCi = ci;
    while (true) {
      if (is1st) {
        is1st = false;

      } else {
        switch (formatType) {
          case ITEM_PROPERTY_PATH, GET, GET_OF_ENTITY_DATA_TYPE -> sb.append(".");
          default -> throw new EclibRuntimeException("Unexpected.");
        }
      }

      if (formatType == ColFormat.GET || formatType == ColFormat.GET_OF_ENTITY_DATA_TYPE) {
        if (currentCi.isRelationColumn()) {
          sb.append("get" + capitalCamel(currentCi.getRelationFieldName()) + "()");

        } else {
          String postfix = formatType == ColFormat.GET_OF_ENTITY_DATA_TYPE
              && ofEntityTypeMethodAvailableDataTypeList.contains(ci.getDtInfo().getKata())
                  ? "OfEntityDataType"
                  : "";
          sb.append("get" + capitalCamel(currentCi.getName()) + postfix + "()");
        }

      } else if (formatType == ColFormat.ITEM_PROPERTY_PATH) {
        sb.append(currentCi.isRelationColumn() ? uncapitalCamel(currentCi.getRelationFieldName())
            : uncapitalCamel(currentCi.getName()));
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

  /**
   * Generates a multiple columns connected string.
   */
  public String generateString(List<DbOrClassColumnInfo> ciList, ColListFormat formatType) {
    StringBuilder sb = new StringBuilder();

    boolean is1st = true;
    for (DbOrClassColumnInfo ci : ciList) {
      DataTypeInfo dtInfo = ci.getDtInfo();
      String colNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
      String colNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
      String colNameUcRelUnderscore = !ci.isRelationColumn() ? colNameUc
          : StringUtil.getUpperCamelFromSnake(ci.getRelationFieldName()) + "_"
              + StringUtil.getUpperCamelFromSnake(ci.getRelationRefCol());

      if (is1st) {
        is1st = false;

      } else {
        switch (formatType.betweenColumns) {
          case PADDED_COMMA -> sb.append(", ");
          case PADDED_AND -> sb.append(" and ");
          case AND -> sb.append("And");
          default -> sb.append("");
        }
      }

      switch (formatType) {
        case ENTITY_GET -> sb.append("e.get" + capitalCamel(ci.getName()) + "()");
        case ENTITY_DEFINE -> sb.append((getEnumConsideredKata(dtInfo)) + " "
            + StringUtil.getLowerCamelFromSnake(ci.getName()));
        case REC_GET_OF_ENTITY_DATA_TYPE -> sb
            .append("rec." + generateString(ci, ColFormat.GET_OF_ENTITY_DATA_TYPE));
        case JPQL -> sb.append(ci.getName().toLowerCase() + " = :" + colNameLc);
        case SQL_PARAM -> sb.append(ci.getName().toLowerCase() + " = :#{#entity."
            + StringUtil.getLowerCamelFromSnake(ci.getName())
            + (ci.getDtInfo().getKata() == DataTypeKataEnum.ENUM ? ".code" : "") + "}");
        case UNCAPITAL_CAMEL_AND -> sb
            .append((is1st ? StringUtils.uncapitalize(colNameUc) : colNameUc));
        case UNCAPITAL_CAMEL_AND_REL_CONSIDERED -> sb.append(
            (is1st ? StringUtils.uncapitalize(colNameUcRelUnderscore) : colNameUcRelUnderscore));
        default -> throw new EclibRuntimeException("ColListFormat not designated.");
      }
    }

    return sb.toString();
  }

  public static enum ColFormat {
    ITEM_PROPERTY_PATH, GET, GET_OF_ENTITY_DATA_TYPE
  }

  public static enum ColListFormat {
    /** naturalKeyをentityからgetする形の "myArg1MyArg2" という形式 */
    JPQL(BetweenColumns.PADDED_AND),

    /**
     * naturalKeyをsqlの引数にする形の"my_arg_1 = :#{#entity.myArg1} and my_arg_2 = :#{#entity.myArg2}"
     * という形式
     */
    SQL_PARAM(BetweenColumns.PADDED_AND),

    /** naturalKeyをentityからgetする形の "myArg1AndMyArg2" という文字列を保持。table別にmap形式。 */
    UNCAPITAL_CAMEL_AND(BetweenColumns.AND),

    /**
     * naturalKeyをentityからgetする形の "myArg1AndMyArg2" という文字列を保持。table別にmap形式。
     * ただしnaturalKeyがrelationを持つ場合はAcc_IdAndApp_Idのようになる。repositoryでの使用を想定。
     */
    UNCAPITAL_CAMEL_AND_REL_CONSIDERED(BetweenColumns.AND),

    /** naturalKeyをentityからgetする形の "e.getMyArg1(), e.getMyArg2()" という文字列を保持。table別にmap形式。 */
    ENTITY_GET(BetweenColumns.PADDED_COMMA),

    /** naturalKeyを引数にとる時の "String myArg1, Integer myArg2" という文字列を保持。table別にmap形式。 */
    ENTITY_DEFINE(BetweenColumns.PADDED_COMMA),

    REC_GET_OF_ENTITY_DATA_TYPE(BetweenColumns.PADDED_COMMA);

    private BetweenColumns betweenColumns;

    private ColListFormat(BetweenColumns betweenColumns) {
      this.betweenColumns = betweenColumns;
    }
  }

  private static enum BetweenColumns {
    NONE, PADDED_COMMA, AND, PADDED_AND
  }

  /*
   * Record
   */

  /**
   * Generates getter with "OfEntityDataType" if it exists.
   */
  public String getOfEntityDataType(DbOrClassColumnInfo ci) {
    return generateString(ci, ColFormat.GET_OF_ENTITY_DATA_TYPE);
  }

  /*
   * Entity
   */

  /**
   * Obtains a value from entity for substituting into record field.
   */
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

  public String naturalKeyDefine(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.ENTITY_DEFINE);
  }

  /*
   * repository
   */

  public String naturalKeySqlParams(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.SQL_PARAM);
  }

  public String naturalKeyUncapitalCamelAnd(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.UNCAPITAL_CAMEL_AND);
  }

  public String naturalKeyUncapitalCamelAndRelConsidered(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.UNCAPITAL_CAMEL_AND_REL_CONSIDERED);
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
