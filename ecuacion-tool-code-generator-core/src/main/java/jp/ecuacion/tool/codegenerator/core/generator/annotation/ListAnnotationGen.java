package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;

/**
 * 以下のような、Listでannotationを引数で持つパターンのannotationを作成する場合のgenerator.
 * 
 * <p>
 * &#64;FieldPattern.List({ &#64;FieldPattern(...), &#64;FieldPattern(...) })
 * </p>
 */
public class ListAnnotationGen extends AnnotationGen {

  SingleAnnotationGen[] annotationGens;

  public ListAnnotationGen(String annotationName, ElementType elementType,
      SingleAnnotationGen... singleAnnotationGens) {
    super(annotationName, elementType);
    this.annotationGens = singleAnnotationGens;
  }

  @Override
  public String generateString(ElementType elementType) throws BizLogicAppException {

    StringBuilder sb = new StringBuilder();

    // springだと、@Pattern.Listの形式の書き方がエラーになった。
    // 通常のjakartaEEでも、@Pattern.Listを使用せず@Patternを複数併記する方式がOKになっているはずだが
    // 念の為springか否かで作成方法を分けておく
    if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
      boolean is1st = true;
      for (SingleAnnotationGen gen : annotationGens) {
        if (is1st) {
          is1st = false;

        } else {
          sb.append(RT);
        }

        sb.append(gen.generateString(elementType));
      }

    } else {
      sb.append("@" + annotationName + ".List({" + RT);

      for (int i = 0; i < annotationGens.length; i++) {
        SingleAnnotationGen gen = annotationGens[i];
        sb.append(T3 + gen.generateString(elementType)
            + ((i == annotationGens.length - 1) ? "" : ",") + RT);
      }

      sb.append(T1 + "})");
    }

    return sb.toString();
  }
}
