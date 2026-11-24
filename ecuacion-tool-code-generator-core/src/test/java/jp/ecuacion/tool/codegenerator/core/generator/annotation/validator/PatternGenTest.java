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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.util.ReaderUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link PatternGen}. */
@DisplayName("PatternGen")
class PatternGenTest {

  /** Creates a STRING dataType. {@code null} arguments mean blank cells. */
  private static DataTypeInfo dataType(@Nullable String stringDataPtn, @Nullable String regEx,
      @Nullable String descDefault) {
    List<@Nullable String> values = new ArrayList<>(Collections.nCopies(20, null));
    values.set(0, "DT_PHONE_NUMBER");
    values.set(1, "STRING");
    values.set(3, "10");
    values.set(5, stringDataPtn);
    // allows prohibited characters, so that no extra pattern is created.
    values.set(6, ReaderUtil.YES);
    values.set(7, regEx);
    values.set(16, descDefault);
    @SuppressWarnings("null")
    DataTypeInfo dt = new DataTypeInfo(values);
    return dt;
  }

  @SuppressWarnings({"null", "NullAway"})
  private static SystemCommonRootInfo sysCmn() {
    return new SystemCommonRootInfo("5.0.0", "test", "jp.ecuacion.test", "spring", "UTF-8", "en",
        null, null, null, null, null, null, null, null);
  }

  private static String params(ValidatorGen gen) {
    ParamListGen plistGen = new ParamListGen();
    gen.getParamGenWithoutFieldId(plistGen);
    return plistGen.generateString();
  }

  /** Returns the params of the PatternGens created from the dataType. */
  private static List<String> patternParams(DataTypeInfo dt) {
    dt.checksAndComplements(sysCmn());
    return dt.getValidatorList(true).stream().filter(v -> v instanceof PatternGen)
        .map(PatternGenTest::params).toList();
  }

  @Nested
  @DisplayName("getParamGenWithoutFieldId()")
  class GetParamGenWithoutFieldId {

    @Test
    @DisplayName("outputs description when it is given")
    void withDescription() {
      PatternGen gen = new PatternGen(dataType(null, null, null), "^[0-9]*$", "digits");

      assertThat(params(gen)).isEqualTo("regexp = \"^[0-9]*$\", description = \"digits\"");
    }

    @Test
    @DisplayName("omits description when it is null")
    void withoutDescription() {
      PatternGen gen = new PatternGen(dataType(null, null, null), "^[0-9]*$", null);

      assertThat(params(gen)).isEqualTo("regexp = \"^[0-9]*$\"");
    }
  }

  @Nested
  @DisplayName("created from DataTypeInfo")
  class FromDataTypeInfo {

    @Test
    @DisplayName("regex with the description uses the key derived from the dataType name")
    void regExWithDescription() {
      assertThat(patternParams(dataType(null, "^[0-9]*$", "digits")))
          .containsExactly("regexp = \"^[0-9]*$\", description = \"phoneNumber\"");
    }

    @Test
    @DisplayName("regex without the description omits description")
    void regExWithoutDescription() {
      assertThat(patternParams(dataType(null, "^[0-9]*$", null)))
          .containsExactly("regexp = \"^[0-9]*$\"");
    }

    @Test
    @DisplayName("data pattern uses its own key regardless of the regex description")
    void dataPattern() {
      assertThat(patternParams(dataType("REG_EX_HAN_NUM", "^[0-3]*$", null))).containsExactly(
          "regexp = \"^[0-9]*$\", description = \"dataPattern.REG_EX_HAN_NUM\"",
          "regexp = \"^[0-3]*$\"");
    }
  }
}
