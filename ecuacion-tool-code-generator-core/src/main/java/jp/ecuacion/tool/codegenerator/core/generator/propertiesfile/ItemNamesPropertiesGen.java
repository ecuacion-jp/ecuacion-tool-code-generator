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
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/**
 * Generates {@code item_names} properties files, covering both SystemCommon columns and the
 * columns of each DB table.
 *
 * <p>The default language is served by the no-suffix (ROOT) file alone; no separate
 * {@code _<defaultLang>} copy is generated, since {@code PropertiesFileUtilBundleReader}'s
 * {@code ResourceBundle} lookup already resolves the no-suffix file for any locale that has no
 * more specific bundle of its own, default language included.</p>
 */
public class ItemNamesPropertiesGen extends AbstractGen {

  /** Constructs an instance with no specific data kind. */
  public ItemNamesPropertiesGen() {
    super(null);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    List<DbOrClassTableInfo> tableList = new ArrayList<>();
    tableList.addAll(getInfo().getDbCommonRootInfo().tableList);
    tableList.addAll(getInfo().getDbRootInfo().tableList);

    PropertiesFileGen gen = new PropertiesFileGen();

    // Create the no-suffix (ROOT) file, serving the default language.
    gen.writeMapToPropFile(
        createSortedMapForPropFile(getInfo().getSysCmnRootInfo().getDefaultLang(), tableList),
        "item_names", null);
    // Create files for each language listed in supportedLangArr
    for (String lang : getInfo().getSysCmnRootInfo().getSupportedLangArr()) {
      gen.writeMapToPropFile(createSortedMapForPropFile(lang, tableList), "item_names", lang);
    }
  }

  private Map<String, String> createSortedMapForPropFile(String lang,
      List<DbOrClassTableInfo> tableList) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    for (DbOrClassTableInfo tableInfo : tableList) {
      putToMap(lang, tableInfo, map);
    }

    return map;
  }

  private void putToMap(String lang, DbOrClassTableInfo tableInfo, Map<String, String> map) {
    for (DbOrClassColumnInfo columnInfo : tableInfo.columnList) {
      String entityName = StringUtil.getLowerCamelFromSnake(tableInfo.getName());
      String varName = StringUtil.getLowerCamelFromSnake(columnInfo.getName());
      String dispName = columnInfo.getDisplayNameMap().get(lang);
      map.put(entityName + "." + varName, dispName);
    }
  }
}
