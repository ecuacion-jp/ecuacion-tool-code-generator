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

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.EntityGenKindEnum;

/**
 * Generator for the JPA {@code @SequenceGenerator} annotation, defining the sequence used for
 * auto-increment columns.
 */
public class SequenceGeneratorGen extends FieldSingleAnnotationGen {

  private String tableName;
  private String columnName;
  private EntityGenKindEnum entityGenKindEnum;
  private DataTypeInfo dtInfo;

  /**
   * Constructs a SequenceGeneratorGen with the given element type, data type, table name, column
   * name, and entity generation kind.
   */
  public SequenceGeneratorGen(ElementType elementType, DataTypeInfo dtInfo, String tableName,
      String columnName, EntityGenKindEnum entityGenKindEnum) {
    super("SequenceGenerator", elementType);
    this.tableName = tableName;
    this.columnName = columnName;
    this.entityGenKindEnum = entityGenKindEnum;
    this.dtInfo = dtInfo;
  }

  /**
   * Returns {@code true} if the column requires a sequence generator, delegating to {@link
   * GeneratedValueGen#needsValidator}.
   */
  public static boolean needsValidator(DbOrClassColumnInfo colInfo,
      Map<String, Map<String, DataTypeInfo>> allDtMap,
      Map<String, Map<String, AbstractRootInfo>> systemMap, String systemName) {

    return GeneratedValueGen.needsValidator(colInfo);
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    return new DataTypeKataEnum[] {INTEGER, BYTE, SHORT, LONG};
  }

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();

    plistGen.add(new ParamGenWithSingleValue("name", tableName + "_" + columnName + "_SEQ_GEN",
        DataTypeKataEnum.STRING));
    // When defined on SystemCommon, it is not possible to specify a per-table sequenceName,
    // so the sequence name is omitted.
    if (entityGenKindEnum != EntityGenKindEnum.ENTITY_SYSTEM_COMMON) {
      plistGen.add(new ParamGenWithSingleValue("sequenceName",
          tableName + "_" + columnName + "_SEQ", DataTypeKataEnum.STRING));
    }

    List<ValidatorGen> list = dtInfo.getValidatorList(true).stream()
        .filter(v -> v instanceof DecimalMinGen).toList();
    if (list.size() == 1) {
      String minVal = ((DecimalMinGen) list.get(0)).getMinVal();
      if (minVal != null && !minVal.equals("") && !minVal.equals("1")) {
        plistGen.add(new ParamGenWithSingleValue("initialValue", minVal, DataTypeKataEnum.INTEGER));
      }
    }

    plistGen.add(new ParamGenWithSingleValue("allocationSize", "1", DataTypeKataEnum.INTEGER));

    return plistGen;
  }

  @Override
  protected void check() {}
}
