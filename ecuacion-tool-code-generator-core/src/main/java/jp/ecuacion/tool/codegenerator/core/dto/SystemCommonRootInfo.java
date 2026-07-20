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
package jp.ecuacion.tool.codegenerator.core.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ecuacion.tool.codegenerator.core.constant.Constants;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.util.ReaderUtil;
import jp.ecuacion.tool.codegenerator.core.validation.StrBoolean;
import org.apache.commons.lang3.StringUtils;

/**
 * Holds system-wide common settings such as the base package, framework kind, character
 * encoding, and prohibited characters.
 */
public class SystemCommonRootInfo extends AbstractRootInfo {

  private String templateVersion;

  @NotEmpty
  @Pattern(regexp = Constants.REG_EX_AL_NUM_HY)
  private String systemName;

  @NotEmpty
  @Size(min = 1, max = 200)
  @Pattern(regexp = Constants.REG_EX_DOWN_NUM_DOT)
  private String basePackage;

  @Pattern(regexp = "jakarta EE|Spring Framework")
  private String frameworkKind;

  @StrBoolean
  private String usesSpringNamingConvention;

  @StrBoolean
  private String usesUtilJpa;

  @NotEmpty
  @Size(min = 1, max = 50)
  private String characterEncoding;

  @NotEmpty
  private String defaultLang;
  private String supportLang1;
  private String supportLang2;
  private String supportLang3;

  private List<String> supportedLangArr = new ArrayList<>();

  @NotEmpty
  private String prohibitedChars;
  @NotEmpty
  private String prohibitedCharsDescDefaultLang;
  private String prohibitedCharsDescSupportLang1;
  private String prohibitedCharsDescSupportLang2;
  private String prohibitedCharsDescSupportLang3;

  private Map<String, String> prohibitedCharsDescLangMap = new HashMap<>();

  /**
   * Constructs an instance with all system-common settings and builds the prohibited-chars
   * description map for each language.
   */
  public SystemCommonRootInfo(String templateVersion, String systemName, String basePackage,
      // String projectType,
      String frameworkKind, String usesSpringNamingConvention, String usesUtilJpa,
      String characterEncoding, String defaultLang, String supportLang1, String supportLang2,
      String supportLang3, String prohibitedChars, String prohibitedCharsDescDefaultLang,
      String prohibitedCharsDescSupportLang1, String prohibitedCharsDescSupportLang2,
      String prohibitedCharsDescSupportLang3) {

    super(DataKindEnum.SYSTEM_COMMON);
    this.templateVersion = templateVersion;
    this.systemName = systemName;
    this.basePackage = basePackage;
    this.frameworkKind = frameworkKind;
    this.usesSpringNamingConvention = usesSpringNamingConvention;
    this.usesUtilJpa = usesUtilJpa;
    this.characterEncoding = characterEncoding;
    this.defaultLang = defaultLang;
    this.supportLang1 = supportLang1;
    this.supportLang2 = supportLang2;
    this.supportLang3 = supportLang3;
    this.prohibitedChars = prohibitedChars;
    this.prohibitedCharsDescDefaultLang = prohibitedCharsDescDefaultLang;
    this.prohibitedCharsDescSupportLang1 = prohibitedCharsDescSupportLang1;
    this.prohibitedCharsDescSupportLang2 = prohibitedCharsDescSupportLang2;
    this.prohibitedCharsDescSupportLang3 = prohibitedCharsDescSupportLang3;

    for (String supportLang : new String[] {supportLang1, supportLang2, supportLang3}) {
      if (!StringUtils.isEmpty(supportLang)) {
        supportedLangArr.add(supportLang);
      }
    }

    for (String[] langAndDesc : new String[][] {new String[] {"", prohibitedCharsDescDefaultLang},
        new String[] {defaultLang, prohibitedCharsDescDefaultLang},
        new String[] {supportLang1, prohibitedCharsDescSupportLang1},
        new String[] {supportLang2, prohibitedCharsDescSupportLang2},
        new String[] {supportLang3, prohibitedCharsDescSupportLang3}}) {

      prohibitedCharsDescLangMap.put(langAndDesc[0], langAndDesc[1]);
    }
  }

  public String getTemplateVersion() {
    return templateVersion;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  // basePackage
  public String getBasePackage() {
    return basePackage;
  }

  // projectType
  // public String getProjectType() {
  // return projectType;
  // }

  /** Returns {@code true} if the framework kind is Spring Framework. */
  public boolean isFrameworkKindSpring() {
    if (frameworkKind.equals("Spring Framework")) {
      return true;

    } else {
      return false;
    }
  }

  public boolean getUsesSpringNamingConvention() {
    return ReaderUtil.boolStrToBoolean(usesSpringNamingConvention);
  }

  public boolean getUsesUtilJpa() {
    return ReaderUtil.boolStrToBoolean(usesUtilJpa);
  }

  // characterEncoding
  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public String getDefaultLang() {
    return defaultLang;
  }

  public String getSupportLang1() {
    return supportLang1;
  }

  public String getSupportLang2() {
    return supportLang2;
  }

  public String getSupportLang3() {
    return supportLang3;
  }

  public List<String> getSupportedLangArr() {
    return supportedLangArr;
  }

  // prohibitedCharacters
  public String getProhibitedChars() {
    return prohibitedChars;
  }

  public String getProhibitedCharsDescDefaultLang() {
    return prohibitedCharsDescDefaultLang;
  }

  public String getProhibitedCharsDescSupportLang1() {
    return prohibitedCharsDescSupportLang1;
  }

  public String getProhibitedCharsDescSupportLang2() {
    return prohibitedCharsDescSupportLang2;
  }

  public String getProhibitedCharsDescSupportLang3() {
    return prohibitedCharsDescSupportLang3;
  }

  /** Returns the prohibited-chars description for the given language, or {@code null} if absent. */
  public @org.jspecify.annotations.Nullable String getProhibitedCharsDesc(String lang) {
    return prohibitedCharsDescLangMap.get(lang);
  }

  /** Always returns {@code true} 
   * because this info is always defined (all blank values would cause an error). */
  @Override
  public boolean isDefined() {
    return true;
  }

  @Override
  public void consistencyCheckAndCoplementData() {}
}
