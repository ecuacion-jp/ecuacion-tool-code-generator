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
package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * An instance of this class is always created and registered in the system map even when no
 * corresponding XML file is placed. Whether the user has configured this setting can be
 * determined via {@code isDefined()}.
 */
public class MiscOptimisticLockRootInfo extends AbstractColAttrRootInfo {

  /** Constructs an empty instance for optimistic locking, used when no XML file is placed. */
  public MiscOptimisticLockRootInfo() {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK);
  }

  /**
   * Constructs an instance with the given column name and data type name for optimistic
   * locking.
   */
  public MiscOptimisticLockRootInfo(String columnName, String dataTypeName) {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK, columnName, dataTypeName);
  }

  @Override
  public void consistencyCheckAndCoplementData() {
  }
}
