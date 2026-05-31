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

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;

/**
 * A ParamGen implementation that generates an annotation parameter holding multiple values as an
 * array.
 */
@SuppressWarnings("NullAway.Init")
public class ParamGenWithMultipleValues extends ParamGen {
  private String key;
  private String[] values;
  private AnnotationGen[] annotations;
  private boolean isStringLiteral;

  /**
   * Constructs an instance with an array of string values, optionally treating them as string
   * literals.
   */
  @SuppressWarnings("null")
  public ParamGenWithMultipleValues(String key, String[] values, boolean isStringLiteral) {
    this.key = key;
    this.values = values;
    this.isStringLiteral = isStringLiteral;
  }

  /** Constructs an instance where the values are annotation instances rather than plain strings. */
  @SuppressWarnings("null")
  public ParamGenWithMultipleValues(String key, AnnotationGen[] annotations) {
    this.key = key;
    this.annotations = annotations;
    this.isStringLiteral = false;
  }

  @Override
  public String generateString() {
    String literalChar = (isStringLiteral) ? "\"" : "";
    boolean usesAnnotation = (annotations != null);
    int length = (usesAnnotation) ? annotations.length : values.length;
    String outputValue = "";
    boolean is1stTime = true;
    for (int i = 0; i < length; i++) {
      String value =
          (usesAnnotation) ? annotations[i].generateString(ElementType.FIELD) : values[i];
      String comma = (is1stTime) ? "" : ", ";
      if (is1stTime) {
        is1stTime = false;
      }

      outputValue += comma + literalChar + value + literalChar;
    }
    outputValue = "{" + outputValue + "}";
    return key + " = " + outputValue;
  }
}

