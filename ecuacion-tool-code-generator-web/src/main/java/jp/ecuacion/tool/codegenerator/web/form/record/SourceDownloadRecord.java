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
