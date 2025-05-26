package jp.ecuacion.tool.codegenerator.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("jp.ecuacion.splib.jpa.config"
    + ",jp.ecuacion.app.accountbook.core.bl"
    + ",jp.ecuacion.app.accountbook.core.advice"
    )
public class AppCoreConfig {

}
