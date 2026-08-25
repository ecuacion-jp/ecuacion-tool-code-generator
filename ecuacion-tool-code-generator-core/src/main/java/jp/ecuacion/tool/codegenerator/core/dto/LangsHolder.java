package jp.ecuacion.tool.codegenerator.core.dto;

/**
 * Holds setSysCmnRootInfo.
 */
public interface LangsHolder {
  
  /** Sets SysCmnRootInfo. */
  public void setSysCmnRootInfo(SystemCommonRootInfo sysCmnRootInfo);
  
  /**
   * Builds the display-name map using the language settings from {@code sysCmnRootInfo}.
   */
  public void buildDisplayNameMap();
}
