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
package jp.ecuacion.tool.codegenerator.core.enums;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link DataTypeStringDataPtnEnum}. */
@DisplayName("DataTypeStringDataPtnEnum")
public class DataTypeStringDataPtnEnumTest {

  @Nested
  @DisplayName("hasEnum() — code-based lookup")
  class HasEnum {

    @ParameterizedTest
    @EnumSource(DataTypeStringDataPtnEnum.class)
    @DisplayName("every declared constant is found by its own code")
    void allConstantsAreFound(DataTypeStringDataPtnEnum ptn) {
      assertThat(DataTypeStringDataPtnEnum.hasEnum(ptn.getCode())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "000", "999", "REG_EX_ALL"})
    @DisplayName("unrecognised code returns false")
    void unknownCodeReturnsFalse(String code) {
      assertThat(DataTypeStringDataPtnEnum.hasEnum(code)).isFalse();
    }

  }

  @Nested
  @DisplayName("hasEnumFromName() — name-based lookup")
  class HasEnumFromName {

    @ParameterizedTest
    @EnumSource(DataTypeStringDataPtnEnum.class)
    @DisplayName("every declared constant is found by its own name")
    void allConstantsAreFound(DataTypeStringDataPtnEnum ptn) {
      assertThat(DataTypeStringDataPtnEnum.hasEnumFromName(ptn.getName())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "001", "UNKNOWN", "reg_ex_all"})
    @DisplayName("unrecognised or wrong-case string returns false")
    void unknownNameReturnsFalse(String name) {
      assertThat(DataTypeStringDataPtnEnum.hasEnumFromName(name)).isFalse();
    }

  }
}
