/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;

/**
 * Generator for annotations that hold a list of annotations as arguments, as in the following
 * pattern:
 *
 * <p>&#64;FieldPattern.List({ &#64;FieldPattern(...), &#64;FieldPattern(...) })
 * </p>
 */
public class ListAnnotationGen extends AnnotationGen {

  SingleAnnotationGen[] annotationGens;

  /**
   * Constructs a ListAnnotationGen with the given annotation name, element type, and component
   * annotation generators.
   */
  public ListAnnotationGen(String annotationName, ElementType elementType,
      SingleAnnotationGen... singleAnnotationGens) {
    super(annotationName, elementType);
    this.annotationGens = singleAnnotationGens;
  }

  @Override
  public String generateString(ElementType elementType) {

    StringBuilder sb = new StringBuilder();

    // In Spring, writing @Pattern.List causes an error.
    // Even in standard Jakarta EE, listing multiple @Pattern annotations without @Pattern.List
    // should be valid, but the generation method is separated by Spring vs non-Spring as a
    // precaution.
    if (info.getSysCmnRootInfo().isFrameworkKindSpring()) {
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
