package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper;


public class GenHelperBoolean extends GenHelperNoNumberObj {

  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    StringBuilder sb = new StringBuilder();

    sb.append(T1 + "public void set" + columnNameCp + "(String str" + columnNameCp
        + ") throws MultipleAppException {" + RT);
    sb.append(T3 + "String str = str" + columnNameCp + ".toUpperCase();" + RT);
    sb.append(T3 + "Boolean b = null;" + RT);
    sb.append(T3 + "if (str.equals(\"0\") || str.equals(\"FALSE\")) b = false;" + RT);
    sb.append(T3 + "else if (str.equals(\"1\") || str.equals(\"TRUE\")) b = true;" + RT);
    sb.append(T3
        + "else throw new AppRuntimeException(BizLogicAppException("
        + "\"MSG_ERR_INCORRECT_BOOLEAN_STRING\", str"
        + columnNameCp + "));" + RT);
    sb.append(T3 + "set" + columnNameCp + "(b);" + RT);
    sb.append(T1 + "}" + RT);

    return sb.toString();
  }
}
