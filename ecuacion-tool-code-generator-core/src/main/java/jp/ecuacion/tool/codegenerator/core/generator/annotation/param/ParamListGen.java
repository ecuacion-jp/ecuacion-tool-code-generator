package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.util.StringUtil;

/** 
 * 複数のパラメータを付加する場合に使用。
 * 
 * <p>
 * 個々のパラメータはParamGenで作成し、それを複数引数に取ることで複数項目の文字列を作成。
 * ParamListGenと言いながら、これもParamGenの子。
 * </p>
 */
public class ParamListGen extends ParamGen {
  private List<ParamGen> paramList = new ArrayList<>();

  public ParamListGen() {}

  public ParamListGen(ParamGen... params) {
    paramList = Arrays.asList(params);
  }

  @Override
  public String generateString() {
    ArrayList<String> arr = new ArrayList<>();
    paramList.forEach(param -> {
      try {
        arr.add(param.generateString());
      } catch (BizLogicAppException ex) {
        throw new RuntimeException(ex);
      }
    });
    return StringUtil.getCsvWithSpace((String[]) arr.toArray(new String[0]));
  }

  public void add(ParamGen param) {
    paramList.add(param);
  }
}
