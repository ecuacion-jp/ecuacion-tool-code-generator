package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * Validates that a string field contains only a valid primary-key marker: null, empty, {@code S}
  * (surrogate), or {@code U} (unique constraint).
 */
@Constraint(validatedBy = {StrPkValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StrPk {
  /** Returns the constraint violation message key. */
  String message() default "test.com.AssertOdd";

  /** Returns the validation groups this constraint belongs to. */
  Class<?>[] groups() default {};

  /** Returns the payload associated with this constraint. */
  Class<? extends Payload>[] payload() default {};

  /** Allows multiple {@link StrPk} constraints on the same element. */
  @interface List {
    /** Returns the repeated {@link StrPk} annotations. */
    StrPk[] values();
  }
}
