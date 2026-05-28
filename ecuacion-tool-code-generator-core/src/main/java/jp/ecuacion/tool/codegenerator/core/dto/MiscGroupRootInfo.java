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
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.reader.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import org.jspecify.annotations.Nullable;

/** Holds group-related settings such as the group column and tables excluded from grouping. */
@SuppressWarnings("NullAway.Init")
public class MiscGroupRootInfo extends AbstractColAttrRootInfo {

  private List<String> tableNamesWithoutGrouping;

  @StrBoolean
  private String needsUngroupedSource;
  @StrBoolean
  private String devidesDaoIntoOtherProject;
  
  private @Nullable String customGroupTableName;
  private @Nullable String customGroupColumnName;

  /** Constructs an empty instance when no group XML file is provided. */
  @SuppressWarnings("null")
  public MiscGroupRootInfo() {
    super(DataKindEnum.MISC_GROUP);
  }

  /** Constructs an instance with the group column settings and tables without grouping. */
  public MiscGroupRootInfo(String columnName, String dataTypeName, String tableNamesWithoutGrouping,
      String needsUngroupedSource, String devidesDaoIntoOtherProject) {

    super(DataKindEnum.MISC_GROUP, columnName, dataTypeName);

    this.tableNamesWithoutGrouping =
        (tableNamesWithoutGrouping == null || tableNamesWithoutGrouping.equals(""))
            ? new ArrayList<>()
            : Arrays.asList(tableNamesWithoutGrouping.split(","));
    this.needsUngroupedSource = needsUngroupedSource;
    this.devidesDaoIntoOtherProject = devidesDaoIntoOtherProject;
  }

  public List<String> getTableNamesWithoutGrouping() {
    return tableNamesWithoutGrouping;
  }

  public void setTableNamesWithoutGrouping(List<String> tableNamesWithoutGrouping) {
    this.tableNamesWithoutGrouping = tableNamesWithoutGrouping;
  }

  public boolean getNeedsUngroupedSource() {
    return ReaderUtil.boolStrToBoolean(needsUngroupedSource);
  }

  public void setNeedsUngroupedSource(String needsUngroupedSource) {
    this.needsUngroupedSource = needsUngroupedSource;
  }

  public boolean getDevidesDaoIntoOtherProject() {
    return ReaderUtil.boolStrToBoolean(devidesDaoIntoOtherProject);
  }

  public void setDevidesDaoIntoOtherProject(String devidesDaoIntoOtherProject) {
    this.devidesDaoIntoOtherProject = devidesDaoIntoOtherProject;
  }

  @Override
  public void consistencyCheckAndCoplementData() {
    
  }

  public @Nullable String getCustomGroupTableName() {
    return customGroupTableName;
  }

  public void setCustomGroupTableName(@Nullable String customGroupTableName) {
    this.customGroupTableName = customGroupTableName;
  }

  public @Nullable String getCustomGroupColumnName() {
    return customGroupColumnName;
  }

  public void setCustomGroupColumnName(@Nullable String customGroupColumnName) {
    this.customGroupColumnName = customGroupColumnName;
  }
}
