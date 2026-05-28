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
package jp.ecuacion.tool.codegenerator.core.generator.propertiesfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

/**
 * Generates ValidationMessagesPatternDescriptions properties files containing regex pattern
 * descriptions for each language.
 */
public class ValidationMessagesPatternDescriptionsGen extends AbstractGen {

  /** Constructs an instance with no specific data kind. */
  public ValidationMessagesPatternDescriptionsGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    PropertiesFileGen gen = new PropertiesFileGen();
    Info info = MainController.tlInfo.get();

    List<String> langList = new ArrayList<>();
    langList.add("");
    langList.addAll(info.getSysCmnRootInfo().getSupportedLangArr());

    for (String lang : langList) {
      Map<String, String> propMap = new LinkedHashMap<>();
      // Message for prohibited character check
      propMap.put("prohibitedChars", info.getSysCmnRootInfo().getProhibitedCharsDesc(lang));

      // Messages for dataType
      for (DataTypeInfo dtInfo : info.getDataTypeRootInfo().dataTypeList) {
        String desc = dtInfo.getStringRegExDesc(
            info.getSysCmnRootInfo().getSupportedLangArr(), lang);
        if (desc != null) {
          propMap.put(StringUtil.getLowerCamelFromSnake(
              dtInfo.getDataTypeName().substring(3)), desc);
        }
      }

      gen.writeMapToPropFile(propMap, "ValidationMessagesPatternDescriptions", lang);
    }
  }
}
