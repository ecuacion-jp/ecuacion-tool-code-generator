package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

/** Generator for the JPA {@code @Id} annotation, marking a field as the primary key. */
public class IdGen extends FieldSingleAnnotationGen {

  /** Constructs an IdGen for the given element type and data type information. */
  public IdGen(ElementType elementType, DataTypeInfo dtInfo) {
    super("Id", elementType);
  }

  /** Returns {@code true} if the column is a primary key. */
  public static boolean needsValidator(DbOrClassColumnInfo colInfo) {
    return colInfo.isPk();
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD};
  }

  @SuppressWarnings("null")
  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // 全てOK
    return DataTypeKataEnum.values();
  }

  @Override
  protected @Nullable ParamListGen getParamGen() {
    return null;
  }

  @Override
  protected void check() {
  }
}
