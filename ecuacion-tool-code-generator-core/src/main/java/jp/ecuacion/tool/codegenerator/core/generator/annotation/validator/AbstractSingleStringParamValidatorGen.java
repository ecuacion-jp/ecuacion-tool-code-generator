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

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;

import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
 * Abstract base for single-string-parameter validator generators ({@code @DecimalMax},
 * {@code @DecimalMin}).
 *
 * <p>Provides the common {@code getAvailableKatas()}, {@code isJakartaEeStandardValidator()},
 * {@code needsValidator(String)}, and {@code getParamGenWithoutFieldId()} implementations.
 * The concrete value is stored in {@link #paramValue}.
 */
public abstract class AbstractSingleStringParamValidatorGen extends ValidatorGen {

  protected final String paramValue;

  /** Constructs an instance with the annotation name, data type info, and the parameter value. */
  protected AbstractSingleStringParamValidatorGen(String annotationName, DataTypeInfo dtInfo,
      String paramValue) {
    super(annotationName, dtInfo);
    this.paramValue = paramValue;
  }

  /** Returns {@code true} when the given value is non-null and non-empty. */
  public static boolean needsValidator(String value) {
    return value != null && !value.equals("");
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL, STRING};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    plistGen.add(new ParamGenWithSingleValue("value", paramValue, STRING));
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }
}
