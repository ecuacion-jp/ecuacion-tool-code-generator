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

import org.jspecify.annotations.Nullable;

/** Represents the JPA relation annotation kind between entity columns. */
public enum RelationKindEnum {

  ONE_TO_ONE("@OneToOne"), MANY_TO_ONE("@ManyToOne"), ONE_TO_MANY("@OneToMany");

  private String name;

  private RelationKindEnum(String name) {
    this.name = name;
  }

  /** Returns the enum constant whose annotation name matches, or {@code null} if none does. */
  public static @Nullable RelationKindEnum getEnumFromName(String name) {
    for (RelationKindEnum anEnum : RelationKindEnum.values()) {
      if (anEnum.getName().equals(name)) {
        return anEnum;
      }
    }

    return null;
  }
  
  public String getName() {
    return name;
  }
  
  /** Returns the inverse relation kind for bidirectional mappings. */
  public RelationKindEnum getInverse() {
    if (this == ONE_TO_ONE) {
      return ONE_TO_ONE;

    } else if (this == MANY_TO_ONE) {
      return ONE_TO_MANY;

    } else {
      // OneToMany is not specified in DB definitions, so this branch is practically unused,
      // but implemented as a safeguard.
      return MANY_TO_ONE;
    }
  }

}
