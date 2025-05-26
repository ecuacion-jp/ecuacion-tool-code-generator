package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeStringDataPtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class PatternGen extends ValidatorGen {

  private String regEx;
  private String descriptionId;

  public PatternGen(DataTypeInfo dtInfo, String regEx, String descriptionId) {
    super("PatternWithDescription", dtInfo);
    this.regEx = regEx;
    this.descriptionId = descriptionId;
  }

  public static boolean needsValidatorPattern1(DataTypeStringDataPtnEnum stringDataPtn) {
    return stringDataPtn != null && stringDataPtn != DataTypeStringDataPtnEnum.REG_EX_ALL;
  }

  public static boolean needsValidatorPattern2(String stringRegEx) {
    return stringRegEx != null && !stringRegEx.equals("");
  }

  public static boolean needsValidatorPattern3(DataTypeInfo dtInfo, String prohibitedCharacters,
      boolean allowsProhibitedCharacters) {
    return prohibitedCharacters != null && !prohibitedCharacters.equals("")
        && dtInfo.getKata() == DataTypeKataEnum.STRING && !allowsProhibitedCharacters;
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {STRING};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    plistGen.add(new ParamGenWithSingleValue("regexp", regEx, DataTypeKataEnum.STRING));
    plistGen
        .add(new ParamGenWithSingleValue("descriptionId", descriptionId, DataTypeKataEnum.STRING));
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return false;
  }
}

