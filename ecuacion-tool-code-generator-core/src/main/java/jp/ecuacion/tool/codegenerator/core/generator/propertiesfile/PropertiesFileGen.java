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
package jp.ecuacion.tool.codegenerator.core.generator.propertiesfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.FileUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import org.jspecify.annotations.Nullable;

/** Generates and manages properties files such as item_names and enum_names for each language. */
public class PropertiesFileGen extends AbstractGen {

  private static final DetailLogger log = new DetailLogger(PropertiesFileGen.class);

  /** Constructs an instance for the OTHER data kind. */
  public PropertiesFileGen() {
    super(DataKindEnum.OTHER);
  }

  /** Writes the given properties map to the file, optionally suffixed by language. */
  public void writeMapToPropFile(Map<String, String> propMap, String filenamePrefix,
      @Nullable String lang) throws IOException, InterruptedException {

    String propFileName = getFileName(filenamePrefix, lang);
    String path = getResourcesPath();
    // Push data into StringBuilder
    StringBuilder sb = new StringBuilder();
    propMap.keySet().forEach(key -> sb.append(key + "=" + propMap.get(key) + "\n"));
    // The file being created. Append if it already exists.
    File propFile = new File(path + "/" + propFileName);
    // Container to hold the contents of the existing file, if any
    StringBuilder origFileSb = new StringBuilder();
    if (propFile.exists()) {
      // Read data from the existing file
      BufferedReader br = new BufferedReader(new FileReader(propFile, StandardCharsets.UTF_8));
      String line = null;
      while ((line = br.readLine()) != null) {
        origFileSb.append(line + "\n");
      }

      br.close();
      // Delete the file once
      new File(path + "/" + propFileName).delete();
    }

    // Save temporarily under a different file name
    outputPropFile(origFileSb.toString() + sb.toString(), path, propFileName);
  }

  static String getFileName(String filenamePrefix, @Nullable String lang) {
    if (lang == null || lang.equals("")) {
      return filenamePrefix + "_" + Constants.PROJECT_TYPE + ".properties";

    } else {
      return filenamePrefix + "_" + Constants.PROJECT_TYPE + "_" + lang + ".properties";
    }
  }

  private static void outputPropFile(String str, String path, String fileName) {
    File dir = new File(path);
    dir.mkdirs();

    File file = new File(path, fileName);

    try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(fw);) {

      bw.write(str);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generate() throws Exception {}

  /**
   * Copies a properties file from the classpath to the resources output directory, renaming it
   * according to the given prefix and language.
   */
  public void copyFileToResourceDir(String systemName, String fromFilePath,
      String fromFilenamePrefix, String fromProjectType, String fromLang, String filenamePrefix,
      String projectType, String lang) throws IOException, InterruptedException {

    String filename = getFileName(fromFilenamePrefix, fromLang);

    // Input stream
    try (InputStream input =
        PropertiesFileGen.class.getClassLoader().getResourceAsStream(filename);) {

      if (input == null) {
        log.info("The source file for copying the .properties file does not exist."
            + " Skipping and continuing processing. [System name: " + systemName
            + ", File name: " + filename + "]");
        return;
      }

      // Load as a properties file
      Properties proper = new Properties();
      proper.load(input);
    }

    String fromfileName = getFileName(fromFilenamePrefix, fromLang);
    try (
        InputStream input =
            PropertiesFileGen.class.getClassLoader().getResourceAsStream(fromfileName);
        InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);) {
      String line = "";
      StringBuilder content = new StringBuilder();
      while ((line = br.readLine()) != null) {
        content.append(line + "\n");
      }

      String toFilePath =
          FileUtil.concatFilePaths(getResourcesPath(), getFileName(filenamePrefix, lang));
      try (FileWriter file = new FileWriter(toFilePath, StandardCharsets.UTF_8);
          PrintWriter pw = new PrintWriter(new BufferedWriter(file));) {
        // Write to file
        pw.print(content);
      }
    }
  }
}
