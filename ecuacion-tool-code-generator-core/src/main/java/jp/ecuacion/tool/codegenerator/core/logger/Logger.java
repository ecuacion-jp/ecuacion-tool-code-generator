package jp.ecuacion.tool.codegenerator.core.logger;

import java.util.ArrayList;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.lib.core.util.ObjectsUtil;
import jp.ecuacion.lib.core.util.PropertyFileUtil;

/**
 * @author 庸介
 *
 */
public class Logger {

  public static ArrayList<String> msgList = new ArrayList<String>();

  /**
   * ログ出力結果を試験しやすいように、メッセージを出力する際にLoggerをかませる。
   */
  public static void log(Object object, String msgId, String... msgArgs) {

    if (object.getClass().getName().equals("java.lang.String")) {
      throw new RuntimeException(
          "引数に設定された、LoggerのconstructorとなるべきinstanceがStringです。正しい引数を設定してください。");
    }

    String logMsg = PropertyFileUtil.getMessage(msgId, msgArgs);
    msgList.add(logMsg);

    freeLog(object.getClass(), logMsg);
  }

  /**
   * ログ出力結果を試験しやすいように、メッセージを出力する際にLoggerをかませる。
   */
  public static void log(Class<?> cls, String msgId, String... msgArgs) {
    String logMsg = PropertyFileUtil.getMessage(msgId, msgArgs);
    msgList.add(logMsg);

    freeLog(cls, logMsg);
  }

  private static void freeLog(Class<?> cls, String msg) {
    DetailLogger logger = new DetailLogger(ObjectsUtil.requireNonNull(cls));
    logger.info(ObjectsUtil.requireNonNull(msg));
  }
}
