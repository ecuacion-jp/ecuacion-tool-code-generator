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
package jp.ecuacion.tool.codegenerator.core.generator.datatype;

import java.lang.annotation.ElementType;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ColumnGenUtil;


/** Generates data type validator annotation and attribute converter source files. */
public class DataTypeGen extends AbstractGen {

  private DataTypeInfo dtInfo;

  protected String dataTypeName;
  protected String kata;

  private ColumnGenUtil code = new ColumnGenUtil();

  /** Constructs an instance for the given data type info. */
  public DataTypeGen(DataTypeInfo dtInfo) {
    super(DataKindEnum.DATA_TYPE);

    sb = new StringBuilder();

    dataTypeName = code.dataTypeNameToCapitalCamel(dtInfo.getDataTypeName());
    kata = dtInfo.getKata().toString();

    this.dtInfo = dtInfo;
  }

  @Override
  public void generate() throws Exception {
    // Create dataType
    genDataType();
  }

  /** Generates the data type validator annotation source file if validators are defined. */
  protected void genDataType() {

    // Skip if no validators are held internally, as there is nothing to generate
    if (dtInfo.getValidatorList(true).size() == 0) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("package " + rootBasePackage + ".base.datatype;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("java.lang.annotation.*", "jakarta.validation.*");

    importMgr.add(AnnotationGenUtil.getNeededImports(dtInfo.getValidatorList(true)));

    sb.append(importMgr.outputStr() + RT);

    sb.append("@Retention(RetentionPolicy.RUNTIME)" + RT);
    sb.append("@Target(ElementType.FIELD)" + RT);
    sb.append("@Documented" + RT);
    sb.append("@Constraint(validatedBy = {})" + RT);

    sb.append(AnnotationGenUtil.getCode(dtInfo.getValidatorList(true), ElementType.TYPE));

    // public class NameDataType {
    sb.append("public @interface " + dataTypeName + "DataTypeValidator {" + RT2);

    sb.append(T1 + "String message() default \"\";" + RT);
    sb.append(T1 + "Class<?>[] groups() default {};" + RT);
    sb.append(T1 + "Class<? extends Payload>[] payload() default {};" + RT2);

    sb.append("}" + RT);

    outputFile(sb, getFilePath("datatype"), dataTypeName + "DataTypeValidator.java");
  }

  /** Generates the JPA attribute converter class for ENUM type data types. */
  public void generateConverter(boolean refersCommon) {
    // After much deliberation over the dataType spec, it was decided not to use dataType, so
    // converters are only created for the enum case.
    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      sb = new StringBuilder();
      String rootPackage = ((refersCommon) ? EclibCoreConstants.PKG : rootBasePackage);
      String dbKata = getDbKata();

      sb.append("package " + rootBasePackage + ".base.converter;" + RT2);
      sb.append("import jakarta.persistence.AttributeConverter;" + RT);
      sb.append("import jakarta.persistence.Converter;" + RT);
      sb.append("import jp.ecuacion.lib.core.util.EnumUtil;" + RT2);
      sb.append("import " + rootPackage + ".base.enums." + dataTypeName + "Enum;" + RT2);

      // Enum to String conversion
      sb.append("@Converter(autoApply = true)" + RT);
      sb.append("public class " + dataTypeName + "Converter implements AttributeConverter<"
          + dataTypeName + "Enum, " + dbKata + "> {" + RT2);
      sb.append(T1 + "@Override" + RT);
      sb.append(T1 + "public " + dbKata + " convertToDatabaseColumn(" + dataTypeName + "Enum obj) {"
          + RT);
      sb.append(T2 + "return (obj == null) ? null : obj.getCode();" + RT);
      sb.append(T1 + "}" + RT2);

      // String to Enum conversion
      sb.append(T1 + "@Override" + RT);
      sb.append(T1 + "public " + dataTypeName + "Enum convertToEntityAttribute(" + dbKata
          + " obj) {" + RT);
      sb.append(T2
          + "// As long as the DB value is valid no issue will occur, "
          + "so any problem here is a programming bug and an unchecked exception is appropriate."
          + RT);
      // sb.append(
      // T2 + "return (obj == null) ? null : " + dataTypeName + "Enum.getEnumFromCode(obj);" + RT);
      sb.append(T2 + "return obj == null ? null : EnumUtil.getEnumFromCode(" + dataTypeName
          + "Enum.class, obj);" + RT);
      sb.append(T1 + "}" + RT);
      sb.append("}" + RT);

      outputFile(sb, getFilePath("converter"), dataTypeName + "Converter" + ".java");
    }
  }

  private String getDbKata() {
    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      return "String";

    } else {
      return StringUtil.getUpperCamelFromSnake(dtInfo.getKata().toString());
    }
  }
}
