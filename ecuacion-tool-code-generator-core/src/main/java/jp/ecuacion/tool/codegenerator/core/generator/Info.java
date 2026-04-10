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

/**
 * Offers a container for needed data to generate various codes.
 */
public class Info {
  // all systems common
  public String outputDir;

  // system unit values
  private Map<DataKindEnum, AbstractRootInfo> rootInfoMap;
  private String systemName;

  // rootInfo unit values
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

  public void setRootInfoUnitValues(String systemName,
      Map<DataKindEnum, AbstractRootInfo> rootInfoMap) {

    // システム単位
    this.systemName = systemName;
    this.rootInfoMap = rootInfoMap;

    // RootInfo単位
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

  public DbOrClassTableInfo getCommonTableInfo() {
    return rootInfoMap.containsKey(DataKindEnum.DB_COMMON) ? dbCommonRootInfo.tableList.get(0)
        : null;
  }

  public DbOrClassTableInfo getTableInfo(String nameSnakeCase) {
    return dbRootInfo.tableList.stream().collect(Collectors.toMap(ti -> ti.getName(), ti -> ti))
        .get(nameSnakeCase);
  }
}
