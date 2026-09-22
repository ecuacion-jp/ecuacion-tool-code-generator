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
package jp.ecuacion.tool.codegenerator.core.dto;

import static jp.ecuacion.lib.validation.constraints.enums.ConditionOperator.NOT_EQUAL_TO;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValueState.EMPTY;
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValueState.NOT_EMPTY;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.EmptyWhen;
import jp.ecuacion.lib.validation.constraints.NotEmptyWhen;
import jp.ecuacion.lib.validation.constraints.PatternWithDescription;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.NotEmptyGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ValidatorGen;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.CrossSheetConsistencyCheckGroup;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
  * Holds the column-level attributes read from the DB or class specification sheet in the Excel
  * file.
 */
@NotEmptyWhen(
    propertyPath = {"relationDirection", "relationFieldName", "relationRefTable", "relationRefCol"},
    conditionPropertyPath = "relationKind", conditionValueState = EMPTY,
    conditionOperator = NOT_EQUAL_TO, emptyWhenConditionNotSatisfied = true)
@EmptyWhen(propertyPath = "relationRefFieldName", conditionPropertyPath = "relationDirection",
    conditionOperator = NOT_EQUAL_TO, conditionValueString = "bidirectional")
@EmptyWhen(propertyPath = "relationIsEager", conditionPropertyPath = "relationKind",
    conditionValueState = EMPTY)
@NotEmptyWhen(propertyPath = "supportedLang1",
    conditionPropertyPath = "sysCmnRootInfo.supportLang1", conditionValueState = NOT_EMPTY,
    emptyWhenConditionNotSatisfied = true, groups = CrossSheetConsistencyCheckGroup.class)
@NotEmptyWhen(propertyPath = "supportedLang2",
    conditionPropertyPath = "sysCmnRootInfo.supportLang2", conditionValueState = NOT_EMPTY,
    emptyWhenConditionNotSatisfied = true, groups = CrossSheetConsistencyCheckGroup.class)
@NotEmptyWhen(propertyPath = "supportedLang3",
    conditionPropertyPath = "sysCmnRootInfo.supportLang3", conditionValueState = NOT_EMPTY,
    emptyWhenConditionNotSatisfied = true, groups = CrossSheetConsistencyCheckGroup.class)
@SuppressWarnings("NullAway.Init")
public class DbOrClassColumnInfo extends StringExcelTableBean implements LangsHolder {

  private List<RelationRefInfo> relationRefInfoList = new ArrayList<>();

  // No longer used for file data loading, so stored directly as boolean
  private boolean isOptLock = false;

  @NotEmpty
  @Size(max = 50)
  @PatternWithDescription(regexp = Constants.REG_EX_UP_NUM_US, description = "upperSnakeCase")
  private String name;

  @NotEmpty
  @Size(min = 1, max = 50)
  private String userFriendlyName;

  // Holds dispName as a Map to support multiple languages. Key is the language (e.g. "ja").
  // LANG_DEF is used as the key for the default language.
  private HashMap<String, String> userFriendlyNameMap = new HashMap<String, String>();

  @NotEmpty
  @Size(max = 50)
  @PatternWithDescription(regexp = Constants.REG_EX_DT_NAME, description = "dataTypeName")
  private String dataType;
  @StrBoolean
  private String isJavaOnly;
  @StrBoolean
  private String isSurrogateKey;
  @StrBoolean
  private String isNaturalKey;
  @StrBoolean
  private String isNullable;
  @StrBoolean
  private String isAutoIncrement;
  @StrBoolean
  private String isForcedIncrement;
  @StrBoolean
  private String isAutoUpdate;
  @StrBoolean
  private String isForcedUpdate;
  @StrBoolean
  private String isCustomGroupColumn;
  @PatternWithDescription(regexp = "^CB|CD|LB|LD$", description = "springAuditing")
  private String springAuditing;

  private String updatedValue;

  @PatternWithDescription(regexp = "^@ManyToOne|@OneToOne$", description = "relationKind")
  private String relationKind;
  private String relationDirection;
  private String relationFieldName;
  private String relationSrcObjVarName;
  private String relationRefTable;
  private String relationRefCol;
  private String relationRefFieldName;
  private String relationIsEager;

  private String index1;
  private String index2;
  private String index3;
  private String index4;
  private String index5;
  private String index6;
  private String index7;
  private String index8;
  private String index9;
  private String index10;

