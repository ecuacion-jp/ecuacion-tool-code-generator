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
package jp.ecuacion.tool.codegenerator.core.enums;

import java.util.Locale;

/** Identifies the language of an Excel input template (Japanese or English). */
public enum ExcelTemplateLanguage {
  JA, EN;

  private static final ThreadLocal<ExcelTemplateLanguage> current = new ThreadLocal<>();

  /** Sets the Excel template language for the current thread. */
  public static void setCurrent(ExcelTemplateLanguage lang) {
    current.set(lang);
  }

  /**
   * Returns the Excel template language for the current thread, or {@code JA} if not set.
   */
  public static ExcelTemplateLanguage getCurrent() {
    ExcelTemplateLanguage lang = current.get();
    return lang != null ? lang : JA;
  }

  /** Returns the {@link Locale} that corresponds to this template language. */
  public Locale toLocale() {
    return this == JA ? Locale.JAPANESE : Locale.ENGLISH;
  }
}
