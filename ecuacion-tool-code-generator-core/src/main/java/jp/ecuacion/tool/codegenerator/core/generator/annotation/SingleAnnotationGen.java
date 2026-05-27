package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import org.jspecify.annotations.Nullable;

/**
 * ListAnnotationGen以外の、Listでannotationを持たない普通のAnnotationのgenerator.
 */
public abstract class SingleAnnotationGen extends AnnotationGen {

  /** Constructor used when creating a normal annotation. */
  public SingleAnnotationGen(String annotationName, @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  /** Returns the element type; overridden here because it is also used by AnnotationGenManager. */
  @Override
  public @Nullable ElementType getElementType() {
    return elementType;
  }

  /** Generates the annotation string after validating the element type and running checks. */
  public String generateString(ElementType elementType) {
    // チェック
    checkIfElementTypeAvailable(elementType);
    check();

    // 文字列生成
    ParamGen paramGen = getParamGen();
    if (paramGen == null || paramGen.generateString().equals("")) {
      return "@" + annotationName;
    } else {
      return "@" + annotationName + "(" + paramGen.generateString() + ")";
    }
  }

  /** Returns the parameter generator specific to this annotation. */
  protected abstract @Nullable ParamGen getParamGen();

  /** Performs any additional annotation-specific checks; subclasses should override when needed. */
  protected abstract void check();

  /**
   * Verifies that the given element type is supported by this annotation generator.
   *
   * @param elementType elementType
   */
  protected void checkIfElementTypeAvailable(ElementType elementType) {
    if (!Arrays.asList(getAvailableElmentTypes()).contains(elementType)) {
      new Violations().add(new BusinessViolation("MSG_ERR_ANNOTATION_ELEMENT_TYPE_NOT_ALLOWED",
          info.getSystemName(), this.getClass().getSimpleName(), elementType.toString()))
          .throwIfAny();
    }
  }

  /**
   * Returns the element types (e.g., FIELD, METHOD) to which this annotation can be applied.
   */
  protected abstract ElementType[] getAvailableElmentTypes();
}
