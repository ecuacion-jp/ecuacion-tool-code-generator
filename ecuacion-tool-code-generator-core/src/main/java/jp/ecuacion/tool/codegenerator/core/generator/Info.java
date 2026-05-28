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
package jp.ecuacion.tool.codegenerator.core.generator;

import java.util.Map;
import java.util.stream.Collectors;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;
import org.jspecify.annotations.Nullable;

/**
 * Offers a container for needed data to generate various codes.
 */
@SuppressWarnings({"NullAway.Init", "null"})
public class Info {
  // all systems common
  public String outputDir;

  // system unit values
  private Map<DataKindEnum, AbstractRootInfo> rootInfoMap;
  private String systemName;

  // rootInfo unit values; populated together with rootInfoMap before the generators run.
  private SystemCommonRootInfo sysCmnRootInfo;
  private DataTypeRootInfo dataTypeRootInfo;
  private EnumRootInfo enumRootInfo;
  private DbOrClassRootInfo dbRootInfo;
  private DbOrClassRootInfo dbCommonRootInfo;
  private MiscSoftDeleteRootInfo removedDataRootInfo;
  private MiscGroupRootInfo groupRootInfo;
  private MiscOptimisticLockRootInfo optimisticLockRootInfo;

  private GeneratePtnEnum genPtn;

  /** Returns rootInfoMap. */
  public Map<DataKindEnum, AbstractRootInfo> getRootInfoMap() {
    return rootInfoMap;
  }

  /** Returns systemName. */
  public String getSystemName() {
    return systemName;
  }

  /** Returns sysCmnRootInfo. */
  public SystemCommonRootInfo getSysCmnRootInfo() {
    return sysCmnRootInfo;
  }

  /** Returns dataTypeRootInfo. */
  public DataTypeRootInfo getDataTypeRootInfo() {
    return dataTypeRootInfo;
  }

  /** Returns enumRootInfo. */
  public EnumRootInfo getEnumRootInfo() {
    return enumRootInfo;
  }

  /** Returns dbRootInfo. */
  public DbOrClassRootInfo getDbRootInfo() {
    return dbRootInfo;
  }

  /** Returns dbCommonRootInfo. */
  public DbOrClassRootInfo getDbCommonRootInfo() {
    return dbCommonRootInfo;
  }

  /** Returns removedDataRootInfo. */
  public MiscSoftDeleteRootInfo getRemovedDataRootInfo() {
    return removedDataRootInfo;
  }

  /** Returns groupRootInfo. */
  public MiscGroupRootInfo getGroupRootInfo() {
    return groupRootInfo;
  }

  /** Returns optimisticLockRootInfo. */
  public MiscOptimisticLockRootInfo getOptimisticLockRootInfo() {
    return optimisticLockRootInfo;
  }

  /** Returns genPtn. */
  public GeneratePtnEnum getGenPtn() {
    return genPtn;
  }

  /** Sets genPtn. */
  public void setGenPtn(GeneratePtnEnum genPtn) {
    this.genPtn = genPtn;
  }

  /**
   * Sets root-info unit values from the given systemName and rootInfoMap.
   *
   * <p>NullAway is suppressed because absent map entries leave the corresponding fields
   *     {@code null}; downstream callers expect them to be present once relevant generators run.
   * </p>
   */
  @SuppressWarnings("NullAway")
  public void setRootInfoUnitValues(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {

    // System-level
    this.systemName = systemName;
    this.rootInfoMap = rootInfoMap;

    // RootInfo-level
    sysCmnRootInfo = (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);
    dataTypeRootInfo = (DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE);
    enumRootInfo = (EnumRootInfo) rootInfoMap.get(DataKindEnum.ENUM);
    dbRootInfo = (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB);
    dbCommonRootInfo = (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON);
    removedDataRootInfo = (MiscSoftDeleteRootInfo) rootInfoMap.get(DataKindEnum.MISC_REMOVED_DATA);
    groupRootInfo = (MiscGroupRootInfo) rootInfoMap.get(DataKindEnum.MISC_GROUP);
    optimisticLockRootInfo =
        (MiscOptimisticLockRootInfo) rootInfoMap.get(DataKindEnum.MISC_OPTIMISTIC_LOCK);
  }

  /*
   * table
   */

  /** Returns the common table info or {@code null} when DB_COMMON is not present. */
  public @Nullable DbOrClassTableInfo getCommonTableInfo() {
    return rootInfoMap.containsKey(DataKindEnum.DB_COMMON) ? dbCommonRootInfo.tableList.get(0)
        : null;
  }

  /**
   * Returns the table info matching the given snake-case name.
   *
   * <p>Throws an {@link IllegalStateException} if no table with the given name exists; callers are
   *     expected to look up tables they know to be present in the parsed data.</p>
   */
  public DbOrClassTableInfo getTableInfo(String nameSnakeCase) {
    DbOrClassTableInfo ti =
        dbRootInfo.tableList.stream().collect(Collectors.toMap(t -> t.getName(), t -> t))
            .get(nameSnakeCase);
    if (ti == null) {
      throw new IllegalStateException("Table not found: " + nameSnakeCase);
    }
    return ti;
  }
}
