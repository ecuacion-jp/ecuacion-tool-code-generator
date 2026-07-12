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
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil;

/**
 * Generator for the JPA {@code @Convert} annotation, specifying the converter class for enum
 * and year-month columns.
 */
public class ConvertGen extends FieldSingleAnnotationGen {
  private DataTypeInfo dtInfo;
  private ColumnGenUtil code = new ColumnGenUtil();

  /** Constructs a ConvertGen for the given element type and data type information. */
  public ConvertGen(ElementType elementType, DataTypeInfo dtInfo) {
    super("Convert", elementType);
    this.dtInfo = dtInfo;
  }

  /**
   * Returns {@code true} if the data type is an enum or year-month, requiring a {@code @Convert}
   * annotation.
   */
  public static boolean needsValidator(DataTypeInfo dtInfo) {
    return dtInfo.getKata() == DataTypeKataEnum.ENUM
        || dtInfo.getKata() == DataTypeKataEnum.YEAR_MONTH;
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {DataTypeKataEnum.ENUM, DataTypeKataEnum.YEAR_MONTH};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    // YearMonthConverter is a fixed, shared converter (ecuacion-splib-jpa); enum converters are
    // generated per-project, one per DataType.
    String converterClassName = dtInfo.getKata() == DataTypeKataEnum.YEAR_MONTH
        ? "YearMonthConverter.class"
        : code.dataTypeNameToCapitalCamel(dtInfo.getDataTypeName()) + "Converter.class";
    plistGen.add(
        new ParamGenWithSingleValue("converter", converterClassName, DataTypeKataEnum.ENUM));

    return plistGen;
  }

  @Override
  protected void check() {}
}
