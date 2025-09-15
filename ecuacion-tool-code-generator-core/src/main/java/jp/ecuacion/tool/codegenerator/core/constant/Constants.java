package jp.ecuacion.tool.codegenerator.core.constant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;


/**
 * 定数を定義。
 *
 * @author 庸介
 */
public class Constants {

  public static final String REG_EX_DT_NAME = "^DT_[A-Z0-9_]*";
  public static final String REG_EX_UP_NUM_US = "^[A-Z0-9_]*$";
  public static final String REG_EX_DOWN_NUM_DOT = "^[a-z0-9\\.]*$";
  public static final String REG_EX_NUM = "^[0-9]*$";
  public static final String REG_EX_NUM_DOT = "^[0-9\\.]*$";
  public static final String REG_EX_AL_NUM_DOT = "^[A-Za-z0-9\\.]*$";
  public static final String REG_EX_AL_NUM_HY = "^[A-Za-z0-9\\-]*$";
  public static final String REG_EX_AL_NUM_US_CM_DOT = "^[A-Za-z0-9_,\\.]*$";

  public static final int OBJECT_CONSTRUCTION_COUNT = 3;
  
  // **
  // ** パッケージ関連
  // **

  public static final String PROJECT_TYPE = "base";

  /** libのパッケージ共通部分. */
  public static final String STR_TOOL_PKG = "jp.ecuacion.tool";
  
  /** code-generator自身のbase package。 */
  public static final String STR_PACKAGE_HOME = STR_TOOL_PKG + ".codegenerator";

  public static final String PKG_STANDARD_VALIDATOR = "jakarta.validation.constraints.*";
  public static final String PKG_CUSTOM_VALIDATOR =
      EclibCoreConstants.PKG + ".jakartavalidation.validator.*";
  
  // **
  // ** ディレクトリ関連
  // **

  // OS非依存のpath separator.
  public static final String PATH_SEPARATOR = File.separator;

  /// ** 生成するクラスファイルの置き場所のルートフォルダ名。 */
  // public static final String DIR_PRODUCT = "products" + PATH_SEPARATOR;

  /** javaソースパス。 */
  public static final String DIR_SRC_JAVA_PATH =
      "src" + PATH_SEPARATOR + "base" + PATH_SEPARATOR + "java" + PATH_SEPARATOR;

  /** デフォルトの~info.xmlファイルの配置場所。テストのために、変更可能としておく。 */
  public static final String DIR_INFO_EXCELS_DEFAULT =
      "../ecuacion-tool-code-generator-batch/ecuacion-tool-code-generator-excel-format";

  // 型関連のString取得

  @SuppressWarnings("serial")
  public static final HashMap<DataTypeKataEnum, String> JAVA_KATA_MAP =
      new HashMap<DataTypeKataEnum, String>() {
        {
          put(DataTypeKataEnum.INTEGER, "Integer");
        }
      };

  // その他

  /**
   * default言語を指定するときに使用。
   */
  public static final String LANG_DEF = "_default";

  public static final int LANG_ADDABLE_MAX = 3;

  //
  // 汎用の正規表現
  //
  /** 全半角（制限なし） ※制限なしのためコード生成なし。 */
  private static final String REG_EX_ALL = null;
  /** 半角。 */
  private static final String REG_EX_HAN = "^[a-zA-Z0-9 -/:-@\\\\[-\\\\`\\\\{-\\\\~]*$";
  /** 半角数字。 */
  private static final String REG_EX_HAN_NUM = "^[0-9]*$";
  /** 英大文字。 */
  private static final String REG_EX_HAN_UC = "^[A-Z]*$";
  /** 英大文字＋_。 */
  private static final String REG_EX_HAN_UC_US = "^[A-Z_]*$";
  /** 英小文字。 */
  private static final String REG_EX_HAN_LC = "^[a-z]*$";
  /** 英小文字＋_。 */
  private static final String REG_EX_HAN_LC_US = "^[a-z_]*$";
  /** 半角数字＋英大文字。 */
  private static final String REG_EX_HAN_NUM_UC = "^[0-9A-Z]*$";
  /** 半角数字＋英大文字＋_。 */
  private static final String REG_EX_HAN_NUM_UC_US = "^[0-9A-Z_]*$";
  /** 半角数字＋英小文字。 */
  private static final String REG_EX_HAN_NUM_LC = "^[0-9a-z]*$";
  /** 半角数字＋英小文字＋_。 */
  private static final String REG_EX_HAN_NUM_LC_US = "^[0-9a-z_]*$";
  /** 半角英字。 */
  private static final String REG_EX_HAN_NUM_UC_LC = "^[a-zA-Z]*$";
  /** 半角英字＋_。 */
  private static final String REG_EX_HAN_NUM_UC_LC_US = "^[a-zA-Z_]*$";
  /** 全角 ※正規表現上、半角以外、という表現。 */
  private static final String REG_EX_ZEN = "^[^ -~｡-ﾟ]*$";

  private static final Map<String, String> stringDataPtnRegExMap = new HashMap<>();

  static {
    stringDataPtnRegExMap.put("REG_EX_ALL", REG_EX_ALL);
    stringDataPtnRegExMap.put("REG_EX_HAN", REG_EX_HAN);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM", REG_EX_HAN_NUM);
    stringDataPtnRegExMap.put("REG_EX_HAN_UC", REG_EX_HAN_UC);
    stringDataPtnRegExMap.put("REG_EX_HAN_UC_US", REG_EX_HAN_UC_US);
    stringDataPtnRegExMap.put("REG_EX_HAN_LC", REG_EX_HAN_LC);
    stringDataPtnRegExMap.put("REG_EX_HAN_LC_US", REG_EX_HAN_LC_US);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_UC", REG_EX_HAN_NUM_UC);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_UC_US", REG_EX_HAN_NUM_UC_US);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_LC", REG_EX_HAN_NUM_LC);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_LC_US", REG_EX_HAN_NUM_LC_US);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_UC_LC", REG_EX_HAN_NUM_UC_LC);
    stringDataPtnRegExMap.put("REG_EX_HAN_NUM_UC_LC_US", REG_EX_HAN_NUM_UC_LC_US);
    stringDataPtnRegExMap.put("REG_EX_ZEN", REG_EX_ZEN);
  }

  public static String getStringDataPtnRegExMap(String key) {
    if (stringDataPtnRegExMap.containsKey(key)) {
      return stringDataPtnRegExMap.get(key);

    } else {
      throw new RuntimeException(
          new BizLogicAppException("MSG_ERR_STRING_DATA_PTN_NOT_FOUND", key));
    }
  }
}
