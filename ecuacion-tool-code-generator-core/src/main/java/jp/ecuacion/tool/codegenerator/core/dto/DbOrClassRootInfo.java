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
package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;


/**
 * Holds root information for DB or class entity definitions, including the list of tables
 * and validation logic.
 */
public class DbOrClassRootInfo extends AbstractRootInfo implements ItemContainer {
  @Override
  public Item[] customizedItems() {
    return new Item[] {};
  }
  
  @Valid
  public List<DbOrClassTableInfo> tableList = new ArrayList<DbOrClassTableInfo>();

  /** Constructs an instance for the given data kind. */
  public DbOrClassRootInfo(DataKindEnum fileKind) {
    super(fileKind);
  }

  @Override
  public boolean isDefined() {
    return tableList.size() > 0;
  }

  /**
   * Validates all tables and columns via bean validation, then runs APP_COMMON-specific
   * consistency checks.
   */
  @Override
  public void consistencyCheckAndComplementData() {
    new Violations()
        .addAll(Validation.buildDefaultValidatorFactory().getValidator().validate(this))
        .throwIfAny();

    for (DbOrClassTableInfo tbl : tableList) {
      tbl.dataConsistencyCheck();
    }

    // Check only for systemCommon
    systemCommonCheck();

    // Applies to both DB and DB_COMMON: a CB/LB column must not have a relation.
    checkCbLbColumnsDoNotHaveRelations();
  }

  private void systemCommonCheck() {

    if (fileKind.equals(DataKindEnum.DB_COMMON)) {

      // There should be at most one table
      if (tableList.size() > 1) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_CONSISTENCY_CHECK_APP_COMMON_ENTITY_MUST_BE_0_OR_1")).throwIfAny();
      }

      if (tableList.size() == 0) {
        return;
      }

      // The following applies when a parent entity exists
      DbOrClassTableInfo ti = tableList.get(0);

      // Name must be AppCommon
      if (!ti.getName().equals("APP_COMMON")) {
        new Violations().add(new BusinessViolation(
            "MSG_ERR_CONSISTENCY_CHECK_NAME_OF_APP_COMMON_ENTITY_CANNOT_BE_CHANGED"))
            .throwIfAny();
      }
    }
  }

  /**
   * A column marked as CB ({@code @CreatedBy}) or LB ({@code @LastModifiedBy}) must not have a
   * relation, regardless of whether it is defined in DB or DB_COMMON (AppCommon). If such a
   * column has a relation, {@code AuditorAware} must return an entity of the related type, and if
   * that implementation resolves the auditor via a JPA repository call (e.g. {@code findById}),
   * that call can trigger Hibernate's auto-flush of the entity currently being updated. The flush
   * re-resolves the CB/LB value, calling the same {@code AuditorAware} again from within the
   * flush it just triggered, and the recursion ends in a {@code StackOverflowError} that fails
   * the update.
   */
  private void checkCbLbColumnsDoNotHaveRelations() {
    for (DbOrClassTableInfo ti : tableList) {
      for (DbOrClassColumnInfo ci : ti.columnList) {
        String springAuditing = ci.getSpringAuditing();
        boolean isCbOrLb = "CB".equals(springAuditing) || "LB".equals(springAuditing);
        if (isCbOrLb && ci.getRelationKind() != null) {
          new Violations().add(new BusinessViolation("MSG_ERR_CB_LB_COLUMN_CANNOT_HAVE_RELATION",
              ti.getName(), ci.getName())).throwIfAny();
        }
      }
    }
  }
}
