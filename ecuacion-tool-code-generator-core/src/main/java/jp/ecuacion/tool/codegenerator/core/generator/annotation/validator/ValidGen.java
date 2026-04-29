package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

public class ValidGen extends FieldSingleAnnotationGen {

  public ValidGen(ElementType elementType) {
    super("Valid", elementType);
  }

  public static boolean needsValidator(String columnName) {
    return true;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // 全てOK
    return DataTypeKataEnum.values();
  }

  @Override
  protected @Nullable ParamListGen getParamGen() {
    return null;
  }

  @Override
  protected void check() {
  }
}
