package jp.ecuacion.tool.codegenerator.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan("jp.ecuacion.splib.jpa.config"
    + ",jp.ecuacion.app.accountbook.core.bl"
    + ",jp.ecuacion.app.accountbook.core.advice"
    )
@PropertySources({
  @PropertySource(value = "classpath:application_profile.properties"),
  @PropertySource(value = "classpath:application_core.properties"),
  @PropertySource(value = "classpath:application_core_profile.properties")
})
public class AppCoreConfig {

}
