package jp.ecuacion.tool.codegenerator.core.reader;

import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class ExcelDbCommonReader extends ExcelAbstractDbOrClassReader {
  public ExcelDbCommonReader(SystemCommonRootInfo info) {
    super("DB共通項目定義", DataKindEnum.DB_COMMON, info);
  }
}
