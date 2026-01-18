package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.EntityGenKindEnum;

public class SequenceGeneratorGen extends FieldSingleAnnotationGen {

  private String tableName;
  private String columnName;
  private EntityGenKindEnum entityGenKindEnum;
  private DataTypeInfo dtInfo;

  public SequenceGeneratorGen(ElementType elementType, DataTypeInfo dtInfo, String tableName,
      String columnName, EntityGenKindEnum entityGenKindEnum) {
    super("SequenceGenerator", elementType);
    this.tableName = tableName;
    this.columnName = columnName;
    this.entityGenKindEnum = entityGenKindEnum;
    this.dtInfo = dtInfo;
  }

  public static boolean needsValidator(DbOrClassColumnInfo colInfo,
      HashMap<String, HashMap<String, DataTypeInfo>> allDtMap,
      HashMap<String, HashMap<String, AbstractRootInfo>> systemMap, String systemName) {

    return GeneratedValueGen.needsValidator(colInfo);
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {INTEGER, BYTE, SHORT, LONG};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();

    plistGen.add(new ParamGenWithSingleValue("name", tableName + "_" + columnName + "_SEQ_GEN",
        DataTypeKataEnum.STRING));
    // SystemCommon上に定義する場合は、table毎のsequenceNameを指定することが不可なので
    // sequence nameの指定はなくす
    if (entityGenKindEnum != EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      plistGen.add(new ParamGenWithSingleValue("sequenceName",
          tableName + "_" + columnName + "_SEQ", DataTypeKataEnum.STRING));
    }

    List<ValidatorGen> list = dtInfo.getValidatorList(true).stream()
        .filter(v -> v instanceof DecimalMinGen).toList();
    if (list.size() == 1) {
      String minVal = ((DecimalMinGen) list.get(0)).getMinVal();
      if (minVal != null && !minVal.equals("") && !minVal.equals("1")) {
        plistGen.add(new ParamGenWithSingleValue("initialValue", minVal, DataTypeKataEnum.INTEGER));
      }
    }

    plistGen.add(new ParamGenWithSingleValue("allocationSize", "1", DataTypeKataEnum.INTEGER));

    return plistGen;
  }

  @Override
  protected void check() {}
}
