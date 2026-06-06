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
package jp.ecuacion.tool.codegenerator.core.generator.record;

import java.util.Arrays;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;

/**
 * Generates SystemCommonBaseRecord.
 */
public class SystemCommonBaseRecordGen extends AbstractBaseRecordGen {

  /** Constructs an instance that targets the common DB column definition. */
  public SystemCommonBaseRecordGen() {
    super(DataKindEnum.DB_COMMON);
  }

  @Override
  public void generate() {
    internalGenerate(
        Arrays.asList(new DbOrClassTableInfo[] {getInfo().getCommonTableInfo()}), true);
  }

  /**
   * Generates the class header that extends {@code SplibRecord} for the system-common base record.
   */
  @Override
  public void generateHeader(DbOrClassTableInfo tableInfo) {

    generateHeaderCommon(tableInfo, "jp.ecuacion.splib.core.record.SplibRecord",
        rootBasePackage + ".base.entity.SystemCommon", "jp.ecuacion.splib.core.container.*");

    sb.append("public abstract class SystemCommonBaseRecord extends SplibRecord {" + RT2);
  }

  @Override
  protected void generateMethods(DbOrClassTableInfo ti) {
  }
}
