package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;

/** A ParamGen implementation that generates a single key-value annotation parameter. */
public class ParamGenWithSingleValue extends ParamGen {
  private String key;
  private String value;
  private boolean isStringLiteral;

  /** Constructs an instance, deriving whether the value is a string literal from the given kata. */
  public ParamGenWithSingleValue(String key, String value, DataTypeKataEnum kata) {
    this.key = key;
    this.value = value;
    isStringLiteral = (kata == DataTypeKataEnum.STRING);
  }

  /**
   * Constructs an instance with an explicit flag indicating whether the value is a string literal.
   */
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

