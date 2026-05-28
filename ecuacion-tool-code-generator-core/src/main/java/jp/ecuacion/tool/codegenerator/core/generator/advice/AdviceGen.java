/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.core.generator.advice;

import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

/** Generator that produces the {@code SoftDeleteAdvice} aspect class source file. */
public class AdviceGen extends AbstractGen {

  /** Constructs an AdviceGen instance. */
  public AdviceGen() {
    super(null);
  }

  @Override
  public void generate() {
    createSource();

    outputFile(sb, getFilePath("advice"), "SoftDeleteAdvice.java");
  }

  /**
   * Builds the source code string for the SoftDeleteAdvice class into the internal StringBuilder.
   */
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
