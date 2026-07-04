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
package jp.ecuacion.tool.codegenerator.core.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for all code generators that produce Java source files.
 */
public abstract class AbstractGen extends AbstractCode {

  protected @Nullable DataKindEnum xmlFilePostFix;
  protected String rootBasePackage;
  protected String rootBasePackageDirectry;

  protected StringBuilder sb = new StringBuilder();

  // // Since dataType info is used broadly, dataType info for each systemName is aggregated here.
  // // Generating it inside the generator would repeat the process once per output file.
  // // It was considered as a constructor argument but that would be cumbersome given how many
  // // constructors exist, so it is passed via a static variable instead.
  // protected static HashMap<String, HashMap<String, DataTypeInfo>> allDtMap;

  /** Constructs an instance with the given XML file postfix identifying the data kind. */
  public AbstractGen(@Nullable DataKindEnum xmlFilePostFix) {
    this.xmlFilePostFix = xmlFilePostFix;
    String osName = System.getProperty("os.name");
    rootBasePackage = getInfo().getSysCmnRootInfo().getBasePackage();
    rootBasePackageDirectry = rootBasePackage.replaceAll("\\.",
        osName.equals("Windows") ? "\\\\" : "/");
  }

  /** Executes the code generation process and produces the output source files. */
  public abstract void generate() throws Exception;

  /** Returns the output directory path for the given package name. */
  protected String getFilePath(String myPackage) {
    return getFilePathWithSrcKind(myPackage, true);
  }

  /**
   * Returns the full output file path for the given package, incorporating the source kind root.
   */
  protected String getFilePathWithSrcKind(String myPackage, boolean isDirForCopyEveryTime) {
    return getBuildPathRootDirPath() + Constants.PATH_SEPARATOR
        + Constants.DIR_SRC_JAVA_PATH + rootBasePackageDirectry + Constants.PATH_SEPARATOR + "base"
        + Constants.PATH_SEPARATOR + myPackage;
  }

  /** Returns the path to the base resources output directory. */
  public String getResourcesPath() {
    return getBuildPathRootDirPath() + "/src/main/resources";
  }

  private String getBuildPathRootDirPath() {
    return getInfo().outputDir + "/" + getInfo().getSystemName() + "/normal";
  }

  /**
   * Writes the content of the given StringBuilder to a file at the specified path.
   *
   * @param sb the StringBuilder holding the content to write to the file
   * @param path the directory path where the file will be created
   * @param fileName the name of the file to write
   */
  public void outputFile(StringBuilder sb, String path, String fileName) {
    String charEncoding = getInfo().getSysCmnRootInfo().getCharacterEncoding();
    try {
      File dir = new File(path);
      dir.mkdirs();

      File file = new File(path, fileName);
      file.createNewFile();

      BufferedWriter bw = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charEncoding)));
      bw.write(sb.toString());
      bw.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ==============
  // Javadoc-related
  // ==============

  /** Generates a complete Javadoc block from an XML-sourced description string. */
  protected String genJavadocFromXml(@Nullable String javadoc, boolean isIndented) {
    String indent = isIndented ? T1 : "";
    return indent + JD_ST + RT + genJavadocPartFromXml(javadoc, isIndented) + indent + JD_END + RT;
  }

  /** Generates the inner lines of a Javadoc block from an XML-sourced description string. */
  protected String genJavadocPartFromXml(@Nullable String javadoc, boolean isIndented) {
    if (javadoc == null) {
      javadoc = "";
    }
    
    String indent = isIndented ? T1 : "";
    return indent + JD_LN_ST + javadoc.replaceAll("\n", RT).replaceAll(RT, RT + indent + JD_LN_ST)
        + RT;
  }

  /** Generates a class-level Javadoc block from the given list of description lines. */
  protected String genJavadocClass(List<String> arr) {
    return genJavadocCommon(arr, false);
  }

  /** Generates a class-level Javadoc block from the given description strings. */
  protected String genJavadocClass(String... args) {
    return genJavadocClass(getStrAlFromA(args));
  }

  /** Generates an indented field-level Javadoc block from the given list of description lines. */
  protected String genJavadocVar(List<String> arr) {
    return genJavadocCommon(arr, true);
  }

  /** Generates an indented field-level Javadoc block from the given description strings. */
  protected String genJavadocVar(String... args) {
    return genJavadocVar(getStrAlFromA(args));
  }

  /** Generates an indented method-level Javadoc block from the given list of description lines. */
  protected String genJavadocMethod(List<String> arr) {
    return genJavadocCommon(arr, true);
  }

  /** Generates an indented method-level Javadoc block from the given description strings. */
  protected String genJavadocMethod(String... args) {
    return genJavadocMethod(getStrAlFromA(args));
  }

  /**
   * Generates a Javadoc block from the given lines, optionally applying one level of indentation.
   */
  @SuppressWarnings("unused")
  protected String genJavadocCommon(List<String> arr, boolean isIndented) {

    String rtn = "";
    String indent = isIndented ? T1 : "";

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

  /**
   * Manages a sorted set of import strings and produces the final import block, eliminating
   * redundant class imports already covered by a wildcard.
   */
  public static class ImportBlock extends AbstractCode {

    private TreeSet<String> importSet = new TreeSet<>();

    /** Adds each of the given fully-qualified class names to the import set. */
    public void add(String... strings) {
      for (String str : strings) {
        importSet.add(str);
      }
    }

    /** Removes the given string from the import set if it is present. */
    public void removeIfContains(String string) {
      if (importSet.contains(string)) {
        importSet.remove(string);
      }
    }

    /**
     * Builds and returns the import block string, removing any class-level imports superseded by a
     * wildcard import from the same package.
     */
    public String outputStr() {
      StringBuilder sb = new StringBuilder();

      List<String> asteriskList = new ArrayList<>();
      List<String> noAsteriskList = new ArrayList<>();
      for (String str : importSet) {
        if (str.substring(str.lastIndexOf(".") + 1).equals("*")) {
          asteriskList.add(str);
        } else {
          noAsteriskList.add(str);
        }
      }

      for (String asteriskPkg : asteriskList) {
        String pkg = asteriskPkg.substring(0, asteriskPkg.lastIndexOf("."));
        for (String str : noAsteriskList) {
          if (str.substring(0, str.lastIndexOf(".")).equals(pkg)) {
            importSet.remove(str);
          }
        }
      }

      for (String str : importSet) {
        sb.append("import " + str + ";" + RT);
      }

      return sb.toString();
    }
  }

}
