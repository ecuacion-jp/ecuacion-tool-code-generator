package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;

/** TODO. */
public class GenHelperBigDecimal extends GenHelperWrappedNumber {
  @Override
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    return mergeStrings(super.getNeededImports(columnInfo), "java.math.BigDecimal");
  }
}
