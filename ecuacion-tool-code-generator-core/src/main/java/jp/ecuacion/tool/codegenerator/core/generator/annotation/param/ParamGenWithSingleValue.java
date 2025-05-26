package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

public class ParamGenWithSingleValue extends ParamGen {
  private String key;
  private String value;
  private boolean isStringLiteral;

  public ParamGenWithSingleValue(String key, String value, DataTypeKataEnum kata) {
    this.key = key;
    this.value = value;
    isStringLiteral = (kata == DataTypeKataEnum.STRING);
  }

  public ParamGenWithSingleValue(String key, String value, boolean isStringLiteral) {
    this.key = key;
    this.value = value;
    this.isStringLiteral = isStringLiteral;
  }

  @Override
  public String generateString() {
    String outputValue;
    if (isStringLiteral) {
      outputValue = "\"" + value + "\"";

    } else {
      outputValue = value;
    }

    return key + " = " + outputValue;
  }
}

