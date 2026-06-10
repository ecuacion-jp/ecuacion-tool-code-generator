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
package jp.ecuacion.tool.codegenerator.batch.exceptionhandler;

import jp.ecuacion.splib.core.exceptionhandler.SplibExceptionHandlerAction;
import jp.ecuacion.splib.core.util.SplibMailUtil;
import org.springframework.stereotype.Component;

/** Provides an exception handler action for the batch module, sending an error mail on failure. */
@Component
public class AppExceptionHandlerAction implements SplibExceptionHandlerAction {

  private final SplibMailUtil splibMailUtil;

  public AppExceptionHandlerAction(SplibMailUtil splibMailUtil) {
    this.splibMailUtil = splibMailUtil;
  }

  @Override
  public void execute(Throwable th) {
    splibMailUtil.sendErrorMail(th);
  }
}
