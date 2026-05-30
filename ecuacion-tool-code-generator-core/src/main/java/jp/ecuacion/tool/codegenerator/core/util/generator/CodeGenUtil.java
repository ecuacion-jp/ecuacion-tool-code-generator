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
package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
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
      List.of(DataTypeKataEnum.BYTE, DataTypeKataEnum.SHORT,
          DataTypeKataEnum.INTEGER, DataTypeKataEnum.LONG);

  public static final List<DataTypeKataEnum> dateTimeDataTypeList =
      List.of(DataTypeKataEnum.TIMESTAMP, DataTypeKataEnum.DATE,
          DataTypeKataEnum.TIME, DataTypeKataEnum.DATE_TIME);

  public static final List<DataTypeKataEnum> ofEntityTypeMethodAvailableDataTypeList =
      ListUtils.union(ListUtils.union(numberDataTypeList, dateTimeDataTypeList),
          Arrays.asList(new DataTypeKataEnum[] {DataTypeKataEnum.ENUM}));

  private Info getInfo() {
    return MainController.tlInfo.get();
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
   */
  private boolean isSnake(String camelOrSnakeString) {
    if (camelOrSnakeString.contains("_")) {
      // It's snake if the string contains "_"
      // Throw an exception when a snake case string contains uppercase and lowercase characters.
      boolean isAllUc = Pattern.compile("[A-Z0-9_].*").matcher(camelOrSnakeString).find();
      boolean isAllLc = Pattern.compile("[a-z0-9_].*").matcher(camelOrSnakeString).find();
      if (!isAllUc && !isAllLc) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_STRING_NEITHER_CAMEL_NOR_SNAKE", camelOrSnakeString)).throwIfAny();
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

  /**
   * Converts the string to lower-camel case, treating it as snake_case when it contains
   * underscores.
   */
  public String uncapitalCamel(String camelOrSnakeString) {
    if (isSnake(camelOrSnakeString)) {
      return StringUtil.getLowerCamelFromSnake(camelOrSnakeString);

    } else {
      return StringUtils.uncapitalize(camelOrSnakeString);
    }
  }

  /**
   * Converts the string to upper-camel case, treating it as snake_case when it contains
   * underscores.
   */
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
  public String getJavaKata(DbOrClassColumnInfo ci) {
    String rtn = null;
    DataTypeInfo dtInfo = ci.getDtInfo();

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
                + StringUtil.getUpperCamelFromSnake(kata.toString()));
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
    return generateString(ci, formatType, "");
  }

  /**
   * Generatos column related string like getter.
   * 
   * <p>The feature of this method is for relation to be considered.</p>
   */
  public String generateString(DbOrClassColumnInfo ci, ColFormat formatType, String argString) {
    StringBuilder sb = new StringBuilder();
    boolean is1st = true;

    DbOrClassColumnInfo currentCi = ci;
    while (true) {
      if (is1st) {
        is1st = false;

      } else {
        switch (formatType) {
          case ITEM_PROPERTY_PATH, SET, GET, GET_OF_ENTITY_DATA_TYPE -> sb.append(".");
          case QUERY_METHOD -> sb.append("_");
          default -> throw new RuntimeException("Unexpected.");
        }
      }

      switch (formatType) {
        case SET, GET, GET_OF_ENTITY_DATA_TYPE -> {
          if (currentCi.isRelation()) {
            sb.append("get" + currentCi.getRelationFieldNameCp() + "()");

          } else {
            String postfix = formatType == ColFormat.GET_OF_ENTITY_DATA_TYPE
                && ofEntityTypeMethodAvailableDataTypeList.contains(ci.getDtInfo().getKata())
                    ? "OfEntityDataType"
                    : "";
            sb.append((formatType == ColFormat.SET ? "set" : "get")
                + capitalCamel(currentCi.getName()) + postfix + "(" + argString + ")");
          }
        }
        case ITEM_PROPERTY_PATH -> sb.append(
            currentCi.isRelation() ? currentCi.getRelationFieldName() : currentCi.getNameCamel());
        case QUERY_METHOD -> sb.append(currentCi.isRelation() ? currentCi.getRelationFieldNameCp()
            : currentCi.getNameCpCamel());
        default -> throw new RuntimeException("Unexpected.");
      }

      if (currentCi.isRelation()) {
        currentCi = getInfo().getTableInfo(currentCi.getRelationRefTable())
            .getColumn(currentCi.getRelationRefCol());

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
      String colNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
      String colNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
      String colNameUcRelUnderscore = !ci.isRelation() ? colNameUc
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
        case ENTITY_DEFINE -> sb
            .append((getJavaKata(ci)) + " " + StringUtil.getLowerCamelFromSnake(ci.getName()));
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
        default -> throw new RuntimeException("ColListFormat not designated.");
      }
    }

    return sb.toString();
  }

  /**
   * Format specifier for generating a single column expression (getter, setter, property path, or
   * query method name).
   */
  public static enum ColFormat {
    ITEM_PROPERTY_PATH, SET, GET, GET_OF_ENTITY_DATA_TYPE, QUERY_METHOD
  }

  /**
   * Format specifier for generating a multi-column expression that joins natural-key columns in
   * various styles.
   */
  public static enum ColListFormat {
    /** JPQL WHERE clause fragment: {@code my_col = :myCol and ...}. */
    JPQL(BetweenColumns.PADDED_AND),

    /** Native SQL parameter binding fragment: {@code my_col = :#{#entity.myCol} and ...}. */
    SQL_PARAM(BetweenColumns.PADDED_AND),

    /** Lower-camel concatenation of column names: {@code myArg1AndMyArg2}. */
    UNCAPITAL_CAMEL_AND(BetweenColumns.AND),

    /**
      * Lower-camel concatenation that appends the referenced column when the natural key includes a
      * relation,
     * e.g. {@code accIdAndAppId}.
     */
    UNCAPITAL_CAMEL_AND_REL_CONSIDERED(BetweenColumns.AND),

    /** Comma-separated getter calls on an entity: {@code e.getMyArg1(), e.getMyArg2()}. */
    ENTITY_GET(BetweenColumns.PADDED_COMMA),

    /** Comma-separated field declarations: {@code String myArg1, Integer myArg2}. */
    ENTITY_DEFINE(BetweenColumns.PADDED_COMMA),

    /** Comma-separated {@code rec.getXxx(OfEntityDataType)} calls for building record arguments. */
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

  /**
   * Returns a comma-separated parameter declaration string for the natural-key columns of the given
   * table.
   */
  public String naturalKeyDefine(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.ENTITY_DEFINE);
  }

  /*
   * repository
   */

  /**
   * Returns the native SQL parameter binding fragment for the natural-key columns of the given
   * table.
   */
  public String naturalKeySqlParams(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.SQL_PARAM);
  }

  /** Returns the lower-camel-concatenated natural-key name string for the given table. */
  public String naturalKeyUncapitalCamelAnd(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.UNCAPITAL_CAMEL_AND);
  }

  /**
   * Returns the lower-camel-concatenated natural-key name string, expanding relation columns to
   * include the referenced column name.
   */
  public String naturalKeyUncapitalCamelAndRelConsidered(DbOrClassTableInfo ti) {
    return generateString(ti.columnList.stream().filter(ci -> ci.isUniqueConstraint()).toList(),
        ColListFormat.UNCAPITAL_CAMEL_AND_REL_CONSIDERED);
  }

  /*
   * var
   */

  /** Returns a null-check expression string: {@code <formattedString> != null}. */
  public String varIsNotNull(String formattedString) {
    return formattedString + " != null";
  }

  /**
   * Returns an {@code if}-statement string guarding on non-null: {@code if (<formattedString> !=
   * null)}.
   */
  public String ifVarIsNotNull(String formattedString) {
    return "if (" + formattedString + " != null) ";
  }

  /** Returns a setter invocation string: {@code setXxx(<argString>)}. */
  public String set(String fieldOrColumnName, String argString) {
    return "set" + capitalCamel(fieldOrColumnName) + "(" + argString + ")";
  }

  /**
   * Returns the base-record class name for the given entity or table name, e.g. {@code
   * MyTableBaseRecord}.
   */
  public String baseRec(String entityOrTableName) {
    return capitalCamel(entityOrTableName) + "BaseRecord";
  }

  /**
   * Returns a local variable declaration string for the base record, e.g. {@code MyTableBaseRecord
   * rec}.
   */
  public String baseRecDef(String entityOrTableName) {
    String uc = capitalCamel(entityOrTableName);
    return uc + "BaseRecord rec";
  }

  /*
   * recGet
   */

  /** Returns a getter call on the record variable: {@code rec.getXxx()}. */
  public String recGet(String fieldOrColumnName) {
    return "rec.get" + capitalCamel(fieldOrColumnName) + "()";
  }

  /** Returns a null-check expression for a record field: {@code rec.getXxx() == null}. */
  public String recGetIsNull(String fieldOrColumnName) {
    return recGet(fieldOrColumnName) + " == null";
  }

  /** Returns a non-null check expression for a record field: {@code rec.getXxx() != null}. */
  public String recGetIsNotNull(String fieldOrColumnName) {
    return varIsNotNull(recGet(fieldOrColumnName));
  }

  /** Returns an {@code if}-statement string guarding on a non-null record field. */
  public String ifRecGetIsNotNull(String fieldOrColumnName) {
    return "if (" + recGet(fieldOrColumnName) + " != null) ";
  }

  /** Returns a {@code ClassName.fieldName} expression, e.g. {@code Account.mailAddress}. */
  public String classDotField(String tableName, DbOrClassColumnInfo columnInfo) {
    return StringUtil.getUpperCamelFromSnake(tableName) + "."
        + StringUtil.getLowerCamelFromSnake(columnInfo.getName());
  }

  /** Returns the soft-delete column name in upper-camel case. */
  public String softDeleteColCaptalCamel() {
    return StringUtil.getUpperCamelFromSnake(getInfo().getRemovedDataRootInfo().getColumnName());
  }

  /** Returns the soft-delete column name in upper-snake case. */
  public String softDeleteColUpperSnake() {
    return getInfo().getRemovedDataRootInfo().getColumnName().toUpperCase();
  }

  /** Returns the soft-delete column name in lower-snake case. */
  public String softDeleteColLowerSnake() {
    return getInfo().getRemovedDataRootInfo().getColumnName().toLowerCase();
  }
}
