package jp.ecuacion.tool.codegenerator.core.enums;

import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertyFileUtil;

/**
 * DataTypeの型。
 */
public enum DataTypeKataEnum {

  /**
   * 文字列。<br>
   * postgreSQL的には「character varying（別名varchar）」が正式名称のようだが、ここは通じやすいこちらの名前としておく<br>
   * postgreSQLではtextという型も存在するが、一般的なシステムではカラムサイズの上限を定義すること、上限を定義する以上textとvarcharに差はないことからtext型は用意しない
   */
  STRING,

  /**
   * 1バイト符号付整数。<br>
   * 自動増分1バイト整数はpostgreSQLには存在しない。 postgreSQLとしての挙動はINTに準ずる。<br>
   */
  BYTE,

  /**
   * 2バイト符号付整数。<br>
   * 自動増分2バイト整数はpostgreSQLには存在しない。 postgreSQLとしての挙動はINTに準ずる。<br>
   */
  SHORT,

  /**
   * 整数 4バイト符号付整数。<br>
   * postgreのINTEGERに対応。<br>
   * 自動増分4バイト整数（serial）はここでは定義されていない。INTEGERかつ「自動採番」="○"の場合に、codeGeneratorの中でinteger→serialに置き換える
   * <br>
   * postgreSQLでは、integer(2)のような、桁数を指定する表現はない。Integer＝4バイト、と決まっている。
   * 
   * <br>
   * 一方システムでは桁数がいつも問題になるので、dataTypeとしては桁数を必須指定とする
   */
  INTEGER,

  /**
   * 8バイト符号付整数。<br>
   * 自動増分8バイト整数（bigserial）はここでは定義されていない。INTEGERかつ「自動採番」="○"の場合に、codeGeneratorの中でinteger→serialに置き換える
   * 
   * <br>
   * postgreSQLとしての挙動はINTに準ずる。<br>
   */
  LONG,

  /**
   * 精度の選択可能な高精度整数。 BigDecimalの小数点以下桁数が0のもの、というのとイコールのはずではあるが、BigDecimalは小数用とし、こちらを整数用とする
   */
  BIG_INTEGER,

  /**
   * 単精度（4バイト）浮動小数点。<br>
   * postgreSQL的には「real」が正式名称のようだが、ここは通じやすいこちらの名前としておく
   */
  FLOAT,

  /**
   * 倍精度（8バイト）浮動小数点。<br>
   * postgreSQL的には「double precision」が正式名称のようだが、ここは通じやすいこちらの名前としておく
   */
  DOUBLE,

  /**
   * 精度の選択可能な高精度数値（numeric(p, s)）。<br>
   * postgreSQLとしては、numeric, numeric(p)という指定も可能だが、FWの中ではすべて「numeric(p, s)」という形で使用する。 <br>
   * pは全体桁数（精度=precision）、sは小数点以下桁数（位取り=scale） <br>
   * この値を格納するjava側の型はBigDecimal。BigDecimalもnumericも、INT等に比べ計算処理は著しく遅くなるため、使いどころは気をつけること。
   * 小数点を含む金銭計算は迷わずこれを使用。 逆に、小数点を使用しない計算は、全てBigIntegerを使用すること。
   * javaの仕様上はBigDecimalで整数計算してもよいのだが、用途をパキっと分けたほうがロジック作りやすかったので。
   * 現在は、上記の制限を実現するため、BigDecimalで小数点以下桁数0だとエラーになるようにしている。
   */
  BIG_DECIMAL,

  TIMESTAMP,

  DATE, TIME, DATE_TIME,

  ENUM, BOOLEAN;

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
   * 引数のnameがEnum内に存在すればtrue、しなければfalseを返す。<br>
   * nameがnullまたは空文字の場合はfalseを返す。
   */
  public static boolean hasEnumFromName(String name) {
    for (DataTypeKataEnum enu : DataTypeKataEnum.values()) {
      if (name != null && name.equals(enu.getName())) {
        return true;
      }
    }

    return false;
  }
}
