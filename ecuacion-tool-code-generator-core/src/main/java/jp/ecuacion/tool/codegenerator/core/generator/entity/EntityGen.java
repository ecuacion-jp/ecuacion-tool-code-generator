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
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.RelationRefInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.AnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.NormalSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.SimpleFieldAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ColumnGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.ConvertGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.GeneratedValueGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.IdGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.SequenceGeneratorGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.validator.VersionGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.AbstractDaoRelatedGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract base class for entity code generators providing shared logic for fields,
 * constructors, accessors, and properties.
 */
public abstract class EntityGen extends AbstractDaoRelatedGen {

  private CodeGenUtil code = new CodeGenUtil();

  /** Constructs an instance for the specified data kind. */
  public EntityGen(DataKindEnum dataKind) {
    super(dataKind);
  }

  /** Returns the enum value that identifies what kind of entity this generator produces. */
  protected abstract EntityGenKindEnum getEntityGenKindEnum();

  /** Appends the package declaration to the given StringBuilder. */
  protected void appendPackage(StringBuilder sb) {
    sb.append("package " + rootBasePackage + ".base.entity;" + RT);
  }

  /** Appends all necessary import statements for the given table to the StringBuilder. */
  protected void appendImport(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    ImportGenUtil importMgr = new ImportGenUtil();
    final String tableNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

    // Required imports
    // Java standard library
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) {
      importMgr.add("java.util.*");
    }

    importMgr.add("java.io.Serializable");
    // persistence
    importMgr.add("jakarta.persistence.*");

