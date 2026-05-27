package jp.ecuacion.tool.codegenerator.web.exceptionhandler;

import jakarta.annotation.Nonnull;
import jp.ecuacion.lib.core.util.MailUtil;
import jp.ecuacion.splib.core.exceptionhandler.SplibExceptionHandlerAction;
import org.springframework.stereotype.Component;

/** Provides an exception handler action for the web module, sending an error mail on failure. */
@Component
public class ActionOnThrowable implements SplibExceptionHandlerAction {

  @Override
  public void execute(@Nonnull Throwable th) {
    MailUtil.sendErrorMail(th);
  }
}
