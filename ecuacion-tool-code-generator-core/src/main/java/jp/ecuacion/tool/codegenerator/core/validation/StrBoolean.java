package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {StrBooleanValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StrBoolean {
  String message() default "test.com.AssertOdd";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @interface List {
    StrBoolean[] values();
  }
}
