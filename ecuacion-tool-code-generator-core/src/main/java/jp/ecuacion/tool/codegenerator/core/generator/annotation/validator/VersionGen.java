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

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/** Generator for the JPA {@code @Version} annotation, used for optimistic locking. */
public class VersionGen extends AbstractParameterlessAnnotationGen {

  // The spec states that @Version applies to short, integer, long + Timestamp
  private static final DataTypeKataEnum[] AVAILABLE_KATAS =
      new DataTypeKataEnum[] {INTEGER, SHORT, LONG, TIMESTAMP, DATE_TIME};

  /** Constructs a VersionGen for the given element type. */
  public VersionGen(ElementType elementType) {
    super("Version", elementType);
  }

  /** Returns {@code true} if the column is used for optimistic locking. */
  public static boolean needsValidator(boolean isOptLock) {
    return isOptLock;
  }

  /** Returns {@code true} if the JPA spec allows {@code @Version} on this kata. */
  public static boolean isKataAllowed(DataTypeKataEnum kata) {
    return Arrays.asList(AVAILABLE_KATAS).contains(kata);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return AVAILABLE_KATAS;
  }
}
