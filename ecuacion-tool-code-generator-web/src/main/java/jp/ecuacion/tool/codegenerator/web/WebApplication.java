package jp.ecuacion.tool.codegenerator.web;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** Starts the code generator web application. */
@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer {
  /** Starts the Spring Boot web application. */
  public static void main(String[] args) {
    SpringApplication.run(WebApplication.class, args);
  }

  /** 既存tomcatにwarとして配置するために必要. */
  @SuppressWarnings({"null", "NullAway"})
  @Override
  protected SpringApplicationBuilder configure(@Nullable SpringApplicationBuilder application) {
    return application.sources(WebApplication.class);
  }
}
