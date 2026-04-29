package jp.ecuacion.tool.codegenerator.core.generator.record;

import java.util.Arrays;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Generates SystemCommonBaseRecord.
 */
public class SystemCommonBaseRecordGen extends AbstractBaseRecordGen {

  public SystemCommonBaseRecordGen() {
    super(DataKindEnum.DB_COMMON);
  }

  @Override
  public void generate() {
    internalGenerate(Arrays.asList(new DbOrClassTableInfo[] {info.getCommonTableInfo()}), true);
  }

  public void generateHeader(DbOrClassTableInfo tableInfo) {

    generateHeaderCommon(tableInfo, "jp.ecuacion.splib.core.record.SplibRecord",
        rootBasePackage + ".base.entity.SystemCommon", "jp.ecuacion.splib.core.container.*");

    sb.append("public abstract class SystemCommonBaseRecord extends SplibRecord {" + RT2);
  }

  @Override
  protected void generateMethods(DbOrClassTableInfo ti) {
  }
}
