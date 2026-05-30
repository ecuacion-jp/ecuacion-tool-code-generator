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
package jp.ecuacion.tool.codegenerator.core.util.generator;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests for {@link ColumnGenUtil}. */
@DisplayName("ColumnGenUtil")
public class CodeGenUtilTest {

  private final ColumnGenUtil sut = new ColumnGenUtil();

  @Nested
  @DisplayName("uncapitalCamel()")
  class UncapitalCamel {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("converts snake or camel input to lower-camel")
    void converts(String input, String expected) {
      assertThat(sut.uncapitalCamel(input)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("ACC_GROUP", "accGroup"),
          Arguments.of("acc_group", "accGroup"),
          Arguments.of("acc",       "acc"),
          Arguments.of("AccGroup",  "accGroup"),
          Arguments.of("accGroup",  "accGroup"));
    }
  }

  @Nested
  @DisplayName("capitalCamel()")
  class CapitalCamel {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("converts snake or camel input to upper-camel")
    void converts(String input, String expected) {
      assertThat(sut.capitalCamel(input)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("ACC_GROUP", "AccGroup"),
          Arguments.of("acc_group", "AccGroup"),
          Arguments.of("acc",       "Acc"),
          Arguments.of("AccGroup",  "AccGroup"),
          Arguments.of("accGroup",  "AccGroup"));
    }
  }

  @Nested
  @DisplayName("dataTypeNameToCapitalCamel()")
  class DataTypeNameToCapitalCamel {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("strips the DT_ prefix and converts the remainder to upper-camel")
    void converts(String input, String expected) {
      assertThat(sut.dataTypeNameToCapitalCamel(input)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("DT_STRING",       "String"),
          Arguments.of("DT_ACC_GROUP",    "AccGroup"),
          Arguments.of("DT_MY_DATA_TYPE", "MyDataType"));
    }
  }
}
