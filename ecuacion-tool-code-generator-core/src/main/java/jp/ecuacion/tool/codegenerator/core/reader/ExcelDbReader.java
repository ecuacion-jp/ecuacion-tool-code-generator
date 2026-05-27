package jp.ecuacion.tool.codegenerator.core.reader;

import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Reads the DB item-definition sheet and converts it into a {@link
 * jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo}.
 */
public class ExcelDbReader extends ExcelAbstractDbOrClassReader {
  /** Constructs an instance that targets the DB item-definition sheet. */
  public ExcelDbReader(SystemCommonRootInfo info) {
    super("DB項目定義", DataKindEnum.DB, info);
  }
}
