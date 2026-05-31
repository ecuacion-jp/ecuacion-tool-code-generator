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
 * The kind of a DataType.
 */
public enum DataTypeKataEnum {

  /**
   * String type.<br>
   * In PostgreSQL the official name is "character varying" (alias varchar), but the more
   * widely understood name is used here.<br>
   * PostgreSQL also has a "text" type, but since standard systems define an upper bound on
   * column size and there is no difference between text and varchar when an upper bound is
   * defined, a text type is not provided here.
   */
  STRING,

  /**
   * 1-byte signed integer.<br>
   * A 1-byte auto-increment integer does not exist in PostgreSQL.
   * PostgreSQL behaviour follows INT.<br>
   */
  BYTE,

  /**
   * 2-byte signed integer.<br>
   * A 2-byte auto-increment integer does not exist in PostgreSQL.
   * PostgreSQL behaviour follows INT.<br>
   */
  SHORT,

  /**
   * Integer — 4-byte signed integer.<br>
   * Corresponds to PostgreSQL INTEGER.<br>
   * A 4-byte auto-increment integer (serial) is not defined here; when the type is INTEGER
   * and "auto-number" is "○", the code generator replaces integer with serial.<br>
   * In PostgreSQL there is no way to specify the number of digits as in integer(2);
   * Integer is always 4 bytes.<br>
   * However, since the number of digits is always an issue in systems, the digit count is
   * a required specification for dataType.
   */
  INTEGER,

  /**
   * 8-byte signed integer.<br>
   * An 8-byte auto-increment integer (bigserial) is not defined here; when the type is
   * INTEGER and "auto-number" is "○", the code generator replaces integer with bigserial.<br>
   * PostgreSQL behaviour follows INT.<br>
   */
  LONG,

  /**
   * High-precision integer with selectable precision.
   * Equivalent to a BigDecimal with zero fractional digits, but BigDecimal is reserved for
   * decimal use while this type is used for integer use.
   */
  BIG_INTEGER,

  /**
   * Single-precision (4-byte) floating-point number.<br>
   * In PostgreSQL the official name is "real", but the more widely understood name is used here.
   */
  FLOAT,

  /**
   * Double-precision (8-byte) floating-point number.<br>
   * In PostgreSQL the official name is "double precision", but the more widely understood
   * name is used here.
   */
  DOUBLE,

  /**
   * High-precision numeric with selectable precision (numeric(p, s)).<br>
   * PostgreSQL also allows numeric and numeric(p), but within the framework all usage is in
   * the form "numeric(p, s)".<br>
   * p is the total number of digits (precision), s is the number of fractional digits (scale).<br>
   * The Java type for storing this value is BigDecimal. Both BigDecimal and numeric are
   * significantly slower in arithmetic than INT etc., so use them carefully.
   * Use this type without hesitation for monetary calculations involving decimal points.
   * Conversely, use BigInteger for all integer arithmetic without decimal points.
   * Java allows integer arithmetic with BigDecimal, but keeping the purposes cleanly
   * separated makes logic easier to build.
   * Currently, to enforce the above restriction, using BigDecimal with zero fractional digits
   * causes an error.
   */
  BIG_DECIMAL,

  TIMESTAMP,

  DATE, TIME, DATE_TIME,

  ENUM, BOOLEAN;

  /**
   * Returns the display name for use on screen. This name can be retrieved but cannot be used
   * to look up the enum value. Returned in the localized language.
   * Many sites are clearly built as Japanese-only, and in that case this approach is simpler.
   * May change at some point.
   */
  public String getDisplayName(Locale locale) {
    return PropertiesFileUtil.getEnumName(locale,
        this.getClass().getSimpleName() + "." + this.toString());
  }

  /**
   * Uses the default Locale.
   */
  public String getDisplayName() {
    return PropertiesFileUtil.getEnumName(Locale.getDefault(),
        this.getClass().getSimpleName() + "." + this.toString());
  }

  /**
   * Returns {@code true} if the given name exists in this enum, {@code false} otherwise.
   */
  public static boolean hasEnumFromName(String name) {
    for (DataTypeKataEnum enu : DataTypeKataEnum.values()) {
      if (name.equals(enu.toString())) {
        return true;
      }
    }

    return false;
  }
}
