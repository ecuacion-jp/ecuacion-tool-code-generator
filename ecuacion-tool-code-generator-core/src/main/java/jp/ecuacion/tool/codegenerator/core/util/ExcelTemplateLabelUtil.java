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
package jp.ecuacion.tool.codegenerator.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Resolves enum/item labels that correspond to Excel template column and sheet names.
 *
 * <p>Unlike {@link jp.ecuacion.lib.core.util.PropertiesFileUtil}, which is locale-driven,
 * this utility selects labels based on the language of the Excel input template
 * ({@code *_for_xl_ja.properties} or {@code *_for_xl_en.properties}).
 * The active language is determined by the {@code Locale} derived from
 * {@link jp.ecuacion.tool.codegenerator.core.enums.ExcelTemplateLanguage#toLocale()}.</p>
 */
public class ExcelTemplateLabelUtil {

  private static final Properties JA_PROPS;
  private static final Properties EN_PROPS;

  static {
    JA_PROPS = load("enum_names_for_xl_ja", "item_names_for_xl_ja");
    EN_PROPS = load("enum_names_for_xl_en", "item_names_for_xl_en");
  }

  private ExcelTemplateLabelUtil() {}

  private static Properties load(String... fileNames) {
    Properties props = new Properties();
    for (String name : fileNames) {
      try (InputStream is =
          ExcelTemplateLabelUtil.class.getResourceAsStream("/" + name + ".properties")) {
        if (is != null) {
          props.load(is);
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to load " + name + ".properties", e);
      }
    }
    return props;
  }

  private static Properties resolve(Locale locale) {
    return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? EN_PROPS : JA_PROPS;
  }

  /** 
   * Returns the enum label for the given key, 
   *     using the locale derived from the Excel template language.
   */
  public static String getEnumName(Locale locale, String key) {
    return resolve(locale).getProperty(key, key);
  }

  /** 
   * Returns the item (field) label for the given key, 
   *     using the locale derived from the Excel template language.
   */
  public static String getItemName(Locale locale, String key) {
    return resolve(locale).getProperty(key, key);
  }
}
