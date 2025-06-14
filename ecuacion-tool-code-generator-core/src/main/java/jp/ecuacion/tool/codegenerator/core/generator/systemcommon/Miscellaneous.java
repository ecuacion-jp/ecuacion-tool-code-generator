package jp.ecuacion.tool.codegenerator.core.generator.systemcommon;

import java.io.IOException;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;

public class Miscellaneous extends AbstractGen {

  public Miscellaneous() {
    super(DataKindEnum.SYSTEM_COMMON);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {

    // // PropertyFileUtilが使用するために必要な設定ファイルを生成
    // generatePropertyFileUtilProperties(info.sysCmnRootInfo);

    // code-generatorで生成したソースの中にあるBizLogicAppExceptionに設定するメッセージファイルを生成
    generateMessagesBaseProperties(info.sysCmnRootInfo);
  }

  /**
   * code-generatorで生成したソースの中にあるBizLogicAppExceptionに設定するメッセージファイルを生成
   * BizLogicAppExceptionのメッセージ。ユーザが見るメッセージではなく開発者が見るもの。
   * そのため日本語があれば問題ないのだが、クライアントロケールがja以外の場合はその言語で出てしまうので、通常標準ロケールに設定される英語は用意しておく。
   * ※enのファイルに日本語のメッセージを書いてもいいんだが、気分で一応英語にしておく。。。
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

      pfGen.copyFileToResourceDir(info.systemName, "src/main/resources", "messages_project", "base",
          lang, "messages", "base", lang);
    }
  }

}
