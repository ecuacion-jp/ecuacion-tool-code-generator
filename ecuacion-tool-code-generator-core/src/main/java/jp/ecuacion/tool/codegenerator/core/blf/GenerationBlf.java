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
package jp.ecuacion.tool.codegenerator.core.blf;

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.logging.DetailLogger;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.advice.AdviceGen;
import jp.ecuacion.tool.codegenerator.core.generator.bl.BlGen;
import jp.ecuacion.tool.codegenerator.core.generator.config.ConfigGen;
import jp.ecuacion.tool.codegenerator.core.generator.constant.ConstantGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.DaoGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.SqlPropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.datatype.DataTypeGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.EntityBodyGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.SystemCommonGen;
import jp.ecuacion.tool.codegenerator.core.generator.enums.EnumGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.MessagesBasePropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.TableListPropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.ValidationMessagesPatternDescriptionsGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.PerTableBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.SystemCommonBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.util.JpaFilterUtilGen;

/** Orchestrates all code-generation steps for a single system. */
public class GenerationBlf {

  private static final DetailLogger log = new DetailLogger(GenerationBlf.class);
  private CodeGenContext info;
  private static final String IDT = "  ";

  /** Constructs this BLF with the given {@link CodeGenContext}. */
  public GenerationBlf(CodeGenContext info) {
    this.info = info;
  }

  /**
   * Delegates to {@link #controlGenerators()} for the normal generation pattern.
   */
  public void execute() throws Exception {
    controlGenerators();
  }

  /**
   * Invokes the appropriate generators for each data kind defined in the current generation
   * pattern.
   */
  public void controlGenerators() throws Exception {
    log.info(IDT + "Collectiong generators.");
    List<AbstractGen> arrGen = new ArrayList<AbstractGen>();
    arrGen.add(new ConstantGen());
    arrGen.add(new SystemCommonBaseRecordGen());
    arrGen.add(new SystemCommonGen());
    arrGen.add(new BlGen());
    arrGen.add(new ValidationMessagesPatternDescriptionsGen());

    if (info.getSysCmnRootInfo().isFrameworkKindSpring()) {
      arrGen.add(new AdviceGen());
      arrGen.add(new ConfigGen());
    }

    if (info.getRootInfoMap().containsKey(DataKindEnum.ENUM)) {
      arrGen.add(new EnumGen());
    }

    info.getDataTypeRootInfo().dataTypeList.stream().forEach(d -> arrGen.add(new DataTypeGen(d)));

    arrGen.add(new PerTableBaseRecordGen(DataKindEnum.DB));
    arrGen.add(new EntityBodyGen(DataKindEnum.DB, false));
    arrGen.add(new DaoGen(DataKindEnum.DB));
    arrGen.add(new SqlPropertiesGen());
    arrGen.add(new JpaFilterUtilGen());
    arrGen.add(new MessagesBasePropertiesGen());
    // Must run after MessagesBasePropertiesGen (SYSTEM_COMMON) since copyFileToResourceDir
    // overwrites the file; appending table list data here ensures it is never lost.
    arrGen.add(new TableListPropertiesGen());

    log.info(IDT + "Executing generators.");
    for (AbstractGen gen : arrGen) {

      log.debug(IDT + IDT + "Executing " + gen.getClass().getSimpleName() + ".");
      gen.generate();
    }
  }
}
