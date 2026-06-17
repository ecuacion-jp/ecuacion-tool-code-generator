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
import static jp.ecuacion.lib.validation.constraints.enums.ConditionValue.STRING;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.validation.constraints.EmptyWhen;
import jp.ecuacion.lib.validation.constraints.EnumElement;
import jp.ecuacion.lib.validation.constraints.IntegerString;
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
import jp.ecuacion.tool.codegenerator.core.util.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import jp.ecuacion.util.excel.table.bean.StringExcelTableBean;
import org.jspecify.annotations.Nullable;

/**
 * Holds data type definition information read from Excel, including validation rules for
 * each type.
 */
@EmptyWhen(
    propertyPath = {"minLength", "maxLength", "stringDataPtn", "stringAllowsProhibitedCharacters",
        "stringRegEx", "stringRegExDescLangDefault", "stringRegExDescLangSupport01",
        "stringRegExDescLangSupport02", "stringRegExDescLangSupport03"},
    conditionPropertyPath = "kata", conditionValue = STRING, conditionOperator = NOT_EQUAL_TO,
    conditionValueString = "STRING")
@EmptyWhen(propertyPath = {"numMinVal", "numMaxVal"}, conditionPropertyPath = "kata",
    conditionValue = STRING, conditionOperator = NOT_EQUAL_TO,
    conditionValueString = {"SHORT", "INTEGER", "LONG", "FLOAT", "DOUBLE", "BIG_INTEGER",
        "BIG_DECIMAL"})
@EmptyWhen(propertyPath = {"numDigitInteger"}, conditionPropertyPath = "kata",
    conditionValue = STRING, conditionOperator = NOT_EQUAL_TO,
    conditionValueString = {"SHORT", "INTEGER", "LONG", "BIG_INTEGER", "BIG_DECIMAL"})
@EmptyWhen(propertyPath = {"numDigitFraction"}, conditionPropertyPath = "kata",
    conditionValue = STRING, conditionOperator = NOT_EQUAL_TO,
    conditionValueString = {"BIG_DECIMAL"})
@EmptyWhen(propertyPath = {"enumCodeLength"}, conditionPropertyPath = "kata",
    conditionValue = STRING, conditionOperator = NOT_EQUAL_TO, conditionValueString = {"ENUM"})
@EmptyWhen(propertyPath = {"notNeedsTimezone"}, conditionPropertyPath = "kata",
    conditionValue = STRING, conditionOperator = NOT_EQUAL_TO,
    conditionValueString = {"DATE_TIME", "TIMESTAMP"})
@SuppressWarnings("NullAway.Init")
public class DataTypeInfo extends StringExcelTableBean {

  public static final String SHEET_NAME_JA = "dataType定義";
  public static final String SHEET_NAME_EN = "dataType Definition";

  public static final List<String> HEADER_LABELS_JA =
      List.of("DataType名", "型", "長さ最小", "長さ最大", "データパターン（日本語）", "データパターン", "禁則文字チェック除外",
          "正規表現", "パターン説明（デフォルト言語）", "パターン説明（追加言語1）", "パターン説明（追加言語2）", "パターン説明（追加言語3）", "最小値",
          "最大値", "整数部桁数", "小数部桁数", "コードの長さ", "timezoneなし", "備考");

  public static final List<String> HEADER_LABELS_EN =
      List.of("DataType Name", "Type", "Min Length", "Max Length", "Data Pattern (Japanese)",
          "Data Pattern", "Exclude Prohibited Chars Check", "Regex", "Pattern Desc (Default Lang)",
          "Pattern Desc (Additional Lang 1)", "Pattern Desc (Additional Lang 2)",
          "Pattern Desc (Additional Lang 3)", "Min Value", "Max Value", "Integer Digits",
          "Decimal Digits", "Code Length", "No Timezone", "Notes");

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
  protected @Nullable String[] getFieldNameArray() {
    return new String[] {"dataTypeName", "kata", "minLength", "maxLength", null, "stringDataPtn",
        "stringAllowsProhibitedCharacters", "stringRegEx", "stringRegExDescLangDefault",
        "stringRegExDescLangSupport01", "stringRegExDescLangSupport02",
        "stringRegExDescLangSupport03", "numMinVal", "numMaxVal", "numDigitInteger",
        "numDigitFraction", "enumCodeLength", "notNeedsTimezone", "remarks"};
  }

  /** Constructs an instance from a column list read from an Excel table row. */
  @SuppressWarnings("null")
  public DataTypeInfo(List<String> colList) {
    super(colList);
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  public DataTypeKataEnum getKata() {
    return DataTypeKataEnum.valueOf(kata);
  }

  private @Nullable Integer getMinLength() {
    return minLength == null ? null : toInteger(minLength);
  }

  /**
   * Made public because it is also used for column size.
   *
   * @return integer
   */
  public @Nullable Integer getMaxLength() {
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

  private @Nullable Integer getNumDigitInteger() {
    return numDigitInteger == null ? null : toInteger(numDigitInteger);
  }

  private @Nullable Integer getNumDigitFraction() {
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

  /** Validates the data type settings and builds the list of validator generators. */
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
          new DataTypeKataEnum[] {DataTypeKataEnum.valueOf(name.toUpperCase(Locale.ROOT))}));
    }
  }

  /** Returns the regex description for the given language, or {@code null} if absent. */
  public @Nullable String getStringRegExDesc(List<String> supportedLangArr, String lang) {
    List<String> descList = Arrays.asList(new String[] {stringRegExDescLangSupport01,
        stringRegExDescLangSupport02, stringRegExDescLangSupport03});

    Map<String, String> map = new HashMap<>();
    map.put("", stringRegExDescLangDefault);
    for (int i = 0; i < supportedLangArr.size(); i++) {
      map.put(supportedLangArr.get(i), descList.get(i));
    }

    return map.get(lang);
  }

  /* Converts a plain string into a regex that rejects any string containing those characters. */
  private String changeNgCharsToRegEx(String str) {

    // If not specified, apply the strictest restriction (all symbols are prohibited)
    if (str == null || str.equals("")) {
      str = "!\"#$%&'()=-^~\\|@`[{;+:*]},<.>/?_";
    }

    // Escape characters required for regex
    String[] escapeStrs =
        new String[] {"*", "\\", "+", ".", "?", "{", "}", "(", ")", "[", "]", "^", "$", "-", "|"};
    for (String escapeStr : escapeStrs) {
      str = str.replace(escapeStr, "\\" + escapeStr);
    }

    // Escape characters required for Java
    str = str.replaceAll("\\\\", "\\\\\\\\");
    str = str.replace("\"", "\\\"");

    // Surround with regex symbols to form a negated character class
    str = "^[^" + str + "]*$";

    return str;
  }

  @Override
  public void afterReading() {

  }
}
