package jp.ecuacion.tool.codegenerator.core.util.reader;

public class ReaderUtil {
  
  public static final String YES = "○";
  
  public static boolean boolStrToBoolean(String boolStr) {
    if (boolStr == null) {
      return false;

    } else if (boolStr.equals(YES)) {
      return true;

    } else {
      return false;
    }
  }
  
  public static String booleanToBoolStr(boolean bl) {
    return bl ? YES : "";
  }
}
