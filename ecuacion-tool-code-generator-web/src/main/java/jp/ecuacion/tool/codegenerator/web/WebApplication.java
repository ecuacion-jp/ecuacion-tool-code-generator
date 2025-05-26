package jp.ecuacion.tool.codegenerator.web;

import jp.ecuacion.splib.web.bean.SplibModelAttributes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer {
  public static void main(String[] args) {
    SpringApplication.run(WebApplication.class, args);
  }

  /** 既存tomcatにwarとして配置するために必要. */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(WebApplication.class);
  }
  
  @Bean
  SplibModelAttributes appCommonModelAttributes() {
    SplibModelAttributes atr = new SplibModelAttributes();
    atr.setBsBgGradient(true);
    atr.setShowsMessagesLinkedToItemsAtTheTop(true);
    atr.setShowsMessagesLinkedToItemsAtEachField(false);
    
    return atr;
  }
}
