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
package jp.ecuacion.tool.codegenerator.core.constant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.lib.validation.constant.EclibValidationConstants;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;


/**
 * Defines constants used throughout the code generator.
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
  // ** Package-related
  // **

  public static final String PROJECT_TYPE = "base";

  /** Common package prefix for the ecuacion tool library. */
  public static final String STR_TOOL_PKG = "jp.ecuacion.tool";

  /** Base package of the code generator itself. */
  public static final String STR_PACKAGE_HOME = STR_TOOL_PKG + ".codegenerator";

  public static final String PKG_STANDARD_VALIDATOR = "jakarta.validation.constraints.*";
  public static final String PKG_CUSTOM_VALIDATOR = EclibValidationConstants.PKG + ".constraints.*";

  // **
  // ** Directory-related
  // **

  // OS-independent path separator.
  public static final String PATH_SEPARATOR = File.separator;

  /// ** Root folder name for placing generated class files. * /
  // public static final String DIR_PRODUCT = "products" + PATH_SEPARATOR;

  /** Root path for generated Java source files. */
  public static final String DIR_SRC_JAVA_PATH =
      "src" + PATH_SEPARATOR + "main" + PATH_SEPARATOR + "java" + PATH_SEPARATOR;

  /** Default location of Excel input files; may be changed for testing purposes. */
  public static final String DIR_INFO_EXCELS_DEFAULT =
      "./ecuacion-tool-code-generator-excel-format";

  // String retrieval for type-related values

  public static final Map<DataTypeKataEnum, String> JAVA_KATA_MAP =
      Map.of(DataTypeKataEnum.INTEGER, "Integer");

  // Other

  /**
   * Key used to specify the default language.
   */
  public static final String LANG_DEF = "_default";

  public static final int LANG_ADDABLE_MAX = 3;

  //
  // General-purpose regular expressions
  //
  /** All characters (no restriction); no code generation because there is no constraint. */
  private static final @org.jspecify.annotations.Nullable String REG_EX_ALL = null;
  /** Half-width ASCII characters. */
  private static final String REG_EX_HAN = "^[a-zA-Z0-9 -/:-@\\\\[-\\\\`\\\\{-\\\\~]*$";
  /** Half-width digits only. */
  private static final String REG_EX_HAN_NUM = "^[0-9]*$";
  /** Uppercase ASCII letters only. */
  private static final String REG_EX_HAN_UC = "^[A-Z]*$";
  /** Uppercase ASCII letters and underscore. */
  private static final String REG_EX_HAN_UC_US = "^[A-Z_]*$";
  /** Lowercase ASCII letters only. */
  private static final String REG_EX_HAN_LC = "^[a-z]*$";
  /** Lowercase ASCII letters and underscore. */
  private static final String REG_EX_HAN_LC_US = "^[a-z_]*$";
  /** Half-width digits and uppercase letters. */
  private static final String REG_EX_HAN_NUM_UC = "^[0-9A-Z]*$";
  /** Half-width digits, uppercase letters, and underscore. */
  private static final String REG_EX_HAN_NUM_UC_US = "^[0-9A-Z_]*$";
  /** Half-width digits and lowercase letters. */
  private static final String REG_EX_HAN_NUM_LC = "^[0-9a-z]*$";
  /** Half-width digits, lowercase letters, and underscore. */
  private static final String REG_EX_HAN_NUM_LC_US = "^[0-9a-z_]*$";
  /** ASCII letters (upper and lower). */
  private static final String REG_EX_HAN_NUM_UC_LC = "^[a-zA-Z]*$";
  /** ASCII letters (upper and lower) and underscore. */
  private static final String REG_EX_HAN_NUM_UC_LC_US = "^[a-zA-Z_]*$";
  /** Full-width characters; expressed as "not half-width" in regex terms. */
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

  /**
   * Returns the regular expression string for the given string-data-pattern key, throwing a
   * violation if the key is not found.
   */
  public static String getStringDataPtnRegExMap(String key) {
    if (stringDataPtnRegExMap.containsKey(key)) {
      return stringDataPtnRegExMap.get(key);

    } else {
      Violations violations =
          new Violations().add(new BusinessViolation("MSG_ERR_STRING_DATA_PTN_NOT_FOUND", key));
      violations.throwIfAny();
      throw new ViolationException(violations);
    }
  }
}
