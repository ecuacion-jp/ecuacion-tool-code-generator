package jp.ecuacion.tool.codegenerator.web.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jp.ecuacion.lib.core.exception.checked.AppExceptionItemIds;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.PropertyFileUtil;
import jp.ecuacion.splib.web.service.SplibGeneral1FormService;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.web.form.SourceDownloadForm;
import net.lingala.zip4j.ZipFile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Scope("prototype")
public class SourceDownloadService extends SplibGeneral1FormService<SourceDownloadForm> {

  @Override
  public void page(SourceDownloadForm form, UserDetails loginUser) throws Exception {}

  @Override
  public void prepareForm(SourceDownloadForm form, UserDetails loginUser) {

  }

  public ResponseEntity<Resource> execute(MultipartFile multipartFile) throws Exception {
    final String originalFileName = multipartFile.getOriginalFilename();

    check(originalFileName);

    String dateTimeString =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS"));
    String threadIdString = Long.valueOf(Thread.currentThread().threadId()).toString();
    String rootDir = PropertyFileUtil.getApplication("app.work-root-dir") + "/" + dateTimeString
        + "-" + threadIdString;

    String inputDir = rootDir + "/" + "inputExcel";
    new File(inputDir).mkdirs();

    String outputDir = rootDir + "/" + "output";
    new File(outputDir).mkdirs();

    // input側にexcelファイルを吐き出しておく
    Path path = Paths.get(inputDir + "/" + originalFileName);
    Files.write(path, multipartFile.getBytes());

    new MainController().execute(inputDir, outputDir);

    // outputDirから###work###以外のディレクトリを取得し、それをzip化
    String dirName = "";
    for (File dir : new File(outputDir).listFiles()) {
      if (!dir.isDirectory()) {
        continue;
      }

      if (dir.getName().startsWith("#")) {
        continue;
      }

      dirName = dir.getName();
      break;
    }

    final String outputFilename = "source.zip";
    ZipFile zipFile = new ZipFile(outputDir + "/" + outputFilename);
    zipFile.addFolder(new File(outputDir + "/" + dirName));
    zipFile.close();

    Path zipFilePath = Path.of(outputDir, outputFilename);
    Resource resource = new PathResource(zipFilePath);

    return ResponseEntity.ok().contentType(getContentType(zipFilePath))
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename(outputFilename, StandardCharsets.UTF_8).build().toString())
        .body(resource);

  }

  private MediaType getContentType(Path path) throws IOException {
    try {
      return MediaType.parseMediaType(Files.probeContentType(path));
    } catch (IOException e) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
  }

  private void check(String originalFileName) throws BizLogicAppException {
    if (originalFileName.equals("")) {
      // ファイル指定なしでsubmitされた
      throw new BizLogicAppException(new AppExceptionItemIds("fileToUpload"),
          "SOURCE_DOWNLOAD_MESSAGE_FILE_NOT_DESIGNATED");
    }

    if (!originalFileName.endsWith(".xlsx")) {
      // ファイルの拡張子が.xlsxではない
      throw new BizLogicAppException("SOURCE_DOWNLOAD_MESSAGE_FILE_EXTENSION_UNAVAILABLE");
    }
  }
}
