package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;

/**
 * annotation generatorの一番の親。
 * 配下にSingleAnnotationGenと、ListAnnotationGenを持つ。
 * 
 */
public abstract class AnnotationGen extends ToolForCodeGen {
  /** アノテーション名をStringで保持。 */
  protected String annotationName;
  /** 現時点の仕様では、elementTypeは必ずしもなくとも実装は可能と思われるが、今後の必要性も考慮し念のため保持しておく。 */
  protected ElementType elementType;

  protected AnnotationGen(String annotationName, ElementType elementType) {
    this.annotationName = annotationName;
    this.elementType = elementType;
  }

  /**
   * コード生成のためのメソッド。
   *
   * @param elementType elementType
   */
  public abstract String generateString(ElementType elementType) throws BizLogicAppException;

  public String getAnnotationName() {
    return annotationName;
  }

  public ElementType getElementType() {
    return elementType;
  }
}
