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

import static org.assertj.core.api.Assertions.assertThat;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests for {@link DigitsGen}. */
@DisplayName("DigitsGen")
public class DigitsGenTest {

  @Nested
  @DisplayName("resolveFraction()")
  class ResolveFraction {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("uses 0 only when the integer is given without the fraction")
    void resolves(@Nullable Integer integer, @Nullable Integer fraction,
        @Nullable Integer expected) {
      assertThat(DigitsGen.resolveFraction(integer, fraction)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of(5, null, 0),
          Arguments.of(5, 2, 2),
          Arguments.of(null, null, null));
    }
  }
}
