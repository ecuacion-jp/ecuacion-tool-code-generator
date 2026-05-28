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

/** Identifies the kind of definition file (e.g. DB, data type, enum, system common). */
public enum DataKindEnum {
  //@formatter:off
  SYSTEM_COMMON, DATA_TYPE, ENUM, DB, DB_COMMON, 
  MISC_REMOVED_DATA, MISC_GROUP, MISC_OPTIMISTIC_LOCK, OTHER;
  //@formatter:on

  public String getLabel() {
    return PropertiesFileUtil.getEnumName(Locale.getDefault(),
        this.getClass().getSimpleName() + "." + this.toString());
  }
}
