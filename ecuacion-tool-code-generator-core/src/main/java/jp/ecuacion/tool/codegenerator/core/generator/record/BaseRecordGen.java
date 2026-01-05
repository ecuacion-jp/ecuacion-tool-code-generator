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
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.BidirectionalRelationInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.dao.AbstractDaoRelatedGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;
import org.apache.commons.lang3.StringUtils;

public class BaseRecordGen extends AbstractDaoRelatedGen {

  private CodeGenUtil code = new CodeGenUtil();

  public BaseRecordGen(DataKindEnum dataKind) {
    super(dataKind);
  }

  @Override
  public void generate() throws AppException {
    for (DbOrClassTableInfo tableInfo : info.dbRootInfo.tableList) {
      String tableNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());
      sb = new StringBuilder();

      createHeader(tableInfo, tableNameCp);
      createStaticInitializer(tableInfo, tableNameCp);
      createConstA(tableInfo, tableNameCp);
      createConstA2(tableInfo, tableNameCp);
      createConstB(tableInfo, tableNameCp, false);
      createConstB2(tableInfo, tableNameCp, false);
      createConstC(tableInfo, tableNameCp, false);
      createConstC2(tableInfo, tableNameCp, false);
      createAccessor(tableInfo, tableNameCp);
      createListsForHtmlSelect(tableInfo);

      sb.append("}" + RT);

      outputFile(sb, getFilePath("record"), tableNameCp + "BaseRecord.java");
    }
  }

  public void createHeader(DbOrClassTableInfo tableInfo, String tableNameCp) throws AppException {
    sb.append("package " + rootBasePackage + ".base.record;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();

    createHeaderCommon(importMgr, tableInfo);

    importMgr.add(rootBasePackage + ".base.entity." + tableNameCp);
    importMgr.add("jp.ecuacion.splib.core.container.*", "jp.ecuacion.lib.core.item.*");

    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class " + tableNameCp
        + "BaseRecord extends SystemCommonBaseRecord implements EclibItemContainer {" + RT2);

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      // field定義
      fieldDefinition(tableInfo.getName(), ci);
    }

    sb.append(RT);
  }

  protected void createHeaderCommon(ImportGenUtil importMgr, DbOrClassTableInfo tableInfo) {

    boolean isAnyColumnEnumDataType = false;
    boolean isAnyColumnBooleanDataType = false;

    // column dependent import
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      DataTypeInfo dtInfo = ci.getDtInfo();

      // 項目の型別にimport必須のものを取り込む
      importMgr.add(code.getHelper(dtInfo.getKata()).getNeededImports(ci));

      // timestampの場合はセットでDateTimeFormatterも必要になるので追加しておく。
      // （metamodel作成時もgetNeededImports()が呼ばれるがその場合はDateTimeFormatterは不要なので分けて記載）
      if (dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME || dtInfo.getKata() == DATE
          || dtInfo.getKata() == TIME) {
        importMgr.add("java.time.format.DateTimeFormatter");
      }

      if (!ci.getIsJavaOnly()) {
        importMgr.add(AnnotationGenUtil.getNeededImports(ci.getValidatorList(false)));
      }

      if (dtInfo.getKata() == ENUM) {
        // 当該enumをimport
        importMgr.add(rootBasePackage + ".base.enums." + code.getEnumConsideredKata(dtInfo));
        isAnyColumnEnumDataType = true;
      }

      if (dtInfo.getKata() == BOOLEAN) {
        isAnyColumnBooleanDataType = true;
      }

      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          if (info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
            importMgr.add(rootBasePackage + ".base.entity."
                + StringUtil.getUpperCamelFromSnake(info.getOrgTableName()));
          }
        }
      }
    }

    // @Validが存在する場合は使用。
    if (tableInfo.hasRelation() || tableInfo.hasBidirectionalRelationRef()) {
      if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
        importMgr.add("jakarta.validation.Valid");
      }

      tableInfo.columnList.stream().filter(e -> e.getBidirectionalInfo() != null).forEach(ci -> {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          if (info.getRelationKind() != RelationKindEnum.ONE_TO_ONE) {
            importMgr.add("java.util.ArrayList", "java.util.List");
          }
        }
      });
    }

    if (isAnyColumnEnumDataType) {
      importMgr.add("java.util.List", "java.util.Locale",
          EclibCoreConstants.PKG + ".util.EnumUtil");
    }

    if (isAnyColumnBooleanDataType) {
      importMgr.add("java.util.List", "java.util.Locale", "java.util.ArrayList",
          EclibCoreConstants.PKG + ".util.PropertyFileUtil");
    }
  }

  protected void fieldDefinition(String tableName, DbOrClassColumnInfo ci) {
    final String columnNameSm = StringUtil.getLowerCamelFromSnake(ci.getName());
    final String refEntityNameLw = ci.getRelationRefTable() == null ? null
        : StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
    DataTypeInfo dtInfo = ci.getDtInfo();

    if (dtInfo.getKata() == DATE_TIME) {
      sb.append(T1 + "/** The argument dataType of setters of datetime fields are not string "
          + "because it's so rare that the user input datetime format string directly on screen "
          + "and you don't have to care about receiving string of date-time format.  */" + RT);
    }
    String kata = dtInfo.getKata() == BOOLEAN || dtInfo.getKata() == DATE_TIME
        ? code.capitalCamel(code.getEnumConsideredKata(dtInfo))
        : "String";

    sb.append(AnnotationGenUtil.getCode(ci.getValidatorList(false), ElementType.FIELD));

    if (ci.isRelationColumn()) {
      sb.append(T1 + "@Valid" + RT);
    }

    sb.append(T1 + "protected "
        + (ci.isRelationColumn()
            ? StringUtils.capitalize(refEntityNameLw) + "BaseRecord " + ci.getRelationFieldName()
            : kata + " " + columnNameSm)
        + ";" + RT);

    // bidirectional relationで参照される側の場合は追加でfieldが必要
    if (ci.isReferedByBidirectionalRelation()) {
      for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
        String entityNameLw = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());
        sb.append(T1 + "@Valid" + RT);
        if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
          sb.append(T1 + "protected " + StringUtils.capitalize(entityNameLw) + "BaseRecord "
              + info.getEmptyConsideredFieldNameToReferFromTable() + ";" + RT);

        } else {
          sb.append(T1 + "protected List<" + StringUtils.capitalize(entityNameLw) + "BaseRecord> "
              + info.getEmptyConsideredFieldNameToReferFromTable()
              + (info.getRelationKind() == RelationKindEnum.ONE_TO_MANY ? " = new ArrayList<>()"
                  : "")
              + ";" + RT);

        }
      }
    }
  }

  private boolean hasTableAnyRelationsOrRefs(String tableName) {
    return info.dbRootInfo.tableList.stream().collect(Collectors.toMap(e -> e.getName(), e -> e))
        .get(tableName).hasAnyRelationsOrRefs();
  }

  public void createStaticInitializer(DbOrClassTableInfo tableInfo, String tableNameCp) {
    sb.append(T1 + "static {" + RT);

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {

      DataTypeInfo dtInfo = ci.getDtInfo();
      if (dtInfo.getKata() == STRING || dtInfo.getKata() == SHORT || dtInfo.getKata() == INTEGER
          || dtInfo.getKata() == LONG || dtInfo.getKata() == FLOAT || dtInfo.getKata() == DOUBLE
          || dtInfo.getKata() == BIG_INTEGER || dtInfo.getKata() == BIG_DECIMAL
          || dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME) {

        sb.append(
            T2 + "getStringLengthMap().put(\"" + StringUtil.getLowerCamelFromSnake(ci.getName())
                + "\", " + dtInfo.getMaxLength() + ");" + RT);
      }
    }

    sb.append(T1 + "}" + RT2);
  }

  public void createConstA(DbOrClassTableInfo tableInfo, String tableNameCp) {
    sb.append(T1 + "public " + tableNameCp + "BaseRecord() {" + RT);

    boolean bl = tableInfo.hasAnyRelationsOrRefs();
    sb.append(
        T2 + (bl ? "this(" + Constants.OBJECT_CONSTRUCTION_COUNT + ")" : "super()") + ";" + RT);

    if (!bl) {
      insideConstA(tableInfo);
    }

    sb.append(T1 + "}" + RT2);
  }

  private void createConstA2(DbOrClassTableInfo tableInfo, String tableNameCp) {
    if (!tableInfo.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T1 + "public " + tableNameCp + "BaseRecord(int count) {" + RT);
    sb.append(T2 + "super();" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideConstA(tableInfo);
    sb.append(T1 + "}" + RT2);
  }

  protected void insideConstA(DbOrClassTableInfo tableInfo) {
    if (!tableInfo.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T2 + "if (count > 0) {" + RT);

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      if (ci.isRelationColumn()) {
        String relEntityNameSm = StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
        sb.append(T3 + ci.getRelationFieldName() + " = new "
            + StringUtils.capitalize(relEntityNameSm) + "BaseRecord("
            + (hasTableAnyRelationsOrRefs(ci.getRelationRefTable()) ? "count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);
      }

      // bidirectional relationで参照される側になっている場合は追加で定義
      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          String relEntityNameSm = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());
          // sb.append(T2 + "if (constructsRelation) {" + RT);
          if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
            sb.append(T3 + info.getEmptyConsideredFieldNameToReferFromTable() + " = new "
                + StringUtils.capitalize(relEntityNameSm)
                + "BaseRecord(count) {public EclibItem[] getItems() {return null;}};" + RT);
          } else {
            // sb.append(T3 + info.getEmptyConsideredFieldNameToReferFromTable()
            // + " = new ArrayList<>();" + RT);
          }
        }
      }
    }
    sb.append(T2 + "}" + RT);
  }

  public void createConstB(DbOrClassTableInfo tableInfo, String tableNameCp, boolean isNotEntity)
      throws AppException {
    boolean bl = tableInfo.hasAnyRelationsOrRefs();

    sb.append(T1 + "public " + tableNameCp + "BaseRecord(" + tableNameCp
        + " e, DatetimeFormatParameters params) {" + RT);

    // containerを受ける場合は親がいないのでこの行は出力しない
    if (!isNotEntity) {
      sb.append(T2 + (bl ? "this(e, params, " + Constants.OBJECT_CONSTRUCTION_COUNT + ")"
          : "super(e, params)") + ";" + RT);

      if (!bl) {
        insideConstB(tableInfo, false);
      }
    }

    sb.append(T1 + "}" + RT2);
  }

  /**
   * このコンストラクタは、relationを持つrecord（以下A）がentityを引数にしたコンストラクタを呼ばれた際に、
   * Aにはentityの内容を格納するし、Aのrelation先のrecord（以下B）にもBに対応するentityの内容を格納するが、
   * Bに紐づくAには値を登録しない、という機能を提供するために使用。 その実現方法として、Aのコンストラクタは普通に呼ばれるのだが、Aのコンストラクタの中からBのコンストラクタを呼ぶ際に、
   * BのrelationであるAには値を代入しないコンストラクタを呼ぶ、というもの。 よって、このメソッドが必要なのは、bidirectionalなrelationを持つか、
   * bidirectionalなrelationの参照先に指定されている場合のみ出力
   */
  private void createConstB2(DbOrClassTableInfo tableInfo, String tableNameCp,
      boolean isNotEntity) {

    if (!tableInfo.hasAnyRelationsOrRefs()) {
      return;
    }

    sb.append(T1 + "public " + tableNameCp + "BaseRecord(" + tableNameCp
        + " e, DatetimeFormatParameters params, int count) {" + RT);
    sb.append(T2 + "super(e, params);" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideConstB(tableInfo, true);

    // 以下はbidirectionalで参照される側の場合のみ出力
    if (tableInfo.hasBidirectionalRelationRef()) {
      sb.append(RT);
      sb.append(T2 + "if (count > 0) {" + RT);

      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        if (ci.isReferedByBidirectionalRelation()) {
          for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
            String refEntityNameLc = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());
            String refEntityNameUc = StringUtils.capitalize(refEntityNameLc);
            String refFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
            String refFieldNameUc = StringUtils.capitalize(refFieldName);

            if (info.getRelationKind() == RelationKindEnum.ONE_TO_ONE) {
              sb.append(
                  T3 + refFieldName + " = (e.get" + refFieldNameUc + "() == null) ? null : new "
                      + refEntityNameUc + "BaseRecord(e.get" + refFieldNameUc + "(), params"
                      + (hasTableAnyRelationsOrRefs(info.getOrgTableName()) ? ", count" : "")
                      + ") {public EclibItem[] getItems() {return null;}};" + RT);

            } else {
              sb.append(
                  T3 + "if (e.get" + StringUtils.capitalize(refFieldName) + "() != null) {" + RT);
              sb.append(T4 + "for (" + refEntityNameUc + " en : e.get"
                  + StringUtils.capitalize(refFieldName) + "()) {" + RT);
              sb.append(T5 + refFieldName + ".add(new " + refEntityNameUc + "BaseRecord(en, params"
                  + (hasTableAnyRelationsOrRefs(info.getOrgTableName()) ? ", count" : "")
                  + ") {public EclibItem[] getItems() {return null;}});" + RT);
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

  protected void insideConstB(DbOrClassTableInfo tableInfo, boolean isCalledFromB2) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      String fieldNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
      String fieldNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
      DataTypeInfo dtInfo = ci.getDtInfo();
      String getPk = "";

      if (ci.isRelationColumn()) {
        String refEntityNameUp = StringUtil.getUpperCamelFromSnake(ci.getRelationRefTable());
        String relFieldNameUp = StringUtils.capitalize(ci.getRelationFieldName());
        if (isCalledFromB2) {
          sb.append(T2 + "if (count > 0) {" + RT);
        }
        sb.append((isCalledFromB2 ? T3 : T2) + "this." + ci.getRelationFieldName() + " = new "
            + refEntityNameUp + "BaseRecord(e.get" + relFieldNameUp + "(), params"
            + (hasTableAnyRelationsOrRefs(ci.getRelationRefTable()) ? ", count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);
        if (isCalledFromB2) {
          sb.append(T2 + "}" + RT);
        }

      } else {
        if (dtInfo.getKata() == STRING || dtInfo.getKata() == BOOLEAN
            || dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME) {
          sb.append(
              T2 + "this." + fieldNameLc + " = e" + getPk + ".get" + fieldNameUc + "();" + RT);
        } else if (dtInfo.getKata() == ENUM) {
          sb.append(T2 + "this." + fieldNameLc + " = (e" + getPk + ".get" + fieldNameUc
              + "() == null) ? \"\" : e" + getPk + ".get" + fieldNameUc + "().getCode();" + RT);
        } else if (dtInfo.getKata() == INTEGER || dtInfo.getKata() == SHORT
            || dtInfo.getKata() == LONG || dtInfo.getKata() == FLOAT
            || dtInfo.getKata() == DOUBLE) {
          String kata = StringUtils.capitalize((dtInfo.getKata().getName().toLowerCase()));
          sb.append(T2 + "this." + fieldNameLc + " = (e" + getPk + ".get" + fieldNameUc
              + "() == null) ? \"\" : " + kata + ".toString(e" + getPk + ".get" + fieldNameUc
              + "());" + RT);

        } else if (dtInfo.getKata() == DATE || dtInfo.getKata() == TIME) {
          String forTimeZone = dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME
              ? ".withOffsetSameInstant(params.getZoneOffset())"
              : "";
          sb.append(T2 + "this." + fieldNameLc + " = e.get" + fieldNameUc
              + "() == null ? \"\" : e.get" + fieldNameUc + "()" + forTimeZone
              + ".format(DateTimeFormatter.ofPattern(dateTimeFormatParams.get"
              + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString()) + "Format()));"
              + RT);

        } else {
          sb.append(T2 + "this." + fieldNameLc + " = e" + getPk + ".get" + fieldNameUc
              + "().toString();" + RT);
        }
      }
    }
  }

  public void createConstC(DbOrClassTableInfo tableInfo, String tableNameCp, boolean isNotEntity)
      throws AppException {

    sb.append(T1 + JD_ST + RT);
    sb.append(T1 + " * clone目的で使用するconstructor。" + RT);
    sb.append(T1
        + " * JSFで一覧からデータ選択した際、選択した行のrecが、recListの中の要素をそのまま渡されるので、cloneしないとrecList側が書き換わってしまうので使用。"
        + RT);
    sb.append(T1 + " * abstractクラスにはclone()を実装できないので、代わりにコンストラクタでの実装とした。" + RT);
    sb.append(T1 + JD_END + RT);
    sb.append(
        T1 + "public " + tableNameCp + "BaseRecord(" + tableNameCp + "BaseRecord rec) {" + RT);

    boolean bl = tableInfo.hasAnyRelationsOrRefs();
    sb.append(T2 + (bl ? "this(rec, " + Constants.OBJECT_CONSTRUCTION_COUNT + ")" : "super(rec)")
        + ";" + RT);

    if (!bl) {
      insideConstC(tableInfo);
    }

    sb.append(T1 + "}" + RT2);
  }

  public void createConstC2(DbOrClassTableInfo tableInfo, String tableNameCp, boolean isNotEntity)
      throws AppException {

    sb.append(T1 + JD_ST + RT);
    sb.append(T1 + " * clone目的で使用するconstructor。" + RT);
    sb.append(T1
        + " * JSFで一覧からデータ選択した際、選択した行のrecが、recListの中の要素をそのまま渡されるので、cloneしないとrecList側が書き換わってしまうので使用。"
        + RT);
    sb.append(T1 + " * abstractクラスにはclone()を実装できないので、代わりにコンストラクタでの実装とした。" + RT);
    sb.append(T1 + JD_END + RT);
    sb.append(T1 + "public " + tableNameCp + "BaseRecord(" + tableNameCp
        + "BaseRecord rec, int count) {" + RT);

    sb.append(T2 + "super(rec);" + RT2);
    sb.append(T2 + "count--;" + RT2);

    insideConstC(tableInfo);

    sb.append(T1 + "}" + RT2);
  }

  protected void insideConstC(DbOrClassTableInfo tableInfo) {
    if (!tableInfo.hasAnyRelationsOrRefs()) {
      return;
    }

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      String columnNameCp = StringUtil.getUpperCamelFromSnake(ci.getName());
      String columnNameSm = StringUtil.getLowerCamelFromSnake(ci.getName());
      String lefthand = "rec.get" + columnNameCp;

      if (ci.isRelationColumn()) {
        sb.append(T2 + "this." + ci.getRelationFieldName() + " = new "
            + StringUtil.getUpperCamelFromSnake(ci.getRelationRefTable()) + "BaseRecord("
            + (hasTableAnyRelationsOrRefs(ci.getRelationRefTable()) ? "count" : "")
            + ") {public EclibItem[] getItems() {return null;}};" + RT);
      }
      sb.append(
          T2 + "this."
              + (ci.isRelationColumn() ? "set" + columnNameCp + "(" + lefthand + "())"
                  : columnNameSm + " = " + lefthand
                      + (ci.getDtInfo().getKata() == DATE_TIME
                          || ci.getDtInfo().getKata() == TIMESTAMP ? "OfEntityDataType" : "")
                      + "()")
              + ";" + RT);
    }
  }

  protected void createAccessor(DbOrClassTableInfo tableInfo, String tableNameCp) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      String fieldNameLc = StringUtil.getLowerCamelFromSnake(ci.getName());
      String fieldNameUc = StringUtil.getUpperCamelFromSnake(ci.getName());
      final String relEntityNameLc = ci.getRelationRefTable() == null ? null
          : StringUtil.getLowerCamelFromSnake(ci.getRelationRefTable());
      String relFieldNameUc = ci.getRelationRefCol() == null ? null
          : StringUtil.getUpperCamelFromSnake(ci.getRelationRefCol());
      sb.append(T1 + "// accessor:" + fieldNameLc + RT);

      DataTypeInfo dtInfo = ci.getDtInfo();
      String recGetKata =
          dtInfo.getKata() == BOOLEAN ? code.capitalCamel(dtInfo.getKata().toString()) : "String";
      final String recSetKata = dtInfo.getKata() == BOOLEAN || dtInfo.getKata() == DATE_TIME
          || dtInfo.getKata() == TIMESTAMP ? code.capitalCamel(code.getEnumConsideredKata(dtInfo))
              : "String";
      final String javaKata = code.getEnumConsideredKata(dtInfo);

      sb.append(T1 + "public " + recGetKata + " get" + fieldNameUc + "() {" + RT);

      if (ci.getDtInfo().getKata() == DATE_TIME || ci.getDtInfo().getKata() == TIMESTAMP) {
        String forTimeZone = dtInfo.getKata() == TIMESTAMP || dtInfo.getKata() == DATE_TIME
            ? ".withOffsetSameInstant(dateTimeFormatParams.getZoneOffset())"
            : "";
        sb.append(T2 + "return " + fieldNameLc + " == null ? \"\" : " + fieldNameLc + forTimeZone
            + ".format(DateTimeFormatter.ofPattern(dateTimeFormatParams.get"
            + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString()) + "Format()));" + RT);

      } else {
        sb.append(T2 + "return "
            + (ci.isRelationColumn()
                ? ci.getRelationFieldName() + " == null ? null : " + ci.getRelationFieldName()
                    + ".get" + relFieldNameUc + "()"
                : fieldNameLc)
            + ";" + RT);
      }
      sb.append(T1 + "}" + RT2);

      sb.append(
          T1 + "public void set" + fieldNameUc + "(" + recSetKata + " " + fieldNameLc + ") {" + RT);
      sb.append(T2 + "this."
          + (ci.isRelationColumn()
              ? ci.getRelationFieldName() + ".set" + relFieldNameUc + "(" + fieldNameLc + ")"
              : fieldNameLc + " = " + fieldNameLc)
          + ";" + RT);
      sb.append(T1 + "}" + RT2);

      // entityとの連携のために、entityのデータ型でデータ取得するgetterを追加
      if (CodeGenUtil.ofEntityTypeMethodAvailableDataTypeList.contains(dtInfo.getKata())) {

        sb.append(T1 + "public " + javaKata + " get" + fieldNameUc + "OfEntityDataType() {" + RT);

        if (dtInfo.getKata() == DATE_TIME || dtInfo.getKata() == TIMESTAMP) {
          sb.append(T2 + "return " + code.uncapitalCamel(ci.getName()) + ";" + RT);

        } else {
          sb.append(T2 + "return (get" + fieldNameUc + "() == null || get" + fieldNameUc
              + "().equals(\"\")) ? null :" + RT);

          if (ci.isRelationColumn()) {
            sb.append(T4 + "get" + fieldNameUc + "OfEntityDataType();" + RT);

          } else if (dtInfo.getKata() == DATE || dtInfo.getKata() == TIME) {
            sb.append(T4 + javaKata + ".parse(" + fieldNameLc
                + ", DateTimeFormatter.ofPattern(dateTimeFormatParams.get"
                + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString()) + "Format()));"
                + RT);

          } else if (dtInfo.getKata() == ENUM) {
            sb.append(
                T4 + "EnumUtil.getEnumFromCode(" + javaKata + ".class, " + fieldNameLc + ");" + RT);

          } else {
            sb.append(
                T4 + javaKata + ".valueOf(" + fieldNameLc + ".replaceAll(\",\", \"\"));" + RT);
          }
        }
        sb.append(T1 + "}" + RT2);
      }

      if (ci.isRelationColumn()) {
        // relationのcolumnの場合は、別途entityを示すfield自体のaccessorも用意しておく
        createAccessorForRelation(relEntityNameLc, ci.getRelationFieldName(), null);
      }

      if (ci.isReferedByBidirectionalRelation()) {
        for (BidirectionalRelationInfo info : ci.getBidirectionalInfo()) {
          // 現時点では、bidirectionalで参照される側はentity名をそのままfield名としているので、field名にもentity名を渡す
          String entityName = StringUtil.getLowerCamelFromSnake(info.getOrgTableName());
          createAccessorForRelation(entityName, info.getEmptyConsideredFieldNameToReferFromTable(),
              info);
        }
      }
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
   * html用にlistを作成。 項目名とEnum名が異なる場合（例えば、Enum名：AccRoleEnum、項目名：role）、メソッド名はgetRoleListになるので注意。
   * 項目名をメソッド名にしないと、thymeleaf側からlist取得する際に問題が出るため。
   * その関係で、個々のentityで持つenum型の項目のlistをまとめてSystemCommonBaseRecordで保持はせず、個々のrecordに持たせている。
   */
  protected void createListsForHtmlSelect(DbOrClassTableInfo tableInfo) {
    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      DataTypeInfo dtInfo = ci.getDtInfo();
      String capitalizedName = StringUtil.getUpperCamelFromSnake(ci.getName());
      String name = StringUtils.uncapitalize(capitalizedName);

      // enumに対しlistを取得
      if (dtInfo.getKata() == ENUM) {
        sb.append(T1 + "public List<String[]> get" + capitalizedName
            + "List(Locale locale, String options) {" + RT);
        sb.append(T2 + "return EnumUtil.getListForHtmlSelect(" + code.getEnumConsideredKata(dtInfo)
            + ".class, locale, options);" + RT);
        sb.append(T1 + "}" + RT2);

        // name取得
        sb.append(T1 + "public String get" + capitalizedName + "Name(Locale locale) {" + RT);
        // sb.append(T2 + "return " + getEnumConsideredKata(dtInfo) + ".getEnumFromCode(get"
        // + capitalizedName + "()).getDisplayName(locale);" + RT);
        sb.append(T2 + "return EnumUtil.getEnumFromCode(" + code.getEnumConsideredKata(dtInfo)
            + ".class, get" + capitalizedName + "()).getDisplayName(locale);" + RT);
        sb.append(T1 + "}" + RT2);
      }

      // booleanに対しlistを取得
      if (dtInfo.getKata() == BOOLEAN) {
        sb.append(T1 + "public List<String[]> get" + capitalizedName
            + "List(Locale locale, String options) {" + RT);
        sb.append(T2 + "List<String[]> rtnList = new ArrayList<>();" + RT2);
        for (String bool : new String[] {"true", "false"}) {
          sb.append(T2 + "rtnList.add(new String[] {\"" + bool
              + "\", PropertyFileUtil.getMessage(locale, \"boolean." + name + "." + bool + "\")});"
              + RT);
        }
        sb.append(RT);
        sb.append(T2 + "return rtnList;" + RT);
        sb.append(T1 + "}" + RT2);

        // name取得
        sb.append(T1 + "public String get" + capitalizedName + "Name(Locale locale) {" + RT);
        sb.append(T2 + "return PropertyFileUtil.getMessage(locale, \"boolean." + name + ".\" + "
            + name + ");" + RT);
        sb.append(T1 + "}" + RT2);
      }
    }
  }
}
