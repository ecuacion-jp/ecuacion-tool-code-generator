package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ValidatorGen;

/** Utility methods for generating annotation code strings and resolving import requirements. */
public class AnnotationGenUtil extends ToolForCodeGen {

  /**
   * Returns the combined annotation code string for all entries in the list, indented for the given
   * element type.
   */
  public static String getCode(List<? extends AnnotationGen> annotationGenList,
      ElementType elementType) {
    List<String> strList = new ArrayList<>();
    annotationGenList.forEach(annotationGen -> {
      String tab = (elementType == ElementType.TYPE) ? "" : T1;
      strList.add(tab + annotationGen.generateString(elementType) + RT);
    });

    StringBuilder sb = new StringBuilder();
    strList.forEach(s -> sb.append(s));
    return sb.toString();
  }

  /** Returns the set of import strings required by the given list of validator generators. */
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
