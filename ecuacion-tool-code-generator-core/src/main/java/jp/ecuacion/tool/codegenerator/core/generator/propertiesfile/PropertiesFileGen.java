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
import java.util.Map;
import java.util.Properties;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.FileUtil;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

public class PropertiesFileGen extends AbstractGen {

  public PropertiesFileGen() {
    super(DataKindEnum.OTHER);
  }

  public void writeMapToPropFile(Map<String, String> propMap, String filenamePrefix, String lang)
      throws IOException, InterruptedException, BizLogicAppException {

    String propFileName = getFileName(filenamePrefix, lang);
    String path = getResourcesPath();
    // Stringbuilderにデータを突っ込む
    StringBuilder sb = new StringBuilder();
    propMap.keySet().forEach(key -> sb.append(key + "=" + propMap.get(key) + "\n"));
    // 作成しようとしているファイル。既に存在する場合は追記。
    File propFile = new File(path + "/" + propFileName);
    // 既にファイルが存在する場合に、その内容を保管しておく入れ物
    StringBuilder origFileSb = new StringBuilder();
    if (propFile.exists()) {
      // 存在するファイルからデータ読み込み
      BufferedReader br = new BufferedReader(new FileReader(propFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        origFileSb.append(line + "\n");
      }

      br.close();
      // ファイルを一度削除
      new File(path + "/" + propFileName).delete();
    }

    // 一旦別ファイル名で保存
    outputPropFile(origFileSb.toString() + sb.toString(), path, propFileName);
  }

  static String getFileName(String filenamePrefix, String lang) {
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

    try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw);) {

      bw.write(str);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generate() throws Exception {}

  public void copyFileToResourceDir(String systemName, String fromFilePath,
      String fromFilenamePrefix, String fromProjectType, String fromLang, String filenamePrefix,
      String projectType, String lang) throws IOException, InterruptedException {

    String filename = getFileName(fromFilenamePrefix, fromLang);

    // 入カストリーム
    try (InputStream input =
        PropertiesFileGen.class.getClassLoader().getResourceAsStream(filename);) {

      if (input == null) {
        return;
      }

      // プロパティファイルとして読み込み
      Properties proper = new Properties();
      proper.load(input);
    }

    String fromfileName = getFileName(fromFilenamePrefix, fromLang);
    try (
        InputStream input =
            PropertiesFileGen.class.getClassLoader().getResourceAsStream(fromfileName);
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader br = new BufferedReader(isr);) {
      String line = "";
      StringBuilder content = new StringBuilder();
      while ((line = br.readLine()) != null) {
        content.append(line + "\n");
      }

      String toFilePath =
          FileUtil.concatFilePaths(getResourcesPath(), getFileName(filenamePrefix, lang));
      try (FileWriter file = new FileWriter(toFilePath);
          PrintWriter pw = new PrintWriter(new BufferedWriter(file));) {
        // ファイルに書き込む
        pw.print(content);
      }
    }
  }
}
