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
package jp.ecuacion.tool.codegenerator.core.generator.constant;

import java.io.IOException;
import java.util.ResourceBundle;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/** Generates the {@code BaseConstants} Java class and the {@code version_*.properties} files. */
public class ConstantGen extends AbstractGen {

  /** Constructs a ConstantGen with no table-level data kind. */
  public ConstantGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    createSource();

    outputFile(sb, getFilePath("constant"), "BaseConstants.java");

    // Create version_*.properties files readable via VersionUtil in ecuacion-lib.
    String codeGeneratorVersion = ResourceBundle.getBundle("version").getString("version");
    outputFile(new StringBuilder("version=" + codeGeneratorVersion + "\n"), getResourcesPath(),
        "version_ecuacion-tool-code-generator.properties");

    String excelTemplateVersion = getInfo().getSysCmnRootInfo().getTemplateVersion();
    outputFile(new StringBuilder("version=" + excelTemplateVersion + "\n"), getResourcesPath(),
        "version_ecuacion-tool-code-generator-excel-format.properties");
  }

  /** Builds the source code for the {@code BaseConstants} class into the internal string buffer. */
  public void createSource() {
    sb.append("package " + rootBasePackage + ".base.constant;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    // importMgr.add(Constants.STR_LIB_CORE_PKG + ".constant.ConstantsInLibCore");
    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class BaseConstants {" + RT2);
    sb.append(T1 + "public static final String PAC_APP_HOME = \"" + rootBasePackage + "\";" + RT2);
    sb.append("}");
  }
}
