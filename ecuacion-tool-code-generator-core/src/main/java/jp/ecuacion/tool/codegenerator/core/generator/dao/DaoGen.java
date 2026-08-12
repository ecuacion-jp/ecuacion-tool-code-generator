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
package jp.ecuacion.tool.codegenerator.core.generator.dao;

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractTableGen;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil.ColFormat;

/** Generates Spring Data JPA repository source files for each entity. */
public class DaoGen extends AbstractTableGen {

  private ColumnGenUtil code = new ColumnGenUtil();

  /** Constructs an instance for the specified data kind. */
  public DaoGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);
  }

  @Override
  public void generate() {

    // Create baseRepositoryImpl per entity
    for (DbOrClassTableInfo tableInfo : getInfo().getDbRootInfo().tableList) {
      String entityNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

      // Generate baseRepository when using Spring
      if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()) {
        createBaseRepository(tableInfo, entityNameCp);
      }
    }

    // Generate baseRepository when using Spring
    if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()) {
      createSystemCommonBaseRepository();
    }
  }

  private void createBaseRepository(DbOrClassTableInfo tableInfo, String tableNameCp) {

    sb = new StringBuilder();

    List<DbOrClassColumnInfo> list = new ArrayList<>(tableInfo.columnList);
    list.addAll(getInfo().getDbCommonRootInfo().tableList.get(0).columnList);
    String idColumnName = null;
    for (DbOrClassColumnInfo ci : list) {
      if (ci.isPk()) {
        idColumnName = ci.getName();
      }
    }

    if (idColumnName == null) {
      throw new RuntimeException("idColumnName is null: " + tableInfo.getName());
    }

    final String idFieldName = StringUtil.getLowerCamelFromSnake(idColumnName);

    // Declaration and constructor
    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    // import
    createBaseRepositoryImport(tableInfo, tableNameCp);

    sb.append("public interface " + tableNameCp + "BaseRepository"
        + " extends SystemCommonBaseRepository<" + tableNameCp
        + ", Long>, JpaSpecificationExecutor<" + tableNameCp + "> {" + RT2);

    sb.append(T1 + "/** Is defined with jpql because hibernate filter "
        + "does not take effect to spring data jpa standard 'findById'. */" + RT);
    sb.append(
        T1 + "@Query(value = \"from " + tableNameCp + " where " + idFieldName + " = :id\")" + RT);
    sb.append(T1 + "Optional<" + tableNameCp + "> findById(Long id);" + RT2);

    // findBy<NaturalKey>
    if (tableInfo.hasUniqueConstraint()) {
      sb.append(T1 + "/** Finds by natural key. */" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findBy"
          + code.naturalKeyUncapitalCamelAndRelConsidered(tableInfo) + "(" + RT);
      sb.append(T3 + code.naturalKeyDefine(tableInfo) + ");" + RT2);
    }

    // findBy<IdOfRelation>
    for (DbOrClassColumnInfo ci : tableInfo.getRelationColumnList()) {
      String rtnType = ci.getRelationKind() == RelationKindEnum.ONE_TO_ONE ? "Optional" : "List";
      sb.append(
          T1 + "/** Is generated for existence check when a parent record is deleted. */" + RT);
      sb.append(T1 + "public " + rtnType + "<" + tableInfo.getNameCpCamel() + "> findBy"
          + code.generateString(ci, ColFormat.QUERY_METHOD) + "(" + code.getJavaKata(ci) + " "
          + getInfo().getTableInfo(ci.getRelationRefTable())
              .getPkColumn().getNameCamel() + ");" + RT2);
    }

    if (tableInfo.hasSoftDeleteFieldInludingSystemCommon()) {

      // findAllFromAllGroups
      sb.append(T1 + "@Query(nativeQuery = true, value = "
          + "\"select * from Instance where del_flg = false\")" + RT);
      sb.append(T1 + "public List<" + tableNameCp + "> findAllFromAllGroups();" + RT2);

      String commonComment = "/** Used for procedures in libraries. "
          + "Native query is used to search soft deleted records.";
      sb.append(T1 + commonComment + " */" + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "Optional<" + tableNameCp + "> findByIdAndSoftDeleteFieldTrueFromAllGroups"
          + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      String noNaturalKeyMsg = T1 + "The entity doesn't have a natural key. "
          + "Unsatisfied condition is used in the where clause. It not called from library. */";
      sb.append(T1 + commonComment
          + (tableInfo.hasUniqueConstraint() ? " */" : RT + noNaturalKeyMsg) + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"select * from " + tableInfo.getName() + " where "
          + (tableInfo.hasUniqueConstraint() ? code.naturalKeySqlParams(tableInfo) : "1 = 2")
          + " and " + code.softDeleteColLowerSnake() + " = true\")" + RT);
      sb.append(
          T1 + "Optional<" + tableNameCp + "> findByNaturalKeyAndSoftDeleteFieldTrueFromAllGroups"
              + "(@Param(\"entity\") " + tableNameCp + " entity);" + RT2);

      sb.append(T1 + commonComment + " */" + RT);
      sb.append(T1 + "@Modifying" + RT);
      sb.append(T1 + "@Query(nativeQuery = true, " + RT);
      sb.append(T3 + "value = \"delete from " + tableInfo.getName() + " where " + idColumnName
          + " = :#{#entity." + idFieldName + "} and " + code.softDeleteColLowerSnake()
          + " = true\")" + RT);
      sb.append(T1 + "void deleteByIdAndSoftDeleteFieldTrueFromAllGroups(@Param(\"entity\") "
          + tableNameCp + " entity);" + RT2);
    }

    sb.append("}" + RT);

    outputFile(sb, getFilePath("repository"), tableNameCp + "BaseRepository.java");
  }

  private void createBaseRepositoryImport(DbOrClassTableInfo tableInfo, String tableNameCp) {
    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.util.*", rootBasePackage + ".base.entity." + tableNameCp,
        "org.springframework.data.jpa.repository.*",
        "org.springframework.data.repository.query.Param");

    if (tableInfo.hasUniqueConstraint()) {
      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        if (ci.isPk() || ci.isUniqueConstraint()) {
          DataTypeInfo dtInfo = ci.getDtInfo();
          importMgr.add(code.getHelper(dtInfo.getKata()).getNeededImports(ci));
        }
      }
    }

    // Import enum classes used
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      if (colInfo.isUniqueConstraint() || colInfo.isPk()) {
        String dataType = colInfo.getDataType();
        DataTypeInfo dtInfo = colInfo.getDtInfo();
        if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
          String importClassStr =
              rootBasePackage + ".base.enums." + code.dataTypeNameToCapitalCamel(dataType) + "Enum";
          importMgr.add(importClassStr);
        }
      }
    }

    sb.append(importMgr.outputStr() + RT);
  }

  private void createSystemCommonBaseRepository() {

    sb = new StringBuilder();

    sb.append("package " + rootBasePackage + ".base.repository;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("jp.ecuacion.splib.jpa.repository.SplibRepository",
        "org.springframework.data.repository.NoRepositoryBean");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@NoRepositoryBean" + RT);
    sb.append(
        "public interface SystemCommonBaseRepository<T, I> extends SplibRepository<T, I> {" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath("repository"), "SystemCommonBaseRepository.java");
  }
}
