package jp.ecuacion.tool.codegenerator.core.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;

/**
 * @author yosuke.tanaka
 *
 */
public abstract class AbstractGen extends ToolForCodeGen {

  protected DataKindEnum xmlFilePostFix;
  protected String rootBasePackage;
  protected String rootBasePackageDirectry;

  protected StringBuilder sb = new StringBuilder();

  // // dataType情報は汎用的に使用するため、全systemName毎のdataType情報を集約しておく
  // // Generator内部で生成すると、作成するファイルの回数だけ生成処理をしてしまうのと、
  // // Constructorの引数に使用と思ったがコンストラクタ呼び出しが多数あり煩雑なので、
  // // static変数に渡すことにした
  // protected static HashMap<String, HashMap<String, DataTypeInfo>> allDtMap;

  public AbstractGen(DataKindEnum xmlFilePostFix) {
    this.xmlFilePostFix = xmlFilePostFix;
    String osName = System.getProperty("os.name");
    rootBasePackage = info.sysCmnRootInfo.getBasePackage();
    rootBasePackageDirectry = rootBasePackage.replaceAll("\\.",
        (osName.equals("Windows")) ? "\\\\" : "/");
  }

  /** */
  public abstract void generate() throws Exception;

  protected String getFilePath(String myPackage) {
    return getFilePathWithSrcKind(myPackage, true);
  }

  protected String getFilePathWithSrcKind(String myPackage, boolean isDirForCopyEveryTime) {
    return getBuildPathRootDirPath(myPackage) + Constants.PATH_SEPARATOR
        + Constants.DIR_SRC_JAVA_PATH + rootBasePackageDirectry + Constants.PATH_SEPARATOR + "base"
        + Constants.PATH_SEPARATOR + myPackage;
  }

  public String getResourcesPath() {
    return getBuildPathRootDirPath(null) + "/src/base/resources";
  }

  private String getBuildPathRootDirPath(String packageName) {

    String basicPath = info.outputDir + "/" + info.systemName + "/"
        + info.getGenPtn().getDirName();
    if (info.getGenPtn() == GeneratePtnEnum.NORMAL
        || info.getGenPtn() == GeneratePtnEnum.NO_GROUP_QUERY) {
      return basicPath;

    } else {
      if (packageName != null && packageName.equals("dao")) {
        return basicPath;

      } else {
        return info.outputDir + "/" + info.systemName + "/common";
      }
    }
  }

  /**
   * @param sb ファイルに出力する文字列を持ったStringBuilder。
   */
  public void outputFile(StringBuilder sb, String path, String fileName) {
    String charEncoding = info.sysCmnRootInfo.getCharacterEncoding();
    try {
      File dir = new File(path);
      dir.mkdirs();

      File file = new File(path, fileName);
      file.createNewFile();

      BufferedWriter bw =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charEncoding));
      bw.write(sb.toString());
      bw.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ==============
  // Javadoc関連
  // ==============

  protected String genJavadocClassFromXml(String javadocClass) {
    return genJavadocFromXml(javadocClass, false);
  }

  protected String genJavadocValueFromXml(String javadocValue) {
    return genJavadocFromXml(javadocValue, true);
  }

  protected String genJavadocFromXml(String javadoc, boolean isIndented) {
    String indent = (isIndented) ? T1 : "";
    return indent + JD_ST + RT + genJavadocPartFromXml(javadoc, isIndented) + indent + JD_END + RT;
  }

  protected String genJavadocPartFromXml(String javadoc, boolean isIndented) {
    if (javadoc == null) {
      javadoc = "";
    }
    
    String indent = (isIndented) ? T1 : "";
    return indent + JD_LN_ST + javadoc.replaceAll("\n", RT).replaceAll(RT, RT + indent + JD_LN_ST)
        + RT;
  }

  protected String genJavadocClass(List<String> arr) {
    return genJavadocCommon(arr, false);
  }

  protected String genJavadocClass(String... args) {
    return genJavadocClass(getStrAlFromA(args));
  }

  protected String genJavadocVar(List<String> arr) {
    return genJavadocCommon(arr, true);
  }

  protected String genJavadocVar(String... args) {
    return genJavadocVar(getStrAlFromA(args));
  }

  protected String genJavadocMethod(List<String> arr) {
    return genJavadocCommon(arr, true);
  }

  protected String genJavadocMethod(String... args) {
    return genJavadocMethod(getStrAlFromA(args));
  }

  protected String genJavadocCommon(List<String> arr, boolean isIndented) {

    String rtn = "";
    String indent = (isIndented) ? T1 : "";

    if (arr == null) {
      arr = new ArrayList<String>();
    }

    rtn += indent + JD_ST + RT;
    for (String line : arr) {
      rtn += indent + JD_LN_ST + line + RT;
    }

    rtn += indent + JD_END + RT;

    return rtn;
  }

  private List<String> getStrAlFromA(String... args) {
    List<String> arr = new ArrayList<>();
    for (String arg : args) {
      arr.add(arg);
    }

    return arr;
  }

}
