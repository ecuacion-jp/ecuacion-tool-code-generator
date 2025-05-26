package jp.ecuacion.tool.codegenerator.core.enums;

public enum GeneratePtnEnum {

  NORMAL("normal", "通常"), NO_GROUP_QUERY("no-group-query", "グループ指定なし"), DAO_ONLY_GROUP_NORMAL(
      "normal",
      "DAOのみ別グループ：通常"), DAO_ONLY_GROUP_NO_GROUP_QUERY("no-group-query", "DAOのみ別グループ：グループ指定なし");

  private String dirName;
  private String dispName;

  public String getDirName() {
    return dirName;
  }

  public String getDisplayName() {
    return dispName;
  }

  private GeneratePtnEnum(String dirName, String dispName) {
    this.dirName = dirName;
    this.dispName = dispName;
  }
}
