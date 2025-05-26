package jp.ecuacion.tool.codegenerator.core.generator.constant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class ConstantGen extends AbstractGen {

  public ConstantGen() {
    super(null);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    createSource();

    outputFile(sb, getFilePath("constant"), "BaseConstants.java");

    PropertiesFileGen gen = new PropertiesFileGen();

    // propertiesファイルを作成
    Map<String, String> map = new HashMap<>();
    map.put("EXCEL_TEMPLATE_VERSION", info.sysCmnRootInfo.getTemplateVersion());
    map.put("CODE_GENERATOR_VERSION",
        ResourceBundle.getBundle("version").getString("project.version"));

    gen.writeMapToPropFile(map, "application", null);

  }

  public void createSource() {
    sb.append("package " + rootBasePackage + ".base.constant;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    // importMgr.add(Constants.STR_LIB_CORE_PKG + ".constant.ConstantsInLibCore");
    sb.append(importMgr.outputStr() + RT);

    sb.append("public abstract class BaseConstants {" + RT2);
    sb.append(T1 + "public static final String PAC_APP_HOME = \"" + rootBasePackage + "\";" + RT2);
    sb.append("}");
  }
}
