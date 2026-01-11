package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;

public abstract class ValidatorGen extends FieldSingleAnnotationGen {

  protected DataTypeInfo dtInfo;
  protected String id;

  public abstract boolean isJakartaEeStandardValidator();

  /** ひとつのvalidatorを作成する場合に使用。 */
  public ValidatorGen(String annotationName, DataTypeInfo dtInfo) {
    super(getAnnotationName(annotationName), null);
    this.dtInfo = dtInfo;
  }

  public static String getAnnotationName(String annotationName) {
    return annotationName.replace("Field", "");
  }

  @Override
  protected void check() {
    // 型のチェックを追加で行う
    checkIfKataAvailable();
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE};
  }

  /** エラーメッセージの内容を変更するためにoverride。 */
  @Override
  protected void checkIfElementTypeAvailable(ElementType elementType) {
    if (!Arrays.asList(getAvailableElmentTypes()).contains(elementType)) {
      throw new RuntimeException(
          new BizLogicAppException("MSG_ERR_VALIDATOR_ELEMENT_TYPE_NOT_ALLOWED", info.systemName,
              this.getClass().getSimpleName(), elementType.toString()));
    }
  }

  /** 今回の型がgetAllowedKataに含まれているかを確認。 */
  private void checkIfKataAvailable() {
    List<DataTypeKataEnum> kataList = Arrays.asList(getAvailableKatas());
    if (!kataList.contains(dtInfo.getKata())) {
      throw new EclibRuntimeException("The specified Kata not allowed. (system name: "
          + info.systemName + ", annotation name: " + this.annotationName + ", dataType name: "
          + dtInfo.getDataTypeName() + ", dataType: " + dtInfo.getKata().toString() + ")");
    }
  }

  protected abstract void getParamGenWithoutFieldId(ParamListGen plistGen);

  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    if (!info.sysCmnRootInfo.isFrameworkKindSpring() && id != null) {
      plistGen.add(new ParamGenWithSingleValue("fieldId", id, DataTypeKataEnum.STRING));
    }

    getParamGenWithoutFieldId(plistGen);

    return plistGen;
  }
}
