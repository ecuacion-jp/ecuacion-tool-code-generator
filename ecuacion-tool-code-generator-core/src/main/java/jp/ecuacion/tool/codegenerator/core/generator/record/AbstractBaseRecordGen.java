package jp.ecuacion.tool.codegenerator.core.generator.record;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BOOLEAN;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DOUBLE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.ENUM;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.FLOAT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.BidirectionalRelationInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.dao.AbstractDaoRelatedGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil.ColFormat;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractBaseRecordGen extends AbstractDaoRelatedGen {

  protected CodeGenUtil code = new CodeGenUtil();

  protected abstract void generateHeader(DbOrClassTableInfo ti) throws AppException;

  protected abstract void generateMethods(DbOrClassTableInfo ti) throws AppException;

  public AbstractBaseRecordGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);
  }

  protected void internalGenerate(List<DbOrClassTableInfo> tiList, boolean isSystemCommon)
      throws AppException {
    for (DbOrClassTableInfo ti : tiList) {
      sb = new StringBuilder();

      generateHeader(ti);
      generateFieldsCommon(ti);
      generateStaticInitializerCommon(ti);
      generateConstNoArgsCommon(ti);
      generateConstEntityArgCommon(ti, isSystemCommon);
      createConstRecArgCommon(ti, isSystemCommon);
      createAccessorCommon(ti);
      createListsForHtmlSelectCommon(ti);
      generateMethods(ti);

      sb.append("}" + RT);

      outputFile(sb, getFilePath("record"), ti.getNameCpCamel() + "BaseRecord.java");
    }
  }

  protected void generateHeaderCommon(DbOrClassTableInfo ti, String... imps) {
    sb.append("package " + rootBasePackage + ".base.record;" + RT2);

    ImportGenUtil imp = new ImportGenUtil();
    imp.add(imps);

    // Add kata-dependent imports.
    ti.columnList.stream()
        .peek(ci -> imp.add(code.getHelper(ci.getDtInfo().getKata()).getNeededImports(ci)))
        // Add validators.
        .filter(ci -> !ci.getIsJavaOnly())
        .forEach(ci -> imp.add(AnnotationGenUtil.getNeededImports(ci.getValidatorList(false))));

    // Add DateTimeFormatter when the kata is timestamp.
    if (ti.hasColumnWithAnyOfKatas(TIMESTAMP, DATE_TIME, DATE, TIME)) {
      imp.add("java.time.format.DateTimeFormatter");
    }

    if (ti.hasColumnWithKata(ENUM)) {
      imp.add("java.util.List", "java.util.Locale", EclibCoreConstants.PKG + ".util.EnumUtil");
      ti.getColumnListWithKata(ENUM).stream()
          .forEach(ci -> imp.add(rootBasePackage + ".base.enums." + code.getJavaKata(ci)));
    }

    if (ti.hasColumnWithKata(BOOLEAN)) {
      imp.add("java.util.List", "java.util.Locale",
          EclibCoreConstants.PKG + ".util.PropertyFileUtil");
    }

    // Add @Valid imports for relation columns
    if ((ti.hasRelation() || ti.hasBidirectionalRelationRef())
        && info.sysCmnRootInfo.isFrameworkKindSpring()) {
      imp.add("jakarta.validation.Valid");
    }

    // Add imports for columns referred by bidirectional relation.
    ti.columnList.stream().filter(ci -> ci.isReferedByBidirectionalRelation())
        .map(ci -> ci.getBidirectionalInfoList()).flatMap(l -> l.stream())
        .filter(info -> info.getRelationKind() == RelationKindEnum.ONE_TO_MANY)
        .forEach(info -> imp.add("java.util.ArrayList", "java.util.List", rootBasePackage
            + ".base.entity." + StringUtil.getUpperCamelFromSnake(info.getOrgTableName())));

    // output
    sb.append(imp.outputStr() + RT);
  }

  protected void generateFieldsCommon(DbOrClassTableInfo ti) {
    ti.columnList.stream().forEach(ci -> fieldDefinition(ti.getName(), ci));
    sb.append(RT);
  }

  private void fieldDefinition(String tableName, DbOrClassColumnInfo ci) {
    final String refEnName =
        ci.getRelationRefTable() == null ? null : ci.getRelationRefTableCamel();
    String kata = ci.getDtInfo().getKata() == BOOLEAN ? "Boolean" : "String";

    sb.append(AnnotationGenUtil.getCode(ci.getValidatorList(false), ElementType.FIELD));

    if (ci.isRelation()) {
      sb.append(T1 + "@Valid" + RT);
    }

    String rel = StringUtils.capitalize(refEnName) + "BaseRecord " + ci.getRelationFieldName();
    sb.append(
        T1 + "protected " + (ci.isRelation() ? rel : kata + " " + ci.getNameCamel()) + ";" + RT);

    // Add method for bidirectional relation if the column has it.
    for (BidirectionalRelationInfo info : ci.getBidirectionalInfoList()) {
      sb.append(T1 + "@Valid" + RT);
      boolean is1To1 = info.getRelationKind() == RelationKindEnum.ONE_TO_ONE;
      String recKata = is1To1 ? info.getOrgTableNameCpCamel() + "BaseRecord"
          : "List<" + info.getOrgTableNameCpCamel() + "BaseRecord>";
      String postfix = (is1To1 ? "" : " = new ArrayList<>()") + ";" + RT;

      sb.append(T1 + "protected " + recKata + " "
          + info.getEmptyConsideredFieldNameToReferFromTable() + postfix);
    }
  }

  protected void generateStaticInitializerCommon(DbOrClassTableInfo ti) {
    sb.append(T1 + "static {" + RT);

    List<DataTypeKataEnum> kataList = Arrays.asList(new DataTypeKataEnum[] {STRING, SHORT, INTEGER,
        LONG, FLOAT, DOUBLE, BIG_INTEGER, BIG_DECIMAL, TIMESTAMP, DATE_TIME});

    ti.columnList.stream().filter(ci -> kataList.contains(ci.getDtInfo().getKata()))
        .forEach(ci -> sb.append(T2 + "getStringLengthMap().put(\"" + ci.getNameCamel() + "\", "
            + ci.getDtInfo().getMaxLength() + ");" + RT));

    sb.append(T1 + "}" + RT2);
  }

  protected void generateConstNoArgsCommon(DbOrClassTableInfo ti) {
    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord() {" + RT);

    boolean bl = ti.hasAnyRelationsOrRefs();
    sb.append(
        T2 + (bl ? "this(" + Constants.OBJECT_CONSTRUCTION_COUNT + ")" : "super()") + ";" + RT);

    sb.append(T1 + "}" + RT2);

    // Next method not needed for tableInfo without relation or reference.
    if (!ti.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord(int count) {" + RT);
    sb.append(T2 + "super();" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideCreateConstNoArgs(ti);
    sb.append(T1 + "}" + RT2);
  }

  protected void insideCreateConstNoArgs(DbOrClassTableInfo ti) {
    sb.append(T2 + "if (count > 0) {" + RT);

    for (DbOrClassColumnInfo ci : ti.columnList) {
      if (ci.isRelation()) {
        sb.append(T3 + ci.getRelationFieldName() + " = new "
            + StringUtils.capitalize(ci.getRelationRefTableCamel()) + "BaseRecord("
            + (info.getTableInfo(ci.getRelationRefTable()).hasAnyRelationsOrRefs() ? "count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);
      }

      // Add field for bidirectional relation.
      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfoList()) {
          if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
            sb.append(T3 + info.getEmptyConsideredFieldNameToReferFromTable() + " = new "
                + info.getOrgTableNameCpCamel()
                + "BaseRecord(count) {public EclibItem[] getItems() {return null;}};" + RT);
          }
        }
      }
    }

    sb.append(T2 + "}" + RT);
  }

  public void generateConstEntityArgCommon(DbOrClassTableInfo ti, boolean isSystemCommon)
      throws AppException {
    boolean bl = ti.hasAnyRelationsOrRefs();

    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord(" + ti.getNameCpCamel()
        + " e, DatetimeFormatParameters params) {" + RT);
    sb.append(T2 + (bl ? "this(e, params, " + Constants.OBJECT_CONSTRUCTION_COUNT + ")"
        : "super(" + (isSystemCommon ? "" : "e, ") + "params)") + ";" + RT);

    if (!bl) {
      insideConstEntityArg(ti, false);
    }

    sb.append(T1 + "}" + RT2);

    // Next method not needed for tableInfo without relation or reference.
    if (!ti.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord(" + ti.getNameCpCamel()
        + " e, DatetimeFormatParameters params, int count) {" + RT);
    sb.append(T2 + "super(e, params);" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideConstEntityArg(ti, true);

    // Add field for bidirectional relation.
    if (ti.hasBidirectionalRelationRef()) {
      sb.append(RT);
      sb.append(T2 + "if (count > 0) {" + RT);

      for (DbOrClassColumnInfo ci : ti.columnList) {
        if (ci.isReferedByBidirectionalRelation()) {
          for (BidirectionalRelationInfo bi : ci.getBidirectionalInfoList()) {
            String refEnNameCp = bi.getOrgTableNameCpCamel();
            String refFiName = bi.getEmptyConsideredFieldNameToReferFromTable();
            String refFiNameCp = StringUtils.capitalize(refFiName);
            boolean hasRelOrRef = info.getTableInfo(bi.getOrgTableName()).hasAnyRelationsOrRefs();
            String newRecPostfix = ", params" + (hasRelOrRef ? ", count" : "")
                + ") {public EclibItem[] getItems() {return null;}}";

            if (bi.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
              sb.append(T3 + refFiName + " = (e.get" + refFiNameCp + "() == null) ? null : new "
                  + refEnNameCp + "BaseRecord(e.get" + refFiNameCp + "()" + newRecPostfix + ";"
                  + RT);

            } else {
              sb.append(T3 + "if (e.get" + refFiNameCp + "() != null) {" + RT);
              sb.append(T4 + "for (" + refEnNameCp + " en : e.get" + refFiNameCp + "()) {" + RT);
              sb.append(T5 + refFiName + ".add(new " + refEnNameCp + "BaseRecord(en" + newRecPostfix
                  + ");" + RT);
              sb.append(T4 + "}" + RT);
              sb.append(T3 + "}" + RT);
            }
          }
        }
      }

      sb.append(T2 + "}" + RT);
    }

    sb.append(T1 + "}" + RT2);
  }

  protected void insideConstEntityArg(DbOrClassTableInfo tableInfo, boolean isCalledFromB2) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      String fiNameCp = StringUtil.getUpperCamelFromSnake(ci.getName());
      String fiName = StringUtil.getLowerCamelFromSnake(ci.getName());
      DataTypeInfo dtInfo = ci.getDtInfo();

      if (ci.isRelation()) {
        sb.append(isCalledFromB2 ? T2 + "if (count > 0) {" + RT : "");

        boolean hasRel = info.getTableInfo(ci.getRelationRefTable()).hasAnyRelationsOrRefs();
        sb.append((isCalledFromB2 ? T3 : T2) + "this." + ci.getRelationFieldName() + " = new "
            + ci.getRelationRefTableCpCamel() + "BaseRecord(e.get" + ci.getRelationFieldNameCp()
            + "(), params" + (hasRel ? ", count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);

        sb.append(isCalledFromB2 ? T2 + "}" + RT : "");

      } else {
        String kata = StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
        String forTimeZone = dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME
            ? ".withOffsetSameInstant(params.getZoneOffset())"
            : "";
        String get = "e.get" + fiNameCp + "()";

        switch (dtInfo.getKata()) {
          case STRING, BOOLEAN -> sb.append(T2 + "this." + fiName + " = " + get + ";" + RT);
          case INTEGER, SHORT, LONG, FLOAT, DOUBLE, ENUM -> sb.append(T2 + "this." + fiName + " = ("
              + get + " == null) ? \"\" : "
              + (dtInfo.getKata() == ENUM ? get + ".getCode();" : kata + ".toString(" + get + ");")
              + RT);
          case DATE, TIME, DATE_TIME, TIMESTAMP -> sb.append(T2 + "this." + fiName + " = e.get"
              + fiNameCp + "() == null ? \"\" : e.get" + fiNameCp + "()" + forTimeZone
              + ".format(DateTimeFormatter.ofPattern(dateTimeFormatParams.get" + kata
              + "Format()));" + RT);
          default -> sb.append(T2 + "this." + fiName + " = " + get + ".toString();" + RT);
        }
      }
    }
  }

  protected void createConstRecArgCommon(DbOrClassTableInfo ti, boolean isSystemCommon)
      throws AppException {

    boolean bl = ti.hasAnyRelationsOrRefs();

    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord(" + ti.getNameCpCamel()
        + "BaseRecord rec) {" + RT);
    sb.append(T2 + (bl ? "this(rec, " + Constants.OBJECT_CONSTRUCTION_COUNT + ")"
        : "super(rec" + (isSystemCommon ? ".getDateTimeFormatParams()" : "") + ")") + ";" + RT);

    if (!bl) {
      insideConstRecArg(ti);
    }

    sb.append(T1 + "}" + RT2);

    // Next method not needed for tableInfo without relation or reference.
    if (!ti.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T1 + "public " + ti.getNameCpCamel() + "BaseRecord(" + ti.getNameCpCamel()
        + "BaseRecord rec, int count) {" + RT);
    sb.append(T2 + "super(rec);" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideConstRecArg(ti);

    sb.append(T1 + "}" + RT2);
  }

  private void insideConstRecArg(DbOrClassTableInfo tableInfo) {
    if (!tableInfo.hasAnyRelationsOrRefs()) {
      return;
    }

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      String lefthand = "rec.get" + ci.getNameCpCamel();

      if (ci.isRelation()) {
        sb.append(T2 + "this." + ci.getRelationFieldName() + " = new "
            + ci.getRelationRefTableCpCamel() + "BaseRecord("
            + (info.getTableInfo(ci.getRelationRefTable()).hasAnyRelationsOrRefs() ? "count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);
      }

      sb.append(
          T2 + "this." + (ci.isRelation() ? "set" + ci.getNameCpCamel() + "(" + lefthand + "())"
              : ci.getNameCamel() + " = " + lefthand + "()") + ";" + RT);
    }
  }

  protected void createAccessorCommon(DbOrClassTableInfo tableInfo) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      String fiName = ci.getNameCamel();
      String fiNameCp = ci.getNameCpCamel();
      String relFiName = ci.getRelationFieldName();
      String relRefColNameCp =
          ci.getRelationRefCol() == null ? null : ci.getRelationRefColCpCamel();

      sb.append(T1 + "// accessor:" + fiName + RT);

      DataTypeInfo dtInfo = ci.getDtInfo();
      String recKata = dtInfo.getKata() == BOOLEAN ? "Boolean" : "String";
      final String javaKata = code.getJavaKata(ci);

      // getter
      sb.append(T1 + "public " + recKata + " get" + fiNameCp + "() {" + RT);
      String getRel =
          relFiName + " == null ? null : " + relFiName + ".get" + relRefColNameCp + "()";
      sb.append(T2 + "return " + (ci.isRelation() ? getRel : fiName) + ";" + RT);
      sb.append(T1 + "}" + RT2);

      // setter
      String setRel = ci.getRelationFieldName() + ".set" + relRefColNameCp + "(" + fiName + ")";
      sb.append(T1 + "public void set" + fiNameCp + "(" + recKata + " " + fiName + ") {" + RT);
      sb.append(T2 + "this." + (ci.isRelation() ? setRel : fiName + " = " + fiName) + ";" + RT);
      sb.append(T1 + "}" + RT2);

      // getter with OfEntityDataType
      if (CodeGenUtil.ofEntityTypeMethodAvailableDataTypeList.contains(dtInfo.getKata())) {
        sb.append(T1 + "public " + javaKata + " get" + fiNameCp + "OfEntityDataType() {" + RT);
        sb.append(T2 + "return (get" + fiNameCp + "() == null || get" + fiNameCp
            + "().equals(\"\")) ? null : ");

        if (ci.isRelation()) {
          sb.append(code.generateString(ci, ColFormat.GET_OF_ENTITY_DATA_TYPE) + ";" + RT);

        } else {
          switch (dtInfo.getKata()) {
            case DATE, TIME, DATE_TIME, TIMESTAMP -> sb.append(javaKata + ".parse(" + fiName
                + ", DateTimeFormatter.ofPattern(dateTimeFormatParams.get"
                + code.capitalCamel(dtInfo.getKata().toString()) + "Format()));" + RT);
            case ENUM -> sb
                .append("EnumUtil.getEnumFromCode(" + javaKata + ".class, " + fiName + ");" + RT);
            default -> sb
                .append(javaKata + ".valueOf(" + fiName + ".replaceAll(\",\", \"\"));" + RT);
          }
        }

        sb.append(T1 + "}" + RT2);
      }

      // accessor for relation field
      if (ci.isRelation()) {
        String relEntity = ci.getRelationRefTable() == null ? null : ci.getRelationRefTableCamel();
        createAccessorForRelation(relEntity, ci.getRelationFieldName(), null);
      }

      // accessor for bidirectional relation
      ci.getBidirectionalInfoList().stream()
          .forEach(info -> createAccessorForRelation(info.getOrgTableNameCamel(),
              info.getEmptyConsideredFieldNameToReferFromTable(), info));
    }
  }

  private void createAccessorForRelation(String relEntityNameLw, String relFieldName,
      BidirectionalRelationInfo info) {
    String relDataType = StringUtils.capitalize(relEntityNameLw) + "BaseRecord";
    if (info != null && info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
      relFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
      relDataType = "List<" + relDataType + ">";
    }

    sb.append(
        T1 + "public " + relDataType + " get" + StringUtils.capitalize(relFieldName) + "() {" + RT);
    sb.append(T2 + "return " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void set" + StringUtils.capitalize(relFieldName) + "(" + relDataType
        + " " + relFieldName + ") {" + RT);
    sb.append(T2 + "this." + relFieldName + " = " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);
  }

  /**
   * Generates list for UI dropdown lists.
   */
  protected void createListsForHtmlSelectCommon(DbOrClassTableInfo ti) {
    for (DbOrClassColumnInfo ci : ti.getColumnListWithAnyOfKatas(ENUM, BOOLEAN)) {
      DataTypeInfo dtInfo = ci.getDtInfo();

      // Obtain list.
      sb.append(T1 + "public List<String[]> get" + ci.getNameCpCamel()
          + "List(Locale locale, String options) {" + RT);

      switch (dtInfo.getKata()) {
        case ENUM -> sb.append(T2 + "return EnumUtil.getListForHtmlSelect(" + code.getJavaKata(ci)
            + ".class, locale, options);" + RT);
        case BOOLEAN -> sb.append(T2 + "return getBooleanDropdownList(locale, \""
            + ci.getNameCamel() + " \", options);" + RT);
        default -> {
        }
      }

      sb.append(T1 + "}" + RT2);

      // Obtain name.
      sb.append(T1 + "public String get" + ci.getNameCpCamel() + "Name(Locale locale) {" + RT);

      if (dtInfo.getKata() == ENUM) {
        sb.append(T2 + "return EnumUtil.getEnumFromCode(" + code.getJavaKata(ci) + ".class, get"
            + ci.getNameCpCamel() + "()).getDisplayName(locale);" + RT);

      } else if (dtInfo.getKata() == BOOLEAN) {
        sb.append(T2 + "return PropertyFileUtil.getMessage(locale, \"boolean." + ci.getNameCamel()
            + ".\" + " + ci.getNameCamel() + ");" + RT);
      }

      sb.append(T1 + "}" + RT2);
    }
  }
}
