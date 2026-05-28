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
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Abstract base for string validators that allow {@code null}, empty string, and a fixed set of
 * specific non-empty values.
 *
 * @param <A> the constraint annotation type
 */
public abstract class AbstractStrValueValidator<A extends Annotation>
    implements ConstraintValidator<A, String> {

  /** Returns the non-empty string values accepted by this validator. */
  protected abstract List<String> allowedNonEmptyValues();

  @Override
  public void initialize(A constraintAnnotation) {}

  @SuppressWarnings("null")
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.equals("") || allowedNonEmptyValues().contains(value);
  }
}
