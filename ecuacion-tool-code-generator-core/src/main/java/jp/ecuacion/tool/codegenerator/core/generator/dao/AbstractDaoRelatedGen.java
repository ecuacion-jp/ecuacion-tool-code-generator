/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    // Configure postfix
    boolean usesSpringName = info.getSysCmnRootInfo().getUsesSpringNamingConvention();
    postfixSm = (usesSpringName) ? "repositoryimpl" : "dao";
    postfixCp = (usesSpringName) ? "RepositoryImpl" : "Dao";
  }
}
