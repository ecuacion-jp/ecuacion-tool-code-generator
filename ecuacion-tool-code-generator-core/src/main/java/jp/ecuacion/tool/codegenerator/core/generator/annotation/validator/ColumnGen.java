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

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

/**
 * Generator for the JPA {@code @Column} annotation, setting name, nullable, length, and column
 * definition.
 */
public class ColumnGen extends FieldSingleAnnotationGen {

  private DbOrClassColumnInfo ci;

  /** Constructs a ColumnGen for the given element type and column information. */
  public ColumnGen(ElementType elementType, DbOrClassColumnInfo ci) {
    super("Column", elementType);
    this.ci = ci;
  }

  @Override
  protected void check() {}

  /** Returns {@code true} since the {@code @Column} annotation is always required. */
  public static boolean needsValidator(String columnName) {
    return true;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    DataTypeInfo dtInfo = ci.getDtInfo();
    
    plistGen.add(new ParamGenWithSingleValue("name", ci.getName(), DataTypeKataEnum.STRING));
    plistGen.add(new ParamGenWithSingleValue("nullable",
        Boolean.valueOf(ci.isNullable()).toString(), DataTypeKataEnum.BOOLEAN));
    if (dtInfo.getKata() == DataTypeKataEnum.STRING || dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      String strLength;
      if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
        strLength = dtInfo.getEnumCodeLength();
      } else {
        Integer maxLength = dtInfo.getMaxLength();
        if (maxLength == null) {
          throw new IllegalStateException(
              "STRING data type requires maxLength: " + dtInfo.getDataTypeName());
        }
        strLength = maxLength.toString();
      }
      plistGen.add(new ParamGenWithSingleValue("length", strLength, DataTypeKataEnum.INTEGER));
    }
    // Setting to map the PostgreSQL column type to serial/bigserial for numeric auto-increment
    if (ci.isAutoIncrement()) {
      if (dtInfo.getKata() == DataTypeKataEnum.INTEGER) {
        plistGen.add(
            new ParamGenWithSingleValue("columnDefinition", "serial", DataTypeKataEnum.STRING));
      }

      if (dtInfo.getKata() == DataTypeKataEnum.LONG) {
        plistGen.add(
            new ParamGenWithSingleValue("columnDefinition", "bigserial", DataTypeKataEnum.STRING));
      }
    }

    if ((dtInfo.getKata() == DataTypeKataEnum.TIMESTAMP
        || dtInfo.getKata() == DataTypeKataEnum.DATE_TIME) && !dtInfo.getNotNeedsTimezone()) {
      plistGen.add(new ParamGenWithSingleValue("columnDefinition", "timestamp with time zone",
          DataTypeKataEnum.STRING));
    }

    if (dtInfo.getKata() == DataTypeKataEnum.DATE) {
      plistGen
          .add(new ParamGenWithSingleValue("columnDefinition", "date", DataTypeKataEnum.STRING));
    }

    if (dtInfo.getKata() == DataTypeKataEnum.TIME) {
      plistGen
          .add(new ParamGenWithSingleValue("columnDefinition", "time", DataTypeKataEnum.STRING));
    }

    return plistGen;
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // All types are OK
    return DataTypeKataEnum.values();
  }
}
