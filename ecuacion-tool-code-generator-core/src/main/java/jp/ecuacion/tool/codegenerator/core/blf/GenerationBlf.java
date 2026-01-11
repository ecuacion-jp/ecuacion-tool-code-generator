package jp.ecuacion.tool.codegenerator.core.blf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.enums.GeneratePtnEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import jp.ecuacion.tool.codegenerator.core.generator.advice.AdviceGen;
import jp.ecuacion.tool.codegenerator.core.generator.bl.BlGen;
import jp.ecuacion.tool.codegenerator.core.generator.config.ConfigGen;
import jp.ecuacion.tool.codegenerator.core.generator.constant.ConstantGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.AbstractDaoRelatedGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.DaoGen;
import jp.ecuacion.tool.codegenerator.core.generator.dao.SqlPropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.datatype.DataTypeGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.EntityBodyGen;
import jp.ecuacion.tool.codegenerator.core.generator.entity.SystemCommonEntityGen;
import jp.ecuacion.tool.codegenerator.core.generator.enums.EnumGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.ValidationMessagesPatternDescriptionsGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.PerTableBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.record.SystemCommonBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.systemcommon.Miscellaneous;
import jp.ecuacion.tool.codegenerator.core.generator.util.UtilGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;

public class GenerationBlf {
  
  private Info info;

  public GenerationBlf(Info info) {
    this.info = MainController.tlInfo.get();
  }
  
  public void execute() throws Exception {
    // 1システムについても複数パターンの生成が必要な場合があるので、パターンを配列で持ち、それをループで実行する形をとる
    List<GeneratePtnEnum> arr = new ArrayList<>();

    if (shouldMakeNoGroupQuery(info)) {
      if (shouldMakeNoGroupQueryForDaoOnly(info)) {
        arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NORMAL);
        arr.add(GeneratePtnEnum.DAO_ONLY_GROUP_NO_GROUP_QUERY);

      } else {
        // グループ指定なしqueryパターンで生成
        arr.add(GeneratePtnEnum.NORMAL);
        arr.add(GeneratePtnEnum.NO_GROUP_QUERY);
      }

    } else {
      arr.add(GeneratePtnEnum.NORMAL);
    }

    // 通常は1システム1パターンだが、複数になる場合は複数に分けて生成
    for (GeneratePtnEnum anEnum : arr) {
      info.setGenPtn(anEnum);
      controlGenerators();
    }
  }

  private boolean shouldMakeNoGroupQuery(Info info) {
    if (info.groupRootInfo == null) {
      return false;
    }

    return info.groupRootInfo.getNeedsUngroupedSource();
  }

  private boolean shouldMakeNoGroupQueryForDaoOnly(Info info) {
    if (info.groupRootInfo == null) {
      return false;
    }

    return info.groupRootInfo.getDevidesDaoIntoOtherProject();
  }
  
  // xmlファイルの種類ごとに必要なファイルを作成
  public void controlGenerators() throws Exception {
    Logger.log(this, "SINGLE_BORDER");
    Logger.log(this, "GEN_FOR_SYSTEM", info.systemName, info.getGenPtn().getDisplayName());

    // // generatorにallDtMapを渡す(あえてstatic）
    // AbstractTableOrClassRelatedGen.setAllDtMap(allDtMap);

    // グループ指定なしを作る場合でかつDaoのみを分けて作成する場合、commonに格納されるファイルは2回作成される。
    // クラスについては再作成されるので（パフォーマンスが悪い以外は）困らないが、propertiesファイルは上書き作成のため、同じキーの項目が2つずつできることになる。
    // それだと具合が悪いので、このタイミングでsrc/base/resources/*.propertiesを削除しておく
    String dirPath = new PropertiesFileGen().getResourcesPath();
    if (new File(dirPath).listFiles() != null) {
      for (File file : new File(dirPath).listFiles()) {
        if (file.getName().endsWith(".properties")) {
          file.delete();
        }
      }
    }

    // ★★dict と abstract 作成
    // dictは１システムで１つのみ作成。ただし、xmlファイルがenumInfo, dataTypeInfo, systemCommonInfoのみの場合は不要。
    // まずはdict作成が必要かどうかの判断をする。
    // 必要であれば、xmlMapごとgeneratorに渡して、generator側でファイルごとに処理。
    Logger.log(this, "GEN_DICT_AND_MORE");
    boolean isNeeded = false;
    for (DataKindEnum dataKind : info.rootInfoMap.keySet()) {
      if (dataKind != DataKindEnum.ENUM && dataKind != DataKindEnum.DATA_TYPE
          && dataKind != DataKindEnum.SYSTEM_COMMON) {
        isNeeded = true;
        break;
      }
    }

    if (isNeeded) {
      List<AbstractGen> arrGen = new ArrayList<AbstractGen>();
      arrGen.add(new ConstantGen());
      if (info.sysCmnRootInfo.isFrameworkKindSpring()) {
        arrGen.add(new AdviceGen());
        arrGen.add(new ConfigGen());
      }
      arrGen.add(new SystemCommonBaseRecordGen());
      arrGen.add(new SystemCommonEntityGen());
      arrGen.add(new BlGen());
      arrGen.add(new ValidationMessagesPatternDescriptionsGen());

      for (AbstractGen gen : arrGen) {
        gen.generate();
      }
    }

    for (DataKindEnum dataKind : info.rootInfoMap.keySet()) {

      if (dataKind == DataKindEnum.ENUM) {
        Logger.log(this, "GEN_ENUM");
        new EnumGen().generate();

      } else if (dataKind == DataKindEnum.DATA_TYPE) {
        Logger.log(this, "GEN_DT");
        // 一つのファイルの中の複数のdataTypeに対して、行ごとに回して作成。
        // dataTypeの型ごとにgeneratorクラスが異なるため、動的に生成
        for (DataTypeInfo dtInfo : info.dataTypeRootInfo.dataTypeList) {
          DataTypeGen gen = new DataTypeGen(dtInfo);;
          gen.generate();
          gen.generateConverter(false);
        }

      } else if (dataKind == DataKindEnum.DB) {
        Logger.log(this, "GEN_DB");
        List<AbstractDaoRelatedGen> genArr =
            new ArrayList<AbstractDaoRelatedGen>();
        genArr.add(new PerTableBaseRecordGen(DataKindEnum.DB));
        genArr.add(new EntityBodyGen(DataKindEnum.DB, false));

        genArr.add(new DaoGen(DataKindEnum.DB));
        genArr.add(new SqlPropertiesGen());

        for (AbstractDaoRelatedGen gen : genArr) {
          gen.generate();
        }

        new UtilGen().generate();

      } else if (dataKind == DataKindEnum.SYSTEM_COMMON) {
        Logger.log(this, "GEN_PROP_FILE");
        // 雑多なファイルを生成
        new Miscellaneous().generate();
      }
    }
  }
}
