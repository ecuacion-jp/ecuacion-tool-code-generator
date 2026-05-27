package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * Validates that a string field contains only a valid boolean marker: null, empty, or the circle
  * character {@code ○}.
 */
@Constraint(validatedBy = {StrBooleanValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StrBoolean {
  /** Returns the constraint violation message key. */
  String message() default "test.com.AssertOdd";

  /** Returns the validation groups this constraint belongs to. */
  Class<?>[] groups() default {};

  /** Returns the payload associated with this constraint. */
  Class<? extends Payload>[] payload() default {};

  /** Allows multiple {@link StrBoolean} constraints on the same element. */
  @interface List {
    /** Returns the repeated {@link StrBoolean} annotations. */
    StrBoolean[] values();
  }
}
