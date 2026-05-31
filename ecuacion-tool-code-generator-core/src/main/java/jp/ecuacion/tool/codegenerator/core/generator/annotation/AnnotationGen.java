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
import jp.ecuacion.tool.codegenerator.core.generator.AbstractCode;
import org.jspecify.annotations.Nullable;

/**
  * Abstract base class for all annotation generators, parent of SingleAnnotationGen and
  * ListAnnotationGen.
 */
public abstract class AnnotationGen extends AbstractCode {
  /** Holds the annotation name as a String. */
  protected String annotationName;
  /** Holds the element type; retained for potential future use even when not strictly required. */
  protected @Nullable ElementType elementType;

  /** Constructs an AnnotationGen with the given annotation name and element type. */
  protected AnnotationGen(String annotationName, @Nullable ElementType elementType) {
    this.annotationName = annotationName;
    this.elementType = elementType;
  }

  /**
   * Generates the annotation string for the given element type.
   *
   * @param elementType the element type to generate the annotation for
   * @return the annotation source string
   */
  public abstract String generateString(ElementType elementType);

  /** Returns the annotation name. */
  public String getAnnotationName() {
    return annotationName;
  }

  /** Returns the element type associated with this annotation generator. */
  public @Nullable ElementType getElementType() {
    return elementType;
  }
}
