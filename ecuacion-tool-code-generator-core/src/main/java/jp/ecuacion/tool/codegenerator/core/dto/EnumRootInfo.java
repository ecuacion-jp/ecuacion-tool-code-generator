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

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** Holds the root information for all enum definitions, including the list of enum classes. */
@SuppressWarnings("NullAway.Init")
public class EnumRootInfo extends AbstractRootInfo {

  private String dataTypeNamePrefix;
  private List<String> dispNameLangArr = new ArrayList<String>();

  @Valid
  public List<EnumClassInfo> enumClassList = new ArrayList<EnumClassInfo>();

  /** Constructs an empty instance representing the enum root info data kind. */
  @SuppressWarnings("null")
  public EnumRootInfo() {
    super(DataKindEnum.ENUM);
  }

  // dataTypeNamePrefix
  public String getDataTypeNamePrefix() {
    return dataTypeNamePrefix;
  }

  public void setDataTypeNamePrefix(String dataTypeNamePrefix) {
    this.dataTypeNamePrefix = dataTypeNamePrefix;
  }

  // dispNameLang
  public List<String> getDisplayNameLangArr() {
    return dispNameLangArr;
  }

  public void setUserFriendlyNameLangArr(List<String> dispNameLangArr) {
    this.dispNameLangArr = dispNameLangArr;
  }

  /** Adds a language code to the list of display-name languages. */
  public void addDispNameLang(String lang) {
    dispNameLangArr.add(lang);
  }
  
  @Override
  public boolean isDefined() {
    return enumClassList.size() > 0;
  }

  @Override
  public void consistencyCheckAndCoplementData() {
    new Violations()
        .addAll(Validation.buildDefaultValidatorFactory().getValidator().validate(this))
        .throwIfAny();
    
    for (EnumClassInfo info : enumClassList) {
      info.afterReading();
    }
  }
}
