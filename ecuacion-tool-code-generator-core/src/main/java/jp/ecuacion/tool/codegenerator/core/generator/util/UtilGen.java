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
import java.util.Objects;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

/**
 * Generates the {@code JpaFilterUtil} utility class that configures soft-delete and group
 * filtering.
 */
public class UtilGen extends AbstractGen {

  /** Constructs an instance for the DB data kind. */
  public UtilGen() {
    super(DataKindEnum.DB);
  }

  @Override
  public void generate() throws IOException, InterruptedException {
    // Create Util
    Logger.log(this, "GEN_UTIL");

    sb = new StringBuilder();
    createJpaFilterUtil();
    outputFile(sb, getFilePath("util"), "JpaFilterUtil.java");
  }

  private void createJpaFilterUtil() {
    final boolean grDefined = getInfo().getGroupRootInfo().isDefined();
    final MiscGroupRootInfo grInfo = getInfo().getGroupRootInfo();

    sb.append("package " + rootBasePackage + ".base.util;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("jp.ecuacion.splib.jpa.util.SplibJpaFilterUtil",
        "org.springframework.stereotype.Component");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@Component" + RT);
    sb.append("public class JpaFilterUtil extends SplibJpaFilterUtil {" + RT2);

    sb.append(T1 + "public JpaFilterUtil() {" + RT);
    sb.append(T2 + "super(" + getInfo().getRemovedDataRootInfo().isDefined() + ", "
        + grDefined + ", " + (grDefined ? "\""
            + StringUtil.getLowerCamelFromSnake(grInfo.getColumnName()) + "\"" : "null")
        + ", " + (grInfo.getCustomGroupTableName() != null) + ", "
        + (grDefined ? "\"groupFilter" + StringUtil
            .getUpperCamelFromSnake(Objects.requireNonNull(grInfo.getCustomGroupTableName())) + "\""
            : "null")
        + ", "
        + (grDefined ? "\"" + StringUtil.getLowerCamelFromSnake(
            Objects.requireNonNull(grInfo.getCustomGroupColumnName())) + "\"" : "null")
        + ");" + RT);
    sb.append(T1 + "}" + RT);
    sb.append("}" + RT);
  }
}
