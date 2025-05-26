package jp.ecuacion.tool.codegenerator.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "jp.ecuacion.splib.web.config"
    + ",jp.ecuacion.util.codegenerator.core.config"
    )
public class AppConfig {
  
//  public static final String SESSION_KEY_LOGIN_USER_ID = "loginUserId";
  
//  @Bean
//  public AuditorAware<Long> auditorAware(HttpServletRequest request) {
//    return new AuditorAware<Long>() {
//      @Override
//      public Optional<Long> getCurrentAuditor() {
//
//        Long accId = (Long) request.getSession().getAttribute(SESSION_KEY_LOGIN_USER_ID);
//        return Optional.of(accId);
//      }
//    };
//  }
}
