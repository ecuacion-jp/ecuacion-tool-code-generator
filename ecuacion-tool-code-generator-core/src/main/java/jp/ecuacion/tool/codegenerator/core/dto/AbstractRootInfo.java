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
import org.jspecify.annotations.Nullable;

/**
 * Base class for all root info objects, holding the data kind and providing common lifecycle
 * hooks.
 */
public abstract class AbstractRootInfo extends AbstractInfo {
  protected DataKindEnum fileKind;

  /** The name of the Excel sheet this root info was read from, used in validation messages. */
  protected @Nullable String sheetName;

  /** Constructs an instance with the given data kind. */
  public AbstractRootInfo(DataKindEnum fileKind) {
    this(fileKind, true);
  }

  /** Constructs an instance with the given data kind and optional {@code isDefined} flag. */
  public AbstractRootInfo(DataKindEnum fileKind, boolean isDefined) {
    this.fileKind = fileKind;
  }

  /** Returns whether this root info has been defined. */
  public abstract boolean isDefined();

  /** Returns the {@link DataKindEnum} that identifies the data kind this root info represents. */
  public DataKindEnum getFileKind() {
    return fileKind;
  }

  /** Returns the name of the Excel sheet this root info was read from. */
  public @Nullable String getSheetName() {
    return sheetName;
  }

  /** Sets the name of the Excel sheet this root info was read from. */
  public void setSheetName(String sheetName) {
    this.sheetName = sheetName;
  }
  
  /**
   * Performs checks on data read from Excel that cannot be handled by Jakarta Validation,
   * such as conditionally required fields.
   */
  public abstract void consistencyCheckAndComplementData();
}
