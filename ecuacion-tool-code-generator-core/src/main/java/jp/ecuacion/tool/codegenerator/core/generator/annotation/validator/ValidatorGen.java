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
package jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import jp.ecuacion.lib.core.violation.BusinessViolation;
import jp.ecuacion.lib.core.violation.Violations;
import jp.ecuacion.tool.codegenerator.core.dto.DataTypeInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataTypeKataEnum;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.FieldSingleAnnotationGen;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamGenWithSingleValue;
import jp.ecuacion.tool.codegenerator.core.generator.annotation.param.ParamListGen;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for all validator annotation generators, handling parameter assembly and type
 * checking.
 */
@SuppressWarnings("NullAway.Init")
public abstract class ValidatorGen extends FieldSingleAnnotationGen {

  protected DataTypeInfo dtInfo;
  protected @Nullable String id;

  /** Returns {@code true} if this validator is a Jakarta EE standard annotation. */
  public abstract boolean isJakartaEeStandardValidator();

  /** Constructor used when creating a single validator. */
  public ValidatorGen(String annotationName, DataTypeInfo dtInfo) {
    super(getAnnotationName(annotationName), null);
    this.dtInfo = dtInfo;
  }

  /**
   * Strips the {@code "Field"} prefix from the annotation name to produce the actual annotation
   * identifier.
   */
  public static String getAnnotationName(String annotationName) {
    return annotationName.replace("Field", "");
  }

  @Override
  protected void check() {
    // Perform an additional type check
    checkIfKataAvailable();
  }

  @Override
  protected ElementType[] getAvailableElmentTypes() {
    return new ElementType[] {java.lang.annotation.ElementType.FIELD,
        java.lang.annotation.ElementType.TYPE};
  }

  /**
   * Overrides the parent to use a validator-specific error message when the element type is not
   * allowed.
   */
  @Override
  protected void checkIfElementTypeAvailable(ElementType elementType) {
    if (!Arrays.asList(getAvailableElmentTypes()).contains(elementType)) {
      new Violations().add(new BusinessViolation("MSG_ERR_VALIDATOR_ELEMENT_TYPE_NOT_ALLOWED",
          getInfo().getSystemName(), this.getClass().getSimpleName(), elementType.toString()))
          .throwIfAny();
    }
  }

  /**
   * Verifies that the current data type is included in the set of allowed katas for this validator.
   */
  private void checkIfKataAvailable() {
    List<DataTypeKataEnum> kataList = Arrays.asList(getAvailableKatas());
    if (!kataList.contains(dtInfo.getKata())) {
      throw new RuntimeException("The specified Kata not allowed. (system name: "
          + getInfo().getSystemName() + ", annotation name: " + this.annotationName
          + ", dataType name: " + dtInfo.getDataTypeName()
          + ", dataType: " + dtInfo.getKata().toString() + ")");
    }
  }

  /**
   * Adds validator-specific parameters (excluding the fieldId parameter) to the given parameter
   * list generator.
   */
  protected abstract void getParamGenWithoutFieldId(ParamListGen plistGen);

  @SuppressWarnings("null")
  @Override
  protected ParamListGen getParamGen() {
    ParamListGen plistGen = new ParamListGen();
    if (!getInfo().getSysCmnRootInfo().isFrameworkKindSpring() && id != null) {
      plistGen.add(new ParamGenWithSingleValue("fieldId", id, DataTypeKataEnum.STRING));
    }

    getParamGenWithoutFieldId(plistGen);

    return plistGen;
  }
}
