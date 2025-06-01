package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalEmpty;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalNotEmpty;
import jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.NotEmptyGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ValidatorGen;
import jp.ecuacion.tool.codegenerator.core.util.reader.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.tool.codegenerator.core.validation.StrPk;
import jp.ecuacion.util.poi.excel.table.bean.StringExcelTableBean;
import org.apache.commons.lang3.StringUtils;

@ConditionalNotEmpty(
    field = {"relationDirection", "relationFieldName", "relationRefTable", "relationRefCol"},
    conditionField = "relationKind",
    conditionPattern = ConditionPattern.valueOfConditionFieldIsNotEmpty,
    emptyWhenConditionNotSatisfied = true)
@ConditionalEmpty(field = "relationRefFieldName", conditionField = "relationDirection",
    conditionPattern = ConditionPattern.stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = "bidirectional")
@ConditionalEmpty(field = "relationIsEager", conditionField = "relationKind",
    conditionPattern = ConditionPattern.valueOfConditionFieldIsEmpty)
public class DbOrClassColumnInfo extends StringExcelTableBean {

  private List<BidirectionalRelationInfo> bidirectionalInfo = new ArrayList<>();

  // ファイルからのデータ取り込みでは使用しなくなったので直接booleanで持つ
  private boolean isOptLock = false;

  @NotEmpty
  @Size(max = 50)
  @Pattern(regexp = Constants.REG_EX_UP_NUM_US)
  private String columnName;

  private String userFriendlyName;

  // 多言語に対応するため、dispNameをMapで持つ。キーは言語（jaなど）。デフォルト言語に対するキーはLANG_DEFを使用
  private HashMap<String, String> userFriendlyNameMap = new HashMap<String, String>();

  @NotEmpty
  @Size(max = 50)
  @Pattern(regexp = Constants.REG_EX_DT_NAME)
  private String dataType;
  @StrBoolean
  private String isJavaOnly;
  @StrPk
  private String pkKind;
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
  @Pattern(regexp = "^CB|CD|LB|LD$")
  private String springAuditing;

  // private String valueChangeMethod;
  private String updatedValue;

  @Pattern(regexp = "^@ManyToOne|@OneToOne$")
  private String relationKind;
  private String relationDirection;
  private String relationFieldName;
  private String relationRefTable;
  private String relationRefCol;
  private String relationRefFieldName;
  private String relationIsEager;

  private String index1;
  private String index2;
  private String index3;

  private String supportedLang1;
  private String supportedLang2;
  private String supportedLang3;

  /** 利便性のために追加。 */
  private DataTypeInfo dtInfo;

  //@formatter:off
  @Override
  protected @Nonnull String[] getFieldNameArray() {
    return new String[] {
        null, "userFriendlyName", "columnName", "dataType", null, 
        "isJavaOnly", "pkKind", "isNullable", "isAutoIncrement", "isForcedIncrement", 
        "isAutoUpdate", "isForcedUpdate", "isCustomGroupColumn", "springAuditing", "relationKind", 
        "relationDirection", 
        "relationFieldName", "relationRefTable", "relationRefCol", "relationRefFieldName", 
        "relationIsEager", 
        "index1", "index2", "index3", null, "supportedLang1", 
        "supportedLang2", "supportedLang3"
    };
  }
  //@formatter:on

  public DbOrClassColumnInfo(List<String> colList) {
    super(colList);
  }

  public DbOrClassColumnInfo(List<String> colList, String localeDefault, String locale1,
      String locale2, String locale3) {

    this(colList);

    String[] locales = new String[] {localeDefault, locale1, locale2, locale3};
    String[] localNames =
        new String[] {userFriendlyName, supportedLang1, supportedLang2, supportedLang3};

    for (int i = 0; i < locales.length; i++) {
      String locale = locales[i];
      if (!StringUtils.isEmpty(locale)) {
        userFriendlyNameMap.put(locale, localNames[i]);
      }
    }
  }

  public static DbOrClassColumnInfo cloneWithoutRelationRelated(DbOrClassColumnInfo ci) {
    String[] arr = new String[] {null, ci.getDisplayName(), ci.getColumnName(), ci.getDataType(),
        null, ci.getIsJavaOnlyString(), ci.getPkKindString(), ci.isNullable,
        ReaderUtil.booleanToBoolStr(ci.isAutoIncrement()),
        ReaderUtil.booleanToBoolStr(ci.isForcedIncrement()),
        ReaderUtil.booleanToBoolStr(ci.isAutoUpdate()),
        ReaderUtil.booleanToBoolStr(ci.isForcedUpdate()),
        ReaderUtil.booleanToBoolStr(ci.isCustomGroupColumn()), ci.getSpringAuditing(), "", "", "",
        "", "", "", "", ci.getIndex1() == null ? null : ci.getIndex1().toString(),
        ci.getIndex2() == null ? null : ci.getIndex2().toString(),
        ci.getIndex3() == null ? null : ci.getIndex3().toString(), null, ci.getSupportedLang1(),
        ci.getSupportedLang2(), ci.getSupportedLang3()};

    DbOrClassColumnInfo rtnCi = new DbOrClassColumnInfo(Arrays.asList(arr));

    rtnCi.setDtInfo(ci.getDtInfo());
    return rtnCi;
  }

