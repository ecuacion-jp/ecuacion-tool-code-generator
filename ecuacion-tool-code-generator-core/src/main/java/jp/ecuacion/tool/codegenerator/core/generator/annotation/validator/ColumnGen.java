package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class ColumnGen extends FieldSingleAnnotationGen {

  private DbOrClassColumnInfo ci;

  public ColumnGen(ElementType elementType, DbOrClassColumnInfo ci) {
    super("Column", elementType);
    this.ci = ci;
  }

  @Override
  protected void check() {}

  public static boolean needsValidator(String columnName) {
    return true;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    DataTypeInfo dtInfo = ci.getDtInfo();
    
    plistGen.add(new ParamGenWithSingleValue("name", ci.getColumnName(), DataTypeKataEnum.STRING));
    plistGen.add(new ParamGenWithSingleValue("nullable",
        Boolean.valueOf(ci.isNullable()).toString(), DataTypeKataEnum.BOOLEAN));
    if (dtInfo.getKata() == DataTypeKataEnum.STRING || dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      String strLength = (dtInfo.getKata() == DataTypeKataEnum.ENUM) ? dtInfo.getEnumCodeLength()
          : dtInfo.getMaxLength().toString();
      plistGen.add(new ParamGenWithSingleValue("length", strLength, DataTypeKataEnum.INTEGER));
    }
    // 数字のautoIncrementの際に、postgreSQLのcolumnの型をserial/bigSerialにするための設定
    if (ci.isAutoIncrement()) {
      if (dtInfo.getKata() == DataTypeKataEnum.INTEGER) {
        plistGen.add(
            new ParamGenWithSingleValue("columnDefinition", "serial", DataTypeKataEnum.STRING));
      }

      if (dtInfo.getKata() == DataTypeKataEnum.LONG) {
        plistGen.add(
            new ParamGenWithSingleValue("columnDefinition", "bigserial", DataTypeKataEnum.STRING));
      }
    }

    if ((dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
        || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME) && !dtInfo.getNotNeedsTimezone()) {
      plistGen.add(new ParamGenWithSingleValue("columnDefinition", "timestamp with time zone",
          DataTypeKataEnum.STRING));
    }

    if (dtInfo.getKata() == DataTypeKataEnum.DATE) {
      plistGen
          .add(new ParamGenWithSingleValue("columnDefinition", "date", DataTypeKataEnum.STRING));
    }

    if (dtInfo.getKata() == DataTypeKataEnum.TIME) {
      plistGen
          .add(new ParamGenWithSingleValue("columnDefinition", "time", DataTypeKataEnum.STRING));
    }

    return plistGen;
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // 全てOK
    return DataTypeKataEnum.values();
  }
}
