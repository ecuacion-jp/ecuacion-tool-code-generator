package jp.ecuacion.tool.codegenerator.core.reader;

import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public class ExcelDbReader extends ExcelAbstractDbOrClassReader {
  public ExcelDbReader(SystemCommonRootInfo info) {
    super("DB項目定義", DataKindEnum.DB, info);
  }
}
