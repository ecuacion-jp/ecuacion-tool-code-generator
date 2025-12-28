package jp.ecuacion.tool.codegenerator.core.generator.datatype;

import java.lang.annotation.ElementType;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.AnnotationGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.CodeGenUtil;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class DataTypeGen extends AbstractGen {

  private DataTypeInfo dtInfo;

  protected String dataTypeName;
  protected String kata;

  public DataTypeGen(DataTypeInfo dtInfo) {
    super(DataKindEnum.DATA_TYPE);

    sb = new StringBuilder();

    dataTypeName = CodeGenUtil.dataTypeNameToUppperCamel(dtInfo.getDataTypeName());
    kata = dtInfo.getKata().getName();

    this.dtInfo = dtInfo;
  }

  @Override
  public void generate() throws Exception {
    // dataType作成
    genDataType();
  }

  protected void genDataType() {

    // 内部にvalidatorを保持しない場合は作成する意味がないのでスキップ
    if (dtInfo.getValidatorList(true).size() == 0) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("package " + rootBasePackage + ".base.datatype;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
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

  /** Converter生成。 */
  public void generateConverter(boolean refersCommon) {
    // dataType仕様有無で紆余曲折したが、結局dataTypeは使わないこととしたのでenumの場合のみconverterを作成することとした
    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      sb = new StringBuilder();
      String rootPackage = ((refersCommon) ? EclibCoreConstants.PKG : rootBasePackage);
      String dbKata = getDbKata();

      sb.append("package " + rootBasePackage + ".base.converter;" + RT2);
      sb.append("import jakarta.persistence.AttributeConverter;" + RT);
      sb.append("import jakarta.persistence.Converter;" + RT);
      sb.append("import jp.ecuacion.lib.core.util.EnumUtil;" + RT2);
      sb.append("import " + rootPackage + ".base.enums." + dataTypeName + "Enum;" + RT2);

      // Enum→Stringへの変換
      sb.append("@Converter(autoApply = true)" + RT);
      sb.append("public class " + dataTypeName + "Converter implements AttributeConverter<"
          + dataTypeName + "Enum, " + dbKata + "> {" + RT2);
      sb.append(T1 + "@Override" + RT);
      sb.append(T1 + "public " + dbKata + " convertToDatabaseColumn(" + dataTypeName + "Enum obj) {"
          + RT);
      sb.append(T2 + "return (obj == null) ? null : obj.getCode();" + RT);
      sb.append(T1 + "}" + RT2);

      // String→Enumへの変換
      sb.append(T1 + "@Override" + RT);
      sb.append(T1 + "public " + dataTypeName + "Enum convertToEntityAttribute(" + dbKata
          + " obj) {" + RT);
      sb.append(T2 + "// DBの値が正しい限り問題は発生しないので、もし問題が起こればプログラムの問題であることから非チェック例外とする" + RT);
      // sb.append(
      // T2 + "return (obj == null) ? null : " + dataTypeName + "Enum.getEnumFromCode(obj);" + RT);
      sb.append(T2 + "return obj == null ? null : EnumUtil.getEnumFromCode("
          + dataTypeName + "Enum.class, obj);" + RT);
      sb.append(T1 + "}" + RT);
      sb.append("}" + RT);

      outputFile(sb, getFilePath("converter"), dataTypeName + "Converter" + ".java");
    }
  }

  private String getDbKata() {
    if (dtInfo.getKata() == DataTypeKataEnum.ENUM) {
      return "String";

    } else {
      return StringUtil.getUpperCamelFromSnake(dtInfo.getKata().getName());
    }
  }
}
