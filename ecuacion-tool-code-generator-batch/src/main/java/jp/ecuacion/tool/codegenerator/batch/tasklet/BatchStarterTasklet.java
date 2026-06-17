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
package jp.ecuacion.tool.codegenerator.batch.tasklet;

import java.util.Objects;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Executes the code generation process as a Spring Batch tasklet. */
@Component
public class BatchStarterTasklet implements Tasklet {

  @Value("${jp.ecuacion.tool.codegenerator.input-dir:" + Constants.DIR_INFO_EXCELS_DEFAULT + "}")
  private @Nullable String inputDir;

  @Value("${jp.ecuacion.tool.codegenerator.output-dir:./products/}")
  private @Nullable String outputDir;

  @Override
  public RepeatStatus execute(@Nullable StepContribution contribution,
      @Nullable ChunkContext chunkContext) throws Exception {

    new MainController().execute(Objects.requireNonNull(inputDir),
        Objects.requireNonNull(outputDir));

    return RepeatStatus.FINISHED;
  }

}
