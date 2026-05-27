package jp.ecuacion.tool.codegenerator.core.generator.dao;

import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/** Abstract base class for DAO-related code generators. */
public abstract class AbstractDaoRelatedGen extends AbstractGen {

  protected final String postfixSm;
  protected final String postfixCp;


  /**
   * Constructs an instance and resolves DAO postfix strings based on Spring naming convention
   * settings.
   */
  public AbstractDaoRelatedGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);

    // postfixの設定
    boolean usesSpringName = info.getSysCmnRootInfo().getUsesSpringNamingConvention();
    postfixSm = (usesSpringName) ? "repositoryimpl" : "dao";
    postfixCp = (usesSpringName) ? "RepositoryImpl" : "Dao";
  }
}
