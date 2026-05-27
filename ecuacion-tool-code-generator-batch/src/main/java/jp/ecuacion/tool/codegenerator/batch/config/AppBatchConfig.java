package jp.ecuacion.tool.codegenerator.batch.config;

import jp.ecuacion.splib.batch.config.SplibAppParentBatchConfig;
import jp.ecuacion.splib.batch.exceptionhandler.SplibExceptionHandler;
import jp.ecuacion.splib.batch.listener.SplibJobExecutionListener;
import jp.ecuacion.splib.batch.listener.SplibStepExecutionListener;
import jp.ecuacion.tool.codegenerator.batch.tasklet.BatchStarterTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** Configures Spring Batch job and step beans for the code generator batch module. */
@Configuration
@ComponentScan(
    basePackages = "jp.ecuacion.splib.batch.config" + ",jp.ecuacion.util.codegenerator.core.config")
public class AppBatchConfig extends SplibAppParentBatchConfig {

  /**
   * Provides a config for batch.
   */
  protected AppBatchConfig(SplibJobExecutionListener jobExecutionListener,
      SplibStepExecutionListener stepExecutionListener, SplibExceptionHandler exceptionHandler) {
    super(jobExecutionListener, stepExecutionListener, exceptionHandler);
  }

  @Bean
  Job job(JobRepository jr, PlatformTransactionManager tm, Step step) {
    return preparedJobBuilder("myJob", jr).start(step).build();
  }

  @Bean
  Step step(JobRepository jr, PlatformTransactionManager tm, BatchStarterTasklet tasklet) {
    return java.util.Objects.requireNonNull(preparedStepBuilder("sampleStep", jr, tm, tasklet),
        "preparedStepBuilder must return a builder when at least one tasklet is provided").build();
  }
}
