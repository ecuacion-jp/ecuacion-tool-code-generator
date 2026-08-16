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

import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * An instance of this class is always created and registered in the system map even when no
 * corresponding XML file is placed. Generated record classes unconditionally rely on an
 * optimistic-lock version column existing (see {@code PerTableBaseRecordGen}), so {@link
 * #consistencyCheckAndCoplementData()} rejects the case where it was left unconfigured.
 */
public class MiscOptimisticLockRootInfo extends AbstractColAttrRootInfo {

  /** Constructs an empty instance for optimistic locking, used when no XML file is placed. */
  public MiscOptimisticLockRootInfo() {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK);
  }

  /**
   * Constructs an instance with the given column name for optimistic locking.
   *
   * <p>No data type is taken here: {@code VersionGen} already validates, per column, that
   * whatever data type is used is one of the ones the JPA spec allows for {@code @Version}, so
   * requiring one single data type name to be shared system-wide would only be a redundant,
   * stricter constraint on top of that.</p>
   */
  @SuppressWarnings({"NullAway", "null"})
  public MiscOptimisticLockRootInfo(String columnName) {
    super(DataKindEnum.MISC_OPTIMISTIC_LOCK, columnName, null);
  }

  @Override
  public void consistencyCheckAndCoplementData() {
    if (!isDefined()) {
      new Violations().add(new BusinessViolation("MSG_ERR_OPTIMISTIC_LOCK_COLUMN_REQUIRED"))
          .throwIfAny();
    }
  }
}
