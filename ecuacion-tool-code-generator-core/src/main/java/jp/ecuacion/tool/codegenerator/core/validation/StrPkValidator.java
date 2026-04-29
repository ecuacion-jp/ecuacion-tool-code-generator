package jp.ecuacion.tool.codegenerator.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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
