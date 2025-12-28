package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper.GenHelperKata;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 庸介
 *
 */
public abstract class AbstractTableOrClassRelatedGen extends AbstractGen {

  protected final String postfixSm;
  protected final String postfixCp;

  /** naturalKeyを引数にとる時の "String myArg1, Integer myArg2" という文字列を保持。table別にmap形式。 */
  protected final Map<String, String> partNaturalKeyArgs = new HashMap<>();

  /** naturalKeyをentityからgetする形の "e.getMyArg1(), e.getMyArg2()" という文字列を保持。table別にmap形式。 */
  protected final Map<String, String> partNaturalKeyEntityFields = new HashMap<>();

  /** naturalKeyをentityからgetする形の "myArg1AndMyArg2" という文字列を保持。table別にmap形式。 */
  protected final Map<String, String> partNaturalKeySmCamel = new HashMap<>();

  /**
   * naturalKeyをentityからgetする形の "myArg1AndMyArg2" という文字列を保持。table別にmap形式。
   * ただしnaturalKeyがrelationを持つ場合はAcc_IdAndApp_Idのようになる。repositoryでの使用を想定。
   */
  protected final Map<String, String> partNaturalKeySmCamelRelConsidered = new HashMap<>();

  /** naturalKeyをentityからgetする形の "myArg1MyArg2" という文字列を保持。table別にmap形式。 */
  protected final Map<String, String> partNaturalKeyjpql = new HashMap<>();

  /**
   * naturalKeyをsqlの引数にする形の"my_arg_1 = :#{#entity.myArg1} and my_arg_2 = :#{#entity.myArg2}"
   * という文字列を保持。table別にmap形式。
   */
  protected final Map<String, String> partNaturalKeyEntityParamSql = new HashMap<>();

  // こいつらみんなメンバ変数をやめたいのだが、時間のある時にやって。。。目指せステップ数2,500未満！
  private List<DbOrClassTableInfo> tableList;
  protected List<DbOrClassTableInfo> commonTableList;
  protected List<DbOrClassColumnInfo> commonColumnList;
  protected List<DbOrClassColumnInfo> pkList;
  protected List<DbOrClassColumnInfo> nonPkList;

  private HashMap<DataTypeKataEnum, GenHelperKata> helperMap =
      new HashMap<DataTypeKataEnum, GenHelperKata>();

  // tableList
  protected List<DbOrClassTableInfo> getTableList() {
    return tableList;
  }

  protected void setTableList(List<DbOrClassTableInfo> tableList) {
    this.tableList = tableList;
  }

  public AbstractTableOrClassRelatedGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);

    // tableListの生成
    DbOrClassRootInfo rootInfo =
        ((DbOrClassRootInfo) info.systemMap.get(info.systemName).get(xmlFilePostFix));
    if (rootInfo != null) {
      setTableList(rootInfo.tableList);
    }

    // postfixの設定
    boolean usesSpringName = info.sysCmnRootInfo.getUsesSpringNamingConvention();
    postfixSm = (usesSpringName) ? "repositoryimpl" : "dao";
    postfixCp = (usesSpringName) ? "RepositoryImpl" : "Dao";

    for (DbOrClassTableInfo tableInfo : tableList) {
      // partNaturalKeyArgs
      StringBuilder sbPartNaturalKeyArgs = new StringBuilder();
      StringBuilder sbPartNaturalKeyEntityFields = new StringBuilder();
      StringBuilder sbPartNaturalKeySmCamel = new StringBuilder();
      StringBuilder sbPartNaturalKeySmCamelRelationConsidered = new StringBuilder();
      StringBuilder sbPartNaturalKeyjpql = new StringBuilder();
      StringBuilder sbPartNaturalKeyEntityParamSql = new StringBuilder();

      boolean isFirst = true;
      for (DbOrClassColumnInfo ci : tableInfo.columnList) {
        DataTypeInfo dtInfo = ci.getDtInfo();
        String colNameUc = StringUtil.getUpperCamelFromSnake(ci.getColumnName());
        String colNameLc = StringUtil.getLowerCamelFromSnake(ci.getColumnName());
        String colNameUcRelUnderscore = !ci.isRelationColumn() ? colNameUc
            : StringUtil.getUpperCamelFromSnake(ci.getRelationFieldName()) + "_"
                + StringUtil.getUpperCamelFromSnake(ci.getRelationRefCol());

        if (ci.isUniqueConstraint()) {
          if (isFirst) {
            isFirst = false;

          } else {
            sbPartNaturalKeyArgs.append(", ");
            sbPartNaturalKeyEntityFields.append(", ");
            sbPartNaturalKeySmCamel.append("And");
            sbPartNaturalKeySmCamelRelationConsidered.append("And");
            sbPartNaturalKeyjpql.append(" and ");
            sbPartNaturalKeyEntityParamSql.append(" and ");
          }

          sbPartNaturalKeyArgs.append((getEnumConsideredKata(dtInfo)) + " "
              + StringUtil.getLowerCamelFromSnake(ci.getColumnName()));
          sbPartNaturalKeyEntityFields.append("e.get"
              + StringUtil.getUpperCamelFromSnake(ci.getColumnName()) + "()");
          sbPartNaturalKeySmCamel
              .append((isFirst ? StringUtils.uncapitalize(colNameUc) : colNameUc));
          sbPartNaturalKeySmCamelRelationConsidered
              .append((isFirst ? StringUtils.uncapitalize(colNameUcRelUnderscore)
                  : colNameUcRelUnderscore));
          sbPartNaturalKeyjpql.append(ci.getColumnName().toLowerCase() + " = :" + colNameLc);
          sbPartNaturalKeyEntityParamSql.append(ci.getColumnName().toLowerCase() + " = :#{#entity."
              + StringUtil.getLowerCamelFromSnake(ci.getColumnName())
              + (ci.getDtInfo().getKata() == DataTypeKataEnum.ENUM ? ".code" : "") + "}");
        }
      }

      partNaturalKeyArgs.put(tableInfo.getTableName(), sbPartNaturalKeyArgs.toString());
      partNaturalKeyEntityFields.put(tableInfo.getTableName(),
          sbPartNaturalKeyEntityFields.toString());
      partNaturalKeySmCamel.put(tableInfo.getTableName(), sbPartNaturalKeySmCamel.toString());
      partNaturalKeySmCamelRelConsidered.put(tableInfo.getTableName(),
          sbPartNaturalKeySmCamelRelationConsidered.toString());
      partNaturalKeyjpql.put(tableInfo.getTableName(), sbPartNaturalKeyjpql.toString());
      partNaturalKeyEntityParamSql.put(tableInfo.getTableName(),
          sbPartNaturalKeyEntityParamSql.toString());
    }

    boolean doesHaveDbCommon =
        info.systemMap.get(info.systemName).containsKey(DataKindEnum.DB_COMMON);

    if (doesHaveDbCommon) {
      DbOrClassRootInfo dbOrClassRootInfo =
          (DbOrClassRootInfo) info.systemMap.get(info.systemName).get(DataKindEnum.DB_COMMON);

      if (dbOrClassRootInfo.tableList.size() > 0) {
        commonTableList = dbOrClassRootInfo.tableList;
        commonColumnList = commonTableList.get(0).columnList;
      } else {
        commonTableList = new ArrayList<>();
        commonColumnList = new ArrayList<>();
      }
    }
  }

  public void makePkList(DbOrClassTableInfo tableInfo) {
    pkList = new ArrayList<DbOrClassColumnInfo>();
    nonPkList = new ArrayList<DbOrClassColumnInfo>();

    for (DbOrClassColumnInfo ci : tableInfo.columnList) {
      nonPkList.add(ci);
    }
  }

  protected String getRootBasePackageOfDataTypeFromAllSystem(String dataTypeName)
      throws AppException {

    return info.sysCmnRootInfo.getBasePackage();

    // // まず、自分のシステムのDataTypeを探す
    // HashMap<String, DataTypeInfo> map = info.allDataTypeMap.get(info.systemName);
    // if (map.containsKey(dataTypeName)) {
    // return info.sysCmnRootInfo.getBasePackage();
    // }
    //
    // // なければDataTypeRefを参照
    // DataTypeRefRootInfo ref =
    // (DataTypeRefRootInfo) info.systemMap.get(info.systemName).get(Constants.XML_POST_FIX_DT_R);
    // if (ref != null) {
    // for (DataTypeRefInfo dtrInfo : ref.dataTypeRefList) {
    // if (dtrInfo.getDataType().equals(dataTypeName)) {
    // return info.sysCmnRootInfo.getBasePackage();
    // }
    // }
    // }
    //
    // // ここまで来ると、該当のDataTypeが存在しなかったということ
    // throw new BizLogicAppException("MSG_ERR_DESIGNATED_DATATYPE_NOT_FOUND",
    // info.systemName, dataTypeName);
  }

  protected GenHelperKata getHelper(DataTypeKataEnum kata) {
    if (!helperMap.containsKey(kata)) {
      try {
        @SuppressWarnings("unchecked")
        Class<GenHelperKata> cls = (Class<GenHelperKata>) Class.forName(Constants.STR_PACKAGE_HOME
            + ".core.generator.tableorclassrelated.entity.genhelper.GenHelper"
            + StringUtil.getUpperCamelFromSnake(kata.getName()));
        Constructor<GenHelperKata> con = cls.getConstructor();
        GenHelperKata helper = con.newInstance();
        helperMap.put(kata, helper);

      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }

    return helperMap.get(kata);
  }

  protected String getEnumConsideredKata(DataTypeInfo dtInfo) {
    String rtn = null;

    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      rtn = CodeGenUtil.dataTypeNameToUppperCamel(dtInfo.getDataTypeName())
          + StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());

    } else if (dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
        || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME) {
      rtn = (dtInfo.getNotNeedsTimezone()) ? "LocalDateTime" : "OffsetDateTime";

    } else if (dtInfo.getKata() == DataTypeKataEnum.DATE) {
      rtn = "LocalDate";

    } else if (dtInfo.getKata() == DataTypeKataEnum.TIME) {
      rtn = "LocalTime";

    } else {
      rtn = StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
    }

    return rtn;
  }
}
