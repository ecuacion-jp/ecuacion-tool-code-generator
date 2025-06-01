package jp.ecuacion.tool.codegenerator.core.dto;

import static jp.ecuacion.lib.core.jakartavalidation.validator.enums.ConditionPattern.stringValueOfConditionFieldIsNotEqualTo;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.jakartavalidation.validator.ConditionalEmpty;
import jp.ecuacion.lib.core.jakartavalidation.validator.EnumElement;
import jp.ecuacion.lib.core.jakartavalidation.validator.IntegerString;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeStringDataPtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.DecimalMaxGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.DecimalMinGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.DigitsGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.PatternGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.SimpleValidatorGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.SizeGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ValidatorGen;
import jp.ecuacion.tool.codegenerator.core.util.reader.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.util.poi.excel.table.bean.StringExcelTableBean;

@ConditionalEmpty(
    field = {"minLength", "maxLength", "stringDataPtn", "stringAllowsProhibitedCharacters",
        "stringRegEx", "stringRegExDescLangDefault", "stringRegExDescLangSupport01",
        "stringRegExDescLangSupport02", "stringRegExDescLangSupport03"},
    conditionField = "kata", conditionPattern = stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = "STRING")
@ConditionalEmpty(field = {"numMinVal", "numMaxVal"}, conditionField = "kata",
    conditionPattern = stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = {"SHORT", "INTEGER", "LONG", "FLOAT", "DOUBLE", "BIG_INTEGER",
        "BIG_DECIMAL"})
@ConditionalEmpty(field = {"numDigitInteger"}, conditionField = "kata",
    conditionPattern = stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = {"SHORT", "INTEGER", "LONG", "BIG_INTEGER", "BIG_DECIMAL"})
@ConditionalEmpty(field = {"numDigitFraction"}, conditionField = "kata",
    conditionPattern = stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = {"BIG_DECIMAL"})
@ConditionalEmpty(field = {"enumCodeLength"}, conditionField = "kata",
    conditionPattern = stringValueOfConditionFieldIsNotEqualTo, conditionValueString = {"ENUM"})
@ConditionalEmpty(field = {"notNeedsTimezone"}, conditionField = "kata",
    conditionPattern = stringValueOfConditionFieldIsNotEqualTo,
    conditionValueString = {"DATE_TIME", "TIMESTAMP"})
public class DataTypeInfo extends StringExcelTableBean {

  public static String[] HEADER_LABELS =
      new String[] {"DataType名", "型", "長さ最小", "長さ最大", "データパターン（日本語）", "データパターン", "禁則文字チェック除外",
          "正規表現", "パターン説明（デフォルト言語）", "パターン説明（追加言語1）", "パターン説明（追加言語2）", "パターン説明（追加言語3）", "最小値",
          "最大値", "整数部桁数", "小数部桁数", "コードの長さ", "timezoneなし", "備考"};

  @NotEmpty
  @Size(min = 0, max = 50)
  @Pattern(regexp = Constants.REG_EX_DT_NAME)
  private String dataTypeName;
  @NotEmpty
  private String kata;
  @IntegerString
  private String minLength;
  @IntegerString
  private String maxLength;
  @EnumElement(enumClass = DataTypeStringDataPtnEnum.class)
  private String stringDataPtn;
  @Size(max = 50)
  private String stringAllowsProhibitedCharacters;
  @Size(max = 100)
  private String stringRegEx;
  private String stringRegExDescLangDefault;
  private String stringRegExDescLangSupport01;
  private String stringRegExDescLangSupport02;
  private String stringRegExDescLangSupport03;
  @IntegerString
  @Size(max = 50)
  private String numMinVal;
  @IntegerString
  @Size(max = 50)
  private String numMaxVal;
  @IntegerString
  private String numDigitInteger;
  @IntegerString
  private String numDigitFraction;
  private String enumCodeLength;
  @StrBoolean
  private String notNeedsTimezone;
  @Size(max = 10000)
  private String remarks;

  private List<ValidatorGen> validatorForBothList = new ArrayList<>();
  private List<ValidatorGen> validatorForRecordList = new ArrayList<>();

  @Override
  protected @Nonnull String[] getFieldNameArray() {
    return new String[] {"dataTypeName", "kata", "minLength", "maxLength", null, "stringDataPtn",
        "stringAllowsProhibitedCharacters", "stringRegEx", "stringRegExDescLangDefault",
        "stringRegExDescLangSupport01", "stringRegExDescLangSupport02",
        "stringRegExDescLangSupport03", "numMinVal", "numMaxVal", "numDigitInteger",
        "numDigitFraction", "enumCodeLength", "notNeedsTimezone", "remarks"};
  }

