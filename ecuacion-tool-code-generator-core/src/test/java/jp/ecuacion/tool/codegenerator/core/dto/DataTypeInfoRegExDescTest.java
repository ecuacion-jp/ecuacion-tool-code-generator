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
package jp.ecuacion.tool.codegenerator.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jp.ecuacion.tool.codegenerator.core.validation.CrossSheetConsistencyCheckGroup;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests the checks on the regex descriptions of {@link DataTypeInfo}:
 *     the description is optional, but must be blank without the regex,
 *     and the descriptions of the support languages follow that of the default language.
 */
@DisplayName("DataTypeInfo - regex descriptions")
class DataTypeInfoRegExDescTest {

  private static final Validator VALIDATOR =
      Validation.buildDefaultValidatorFactory().getValidator();

  private static final int FIELD_COUNT = 20;
  private static final int REG_EX = 7;
  private static final int DESC_DEFAULT = 16;
  private static final int DESC_SUPPORT_01 = 17;

  /** Creates a STRING dataType. {@code null} arguments mean blank cells. */
  static DataTypeInfo dataType(@Nullable String regEx, @Nullable String descDefault,
      @Nullable String descSupport01) {
    List<@Nullable String> values = new ArrayList<>(Collections.nCopies(FIELD_COUNT, null));
    values.set(0, "DT_TEST");
    values.set(1, "STRING");
    values.set(3, "10");
    values.set(REG_EX, regEx);
    values.set(DESC_DEFAULT, descDefault);
    values.set(DESC_SUPPORT_01, descSupport01);
    @SuppressWarnings("null")
    DataTypeInfo dt = new DataTypeInfo(values);
    return dt;
  }

  /** Creates a system-common info with default language "en" and support language 1. */
  @SuppressWarnings({"null", "NullAway"})
  static SystemCommonRootInfo sysCmn(@Nullable String supportLang1) {
    return new SystemCommonRootInfo("5.0.0", "test", "jp.ecuacion.test", "spring", "UTF-8", "en",
        supportLang1, null, null, null, null, null, null, null);
  }

  /*
   * The checks are class-level constraints, whose target properties are not in the property path
   * of the violation but in the "propertyPath" attribute of the constraint.
   */
  private static Set<String> violatedPaths(Set<? extends ConstraintViolation<?>> violations) {
    return violations.stream()
        .map(v -> v.getConstraintDescriptor().getAttributes().get("propertyPath"))
        .flatMap(p -> p instanceof String[] arr ? Arrays.stream(arr) : Stream.of(String.valueOf(p)))
        .collect(Collectors.toSet());
  }

  @Nested
  @DisplayName("default language")
  class DefaultLang {

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {
        // regEx, descDefault, violated
        "^[0-9]*$, digits, false",
        "^[0-9]*$, null, false", // optional since ADR-0022 of ecuacion-lib
        "null, null, false",
        "null, digits, true"})
    @DisplayName("the description is optional, but must be blank without the regex")
    void check(@Nullable String regEx, @Nullable String descDefault, boolean violated) {
      Set<String> paths = violatedPaths(VALIDATOR.validate(dataType(regEx, descDefault, null)));

      assertThat(paths.contains("stringRegExDescLangDefault")).isEqualTo(violated);
    }
  }

  @Nested
  @DisplayName("support languages")
  class SupportLang {

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {
        // supportLang1, descDefault, descSupport01, violated
        "ja, digits, 数字, false",
        "ja, digits, null, true",
        "ja, null, null, false",
        "ja, null, 数字, true",
        "null, digits, null, false",
        "null, digits, 数字, true"})
    @DisplayName("required exactly when the default description and the language are configured")
    void check(@Nullable String supportLang1, @Nullable String descDefault,
        @Nullable String descSupport01, boolean violated) {
      DataTypeInfo dt = dataType("^[0-9]*$", descDefault, descSupport01);
      dt.setSysCmnRootInfo(sysCmn(supportLang1));

      Set<String> paths =
          violatedPaths(VALIDATOR.validate(dt, CrossSheetConsistencyCheckGroup.class));

      assertThat(paths.contains("stringRegExDescLangSupport01")).isEqualTo(violated);
    }
  }
}
