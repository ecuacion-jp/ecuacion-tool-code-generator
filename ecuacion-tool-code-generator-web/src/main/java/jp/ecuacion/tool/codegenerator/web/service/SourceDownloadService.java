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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Processes source code generation requests and packages the output as a ZIP archive 
 * for download. */
@Service
@Scope("prototype")
public class SourceDownloadService extends SplibGeneral1FormService<SourceDownloadForm> {

  @Autowired
  private Environment env;

  @Override
  public void page(@Nullable SourceDownloadForm form, @Nullable UserDetails loginUser)
      throws Exception {}

  @Override
  public void prepareForm(@Nullable SourceDownloadForm form, @Nullable UserDetails loginUser) {

  }

  /** Generates source code from the uploaded Excel file and returns the result as a ZIP archive. */
  public ResponseEntity<Resource> execute(MultipartFile multipartFile) throws Exception {
    final String originalFileName = Objects.requireNonNull(multipartFile.getOriginalFilename());

    check(originalFileName);

    String dateTimeString =
        LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS"));
    String threadIdString = Long.valueOf(Thread.currentThread().threadId()).toString();
    Boolean hasDir = PropertiesFileUtil.hasApplication("app.work-root-dir");
    String rootDir = (hasDir ? env.getProperty("app.work-root-dir") : "./app-work")
        + "/ecuacion-tool-code-generator/" + dateTimeString + "-" + threadIdString;

    String inputDir = rootDir + "/" + "inputExcel";
    new File(inputDir).mkdirs();

    String outputDir = rootDir + "/" + "output";
    new File(outputDir).mkdirs();

    // Write the Excel file to the input directory
    Path path = Paths.get(inputDir + "/" + originalFileName);
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

    final String outputFilename = "source.zip";
    ZipFile zipFile = new ZipFile(outputDir + "/" + outputFilename);
    zipFile.addFolder(new File(outputDir + "/" + dirName));
    zipFile.close();

    Path zipFilePath = Path.of(outputDir, outputFilename);
    Resource resource = new FileSystemResource(zipFilePath);

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

  private void check(String originalFileName) {
    if (originalFileName.equals("")) {
      new Violations().add(new BusinessViolation(new String[] {"fileToUpload"},
          "SOURCE_DOWNLOAD_MESSAGE_FILE_NOT_DESIGNATED")).throwIfAny();
    }

    if (!originalFileName.endsWith(".xlsx")) {
      new Violations()
          .add(new BusinessViolation("SOURCE_DOWNLOAD_MESSAGE_FILE_EXTENSION_UNAVAILABLE"))
          .throwIfAny();
    }
  }
}
