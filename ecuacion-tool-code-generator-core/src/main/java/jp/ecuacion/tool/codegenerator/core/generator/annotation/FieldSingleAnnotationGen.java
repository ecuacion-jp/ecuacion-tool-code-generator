package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/**
 * Fieldに付加するannotationのgenerator.
 */
public abstract class FieldSingleAnnotationGen extends SingleAnnotationGen {

  public FieldSingleAnnotationGen(String annotationName, ElementType elementType) {
    super(annotationName, elementType);
  }

  /** 
   * 複数のvalidatorを組み合わせて一つのvalidatorを生成する場合に、TYPEが必要になるので追加。
   */
  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE};
  }

  /**
   * annotation / validatorを付加可能なjavaデータ型を定義。 この中に入っていないものは例外をあげ処理を終了。
   * インスタンスごとに代わるものではないが、わかりやすく継承して持たせたいのでインスタンスメソッドにする
   */
  protected abstract DataTypeKataEnum[] getAvailableKatas();
}
