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
package jp.ecuacion.tool.codegenerator.cli.command;

import java.util.Objects;
import jp.ecuacion.splib.cli.runner.SplibCliRunner;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Executes the code generation process. */
@Component
public class CodeGeneratorRunner implements SplibCliRunner {

  public static final String PROP_INPUT_DIR = "jp.ecuacion.tool.code-generator.input-dir";
  public static final String PROP_OUTPUT_DIR = "jp.ecuacion.tool.code-generator.output-dir";

  @Value("${" + PROP_INPUT_DIR + ":" + Constants.DIR_INFO_EXCELS_DEFAULT + "}")
  private @Nullable String inputDir;

  @Value("${" + PROP_OUTPUT_DIR + ":./products/}")
  private @Nullable String outputDir;

  @Override
  public void execute(String @NonNull [] args) throws Exception {
    new MainController().execute(Objects.requireNonNull(inputDir),
        Objects.requireNonNull(outputDir));
  }
}
