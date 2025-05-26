package jp.ecuacion.tool.codegenerator.core.generator;

import java.io.File;
import java.util.HashMap;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;

/**
 * 持ち歩くのが面倒なので、staticで保持させどこでも使えるようにする。
 */
public class Info {
  // 全体共通の情報
  public HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap;
  public String inputDir;
  public String outputDir;

  // system単位の情報
  public HashMap<DataKindEnum, AbstractRootInfo> rootInfoMap;
  public String systemName;

  // rootInfo単位の情報
  public SystemCommonRootInfo sysCmnRootInfo;
  public DataTypeRootInfo dataTypeRootInfo;
  public EnumRootInfo enumRootInfo;
  public DbOrClassRootInfo dbRootInfo;
  public DbOrClassRootInfo dbCommonRootInfo;
  public MiscSoftDeleteRootInfo removedDataRootInfo;
  public MiscGroupRootInfo groupRootInfo;

  private GeneratePtnEnum genPtn;

  /** 全体共通の値の格納. */
  public void setCommonUnitValues(
      HashMap<String, HashMap<DataKindEnum, AbstractRootInfo>> systemMap) {

    this.systemMap = systemMap;
    // this.allDataTypeMap = allDataTypeMap;
  }

  /** システム・RootInfo単位の値の格納。system単位のループの頭で情報更新される。. */
  public void setRootInfoUnitValues(String systemName) {

    // システム単位
    this.systemName = systemName;
    rootInfoMap = systemMap.get(systemName);

    // RootInfo単位
    sysCmnRootInfo = (SystemCommonRootInfo) rootInfoMap.get(DataKindEnum.SYSTEM_COMMON);
    dataTypeRootInfo = (DataTypeRootInfo) rootInfoMap.get(DataKindEnum.DATA_TYPE);
    enumRootInfo = (EnumRootInfo) rootInfoMap.get(DataKindEnum.ENUM);
    dbRootInfo = (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB);
    dbCommonRootInfo = (DbOrClassRootInfo) rootInfoMap.get(DataKindEnum.DB_COMMON);
    // dbFkRootInfo = ((DbFkRootInfo) rootInfoMap.get(Constants.XML_POST_FIX_DB_FK));
    removedDataRootInfo =
        (MiscSoftDeleteRootInfo) rootInfoMap.get(DataKindEnum.MISC_REMOVED_DATA);
    groupRootInfo = (MiscGroupRootInfo) rootInfoMap.get(DataKindEnum.MISC_GROUP);
  }

  public GeneratePtnEnum getGenPtn() {
    return genPtn;
  }

  public void setGenPtn(GeneratePtnEnum genPtn) {
    this.genPtn = genPtn;
  }

  public String getWorkDir() {
    return outputDir + "/" + "###work###" + File.separator;
  }
}
