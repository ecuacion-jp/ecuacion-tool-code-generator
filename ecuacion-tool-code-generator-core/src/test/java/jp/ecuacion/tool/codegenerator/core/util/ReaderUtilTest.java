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
package jp.ecuacion.tool.codegenerator.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link ReaderUtil}. */
@DisplayName("ReaderUtil")
public class ReaderUtilTest {

  @Nested
  @DisplayName("boolStrToBoolean()")
  class BoolStrToBoolean {

    @Test
    @DisplayName("YES marker (○) returns true")
    void yesMarkerReturnsTrue() {
      assertThat(ReaderUtil.boolStrToBoolean(ReaderUtil.YES)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "o", "x", "true", "false", "○○"})
    @DisplayName("any other value returns false")
    void otherValuesReturnFalse(String value) {
      assertThat(ReaderUtil.boolStrToBoolean(value)).isFalse();
    }
  }

  @Nested
  @DisplayName("booleanToBoolStr()")
  class BooleanToBoolStr {

    @Test
    @DisplayName("true returns YES marker (○)")
    void trueReturnsYesMarker() {
      assertThat(ReaderUtil.booleanToBoolStr(true)).isEqualTo(ReaderUtil.YES);
    }

    @Test
    @DisplayName("false returns empty string")
    void falseReturnsEmptyString() {
      assertThat(ReaderUtil.booleanToBoolStr(false)).isEmpty();
    }
  }
}
