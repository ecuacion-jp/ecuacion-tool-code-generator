package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.dao;

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

public class SqlPropertiesGen extends AbstractDaoRelatedGen {

  StringBuilder sbWhere;
  StringBuilder sbWhereAndRem;
  StringBuilder sbOrder;

  String sqlRt = " \\n \\";

  public SqlPropertiesGen() {
    super(DataKindEnum.DB);
  }

  @Override
  public void generate() throws AppException {
    // 作成不要な場合は終了
    if (!info.sysCmnRootInfo.getUsesUtilJpa()) {
      return;
    }

    // List<SqlInfo> sqlInfoArrForOrmXml = new ArrayList<>();
    List<SqlInfo> sqlInfoArrForNativeSqlProp = new ArrayList<>();

    for (DbOrClassTableInfo tableInfo : info.dbRootInfo.tableList) {
      final String tableNameCp = StringUtil.getUpperCamelFromSnake(tableInfo.getName());

      makeParts(tableInfo);
      // insert
      sqlInfoArrForNativeSqlProp.add(getInsertSqlInfo(tableInfo));
      // insertAll
      sqlInfoArrForNativeSqlProp.add(getInsertAllSqlInfo(tableInfo));
      // truncate
      sqlInfoArrForNativeSqlProp.add(getTruncateSqlInfo(tableInfo.getName()));

      // propertiesファイルはクラスごとに書き出し
      outputFileForSqlProperties(sqlInfoArrForNativeSqlProp, getFilePath(postfixSm),
          tableNameCp + "Base" + postfixCp + ".nativesql.properties");
      // リストを空にする
      sqlInfoArrForNativeSqlProp.clear();
    }
  }

  protected void outputFileForSqlProperties(List<SqlInfo> sqlInfoArr, String path,
      String fileName) {
    StringBuilder sqls = new StringBuilder();
    // jpql/sqlを出力
    sqlInfoArr.forEach(sqlInfo -> sqls.append(sqlInfo.getSqlId() + "=" + sqlInfo.getSql() + RT2));

    super.outputFile(sqls, path, fileName);
  }

  private void makeParts(DbOrClassTableInfo tableInfo) {
    sbWhere = new StringBuilder();
    sbWhereAndRem = new StringBuilder();
    sbOrder = new StringBuilder();

    sbWhere.append(T1 + "where ");
    sbOrder.append(T1 + "order by ");
    boolean isFirst = true;
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(ci -> ci.isPk()).toList()) {
      if (isFirst) {
        sbWhere.append(ci.getName() + " = ?");
        sbOrder.append(ci.getName());
        isFirst = false;
      } else {
        sbWhere.append(sqlRt + RT + T2 + "and " + ci.getName() + " = ?");
        sbOrder.append(", " + ci.getName());
      }
    }
    sbWhereAndRem.append(sbWhere + sqlRt + RT + T2 + "and REM_FLG = '0'");
  }

  private SqlInfo getInsertSqlInfo(DbOrClassTableInfo tableInfo) {
    StringBuilder sql = new StringBuilder();
    String insertCommonSqlPart = insertCommonSqlPart(tableInfo);

    // insertAll 1行目と後半
    sql.append("insert into " + tableInfo.getName() + " (" + sqlRt + RT);
    sql.append(insertCommonSqlPart);
    sql.append("{+insertAll ? } returning " + tableInfo.getPkColumn().getName());

    return new SqlInfo(tableInfo.getName(), "insert", SqlKind.NATIVE_SQL,
        SqlFileKind.NATIVE_SQL_PROPERTIES, sql.toString());
  }

  private SqlInfo getInsertAllSqlInfo(DbOrClassTableInfo tableInfo) {
    StringBuilder sql = new StringBuilder();
    String insertCommonSqlPart = insertCommonSqlPart(tableInfo);

    // insertAll 1行目と後半
    sql.append("insert into " + tableInfo.getName() + " (" + sqlRt + RT);
    sql.append(insertCommonSqlPart);
    sql.append("{+insertAll ? } ");

    return new SqlInfo(tableInfo.getName(), "insertAll", SqlKind.NATIVE_SQL,
        SqlFileKind.NATIVE_SQL_PROPERTIES, sql.toString());
  }

  private String insertCommonSqlPart(DbOrClassTableInfo tableInfo) {
    StringBuilder sbInsert = new StringBuilder();
    boolean is1stTime = true;
    for (DbOrClassColumnInfo ci : tableInfo.columnList.stream().filter(e -> !e.getIsJavaOnly())
        .toList()) {
      DataTypeKataEnum kata = ci.getDtInfo().getKata();
      if (!(ci.isAutoIncrement()
          && (kata == DataTypeKataEnum.INTEGER || kata == DataTypeKataEnum.LONG))) {
        String comma = "";
        if (is1stTime) {
          is1stTime = false;

        } else {
          comma = ", ";
        }

        sbInsert.append(T2 + comma + ci.getName() + sqlRt + RT);
      }
    }
    if (info.getCommonTableInfo() != null) {
      for (DbOrClassColumnInfo ci : info.getCommonTableInfo().columnList.stream()
          .filter(e -> !e.getIsJavaOnly()).toList()) {
        DataTypeKataEnum kata = ci.getDtInfo().getKata();
        if (!(ci.isAutoIncrement()
            && (kata == DataTypeKataEnum.INTEGER || kata == DataTypeKataEnum.LONG))) {
          sbInsert.append(T2 + ", " + ci.getName() + sqlRt + RT);
        }
      }
    }

    // sbInsert.append(T2 + "LST_UPD_ACC_ID," + sqlRt + RT);
    // sbInsert.append(T2 + "LST_UPD_TIME," + sqlRt + RT);
    // sbInsert.append(T2 + "REM_FLG," + sqlRt + RT);
    // sbInsert.append(T2 + "DB_UPD_VER" + sqlRt + RT);
    sbInsert.append(T1 + ") values ");

    return sbInsert.toString();
  }

  private SqlInfo getTruncateSqlInfo(String tableName) {
    return new SqlInfo(tableName, "truncate", SqlKind.NATIVE_SQL, SqlFileKind.NATIVE_SQL_PROPERTIES,
        "truncate table " + tableName);
  }

  class SqlInfo {
    private String className;
    private String sqlId;
    private SqlKind sqlKind;
    private SqlFileKind sqlFileKind;
    private String sql;

    public SqlInfo(String className, String sqlId, SqlKind sqlKind, SqlFileKind sqlFileKind,
        String sql) {
      this.sqlId = sqlId;
      this.sqlKind = sqlKind;
      this.sqlFileKind = sqlFileKind;
      this.sql = sql;
    }

    public String getClassName() {
      return className;
    }

    public String getSqlId() {
      return sqlId;
    }

    public SqlKind getSqlKind() {
      return sqlKind;
    }

    public SqlFileKind getSqlFileKind() {
      return sqlFileKind;
    }

    public String getSql() {
      return sql;
    }
  }


  enum SqlKind {
    JPQL, NATIVE_SQL
  }


  enum SqlFileKind {
    ORM_XML, JPQL_PROPERTIES, NATIVE_SQL_PROPERTIES
  }
}
