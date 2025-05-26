package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;


public class DataTypeRootInfo extends AbstractRootInfo {

  public DataTypeRootInfo(List<DataTypeInfo> list) {
    super(DataKindEnum.DATA_TYPE);
    this.dataTypeList = list;
  }

  @Valid
  public List<DataTypeInfo> dataTypeList = new ArrayList<>();
  
  @Override
  public boolean isDefined() {
    return dataTypeList.size() > 0;
  }

  @Override
  public void consistencyCheckAndCoplementData() throws BizLogicAppException {
    // ExcelDataTypeReaderが、PoiStringTableToBeanReaderを使用しているため
    // excel読み込み時にチェックしていることから再度の実行は必要なし。
  }
}
