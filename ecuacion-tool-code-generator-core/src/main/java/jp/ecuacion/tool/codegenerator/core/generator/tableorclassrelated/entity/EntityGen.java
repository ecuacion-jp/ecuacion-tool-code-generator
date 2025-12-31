package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.BidirectionalRelationInfo;
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
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.AbstractTableOrClassRelatedGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public abstract class EntityGen extends AbstractTableOrClassRelatedGen {

  private CodeGenUtil code = new CodeGenUtil();

  public EntityGen(DataKindEnum dataKind) {
    super(dataKind);
  }

  protected abstract EntityGenKindEnum getEntityGenKindEnum();

  protected void appendPackage(StringBuilder sb) {
    sb.append("package " + rootBasePackage + ".base.entity;" + RT);
  }

  protected void appendImport(StringBuilder sb, DbOrClassTableInfo tableInfo) throws AppException {
    ImportGenUtil importMgr = new ImportGenUtil();
    final String tableNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

    // import必須のもの
    // java標準
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) {
      importMgr.add("java.util.*");
    }

    importMgr.add("java.io.Serializable");
    // persistence
    importMgr.add("jakarta.persistence.*");
    // ecuacion-lib
    importMgr.add(EclibCoreConstants.PKG + ".exception.checked.*");

    // validationを使う場合は追加
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      importMgr.add(AnnotationGenUtil.getNeededImports(ci.getValidatorList(true)));
    }

    // 別でjakarta.persistence.*を追加していたとしても、Versionが他の*と重複するため改めて明示的に定義しておく。
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      // @Versionを使用する場合はimport。
      if (VersionGen.needsValidator(ci.isOptLock())) {
        importMgr.add("jakarta.persistence.Version");
      }

      // auditing
      auditingImport(importMgr, ci.getSpringAuditing(), "CB", "CreatedBy");
      auditingImport(importMgr, ci.getSpringAuditing(), "CD", "CreatedDate");
      auditingImport(importMgr, ci.getSpringAuditing(), "LB", "LastModifiedBy");
      auditingImport(importMgr, ci.getSpringAuditing(), "LD", "LastModifiedDate");
    }

    // soft deleteを使用する場合
    if (info.sysCmnRootInfo.isFrameworkKindSpring() && info.removedDataRootInfo.isDefined()
        && tableInfo.hasSoftDeleteFieldExcludingSystemCommon()) {
      importMgr.add("org.hibernate.annotations.Filter", "org.hibernate.annotations.FilterDef");
    }

    // @Filterを使用する場合はimport
    if (info.groupRootInfo.isDefined()) {
      if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
        // 共通group定義が存在する場合はそのfilterDefは必ずsystemCommonに出力
        importMgr.add("org.hibernate.annotations.FilterDef", "org.hibernate.annotations.ParamDef",
            "org.hibernate.type.descriptor.java.*");

        // systemCommonに共通group項目を持っている場合
        if (tableInfo.hasGroupColumn()) {
          importMgr.add("org.hibernate.annotations.Filter");
        }

      } else {
        if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY
            && !info.groupRootInfo.getTableNamesWithoutGrouping().contains(tableInfo.getName())) {

          importMgr.add("org.hibernate.annotations.Filter");

          // customGroupColumnを持っている場合は、filterDefなども必要。
          if (tableInfo.hasCustomGroupColumn()) {
            importMgr.add("org.hibernate.annotations.FilterDef",
                "org.hibernate.annotations.ParamDef", "org.hibernate.type.descriptor.java.*");
          }

          // Tableがorg.hibernate.annotations.*にも含まれている関係で、明示的な定義が必要になった。。
          importMgr.add("jakarta.persistence.Table");
        }
      }
    }

    // relationを使用する場合
    if (tableInfo.hasRelation()) {
      importMgr.add("jakarta.validation.*");
      importMgr.add("org.hibernate.annotations.OnDelete",
          "org.hibernate.annotations.OnDeleteAction");
    }

    // entityの種類ごとに必要なもの
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) {
      // 親entityは同一パッケージ内にいるのでimport不要
      // baseRecord
      importMgr.add(rootBasePackage + ".base.record." + tableNameCp + "BaseRecord");

      importMgr.add("jakarta.annotation.Nonnull");

      // bidirectionalInfoでかつOneToManyの場合は、明示的にbidirectionの参照元のBaseRecordをimportする必要あり。
      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          if (info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
            importMgr.add(rootBasePackage + ".base.record."
                + StringUtil.getUpperCamelFromSnake(info.getOrgTableName()) + "BaseRecord");
          }
        }
      }

    } else if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      // 親entity
      importMgr.add(EclibCoreConstants.PKG_PARENT + ".jpa.entity.EclibEntity");
      // baseRecord
      importMgr.add(rootBasePackage + ".base.record.SystemCommonBaseRecord");
      // auditing. springの場合のみ。本当はsystemCommon決め打ちではないのだが、簡易的にこうしておく
      importMgr.add("org.springframework.data.jpa.domain.support.*");
    }

    // 項目の型別にimport必須のものを取り込む
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      DataTypeInfo dtInfo = colInfo.getDtInfo();
      importMgr.add(getHelper(dtInfo.getKata()).getNeededImports(colInfo));
    }

    // 使用するenumクラスをimport
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      String dataType = colInfo.getDataType();
      DataTypeInfo dtInfo = colInfo.getDtInfo();
      if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
        // importMgr.add("jp.ecuacion.lib.core.util.EnumUtil");

        String importClassStr = getRootBasePackageOfDataTypeFromAllSystem(colInfo.getDataType())
            + ".base.enums." + CodeGenUtil.dataTypeNameToUppperCamel(dataType) + "Enum";
        importMgr.add(importClassStr);
        // batch（javaSE環境）だと@ConverterにautoApply =
        // trueをつけても無視され、明示的に@Convertタグを書く必要がある関係で、Converterのimportが必要
        importClassStr = getRootBasePackageOfDataTypeFromAllSystem(colInfo.getDataType())
            + ".base.converter." + CodeGenUtil.dataTypeNameToUppperCamel(dataType) + "Converter";
        importMgr.add(importClassStr);
      }
    }

    // import文を書き出し。クラス宣言との間を一行開けるためにRTを追加。
    sb.append(importMgr.outputStr() + RT);
  }

  private void auditingImport(ImportGenUtil importMgr, String springAuditing, String keyword,
      String importClass) {
    if (springAuditing != null && springAuditing.equals(keyword)) {
      importMgr.add("org.springframework.data.annotation." + importClass);
    }
  }

  /**
   * 共通のgrouping用FilterDefを出力するために使用。
   */
  protected void getGroupFilterDefAnnotationString(StringBuilder sb) throws BizLogicAppException {
    getGroupFilterDefAnnotationString(sb, "groupFilter", info.groupRootInfo.getColumnName(),
        info.groupRootInfo.getDtInfo());
  }

  protected void getGroupFilterDefAnnotationString(StringBuilder sb, String filterName,
      String colName, DataTypeInfo dtInfo) throws BizLogicAppException {

    String fieldNameLc = StringUtil.getLowerCamelFromSnake(colName);

    sb.append("@FilterDef(name = \"" + filterName + "\", " + RT);
    sb.append(T2 + "parameters = @ParamDef(name = \"" + fieldNameLc + "\", type = "
        + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().getName()) + "JavaType.class)," + RT);
    sb.append(T2 + "defaultCondition = \"" + colName + " = :" + fieldNameLc + "\")" + RT);
  }

  /**
   * 共通のgrouping用Filterを出力するために使用。
   */
  protected void getGroupFilterAnnotationString(StringBuilder sb) throws BizLogicAppException {
    getGroupFilterAnnotationString(sb, "groupFilter");
  }

  protected void getGroupFilterAnnotationString(StringBuilder sb, String filterName)
      throws BizLogicAppException {
    ParamListGen paramGenList = new ParamListGen();
    paramGenList.add(new ParamGenWithSingleValue("name", filterName, true));

    NormalSingleAnnotationGen filter =
        new NormalSingleAnnotationGen("Filter", ElementType.TYPE, paramGenList);
    sb.append(filter.generateString(ElementType.TYPE) + RT);
  }

  protected void getSoftDeleteAnnotationsString(StringBuilder sb, DbOrClassTableInfo tableInfo)
      throws BizLogicAppException {
    if (info.sysCmnRootInfo.isFrameworkKindSpring() && info.removedDataRootInfo.isDefined()
        && tableInfo.hasSoftDeleteFieldExcludingSystemCommon()) {
      sb.append("@FilterDef(name = \"softDeleteFilter\", defaultCondition = \""
          + info.removedDataRootInfo.getColumnName() + " = false\")" + RT);
      sb.append("@Filter(name = \"softDeleteFilter\")" + RT);
    }
  }

  // serialVersionUID。1固定
  protected void appendSerialVersionUid(StringBuilder sb) {
    sb.append(T1 + "private static final long serialVersionUID = 1L;" + RT2);
  }

  /**
   * @param tableInfo Entity, EntityPkはこの値が必須。それ以外はnullでよい。(isPkPart == nullならばtableInfo ==
   *        nullであるという前提に立っている）
   */
  protected void appendField(StringBuilder sb, DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> colInfoList) {

    for (DbOrClassColumnInfo ci : colInfoList.stream().filter(e -> !e.getIsJavaOnly()).toList()) {
      createFieldInternal(sb, tableInfo.getName(), ci);

      // colが@Idでかつrelationの場合、@MapsIdのために通常の@Idカラムも必要
      if (ci.isPk() && ci.isRelationColumn()) {
        DbOrClassColumnInfo ci2 = DbOrClassColumnInfo.cloneWithoutRelationRelated(ci);
        createFieldInternal(sb, tableInfo.getName(), ci2);
      }

      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          sb.append(T1 + info.getRelationKind().getName()
              + "(cascade={CascadeType.DETACH, CascadeType.REMOVE}, mappedBy = \""
              + info.getOrgFieldNameToReferDst() + "\")" + RT);
          String refEntityNameLw = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());

          if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
            sb.append(T1 + "protected " + StringUtils.capitalize(refEntityNameLw) + " "
                + info.getEmptyConsideredFieldNameToReferFromTable() + ";" + RT2);

          } else {
            MiscSoftDeleteRootInfo softDeleteInfo = MainController.tlInfo.get().removedDataRootInfo;
            MiscGroupRootInfo groupInfo = MainController.tlInfo.get().groupRootInfo;
            if (softDeleteInfo.isDefined()) {
              sb.append(T1 + "@Filter(name = \"softDeleteFilter\")" + RT);
            }
            if (groupInfo.isDefined()) {
              sb.append(T1 + "@Filter(name = \"groupFilter\")" + RT);
            }
            sb.append(T1 + "protected List<" + StringUtils.capitalize(refEntityNameLw) + "> "
                + info.getEmptyConsideredFieldNameToReferFromTable() + ";" + RT2);
          }
        }
      }
    }
  }

  private void createFieldInternal(StringBuilder sb, String tableName, DbOrClassColumnInfo ci) {

    String columnName = StringUtil.getLowerCamelFromSnake(ci.getName());
    String kata = getEnumConsideredKata(ci);

    // 第二引数は、CommonInfoの場合nullなので、nullを考慮
    sb.append(getEntityFieldAnnotations(getEntityGenKindEnum(), tableName, ci,
        code.classDotField(tableName, ci)));

    if (ci.isRelationColumn()) {
      sb.append(T1 + "@Valid" + RT);
      sb.append(T1 + ci.getRelationKind().getName() + "(fetch = FetchType."
          + (ci.getRelationIsEager() ? "EAGER" : "LAZY") + ", cascade = {CascadeType.DETACH})"
          + RT);
      sb.append(T1 + "@OnDelete(action = OnDeleteAction.CASCADE)" + RT);
      sb.append(T1 + "@JoinColumn(name = \"" + ci.getName() + "\", referencedColumnName = \""
          + ci.getRelationRefCol() + "\", nullable = " + (ci.isNullable() ? "true" : "false") + ")"
          + RT);
      if (ci.isPk()) {
        sb.append(T1 + "@MapsId" + RT);
      }

      String entityNameUp = StringUtil.getUpperCamelFromSnake(ci.getRelationRefTable());
      String fieldNameLw = ci.getRelationFieldName();
      sb.append(T1 + "private " + StringUtils.capitalize(entityNameUp) + " " + fieldNameLw
          + " = new " + StringUtils.capitalize(entityNameUp) + "();" + RT2);

    } else {
      // 通常であればprivateにしてしまってよいのだが、EclibEntityの項目（LST_UPD_TIMEなど）も生成しており、
      // その場合はprotectedである必要がある。
      // どうせEntityはfinal classとして作成しているので、ここはprotectedでもいいかなと^^;
      sb.append(T1 + "protected " + kata + " " + columnName + ";" + RT2);
    }
  }

  /**
   * @param tableNameCp Entity, Pkの場合のみ値が必要。それ以外はnullを渡してもOK。
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

  protected void appendFieldNameArr(StringBuilder sb, DbOrClassTableInfo tableInfo,
      String entityNameCp, boolean isInGetPkOfSurrogateKeyStrategyEntity) {
    // systemCommonについては作成しない
    if (getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      return;
    }

    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public String[] getFieldNameArr() {" + RT);
    sb.append(T2 + "return new String[] {");

    // このリストはdbCommmonの項目も同時に表示するので、あらかじめマージしておく
    ArrayList<DbOrClassColumnInfo> arr = new ArrayList<>();
    arr.addAll(tableInfo.columnList);
    if (info.dbCommonRootInfo != null) {
      arr.addAll(info.dbCommonRootInfo.tableList.get(0).columnList.stream()
          .filter(e -> !e.getIsJavaOnly()).toList());
    }

    boolean isFirst = true;
    for (DbOrClassColumnInfo ci : arr) {
      // surrogateKeyStorategyのBODYにあるgetPk内の場合は、Pk項目のみのFieldInfoを並べるため、Pkでない場合はスキップ
      if (isInGetPkOfSurrogateKeyStrategyEntity && !ci.isPk()) {
        continue;
      }

      // entityPkの場合はpk項目のみ作成
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

  protected void appendDefaultConstructor(StringBuilder sb, String tableNameCp) {
    sb.append(T1 + JD_ST + "defaultコンストラクタ" + JD_END + RT);
    sb.append(T1 + "public " + tableNameCp + "() {" + "}" + RT2);
  }

  protected void appendRecConstructor(StringBuilder sb, DbOrClassTableInfo tableInfo,
      String entityNameCp) {
    // recを引数としたコンストラクタ
    sb.append(T1 + "/** recordを引数にとるコンストラクタ */" + RT);
    // BaseRecordの前の部分に"Pk"が入らないように、あえてPkがつかない名前を取得している
    sb.append(T1 + "public " + entityNameCp + "("
        + (this instanceof SystemCommonEntityGen ? "SystemCommon"
            : StringUtil.getUpperCamelFromSnake(tableInfo.getName()))
        + "BaseRecord rec) throws MultipleAppException {" + RT);
    sb.append(T2 + "super("
        + ((getEntityGenKindEnum() == EntityGenKindEnum.ENTITY_BODY) ? "rec" : "") + ");" + RT);

    tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly()).toList().forEach(ci -> {
      substituteFieldsFromRecordToEntity(sb, ci, true);
    });

    sb.append(T1 + "}" + RT2);
  }

  protected void appendNaturalKeyConstructor(StringBuilder sb, DbOrClassTableInfo tableInfo,
      String entityNameCp) {
    sb.append(T1 + "/**" + RT);
    sb.append(T1 + " * naturalKeyを引数にとるコンストラクタ。" + RT);
    sb.append(T1 + " * naturalKeyとsurrogateKeyのコンストラクタを両方作ると重複する可能性があるため、naturalKeyのみとする。" + RT);
    sb.append(T1 + " * （surrogateKeyは、insertの際は入力しない、"
        + "selectの際はEntity.getPk(field)でPk取得、updateの際はselectしたものを使用することから、" + RT);
    sb.append(T1 + " * naturalKeyよりconstructorの引数に設定したい状況は少ないと思われる） " + RT);
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

      sb.append(comma + getEnumConsideredKata(ci) + " "
          + StringUtil.getLowerCamelFromSnake(ci.getName()));
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

  protected void substituteFieldsFromRecordToEntity(StringBuilder sb, DbOrClassColumnInfo ci,
      boolean usesRec) {
    // if (createsField(ci)) {
    String leftHandSide = "set" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "(";
    DataTypeInfo dtInfo = ci.getDtInfo();
    String fieldNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
    String fieldNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
    String kataUc = getEnumConsideredKata(dtInfo);

    // relationがある場合は、インスタンス生成を行う
    String relFieldNameUc = StringUtils.capitalize(ci.getRelationFieldName());
    if (ci.isRelationColumn()) {
      String entityName = StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
      sb.append(T2 + ci.getRelationFieldName() + " = rec.get" + relFieldNameUc
          + "() == null ? null : new " + StringUtils.capitalize(entityName) + "(rec.get"
          + relFieldNameUc + "());" + RT);

    } else {

      if (dtInfo.getKata() == DataTypeKataEnum.FLOAT
          || dtInfo.getKata() == DataTypeKataEnum.DOUBLE) {
        sb.append(
            T2 + leftHandSide + "(" + ((usesRec) ? "rec.get" + fieldNameUc + "()" : fieldNameLc)
                + " == null || " + ((usesRec) ? "rec.get" + fieldNameUc + "()" : fieldNameLc)
                + ".equals(\"\"))? null: " + kataUc + ".valueOf"
                + ((usesRec) ? "(rec.get" + fieldNameUc + "().replaceAll(\",\", \"\"))"
                    : "(" + fieldNameLc + ")")
                + ");" + RT);

        // } else if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
        // String obtainedValue =
        // ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
        // : StringUtil.getLowerCamelFromSnake(ci.getName()));
        // sb.append(T2 + leftHandSide + obtainedValue + " == null ? null :
        // EnumUtil.getEnumFromCode("
        // + CodeGenUtil.dataTypeNameToUppperCamel(dtInfo.getDataTypeName()) + "Enum.class, "
        // + obtainedValue + "));" + RT);

      } else if (CodeGenUtil.ofEntityTypeMethodAvailableDataTypeList.contains(dtInfo.getKata())) {
        sb.append(T2 + fieldNameLc + " = rec.get" + fieldNameUc + "OfEntityDataType();" + RT);

      } else if (dtInfo.getKata() == DataTypeKataEnum.BOOLEAN) {
        // booleanは、record上ではBooleanで保持されているため、Stringから読み込む場合（userRec ==
        // falseの場合）のみBoolean.valueOfを付加
        sb.append(T2 + leftHandSide
            + ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
                : ("Boolean.valueOf(" + StringUtil.getLowerCamelFromSnake(ci.getName())) + ")")
            + ");" + RT);

      } else {
        sb.append(T2 + leftHandSide
            + ((usesRec) ? "rec.get" + StringUtil.getUpperCamelFromSnake(ci.getName()) + "()"
                : StringUtil.getLowerCamelFromSnake(ci.getName()))
            + ");" + RT);
      }
    }

    // bidirectional relationの参照先カラムの場合はそのfieldの代入も追加
    if (ci.isReferedByBidirectionalRelation()) {
      for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
        String entityNameUc = StringUtil.getUpperCamelFromSnake(info.getOrgTableName());
        String entityNameLc = StringUtils.uncapitalize(entityNameUc);

        String bidirFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
        String bidirFieldNameUc = StringUtils.capitalize(bidirFieldName);

        // ID column of ref-from table
        String pkColumnInOrgTable = MainController.tlInfo.get().dbRootInfo.tableList.stream()
            .filter(tbl -> tbl.getName().equals(info.getOrgTableName())).toList().get(0)
            .getPkColumn().getName();
        if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
          sb.append(T2 + bidirFieldName + " = rec.get" + bidirFieldNameUc + "() == null || rec.get"
              + bidirFieldNameUc + "().get" + StringUtil.getUpperCamelFromSnake(pkColumnInOrgTable)
              + "() == null ? null : new " + StringUtils.capitalize(entityNameLc) + "(rec.get"
              + bidirFieldNameUc + "());" + RT);

        } else {
          sb.append(
              T2 + "if (rec.get" + StringUtils.capitalize(bidirFieldName) + "() != null) {" + RT);
          sb.append(T3 + bidirFieldName + " = new ArrayList<>();" + RT);
          sb.append(T3 + "for (" + entityNameUc + "BaseRecord " + entityNameLc + "Rec : rec.get"
              + StringUtils.capitalize(bidirFieldName) + "()) {" + RT);
          sb.append(T4 + bidirFieldName + ".add(new " + entityNameUc + "(" + entityNameLc + "Rec));"
              + RT);
          sb.append(T3 + "}" + RT);
          sb.append(T2 + "}" + RT);
        }
      }
    }
  }

  protected void appendUpdate(StringBuilder sb, DbOrClassTableInfo tableInfo)
      throws BizLogicAppException {
    List<DbOrClassColumnInfo> baseList =
        tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly()).filter(e -> !e.isPk())
            .filter(e -> !e.isGroupColumn()).toList();

    StringBuilder relString = new StringBuilder();
    baseList.stream().filter(e -> e.isRelationColumn()).forEach(ci -> relString.append(
        ", " + code.capitalCamel(ci.getRelationRefTable()) + " " + ci.getRelationFieldName()));
    sb.append(T1 + "public void update(" + code.baseRecDef(tableInfo.getName()) + relString
        + ", String... skipUpdateFields) {" + RT);

    // Remove groupColumn to avoid the data to be moved to other group (=normally other customer's
    // dara realm).
    if (baseList.stream().filter(ci -> !ci.isRelationColumn()).toList().size() > 0) {
      sb.append(T2 + "List<String> skipUpdateFieldList = Arrays.asList(skipUpdateFields);" + RT2);
    }

    for (DbOrClassColumnInfo ci : baseList) {
      if (ci.isRelationColumn()) {
        String name = ci.getRelationFieldName();
        sb.append(T2 + "if (" + name + " != null) set"
            + StringUtils.capitalize(ci.getRelationFieldName()) + "(" + name + ");" + RT);

      } else {
        String name = code.uncapitalCamel(ci.getName());
        sb.append(T2 + "if (" + code.recGet(name) + " != null && !skipUpdateFieldList.contains("
            + "FIELD_" + ci.getName() + ")) " + code.set(name, code.recGetOfEntityType(ci)) + ";"
            + RT);
      }
    }

    sb.append(T1 + "}" + RT2);
  }

  protected void appendAccessor(StringBuilder sb, DbOrClassTableInfo tableInfo) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      String columnNameCp = StringUtil.getUpperCamelFromSnake(ci.getName());
      String columnNameSm = StringUtil.getLowerCamelFromSnake(ci.getName());
      final String relEntityName = ci.getRelationRefTable() == null ? null
          : StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
      String relFieldName = ci.getRelationRefCol() == null ? null
          : StringUtil.getUpperCamelFromSnake(ci.getRelationRefCol());

      sb.append(T1 + "public " + getEnumConsideredKata(ci) + " get" + columnNameCp + "() {" + RT);
      sb.append(T2 + "return "
          + (ci.isRelationColumn()
              ? ci.getRelationFieldName() + " == null ? null : " + ci.getRelationFieldName()
                  + ".get" + relFieldName + "()"
              : columnNameSm)
          + ";" + RT);
      sb.append(T1 + "}" + RT2);

      sb.append(T1 + "public void set" + columnNameCp + "(" + getEnumConsideredKata(ci) + " "
          + columnNameSm + ") {" + RT);
      sb.append(T2 + "this."
          + (ci.isRelationColumn()
              ? ci.getRelationFieldName() + ".set" + relFieldName + "(" + columnNameSm + ")"
              : columnNameSm + " = " + columnNameSm)
          + ";" + RT);
      sb.append(T1 + "}" + RT2);

      if (ci.isRelationColumn()) {
        // relationのcolumnの場合は、別途entityを示すfield自体のaccessorも用意しておく
        appendAccessorForRelation(sb, relEntityName, ci.getRelationFieldName(), false, null);
      }

      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          appendAccessorForRelation(sb, StringUtil.getLowerCamelFromSnake(info.getOrgTableName()),
              info.getEmptyConsideredFieldNameToReferFromTable(), true, info);
        }
      }
    }
  }

  private void appendAccessorForRelation(StringBuilder sb, String relEntityName,
      String relFieldName, boolean isReferedByBidirectionalRelation,
      BidirectionalRelationInfo info) {
    // bidirectionの参照先の場合で、かつoneToManyの場合、それを考慮したfieldName, relEntityNameの値に変更
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

  protected String getEnumConsideredKata(DbOrClassColumnInfo colInfo) {
    return getEnumConsideredKata(colInfo.getDtInfo());
  }

  private LinkedHashMap<String, String> createSortedMapForPropFile(String lang,
      List<DbOrClassTableInfo> tableList, EntityGenKindEnum entityKind) {
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

    // 各テーブルに対しmapにfieldの表示名を格納
    if (tableList != null) {
      for (DbOrClassTableInfo tableInfo : tableList) {
        putToMap(lang, tableInfo, map, entityKind);
      }
    }

    return map;
  }

  private void putToMap(String lang, DbOrClassTableInfo tableInfo,
      LinkedHashMap<String, String> map, EntityGenKindEnum entityKind) {

    // // SystemCommonに持つ項目（例えばcreateTime)に対しても、SystemCommon.createTimeに加えて
    // // Acc.createTimeも作成する必要があるのでそれも含めたlistを保持

    for (DbOrClassColumnInfo columnInfo : tableInfo.columnList) {
      String entityName = StringUtil.getUpperCamelFromSnake(tableInfo.getName());
      String varName = StringUtil.getLowerCamelFromSnake(columnInfo.getName());
      String dispName = columnInfo.getDisplayNameMap().get(lang);
      map.put(entityName + "." + varName, dispName);

      // // spring mvcでのメッセージ出力時の項目に対応するため、entityNameの頭文字を小文字に変更したものも登録
      map.put(StringUtils.uncapitalize(entityName) + "." + varName, dispName);
    }
  }

  /**
   * item_names_xx.propertiesを作成するための共通処理。
   */
  protected void appendItemNamesProperties(EntityGenKindEnum entityKind)
      throws IOException, InterruptedException, BizLogicAppException {
    PropertiesFileGen gen = new PropertiesFileGen();

    // fallback用のファイルを作成。
    gen.writeMapToPropFile(createSortedMapForPropFile(info.sysCmnRootInfo.getDefaultLang(),
        getTableList(), entityKind), "item_names", null);
    // default言語用のファイルを作成
    gen.writeMapToPropFile(createSortedMapForPropFile(info.sysCmnRootInfo.getDefaultLang(),
        getTableList(), entityKind), "item_names", info.sysCmnRootInfo.getDefaultLang());
    // supportedLangArrに入っているものについて作成
    for (String lang : info.sysCmnRootInfo.getSupportedLangArr()) {
      gen.writeMapToPropFile(createSortedMapForPropFile(lang, getTableList(), entityKind),
          "item_names", lang);
    }
  }

  protected void appendAutoInsertOrUpdateGen(StringBuilder sb, DbOrClassTableInfo tableInfo,
      boolean isUpdate, boolean isFromSystemCommon) throws BizLogicAppException {

    // 対象となるfieldが一つもない場合はそもそも本メソッドを作りたくないのでその判断を先にする
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

    // 以下、メソッド作成が必要な場合。
    sb.append(T1 + ((isUpdate) ? "@PreUpdate" : "@PrePersist") + RT);
    sb.append(T1 + "public void " + ((isUpdate) ? "preUpdate" : "preInsert") + "() {" + RT);

    // SystemAdminでない場合はSystemCommonの同メソッドを呼び出す処理を入れる。
    // SystemCommonにprePersist / preUpdateがない場合を考慮できていないが・・・
    if (!isFromSystemCommon) {
      sb.append(T2 + "//@PrePersist, @PreUpdateは、子クラスに実装してしまうと親クラス側が呼ばれなくなるため、本メソッドの中で呼び出す" + RT);
      sb.append(T2 + "super." + ((isUpdate) ? "preUpdate" : "preInsert") + "();" + RT2);

    }
    for (DbOrClassColumnInfo colInfo : tableInfo.columnList) {
      DataTypeInfo dtInfo = colInfo.getDtInfo();
      String fieldName = StringUtil.getLowerCamelFromSnake(colInfo.getName());
      boolean isForced =
          !isUpdate && colInfo.isForcedIncrement() || isUpdate && colInfo.isForcedUpdate();

      if (!needsAutoInsertOrUpdate(colInfo, isUpdate)) {
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
        String kataName = getEnumConsideredKata(colInfo);
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName + " = "
            + kataName + ".now();" + RT);

      } else if (dtInfo.getKata() == DataTypeKataEnum.SHORT
          || dtInfo.getKata() == DataTypeKataEnum.INTEGER
          || dtInfo.getKata() == DataTypeKataEnum.LONG) {
        sb.append(T2 + ((isForced) ? "" : "if (" + fieldName + " == null) ") + fieldName + " = 1"
            + ((dtInfo.getKata() == DataTypeKataEnum.LONG) ? "L" : "") + ";" + RT);

      } else {
        throw new BizLogicAppException("MSG_ERR_IMPOSSIBLE");
      }
    }

    sb.append(T1 + "}" + RT2);
  }

  /**
   * 各cInfoに対して、preInsert/preUpdateが必要かを判断する。
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

  /** Pkの各項目を引数で与える場合の( )内の文字列を取得。 **/
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
        String tmpKata = StringUtil.getUpperCamelFromSnake(dtInfo.getKata().getName());
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

  protected List<DbOrClassColumnInfo> getMergedColumnList(DbOrClassTableInfo tableInfo,
      List<DbOrClassColumnInfo> commonColumnList) {
    // commonColumnListをmergeしたlistを作成
    List<DbOrClassColumnInfo> mergedList = new ArrayList<>();
    mergedList.addAll(tableInfo.columnList);
    mergedList.addAll(commonColumnList);

    return mergedList;
  }

  /**
   * Entityとしての削除フラグの保持有無を返すメソッドを生成。
   * 
   * <p>
   * 当entityに削除フラグ項目を持っている場合（SystemCommonEntityからの呼び出しの場合、
   * 個々のEntityではなくSystemCommonEntityに項目を持っている場合）は、具体的にメソッドとして定義。
   * SystemCommonEntityからの呼び出しだが、個々のentity側で定義される場合は、abstractメソッドとして定義を行う。
   * </p>
   * 具体的には、
   * <ul>
   * <li>1-1.「SystemCommonからの呼び出し」かつ「削除フラグ使用」かつ「削除フラグ項目を持っていない」場合はabstract定義</li>
   * <li>1-2.「SystemCommonからの呼び出しでない」かつ「削除フラグ使用」かつ「削除フラグ項目を持っていない」場合は出力なしで終了</li>
   * <li>2.「削除フラグ使用」かつ「削除フラグ項目を持っている」場合はtrueを返す</li>
   * <li>3. 上記2パターン以外の場合はfalseを返す</li>
   * </ul>
   */
  protected void appendHasSoftDeleteFieldGen(StringBuilder sb, DbOrClassTableInfo tableInfo,
      boolean isCallFromSystemCommon) {
    String colName = ((MiscSoftDeleteRootInfo) info.rootInfoMap.get(DataKindEnum.MISC_REMOVED_DATA))
        .getColumnName();
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
   * entityのフィールドに付加するvalidatorを生成。
   */
  private String getEntityFieldAnnotations(EntityGenKindEnum entityGenKindEnum, String tableName,
      DbOrClassColumnInfo colInfo, String id) {
    List<AnnotationGen> annotationGenList = new ArrayList<>();

    // validator
    annotationGenList.addAll(colInfo.getValidatorList(true));

    // relationColumnの場合はこれ以下は不要
    if (colInfo.isRelationColumn()) {
      // 文字列出力。isRelationColumn() == trueの場合はNotEmptyのみが対象。
      return AnnotationGenUtil.getCode(annotationGenList, ElementType.FIELD);
    }

    // 以下はisRelationColumn() == falseの場合のみ

    // id
    if (IdGen.needsValidator(colInfo)) {
      annotationGenList.add(new IdGen(ElementType.FIELD, colInfo.getDtInfo()));
    }

    // version
    if (VersionGen.needsValidator(colInfo.isOptLock())) {
      annotationGenList.add(new VersionGen(ElementType.FIELD));
    }

    // 監査関連
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

    // 文字列出力
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
