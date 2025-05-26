package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGenHelper;

public abstract class GenHelperKata extends AbstractGenHelper {

  // protected abstract String getEntityAccessor(String columnNameCp, String columnNameSm,
  // String dataType);

  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    return new String[] {};
  }

  /** 2つのStringの配列をmergeして新たなString配列を返す。 */
  protected String[] mergeStrings(String[] str1, String... strs) {
    String[] rtnStrs = new String[str1.length + strs.length];
    System.arraycopy(str1, 0, rtnStrs, 0, str1.length);
    System.arraycopy(strs, 0, rtnStrs, str1.length, strs.length);

    return rtnStrs;
  }

  /**
   * "EntityGenHelperXxx"というクラス名のうち、Xxxがそのままjavaの型名を表すようにしているので、 Xxxを取り出すための便利メソッドを用意しておく。
   */
  protected String getJavaKataName() {
    return this.getClass().getSimpleName().replace("EntityGenHelper", "");
  }
}
