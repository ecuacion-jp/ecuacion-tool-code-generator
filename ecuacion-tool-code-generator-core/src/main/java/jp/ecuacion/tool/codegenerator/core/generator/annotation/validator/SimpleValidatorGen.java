package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/** 
 * 引数なしのvalidatorを簡単に作成する。
 * 固定で考えることもない単純なvalidator（@IntegerStringなど）で一生懸命コード行数を増やすのも無駄なので、
 * 個別classをつくらず簡単にvalidatorを作成できる道を作っておく。
 */
public class SimpleValidatorGen extends ValidatorGen {

  private boolean isJakartaEeStandardValidator;
  private DataTypeKataEnum[] availableKatas;

  public SimpleValidatorGen(String annotationName, DataTypeInfo dtInfo,
      boolean isJakartaEeStandardValidator, DataTypeKataEnum[] availableKatas) {
    super(annotationName, dtInfo);

    this.isJakartaEeStandardValidator = isJakartaEeStandardValidator;
    this.availableKatas = availableKatas;
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return availableKatas;
  }

  @Override
  protected void check() {}

  @Override
  public boolean isJakartaEeStandardValidator() {
    return isJakartaEeStandardValidator;
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {
    
  }
}
