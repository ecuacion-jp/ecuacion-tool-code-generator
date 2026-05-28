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
package jp.ecuacion.tool.codegenerator.core.enums;

/**
 * Represents the code generation pattern, distinguishing between normal and no-group-query
 * variants.
 */
public enum GeneratePtnEnum {

  NORMAL("normal", "normal"), NO_GROUP_QUERY("no-group-query",
      "no-group-query"), DAO_ONLY_GROUP_NORMAL("normal",
          "DAO-only separate group: normal"), DAO_ONLY_GROUP_NO_GROUP_QUERY("no-group-query",
              "DAO-only separate group: no-group-query");

  private String dirName;
  private String dispName;

  public String getDirName() {
    return dirName;
  }

  public String getDisplayName() {
    return dispName;
  }

  private GeneratePtnEnum(String dirName, String dispName) {
    this.dirName = dirName;
    this.dispName = dispName;
  }
}
