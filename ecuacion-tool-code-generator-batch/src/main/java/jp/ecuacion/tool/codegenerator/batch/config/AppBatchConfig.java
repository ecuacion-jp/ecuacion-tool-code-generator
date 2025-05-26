package jp.ecuacion.tool.codegenerator.batch.config;

import jp.ecuacion.splib.batch.config.SplibAppParentBatchConfig;
import jp.ecuacion.tool.codegenerator.batch.tasklet.BatchStarterTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan(
    basePackages = "jp.ecuacion.splib.batch.config" + ",jp.ecuacion.util.codegenerator.core.config")
public class AppBatchConfig extends SplibAppParentBatchConfig {

  @Bean
  Job job(JobRepository jr, PlatformTransactionManager tm, Step step) {
    return preparedJobBuilder("myJob", jr).start(step).build();
  }

  @Bean
  Step step(JobRepository jr, PlatformTransactionManager tm, BatchStarterTasklet tasklet) {
    return preparedStepBuilder("sampleStep", jr, tm, tasklet).build();
  }
}
