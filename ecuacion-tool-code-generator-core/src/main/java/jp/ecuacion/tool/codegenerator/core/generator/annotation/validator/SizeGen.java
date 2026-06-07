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
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;

import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

/**
 * Generator for the {@code @SizeString} validator annotation, constraining the minimum and maximum
 * length of a string or BigDecimal field.
 */
public class SizeGen extends ValidatorGen {

  private @Nullable Integer minSize;
  private @Nullable Integer maxSize;

  /** Builds a {@code Size} validator generator with the optional min/max bounds. */
  public SizeGen(DataTypeInfo dtInfo, @Nullable Integer minSize, @Nullable Integer maxSize) {
    super("SizeString", dtInfo);
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  /** Returns whether a {@code Size} validator should be generated for the given bounds. */
  public static boolean needsValidator(@Nullable Integer minSize, @Nullable Integer maxSize) {
    return !(minSize == null && maxSize == null);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {STRING, BIG_DECIMAL};
  }


  /** Adds min and/or max size parameters to the given parameter list generator. */
  @SuppressWarnings("null")
  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {

    if (minSize != null) {
      plistGen
          .add(new ParamGenWithSingleValue("min", minSize.toString(), DataTypeKataEnum.INTEGER));
    }

    if (maxSize != null) {
      plistGen
          .add(new ParamGenWithSingleValue("max", maxSize.toString(), DataTypeKataEnum.INTEGER));
    }
  }

  @Override
  public boolean isJakartaEeStandardValidator() {
    return false;
  }
}