    // Add when using validation
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      importMgr.add(AnnotationGenUtil.getNeededImports(ci.getValidatorList(true)));
    }

    // Even if jakarta.persistence.* is added separately, Version conflicts with other *, so
    // explicitly define it again.
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      // Import when using @Version.
      if (VersionGen.needsValidator(ci.isOptLock())) {
        importMgr.add("jakarta.persistence.Version");
      }

      // auditing
      auditingImport(importMgr, ci.getSpringAuditing(), "CB", "CreatedBy");
      auditingImport(importMgr, ci.getSpringAuditing(), "CD", "CreatedDate");
      auditingImport(importMgr, ci.getSpringAuditing(), "LB", "LastModifiedBy");
      auditingImport(importMgr, ci.getSpringAuditing(), "LD", "LastModifiedDate");
    }

    // When using soft delete
    // Also needs to be added when there is a bidirectional relation
    if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()
        && getInfo().getRemovedDataRootInfo().isDefined()) {
      if (tableInfo.hasSoftDeleteFieldExcludingSystemCommon()) {
        importMgr.add("org.hibernate.annotations.Filter", "org.hibernate.annotations.FilterDef");

      } else if (tableInfo.hasBidirectionalRelationRefColumn()) {
        importMgr.add("org.hibernate.annotations.Filter");
      }
    }

    // Import when using @Filter
    if (getInfo().getGroupRootInfo().isDefined()) {
      if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
        // When a common group definition exists, its filterDef is always output to systemCommon
        importMgr.add("org.hibernate.annotations.FilterDef", "org.hibernate.annotations.ParamDef",
            "org.hibernate.type.descriptor.java.*");

        // When the common group column exists in systemCommon
        if (tableInfo.hasGroupColumn()) {
          importMgr.add("org.hibernate.annotations.Filter");
        }

      } else {
        if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY && !getInfo().getGroupRootInfo()
            .getTableNamesWithoutGrouping().contains(tableInfo.getName())) {

          importMgr.add("org.hibernate.annotations.Filter");

          // When a customGroupColumn is present, filterDef etc. are also needed.
          if (tableInfo.hasCustomGroupColumn()) {
            importMgr.add("org.hibernate.annotations.FilterDef",
                "org.hibernate.annotations.ParamDef", "org.hibernate.type.descriptor.java.*");
          }

          // Explicit definition became necessary because Table is also included in
          // org.hibernate.annotations.*.
          importMgr.add("jakarta.persistence.Table");
        }
      }
    }

    // When using relations
    if (tableInfo.hasRelationColumn()) {
      importMgr.add("jakarta.validation.*");
      importMgr.add("org.hibernate.annotations.OnDelete",
          "org.hibernate.annotations.OnDeleteAction");
    }

    // Required items per entity kind
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) {
      // Parent entity is in the same package, so no import is needed
      // baseRecord
      importMgr.add(rootBasePackage + ".base.record." + tableNameCp + "BaseRecord");

      importMgr.add("jakarta.annotation.Nonnull");

    } else if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      // Parent entity
      importMgr.add("jp.ecuacion.splib.jpa.entity.SplibEntity");
      // baseRecord
      importMgr.add(rootBasePackage + ".base.record.SystemCommonBaseRecord");
      // auditing. Spring only. Not truly hardcoded to systemCommon, but simplified here.
      importMgr.add("org.springframework.data.jpa.domain.support.*");
    }

    // Add required imports per column data type
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      DataTypeInfo dtInfo = colInfo.getDtInfo();
      importMgr.add(code.getHelper(dtInfo.getKata()).getNeededImports(colInfo));
    }

    // Import enum classes used
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      String dataType = colInfo.getDataType();
      DataTypeInfo dtInfo = colInfo.getDtInfo();
      if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
        // importMgr.add("jp.ecuacion.lib.core.util.EnumUtil");

        String importClassStr =
            rootBasePackage + ".base.enums." + code.dataTypeNameToCapitalCamel(dataType) + "Enum";
        importMgr.add(importClassStr);
        // In a batch (Java SE) environment, autoApply = true on @Converter is ignored,
        // requiring an explicit @Convert tag, so the Converter import is needed.
        importClassStr = rootBasePackage + ".base.converter."
            + code.dataTypeNameToCapitalCamel(dataType) + "Converter";
        importMgr.add(importClassStr);
      }
    }

    // Output import statements. An extra RT is added to leave a blank line before the class
    // declaration.
    sb.append(importMgr.outputStr() + RT);
  }

  private void auditingImport(ImportGenUtil importMgr, String springAuditing, String keyword,
      String importClass) {
    if (springAuditing != null && springAuditing.equals(keyword)) {
      importMgr.add("org.springframework.data.annotation." + importClass);
    }
  }

  /**
   * Appends the common grouping FilterDef annotation string to the StringBuilder.
   */
  protected void getGroupFilterDefAnnotationString(StringBuilder sb) {
    getGroupFilterDefAnnotationString(sb, "groupFilter",
        getInfo().getGroupRootInfo().getColumnName(), getInfo().getGroupRootInfo().getDtInfo());
  }

  /**
   * Appends a FilterDef annotation with the given filter name and column details to the
   * StringBuilder.
   */
  protected void getGroupFilterDefAnnotationString(StringBuilder sb, String filterName,
      String colName, DataTypeInfo dtInfo) {

    String fieldNameLc = StringUtil.getLowerCamelFromSnake(colName);

    sb.append("@FilterDef(name = \"" + filterName + "\", " + RT);
    sb.append(T2 + "parameters = @ParamDef(name = \"" + fieldNameLc + "\", type = "
        + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString()) + "JavaType.class)," + RT);
    sb.append(T2 + "defaultCondition = \"" + colName + " = :" + fieldNameLc + "\")" + RT);
  }

  /**
   * Appends the common grouping Filter annotation string to the StringBuilder.
   */
  protected void getGroupFilterAnnotationString(StringBuilder sb) {
    getGroupFilterAnnotationString(sb, "groupFilter");
  }

  /** Appends a Filter annotation with the given filter name to the StringBuilder. */
  protected void getGroupFilterAnnotationString(StringBuilder sb, String filterName) {
    ParamListGen paramGenList = new ParamListGen();
    paramGenList.add(new ParamGenWithSingleValue("name", filterName, true));

    NormalSingleAnnotationGen filter =
        new NormalSingleAnnotationGen("Filter", ElementType.TYPE, paramGenList);
    sb.append(filter.generateString(ElementType.TYPE) + RT);
  }

  /**
   * Appends soft delete FilterDef and Filter annotation strings if soft delete is configured for
   * the table.
   */
  protected void getSoftDeleteAnnotationsString(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    if (getInfo().getSysCmnRootInfo().isFrameworkKindSpring()
        && getInfo().getRemovedDataRootInfo().isDefined()
        && tableInfo.hasSoftDeleteFieldExcludingSystemCommon()) {
      sb.append("@FilterDef(name = \"softDeleteFilter\", defaultCondition = \""
          + getInfo().getRemovedDataRootInfo().getColumnName() + " = false\")" + RT);
      sb.append("@Filter(name = \"softDeleteFilter\")" + RT);
    }
  }

  /** Appends the serialVersionUID constant declaration (fixed to 1) to the StringBuilder. */
  protected void appendSerialVersionUid(StringBuilder sb) {
    sb.append(T1 + "private static final long serialVersionUID = 1L;" + RT2);
  }

  /**
   * Appends field declarations for all non-Java-only columns of the given table.
   *
   * @param tableInfo Entity info required for proper field generation; must not be null
   *     for entity body or PK.
   */
  protected void appendField(StringBuilder sb, DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> colInfoList) {

    for (DbOrClassColumnInfo ci : colInfoList.stream().filter(e -> !e.getIsJavaOnly()).toList()) {
      createFieldInternal(sb, tableInfo.getName(), ci);

      // When a column is both @Id and a relation, a normal @Id column is also needed for @MapsId
      if (ci.isPk() && ci.isRelation()) {
        DbOrClassColumnInfo ci2 = DbOrClassColumnInfo.cloneWithoutRelationRelated(ci);
        createFieldInternal(sb, tableInfo.getName(), ci2);
      }

      if (ci.hasBidirectionalRelationRef()) {
        for (RelationRefInfo info : ci.getBidirectionalRelationRefInfoList()) {
          sb.append(T1 + info.getRelationKind().getName()
              + "(cascade={CascadeType.DETACH, CascadeType.REMOVE}, mappedBy = \""
              + info.getOrgFieldNameToReferDst() + "\")" + RT);
          final String refEntityNameLw = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());

          if (info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
            sb.append(T1 + "@OrderBy(\"id ASC\")" + RT);
          }
          // Filter condition required when referenced by a bidirectional relation
          MiscSoftDeleteRootInfo softDeleteInfo =
              getInfo().getRemovedDataRootInfo();
          MiscGroupRootInfo groupInfo = getInfo().getGroupRootInfo();
          if (softDeleteInfo.isDefined()) {
            sb.append(T1 + "@Filter(name = \"softDeleteFilter\")" + RT);
          }
          if (groupInfo.isDefined()) {
            sb.append(T1 + "@Filter(name = \"groupFilter\")" + RT);
          }

          if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
            sb.append(T1 + "protected " + StringUtils.capitalize(refEntityNameLw) + " "
                + info.getEmptyConsideredFieldNameToReferFromTable() + ";" + RT2);

          } else {
            sb.append(T1 + "protected List<" + StringUtils.capitalize(refEntityNameLw) + "> "
                + info.getEmptyConsideredFieldNameToReferFromTable() + ";" + RT2);
          }
        }
      }
    }
  }

  private void createFieldInternal(StringBuilder sb, String tableName, DbOrClassColumnInfo ci) {

    String columnName = StringUtil.getLowerCamelFromSnake(ci.getName());
    String kata = code.getJavaKata(ci);

    // The second argument may be null in the case of CommonInfo, so null must be considered
    sb.append(getEntityFieldAnnotations(getEntityGenKindEnum(), tableName, ci,
        code.classDotField(tableName, ci)));

    if (ci.isRelation()) {
      jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum relationKind =
          java.util.Objects.requireNonNull(ci.getRelationKind(),
              "isRelation() guarantees getRelationKind() is non-null");
      sb.append(T1 + "@Valid" + RT);
      sb.append(T1 + relationKind.getName() + "(fetch = FetchType."
          + (ci.getRelationIsEager() ? "EAGER" : "LAZY") + ", cascade = {CascadeType.DETACH})"
          + RT);
      sb.append(T1 + "@OnDelete(action = OnDeleteAction.CASCADE)" + RT);
      sb.append(T1 + "@JoinColumn(name = \"" + ci.getName() + "\", referencedColumnName = \""
          + ci.getRelationRefCol() + "\", nullable = " + (ci.isNullable() ? "true" : "false")
          + ", columnDefinition = \""
          + (ci.getDtInfo().getKata() == DataTypeKataEnum.LONG ? "bigint" : "int") + "\")" + RT);
      if (ci.isPk()) {
        sb.append(T1 + "@MapsId" + RT);
      }

      String entityNameUp = StringUtil.getUpperCamelFromSnake(ci.getRelationRefTable());
      String fieldNameLw = ci.getRelationFieldName();
      sb.append(T1 + "private " + StringUtils.capitalize(entityNameUp) + " " + fieldNameLw
          + " = new " + StringUtils.capitalize(entityNameUp) + "();" + RT2);

    } else {
      // Normally private would suffice, but fields defined in EclibEntity (e.g. LST_UPD_TIME) are
      // also generated and need to be protected.
      // Since Entity classes are already created as final, protected is fine here anyway.
      sb.append(T1 + "protected " + kata + " " + columnName + ";" + RT2);
    }
  }

  /**
   * Appends FIELD_xxx static constant declarations for all non-Java-only columns.
   *
   * @param tableNameCp Required only for Entity and Pk entity kinds; may be null otherwise.
   */
  protected void appendFieldName(StringBuilder sb, String tableNameCp,
      DbOrClassTableInfo tableInfo) {
    sb.append(T1 + "// ID" + RT);
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      sb.append(T1 + "public static final String FIELD_" + colInfo.getName() + " = \""
          + StringUtil.getLowerCamelFromSnake(colInfo.getName()) + "\";" + RT);
    }

    sb.append(RT);
  }

  /** Appends the getFieldNameArr() override method listing all field names as a String array. */
  protected void appendFieldNameArr(StringBuilder sb, DbOrClassTableInfo tableInfo,
      String entityNameCp, boolean isInGetPkOfSurrogateKeyStrategyEntity) {
    // Not generated for systemCommon
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      return;
    }

    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public String[] getFieldNameArr() {" + RT);
    sb.append(T2 + "return new String[] {");

    // This list also displays dbCommon columns, so merge them in advance
    ArrayList<DbOrClassColumnInfo> arr = new ArrayList<>();
    arr.addAll(tableInfo.columnList);
    if (getInfo().getDbCommonRootInfo() != null) {
      arr.addAll(getInfo().getDbCommonRootInfo().tableList.get(0).columnList.stream()
          .filter(e -> !e.getIsJavaOnly()).toList());
    }

    boolean isFirst = true;
    for (DbOrClassColumnInfo ci : arr) {
      // Inside getPk() of a surrogateKeyStrategy BODY, only PK fields are listed, so skip non-PK
      // columns
      if (isInGetPkOfSurrogateKeyStrategyEntity && !ci.isPk()) {
        continue;
      }

      // For entityPk, only PK fields are generated
      if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) {

        if (isFirst) {
          isFirst = false;

        } else {
          sb.append(", ");
        }

        sb.append("\"" + StringUtil.getLowerCamelFromSnake(ci.getName()) + "\"");
      }
    }

    sb.append("};" + RT);
    sb.append(T1 + "}" + RT2);
  }

  /** Appends the default no-args constructor declaration. */
  protected void appendDefaultConstructor(StringBuilder sb, String tableNameCp) {
    sb.append(T1 + JD_ST + "Default constructor." + JD_END + RT);
    sb.append(T1 + "public " + tableNameCp + "() {" + "}" + RT2);
  }

  /** Appends a constructor that accepts a BaseRecord argument and initializes fields from it. */
  protected void appendRecConstructor(StringBuilder sb, DbOrClassTableInfo ti,
      String entityNameCp) {
    // Constructor that takes a record as argument
    sb.append(T1 + "/** A constructor with record argument */" + RT);

    // Deliberately using the name without "Pk" so that "Pk" is not inserted before "BaseRecord"
    sb.append(T1 + "public " + entityNameCp + "("
        + (this instanceof SystemCommonGen ? "SystemCommon"
            : StringUtil.getUpperCamelFromSnake(ti.getName()))
        + "BaseRecord rec" + args(ti) + ") {" + RT);
    sb.append(T2 + "super("
        + ((getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) ? "rec" : "") + ");" + RT2);

    // ti.columnList.stream().filter(e -> !e.getIsJavaOnly()).toList().forEach(ci -> {
    // substituteFieldsFromRecordToEntity(sb, ci, true);
    // });

    fields(sb, ti, false);

    sb.append(T1 + "}" + RT2);
  }

  /** Appends a constructor taking the natural key columns as arguments. */
  protected void appendNaturalKeyConstructor(StringBuilder sb, DbOrClassTableInfo tableInfo,
      String entityNameCp) {
    sb.append(T1 + "/**" + RT);
    sb.append(T1 + " * Constructor that takes naturalKey as arguments." + RT);
    sb.append(
        T1 + " * Having both a naturalKey and surrogateKey constructor could cause conflicts, "
            + "so only the naturalKey constructor is provided." + RT);
    sb.append(T1 + " * (The surrogateKey is not used on insert; "
        + "on select it is retrieved via Entity.getPk(field);" + RT);
    sb.append(T1 + " * on update the selected entity is reused, "
        + "so there are few scenarios where passing it as a constructor argument is preferred.) "
        + RT);
    sb.append(T1 + " */" + RT);
    sb.append(T1 + "public " + entityNameCp + "(");
    boolean is1st = true;
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      if (!ci.isUniqueConstraint()) {
        continue;
      }

      String comma = "";
      if (is1st) {
        is1st = false;

      } else {
        comma = ", ";
      }

      sb.append(
          comma + code.getJavaKata(ci) + " " + StringUtil.getLowerCamelFromSnake(ci.getName()));
    }
    sb.append(") {" + RT);
    sb.append(T2 + "this();" + RT);
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      if (!ci.isUniqueConstraint()) {
        continue;
      }

      sb.append(T2 + "set" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "("
          + StringUtil.getLowerCamelFromSnake(ci.getName()) + ");" + RT);
    }
    sb.append(T1 + "}" + RT2);
  }

  // private void substituteFieldsFromRecordToEntity(StringBuilder sb, DbOrClassColumnInfo ci,
  // boolean usesRec) {
  // // if (createsField(ci)) {
  // String leftHandSide = "set" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "(";
  // DataTypeInfo dtInfo = ci.getDtInfo();
  // String fieldNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
  // String fieldNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
  // String kataUc = code.getJavaKata(ci);
  //
  // // When a relation exists, create an instance
  // String relFieldNameUc = StringUtil.capitalize(ci.getRelationFieldName());
  // if (ci.isRelation()) {
  // String entityName = StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
  // sb.append(T2 + ci.getRelationFieldName() + " = rec.get" + relFieldNameUc
  // + "() == null ? null : new " + StringUtil.capitalize(entityName) + "(rec.get"
  // + relFieldNameUc + "());" + RT);
  //
  // } else {
  //
  // if (dtInfo.getKata() == DataTypeKataEnum.FLOAT
  // || dtInfo.getKata() == DataTypeKataEnum.DOUBLE) {
  // sb.append(
  // T2 + leftHandSide + "(" + ((usesRec) ? "rec.get" + fieldNameUc + "()" : fieldNameLc)
  // + " == null || " + ((usesRec) ? "rec.get" + fieldNameUc + "()" : fieldNameLc)
  // + ".equals(\"\"))? null: " + kataUc + ".valueOf"
  // + ((usesRec) ? "(rec.get" + fieldNameUc + "().replaceAll(\",\", \"\"))"
  // : "(" + fieldNameLc + ")")
  // + ");" + RT);
  //
  // // } else if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
  // // String obtainedValue =
  // // ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
  // // : StringUtil.getLowerCamelFromSnake(ci.getName()));
  // // sb.append(T2 + leftHandSide + obtainedValue + " == null ? null :
  // // EnumUtil.getEnumFromCode("
  // // + CodeGenUtil.dataTypeNameToUppperCamel(dtInfo.getDataTypeName()) + "Enum.class, "
  // // + obtainedValue + "));" + RT);
  //
  // } else if (CodeGenUtil.ofEntityTypeMethodAvailableDataTypeList.contains(dtInfo.getKata())) {
  // sb.append(T2 + fieldNameLc + " = rec.get" + fieldNameUc + "OfEntityDataType();" + RT);
  //
  // } else if (dtInfo.getKata() == DataTypeKataEnum.BOOLEAN) {
  // // boolean is stored as Boolean in the record, so Boolean.valueOf is added only when reading
  // // from a String (i.e., when usesRec == false)
  // sb.append(T2 + leftHandSide
  // + ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
  // : ("Boolean.valueOf(" + StringUtil.getLowerCamelFromSnake(ci.getName())) + ")")
  // + ");" + RT);
  //
  // } else {
  // sb.append(T2 + leftHandSide
  // + ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
  // : StringUtil.getLowerCamelFromSnake(ci.getName()))
  // + ");" + RT);
  // }
  // }
  //
  // // Also add field assignment for columns that are targets of a bidirectional relation
  // if (ci.isReferedByBidirectionalRelation()) {
  // for (BidirectionalRelationInfo info : ci.getBidirectionalInfoList()) {
  // String entityNameUc = StringUtil.getUpperCamelFromSnake(info.getOrgTableName());
  // String entityNameLc = StringUtil.uncapitalize(entityNameUc);
  //
  // String bidirFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
  // String bidirFieldNameUc = StringUtil.capitalize(bidirFieldName);
  //
  // // ID column of ref-from table
  // String pkColumnInOrgTable = getInfo().getDbRootInfo().tableList.stream()
  // .filter(tbl -> tbl.getName().equals(info.getOrgTableName())).toList().get(0)
  // .getPkColumn().getName();
  // if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
  // sb.append(T2 + bidirFieldName + " = rec.get" + bidirFieldNameUc + "() == null || rec.get"
  // + bidirFieldNameUc + "().get" + StringUtil.getUpperCamelFromSnake(pkColumnInOrgTable)
  // + "() == null ? null : new " + StringUtil.capitalize(entityNameLc) + "(rec.get"
  // + bidirFieldNameUc + "());" + RT);
  //
  // } else {
  // sb.append(
  // T2 + "if (rec.get" + StringUtil.capitalize(bidirFieldName) + "() != null) {" + RT);
  // sb.append(T3 + bidirFieldName + " = new ArrayList<>();" + RT);
  // sb.append(T3 + "for (" + entityNameUc + "BaseRecord " + entityNameLc + "Rec : rec.get"
  // + StringUtil.capitalize(bidirFieldName) + "()) {" + RT);
  // sb.append(T4 + bidirFieldName + ".add(new " + entityNameUc + "(" + entityNameLc + "Rec));"
  // + RT);
  // sb.append(T3 + "}" + RT);
  // sb.append(T2 + "}" + RT);
  // }
  // }
  // }
  // }

  private String args(DbOrClassTableInfo ti) {
    List<DbOrClassColumnInfo> baseList = ti.columnList.stream().filter(ci -> !ci.getIsJavaOnly())
        .filter(ci -> StringUtils.isEmpty(ci.getSpringAuditing())).toList();

    StringBuilder dateTimeString = new StringBuilder();
    baseList.stream()
        .filter(ci -> ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
            || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP)
        .forEach(ci -> dateTimeString
            .append(", " + code.getJavaKata(ci) + " " + code.uncapitalCamel(ci.getName())));

    StringBuilder relString = new StringBuilder();
    baseList.stream().filter(e -> e.isRelation()).forEach(ci -> relString.append(
        ", " + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getRelationFieldName()));

    return dateTimeString.toString() + relString.toString();
  }

  /**
   * Appends the update() method that copies non-null field values from the record to the entity.
   */
  protected void appendUpdate(StringBuilder sb, DbOrClassTableInfo ti) {
    sb.append(T1 + "public void update(" + code.baseRecDef(ti.getName()) + args(ti)
        + ", String... skipUpdateFields) {" + RT);

    // Remove groupColumn to avoid the data to be moved to other group (=normally other customer's
    // dara realm).
    if (ti.columnList.stream().filter(e -> !e.getIsJavaOnly()).filter(ci -> !ci.isRelation())
        .toList().size() > 0) {
      sb.append(T2 + "List<String> skipUpdateFieldList = Arrays.asList(skipUpdateFields);" + RT2);
    }

    fields(sb, ti, true);

    sb.append(T1 + "}" + RT2);
  }

  private void fields(StringBuilder sb, DbOrClassTableInfo ti, boolean isUpdate) {
    List<DbOrClassColumnInfo> baseList =
        ti.columnList.stream().filter(e -> !e.getIsJavaOnly()).toList();

    // if (uploadedDateTime != null && !skipUpdateFieldList.contains(FIELD_UPLOADED_DATETIME))
    // setUploadedDatetime(uploadedDateTime);
    for (DbOrClassColumnInfo ci : baseList) {
      String fieldName = code.uncapitalCamel(ci.getName());
      String updString =
          !isUpdate ? "" : " && !skipUpdateFieldList.contains(FIELD_" + ci.getName() + ")";
      if (ci.isRelation()) {
        String name = ci.getRelationFieldName();
        sb.append(T2 + "if (" + name + " != null) set"
            + StringUtils.capitalize(ci.getRelationFieldName()) + "(" + name + ");" + RT);

      } else if (ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME
          || ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP) {
        sb.append(T2 + "if (" + fieldName + " != null" + updString + ") "
            + code.set(fieldName, fieldName) + ";" + RT);

      } else {
        sb.append(T2 + "if (" + code.recGet(fieldName) + " != null" + updString + ") "
            + code.set(fieldName, "rec." + code.getOfEntityDataType(ci)) + ";" + RT);
      }
    }
  }

  /** Appends getter and setter methods for all non-Java-only columns of the table. */
  protected void appendAccessor(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      String columnNameCp = StringUtil.getUpperCamelFromSnake(ci.getName());
      String columnNameSm = StringUtil.getLowerCamelFromSnake(ci.getName());
      final String relEntityName = ci.getRelationRefTable() == null ? null
          : StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
      String relFieldName = ci.getRelationRefCol() == null ? null
          : StringUtil.getUpperCamelFromSnake(ci.getRelationRefCol());

      sb.append(T1 + "public " + code.getJavaKata(ci) + " get" + columnNameCp + "() {" + RT);
      sb.append(T2 + "return "
          + (ci.isRelation()
              ? ci.getRelationFieldName() + " == null ? null : " + ci.getRelationFieldName()
                  + ".get" + relFieldName + "()"
              : columnNameSm)
          + ";" + RT);
      sb.append(T1 + "}" + RT2);

      sb.append(T1 + "public void set" + columnNameCp + "(" + getEnumConsideredKata(ci) + " "
          + columnNameSm + ") {" + RT);
      sb.append(T2 + "this."
          + (ci.isRelation()
              ? ci.getRelationFieldName() + ".set" + relFieldName + "(" + columnNameSm + ")"
              : columnNameSm + " = " + columnNameSm)
          + ";" + RT);
      sb.append(T1 + "}" + RT2);

      if (ci.isRelation()) {
        // For relation columns, also provide an accessor for the field representing the entity
        // itself
        appendAccessorForRelation(sb, relEntityName, ci.getRelationFieldName(), false, null);
      }

      if (ci.hasBidirectionalRelationRef()) {
        for (RelationRefInfo info : ci.getBidirectionalRelationRefInfoList()) {
          appendAccessorForRelation(sb, StringUtil.getLowerCamelFromSnake(info.getOrgTableName()),
              info.getEmptyConsideredFieldNameToReferFromTable(), true, info);
        }
      }
    }
  }

  private void appendAccessorForRelation(StringBuilder sb,
      @org.jspecify.annotations.Nullable String relEntityName,
      @org.jspecify.annotations.Nullable String relFieldName,
      boolean isReferedByBidirectionalRelation,
      @org.jspecify.annotations.Nullable RelationRefInfo info) {
    // For a bidirectional reference target with OneToMany, update fieldName and relEntityName
    // accordingly
    if (info != null && info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
      relFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
      relEntityName = "List<" + StringUtils.capitalize(relEntityName) + ">";
    }

    sb.append(T1 + "public " + StringUtils.capitalize(relEntityName) + " get"
        + StringUtils.capitalize(relFieldName) + "() {" + RT);
    sb.append(T2 + "return " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void set" + StringUtils.capitalize(relFieldName) + "("
        + StringUtils.capitalize(relEntityName) + " " + relFieldName + ") {" + RT);
    sb.append(T2 + "this." + relFieldName + " = " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);
  }

  /** Returns the Java type name for the given column, considering enum types. */
  protected String getEnumConsideredKata(DbOrClassColumnInfo ci) {
    return code.getJavaKata(ci);
  }

  private LinkedHashMap<String, String> createSortedMapForPropFile(String lang,
      List<DbOrClassTableInfo> tableList, EntityGenKindEnum entityKind) {
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

    // Store the display name of each field in the map for each table
    if (tableList != null) {
      for (DbOrClassTableInfo tableInfo : tableList) {
        putToMap(lang, tableInfo, map, entityKind);
      }
    }

    return map;
  }

  private void putToMap(String lang, DbOrClassTableInfo tableInfo,
      LinkedHashMap<String, String> map, EntityGenKindEnum entityKind) {

    // // For fields in SystemCommon (e.g. createTime), in addition to SystemCommon.createTime,
    // // Acc.createTime must also be created, so the list includes both

    for (DbOrClassColumnInfo columnInfo : tableInfo.columnList) {
      String entityName = StringUtil.getUpperCamelFromSnake(tableInfo.getName());
      String varName = StringUtil.getLowerCamelFromSnake(columnInfo.getName());
      String dispName = columnInfo.getDisplayNameMap().get(lang);
      map.put(entityName + "." + varName, dispName);

      // // Also register with the first letter of entityName lowercased, to match Spring MVC
      // message output
      map.put(StringUtils.uncapitalize(entityName) + "." + varName, dispName);
    }
  }

  /**
   * Common processing to create item_names_xx.properties files for each configured language.
   */
  protected void appendItemNamesProperties(EntityGenKindEnum entityKind,
      List<DbOrClassTableInfo> tableList) throws IOException, InterruptedException {
    PropertiesFileGen gen = new PropertiesFileGen();

    // Create a fallback file.
    gen.writeMapToPropFile(createSortedMapForPropFile(
        getInfo().getSysCmnRootInfo().getDefaultLang(),
        tableList, entityKind), "item_names", null);
    // Create a file for the default language
    gen.writeMapToPropFile(createSortedMapForPropFile(
        getInfo().getSysCmnRootInfo().getDefaultLang(),
        tableList, entityKind), "item_names", getInfo().getSysCmnRootInfo().getDefaultLang());
    // Create files for each language listed in supportedLangArr
    for (String lang : getInfo().getSysCmnRootInfo().getSupportedLangArr()) {
      gen.writeMapToPropFile(createSortedMapForPropFile(lang, tableList, entityKind), "item_names",
          lang);
    }
  }

  /**
   * Appends the PrePersist or PreUpdate lifecycle callback method that auto-sets default field
   * values.
   */
  protected void appendAutoInsertOrUpdateGen(StringBuilder sb, DbOrClassTableInfo tableInfo,
      boolean isUpdate, boolean isFromSystemCommon) {

    // If there are no target fields at all, skip generating this method, so check that first
    boolean needsMethod = false;
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      boolean bl = needsAutoInsertOrUpdate(colInfo, isUpdate);
      if (bl) {
        needsMethod = true;
        break;
      }
    }

    if (!needsMethod) {
      return;
    }

    // From here: the method needs to be generated.
    sb.append(T1 + ((isUpdate) ? "@PreUpdate" : "@PrePersist") + RT);
    sb.append(T1 + "public void " + ((isUpdate) ? "preUpdate" : "preInsert") + "() {" + RT);

    // When not called from SystemCommon, include a call to the same method in SystemCommon.
    // Note: the case where SystemCommon has no prePersist / preUpdate is not yet handled.
    if (!isFromSystemCommon) {
      sb.append(T2 + "// Calling super here because overriding @PrePersist / @PreUpdate "
          + "in a subclass would prevent the parent class method from being invoked." + RT);
      sb.append(T2 + "super." + ((isUpdate) ? "preUpdate" : "preInsert") + "();" + RT2);

    }
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      DataTypeInfo dtInfo = ci.getDtInfo();
      String fieldName = StringUtil.getLowerCamelFromSnake(ci.getName());
      boolean isForced = !isUpdate && ci.isForcedIncrement() || isUpdate && ci.isForcedUpdate();

      if (!needsAutoInsertOrUpdate(ci, isUpdate)) {
        continue;
      }

      if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName
            + " = Enum.FALSE;" + RT);

      } else if (dtInfo.getKata() == DataTypeKataEnum.BOOLEAN) {
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName
            + " = false;" + RT);

      } else if (dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
          || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME) {
        String kataName = code.getJavaKata(ci);
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName + " = "
            + kataName + ".now();" + RT);

      } else if (dtInfo.getKata() == DataTypeKataEnum.SHORT
          || dtInfo.getKata() == DataTypeKataEnum.INTEGER
          || dtInfo.getKata() == DataTypeKataEnum.LONG) {
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName + " = 1"
            + ((dtInfo.getKata() == DataTypeKataEnum.LONG) ? "L" : "") + ";" + RT);

      } else {
        new Violations().add(new BusinessViolation("MSG_ERR_IMPOSSIBLE")).throwIfAny();
      }
    }

    sb.append(T1 + "}" + RT2);
  }

  /**
    * Returns {@code true} if the given column requires an automatic value to be set on insert or
    * update.
   */
  private boolean needsAutoInsertOrUpdate(DbOrClassColumnInfo colInfo, boolean isUpdate) {
    DataTypeInfo dtInfo = colInfo.getDtInfo();

    if ((!isUpdate && colInfo.isAutoIncrement() || isUpdate && colInfo.isAutoUpdate())
        && (dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
            || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME
            || dtInfo.getKata() == DataTypeKataEnum.BOOLEAN
            || dtInfo.getKata() == DataTypeKataEnum.ENUM)) {
      return true;
    }
    if (!isUpdate && colInfo.isOptLock()) {
      return true;
    }

    return false;
  }

  /** Returns the argument list string for PK columns to be used inside parentheses. */
  protected String getPkArgList(DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> commonColumnList, boolean areAllArgsString) {
    // String pk1, Integer pk2, ...
    StringBuilder lclSb = new StringBuilder();

    boolean isFirst = true;

    for (DbOrClassColumnInfo ci : getMergedColumnList(tableInfo, commonColumnList)) {
      String columnNameSm = StringUtil.getLowerCamelFromSnake(ci.getName());
      if (ci.isPk()) {
        DataTypeInfo dtInfo = ci.getDtInfo();
        String comma = (isFirst) ? "" : ", ";
        String tmpKata = StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
        String kata = (areAllArgsString) ? "String "
            : (Objects.requireNonNull(tmpKata).equals("Enum")
                ? StringUtil.getUpperCamelFromSnake(ci.getName())
                : "") + tmpKata;
        lclSb.append(comma + kata + " " + columnNameSm);
        if (isFirst) {
          isFirst = false;
        }
      }
    }

    return lclSb.toString();
  }

  /** Returns a merged list combining the table's own columns with the common column list. */
  protected List<DbOrClassColumnInfo> getMergedColumnList(DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> commonColumnList) {
    // Create a list by merging commonColumnList
    List<DbOrClassColumnInfo> mergedList = new ArrayList<>();
    mergedList.addAll(tableInfo.columnList);
    mergedList.addAll(commonColumnList);

    return mergedList;
  }

  /**
    * Generates the hasSoftDeleteField() method indicating whether the entity holds a soft delete
    * flag column.
   *
   * <p>When the entity has the soft delete column the method returns true; when the column is in a
   * different class (e.g. SystemCommon), the method is generated as abstract (called from
   * SystemCommon) or omitted (called from a per-table entity).
   * </p>
   * <ul>
    * <li>1-1. Called from SystemCommon AND soft delete used AND column not present: abstract
    * definition.</li>
    * <li>1-2. Not called from SystemCommon AND soft delete used AND column not present: no
    * output.</li>
   * <li>2. Soft delete used AND column present: returns true.</li>
   * <li>3. Otherwise: returns false.</li>
   * </ul>
   */
  protected void appendHasSoftDeleteFieldGen(StringBuilder sb, DbOrClassTableInfo tableInfo,
      boolean isCallFromSystemCommon) {
    MiscSoftDeleteRootInfo softDeleteRootInfo = java.util.Objects.requireNonNull(
        (MiscSoftDeleteRootInfo) getInfo().getRootInfoMap().get(DataKindEnum.MISC_REMOVED_DATA),
        "MISC_REMOVED_DATA must be populated");
    String colName = softDeleteRootInfo.getColumnName();
    boolean usesSoftDelete = colName != null && !colName.equals("");

    boolean containsSoftDeleteField = (tableInfo.columnList.stream().map(e -> e.getName())
        .collect(Collectors.toList()).contains(colName));

    if (usesSoftDelete && !containsSoftDeleteField) {
      if (isCallFromSystemCommon) {
        sb.append(T1 + "public abstract boolean hasSoftDeleteField();" + RT);
      }

    } else {
      sb.append(T1 + "public boolean hasSoftDeleteField() {" + RT);
      sb.append(T2 + "return " + (usesSoftDelete && containsSoftDeleteField ? "true" : "false")
          + ";" + RT);
      sb.append(T1 + "}" + RT);
    }
  }

  /**
    * Generates the annotation strings to be attached to entity fields, including validators and JPA
    * annotations.
   */
  private String getEntityFieldAnnotations(EntityGenKindEnum entityGenKindEnum, String tableName,
      DbOrClassColumnInfo colInfo, String id) {
    List<AnnotationGen> annotationGenList = new ArrayList<>();

    // validator
    annotationGenList.addAll(colInfo.getValidatorList(true));

    // Nothing below is needed for relation columns
    if (colInfo.isRelation()) {
      // Output string. When isRelationColumn() == true, only NotEmpty is targeted.
      return AnnotationGenUtil.getCode(annotationGenList, ElementType.FIELD);
    }

    // The following applies only when isRelationColumn() == false

    // id
    if (IdGen.needsValidator(colInfo)) {
      annotationGenList.add(new IdGen(ElementType.FIELD, colInfo.getDtInfo()));
    }

    // version
    if (VersionGen.needsValidator(colInfo.isOptLock())) {
      annotationGenList.add(new VersionGen(ElementType.FIELD));
    }

    // Auditing-related
    auditingAnnotation(annotationGenList, colInfo.getSpringAuditing(), "CB", "CreatedBy");
    auditingAnnotation(annotationGenList, colInfo.getSpringAuditing(), "CD", "CreatedDate");
    auditingAnnotation(annotationGenList, colInfo.getSpringAuditing(), "LB", "LastModifiedBy");
    auditingAnnotation(annotationGenList, colInfo.getSpringAuditing(), "LD", "LastModifiedDate");

    // Column
    if (ColumnGen.needsValidator(colInfo.getName())) {
      annotationGenList.add(new ColumnGen(ElementType.FIELD, colInfo));
    }

    addFieldConvert(annotationGenList, colInfo.getDtInfo());

    // GeneratedValue
    if (GeneratedValueGen.needsValidator(colInfo)) {
      annotationGenList.add(new GeneratedValueGen(ElementType.FIELD, tableName, colInfo.getName()));
      annotationGenList.add(new SequenceGeneratorGen(ElementType.FIELD, colInfo.getDtInfo(),
          tableName, colInfo.getName(), entityGenKindEnum));
    }

    // Output string
    return AnnotationGenUtil.getCode(annotationGenList, ElementType.FIELD);
  }

  private void auditingAnnotation(List<AnnotationGen> annotationGenList, String springAuditing,
      String keyword, String annotationName) {
    if (springAuditing != null && springAuditing.equals(keyword)) {
      annotationGenList.add(new SimpleFieldAnnotationGen(annotationName));
    }
  }

  private void addFieldConvert(List<AnnotationGen> annotationGenList, DataTypeInfo dtInfo) {
    if (ConvertGen.needsValidator(dtInfo)) {
      annotationGenList.add(new ConvertGen(ElementType.FIELD, dtInfo));
    }
  }
}
