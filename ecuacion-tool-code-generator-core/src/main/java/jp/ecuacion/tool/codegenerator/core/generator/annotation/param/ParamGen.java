package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

/** Abstract base class for annotation parameter generators. */
public abstract class ParamGen {
  /** Generates and returns the parameter string for an annotation. */
  public abstract String generateString();
}
