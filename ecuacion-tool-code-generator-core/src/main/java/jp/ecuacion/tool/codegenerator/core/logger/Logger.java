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
          "The instance passed as the first argument, which should be the Logger constructor's caller, is a String. Please pass the correct argument.");
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
