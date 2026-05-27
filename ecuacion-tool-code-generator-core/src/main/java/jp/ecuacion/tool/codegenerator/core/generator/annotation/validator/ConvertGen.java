package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;

/**
 * Generator for the JPA {@code @Convert} annotation, specifying the converter class for enum
 * columns.
 */
public class ConvertGen extends FieldSingleAnnotationGen {
  private DataTypeInfo dtInfo;
  private CodeGenUtil code = new CodeGenUtil();

  /** Constructs a ConvertGen for the given element type and data type information. */
  public ConvertGen(ElementType elementType, DataTypeInfo dtInfo) {
    super("Convert", elementType);
    this.dtInfo = dtInfo;
  }

  /** Returns {@code true} if the data type is an enum, requiring a {@code @Convert} annotation. */
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
        code.dataTypeNameToCapitalCamel(dtInfo.getDataTypeName())
            + "Converter.class",
        DataTypeKataEnum.ENUM));

    return plistGen;
  }

  @Override
  protected void check() {}
}
