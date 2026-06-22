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

import jakarta.validation.constraints.Pattern;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Holds soft-delete settings, including the delete-flag column, initial and updated values.
 */
@SuppressWarnings("NullAway.Init")
public class MiscSoftDeleteRootInfo extends AbstractColAttrRootInfo {

  @Pattern(regexp = Constants.REG_EX_AL_NUM_DOT)
  private String initialValue;
  @Pattern(regexp = Constants.REG_EX_AL_NUM_DOT)
  private String updatedValue;

  /** Constructs an empty instance used when no XML file is provided for soft-delete settings. */
  @SuppressWarnings("null")
  public MiscSoftDeleteRootInfo() {
    super(DataKindEnum.MISC_REMOVED_DATA);
  }

  /** Constructs an instance with all soft-delete settings populated. */
  public MiscSoftDeleteRootInfo(String columnName, String dataTypeName, String initialValue,
      String updatedValue) {

    super(DataKindEnum.MISC_REMOVED_DATA, columnName, dataTypeName);

    this.initialValue = initialValue;
    this.updatedValue = updatedValue;
  }

  public String getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(String initialValue) {
    this.initialValue = initialValue;
  }

  public String getUpdatedValue() {
    return updatedValue;
  }

  public void setUpdatedValue(String updatedValue) {
    this.updatedValue = updatedValue;
  }

  @Override
  public void consistencyCheckAndCoplementData() {}
}
