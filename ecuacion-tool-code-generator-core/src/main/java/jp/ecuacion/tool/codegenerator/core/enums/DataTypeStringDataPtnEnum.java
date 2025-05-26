package jp.ecuacion.tool.codegenerator.core.enums;

import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertyFileUtil;


/**
 * varchar型の項目のデータパターン。<br>
 * 個別プロジェクトから参照する場合は、Enumだけを参照するのではなく、dataTypeごと参照する
 */
public enum DataTypeStringDataPtnEnum {

  /**
   * 全半角（制限なし）。
   */
  REG_EX_ALL("001"),

  /**
   * 全角。
   */
  REG_EX_HAN("100"),

  /**
   * 半角。
   */
  REG_EX_HAN_NUM("101"),

  /**
   * 半角数字。
   */
  REG_EX_HAN_UC("102"),

  /**
   * 英大文字。
   */
  REG_EX_HAN_UC_US("103"),

  /**
   * 英大文字＋_。
   */
  REG_EX_HAN_LC("104"),

  /**
   * 英小文字。
   */
  REG_EX_HAN_LC_US("105"),

  /**
   * 英小文字＋_。
   */
  REG_EX_HAN_NUM_UC("106"),

  /**
   * 半角数字＋英大文字。
   */
  REG_EX_HAN_NUM_UC_US("107"),

  /**
   * 半角数字＋英大文字＋_。
   */
  REG_EX_HAN_NUM_LC("108"),

  /**
   * 半角数字＋英小文字。
   */
  REG_EX_HAN_NUM_LC_US("109"),

  /**
   * 半角数字＋英小文字＋_。
   */
  REG_EX_HAN_NUM_UC_LC("110"),

  /**
   * 半角英字。
   */
  REG_EX_HAN_NUM_UC_LC_US("111"),

  /**
   * 半角英字＋_。
   */
  REG_EX_ZEN("112");

  private String code;

  private DataTypeStringDataPtnEnum(String code) {
    this.code = code;
  }

  /**
   * codeを返す。 codeがnull, 空文字の場合は、Enum生成時にチェックエラーとなるため考慮不要
   */
  public String getCode() {
    return code;
  }

  /**
   * nameを返す。 nameがnull, 空文字の場合は、Enum生成時にチェックエラーとなるため考慮不要
   */
  public String getName() {
    return this.toString();
  }

  /**
   * 画面で表示するための名称を返す。 この名称は、getはできるがそれをもとにenumを取得することはできない。 localizeされた言語で返す。
   * 明らかに日本語専用のサイトを作成する場合も多いし、その場合にこの仕組みのほうが楽なので。 またどこかで変わるかもしれないけど。
   */
  public String getDisplayName(Locale locale) {
    return PropertyFileUtil.getEnumName(locale,
        this.getClass().getSimpleName() + "." + this.toString());
  }

  /**
   * defaultのLocaleを使用。
   */
  public String getDisplayName() {
    return PropertyFileUtil.getEnumName(Locale.getDefault(),
        this.getClass().getSimpleName() + "." + this.toString());
  }

  /**
   * 引数のcodeがEnum内に存在すればtrue、しなければfalseを返す。<br>
   * codeがnullまたは空文字の場合はfalseを返す。
   */
  public static boolean hasEnum(String code) {
    for (DataTypeStringDataPtnEnum enu : DataTypeStringDataPtnEnum.values()) {
      if (code != null && code.equals(enu.getCode())) {
        return true;
      }
    }

    return false;
  }

  /**
   * 引数のnameがEnum内に存在すればtrue、しなければfalseを返す。<br>
   * nameがnullまたは空文字の場合はfalseを返す。
   */
  public static boolean hasEnumFromName(String name) {
    for (DataTypeStringDataPtnEnum enu : DataTypeStringDataPtnEnum.values()) {
      if (name != null && name.equals(enu.getName())) {
        return true;
      }
    }

    return false;
  }
}
