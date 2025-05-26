package jp.ecuacion.tool.codegenerator.web.form;

import jakarta.validation.Valid;
import jp.ecuacion.splib.web.form.SplibGeneralForm;
import jp.ecuacion.tool.codegenerator.web.form.record.SourceDownloadRecord;

public class SourceDownloadForm extends SplibGeneralForm {

  @Valid
  private SourceDownloadRecord sourceDownload = new SourceDownloadRecord();

  public SourceDownloadRecord getSourceDownload() {
    return sourceDownload;
  }

  public void setSourceDownload(SourceDownloadRecord sourceDownload) {
    this.sourceDownload = sourceDownload;
  }
}
