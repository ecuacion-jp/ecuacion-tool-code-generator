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
package jp.ecuacion.tool.codegenerator.core.util.reader;

/**
 * Utility methods for converting between boolean flags and the string marker used in Excel sheets.
 */
public class ReaderUtil {

  public static final String YES = "○";

  /**
   * Returns {@code true} if the given string equals the {@link #YES} marker; {@code false} for
   * {@code null} or any other value.
   */
  @SuppressWarnings("unused")
  public static boolean boolStrToBoolean(String boolStr) {
    if (boolStr == null) {
      return false;

    } else if (boolStr.equals(YES)) {
      return true;

    } else {
      return false;
    }
  }

  /**
   * Converts a boolean to the Excel marker string: {@link #YES} for {@code true}, empty string for
   * {@code false}.
   */
  public static String booleanToBoolStr(boolean bl) {
    return bl ? YES : "";
  }
}
