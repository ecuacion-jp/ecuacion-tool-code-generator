package jp.ecuacion.tool.codegenerator.web.form.record;

import jp.ecuacion.splib.core.record.SplibRecord;
import jp.ecuacion.splib.web.bean.HtmlItem;
import jp.ecuacion.splib.web.form.record.RecordInterface;
import jp.ecuacion.tool.codegenerator.web.constant.Constants;
import org.springframework.web.multipart.MultipartFile;

public class SourceDownloadRecord extends SplibRecord implements RecordInterface {

  private MultipartFile fileToUpload;

  public static HtmlItem[] htmlItems = new HtmlItem[] {};

  public SourceDownloadRecord() {
    super();
  }

  @Override
  public HtmlItem[] getHtmlItems() {
    return mergeHtmlItems(htmlItems, Constants.COMMON_HTML_ITEMS);
  }

  public MultipartFile getFileToUpload() {
    return fileToUpload;
  }

  public void setFileToUpload(MultipartFile fileToUpload) {
    this.fileToUpload = fileToUpload;
  }

}
