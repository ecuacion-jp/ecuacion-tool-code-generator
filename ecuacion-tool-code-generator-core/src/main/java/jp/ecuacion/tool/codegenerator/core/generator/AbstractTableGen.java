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
package jp.ecuacion.tool.codegenerator.core.generator;

import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo.RelationRefInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.RelationKindEnum;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for generators that produce Java source files derived from table definitions.
 *
 * <p>Provides the DAO/repository naming postfix resolution and a shared accessor generator for
 * relation fields, used by both entity and record generators.
 */
public abstract class AbstractTableGen extends AbstractGen {

  protected final String postfixSm;
  protected final String postfixCp;

  /**
   * Constructs an instance and resolves the DAO/repository postfix strings based on the Spring
   * naming convention setting.
   */
  public AbstractTableGen(DataKindEnum xmlFilePostFix) {
    super(xmlFilePostFix);
    boolean usesSpringName = getInfo().getSysCmnRootInfo().getUsesSpringNamingConvention();
    postfixSm = usesSpringName ? "repositoryimpl" : "dao";
    postfixCp = usesSpringName ? "RepositoryImpl" : "Dao";
  }

  /**
   * Appends getter and setter methods for a relation field to {@link AbstractGen#sb}.
   *
   * <p>When {@code info} indicates a one-to-many relation, the data type is wrapped in
   * {@code List<>} and the field name is taken from
   * {@link RelationRefInfo#getEmptyConsideredFieldNameToReferFromTable()}.
   *
   * @param relEntityNameLw lowercase-camel entity name (e.g. {@code "orderDetail"})
   * @param relFieldName the relation field name
   * @param info relation ref info for bidirectional relations, or {@code null} for direct ones
   * @param entityTypeSuffix appended after the capitalized entity name ({@code ""} for entity
   *        classes, {@code "BaseRecord"} for record classes)
   */
  protected void appendRelationAccessor(@Nullable String relEntityNameLw,
      @Nullable String relFieldName, @Nullable RelationRefInfo info, String entityTypeSuffix) {
    String baseType = StringUtils.capitalize(relEntityNameLw) + entityTypeSuffix;
    String relDataType = baseType;
    if (info != null && info.getRelationKind() == RelationKindEnum.ONE_TO_MANY) {
      relFieldName = info.getEmptyConsideredFieldNameToReferFromTable();
      relDataType = "List<" + baseType + ">";
    }

    sb.append(T1 + "public " + relDataType + " get" + StringUtils.capitalize(relFieldName) + "() {"
        + RT);
    sb.append(T2 + "return " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(T1 + "public void set" + StringUtils.capitalize(relFieldName) + "(" + relDataType
        + " " + relFieldName + ") {" + RT);
    sb.append(T2 + "this." + relFieldName + " = " + relFieldName + ";" + RT);
    sb.append(T1 + "}" + RT2);
  }
}
