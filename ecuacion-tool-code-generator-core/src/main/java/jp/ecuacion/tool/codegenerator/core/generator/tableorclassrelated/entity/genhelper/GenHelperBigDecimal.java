package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.genhelper;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;

public class GenHelperBigDecimal extends GenHelperWrappedNumber {
  @Override
  public String[] getNeededImports(DbOrClassColumnInfo columnInfo) {
    return mergeStrings(super.getNeededImports(columnInfo), "java.math.BigDecimal");
  }
}
