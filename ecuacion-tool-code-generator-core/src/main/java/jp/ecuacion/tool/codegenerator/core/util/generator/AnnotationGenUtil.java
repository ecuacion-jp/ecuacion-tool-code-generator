package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ValidatorGen;

public class AnnotationGenUtil extends ToolForCodeGen {

  /** AnnotationGenのリストからコードを取得。 */
  public static String getCode(List<? extends AnnotationGen> annotationGenList,
      ElementType elementType) {
    List<String> strList = new ArrayList<>();
    annotationGenList.forEach(annotationGen -> {
      String tab = (elementType == ElementType.TYPE) ? "" : T1;
      try {
        strList.add(tab + annotationGen.generateString(elementType) + RT);
      } catch (BizLogicAppException ex) {
        throw new RuntimeException(ex);
      }
    });

    StringBuilder sb = new StringBuilder();
    strList.forEach(s -> sb.append(s));
    return sb.toString();
  }

  public static String[] getNeededImports(List<ValidatorGen> list) {
    Set<String> rtnSet = new HashSet<>();
    for (ValidatorGen an : list) {
      if (an.isJakartaEeStandardValidator()) {
        rtnSet.add(Constants.PKG_STANDARD_VALIDATOR);

      } else {
        rtnSet.add(Constants.PKG_CUSTOM_VALIDATOR);
      }
    }

    return rtnSet.toArray(new String[rtnSet.size()]);
  }
}
