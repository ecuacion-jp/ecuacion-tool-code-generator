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

public class DigitsGen extends ValidatorGen {

  private Integer integer;
  private Integer fraction;

  public DigitsGen(DataTypeInfo dtInfo, Integer integer, Integer fraction) {
    super("FieldDigits", dtInfo);
    this.integer = integer;
    this.fraction = fraction;
  }

  public static boolean needsValidator(Integer integer, Integer fraction) {
    return !(integer == null && fraction == null);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL, STRING};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    if (integer != null) {
      plistGen.add(
          new ParamGenWithSingleValue("integer", integer.toString(), DataTypeKataEnum.INTEGER));
    }

    if (fraction != null) {
      plistGen.add(
          new ParamGenWithSingleValue("fraction", fraction.toString(), DataTypeKataEnum.INTEGER));
    }
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }
}
