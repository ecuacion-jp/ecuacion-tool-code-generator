package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;

public class NormalSingleAnnotationGen extends SingleAnnotationGen {

  private ParamGen paramGen;

  public NormalSingleAnnotationGen(String annotationName, ElementType elementType,
      ParamGen paramGen) {
    super(annotationName, elementType);
    this.paramGen = paramGen;
  }

  @Override
  protected ParamGen getParamGen() {
    return paramGen;
  }

  @Override
  protected void check() {}

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {ElementType.TYPE, ElementType.FIELD};
  }
}
