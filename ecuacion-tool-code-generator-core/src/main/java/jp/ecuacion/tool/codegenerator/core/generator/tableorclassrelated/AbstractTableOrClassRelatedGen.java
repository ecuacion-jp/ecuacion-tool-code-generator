package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper.GenHelperKata;

/**
 * @author 庸介
 *
 */
public abstract class AbstractTableOrClassRelatedGen extends AbstractGen {

  protected final String postfixSm;
  protected final String postfixCp;

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
}
