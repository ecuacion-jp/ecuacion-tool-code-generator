package jp.ecuacion.tool.codegenerator.core.reader;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.util.poi.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

public abstract class ExcelAbstractDbOrClassReader extends StringOneLineHeaderExcelTableReader {

  private static int COL_TABLE_NAME = 0;

  private SystemCommonRootInfo sysCmnRootInfo;
  // private StringUtil strUtil = new StringUtil();
  private DataKindEnum fileKind;

  private static final String[] headerLabels =
      new String[] {"テーブル名", "表示名（デフォルト言語）", "カラム名", "dataType", "dataType存在確認", "javaのみ", "PK・UK",
          "nullable", "自動採番", "強制採番", "自動更新", "強制更新", "グループ識別項目", "SPRING監査", "関連：種類",
          "関連：direction", "関連：参照元変数名", "関連：参照先テーブル", "関連：参照先カラム", "関連：参照先変数名", "関連：eager", "index1",
          "index2", "index3", "備考", "表示名（追加言語1）", "表示名（追加言語2）", "表示名（追加言語3）"};

  public ExcelAbstractDbOrClassReader(@Nonnull String sheetName, DataKindEnum fileKind,
      SystemCommonRootInfo systemCommonRootInfo) {
    super(sheetName, headerLabels, null, 1, null);
    this.fileKind = fileKind;
    sysCmnRootInfo = systemCommonRootInfo;
  }

  public HashMap<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException, AppException {

    HashMap<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    DbOrClassRootInfo rootInfo = new DbOrClassRootInfo(fileKind);
    rtnMap.put(fileKind, rootInfo);

    // 表の情報をlistの形で取得
    List<List<String>> rowList = read(excelPath);

    // 扱いやすいようにenumClassのmapを作成しておく
    Map<String, DbOrClassTableInfo> existingTableMap = new HashMap<>();

    for (List<String> colList : rowList) {
      if (!existingTableMap.containsKey(colList.get(COL_TABLE_NAME))) {
        existingTableMap.put(colList.get(COL_TABLE_NAME),
            new DbOrClassTableInfo(colList.get(COL_TABLE_NAME)));
        rootInfo.tableList.add(existingTableMap.get(colList.get(COL_TABLE_NAME)));
      }

      DbOrClassTableInfo info = existingTableMap.get(colList.get(COL_TABLE_NAME));
      info.columnList.add(new DbOrClassColumnInfo(colList, sysCmnRootInfo.getDefaultLang(),
          sysCmnRootInfo.getSupportLang1(), sysCmnRootInfo.getSupportLang2(),
          sysCmnRootInfo.getSupportLang3()));
    }

    return rtnMap;
  }
}
