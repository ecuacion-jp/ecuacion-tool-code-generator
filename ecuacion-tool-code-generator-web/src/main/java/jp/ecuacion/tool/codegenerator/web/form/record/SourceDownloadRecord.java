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
package jp.ecuacion.tool.codegenerator.web.form.record;

import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.splib.core.record.SplibRecord;
import jp.ecuacion.splib.web.item.HtmlItem;
import jp.ecuacion.splib.web.item.HtmlItemContainer;
import jp.ecuacion.tool.codegenerator.web.constant.Constants;
import org.springframework.web.multipart.MultipartFile;

/** Holds input data for the source code download form. */
@ItemNameKeyClass("SourceDownload")
@SuppressWarnings({"NullAway.Init", "null"})
public class SourceDownloadRecord extends SplibRecord implements HtmlItemContainer {

  private MultipartFile fileToUpload;

  public static HtmlItem[] htmlItems = new HtmlItem[] {};

  /** Constructs a new instance. */
  public SourceDownloadRecord() {
    super();
  }

  @Override
  public HtmlItem[] customizedItems() {
    return mergeHtmlItems(htmlItems, Constants.COMMON_HTML_ITEMS);
  }

  /** Gets the uploaded file. */
  public MultipartFile getFileToUpload() {
    return fileToUpload;
  }

  /** Sets the uploaded file. */
  public void setFileToUpload(MultipartFile fileToUpload) {
    this.fileToUpload = fileToUpload;
  }

}
