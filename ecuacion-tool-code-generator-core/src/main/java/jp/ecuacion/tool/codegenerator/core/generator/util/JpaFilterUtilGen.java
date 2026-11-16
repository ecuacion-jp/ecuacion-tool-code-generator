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
package jp.ecuacion.tool.codegenerator.core.generator.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscAdminScopeRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/**
 * Generates the {@code JpaFilterUtil} utility class that configures soft-delete and group
 * filtering.
 */
public class JpaFilterUtilGen extends AbstractGen {

  /** Constructs an instance for the DB data kind. */
  public JpaFilterUtilGen() {
    super(DataKindEnum.DB);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    // Create Util
    sb = new StringBuilder();
    createJpaFilterUtil();
    outputFile(sb, getFilePath("util"), "JpaFilterUtil.java");
  }

  private void createJpaFilterUtil() {
    final boolean grDefined = getInfo().getGroupRootInfo().isDefined();
    final MiscGroupRootInfo grInfo = getInfo().getGroupRootInfo();

    sb.append("package " + rootBasePackage + ".base.util;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.util.Map", "jp.ecuacion.splib.jpa.util.SplibJpaFilterUtil",
        "org.springframework.stereotype.Component");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@Component" + RT);
    sb.append("public class JpaFilterUtil extends SplibJpaFilterUtil {" + RT2);

    sb.append(T1 + "public JpaFilterUtil() {" + RT);
    sb.append(T2 + "super(" + getInfo().getRemovedDataRootInfo().isDefined() + ", " + grDefined
        + ", "
        + (grDefined ? "\"" + StringUtil.getLowerCamelFromSnake(grInfo.getColumnName()) + "\""
            : "null")
        + ", " + customGroupFilterParamNameMapString() + adminScopeArgumentString() + ");" + RT);
    sb.append(T1 + "}" + RT);
    sb.append("}" + RT);
  }

  /**
   * Returns the source of the argument of the map from each admin scope filter name to its
   *     parameter name. It's not output if the admin scope is not used.
   */
  private String adminScopeArgumentString() {
    MiscAdminScopeRootInfo adminScopeInfo = getInfo().getAdminScopeRootInfo();
    if (!adminScopeInfo.isDefined()) {
      return "";
    }

    List<String> entries = new ArrayList<>();
    entries.add(
        T4 + "Map.entry(\"adminScopeFilter\", \"" + adminScopeInfo.getLwFieldName() + "\")");
    for (DbOrClassTableInfo ti : getInfo().getDbRootInfo().tableList) {
      if (ti.hasCustomAdminScopeColumn()) {
        entries.add(T4 + "Map.entry(\"" + ti.getAdminScopeFilterName() + "\", \""
            + ti.getCustomAdminScopeColumn().getNameCamel() + "\")");
      }
    }

    return ", Map.ofEntries(" + RT + String.join("," + RT, entries) + ")";
  }

  /**
   * Returns the source of the map from each custom group filter name to its parameter name.
   *
   * <p>Each table with a group identifier column has its own filter
   *     ({@code groupFilter<TableName>}).</p>
   */
  private String customGroupFilterParamNameMapString() {
    List<String> entries = new ArrayList<>();
    for (DbOrClassTableInfo ti : getInfo().getDbRootInfo().tableList) {
      String filterName = ti.hasCustomGroupColumn() ? ti.getGroupFilterName() : null;
      if (filterName != null) {
        entries.add(T4 + "Map.entry(\"" + filterName + "\", \""
            + ti.getCustomGroupColumn().getNameCamel() + "\")");
      }
    }

    return entries.isEmpty() ? "Map.of()"
        : "Map.ofEntries(" + RT + String.join("," + RT, entries) + ")";
  }
}
