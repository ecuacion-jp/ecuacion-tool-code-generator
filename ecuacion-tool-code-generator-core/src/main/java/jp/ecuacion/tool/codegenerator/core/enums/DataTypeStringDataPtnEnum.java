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
import jp.ecuacion.lib.core.util.PropertiesFileUtil;


/**
 * Data pattern for varchar-type columns.<br>
 * When referencing from an individual project, refer to the dataType rather than the enum alone.
 */
public enum DataTypeStringDataPtnEnum {

  /**
   * Full-width and half-width characters (no restriction).
   */
  REG_EX_ALL("001"),

  /**
   * Full-width characters only.
   */
  REG_EX_HAN("100"),

  /**
   * Half-width characters only.
   */
  REG_EX_HAN_NUM("101"),

  /**
   * Half-width digits only.
   */
  REG_EX_HAN_UC("102"),

  /**
   * Uppercase ASCII letters only.
   */
  REG_EX_HAN_UC_US("103"),

  /**
   * Uppercase ASCII letters and underscore.
   */
  REG_EX_HAN_LC("104"),

  /**
   * Lowercase ASCII letters only.
   */
  REG_EX_HAN_LC_US("105"),

  /**
   * Lowercase ASCII letters and underscore.
   */
  REG_EX_HAN_NUM_UC("106"),

  /**
   * Half-width digits and uppercase letters.
   */
  REG_EX_HAN_NUM_UC_US("107"),

  /**
   * Half-width digits, uppercase letters, and underscore.
   */
  REG_EX_HAN_NUM_LC("108"),

  /**
   * Half-width digits and lowercase letters.
   */
  REG_EX_HAN_NUM_LC_US("109"),

  /**
   * Half-width digits, lowercase letters, and underscore.
   */
  REG_EX_HAN_NUM_UC_LC("110"),

  /**
   * ASCII letters (upper and lower).
   */
  REG_EX_HAN_NUM_UC_LC_US("111"),

  /**
   * ASCII letters (upper and lower) and underscore.
   */
  REG_EX_ZEN("112");

  private final String code;

  private DataTypeStringDataPtnEnum(String code) {
    this.code = code;
  }

  /**
   * Returns the code. Null or empty code would cause a validation error at enum creation, so
   * it need not be considered here.
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the name. Null or empty name would cause a validation error at enum creation, so
   * it need not be considered here.
   */
  public String getName() {
    return this.toString();
  }

  /**
   * Returns the display name for use on screen. This name can be retrieved but cannot be used
   * to look up the enum value. Returned in the localized language.
   * Many sites are clearly built as Japanese-only, and in that case this approach is simpler.
   * May change at some point.
   */
  public String getDisplayName(Locale locale) {
    return PropertiesFileUtil.getEnumName(locale,
        DataTypeStringDataPtnEnum.class.getSimpleName() + "." + this.toString());
  }

  /**
   * Uses the default Locale.
   */
  public String getDisplayName() {
    return PropertiesFileUtil.getEnumName(Locale.getDefault(),
        DataTypeStringDataPtnEnum.class.getSimpleName() + "." + this.toString());
  }

  /**
   * Returns {@code true} if the given code matches one of the enum constants, {@code false}
   * otherwise.
   */
  public static boolean hasEnum(String code) {
    for (DataTypeStringDataPtnEnum enu : DataTypeStringDataPtnEnum.values()) {
      if (code.equals(enu.getCode())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns {@code true} if the given name exists in this enum, {@code false} otherwise.
   */
  public static boolean hasEnumFromName(String name) {
    for (DataTypeStringDataPtnEnum enu : DataTypeStringDataPtnEnum.values()) {
      if (name.equals(enu.getName())) {
        return true;
      }
    }

    return false;
  }
}