  public DataTypeInfo(List<String> colList) {
    super(colList);
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  public DataTypeKataEnum getKata() {
    return DataTypeKataEnum.valueOf(kata);
  }

  private Integer getMinLength() {
    return minLength == null ? null : toInteger(minLength);
  }

  /**
   * Column sizeでも使用するためpublic.
   *
   * @return integer
   */
  public Integer getMaxLength() {
    return maxLength == null ? null : toInteger(maxLength);
  }

  public String getStringRegExDescLangDefault() {
    return stringRegExDescLangDefault;
  }

  public String getStringRegExDescLangSupport01() {
    return stringRegExDescLangSupport01;
  }

  public String getStringRegExDescLangSupport02() {
    return stringRegExDescLangSupport02;
  }

  public String getStringRegExDescLangSupport03() {
    return stringRegExDescLangSupport03;
  }

  private Integer getNumDigitInteger() {
    return numDigitInteger == null ? null : toInteger(numDigitInteger);
  }

  private Integer getNumDigitFraction() {
    return numDigitFraction == null ? null : toInteger(numDigitFraction);
  }

  public String getEnumCodeLength() {
    return enumCodeLength;
  }

  public boolean getNotNeedsTimezone() {
    return ReaderUtil.boolStrToBoolean(notNeedsTimezone);
  }

  public String getRemarks() {
    return remarks;
  }

  /**
   * Returns a list of {@code ValidatorGen}.
   * 
   * <p>This is mainly used for {@code DataTypeGen}. When the column is designated, 
   *     use {@code DbOrClassColumnInfo#getValidatorList} 
   *     because it considers column related settings.</p>
   *
   * @param forEntity Set {@code true} 
   *     when you want to obtain validator list for {@code Entity}.
   * @return a list of {@code ValidatorGen}.
   */
  public List<ValidatorGen> getValidatorList(boolean forEntity) {
    List<ValidatorGen> rtnList = new ArrayList<>(validatorForBothList);
    if (!forEntity) {
      rtnList.addAll(validatorForRecordList);
    }

    return rtnList;
  }

  public String getPatternDescriptionId() {
    return StringUtil.getLowerCamelFromSnake(dataTypeName.substring(3));
  }

  public void checksAndComplements(SystemCommonRootInfo sysCmnRootInfo) {
    createValidators(sysCmnRootInfo);
  }

  private void createValidators(SystemCommonRootInfo sysCmnRootInfo) {
    // create vavlidators
    if (SizeGen.needsValidator(getMinLength(), getMaxLength())) {
      validatorForBothList.add(new SizeGen(this, getMinLength(), getMaxLength()));
    }

    if (stringDataPtn != null) {
      DataTypeStringDataPtnEnum dataPtnEnum = DataTypeStringDataPtnEnum.valueOf(stringDataPtn);
      if (PatternGen.needsValidatorPattern1(dataPtnEnum)) {
        String regEx1 = Constants.getStringDataPtnRegExMap(dataPtnEnum.toString());
        validatorForBothList.add(new PatternGen(this, regEx1, getPatternDescriptionId()));
      }
    }

    if (PatternGen.needsValidatorPattern2(stringRegEx)) {
      String regEx2 = stringRegEx;
      validatorForBothList.add(new PatternGen(this, regEx2, getPatternDescriptionId()));
    }

    String regEx3 = changeNgCharsToRegEx(sysCmnRootInfo.getProhibitedChars());
    if (PatternGen.needsValidatorPattern3(this, regEx3,
        ReaderUtil.boolStrToBoolean(stringAllowsProhibitedCharacters))) {
      validatorForBothList.add(new PatternGen(this, regEx3, "prohibitedChars"));
    }

    if (DecimalMinGen.needsValidator(numMinVal)) {
      validatorForBothList.add(new DecimalMinGen(this, numMinVal));
    }

    if (DecimalMaxGen.needsValidator(numMaxVal)) {
      validatorForBothList.add(new DecimalMaxGen(this, numMaxVal));
    }

    if (DigitsGen.needsValidator(getNumDigitInteger(), getNumDigitFraction())) {
      validatorForBothList.add(new DigitsGen(this, getNumDigitInteger(), getNumDigitFraction()));
    }

    // for record
    DataTypeKataEnum kata = getKata();
    if (kata == DataTypeKataEnum.INTEGER || kata == DataTypeKataEnum.LONG) {
      String name = StringUtil.getUpperCamelFromSnake(kata.toString());
      validatorForRecordList.add(new SimpleValidatorGen(name + "String", this, false,
          new DataTypeKataEnum[] {DataTypeKataEnum.valueOf(name.toUpperCase())}));
    }
  }

  public String getStringRegExDesc(List<String> supportedLangArr, String lang) {
    List<String> descList = Arrays.asList(new String[] {stringRegExDescLangSupport01,
        stringRegExDescLangSupport02, stringRegExDescLangSupport03});

    Map<String, String> map = new HashMap<>();
    map.put("", stringRegExDescLangDefault);
    for (int i = 0; i < supportedLangArr.size(); i++) {
      map.put(supportedLangArr.get(i), descList.get(i));
    }

    return map.get(lang);
  }

  /* ただの文字列を、それらの文字を含む文字列がNGとなる正規表現に変更。 */
  private String changeNgCharsToRegEx(String str) {

    // 指定されていない場合は、最も厳しい制限とする（記号がすべてNG）
    if (str == null || str.equals("")) {
      str = "!\"#$%&'()=-^~\\|@`[{;+:*]},<.>/?_";
    }

    // 正規表現として必要なエスケープ
    String[] escapeStrs =
        new String[] {"*", "\\", "+", ".", "?", "{", "}", "(", ")", "[", "]", "^", "$", "-", "|"};
    for (String escapeStr : escapeStrs) {
      str = str.replace(escapeStr, "\\" + escapeStr);
    }

    // javaとして必要なエスケープ
    str = str.replaceAll("\\\\", "\\\\\\\\");
    str = str.replace("\"", "\\\"");

    // 正規表現とするための記号を追加
    str = "^[^" + str + "]*$";

    return str;
  }

  @Override
  public void afterReading() throws AppException {

  }
}
