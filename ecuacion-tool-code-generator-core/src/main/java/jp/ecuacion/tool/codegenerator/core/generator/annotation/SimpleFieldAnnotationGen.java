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
package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
  * A convenience FieldSingleAnnotationGen for simple, parameter-free field annotations such as
  * {@code @Version}.
 */
public class SimpleFieldAnnotationGen extends FieldSingleAnnotationGen {

  /** Constructs a SimpleFieldAnnotationGen for the given annotation name. */
  public SimpleFieldAnnotationGen(String annotationName) {
    super(annotationName, ElementType.FIELD);
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return DataTypeKataEnum.values();
  }

  @Override
  protected ParamGen getParamGen() {
    return new ParamListGen();
  }

  @Override
  protected void check() {
  }
}
