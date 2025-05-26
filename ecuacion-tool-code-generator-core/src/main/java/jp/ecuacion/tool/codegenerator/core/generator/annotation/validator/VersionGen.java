package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;
import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class VersionGen extends FieldSingleAnnotationGen {

  public VersionGen(ElementType elementType) {
    super("Version", elementType);
  }

  public static boolean needsValidator(boolean isOptLock) {
    return isOptLock;
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // @Versionはshort, integer, long ＋Timestamp と仕様書に書いてある
    return new DataTypeKataEnum[] {INTEGER, SHORT, LONG, TIMESTAMP, DATE_TIME};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    return plistGen;
  }

  @Override
  protected void check() {
  }
}
