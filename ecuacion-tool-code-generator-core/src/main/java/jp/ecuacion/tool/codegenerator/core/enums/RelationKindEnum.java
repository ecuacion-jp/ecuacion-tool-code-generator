package jp.ecuacion.tool.codegenerator.core.enums;

public enum RelationKindEnum {
  
  ONE_TO_ONE("@OneToOne"), MANY_TO_ONE("@ManyToOne"), ONE_TO_MANY("@OneToMany");
  
  private String name;
  
  private RelationKindEnum(String name) {
    this.name = name;
  }
  
  public static RelationKindEnum getEnumFromName(String name) {
    for (RelationKindEnum anEnum : RelationKindEnum.values()) {
      if (anEnum.getName().equals(name)) {
        return anEnum;
      }
    }
    
    return null;
  }
  
  public String getName() {
    return name;
  }
  
  public RelationKindEnum getInverse() {
    if (this == ONE_TO_ONE) {
      return ONE_TO_ONE;

    } else if (this == MANY_TO_ONE) {
      return ONE_TO_MANY;

    } else {
      // DB定義でOneToManyを指定することはないので実質これを使うことはないと思われるが念の為実装。
      return MANY_TO_ONE;
    }
  }

}
