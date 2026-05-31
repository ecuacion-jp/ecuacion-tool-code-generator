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
package jp.ecuacion.tool.codegenerator.core.generatorhelper.util;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests for {@link ColumnGenUtil}. */
@DisplayName("ColumnGenUtil")
public class ColumnGenUtilTest {

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

  // ---------- var helpers ----------

  @Nested
  @DisplayName("varIsNotNull()")
  class VarIsNotNull {

    @Test
    @DisplayName("appends != null to the given expression")
    void appendsNotNull() {
      assertThat(sut.varIsNotNull("foo")).isEqualTo("foo != null");
    }
  }

  @Nested
  @DisplayName("ifVarIsNotNull()")
  class IfVarIsNotNull {

    @Test
    @DisplayName("wraps expression in an if-not-null guard")
    void wrapsInIfGuard() {
      assertThat(sut.ifVarIsNotNull("foo")).isEqualTo("if (foo != null) ");
    }
  }

  @Nested
  @DisplayName("set()")
  class Set {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("generates a setter call from snake or camel column name")
    void generates(String fieldName, String argString, String expected) {
      assertThat(sut.set(fieldName, argString)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("my_col",  "val", "setMyCol(val)"),
          Arguments.of("myCol",   "val", "setMyCol(val)"),
          Arguments.of("MY_COL",  "val", "setMyCol(val)"));
    }
  }

  @Nested
  @DisplayName("baseRec()")
  class BaseRec {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("returns BaseRecord class name from snake or camel table name")
    void returns(String tableName, String expected) {
      assertThat(sut.baseRec(tableName)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("my_table",  "MyTableBaseRecord"),
          Arguments.of("myTable",   "MyTableBaseRecord"),
          Arguments.of("MY_TABLE",  "MyTableBaseRecord"));
    }
  }

  @Nested
  @DisplayName("baseRecDef()")
  class BaseRecDef {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("returns BaseRecord local variable declaration from snake or camel table name")
    void returns(String tableName, String expected) {
      assertThat(sut.baseRecDef(tableName)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("my_table",  "MyTableBaseRecord rec"),
          Arguments.of("myTable",   "MyTableBaseRecord rec"),
          Arguments.of("MY_TABLE",  "MyTableBaseRecord rec"));
    }
  }

  // ---------- rec helpers ----------

  @Nested
  @DisplayName("recGet()")
  class RecGet {

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("generates rec.getXxx() from snake or camel field name")
    void generates(String fieldName, String expected) {
      assertThat(sut.recGet(fieldName)).isEqualTo(expected);
    }

    static Stream<@NonNull Arguments> inputs() {
      return Stream.of(
          Arguments.of("my_col",  "rec.getMyCol()"),
          Arguments.of("myCol",   "rec.getMyCol()"),
          Arguments.of("MY_COL",  "rec.getMyCol()"));
    }
  }

  @Nested
  @DisplayName("recGetIsNull()")
  class RecGetIsNull {

    @Test
    @DisplayName("appends == null to the rec getter")
    void appendsIsNull() {
      assertThat(sut.recGetIsNull("my_col")).isEqualTo("rec.getMyCol() == null");
    }
  }

  @Nested
  @DisplayName("recGetIsNotNull()")
  class RecGetIsNotNull {

    @Test
    @DisplayName("appends != null to the rec getter")
    void appendsIsNotNull() {
      assertThat(sut.recGetIsNotNull("my_col")).isEqualTo("rec.getMyCol() != null");
    }
  }

  @Nested
  @DisplayName("ifRecGetIsNotNull()")
  class IfRecGetIsNotNull {

    @Test
    @DisplayName("wraps rec getter in an if-not-null guard")
    void wrapsInIfGuard() {
      assertThat(sut.ifRecGetIsNotNull("my_col")).isEqualTo("if (rec.getMyCol() != null) ");
    }
  }
}
