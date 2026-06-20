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
import jp.ecuacion.tool.codegenerator.core.dto.TableListInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/**
 * Generates {@code table_<TABLE_NAME>=<display name>} entries in {@code messages_base.properties}
 * and its language variants, based on the table-list sheet.
 */
public class TableListPropertiesGen extends AbstractGen {

  /** Constructs an instance with no specific data kind. */
  public TableListPropertiesGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    if (!getInfo().getTableListRootInfo().isDefined()) {
      return;
    }

    PropertiesFileGen gen = new PropertiesFileGen();
    List<String> langList = new ArrayList<>();
    langList.add("");
    langList.addAll(getInfo().getSysCmnRootInfo().getSupportedLangArr());

    for (String lang : langList) {
      String lookupKey =
          lang.isEmpty() ? getInfo().getSysCmnRootInfo().getDefaultLang() : lang;
      Map<String, String> propMap = new LinkedHashMap<>();
      for (TableListInfo tableInfo : getInfo().getTableListRootInfo().tableList) {
        String displayName = tableInfo.getDisplayNameMap().get(lookupKey);
        if (displayName != null && !displayName.isEmpty()) {
          propMap.put("table_" + tableInfo.getTableName(), displayName);
        }
      }
      if (!propMap.isEmpty()) {
        gen.writeMapToPropFile(propMap, "messages", lang);
      }
    }
  }
}
