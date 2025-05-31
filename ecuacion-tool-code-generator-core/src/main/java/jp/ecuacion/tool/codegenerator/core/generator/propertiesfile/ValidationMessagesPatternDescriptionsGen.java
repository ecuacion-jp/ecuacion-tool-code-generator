package jp.ecuacion.tool.codegenerator.core.generator.propertiesfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.Info;

public class ValidationMessagesPatternDescriptionsGen extends AbstractGen {

  public ValidationMessagesPatternDescriptionsGen() {
    super(null);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    PropertiesFileGen gen = new PropertiesFileGen();
    Info info = MainController.tlInfo.get();

    List<String> langList = new ArrayList<>();
    langList.add("");
    langList.addAll(info.sysCmnRootInfo.getSupportedLangArr());

    for (String lang : langList) {
      Map<String, String> propMap = new LinkedHashMap<>();
      // 禁則文字チェックに対するメッセージ
      propMap.put("prohibitedChars", info.sysCmnRootInfo.getProhibitedCharsDesc(lang));

      // dataTypeに対するメッセージ
      for (DataTypeInfo dtInfo : info.dataTypeRootInfo.dataTypeList) {
        String desc = dtInfo.getStringRegExDesc(info.sysCmnRootInfo.getSupportedLangArr(), lang);
        if (desc != null) {
          propMap.put(StringUtil.getLowerCamelFromSnake(
              dtInfo.getDataTypeName().substring(3)), desc);
        }
      }

      gen.writeMapToPropFile(propMap, "ValidationMessagesPatternDescriptions", lang);
    }
  }
}
