package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

/** TODO. */
public abstract class AbstractInfo {
  
  protected Info info;

  /** TODO. */
  public AbstractInfo() {
    this.info = MainController.tlInfo.get();
  }
}
