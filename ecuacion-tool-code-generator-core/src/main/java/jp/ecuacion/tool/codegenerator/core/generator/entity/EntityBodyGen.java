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
package jp.ecuacion.tool.codegenerator.core.generator.entity;

import java.io.IOException;
import java.util.List;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** Generates the concrete entity body class source code for each DB table. */
public class EntityBodyGen extends EntityGen {

  /** Constructs an instance for the given data kind. */
  public EntityBodyGen(DataKindEnum xmlFilePostFix, boolean isPkPart) {
    super(xmlFilePostFix);
  }

  @Override
  protected EntityGenKindEnum getEntityGenKindEnum() {
    return EntityGenKindEnum.ENTITY_BODY;
  }

  @Override
  public void generate() throws IOException, InterruptedException {

    DbOrClassRootInfo dbCommon = java.util.Objects.requireNonNull(
        (DbOrClassRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.DB_COMMON),
        "DB_COMMON must be populated");
    for (DbOrClassTableInfo tableInfo : getInfo().getDbRootInfo().tableList) {
      sb = new StringBuilder();
      createSource(tableInfo, dbCommon.tableList.get(0).columnList);
      outputFile(sb, getFilePath("entity"),
          StringUtil.getUpperCamelFromSnake(tableInfo.getName()) + ".java");
    }

    appendItemNamesProperties(EntityGenKindEnum.ENTITY_BODY, getInfo().getDbRootInfo().tableList);
  }

  /**
   * Assembles the full source code of the entity class for the given table, including fields,
   * constructors, accessors, and lifecycle hooks.
   */
  public void createSource(DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> commonColumnList) {

    final String entityNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

    // Header definitions
    appendPackage(sb);
    appendImport(sb, tableInfo);

    // Class definition
    sb.append("@Entity" + RT);
    sb.append(tableInfo.getTableAnnotationString(tableInfo) + RT);

    // Group definition
    if (getInfo().getGroupRootInfo().isDefined()
        && !getInfo().getGroupRootInfo().getTableNamesWithoutGrouping()
            .contains(tableInfo.getName())) {
      if (tableInfo.hasCustomGroupColumn()) {
        DbOrClassColumnInfo customGroupCi = tableInfo.getCustomGroupColumn();
        String filterName = "groupFilter" + StringUtil.getUpperCamelFromSnake(tableInfo.getName());
        getGroupFilterDefAnnotationString(sb, filterName, customGroupCi.getName(),
            customGroupCi.getDtInfo());
        getGroupFilterAnnotationString(sb, filterName);

      } else {
        getGroupFilterAnnotationString(sb);
      }
    }

    // When using soft delete
    getSoftDeleteAnnotationsString(sb, tableInfo);

    sb.append("public final class " + entityNameCp
        + " extends SystemCommon implements Serializable {" + RT2);

    appendSerialVersionUid(sb);

    // Various field definitions
    appendField(sb, tableInfo, tableInfo.columnList);
    appendFieldName(sb, entityNameCp, tableInfo);
    appendFieldNameArr(sb, tableInfo, entityNameCp, false);

    // Various constructor definitions
    appendDefaultConstructor(sb, entityNameCp);
    appendRecConstructor(sb, tableInfo, entityNameCp);
    if (tableInfo.hasUniqueConstraint()) {
      appendNaturalKeyConstructor(sb, tableInfo, entityNameCp);
    }

    // update with record
    appendUpdate(sb, tableInfo);

    // accessor
    appendAccessor(sb, tableInfo);

    // prePersist
    appendAutoInsertOrUpdateGen(sb, tableInfo, false, false);
    // preUpdate
    appendAutoInsertOrUpdateGen(sb, tableInfo, true, false);
    // Unique-constraint-related
    appendUniqueConstraintGen(sb, tableInfo);

    // hasSoftDeleteField
    appendHasSoftDeleteFieldGen(sb, tableInfo, false);

    sb.append("}" + RT);
  }

  private void appendUniqueConstraintGen(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    sb.append(T1 + "// getNaturalKeyFieldList()" + RT);
    sb.append(T1 + "public List<String> getNaturalKeyFieldList() {" + RT);
    if (tableInfo.hasUniqueConstraint()) {
      sb.append(T2 + "List<String> rtnList = new ArrayList<>();" + RT);

      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        if (ci.isUniqueConstraint()) {
          sb.append(T2 + "rtnList.add(\"" + StringUtil.getLowerCamelFromSnake(ci.getName()) + "\");"
              + RT);
        }
      }
      sb.append(T2 + "return rtnList;" + RT);

    } else {
      sb.append(T2 + "return null;" + RT);
    }

    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "// getSetOfUniqueConstraintFieldList()" + RT);
    sb.append(T1
        + "// Currently only naturalKey is effectively supported, "
        + "so it is added to the Set and returned."
        + RT);
    sb.append(T1
        + "// In the future, other unique keys should also be configurable "
        + "(otherwise auto-deletion of soft-deleted records on insert would not work)."
        + RT);

    sb.append(T1 + "@NonNull" + RT);
    sb.append(T1 + "public Set<List<String>> getSetOfUniqueConstraintFieldList() {" + RT);
    sb.append(T2 + "Set<List<String>> rtnSet = new HashSet<>();" + RT);
    sb.append(T2 + "List<String> list = getNaturalKeyFieldList();" + RT);
    sb.append(T2 + "if (list != null) {" + RT);
    sb.append(T3 + "rtnSet.add(list);" + RT);
    sb.append(T2 + "}" + RT2);
    sb.append(T2 + "return rtnSet;" + RT);
    sb.append(T1 + "}" + RT2);
  }
}
