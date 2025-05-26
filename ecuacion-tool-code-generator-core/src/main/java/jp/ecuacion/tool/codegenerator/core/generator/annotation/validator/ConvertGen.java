package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.StringGenUtil;

public class ConvertGen extends FieldSingleAnnotationGen {
  private DataTypeInfo dtInfo;

  public ConvertGen(ElementType elementType, DataTypeInfo dtInfo) {
    super("Convert", elementType);
    this.dtInfo = dtInfo;
  }

  public static boolean needsValidator(DataTypeInfo dtInfo) {
    return dtInfo.getKata() == DataTypeKataEnum.ENUM;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // enumでのみ使用
    return new DataTypeKataEnum[] {DataTypeKataEnum.ENUM};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    plistGen.add(new ParamGenWithSingleValue("converter",
        StringGenUtil.dataTypeNameToUppperCamel(dtInfo.getDataTypeName())
            + "Converter.class",
        DataTypeKataEnum.ENUM));

    return plistGen;
  }

  @Override
  protected void check() {}
}
