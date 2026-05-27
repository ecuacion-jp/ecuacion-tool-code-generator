package jp.ecuacion.tool.codegenerator.core.generator;

import jp.ecuacion.tool.codegenerator.core.controller.MainController;

/**
 * Base class for all code generator tools, providing commonly used string constants.
 */
public abstract class ToolForCodeGen {
  // 定数
  protected static final String T1 = "  ";
  protected static final String T2 = T1 + T1;
  protected static final String T3 = T2 + T1;
  protected static final String T4 = T3 + T1;
  protected static final String T5 = T4 + T1;
  protected static final String RT = "\r\n";
  protected static final String RT2 = "\r\n\r\n";
  protected static final String SP = " ";
  protected static final String JD_LN_ST = " * ";
  protected static final String JD_ST = "/**";
  protected static final String JD_END = " */";

  protected Info info;
  
  /** Constructs an instance and binds the thread-local generation info. */
  public ToolForCodeGen() {
    this.info = MainController.tlInfo.get();
  }
  

}
