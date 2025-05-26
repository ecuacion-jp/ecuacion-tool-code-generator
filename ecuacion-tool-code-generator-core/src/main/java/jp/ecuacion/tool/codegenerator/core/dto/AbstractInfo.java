package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.tool.codegenerator.core.controller.CodeGeneratorAction;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public abstract class AbstractInfo {
  
  protected Info info;

  public AbstractInfo() {
    this.info = CodeGeneratorAction.tlInfo.get();
  }
}
