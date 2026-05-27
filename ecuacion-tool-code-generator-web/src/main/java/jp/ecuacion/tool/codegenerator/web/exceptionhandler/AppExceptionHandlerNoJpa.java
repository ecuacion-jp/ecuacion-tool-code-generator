package jp.ecuacion.tool.codegenerator.web.exceptionhandler;

import jakarta.servlet.http.HttpServletRequest;
import jp.ecuacion.splib.core.exceptionhandler.SplibExceptionHandlerAction;
import jp.ecuacion.splib.web.exceptionhandler.SplibExceptionHandler;
import jp.ecuacion.splib.web.util.SplibLoginStateUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;

/** Handles exceptions for the web application without JPA support. */
@ControllerAdvice
public class AppExceptionHandlerNoJpa extends SplibExceptionHandler {

  protected AppExceptionHandlerNoJpa(HttpServletRequest request,
      @Nullable SplibExceptionHandlerAction actionOnThrowable,
      SplibLoginStateUtil loginStateUtil) {
    super(request, actionOnThrowable, loginStateUtil);
  }
}
