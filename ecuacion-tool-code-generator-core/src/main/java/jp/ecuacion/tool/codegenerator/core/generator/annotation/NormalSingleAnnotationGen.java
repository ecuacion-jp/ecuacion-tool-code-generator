package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;

/**
 * A general-purpose SingleAnnotationGen that accepts an arbitrary ParamGen at construction time.
 */
public class NormalSingleAnnotationGen extends SingleAnnotationGen {

  private ParamGen paramGen;

  /**
   * Constructs a NormalSingleAnnotationGen with the given annotation name, element type, and
   * parameter generator.
   */
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
