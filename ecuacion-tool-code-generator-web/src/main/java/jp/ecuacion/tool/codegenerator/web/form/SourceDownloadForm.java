/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
