package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;

/** 
 * ListAnnotationGen以外の、Listでannotationを持たない普通のAnnotationのgenerator.
 */
public abstract class SingleAnnotationGen extends AnnotationGen {

  /** 通常のAnnotationを生成する際のコンストラクタ。 */
  public SingleAnnotationGen(String annotationName, ElementType elementType) {
    super(annotationName, elementType);
  }

  /** AnnotationGenManagerでも使うので、getterを作っておく。 */
  public ElementType getElementType() {
    return elementType;
  }

  public String generateString(ElementType elementType) throws BizLogicAppException {
    // チェック
    checkIfElementTypeAvailable(elementType);
    check();

    // 文字列生成
    if (getParamGen() == null || getParamGen().generateString().equals("")) {
      return "@" + annotationName;
    } else {
      return "@" + annotationName + "(" + getParamGen().generateString() + ")";
    }
  }

  /** annotation毎に固有なパラメータを取得するメソッド。 */
  protected abstract ParamGen getParamGen();

  /** 本メソッドを継承するオブジェクト内で追加でチェックすることがあれば継承。なくても一応継承して^^;。 */
  protected abstract void check();

  /**
   * 指定のElementTypeをサポートしていることを確認。
   *
   * @param elementType elementType
   */
  protected void checkIfElementTypeAvailable(ElementType elementType) throws BizLogicAppException {
    if (!Arrays.asList(getAvailableElmentTypes()).contains(elementType)) {
      throw new BizLogicAppException("MSG_ERR_ANNOTATION_ELEMENT_TYPE_NOT_ALLOWED",
          info.systemName, this.getClass().getSimpleName(), elementType.toString());
    }
  }

  /**
   * 使用可能なElementType(FIELD, METHODなど）を設定。 インスタンスごとに代わるものではないが、わかりやすく継承して持たせたいのでインスタンスメソッドにする
   */
  protected abstract ElementType[] getAvailableElmentTypes();
}
