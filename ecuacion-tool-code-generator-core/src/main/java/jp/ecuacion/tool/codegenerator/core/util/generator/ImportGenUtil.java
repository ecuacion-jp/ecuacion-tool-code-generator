package jp.ecuacion.tool.codegenerator.core.util.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import jp.ecuacion.tool.codegenerator.core.generator.ToolForCodeGen;

public class ImportGenUtil extends ToolForCodeGen {

  private TreeSet<String> importSet = new TreeSet<>();

  public void add(String... strings) {
    for (String str : strings) {
      importSet.add(str);
    }
  }

  public void removeIfContains(String string) {
    if (importSet.contains(string)) {
      importSet.remove(string);
    }
  }
  
  public String outputStr() {
    StringBuilder sb = new StringBuilder();
    
    // ひとつがa.b.cでもうひとつがa.b.*の場合にa.b.cを削除
    List<String> asteriskList = new ArrayList<>();
    List<String> noAsteriskList = new ArrayList<>();
    for (String str : importSet) {
      if (str.substring(str.lastIndexOf(".") + 1).equals("*")) {
        asteriskList.add(str);

      } else {
        noAsteriskList.add(str);
      }
    }

    for (String asteriskPkg : asteriskList) {
      String pkg = asteriskPkg.substring(0, asteriskPkg.lastIndexOf("."));
      
      for (String str : noAsteriskList) {
        if (str.substring(0, str.lastIndexOf(".")).equals(pkg)) {
          importSet.remove(str);
        }
      }
    }
    
    for (String str : importSet) {
      sb.append("import " + str + ";" + RT);
    }

    return sb.toString();
  }
}
