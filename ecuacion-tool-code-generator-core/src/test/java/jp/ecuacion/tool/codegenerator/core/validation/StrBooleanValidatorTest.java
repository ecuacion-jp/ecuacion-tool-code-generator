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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link StrBooleanValidator}. */
@DisplayName("StrBooleanValidator")
public class StrBooleanValidatorTest {

  private final StrBooleanValidator sut = new StrBooleanValidator();
  @SuppressWarnings("null")
  private final ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

  @Nested
  @DisplayName("isValid()")
  class IsValid {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "○"})
    @DisplayName("null, empty string, and ○ are valid")
    void validValues(String value) {
      assertThat(sut.isValid(value, ctx)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "o", "●", "false", "true", " "})
    @DisplayName("any other value is invalid")
    void invalidValues(String value) {
      assertThat(sut.isValid(value, ctx)).isFalse();
    }
  }
}
