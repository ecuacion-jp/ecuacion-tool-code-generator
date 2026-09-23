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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the "index1".."index10" value parsing (plain "1" vs. "U"-prefixed "U1" for a unique
 * index), exercised through the real string-parsing path rather than a mock.
 */
class DbOrClassColumnInfoTest {

  // Position of "index1" within DbOrClassColumnInfo.getFieldNameArray(); the other 9 index
  // columns immediately follow it (index2 = 22, ..., index10 = 30).
  private static final int INDEX1_POSITION = 21;
  private static final int FIELD_COUNT = 36;

  private DbOrClassColumnInfo colWithIndex1Value(String value) {
    List<String> values = new ArrayList<>(Collections.nCopies(FIELD_COUNT, ""));
    values.set(1, "TEST_COL"); // name
    values.set(INDEX1_POSITION, value); // index1
    return new DbOrClassColumnInfo(values);
  }

  @Nested
  @DisplayName("getIndex1() / isIndexUnique(1)")
  class Index1Parsing {

    @Test
    @DisplayName("a plain digit value is parsed as the position, and is not unique")
    void plainValue() {
      DbOrClassColumnInfo ci = colWithIndex1Value("2");

      assertThat(ci.getIndex1()).isEqualTo(2);
      assertThat(ci.isIndexUnique(1)).isFalse();
    }

    @Test
    @DisplayName("a \"U\"-prefixed value is parsed as the position, and is unique")
    void uPrefixedValue() {
      DbOrClassColumnInfo ci = colWithIndex1Value("U2");

      assertThat(ci.getIndex1()).isEqualTo(2);
      assertThat(ci.isIndexUnique(1)).isTrue();
    }

    @Test
    @DisplayName("an empty value means the column does not participate, and is not unique")
    void emptyValue() {
      DbOrClassColumnInfo ci = colWithIndex1Value("");

      assertThat(ci.getIndex1()).isNull();
      assertThat(ci.isIndexUnique(1)).isFalse();
    }

    @Test
    @DisplayName("an invalid value throws")
    void invalidValue() {
      DbOrClassColumnInfo ci = colWithIndex1Value("U");

      assertThatThrownBy(ci::getIndex1).isInstanceOf(IllegalArgumentException.class);
    }
  }
}
