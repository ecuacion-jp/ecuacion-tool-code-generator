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
package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/**
 * Generator for the Jakarta Bean Validation {@code @Valid} annotation, triggering cascaded
 * validation.
 */
public class ValidGen extends AbstractParameterlessAnnotationGen {

  /** Constructs a ValidGen for the given element type. */
  public ValidGen(ElementType elementType) {
    super("Valid", elementType);
  }

  /** Returns {@code true} since the {@code @Valid} annotation is always required. */
  public static boolean needsValidator(String columnName) {
    return true;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // All types are OK
    return DataTypeKataEnum.values();
  }
}
