package jp.ecuacion.tool.codegenerator.batch.tasklet;

import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.controller.CodeGeneratorAction;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class BatchStarterTasklet implements Tasklet {

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    
    new CodeGeneratorAction().execute(Constants.DIR_INFO_EXCELS_DEFAULT, "./products/");
    
    return RepeatStatus.FINISHED;
  }

}
