package jp.ecuacion.tool.codegenerator.core.generator.annotation;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/** 
 * 固定のannotationを簡単に作成するannotationGen。
 * 固定で考えることもない単純なannnotation（@Versionなど）で一生懸命コード行数を増やすのも無駄なので、
 * 個別clasasをつくらず簡単にannotationを作成できる道を作っておく。
 */
public class SimpleFieldAnnotationGen extends FieldSingleAnnotationGen {

  public SimpleFieldAnnotationGen(String annotationName) {
    super(annotationName, ElementType.FIELD);
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return DataTypeKataEnum.values();
  }

  @Override
  protected ParamGen getParamGen() {
    return new ParamListGen();
  }

  @Override
  protected void check() {
  }
}
