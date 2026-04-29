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
import org.jspecify.annotations.Nullable;

public class DigitsGen extends ValidatorGen {

  private @Nullable Integer integer;
  private @Nullable Integer fraction;

  /** Builds a {@code Digits} validator generator with the optional integer/fraction bounds. */
  public DigitsGen(DataTypeInfo dtInfo, @Nullable Integer integer, @Nullable Integer fraction) {
    super("FieldDigits", dtInfo);
    this.integer = integer;
    this.fraction = fraction;
  }

  /** Returns whether a {@code Digits} validator should be generated for the given bounds. */
  public static boolean needsValidator(@Nullable Integer integer, @Nullable Integer fraction) {
    return !(integer == null && fraction == null);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL, STRING};
  }

  @SuppressWarnings("null")
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