  public List<BidirectionalRelationInfo> getBidirectionalInfo() {
    return bidirectionalInfo;
  }

  public boolean isReferedByBidirectionalRelation() {
    return bidirectionalInfo.size() != 0;
  }

  public boolean hasBidirectionalInfo() {
    return bidirectionalInfo != null && bidirectionalInfo.size() > 0;
  }

  // columnName
  public String getColumnName() {
    return columnName;
  }

  // dispName
  public String getDisplayName() {
    return userFriendlyName;
  }

  public Map<String, String> getDisplayNameMap() {
    return new HashMap<String, String>(userFriendlyNameMap);
  }

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
    return pkKind != null && pkKind.equals("S");
  }

  public String getPkKindString() {
    return pkKind;
  }

  public boolean isUniqueConstraint() {
    return pkKind != null && pkKind.equals("U");
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

  /** Settings側も加味した上でgroupの項目か否かを返す。 */
  public boolean isGroupColumn() {
    String groupColumnName = MainController.tlInfo.get().groupRootInfo.getColumnName();
    return (groupColumnName != null && groupColumnName.equals(columnName)) || isCustomGroupColumn();
  }

  // optLock
  public void setOptLock(boolean isOptLock) {
    this.isOptLock = isOptLock;
  }

  public boolean isOptLock() {
    return isOptLock;
  }

  /** relationのcolumnかを判断するメソッド。 */
  public boolean isRelationColumn() {
    return getRelationKind() != null;
  }

  public RelationKindEnum getRelationKind() {
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

  public String getRelationRefTable() {
    return relationRefTable;
  }

  public String getRelationRefCol() {
    return relationRefCol;
  }

  public String getRelationRefFieldName() {
    return relationRefFieldName;
  }

  public boolean getRelationIsEager() {
    return ReaderUtil.boolStrToBoolean(relationIsEager);
  }

  public Integer getIndex1() {
    return toInteger(index1);
  }

  public Integer getIndex2() {
    return toInteger(index2);
  }

  public Integer getIndex3() {
    return toInteger(index3);
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

  public static class BidirectionalRelationInfo {

    private RelationKindEnum relationKind;
    private String tableName;
    private String columnName;
    private String fieldNameToReferFromTable;
    private String referFromTableName;
    private String referFromFieldName;

    public BidirectionalRelationInfo(RelationKindEnum relationKind, String tableName,
        String columnName, String fieldNameToReferFromTable, String referFromTableName,
        String referFromFieldName) {
      this.relationKind = relationKind;
      this.tableName = tableName;
      this.columnName = columnName;
      this.fieldNameToReferFromTable = fieldNameToReferFromTable;
      this.referFromTableName = referFromTableName;
      this.referFromFieldName = referFromFieldName;
    }

    public RelationKindEnum getRelationKind() {
      return relationKind;
    }

    public String getTableName() {
      return tableName;
    }

    public String getColumnName() {
      return columnName;
    }

    public String getFieldNameToReferFromTable() {
      return fieldNameToReferFromTable;
    }

    public String getReferFromTableName() {
      return referFromTableName;
    }

    public String getReferFromFieldName() {
      return referFromFieldName;
    }

    public String getEmptyConsideredFieldNameToReferFromTable() {
      String fieldNamePostfix = (relationKind == RelationKindEnum.ONE_TO_ONE) ? "" : "List";
      return StringUtils.isEmpty(fieldNameToReferFromTable)
          ? StringUtil.getLowerCamelFromSnake(referFromTableName) + fieldNamePostfix
          : fieldNameToReferFromTable;
    }
  }

  public DataTypeInfo getDtInfo() {
    return dtInfo;
  }

  public void setDtInfo(DataTypeInfo dtInfo) {
    this.dtInfo = dtInfo;
  }

  /**
   * DataTypeInfo#getValidatorList に加えて、@NotEmptyの情報を追加
   *
   * @param forEntity Entityの場合true、Recordの場合false
   * @return {@code List<ValidatorGen>}
   */
  public List<ValidatorGen> getValidatorList(boolean forEntity) {
    List<ValidatorGen> rtnList = new ArrayList<>();

    // isJavaOnlyの場合はvalidatorなし
    if (getIsJavaOnly()) {
      return rtnList;
    }

    if (forEntity) {
      if (NotEmptyGen.needsValidator(this)) {
        rtnList.add(new NotEmptyGen(dtInfo));
      }
    }

    if (!isRelationColumn()) {
      rtnList.addAll(forEntity ? dtInfo.getValidatorList(true) : dtInfo.getValidatorList(false));
    }

    return rtnList;
  }

  @Override
  public void afterReading() {}
}
