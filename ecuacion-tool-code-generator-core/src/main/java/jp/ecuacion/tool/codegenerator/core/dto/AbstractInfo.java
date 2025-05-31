package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public abstract class AbstractInfo {
  
  protected Info info;

  public AbstractInfo() {
    this.info = MainController.tlInfo.get();
  }
}
