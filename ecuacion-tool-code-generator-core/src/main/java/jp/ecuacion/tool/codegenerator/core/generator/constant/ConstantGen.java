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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;

/** Generates the {@code BaseConstants} Java class and the {@code application.properties} file. */
public class ConstantGen extends AbstractGen {

  /** Constructs a ConstantGen with no table-level data kind. */
  public ConstantGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    createSource();

    outputFile(sb, getFilePath("constant"), "BaseConstants.java");

    PropertiesFileGen gen = new PropertiesFileGen();

    // Create the properties file
    Map<String, String> map = new HashMap<>();
    map.put("EXCEL_TEMPLATE_VERSION", getInfo().getSysCmnRootInfo().getTemplateVersion());
    map.put("CODE_GENERATOR_VERSION",
        ResourceBundle.getBundle("version").getString("project.version"));

    gen.writeMapToPropFile(map, "application", null);

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
