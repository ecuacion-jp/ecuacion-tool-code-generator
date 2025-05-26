package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class SizeGen extends ValidatorGen {

  private Integer minSize;
  private Integer maxSize;

  public SizeGen(DataTypeInfo dtInfo, Integer minSize, Integer maxSize) {
    super("FieldSize", dtInfo);
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  public static boolean needsValidator(Integer minSize, Integer maxSize) {
    return !(minSize == null && maxSize == null);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {STRING, BIG_DECIMAL};
  }


  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {

    if (minSize != null) {
      plistGen
          .add(new ParamGenWithSingleValue("min", minSize.toString(), DataTypeKataEnum.INTEGER));
    }

    if (maxSize != null) {
      plistGen
          .add(new ParamGenWithSingleValue("max", maxSize.toString(), DataTypeKataEnum.INTEGER));
    }
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }
}
