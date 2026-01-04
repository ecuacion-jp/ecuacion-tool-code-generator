package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.dao;

import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/**
 * @author 庸介
 *
 */
public abstract class AbstractDaoRelatedGen extends AbstractGen {

  protected final String postfixSm;
  protected final String postfixCp;


  public AbstractDaoRelatedGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);

    // postfixの設定
    boolean usesSpringName = info.sysCmnRootInfo.getUsesSpringNamingConvention();
    postfixSm = (usesSpringName) ? "repositoryimpl" : "dao";
    postfixCp = (usesSpringName) ? "RepositoryImpl" : "Dao";
  }
}
