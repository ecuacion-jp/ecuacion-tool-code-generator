package jp.ecuacion.tool.codegenerator.batch.tasklet;

import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/** Executes the code generation process as a Spring Batch tasklet. */
@Component
public class BatchStarterTasklet implements Tasklet {

  @Override
  public RepeatStatus execute(@Nullable StepContribution contribution,
      @Nullable ChunkContext chunkContext) throws Exception {

    new MainController().execute(Constants.DIR_INFO_EXCELS_DEFAULT, "./products/");

    return RepeatStatus.FINISHED;
  }

}
