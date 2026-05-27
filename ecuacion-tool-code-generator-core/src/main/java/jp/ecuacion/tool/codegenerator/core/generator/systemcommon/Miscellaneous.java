package jp.ecuacion.tool.codegenerator.core.generator.systemcommon;

import java.io.IOException;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;

/** Generates miscellaneous system-common files such as the messages base properties file. */
public class Miscellaneous extends AbstractGen {

  /** Constructs an instance for the system-common data kind. */
  public Miscellaneous() {
    super(DataKindEnum.SYSTEM_COMMON);
  }

  @Override
  public void generate() throws IOException, InterruptedException {

    // // PropertiesFileUtilが使用するために必要な設定ファイルを生成
    // generatePropertiesFileUtilProperties(info.getSysCmnRootInfo());

    // code-generatorで生成したソースの中にあるBizLogicAppExceptionに設定するメッセージファイルを生成
    generateMessagesBaseProperties(info.getSysCmnRootInfo());
  }

  /**
   * Generates the messages_project_base properties files for each configured language
   * by copying the template from the classpath resources directory.
   */
  private void generateMessagesBaseProperties(SystemCommonRootInfo sysCmnRootInfo)
      throws IOException, InterruptedException {
    // 言語のリストを取得
    String[] langs =
        new String[] {"", sysCmnRootInfo.getDefaultLang(), sysCmnRootInfo.getSupportLang1(),
            sysCmnRootInfo.getSupportLang2(), sysCmnRootInfo.getSupportLang3()};

    PropertiesFileGen pfGen = new PropertiesFileGen();

    // src/main/resourcesのmessages_project_base_xx.propertiesをコピーして作成
    for (String lang : langs) {
      if (lang == null) {
        continue;
      }

      pfGen.copyFileToResourceDir(info.getSystemName(),
          "src/main/resources", "messages_project", "base", lang, "messages", "base", lang);
    }
  }

}
