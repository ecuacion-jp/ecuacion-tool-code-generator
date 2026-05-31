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
package jp.ecuacion.tool.codegenerator.core.generator.annotation.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.util.StringUtil;

/**
 * A ParamGen that aggregates multiple ParamGen instances into a comma-separated parameter string.
 *
 * <p>Each individual parameter is produced by a ParamGen, and multiple parameters are joined
  * to form the complete annotation parameter list. Despite its name, ParamListGen itself extends
  * ParamGen.
 * </p>
 */
public class ParamListGen extends ParamGen {
  private List<ParamGen> paramList = new ArrayList<>();

  /** Constructs an empty ParamListGen with no initial parameters. */
  public ParamListGen() {}

  /** Constructs a ParamListGen with the given initial parameters. */
  public ParamListGen(ParamGen... params) {
    paramList = Arrays.asList(params);
  }

  @Override
  public String generateString() {
    ArrayList<String> arr = new ArrayList<>();
    paramList.forEach(param -> {
      arr.add(param.generateString());
    });
    return StringUtil.getCsvWithSpace((String[]) arr.toArray(new String[0]));
  }

  /** Adds a parameter generator to the list. */
  public void add(ParamGen param) {
    paramList.add(param);
  }
}
