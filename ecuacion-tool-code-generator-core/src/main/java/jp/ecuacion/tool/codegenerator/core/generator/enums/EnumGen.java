package jp.ecuacion.tool.codegenerator.core.generator.enums;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.constant.EclibCoreConstants;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.dto.EnumClassInfo;
import jp.ecuacion.tool.codegenerator.core.dto.EnumValueInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class EnumGen extends AbstractGen {

  public EnumGen() {
    super(DataKindEnum.ENUM);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    List<EnumClassInfo> enumClassList = info.enumRootInfo.enumClassList;

    // Enum作成
    Logger.log(this, "GEN_ENUM_ENUMS");
    for (EnumClassInfo enumClassInfo : enumClassList) {
      sb = new StringBuilder();
      createEnum(enumClassInfo);
      outputFile(sb, getFilePath("enums"), enumClassInfo.getEnumName() + ".java");
    }

    Logger.log(this, "GEN_ENUM_ENUM_RELATED_PROP_FILES");
    PropertiesFileGen gen = new PropertiesFileGen();

    // propertiesファイルを作成。
    // default言語用のファイルを作成。default言語がen,
    // entity_namesであれば、entity_names.propertiesを、entity_names_en.propertiesと同一内容で作成
    gen.writeMapToPropFile(
        createSortedMapForPropFile(info.sysCmnRootInfo.getDefaultLang(), enumClassList),
        "enum_names", null);
    // entity_names_en.propertiesを作成
    gen.writeMapToPropFile(
        createSortedMapForPropFile(info.sysCmnRootInfo.getDefaultLang(), enumClassList),
        "enum_names", info.sysCmnRootInfo.getDefaultLang());
    // supportedLangArrに入っているものについて作成
    for (String lang : info.sysCmnRootInfo.getSupportedLangArr()) {
      gen.writeMapToPropFile(createSortedMapForPropFile(lang, enumClassList), "enum_names", lang);
    }
  }

  private void createEnum(EnumClassInfo enumClassInfo) {
    final String enumName = enumClassInfo.getEnumName();

    sb.append("package " + rootBasePackage + ".base.enums;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("java.util.Locale");
    importMgr.add(EclibCoreConstants.PKG + ".util.PropertyFileUtil");
    sb.append(importMgr.outputStr() + RT);

    sb.append("public enum " + enumName + " {" + RT2);

    boolean isFirst = true;
    for (EnumValueInfo enumValueInfo : enumClassInfo.enumList) {
      // ソースを見やすくするために変数に入れておく
      final String code = enumValueInfo.getCode();
      final String varName = enumValueInfo.getVarName();

      // 2つ目以降の場合はカンマ区切りを入れる
      if (isFirst) {
        isFirst = false;

      } else {
        sb.append("," + RT2);
      }

      sb.append(T1 + varName + "(\"" + code + "\")");
    }
    sb.append(";" + RT2);

    sb.append(T1 + "private String code;" + RT2);

    sb.append(T1 + "private " + enumName + "(String code) {" + RT);
    sb.append(T2 + "this.code = code;" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("codeを返す。", "codeがnull, 空文字の場合は、Enum生成時にチェックエラーとなるため考慮不要"));
    sb.append(T1 + "public String getCode() {" + RT);
    sb.append(T2 + "return code;" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("画面で表示するための名称を返す。", "この名称は、getはできるがそれをもとにenumを取得することはできない。",
        "localizeされた言語で返す。"));
    sb.append(T1 + "public String getDisplayName(Locale locale) {" + RT);
    sb.append(T2 + "return PropertyFileUtil.getEnumName("
        + "locale, this.getClass().getSimpleName() + \".\" + this.toString());" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append(genJavadocMethod("defaultのLocaleを使用。"));
    sb.append(T1 + "public String getDisplayName() {" + RT);
    sb.append(T2 + "return PropertyFileUtil.getEnumName("
        + "Locale.getDefault(), this.getClass().getSimpleName() + \".\" + this.toString());" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);
  }

  private Map<String, String> createSortedMapForPropFile(String lang,
      List<EnumClassInfo> enumClassList) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    for (EnumClassInfo enumClassInfo : enumClassList) {
      for (EnumValueInfo enumValueInfo : enumClassInfo.enumList) {
        String enumName = enumClassInfo.getEnumName();
        String varName = enumValueInfo.getVarName();
        String dispName = enumValueInfo.getDisplayNameMap().get(lang);
        map.put(enumName + "." + varName, dispName);
      }
    }
    return map;
  }
}
