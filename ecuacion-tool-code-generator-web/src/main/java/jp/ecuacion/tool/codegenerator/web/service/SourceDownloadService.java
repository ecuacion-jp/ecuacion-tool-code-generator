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
package jp.ecuacion.tool.codegenerator.web.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.splib.web.service.SplibGeneral1FormService;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.web.form.SourceDownloadForm;
import net.lingala.zip4j.ZipFile;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Processes source code generation requests and packages the output as a ZIP archive 
 * for download. */
@Service
@Scope("prototype")
public class SourceDownloadService extends SplibGeneral1FormService<SourceDownloadForm> {

  public static final String PROP_WORK_DIR = "jp.ecuacion.tool.code-generator.work-dir";

  @Autowired
  private Environment env;

  @Override
  public void page(@Nullable SourceDownloadForm form, @Nullable UserDetails loginUser)
      throws Exception {}

  @Override
  public void prepareForm(@Nullable SourceDownloadForm form, @Nullable UserDetails loginUser) {

  }

  /** Generates source code from the uploaded Excel file and returns the result as a ZIP archive. */
  @SuppressWarnings("null")
  public ResponseEntity<StreamingResponseBody> execute(MultipartFile multipartFile)
      throws Exception {
    final String originalFileName = Objects.requireNonNull(multipartFile.getOriginalFilename());

    check(originalFileName);

    String dateTimeString = LocalDateTime.now(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS"));
    String threadIdString = Long.valueOf(Thread.currentThread().threadId()).toString();
    Boolean hasDir = PropertiesFileUtil.hasApplication(PROP_WORK_DIR);
    String rootDir = (hasDir ? env.getProperty(PROP_WORK_DIR) : "./app-work")
        + "/ecuacion-tool-code-generator/" + dateTimeString + "-" + threadIdString;
    File rootDirFile = new File(rootDir);

    String inputDir = rootDir + "/" + "inputExcel";
    new File(inputDir).mkdirs();

    String outputDir = rootDir + "/" + "output";
    new File(outputDir).mkdirs();

    // From here on the request-scoped work directory exists on disk, so any failure must
    // clean it up before propagating: otherwise every rejected/failed request (not just
    // successful downloads) leaves it behind forever.
    try {
      // Write the Excel file to the input directory
      Path base = Paths.get(inputDir).toAbsolutePath().normalize();
      Path path = base.resolve(originalFileName).normalize();
      if (!path.startsWith(base)) {
        new Violations()
            .add(new BusinessViolation("SOURCE_DOWNLOAD_MESSAGE_FILE_EXTENSION_UNAVAILABLE"))
            .throwIfAny();
      }
      Files.write(path, multipartFile.getBytes());

      new MainController().execute(inputDir, outputDir);

      // Get all directories from outputDir except ###work###, then zip them
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

      if (dirName.isEmpty()) {
        new Violations()
            .add(new BusinessViolation("SOURCE_DOWNLOAD_MESSAGE_NO_SOURCE_GENERATED"))
            .throwIfAny();
      }

      final String outputFilename = "source.zip";
      ZipFile zipFile = new ZipFile(outputDir + "/" + outputFilename);
      zipFile.addFolder(new File(outputDir + "/" + dirName));
      zipFile.close();

      Path zipFilePath = Path.of(outputDir, outputFilename);
      MediaType contentType = getContentType(zipFilePath);

      // Streaming (rather than handing back a Resource) lets us delete the work directory
      // only once the bytes have actually been written to the client, instead of right after
      // this method returns while Spring may still be reading the file to send it.
      StreamingResponseBody body = outputStream -> {
        try {
          Files.copy(zipFilePath, outputStream);
        } finally {
          MainController.delete(rootDirFile);
        }
      };

      return ResponseEntity.ok().contentType(contentType)
          .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
              .filename(outputFilename, StandardCharsets.UTF_8).build().toString())
          .body(body);

    } catch (Exception e) {
      MainController.delete(rootDirFile);
      throw e;
    }
  }

  private MediaType getContentType(Path path) throws IOException {
    try {
      String contentType = Files.probeContentType(path);
      return contentType == null ? MediaType.APPLICATION_OCTET_STREAM
          : MediaType.parseMediaType(contentType);
    } catch (IOException e) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
  }

  private void check(String originalFileName) {
    if (originalFileName.equals("")) {
      new Violations().add(new BusinessViolation(new String[] {"fileToUpload"},
          "SOURCE_DOWNLOAD_MESSAGE_FILE_NOT_DESIGNATED")).throwIfAny();
    }

    if (!originalFileName.toLowerCase().endsWith(".xlsx")) {
      new Violations()
          .add(new BusinessViolation("SOURCE_DOWNLOAD_MESSAGE_FILE_EXTENSION_UNAVAILABLE"))
          .throwIfAny();
    }
  }
}
