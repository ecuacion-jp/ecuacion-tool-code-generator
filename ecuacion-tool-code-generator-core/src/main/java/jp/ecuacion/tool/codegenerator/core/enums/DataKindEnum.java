package jp.ecuacion.tool.codegenerator.core.enums;

import java.util.Locale;
import jp.ecuacion.lib.core.util.PropertyFileUtil;

public enum DataKindEnum {
  //@formatter:off
  SYSTEM_COMMON, DATA_TYPE, ENUM, DB, DB_COMMON, 
  MISC_REMOVED_DATA, MISC_GROUP, MISC_OPTIMISTIC_LOCK, OTHER;
  //@formatter:on

  public String getLabel() {
    return PropertyFileUtil.getEnumName(Locale.getDefault(),
        this.getClass().getSimpleName() + "." + this.toString());
  }
}
