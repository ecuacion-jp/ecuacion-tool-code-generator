package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import org.jspecify.annotations.Nullable;

/**
 * Fieldに付加するannotationのgenerator.
 */
public abstract class FieldSingleAnnotationGen extends SingleAnnotationGen {

  /** Constructs a FieldSingleAnnotationGen with the given annotation name and element type. */
  public FieldSingleAnnotationGen(String annotationName, @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  /**
    * Returns the available element types, including TYPE in addition to FIELD to support combined
    * validators.
   */
  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE};
  }

  /**
   * Returns the Java data types to which this annotation or validator can be applied.
   * An exception is thrown if the actual type is not included in the returned array.
   */
  protected abstract DataTypeKataEnum[] getAvailableKatas();
}
