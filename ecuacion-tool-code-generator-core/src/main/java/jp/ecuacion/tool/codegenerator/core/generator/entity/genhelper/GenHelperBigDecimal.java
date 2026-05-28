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
package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;

/**
 * Code generation helper for {@code BIG_DECIMAL}-type columns, which adds the
 * {@code java.math.BigDecimal} import.
 */
public class GenHelperBigDecimal extends GenHelperWrappedNumber {
  @Override
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    return mergeStrings(super.getNeededImports(columnInfo), "java.math.BigDecimal");
  }
}