  @Size(min = 1, max = 50)
  private String supportedLang1;
  @Size(min = 1, max = 50)
  private String supportedLang2;
  @Size(min = 1, max = 50)
  private String supportedLang3;

  /** Added for convenience; holds the resolved DataTypeInfo for this column. */
  private DataTypeInfo dtInfo;

  /** Held for {@code @NotEmptyWhen}'s conditionPropertyPath; 
   * not re-validated (not {@code @Valid}). */
  @SuppressWarnings("unused")
  private SystemCommonRootInfo sysCmnRootInfo;

  private ColumnGenUtil code = new ColumnGenUtil();

  //@formatter:off
  @Override
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {
        null, "name", "dataType", null,
        "isJavaOnly", "isSurrogateKey", "isNaturalKey", "isNullable", "isAutoIncrement",
        "isForcedIncrement",
        "isAutoUpdate", "isForcedUpdate", "isCustomGroupColumn", "springAuditing", "relationKind",
        "relationDirection",
        "relationFieldName", "relationSrcObjVarName", "relationRefTable", "relationRefCol",
        "relationRefFieldName", "relationIsEager",
        "index1", "index2", "index3", "index4", "index5", "index6", "index7", "index8", "index9",
        "index10", null, "userFriendlyName", "supportedLang1",
        "supportedLang2", "supportedLang3"
    };
  }
  //@formatter:on

  /** Constructs a column info instance by parsing the given raw column value list. */
  @SuppressWarnings("null")
  public DbOrClassColumnInfo(List<String> colList) {
    super(colList);
  }

  /**
   * Sets the system-common root info, needed both as the condition source for the
   * {@code @NotEmptyWhen} constraints above (validated under {@link
   * CrossSheetConsistencyCheckGroup}) and to build the display-name map.
   *
   * <p>Called from {@code CheckAndComplementDataBlf} once all sheets have been read; this info
   * is intentionally unavailable while this sheet's own data is being parsed.</p>
   */
  @Override
  public void setSysCmnRootInfo(SystemCommonRootInfo sysCmnRootInfo) {
    this.sysCmnRootInfo = sysCmnRootInfo;
  }

  /**
   * Builds the display-name map using the language settings from {@code sysCmnRootInfo}.
   *
   * <p>Must be called after {@link #setSysCmnRootInfo} and after the
   * {@link CrossSheetConsistencyCheckGroup} validation has passed.</p>
   */
  @Override
  public void buildDisplayNameMap() {
    String[] locales =
        new String[] {sysCmnRootInfo.getDefaultLang(), sysCmnRootInfo.getSupportLang1(),
            sysCmnRootInfo.getSupportLang2(), sysCmnRootInfo.getSupportLang3()};
    String[] localNames =
        new String[] {userFriendlyName, supportedLang1, supportedLang2, supportedLang3};

    for (int i = 0; i < locales.length; i++) {
      String locale = locales[i];
      if (!StringUtils.isEmpty(locale)) {
        userFriendlyNameMap.put(locale, localNames[i]);
      }
    }
  }

  /** Creates a shallow copy of the given column info with all relation-related fields cleared. */
  @SuppressWarnings("null")
  public static DbOrClassColumnInfo cloneWithoutRelationRelated(DbOrClassColumnInfo ci) {
    String[] arr = new String[] {null, ci.getName(), ci.getDataType(), null,
        ci.getIsJavaOnlyString(), ReaderUtil.booleanToBoolStr(ci.isPk()),
        ReaderUtil.booleanToBoolStr(ci.isUniqueConstraint()), ci.isNullable,
        ReaderUtil.booleanToBoolStr(ci.isAutoIncrement()),
        ReaderUtil.booleanToBoolStr(ci.isForcedIncrement()),
        ReaderUtil.booleanToBoolStr(ci.isAutoUpdate()),
        ReaderUtil.booleanToBoolStr(ci.isForcedUpdate()),
        ReaderUtil.booleanToBoolStr(ci.isCustomGroupColumn()), ci.getSpringAuditing(), "", "", "",
        "", "", "", "", "", ci.getIndex1() == null ? null : ci.getIndex1().toString(),
        ci.getIndex2() == null ? null : ci.getIndex2().toString(),
        ci.getIndex3() == null ? null : ci.getIndex3().toString(),
        ci.getIndex4() == null ? null : ci.getIndex4().toString(),
        ci.getIndex5() == null ? null : ci.getIndex5().toString(),
        ci.getIndex6() == null ? null : ci.getIndex6().toString(),
        ci.getIndex7() == null ? null : ci.getIndex7().toString(),
        ci.getIndex8() == null ? null : ci.getIndex8().toString(),
        ci.getIndex9() == null ? null : ci.getIndex9().toString(),
        ci.getIndex10() == null ? null : ci.getIndex10().toString(), null, ci.getDisplayName(),
        ci.getSupportedLang1(), ci.getSupportedLang2(), ci.getSupportedLang3()};

    DbOrClassColumnInfo rtnCi = new DbOrClassColumnInfo(Arrays.asList(arr));

    rtnCi.setDtInfo(ci.getDtInfo());
    return rtnCi;
  }

  public List<RelationRefInfo> getRelationRefInfoList() {
    return relationRefInfoList;
  }

  public List<RelationRefInfo> getBidirectionalRelationRefInfoList() {
    return relationRefInfoList.stream().filter(info -> info.isBidirectional).toList();
  }

  /**
   * Returns {@code true} if this column is the target of at least one bidirectional relation
   * reference.
   */
  public boolean hasBidirectionalRelationRef() {
    return getBidirectionalRelationRefInfoList() != null
        && getBidirectionalRelationRefInfoList().size() != 0;
  }

  // name
  public String getName() {
    return name;
  }

  public String getNameCpCamel() {
    return StringUtil.getUpperCamelFromSnake(name);
  }

  public String getNameCamel() {
    return StringUtil.getLowerCamelFromSnake(name);
  }

  // dispName
  /** Returns the user-friendly display name of this column. */
  public String getDisplayName() {
    return userFriendlyName;
  }

  public Map<String, String> getDisplayNameMap() {
    return new HashMap<String, String>(userFriendlyNameMap);
  }

  /** Adds the given display name to the locale-keyed display-name map. */
  public void addDispNameToMap(String localeString, String dispName) {
    userFriendlyNameMap.put(localeString, dispName);
  }

  public String getDataType() {
    return dataType;
  }

  public boolean getIsJavaOnly() {
    return ReaderUtil.boolStrToBoolean(isJavaOnly);
  }

  public String getIsJavaOnlyString() {
    return isJavaOnly;
  }

  public boolean isPk() {
    return ReaderUtil.boolStrToBoolean(isSurrogateKey);
  }

  public boolean isUniqueConstraint() {
    return ReaderUtil.boolStrToBoolean(isNaturalKey);
  }

  // nullable
  public boolean isNullable() {
    return ReaderUtil.boolStrToBoolean(isNullable);
  }

  // autoIncrement
  public boolean isAutoIncrement() {
    return ReaderUtil.boolStrToBoolean(isAutoIncrement);
  }

  // ForcedIncrement
  public boolean isForcedIncrement() {
    return ReaderUtil.boolStrToBoolean(isForcedIncrement);
  }

  // autoUpdate
  public boolean isAutoUpdate() {
    return ReaderUtil.boolStrToBoolean(isAutoUpdate);
  }

  // forcedUpdate
  public boolean isForcedUpdate() {
    return ReaderUtil.boolStrToBoolean(isForcedUpdate);
  }

  public boolean isCustomGroupColumn() {
    return ReaderUtil.boolStrToBoolean(isCustomGroupColumn);
  }

  public String getSpringAuditing() {
    return springAuditing;
  }

  // updatedValue
  public String getUpdatedValue() {
    return updatedValue;
  }

  /** Returns whether this column is a group column, taking Settings into account. */
  public boolean isGroupColumn() {
    String groupColumnName = MainController.tlInfo.get().getGroupRootInfo().getColumnName();
    return (groupColumnName != null && groupColumnName.equals(name)) || isCustomGroupColumn();
  }

  // optLock
  public void setOptLock(boolean isOptLock) {
    this.isOptLock = isOptLock;
  }

  public boolean isOptLock() {
    return isOptLock;
  }

  /** Returns whether this column has a relation configured. */
  public boolean isRelation() {
    return getRelationKind() != null;
  }

  public @org.jspecify.annotations.Nullable RelationKindEnum getRelationKind() {
    return RelationKindEnum.getEnumFromName(relationKind);
  }

  public boolean isRelationBidirectinal() {
    return relationDirection.equals("bidirectional");
  }

  public String getRelationDirection() {
    return relationDirection;
  }

  public String getRelationFieldName() {
    return relationFieldName;
  }

  public String getRelationFieldNameCp() {
    return StringUtils.capitalize(relationFieldName);
  }

  public String getRelationSrcObjVarName() {
    return relationSrcObjVarName;
  }

  /** Returns the effective object variable name for the relation field in the entity.
   *  When {@code relationSrcObjVarName} is specified, that value is used; otherwise falls back to
   *  {@code relationFieldName}.
   */
  public String getEffectiveRelationObjVarName() {
    return StringUtils.isEmpty(relationSrcObjVarName) ? relationFieldName : relationSrcObjVarName;
  }

  public String getEffectiveRelationObjVarNameCp() {
    return StringUtils.capitalize(getEffectiveRelationObjVarName());
  }

  public String getRelationRefTable() {
    return relationRefTable;
  }

  public String getRelationRefTableCpCamel() {
    return code.capitalCamel(relationRefTable);
  }

  public String getRelationRefTableCamel() {
    return code.uncapitalCamel(relationRefTable);
  }

  public String getRelationRefCol() {
    return relationRefCol;
  }

  public String getRelationRefColCpCamel() {
    return code.capitalCamel(relationRefCol);
  }

  public String getRelationRefFieldName() {
    return relationRefFieldName;
  }

  public boolean getRelationIsEager() {
    return ReaderUtil.boolStrToBoolean(relationIsEager);
  }

  // An index column value is either a plain position ("1", "2", ...) for a normal index, or the
  // same position prefixed with "U" ("U1", "U2", ...) to mark that index group as unique.
  private static final Pattern INDEX_VALUE_PATTERN = Pattern.compile("^(U)?([1-9][0-9]*)$");

  public @org.jspecify.annotations.Nullable Integer getIndex1() {
    return toIndexPosition(index1);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex2() {
    return toIndexPosition(index2);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex3() {
    return toIndexPosition(index3);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex4() {
    return toIndexPosition(index4);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex5() {
    return toIndexPosition(index5);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex6() {
    return toIndexPosition(index6);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex7() {
    return toIndexPosition(index7);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex8() {
    return toIndexPosition(index8);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex9() {
    return toIndexPosition(index9);
  }

  public @org.jspecify.annotations.Nullable Integer getIndex10() {
    return toIndexPosition(index10);
  }

  /**
   * Returns the index-group position for the given 1-based index serial (1 to 10), or
   * {@code null} if this column does not participate in that index group.
   *
   * @param indexSerial which of the 10 independent index groups (1 to 10) to read.
   * @return the column's position within that index group, or {@code null}.
   */
  public @org.jspecify.annotations.Nullable Integer getIndex(int indexSerial) {
    return switch (indexSerial) {
      case 1 -> getIndex1();
      case 2 -> getIndex2();
      case 3 -> getIndex3();
      case 4 -> getIndex4();
      case 5 -> getIndex5();
      case 6 -> getIndex6();
      case 7 -> getIndex7();
      case 8 -> getIndex8();
      case 9 -> getIndex9();
      case 10 -> getIndex10();
      default -> throw new IllegalArgumentException(
          "indexSerial must be between 1 and 10: " + indexSerial);
    };
  }

  /**
   * Returns {@code true} if this column's entry for the given index serial (1 to 10) marks
   * that index group as a unique index, i.e. its value is prefixed with {@code "U"}.
   *
   * @param indexSerial which of the 10 independent index groups (1 to 10) to read.
   * @return {@code true} if this column marks the index group as unique.
   */
  public boolean isIndexUnique(int indexSerial) {
    String value = switch (indexSerial) {
      case 1 -> index1;
      case 2 -> index2;
      case 3 -> index3;
      case 4 -> index4;
      case 5 -> index5;
      case 6 -> index6;
      case 7 -> index7;
      case 8 -> index8;
      case 9 -> index9;
      case 10 -> index10;
      default -> throw new IllegalArgumentException(
          "indexSerial must be between 1 and 10: " + indexSerial);
    };

    Matcher matcher = matchIndexValue(value);
    return matcher != null && matcher.group(1) != null;
  }

  private @org.jspecify.annotations.Nullable Integer toIndexPosition(@Nullable String value) {
    Matcher matcher = matchIndexValue(value);
    return matcher == null ? null : Integer.valueOf(matcher.group(2));
  }

  private @org.jspecify.annotations.Nullable Matcher matchIndexValue(@Nullable String value) {
    if (value == null || value.equals("")) {
      return null;
    }

    Matcher matcher = INDEX_VALUE_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Index value must be a positive integer, optionally "
          + "prefixed with \"U\" to mark it as a unique index (e.g. \"1\", \"U1\"): \"" + value
          + "\" (column: " + name + ")");
    }

    return matcher;
  }

  // supportedLang1
  public String getSupportedLang1() {
    return supportedLang1;
  }

  // supportedLang2
  public String getSupportedLang2() {
    return supportedLang2;
  }

  // supportedLang3
  public String getSupportedLang3() {
    return supportedLang3;
  }

  /**
   * Returns {@code true} if this column has either an outbound relation or an inbound bidirectional
   * relation reference.
   */
  public boolean hasAnyRelationsOrRefs() {
    return isRelation() || hasBidirectionalRelationRef();
  }

  /** Holds reference information for a relation pointing back to this column's owner table. */
  public static class RelationRefInfo {
    private boolean isBidirectional;
    private RelationKindEnum relationKind;
    private String dstTableName;
    private String dstColumnName;
    private String dstFieldNameToReferOrgTable;
    private String orgTableName;
    private String orgFieldName;
    private String orgFieldNameToReferDst;

    private ColumnGenUtil code = new ColumnGenUtil();

    /** Constructs a relation-reference info with all required relationship metadata. */
    public RelationRefInfo(boolean isBidirectional, RelationKindEnum relationKind,
        String dstTableName, String dstColumnName, String dstFieldNameToReferOrgTable,
        String orgTableName, String orgFieldName, String orgFieldNameToReferDst) {
      this.isBidirectional = isBidirectional;
      this.relationKind = relationKind;
      this.dstTableName = dstTableName;
      this.dstColumnName = dstColumnName;
      this.dstFieldNameToReferOrgTable = dstFieldNameToReferOrgTable;
      this.orgTableName = orgTableName;
      this.orgFieldName = orgFieldName;
      this.orgFieldNameToReferDst = orgFieldNameToReferDst;
    }

    public boolean isBidirectional() {
      return isBidirectional;
    }

    public RelationKindEnum getRelationKind() {
      return relationKind;
    }

    public String getDstTableName() {
      return dstTableName;
    }

    public String getDstColumnName() {
      return dstColumnName;
    }

    public String getDstFieldNameToReferOrgTable() {
      return dstFieldNameToReferOrgTable;
    }

    public String getOrgTableName() {
      return orgTableName;
    }

    public String getOrgTableNameCamel() {
      return code.uncapitalCamel(orgTableName);
    }

    public String getOrgTableNameCpCamel() {
      return code.capitalCamel(orgTableName);
    }

    public String getOrgFieldName() {
      return orgFieldName;
    }

    public String getOrgFieldNameToReferDst() {
      return orgFieldNameToReferDst;
    }

    /**
     * Returns the field name used to refer back to the original table, defaulting to the
     * lower-camel table name when not explicitly set.
     */
    public String getEmptyConsideredFieldNameToReferFromTable() {
      String fieldNamePostfix = (relationKind == RelationKindEnum.ONE_TO_ONE) ? "" : "List";
      return StringUtils.isEmpty(dstFieldNameToReferOrgTable)
          ? StringUtil.getLowerCamelFromSnake(orgTableName) + fieldNamePostfix
          : dstFieldNameToReferOrgTable;
    }
  }

  public DataTypeInfo getDtInfo() {
    return dtInfo;
  }

  public void setDtInfo(DataTypeInfo dtInfo) {
    this.dtInfo = dtInfo;
  }

  /**
   * Returns the validator list, adding {@code @NotEmpty} information on top of
   * {@code DataTypeInfo#getValidatorList}.
   *
   * @param forEntity {@code true} for Entity, {@code false} for Record
   * @return {@code List<ValidatorGen>}
   */
  public List<ValidatorGen> getValidatorList(boolean forEntity) {
    List<ValidatorGen> rtnList = new ArrayList<>();

    // No validators when isJavaOnly
    if (getIsJavaOnly()) {
      return rtnList;
    }

    if (forEntity) {
      if (NotEmptyGen.needsValidator(this)) {
        rtnList.add(new NotEmptyGen(dtInfo));
      }
    }

    if (!isRelation()) {
      rtnList.addAll(forEntity ? dtInfo.getValidatorList(true) : dtInfo.getValidatorList(false));
    }

    return rtnList;
  }

  @Override
  public void afterReading() {}
}
