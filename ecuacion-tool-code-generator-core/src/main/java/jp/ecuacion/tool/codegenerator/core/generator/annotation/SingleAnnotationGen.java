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

  /** 通常のAnnotationを生成する際のコンストラクタ。 */
  public SingleAnnotationGen(String annotationName, @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  /** AnnotationGenManagerでも使うので、getterを作っておく。 */
  @Override
  public @Nullable ElementType getElementType() {
    return elementType;
  }

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

  /** annotation毎に固有なパラメータを取得するメソッド。 */
  protected abstract @Nullable ParamGen getParamGen();

  /** 本メソッドを継承するオブジェクト内で追加でチェックすることがあれば継承。なくても一応継承して^^;。 */
  protected abstract void check();

  /**
   * 指定のElementTypeをサポートしていることを確認。
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
   * 使用可能なElementType(FIELD, METHODなど）を設定。 インスタンスごとに代わるものではないが、わかりやすく継承して持たせたいのでインスタンスメソッドにする
   */
  protected abstract ElementType[] getAvailableElmentTypes();
}
