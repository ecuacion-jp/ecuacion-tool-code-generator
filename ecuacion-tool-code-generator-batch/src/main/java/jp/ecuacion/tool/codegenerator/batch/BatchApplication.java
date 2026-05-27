package jp.ecuacion.tool.codegenerator.batch;

import jp.ecuacion.splib.batch.SplibBatchApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the code generator batch application. */
@SpringBootApplication
public class BatchApplication extends SplibBatchApplication {

  /** Starts the Spring Boot batch application. */
  public static void main(String[] args) {
    SplibBatchApplication.main(BatchApplication.class, args);
  }
}
