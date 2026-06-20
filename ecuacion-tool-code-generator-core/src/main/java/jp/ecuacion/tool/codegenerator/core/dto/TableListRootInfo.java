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

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** Holds all table-list entries read from the テーブル一覧 sheet. */
@SuppressWarnings("NullAway.Init")
public class TableListRootInfo extends AbstractRootInfo {

  public List<TableListInfo> tableList = new ArrayList<>();

  /** Constructs an empty instance for the TABLE_LIST data kind. */
  public TableListRootInfo() {
    super(DataKindEnum.TABLE_LIST);
  }

  @Override
  public boolean isDefined() {
    return !tableList.isEmpty();
  }

  @Override
  public void consistencyCheckAndCoplementData() {}
}
