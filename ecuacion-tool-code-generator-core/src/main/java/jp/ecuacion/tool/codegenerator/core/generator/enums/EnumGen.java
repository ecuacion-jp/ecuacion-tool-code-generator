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
package jp.ecuacion.tool.codegenerator.core.generator.enums;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;

/**
 * Generates enum Java source files and the corresponding enum_names properties files for all
 * configured languages.
 */
public class EnumGen extends AbstractGen {

  private static final DetailLogger log = new DetailLogger(EnumGen.class);

  /** Constructs an instance for the ENUM data kind. */
  public EnumGen() {
    super(DataKindEnum.ENUM);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    List<EnumClassInfo> enumClassList = getInfo().getEnumRootInfo().enumClassList;

    // Create enums
    log.info("Generating enum: enum source.");
    for (EnumClassInfo enumClassInfo : enumClassList) {
      sb = new StringBuilder();
      createEnum(enumClassInfo);
      outputFile(sb, getFilePath("enums"), enumClassInfo.getEnumName() + ".java");
    }

    log.info("Generating enum: properties files related to enum.");
    PropertiesFileGen gen = new PropertiesFileGen();

    // Create properties files.
    // Create a file for the default language. For example, if the default language is "en",
    // create enum_names.properties with the same content as enum_names_en.properties.
    gen.writeMapToPropFile(
        createSortedMapForPropFile(getInfo().getSysCmnRootInfo().getDefaultLang(), enumClassList),
        "enum_names", null);
    // Create enum_names_en.properties
    gen.writeMapToPropFile(
        createSortedMapForPropFile(getInfo().getSysCmnRootInfo().getDefaultLang(), enumClassList),
        "enum_names", getInfo().getSysCmnRootInfo().getDefaultLang());
    // Create files for each language listed in supportedLangArr
    for (String lang : getInfo().getSysCmnRootInfo().getSupportedLangArr()) {
      gen.writeMapToPropFile(createSortedMapForPropFile(lang, enumClassList), "enum_names", lang);
    }
  }

  private void createEnum(EnumClassInfo enumClassInfo) {
    final String enumName = enumClassInfo.getEnumName();

    sb.append("package " + rootBasePackage + ".base.enums;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.util.Locale");
    importMgr.add(EclibCoreConstants.PKG + ".util.PropertiesFileUtil");
    sb.append(importMgr.outputStr() + RT);

    sb.append("public enum " + enumName + " {" + RT2);

    boolean isFirst = true;
    for (EnumValueInfo enumValueInfo : enumClassInfo.enumList) {
      // Store in variables to improve source readability
      final String code = enumValueInfo.getCode();
      final String varName = enumValueInfo.getVarName();

      // Insert a comma separator for the second and subsequent entries
      if (isFirst) {
        isFirst = false;

      } else {
        sb.append("," + RT2);
      }

      sb.append(T1 + varName + "(\"" + code + "\")");
    }
    sb.append(";" + RT2);

    sb.append(T1 + "private String code;" + RT2);

    sb.append(T1 + "private " + enumName + "(String code) {" + RT);
    sb.append(T2 + "this.code = code;" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("Returns the code.", "No need to handle null or empty code "
        + "as a validation error is raised when the Enum is generated."));
    sb.append(T1 + "public String getCode() {" + RT);
    sb.append(T2 + "return code;" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("Returns the display name for use in the UI.",
        "This name can be retrieved but cannot be used to look up the enum.",
        "Returns in the localized language."));
    sb.append(T1 + "public String getDisplayName(Locale locale) {" + RT);
    sb.append(T2 + "return PropertiesFileUtil.getEnumName("
        + "locale, this.getClass().getSimpleName() + \".\" + this.toString());" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("Uses the default Locale."));
    sb.append(T1 + "public String getDisplayName() {" + RT);
    sb.append(T2 + "return PropertiesFileUtil.getEnumName("
        + "Locale.getDefault(), this.getClass().getSimpleName() + \".\" + this.toString());" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);
  }

  private Map<String, String> createSortedMapForPropFile(String lang,
      List<EnumClassInfo> enumClassList) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    for (EnumClassInfo enumClassInfo : enumClassList) {
      for (EnumValueInfo enumValueInfo : enumClassInfo.enumList) {
        String enumName = enumClassInfo.getEnumName();
        String varName = enumValueInfo.getVarName();
        String dispName = enumValueInfo.getDisplayNameMap().get(lang);
        map.put(enumName + "." + varName, dispName);
      }
    }
    return map;
  }
}
