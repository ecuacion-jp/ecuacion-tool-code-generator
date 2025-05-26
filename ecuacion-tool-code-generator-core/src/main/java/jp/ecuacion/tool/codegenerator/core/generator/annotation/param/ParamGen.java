package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;

public abstract class ParamGen {
  public abstract String generateString() throws BizLogicAppException;
}
