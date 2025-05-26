package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class GeneratedValueGen extends FieldSingleAnnotationGen {

  private String tableName;
  private String columnName;

  public GeneratedValueGen(ElementType elementType, String tableName, String columnName) {
    super("GeneratedValue", elementType);
    this.tableName = tableName;
    this.columnName = columnName;
  }

  public static boolean needsValidator(DbOrClassColumnInfo colInfo) {

    DataTypeInfo dtInfo = colInfo.getDtInfo();

    if ((dtInfo.getKata() == DataTypeKataEnum.INTEGER || dtInfo.getKata() == DataTypeKataEnum.LONG)
        && colInfo.isAutoIncrement()) {
      return true;

    } else {
      return false;
    }
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // 全てOK
    return DataTypeKataEnum.values();
  }

  @Override
  protected ParamListGen getParamGen() {
    return new ParamListGen(
        new ParamGenWithSingleValue("strategy", "GenerationType.SEQUENCE", DataTypeKataEnum.ENUM),
        new ParamGenWithSingleValue("generator", tableName + "_" + columnName + "_SEQ_GEN",
            DataTypeKataEnum.STRING));
  }

  @Override
  protected void check() {
  }
}
