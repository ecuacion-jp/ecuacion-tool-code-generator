package jp.ecuacion.tool.codegenerator.core.reader;

import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Reads the common DB column definition sheet and converts it into a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public class ExcelDbCommonReader extends ExcelAbstractDbOrClassReader {
  /** Constructs an instance that targets the common DB item-definition sheet. */
  public ExcelDbCommonReader(SystemCommonRootInfo info) {
    super("DB共通項目定義", DataKindEnum.DB_COMMON, info);
  }
}
