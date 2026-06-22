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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.dto.CodeGenContext;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractTableGen;
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
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.TableListPropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.ValidationMessagesPatternDescriptionsGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.PerTableBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.SystemCommonBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.util.JpaFilterUtilGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;

/** Orchestrates all code-generation steps for a single system. */
public class GenerationBlf {

  private CodeGenContext info;

  /** Constructs this BLF with the given {@link CodeGenContext}. */
  public GenerationBlf(CodeGenContext info) {
    this.info = info;
  }

  /**
   * Determines which generation patterns are needed and delegates to {@link #controlGenerators()}
   * for each.
   */
  public void execute() throws Exception {
    // A single system may require multiple generation patterns, so patterns are stored in an array
    // and executed in a loop
    List<GeneratePtnEnum> arr = new ArrayList<>();

    if (shouldMakeNoGroupQuery(info)) {
      if (shouldMakeNoGroupQueryForDaoOnly(info)) {
        arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NORMAL);
        arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NO_GROUP_QUERY);

      } else {
        // Generate with no-group-query pattern
        arr.add(GeneratePtnEnum.NORMAL);
        arr.add(GeneratePtnEnum.NO_GROUP_QUERY);
      }

    } else {
      arr.add(GeneratePtnEnum.NORMAL);
    }

    // Normally one system produces one pattern;
    // when multiple are needed, generate separately for each
    for (GeneratePtnEnum anEnum : arr) {
      info.setGenPtn(anEnum);
      controlGenerators();
    }
  }

  @SuppressWarnings("unused")
  private boolean shouldMakeNoGroupQuery(CodeGenContext info) {
    if (info.getGroupRootInfo() == null) {
      return false;
    }

    return info.getGroupRootInfo().getNeedsUngroupedSource();
  }

  @SuppressWarnings("unused")
  private boolean shouldMakeNoGroupQueryForDaoOnly(CodeGenContext info) {
    if (info.getGroupRootInfo() == null) {
      return false;
    }

    return info.getGroupRootInfo().getDevidesDaoIntoOtherProject();
  }

  /**
   * Invokes the appropriate generators for each data kind defined in the current generation
   * pattern.
   */
  public void controlGenerators() throws Exception {
    Logger.log(this, "SINGLE_BORDER");
    Logger.log(this, "GEN_FOR_SYSTEM", info.getSystemName(), info.getGenPtn().getDisplayName());

    // // Pass allDtMap to generator (intentionally static)
    // AbstractTableOrClassRelatedGen.setAllDtMap(allDtMap);

    // When building a no-group query and generating only the DAO in a separate project, files
    // stored
    // in common are generated twice. For classes this is fine (just a performance issue), but
    // properties files are overwritten, resulting in duplicate keys.
    // To avoid this, delete src/main/resources/*.properties at this point
    String dirPath = new PropertiesFileGen().getResourcesPath();
    if (new File(dirPath).listFiles() != null) {
      for (File file : new File(dirPath).listFiles()) {
        if (file.getName().endsWith(".properties")) {
          file.delete();
        }
      }
    }

    // Generate dict and abstract
    // dict is created once per system, but not needed when the only xml files are
    // enumInfo, dataTypeInfo, and systemCommonInfo.
    // First determine whether dict creation is needed.
    // If needed, pass the xmlMap to the generator, which processes it file by file.
    Logger.log(this, "GEN_DICT_AND_MORE");
    boolean isNeeded = false;
    for (DataKindEnum dataKind : info.getRootInfoMap().keySet()) {
      if (dataKind != DataKindEnum.ENUM && dataKind != DataKindEnum.DATA_TYPE
          && dataKind != DataKindEnum.SYSTEM_COMMON) {
        isNeeded = true;
        break;
      }
    }

    if (isNeeded) {
      List<AbstractGen> arrGen = new ArrayList<AbstractGen>();
      arrGen.add(new ConstantGen());
      if (info.getSysCmnRootInfo().isFrameworkKindSpring()) {
        arrGen.add(new AdviceGen());
        arrGen.add(new ConfigGen());
      }
      arrGen.add(new SystemCommonBaseRecordGen());
      arrGen.add(new SystemCommonGen());
      arrGen.add(new BlGen());
      arrGen.add(new ValidationMessagesPatternDescriptionsGen());

      for (AbstractGen gen : arrGen) {
        gen.generate();
      }
    }

    for (DataKindEnum dataKind : info.getRootInfoMap().keySet()) {

      if (dataKind == DataKindEnum.ENUM) {
        Logger.log(this, "GEN_ENUM");
        new EnumGen().generate();

      } else if (dataKind == DataKindEnum.DATA_TYPE) {
        Logger.log(this, "GEN_DT");
        // Iterate over multiple dataTypes in a single file row by row.
        // The generator class differs per dataType kind, so create dynamically
        for (DataTypeInfo dtInfo : info.getDataTypeRootInfo().dataTypeList) {
          DataTypeGen gen = new DataTypeGen(dtInfo);;
          gen.generate();
          gen.generateConverter(false);
        }

      } else if (dataKind == DataKindEnum.DB) {
        Logger.log(this, "GEN_DB");
        List<AbstractTableGen> genArr = new ArrayList<AbstractTableGen>();
        genArr.add(new PerTableBaseRecordGen(DataKindEnum.DB));
        genArr.add(new EntityBodyGen(DataKindEnum.DB, false));

        genArr.add(new DaoGen(DataKindEnum.DB));
        genArr.add(new SqlPropertiesGen());

        for (AbstractTableGen gen : genArr) {
          gen.generate();
        }

        new JpaFilterUtilGen().generate();

      } else if (dataKind == DataKindEnum.SYSTEM_COMMON) {
        Logger.log(this, "GEN_PROP_FILE");
        // Generate miscellaneous files
        new MessagesBasePropertiesGen().generate();

      } else if (dataKind == DataKindEnum.TABLE_LIST) {
        Logger.log(this, "GEN_TABLE_LIST");
        new TableListPropertiesGen().generate();
      }
    }
  }
}
