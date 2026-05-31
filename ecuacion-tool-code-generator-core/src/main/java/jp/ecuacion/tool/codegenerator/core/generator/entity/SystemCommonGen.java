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
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Generates the SystemCommon entity source file that serves as the mapped superclass for all
 * entities.
 */
public class SystemCommonGen extends EntityGen {

  /** Constructs an instance for the DB_COMMON data kind. */
  public SystemCommonGen() {
    super(DataKindEnum.DB_COMMON);
  }

  protected EntityGenKindEnum getEntityGenKindEnum() {
    return EntityGenKindEnum.ENTITY_SYSTEM_COMMON;
  }

  @Override
  public void generate() throws IOException, InterruptedException {

    DbOrClassTableInfo tableInfo = getInfo().getCommonTableInfo();
    if (tableInfo != null) {
      sb = new StringBuilder();
      createSource(tableInfo);

    } else {
      sb = new StringBuilder();

      // Header definitions
      appendPackage(sb);
      ImportBlock importMgr = new ImportBlock();
      importMgr.add("jp.ecuacion.splib.jpa.entity.SplibEntity");
      importMgr.add("jakarta.persistence.*", "java.io.Serializable");
      importMgr.add(rootBasePackage + ".base.record.SystemCommonBaseRecord");
      sb.append(importMgr.outputStr() + RT);
      // Class definition
      sb.append("@MappedSuperclass" + RT);
      sb.append("public abstract class SystemCommon "
          + "extends SplibEntity implements Serializable {" + RT2);
      sb.append(T1 + "private static final long serialVersionUID = 1L;" + RT2);
      sb.append(T1 + "public SystemCommon() {}" + RT);
      sb.append(T1 + "public SystemCommon(SystemCommonBaseRecord rec) {super();}" + RT2);
      sb.append(T1 + "@PrePersist" + RT);
      sb.append(T1 + "public void preInsert() {}" + RT2);
      sb.append(T1 + "@PreUpdate" + RT);
      sb.append(T1 + "public void preUpdate() {}" + RT);
      sb.append("}" + RT);
    }

    outputFile(sb, getFilePath("entity"), "SystemCommon.java");

    appendItemNamesProperties(EntityGenKindEnum.ENTITY_SYSTEM_COMMON,
        getInfo().getDbCommonRootInfo().tableList);
  }

  /** Generates and appends the full SystemCommon class source from the given table info. */
  public void createSource(DbOrClassTableInfo tableInfo) {

    final String entityNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

    // Header definitions
    appendPackage(sb);
    appendImport(sb, tableInfo);

    // Class definition
    // When a grouping definition exists, a filter definition is always written in systemCommon.
    if (getInfo().getGroupRootInfo().isDefined()) {
      getGroupFilterDefAnnotationString(sb);
    }
    if (tableInfo.hasGroupColumn()) {
      getGroupFilterAnnotationString(sb);
    }

    // When using soft delete
    getSoftDeleteAnnotationsString(sb, tableInfo);

    sb.append("@MappedSuperclass" + RT);
    sb.append("@EntityListeners(AuditingEntityListener.class)" + RT);
    sb.append(
        "public abstract class SystemCommon extends SplibEntity implements Serializable {"
            + RT2);

    appendSerialVersionUid(sb);

    // Various field definitions
    appendField(sb, tableInfo, tableInfo.columnList);
    appendFieldName(sb, entityNameCp, tableInfo);

    // Various constructor definitions
    appendDefaultConstructor(sb, entityNameCp);
    appendRecConstructor(sb, tableInfo, entityNameCp);

    // accessor
    appendAccessor(sb, tableInfo);

    // prePersist
    appendAutoInsertOrUpdateGen(sb, tableInfo, false, true);
    // preUpdate
    appendAutoInsertOrUpdateGen(sb, tableInfo, true, true);

    // hasSoftDeleteField
    appendHasSoftDeleteFieldGen(sb, tableInfo, true);

    sb.append("}");
  }
}
