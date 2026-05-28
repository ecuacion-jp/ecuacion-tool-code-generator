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

import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for dual-integer-parameter validator generators ({@code @Size}, {@code @Digits}).
 *
 * <p>Provides the shared constructor, {@code needsValidator(Integer, Integer)}, and
 * {@code getParamGenWithoutFieldId()} using the abstract {@link #getParam1Name()} and
 * {@link #getParam2Name()} for the annotation attribute names.
 */
public abstract class AbstractDualIntParamValidatorGen extends ValidatorGen {

  protected final @Nullable Integer param1;
  protected final @Nullable Integer param2;

  /** Constructs an instance with the annotation name, data type info, and the two bounds. */
  protected AbstractDualIntParamValidatorGen(String annotationName, DataTypeInfo dtInfo,
      @Nullable Integer param1, @Nullable Integer param2) {
    super(annotationName, dtInfo);
    this.param1 = param1;
    this.param2 = param2;
  }

  /** Returns {@code true} when at least one of the two bounds is non-null. */
  public static boolean needsValidator(@Nullable Integer p1, @Nullable Integer p2) {
    return !(p1 == null && p2 == null);
  }

  /** Returns the annotation attribute name for the first parameter (e.g. {@code "min"}). */
  protected abstract String getParam1Name();

  /** Returns the annotation attribute name for the second parameter (e.g. {@code "max"}). */
  protected abstract String getParam2Name();

  @SuppressWarnings("null")
  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    if (param1 != null) {
      plistGen.add(new ParamGenWithSingleValue(getParam1Name(), param1.toString(),
          DataTypeKataEnum.INTEGER));
    }
    if (param2 != null) {
      plistGen.add(new ParamGenWithSingleValue(getParam2Name(), param2.toString(),
          DataTypeKataEnum.INTEGER));
    }
  }
}
