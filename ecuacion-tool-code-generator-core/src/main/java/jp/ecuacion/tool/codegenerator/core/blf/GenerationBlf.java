package jp.ecuacion.tool.codegenerator.core.blf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.controller.MainController;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen;
import jp.ecuacion.tool.codegenerator.core.generator.Info;
import jp.ecuacion.tool.codegenerator.core.generator.advice.AdviceGen;
import jp.ecuacion.tool.codegenerator.core.generator.config.ConfigGen;
import jp.ecuacion.tool.codegenerator.core.generator.constant.ConstantGen;
import jp.ecuacion.tool.codegenerator.core.generator.datatype.DataTypeGen;
import jp.ecuacion.tool.codegenerator.core.generator.enums.EnumGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.PropertiesFileGen;
import jp.ecuacion.tool.codegenerator.core.generator.propertiesfile.ValidationMessagesPatternDescriptionsGen;
import jp.ecuacion.tool.codegenerator.core.generator.systemcommon.Miscellaneous;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.AbstractTableOrClassRelatedGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.dao.DaoGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.dao.SqlPropertiesGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.EntityBodyGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity.SystemCommonEntityGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.record.BaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.record.SystemCommonBaseRecordGen;
import jp.ecuacion.tool.codegenerator.core.generator.util.UtilGen;
import jp.ecuacion.tool.codegenerator.core.logger.Logger;

public class GenerationBlf {
  
  private Info info;

  public GenerationBlf(Info info, String outputDir) {
    this.info = MainController.tlInfo.get();
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

        // } else if (name.endsWith(Constants.XML_POST_FIX_DT_R)) {
        // Logger.log(this, "GEN_DT_R");
        //
        // //
        // 参照している親のdataTypeに対するconverterは、親のところに置いておいても機能してくれない（自動認識してくれない）ので、個別システム側のbaseに作る必要がある
        // if (info.dataTypeRefRootInfo != null) {
        // for (DataTypeRefInfo dtRef : info.dataTypeRefRootInfo.dataTypeRefList) {
        // // 参照しているdataTypeのinfoを取得し、それをもとにconveterを生成
        // String dtName = dtRef.getDataType();
        // DataTypeInfo dtInfo = allDtMap.get(dtRef.getSystemName()).get(dtName);
        // DataTypeGen gen = createDataTypeGen(dtInfo);
        // gen.generateConverter(true);
        // }
        // }


      } else if (dataKind == DataKindEnum.DB) {
        Logger.log(this, "GEN_DB");
        List<AbstractTableOrClassRelatedGen> genArr =
            new ArrayList<AbstractTableOrClassRelatedGen>();
        genArr.add(new BaseRecordGen(DataKindEnum.DB));
        genArr.add(new EntityBodyGen(DataKindEnum.DB, false));
        // genArr.add(new EntityPkGen(Constants.XML_POST_FIX_DB, true));

        genArr.add(new DaoGen(DataKindEnum.DB));
        genArr.add(new SqlPropertiesGen());

        for (AbstractTableOrClassRelatedGen gen : genArr) {
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
