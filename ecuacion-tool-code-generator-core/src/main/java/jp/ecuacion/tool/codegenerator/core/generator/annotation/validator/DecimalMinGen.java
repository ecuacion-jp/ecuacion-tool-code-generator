package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class DecimalMinGen extends ValidatorGen {

  private String minVal;

  public DecimalMinGen(DataTypeInfo dtInfo, String minVal) {
    super("DecimalMin", dtInfo);
    this.minVal = minVal;
  }

  public static boolean needsValidator(String numMinVal) {
    return numMinVal != null && !numMinVal.equals("");
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL, STRING};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    if (minVal != null) {
      plistGen.add(new ParamGenWithSingleValue("value", minVal, STRING));
    }
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }

  public String getMinVal() {
    return minVal;
  }
}
