package jp.ecuacion.tool.codegenerator.core.generator.config;

import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.util.generator.ImportGenUtil;

public class ConfigGen extends AbstractGen {

  public ConfigGen() {
    super(null);
  }

  @Override
  public void generate() throws AppException {
    createSource();

    outputFile(sb, getFilePath("config"), "BaseConfig.java");
  }

  public void createSource() {
    sb.append("package " + rootBasePackage + ".base.config;" + RT2);

    ImportGenUtil importMgr = new ImportGenUtil();
    importMgr.add("org.springframework.context.annotation.ComponentScan",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.autoconfigure.domain.EntityScan", 
        "java.time.OffsetDateTime", "java.time.temporal.TemporalAccessor",
        "java.util.Optional", "org.springframework.context.annotation.Bean",
        "org.springframework.data.jpa.repository.config.*", "org.springframework.data.auditing.*");
    sb.append(importMgr.outputStr() + RT);

    sb.append("@Configuration" + RT);
    sb.append("@EntityScan(\"" + rootBasePackage + ".base.entity\")" + RT);
    sb.append("@EnableJpaAuditing(dateTimeProviderRef = \"dateTimeProvider\")" + RT);
    sb.append("@ComponentScan(\"jp.ecuacion.splib.jpa.config\"" + RT);
    sb.append(T2 + "+ \"," + rootBasePackage + ".base.advice\"" + RT);
    sb.append(T2 + "+ \"," + rootBasePackage + ".base.util\"" + RT);
    sb.append(T2 + ")" + RT);

    sb.append("public class BaseConfig {" + RT2);
    
    sb.append(T1 + "@Bean" + RT);
    sb.append(T1 + "DateTimeProvider dateTimeProvider() {" + RT);
    sb.append(T2 + "return new DateTimeProvider() {" + RT);
    sb.append(T3 + "@Override" + RT);
    sb.append(T3 + "public Optional<TemporalAccessor> getNow() {" + RT);
    sb.append(T4 + "OffsetDateTime time = OffsetDateTime.now();" + RT);
    sb.append(T4 + "return Optional.of(time);" + RT);
    sb.append(T3 + "}" + RT);
    sb.append(T2 + "};" + RT);
    sb.append(T1 + "}" + RT2);

    sb.append("}" + RT);
  }
}
