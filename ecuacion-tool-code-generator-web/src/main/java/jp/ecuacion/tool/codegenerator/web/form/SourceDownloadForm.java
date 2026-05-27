package jp.ecuacion.tool.codegenerator.web.form;

import jakarta.validation.Valid;
import jp.ecuacion.splib.web.form.SplibGeneralForm;
import jp.ecuacion.tool.codegenerator.web.form.record.SourceDownloadRecord;

/** Contains form data for the source code download feature. */
public class SourceDownloadForm extends SplibGeneralForm {

  @Valid
  private SourceDownloadRecord sourceDownload = new SourceDownloadRecord();

  /** Gets the source download record. */
  public SourceDownloadRecord getSourceDownload() {
    return sourceDownload;
  }

  /** Sets the source download record. */
  public void setSourceDownload(SourceDownloadRecord sourceDownload) {
    this.sourceDownload = sourceDownload;
  }
}
