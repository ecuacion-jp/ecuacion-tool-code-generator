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
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import org.jspecify.annotations.Nullable;

/**
 * Generator for annotations applied to fields.
 */
public abstract class FieldSingleAnnotationGen extends SingleAnnotationGen {

  /** Constructs a FieldSingleAnnotationGen with the given annotation name and element type. */
  public FieldSingleAnnotationGen(String annotationName, @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  /**
    * Returns the available element types, including TYPE in addition to FIELD to support combined
    * validators.
   */
  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE};
  }

  /**
   * Returns the Java data types to which this annotation or validator can be applied.
   * An exception is thrown if the actual type is not included in the returned array.
   */
  protected abstract DataTypeKataEnum[] getAvailableKatas();
}
