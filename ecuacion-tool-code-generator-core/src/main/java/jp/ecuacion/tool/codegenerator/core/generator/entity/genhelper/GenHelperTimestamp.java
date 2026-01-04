package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;

public class GenHelperTimestamp extends GenHelperNoNumberObj {
  @Override
  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    StringBuilder sb = new StringBuilder();

    sb.append(T1 + "public void set" + columnNameCp + "(String str" + columnNameCp
        + ") throws MultipleAppException {" + RT);
    sb.append(T2 + "try {" + RT);
    sb.append(
        T4 + "SimpleDateFormat sdf = new SimpleDateFormat(\"yyyy'/'MM'/'dd' 'HH':'mm':'ss'.'SSS\");"
            + RT);
    sb.append(T4 + "set" + columnNameCp + "(new Timestamp(sdf.parse(str" + columnNameCp
        + ").getTime()));" + RT);
    sb.append(T2 + "} catch (ParseException pe) {" + RT);
    sb.append(T3 + "ArrayList<AppException> exArr = new ArrayList<AppException>();" + RT);
    sb.append(
        T3 + "exArr.add(new BizLogicAppException(\"MSG_ERR_TIMESTAMP_FORMAT_WRONG\", "
            + columnNameSm + "Info.getDisplayName(), str" + columnNameCp + "));" + RT);
    sb.append(T3 + "throw new MultipleAppException(exArr);" + RT);
    sb.append(T2 + "}" + RT);
    sb.append(T1 + "}" + RT2);
    return sb.toString();
  }

  @Override
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    String[] rtnStrings =
        mergeStrings(super.getNeededImports(columnInfo), "java.time.*");
    // if (columnInfo.isAutoIncrement() || columnInfo.isAutoUpdate() ||
    // columnInfo.isForcedIncrement()
    // || columnInfo.isForcedUpdate()) {
    // rtnStrings = mergeStrings(rtnStrings, "jp.ecuacion.lib.core.util.*");
    // }

    return rtnStrings;
  }
}
