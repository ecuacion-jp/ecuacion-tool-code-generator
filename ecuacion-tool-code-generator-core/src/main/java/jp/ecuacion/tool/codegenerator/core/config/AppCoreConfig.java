package jp.ecuacion.tool.codegenerator.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan("jp.ecuacion.splib.jpa.config"
    )
@PropertySources({
  @PropertySource(value = "classpath:application_core.properties"),
  @PropertySource(value = "classpath:application_core-profile.properties")
})
public class AppCoreConfig {

}
