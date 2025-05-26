package jp.ecuacion.tool.codegenerator.core.generator;

import jp.ecuacion.tool.codegenerator.core.controller.CodeGeneratorAction;

/**
 * 各種Generatorはこれを継承して作成する。 このクラスは単なるツールなのだが、頻度の高い定数をここに定義しておくと、クラス名.T1のように 書かなくても済むので非常に楽。
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
  
  public ToolForCodeGen() {
    this.info = CodeGeneratorAction.tlInfo.get();
  }
  

}
