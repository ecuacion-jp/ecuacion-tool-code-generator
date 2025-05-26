package jp.ecuacion.tool.codegenerator.core.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.util.poi.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

public class ExcelEnumReader extends StringOneLineHeaderExcelTableReader {

  private static int COL_DATA_TYPE_NAME = 0;

  private SystemCommonRootInfo sysCmnRootInfo;

  private static final String[] headerLabels = new String[] {"DataType名", "javaのみ",
      "code", "varName", "dispName（デフォルト言語）", "備考",
      "dispName（追加言語1）", "dispName（追加言語2）", "dispName（追加言語3）"};

  public ExcelEnumReader(SystemCommonRootInfo sysCmnRootInfo) {
    super("enum定義", headerLabels, null, 1, null);
    this.sysCmnRootInfo = sysCmnRootInfo;
  }

  public HashMap<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException, AppException {

    HashMap<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    EnumRootInfo rootInfo = new EnumRootInfo();
    rtnMap.put(DataKindEnum.ENUM, rootInfo);

    // 表の情報をlistの形で取得
    List<List<String>> rowList = read(excelPath);

    // 扱いやすいようにenumClassのmapを作成しておく
    Map<String, EnumClassInfo> existingEnumClassMap = new HashMap<>();

    for (List<String> colList : rowList) {
      if (!existingEnumClassMap.containsKey(colList.get(COL_DATA_TYPE_NAME))) {
        existingEnumClassMap.put(colList.get(COL_DATA_TYPE_NAME), new EnumClassInfo(colList));
        rootInfo.enumClassList.add(existingEnumClassMap.get(colList.get(COL_DATA_TYPE_NAME)));
      }

      EnumClassInfo info = existingEnumClassMap.get(colList.get(COL_DATA_TYPE_NAME));
      info.enumList.add(new EnumValueInfo(colList, sysCmnRootInfo));
    }

    return rtnMap;
  }
}
