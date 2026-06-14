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
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;

/**
 * Reads the common DB column definition sheet and converts it into a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public class ExcelDbCommonReader extends ExcelAbstractDbOrClassReader {

  private static final String SHEET_NAME_JA = "DB共通項目定義";
  private static final String SHEET_NAME_EN = "DB Common Item Definition";

  /** Constructs an instance 
   *     that targets the common DB item-definition sheet for the given language. */
  public ExcelDbCommonReader(SystemCommonRootInfo info, ExcelTemplateLanguage lang) {
    super(lang == ExcelTemplateLanguage.JA ? SHEET_NAME_JA : SHEET_NAME_EN, DataKindEnum.DB_COMMON,
        info, lang);
  }
}
