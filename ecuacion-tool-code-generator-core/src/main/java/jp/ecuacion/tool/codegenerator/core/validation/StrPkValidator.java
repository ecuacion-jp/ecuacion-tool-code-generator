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
package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a string value represents a primary-key kind in the code-generator convention:
 * null, empty, {@code S}, or {@code U}.
 */
public class StrPkValidator implements ConstraintValidator<StrPk, String> {

  @SuppressWarnings("null")
  @Override
  public void initialize(StrPk constraintAnnotation) {}

  @SuppressWarnings("null")
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return (value == null || value.equals("") || value.equals("S")
        || value.equals("U"));
  }

}
