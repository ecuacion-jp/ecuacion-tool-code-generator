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

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for annotation generators that produce no-parameter annotations (e.g.
 * {@code @Id}, {@code @Version}, {@code @Valid}).
 *
 * <p>Provides the shared {@link #getParamGen()} (returns {@code null}) and {@link #check()}
 * (no-op) implementations. Concrete classes still define {@link #getAvailableKatas()} and may
 * override {@link #getAvailableElmentTypes()}.
 */
public abstract class AbstractParameterlessAnnotationGen extends FieldSingleAnnotationGen {

  /** Constructs an instance with the given annotation name and element type. */
  protected AbstractParameterlessAnnotationGen(String annotationName,
      @Nullable ElementType elementType) {
    super(annotationName, elementType);
  }

  @Override
  protected @Nullable ParamListGen getParamGen() {
    return null;
  }

  @Override
  protected void check() {}
}
