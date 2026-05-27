package jp.ecuacion.tool.codegenerator.core.dto;

import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/** TODO. */
public abstract class AbstractRootInfo extends AbstractInfo {
  protected DataKindEnum fileKind;

  /** TODO. */
  public AbstractRootInfo(DataKindEnum fileKind) {
    this(fileKind, true);
  }

  /** TODO. */
  public AbstractRootInfo(DataKindEnum fileKind, boolean isDefined) {
    this.fileKind = fileKind;
  }

  /** 定義がされているかを示す。. */
  public abstract boolean isDefined();
  
  /** TODO. */
  public DataKindEnum getFileKind() {
    return fileKind;
  }
  
  /** excelから読み込んだデータに対し、jakarta validationでチェックできない選択必須などのチェックを行う。. */
  public abstract void consistencyCheckAndCoplementData();
}
