package jp.ecuacion.tool.codegenerator.core.util.reader;

/**
 * Utility methods for converting between boolean flags and the string marker used in Excel sheets.
 */
public class ReaderUtil {

  public static final String YES = "○";

  /**
   * Returns {@code true} if the given string equals the {@link #YES} marker; {@code false} for
   * {@code null} or any other value.
   */
  @SuppressWarnings("unused")
  public static boolean boolStrToBoolean(String boolStr) {
    if (boolStr == null) {
      return false;

    } else if (boolStr.equals(YES)) {
      return true;

    } else {
      return false;
    }
  }

  /**
   * Converts a boolean to the Excel marker string: {@link #YES} for {@code true}, empty string for
   * {@code false}.
   */
  public static String booleanToBoolStr(boolean bl) {
    return bl ? YES : "";
  }
}
