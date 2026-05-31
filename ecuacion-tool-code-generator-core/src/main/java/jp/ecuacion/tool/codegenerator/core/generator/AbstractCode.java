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
package jp.ecuacion.tool.codegenerator.core.generator;

import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;

/**
 * Base class for all code generator tools, providing commonly used string constants.
 */
public abstract class AbstractCode {
  // Constants
  protected static final String T1 = "  ";
  protected static final String T2 = T1 + T1;
  protected static final String T3 = T2 + T1;
  protected static final String T4 = T3 + T1;
  protected static final String T5 = T4 + T1;
  protected static final String RT = "\r\n";
  protected static final String RT2 = "\r\n\r\n";
  protected static final String SP = " ";
  protected static final String JD_LN_ST = " * ";
  protected static final String JD_ST = "/**";
  protected static final String JD_END = " */";

  /** Returns the current thread's {@link CodeGenContext} from the thread-local context. */
  protected CodeGenContext getInfo() {
    return MainController.tlInfo.get();
  }

}
