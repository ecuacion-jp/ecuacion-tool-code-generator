package jp.ecuacion.tool.codegenerator.core.generator.util;

import java.io.IOException;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.util.StringUtil;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class UtilGen extends AbstractGen {

  public UtilGen() {
    super(DataKindEnum.DB);
  }

  @Override
  public void generate() throws AppException, IOException, InterruptedException {
    // Util作成
    Logger.log(this, "GEN_UTIL");

    sb = new StringBuilder();
    createJpaFilterUtil();
    outputFile(sb, getFilePath("util"), "JpaFilterUtil.java");
  }

  private void createJpaFilterUtil() {
    final boolean grDefined = info.groupRootInfo.isDefined();
    final MiscGroupRootInfo grInfo = info.groupRootInfo;

    sb.append("package " + rootBasePackage + ".base.util;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("jp.ecuacion.splib.jpa.util.SplibJpaFilterUtil",
        "org.springframework.stereotype.Component");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@Component" + RT);
    sb.append("public class JpaFilterUtil extends SplibJpaFilterUtil {" + RT2);

    sb.append(T1 + "public JpaFilterUtil() {" + RT);
    sb.append(T2 + "super(" + info.removedDataRootInfo.isDefined() + ", " + grDefined + ", "
        + (grDefined
            ? "\"" + StringUtil.getLowerCamelFromSnake(grInfo.getColumnName())
                + "\""
            : "null")
        + ", " + (grInfo.getCustomGroupTableName() != null) + ", "
        + (grDefined
            ? "\"groupFilter" + StringUtil
                .getUpperCamelFromSnake(grInfo.getCustomGroupTableName()) + "\""
            : "null")
        + ", "
        + (grDefined
            ? "\"" + StringUtil
                .getLowerCamelFromSnake(grInfo.getCustomGroupColumnName()) + "\""
            : "null")
        + ");" + RT);
    sb.append(T1 + "}" + RT);
    sb.append("}" + RT);
  }
}
