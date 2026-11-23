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
import java.util.Locale;
import java.util.Map;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeStringDataPtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.PatternGen;

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
    List<String> langList = new ArrayList<>();
    langList.add("");
    langList.addAll(getInfo().getSysCmnRootInfo().getSupportedLangArr());

    for (String lang : langList) {
      Map<String, String> propMap = new LinkedHashMap<>();
      // Message for prohibited character check
      String prohibitedCharsDesc = getInfo().getSysCmnRootInfo().getProhibitedCharsDesc(lang);
      if (prohibitedCharsDesc != null) {
        propMap.put("prohibitedChars", prohibitedCharsDesc);
      }

      // Messages for data patterns.
      // All of them are always output, not only the ones used in the dataTypes,
      // so that hand-written code can also refer to them.
      Locale locale = toLocale(lang.isEmpty() ? getInfo().getSysCmnRootInfo().getDefaultLang()
          : lang);
      for (DataTypeStringDataPtnEnum dataPtn : DataTypeStringDataPtnEnum.values()) {
        if (PatternGen.needsValidatorPattern1(dataPtn)) {
          propMap.put(dataPtn.getPatternDescriptionKey(), dataPtn.getDisplayName(locale));
        }
      }

      // Messages for dataType
      for (DataTypeInfo dtInfo : getInfo().getDataTypeRootInfo().dataTypeList) {
        String desc = dtInfo.getStringRegExDesc(
            getInfo().getSysCmnRootInfo().getSupportedLangArr(), lang);
        if (desc != null) {
          propMap.put(StringUtil.getLowerCamelFromSnake(
              dtInfo.getDataTypeName().substring(3)), desc);
        }
      }

      gen.writeMapToPropFile(propMap, "ValidationMessagesPatternDescriptions", lang);
    }
  }

  /*
   * Only the language part is used (like "ja" from "ja_JP"),
   * since the display names of data patterns are prepared per language.
   * Languages without prepared display names fall back to English (the base bundle).
   */
  static Locale toLocale(String lang) {
    return Locale.of(lang.split("[_-]")[0]);
  }
}
