package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_DECIMAL;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BIG_INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BOOLEAN;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.BYTE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DATE_TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.DOUBLE;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.ENUM;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.FLOAT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.INTEGER;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.LONG;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.SHORT;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.STRING;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIME;
import static jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum.TIMESTAMP;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public class NotEmptyGen extends ValidatorGen {

  public NotEmptyGen(DataTypeInfo dtInfo) {
    super(getAnnotationName(dtInfo), dtInfo);
  }

  private static String getAnnotationName(DataTypeInfo dtInfo) {
    return (dtInfo.getKata() == DataTypeKataEnum.STRING) ? "FieldNotEmpty" : "FieldNotNull";
  }

  public static boolean needsValidator(DbOrClassColumnInfo ci) {

    // LstUpdTimeカラムで、@FieldNotEmptyを付けると、@PreUpdateを付けていても更新エラーが出る（insert時はPreInsertはちゃんときくのだが・・・）
    // 問題が解決していないので、暫定的にtimestampには@FieldNotEmptyはつけないことにする
    if (ci.getDtInfo().getKata() == DataTypeKataEnum.TIMESTAMP
        || ci.getDtInfo().getKata() == DataTypeKataEnum.DATE_TIME) {
      return false;
    }

    // surrogateKeyでかつ自動採番の場合、insert時はnullで登録しDB側に自動発行してもらうが、
    // その前にvalidateが走りエラーになるためNotEmptyはつけない
    if (ci.isPk() && ci.isAutoIncrement()) {
      return false;
    }

    // いくつかの条件では、DBとしてはnot nullなのだが、DB登録時に自動で埋まる項目のため、
    // 画面の表示項目でないentity（uploadされたエクセルのデータをdb登録など）を画面にエラーを出すためにDB登録前にvalidateすると、
    // その項目の@NotEmptyに引っかかりエラーとなってしまう。
    // それを避けるため、特定条件の場合はfalseを返す
    if (ci.getSpringAuditing() != null || ci.isAutoIncrement() || ci.isOptLock()) {
      return false;
    }

    return !ci.isNullable();
  }

  @Override
  protected DataTypeKataEnum[] getAvailableKatas() {
    // なんでもOK
    return new DataTypeKataEnum[] {STRING, INTEGER, BYTE, SHORT, LONG, FLOAT, DOUBLE, BIG_INTEGER,
        BIG_DECIMAL, TIMESTAMP, DATE, TIME, DATE_TIME, ENUM, BOOLEAN};
  }

  @Override
  protected void getParamGenWithoutFieldId(ParamListGen plistGen) {}

  @Override
  public boolean isJakartaEeStandardValidator() {
    return true;
  }
}
