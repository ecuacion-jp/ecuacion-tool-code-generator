package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a string value represents a primary-key kind in the code-generator convention:
 * null, empty, {@code S}, or {@code U}.
 */
public class StrPkValidator implements ConstraintValidator<StrPk, String> {

  @SuppressWarnings("null")
  @Override
  public void initialize(StrPk constraintAnnotation) {}

  @SuppressWarnings("null")
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return (value == null || value.equals("") || value.equals("S")
        || value.equals("U"));
  }

}
