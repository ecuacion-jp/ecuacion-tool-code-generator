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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link RelationKindEnum}. */
@DisplayName("RelationKindEnum")
public class RelationKindEnumTest {

  @Nested
  @DisplayName("getEnumFromName()")
  class GetEnumFromName {

    @Test
    @DisplayName("@OneToOne returns ONE_TO_ONE")
    void oneToOne() {
      assertThat(RelationKindEnum.getEnumFromName("@OneToOne")).isEqualTo(RelationKindEnum.ONE_TO_ONE);
    }

    @Test
    @DisplayName("@ManyToOne returns MANY_TO_ONE")
    void manyToOne() {
      assertThat(RelationKindEnum.getEnumFromName("@ManyToOne")).isEqualTo(RelationKindEnum.MANY_TO_ONE);
    }

    @Test
    @DisplayName("@OneToMany returns ONE_TO_MANY")
    void oneToMany() {
      assertThat(RelationKindEnum.getEnumFromName("@OneToMany")).isEqualTo(RelationKindEnum.ONE_TO_MANY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "OneToOne", "@oneToOne", "unknown"})
    @DisplayName("unrecognised string returns null")
    void unknownReturnsNull(String name) {
      assertThat(RelationKindEnum.getEnumFromName(name)).isNull();
    }

  }

  @Nested
  @DisplayName("getInverse()")
  class GetInverse {

    @Test
    @DisplayName("ONE_TO_ONE is its own inverse")
    void oneToOneInverse() {
      assertThat(RelationKindEnum.ONE_TO_ONE.getInverse()).isEqualTo(RelationKindEnum.ONE_TO_ONE);
    }

    @Test
    @DisplayName("MANY_TO_ONE inverts to ONE_TO_MANY")
    void manyToOneInverse() {
      assertThat(RelationKindEnum.MANY_TO_ONE.getInverse()).isEqualTo(RelationKindEnum.ONE_TO_MANY);
    }

    @Test
    @DisplayName("ONE_TO_MANY inverts to MANY_TO_ONE")
    void oneToManyInverse() {
      assertThat(RelationKindEnum.ONE_TO_MANY.getInverse()).isEqualTo(RelationKindEnum.MANY_TO_ONE);
    }
  }
}
