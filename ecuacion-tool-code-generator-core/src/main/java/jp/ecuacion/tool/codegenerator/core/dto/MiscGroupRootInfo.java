package jp.ecuacion.tool.codegenerator.core.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.reader.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;

public class MiscGroupRootInfo extends AbstractColAttrRootInfo {

  private List<String> tableNamesWithoutGrouping;

  @StrBoolean
  private String needsUngroupedSource;
  @StrBoolean
  private String devidesDaoIntoOtherProject;
  
  private String customGroupTableName;
  private String customGroupColumnName;

  public MiscGroupRootInfo() {
    super(DataKindEnum.MISC_GROUP);
  }

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
  public void consistencyCheckAndCoplementData() throws BizLogicAppException {
    
  }

  public String getCustomGroupTableName() {
    return customGroupTableName;
  }

  public void setCustomGroupTableName(String customGroupTableName) {
    this.customGroupTableName = customGroupTableName;
  }

  public String getCustomGroupColumnName() {
    return customGroupColumnName;
  }

  public void setCustomGroupColumnName(String customGroupColumnName) {
    this.customGroupColumnName = customGroupColumnName;
  }
}
