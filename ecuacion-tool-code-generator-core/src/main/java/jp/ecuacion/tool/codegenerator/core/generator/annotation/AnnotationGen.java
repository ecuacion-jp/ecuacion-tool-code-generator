package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;
import org.jspecify.annotations.Nullable;

/**
  * Abstract base class for all annotation generators, parent of SingleAnnotationGen and
  * ListAnnotationGen.
 */
public abstract class AnnotationGen extends ToolForCodeGen {
  /** Holds the annotation name as a String. */
  protected String annotationName;
  /** Holds the element type; retained for potential future use even when not strictly required. */
  protected @Nullable ElementType elementType;

  /** Constructs an AnnotationGen with the given annotation name and element type. */
  protected AnnotationGen(String annotationName, @Nullable ElementType elementType) {
    this.annotationName = annotationName;
    this.elementType = elementType;
  }

  /**
   * Generates the annotation string for the given element type.
   *
   * @param elementType the element type to generate the annotation for
   * @return the annotation source string
   */
  public abstract String generateString(ElementType elementType);

  /** Returns the annotation name. */
  public String getAnnotationName() {
    return annotationName;
  }

  /** Returns the element type associated with this annotation generator. */
  public @Nullable ElementType getElementType() {
    return elementType;
  }
}
