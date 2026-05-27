package jp.ecuacion.tool.codegenerator.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Configures component scanning for this web application. */
@Configuration
@ComponentScan(basePackages = "jp.ecuacion.splib.web.config"
    + ",jp.ecuacion.util.codegenerator.core.config"
    )
public class AppConfig {
  
}
