package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper;

public abstract class GenHelperWrappedNumber extends GenHelperKata {

  // @Override
  // public String getEntityAccessor(String columnNameCp, String columnNameSm, String dataType) {
  // StringBuilder sb = new StringBuilder();
  //
  // sb.append(T1 + "public " + getJavaKataName() + " get" + columnNameCp + "() {" + RT);
  // sb.append(T2 + "return " + columnNameSm + ";" + RT);
  // sb.append(T1 + "}" + RT2);
  //
  // sb.append(T1 + "public void set" + columnNameCp + "(" + getJavaKataName() + " " + columnNameSm
  // + ") throws MultipleAppException {" + RT);
  // sb.append(T2 + "check(" + columnNameSm + ", " + columnNameSm + "Info);" + RT);
  // sb.append(T2 + "this." + columnNameSm + " = " + columnNameSm + ";" + RT);
  // sb.append(T1 + "}" + RT2);
  //
  // sb.append(T1 + "public void set" + columnNameCp + "(String str" + columnNameCp
  // + ") throws MultipleAppException {" + RT);
  // sb.append(T2 + "try {" + RT);
  // sb.append(T2 + "set" + columnNameCp + "(" + "new " + getJavaKataName() + "(str" + columnNameCp
  // + ")" + ");" + RT);
  // sb.append(T2 + "} catch (NumberFormatException nfe) {" + RT);
  // sb.append(T3 + "ArrayList<AppException> exArr = new ArrayList<AppException>();" + RT);
  // sb.append(T3
  // + "exArr.add(new BizLogicAppException(\"MSG_ERR_VALIDATOR_NUMERICAL_INVALID\", "
  // + columnNameSm + "Info));" + RT);
  // sb.append(T3 + "throw new MultipleAppException(exArr);" + RT);
  // sb.append(T2 + "}" + RT);
  // sb.append(T1 + "}" + RT2);
  //
  // return sb.toString();
  // }
}
