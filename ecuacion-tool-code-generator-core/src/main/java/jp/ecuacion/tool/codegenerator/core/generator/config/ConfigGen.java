/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.core.generator.config;

import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;

/** Generates the {@code BaseConfig} Spring configuration class for the target project. */
public class ConfigGen extends AbstractGen {

  /** Constructs an instance with no specific table target. */
  public ConfigGen() {
    super(null);
  }

  @Override
  public void generate() {
    createSource();

    outputFile(sb, getFilePath("config"), "BaseConfig.java");
  }

  /**
   * Assembles the source code content of {@code BaseConfig.java}, including annotations and
   * the date-time provider bean.
   */
  public void createSource() {
    sb.append("package " + rootBasePackage + ".base.config;" + RT2);

    ImportBlock importMgr = new ImportBlock();
    importMgr.add("org.springframework.context.annotation.ComponentScan",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.persistence.autoconfigure.EntityScan", "java.time.OffsetDateTime",
        "java.time.temporal.TemporalAccessor", "java.util.Optional",
        "org.springframework.context.annotation.Bean",
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
