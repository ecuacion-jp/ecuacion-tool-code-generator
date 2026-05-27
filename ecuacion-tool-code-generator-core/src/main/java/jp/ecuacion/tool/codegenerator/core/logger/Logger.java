package jp.ecuacion.tool.codegenerator.core.logger;

import java.util.ArrayList;
import java.util.Locale;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertiesFileUtil;

/** Logging wrapper that collects log messages into an in-memory list for test verification. */
public class Logger {

  public static ArrayList<String> msgList = new ArrayList<String>();

  /**
   * Resolves the message for the given id and args, appends it to the in-memory list,
   * and writes it to the underlying detail logger.
   */
  public static void log(Object object, String msgId, Object... msgArgs) {

    if (object.getClass().getName().equals("java.lang.String")) {
      throw new RuntimeException(
          "引数に設定された、LoggerのconstructorとなるべきinstanceがStringです。正しい引数を設定してください。");
    }

    String logMsg = PropertiesFileUtil.getMessage(Locale.ENGLISH, msgId, msgArgs);
    msgList.add(logMsg);

    freeLog(object.getClass(), logMsg);
  }

  /**
   * Resolves the message for the given id and args, appends it to the in-memory list,
   * and writes it to the underlying detail logger using the supplied class as the logger name.
   */
  public static void log(Class<?> cls, String msgId, Object... msgArgs) {
    String logMsg = PropertiesFileUtil.getMessage(Locale.ENGLISH, msgId, msgArgs);
    msgList.add(logMsg);

    freeLog(cls, logMsg);
  }

  private static void freeLog(Class<?> cls, String msg) {
    DetailLogger logger = new DetailLogger(ObjectsUtil.requireNonNull(cls));
    logger.info(ObjectsUtil.requireNonNull(msg));
  }
}
