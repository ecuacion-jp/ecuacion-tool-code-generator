package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

public abstract class AbstractRootInfo extends AbstractInfo {
  protected DataKindEnum fileKind;

  public AbstractRootInfo(DataKindEnum fileKind) {
    this(fileKind, true);
  }

  public AbstractRootInfo(DataKindEnum fileKind, boolean isDefined) {
    this.fileKind = fileKind;
  }

  /** 定義がされているかを示す。 */
  public abstract boolean isDefined();
  
  public DataKindEnum getFileKind() {
    return fileKind;
  }
  
  /** excelから読み込んだデータに対し、jakarta validationでチェックできない選択必須などのチェックを行う。 */
  public abstract void consistencyCheckAndCoplementData() throws AppException;
}
