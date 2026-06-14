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

import java.io.File;
import java.io.IOException;
import jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Detects whether an Excel input file uses a Japanese or English template by checking whether
 * the Japanese-named "General Settings" sheet exists.
 */
public class ExcelTemplateLanguageDetector {

  private static final String JA_GENERAL_SETTINGS_SHEET = "各種設定";

  /** Opens the workbook briefly to inspect sheet names, then returns the detected language. */
  public static ExcelTemplateLanguage detect(String excelPath) throws IOException {
    try (Workbook wb = WorkbookFactory.create(new File(excelPath))) {
      return wb.getSheet(JA_GENERAL_SETTINGS_SHEET) != null
          ? ExcelTemplateLanguage.JA
          : ExcelTemplateLanguage.EN;
    }
  }
}
