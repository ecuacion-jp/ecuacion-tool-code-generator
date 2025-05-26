package jp.ecuacion.tool.codegenerator.batch.exceptionhandler;

import jakarta.annotation.Nonnull;
import jp.ecuacion.lib.core.util.MailUtil;
import jp.ecuacion.splib.core.exceptionhandler.SplibExceptionHandlerAction;
import org.springframework.stereotype.Component;

@Component
public class AppExceptionHandlerAction implements SplibExceptionHandlerAction {

  @Override
  public void execute(@Nonnull Throwable th) {
    MailUtil.sendErrorMail(th);
  }

}
