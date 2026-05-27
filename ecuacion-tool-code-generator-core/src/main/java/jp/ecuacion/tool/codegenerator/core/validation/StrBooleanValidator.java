package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a string value represents a boolean in the code-generator convention: null, empty,
 * or {@code ○}.
 */
public class StrBooleanValidator implements ConstraintValidator<StrBoolean, String> {

  @Override
  public void initialize(@SuppressWarnings("null") StrBoolean constraintAnnotation) {}

  @SuppressWarnings("null")
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // null, ""はfalse, "○"はtrueを表す
    return (value == null || value.equals("") || value.equals("○"));
  }

}
