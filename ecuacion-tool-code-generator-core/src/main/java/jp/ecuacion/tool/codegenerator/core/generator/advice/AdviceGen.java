package jp.ecuacion.tool.codegenerator.core.generator.advice;

import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class AdviceGen extends AbstractGen {

  public AdviceGen() {
    super(null);
  }

  @Override
  public void generate() {
    createSource();

    outputFile(sb, getFilePath("advice"), "SoftDeleteAdvice.java");
  }

  public void createSource() {
    sb.append("package " + rootBasePackage + ".base.advice;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("jp.ecuacion.splib.jpa.advice.SplibSoftDeleteAdvice",
        "jp.ecuacion.splib.jpa.util.SplibJpaFilterUtil",
        "org.aspectj.lang.annotation.*", "org.springframework.stereotype.Component");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@Aspect" + RT);
    sb.append("@Component" + RT);

    sb.append("public class SoftDeleteAdvice extends SplibSoftDeleteAdvice {" + RT2);
    sb.append("  protected SoftDeleteAdvice(SplibJpaFilterUtil filterUtil) {" + RT);
    sb.append("    super(filterUtil);" + RT);
    sb.append("  }" + RT2);

    sb.append("}" + RT);
  }
}
