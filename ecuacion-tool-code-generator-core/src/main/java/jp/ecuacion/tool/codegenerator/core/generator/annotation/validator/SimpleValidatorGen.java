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
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
 * A convenience ValidatorGen for simple, parameter-free validators such as {@code @IntegerString}.
 */
public class SimpleValidatorGen extends ValidatorGen {

  private boolean isJakartaEeStandardValidator;
  private DataTypeKataEnum[] availableKatas;

  /**
   * Constructs a SimpleValidatorGen with the annotation name, data type, Jakarta EE standard flag,
   * and allowed katas.
   */
  public SimpleValidatorGen(String annotationName, DataTypeInfo dtInfo,
      boolean isJakartaEeStandardValidator, DataTypeKataEnum[] availableKatas) {
    super(annotationName, dtInfo);

    this.isJakartaEeStandardValidator = isJakartaEeStandardValidator;
    this.availableKatas = availableKatas;
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return availableKatas;
  }

  @Override
  protected void check() {}

  @Override
  public boolean isJakartaEeStandardValidator() {
    return isJakartaEeStandardValidator;
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    
  }
}
