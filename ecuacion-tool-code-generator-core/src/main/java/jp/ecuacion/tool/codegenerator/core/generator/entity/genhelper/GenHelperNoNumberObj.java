package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

public abstract class GenHelperNoNumberObj extends GenHelperKata {

  // @Override
  // public String getEntityAccessor(String columnNameCp, String columnNameSm, String dataType) {
  // StringBuilder sb = new StringBuilder();
  //
  // sb.append(T1 + "public " + getJavaKataName() + " get" + columnNameCp + "() {" + RT);
  // sb.append(T2 + "return " + columnNameSm + ";" + RT);
  // sb.append(T1 + "}" + RT2);
  // sb.append(T1 + "public void set" + columnNameCp + "(" + getJavaKataName() + " " + columnNameSm
  // + ") throws MultipleAppException {" + RT);
  // sb.append(T2 + "this." + columnNameSm + " = " + columnNameSm + ";" + RT);
  // sb.append(T1 + "}" + RT2);
  //
  // sb.append(getStringParamSetter(columnNameCp, columnNameSm, dataType));
  // return sb.toString();
  // }

  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    return "";
  }
}
