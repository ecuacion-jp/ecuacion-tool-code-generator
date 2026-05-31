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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * Validates that a string field contains only a valid primary-key marker: null, empty, {@code S}
  * (surrogate), or {@code U} (unique constraint).
 */
@Constraint(validatedBy = {StrPkValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StrPk {
  /** Returns the constraint violation message key. */
  String message() default "test.com.AssertOdd";

  /** Returns the validation groups this constraint belongs to. */
  Class<?>[] groups() default {};

  /** Returns the payload associated with this constraint. */
  Class<? extends Payload>[] payload() default {};

  /** Allows multiple {@link StrPk} constraints on the same element. */
  @interface List {
    /** Returns the repeated {@link StrPk} annotations. */
    StrPk[] values();
  }
}
