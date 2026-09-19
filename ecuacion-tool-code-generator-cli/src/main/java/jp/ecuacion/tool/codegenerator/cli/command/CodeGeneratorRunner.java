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
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Executes the code generation process. */
@Component
public class CodeGeneratorRunner implements SplibCliRunner {

  public static final String PROP_INPUT_FILE = "jp.ecuacion.tool.code-generator.input-file";
  public static final String PROP_OUTPUT_DIR = "jp.ecuacion.tool.code-generator.output-dir";

  /** Comma-separated list of Excel file paths. Required; there is no default. */
  @Value("${" + PROP_INPUT_FILE + "}")
  private @Nullable String inputFile;

  @Value("${" + PROP_OUTPUT_DIR + ":./products/}")
  private @Nullable String outputDir;

  @Override
  public void execute(String @NonNull [] args) throws Exception {
    // The CLI can process multiple Excel files in one run, so error messages need the file name
    // to tell them apart.
    new MainController().execute(Objects.requireNonNull(inputFile),
        Objects.requireNonNull(outputDir), true);
  }
}
