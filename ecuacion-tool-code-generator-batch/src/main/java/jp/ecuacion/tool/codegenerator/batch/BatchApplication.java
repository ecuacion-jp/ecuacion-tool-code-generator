package jp.ecuacion.tool.codegenerator.batch;

import jp.ecuacion.splib.batch.SplibBatchApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchApplication extends SplibBatchApplication {

  public static void main(String[] args) {
    SplibBatchApplication.main(BatchApplication.class, args);
  }
}
