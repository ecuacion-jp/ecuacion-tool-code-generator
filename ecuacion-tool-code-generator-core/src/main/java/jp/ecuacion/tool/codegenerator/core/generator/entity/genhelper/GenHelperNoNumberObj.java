package jp.ecuacion.tool.codegenerator.core.generator.entity.genhelper;

/** Abstract generation helper for non-numeric object type columns. */
public abstract class GenHelperNoNumberObj extends GenHelperKata {

  /**
   * Returns a string setter method implementation for String parameters; returns empty string by
   * default.
   */
  protected String getStringParamSetter(String columnNameCp, String columnNameSm, String dataType) {
    return "";
  }
}
