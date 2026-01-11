package jp.ecuacion.tool.codegenerator.core.generator.record;

import java.util.List;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil.ColFormat;

public class PerTableBaseRecordGen extends AbstractBaseRecordGen {

  public PerTableBaseRecordGen(DataKindEnum dataKind) {
    super(dataKind);
  }

  @Override
  public void generate() throws AppException {
    internalGenerate(info.dbRootInfo.tableList, false);
  }

  public void generateHeader(DbOrClassTableInfo ti) throws AppException {

    generateHeaderCommon(ti, rootBasePackage + ".base.entity." + ti.getNameCpCamel(),
        "jp.ecuacion.splib.core.container.*", "jp.ecuacion.lib.core.item.*",
        "jp.ecuacion.lib.core.util.StringUtil");

    sb.append("public abstract class " + ti.getNameCpCamel()
        + "BaseRecord extends SystemCommonBaseRecord implements EclibItemContainer {" + RT2);
  }

  @Override
  protected void generateMethods(DbOrClassTableInfo ti) throws AppException {
    createIdsAndOptimisticLockVersions(ti);    
  }

  private void createIdsAndOptimisticLockVersions(DbOrClassTableInfo ti) {
    List<DbOrClassColumnInfo> relColList = ti.getRelationColumnWithoutGroupList();

    // getIds
    sb.append(T1 + "public String getIds() {" + RT);
    sb.append(T2 + "return StringUtil.getCsv(new String[] {"
        + code.generateString(ti.getPkColumn(), ColFormat.GET));
    for (DbOrClassColumnInfo ci : relColList) {
      String relField = ci.getRelationFieldNameCp();
      DbOrClassColumnInfo pk =
          info.getTableInfo(ci.getRelationRefTable()).getPkColumnIncludingSystemCommon();
      String refPkGet = code.generateString(pk, ColFormat.GET);
      sb.append(", get" + relField + "() == null ? null : get" + relField + "()." + refPkGet);
    }
    sb.append("});" + RT);
    sb.append(T1 + "}" + RT2);

    // setIds
    sb.append(T1 + "public void setIds(String idCsv) {" + RT);
    sb.append(T2 + "String[] ids = idCsv.split(\",\");" + RT);
    sb.append(T2 + "if (ids.length < " + (1 + relColList.size()) + ") return;" + RT2);

    sb.append(T2 + code.generateString(ti.getPkColumn(), ColFormat.SET, "ids[0]") + ";" + RT);
    int i = 0;
    while (relColList.size() > i) {
      sb.append(T2 + code.generateString(relColList.get(i), ColFormat.SET, "ids[" + (i + 1) + "]")
          + ";" + RT);
      i++;
    }
    sb.append(T1 + "}" + RT2);

    // getOptimisticLockVersions
    sb.append(T1 + "public String getOptimisticLockVersions() {" + RT);
    sb.append(T2 + "return StringUtil.getCsv(new String[] {getVersion()");
    for (DbOrClassColumnInfo ci : relColList) {
      String relField = ci.getRelationFieldNameCp();
      DbOrClassColumnInfo v =
          info.getTableInfo(ci.getRelationRefTable()).getVersionColumnIncludingSystemCommon();
      String refVerGet = code.generateString(v, ColFormat.GET);
      sb.append(", get" + relField + "() == null ? null : get" + relField + "()." + refVerGet);
    }
    sb.append("});" + RT);
    sb.append(T1 + "}" + RT2);

    // setOptimisticLockVersions
    sb.append(T1 + "public void setOptimisticLockVersions(String versionCsv) {" + RT);
    sb.append(T2 + "String[] versions = versionCsv.split(\",\");" + RT);
    sb.append(T2 + "if (versions.length < " + (1 + relColList.size()) + ") return;" + RT2);

    sb.append(T2 + "setVersion(versions[0]);" + RT);
    i = 0;
    while (relColList.size() > i) {
      sb.append(T2 + "get" + relColList.get(i).getRelationFieldNameCp() + "().setVersion(versions["
          + (i + 1) + "]);" + RT);
      i++;
    }
    sb.append(T1 + "}" + RT);
  }
}
