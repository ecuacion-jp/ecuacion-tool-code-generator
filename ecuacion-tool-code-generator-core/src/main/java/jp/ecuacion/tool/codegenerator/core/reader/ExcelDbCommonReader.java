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
package jp.ecuacion.tool.codegenerator.core.reader;

import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Reads the common DB column definition sheet and converts it into a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public class ExcelDbCommonReader extends ExcelAbstractDbOrClassReader {
  /** Constructs an instance that targets the common DB item-definition sheet. */
  public ExcelDbCommonReader(SystemCommonRootInfo info) {
    super("DB共通項目定義", DataKindEnum.DB_COMMON, info);
  }
}
