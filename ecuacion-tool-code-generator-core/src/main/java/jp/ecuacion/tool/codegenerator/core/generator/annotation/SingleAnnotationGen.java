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
import java.util.Arrays;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import org.jspecify.annotations.Nullable;

/**
 * Generator for ordinary annotations that do not hold a list of annotations as arguments,
 * unlike {@link ListAnnotationGen}.
 */
public abstract class SingleAnnotationGen extends AnnotationGen {

  /** Constructor used when creating a normal annotation. */
  public SingleAnnotationGen(String annotationName, @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  /** Returns the element type; overridden here because it is also used by AnnotationGenManager. */
  @Override
  public @Nullable ElementType getElementType() {
    return elementType;
  }

  /** Generates the annotation string after validating the element type and running checks. */
  public String generateString(ElementType elementType) {
    // Validate
    checkIfElementTypeAvailable(elementType);
    check();

    // Generate string
    ParamGen paramGen = getParamGen();
    if (paramGen == null || paramGen.generateString().equals("")) {
      return "@" + annotationName;
    } else {
      return "@" + annotationName + "(" + paramGen.generateString() + ")";
    }
  }

  /** Returns the parameter generator specific to this annotation. */
  protected abstract @Nullable ParamGen getParamGen();

  /** Performs any additional annotation-specific checks; subclasses should override when needed. */
  protected abstract void check();

  /**
   * Verifies that the given element type is supported by this annotation generator.
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
   * Returns the element types (e.g., FIELD, METHOD) to which this annotation can be applied.
   */
  protected abstract ElementType[] getAvailableElmentTypes();
}
