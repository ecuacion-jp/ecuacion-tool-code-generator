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
package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BOOLEAN;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DOUBLE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.ENUM;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.FLOAT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;

import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
 * Generator for the {@code @FieldNotEmpty} or {@code @FieldNotNull} validator annotation, chosen
 * based on data type.
 */
public class NotEmptyGen extends ValidatorGen {

  /** Constructs a NotEmptyGen, selecting the appropriate annotation name based on the data type. */
  public NotEmptyGen(DataTypeInfo dtInfo) {
    super(getAnnotationName(dtInfo), dtInfo);
  }

  private static String getAnnotationName(DataTypeInfo dtInfo) {
    return (dtInfo.getKata() == DataTypeKataEnum.STRING) ? "FieldNotEmpty" : "FieldNotNull";
  }

  /**
   * Returns {@code true} if the column requires a not-empty or not-null validator based on its
   * attributes.
   */
  public static boolean needsValidator(DbOrClassColumnInfo ci) {

    // On the LstUpdTime column, adding @FieldNotEmpty causes an update error even when @PreUpdate
    // is present (though @PreInsert works correctly on insert).
    // Since the problem has not been resolved, @FieldNotEmpty is tentatively not added to
    // timestamp columns.
    if (ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP
        || ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME) {
      return false;
    }

    // For surrogate keys with auto-numbering, null is registered on insert and the DB issues
    // the value automatically. Validation runs before that and would fail, so @NotEmpty is
    // not added.
    if (ci.isPk() && ci.isAutoIncrement()) {
      return false;
    }

    // Under certain conditions, a column is NOT NULL in the DB but is automatically filled
    // at registration time. Validating before DB registration for entities that are not screen
    // display items (e.g., uploading Excel data to DB) would trigger a @NotEmpty error on
    // such columns. To avoid this, return false for specific conditions.
    if (ci.getSpringAuditing() != null || ci.isAutoIncrement() || ci.isOptLock()) {
      return false;
    }

    return !ci.isNullable();
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // All types are OK
    return new DataTypeKataEnum[] {STRING, INTEGER, BYTE, SHORT, LONG, FLOAT, DOUBLE, BIG_INTEGER,
        BIG_DECIMAL, TIMESTAMP, DATE, TIME, DATE_TIME, ENUM, BOOLEAN};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {}

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }
}
