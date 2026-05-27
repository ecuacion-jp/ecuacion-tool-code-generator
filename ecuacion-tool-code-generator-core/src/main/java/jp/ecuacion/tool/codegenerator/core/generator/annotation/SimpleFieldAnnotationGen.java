package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
  * A convenience FieldSingleAnnotationGen for simple, parameter-free field annotations such as
  * {@code @Version}.
 */
public class SimpleFieldAnnotationGen extends FieldSingleAnnotationGen {

  /** Constructs a SimpleFieldAnnotationGen for the given annotation name. */
  public SimpleFieldAnnotationGen(String annotationName) {
    super(annotationName, ElementType.FIELD);
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return DataTypeKataEnum.values();
  }

  @Override
  protected ParamGen getParamGen() {
    return new ParamListGen();
  }

  @Override
  protected void check() {
  }
}
