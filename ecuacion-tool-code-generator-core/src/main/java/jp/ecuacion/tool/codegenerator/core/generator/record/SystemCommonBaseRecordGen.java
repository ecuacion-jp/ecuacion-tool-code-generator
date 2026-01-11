package jp.ecuacion.tool.codegenerator.core.generator.record;

import java.util.Arrays;
import jp.ecuacion.lib.core.exception.checked.AppException;
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
  public void generate() throws AppException {
    internalGenerate(Arrays.asList(new DbOrClassTableInfo[] {info.getCommonTableInfo()}), true);
  }

  public void generateHeader(DbOrClassTableInfo tableInfo) throws AppException {

    generateHeaderCommon(tableInfo, "jp.ecuacion.splib.core.record.SplibRecord",
        rootBasePackage + ".base.entity.SystemCommon", "jp.ecuacion.splib.core.container.*");

    sb.append("public abstract class SystemCommonBaseRecord extends SplibRecord {" + RT2);
  }

  @Override
  protected void generateMethods(DbOrClassTableInfo ti) throws AppException {
  }
}
