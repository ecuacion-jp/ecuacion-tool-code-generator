package jp.ecuacion.tool.codegenerator.core.generator;

import java.util.Map;
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
  public Map<DataKindEnum, AbstractRootInfo> rootInfoMap;
  public String systemName;

  // rootInfo unit values
  public SystemCommonRootInfo sysCmnRootInfo;
  public DataTypeRootInfo dataTypeRootInfo;
  public EnumRootInfo enumRootInfo;
  public DbOrClassRootInfo dbRootInfo;
  public DbOrClassRootInfo dbCommonRootInfo;
  public MiscSoftDeleteRootInfo removedDataRootInfo;
  public MiscGroupRootInfo groupRootInfo;
  public MiscOptimisticLockRootInfo optimisticLockRootInfo;

  private GeneratePtnEnum genPtn;

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

  public DbOrClassTableInfo getCommonTableInfo() {
    return rootInfoMap.containsKey(DataKindEnum.DB_COMMON) ? dbCommonRootInfo.tableList.get(0)
        : null;
  }

  public GeneratePtnEnum getGenPtn() {
    return genPtn;
  }

  public void setGenPtn(GeneratePtnEnum genPtn) {
    this.genPtn = genPtn;
  }
}
