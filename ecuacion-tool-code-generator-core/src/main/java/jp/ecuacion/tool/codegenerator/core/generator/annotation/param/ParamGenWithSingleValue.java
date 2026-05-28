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
package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/** A ParamGen implementation that generates a single key-value annotation parameter. */
public class ParamGenWithSingleValue extends ParamGen {
  private String key;
  private String value;
  private boolean isStringLiteral;

  /** Constructs an instance, deriving whether the value is a string literal from the given kata. */
  public ParamGenWithSingleValue(String key, String value, DataTypeKataEnum kata) {
    this.key = key;
    this.value = value;
    isStringLiteral = (kata == DataTypeKataEnum.STRING);
  }

  /**
   * Constructs an instance with an explicit flag indicating whether the value is a string literal.
   */
  public ParamGenWithSingleValue(String key, String value, boolean isStringLiteral) {
    this.key = key;
    this.value = value;
    this.isStringLiteral = isStringLiteral;
  }

  @Override
  public String generateString() {
    String outputValue;
    if (isStringLiteral) {
      outputValue = "\"" + value + "\"";

    } else {
      outputValue = value;
    }

    return key + " = " + outputValue;
  }
}

