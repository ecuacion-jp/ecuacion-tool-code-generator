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
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;


/** Holds the root information for all data type definitions. */
public class DataTypeRootInfo extends AbstractRootInfo {

  /** Constructs an instance from the given list of data type definitions. */
  public DataTypeRootInfo(List<DataTypeInfo> list) {
    super(DataKindEnum.DATA_TYPE);
    this.dataTypeList = list;
  }

  @Valid
  public List<DataTypeInfo> dataTypeList = new ArrayList<>();
  
  @Override
  public boolean isDefined() {
    return dataTypeList.size() > 0;
  }

  @Override
  public void consistencyCheckAndCoplementData() {
    // ExcelDataTypeReader uses PoiStringTableToBeanReader, which performs validation during
    // Excel reading, so no additional execution is needed here.
  }
}
