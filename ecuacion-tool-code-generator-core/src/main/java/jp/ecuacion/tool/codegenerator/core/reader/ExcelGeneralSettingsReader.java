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
package jp.ecuacion.tool.codegenerator.core.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscGroupRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscOptimisticLockRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.MiscSoftDeleteRootInfo;
import jp.ecuacion.tool.codegenerator.core.dto.SystemCommonRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;

/**
 * Reads the "general settings" sheet from the input Excel file.
 *
 * <p>NullAway is suppressed at class level: this reader is allowed to assume that every property
 *     key listed by the template exists, and any missing key surfaces as a downstream validation
 *     error rather than as a typed null check here.</p>
 */
@SuppressWarnings("NullAway")
public class ExcelGeneralSettingsReader extends StringOneLineHeaderExcelTableReader {

  private static final String[] headerLabels = new String[] {"分類", "分類説明", "項目", "説明", "値", "備考"};

  /** Constructs an instance that targets the general-settings sheet. */
  public ExcelGeneralSettingsReader() {
    super("各種設定", headerLabels);
  }

  private static int COL_KIND = 0;
  // private static int COL_KIND_DESC = 1;
  private static int COL_KEY = 2;
  // private static int COL_KEY_DESC = 3;
  private static int COL_VALUE = 4;
  // private static int COL_NOTE = 5;

  private static String GROUP_SYSTEM_COMMON = "SYSTEM_COMMON";
  private static String GROUP_LOGICAL_DELETE = "LOGICAL_DELETE";
  private static String GROUP_GROUPING = "GROUPING";
  private static String GROUP_OPTIMISTIC_LOCKING = "OPTIMISTIC_LOCKING";


  /**
   * Reads the Excel file at the given path and returns a data-kind-to-root-info map for all setting
   * groups.
   */
  public HashMap<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    HashMap<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();

    // Retrieve table data in list form
    List<List<String>> rowList = read(excelPath);
    // Load data temporarily, grouped by category
    HashMap<String, HashMap<String, String>> propertiesMap = new HashMap<>();

    for (List<String> colList : rowList) {
      if (!propertiesMap.keySet().contains(colList.get(COL_KIND))) {
        propertiesMap.put(colList.get(COL_KIND), new HashMap<>());
      }

      HashMap<String, String> props = propertiesMap.get(colList.get(COL_KIND));
      props.put(colList.get(COL_KEY), colList.get(COL_VALUE));
    }

    // SYSTEM_COMMON
    rtnMap.put(DataKindEnum.SYSTEM_COMMON, getSystemCommon(propertiesMap.get(GROUP_SYSTEM_COMMON)));
    // LOGICAL_DELETE
    rtnMap.put(DataKindEnum.MISC_REMOVED_DATA,
        getLogicalDelete(propertiesMap.get(GROUP_LOGICAL_DELETE)));
    // GROUPING
    rtnMap.put(DataKindEnum.MISC_GROUP, getGroup(propertiesMap.get(GROUP_GROUPING)));
    // OPTIMISTIC_LOCKING
    rtnMap.put(DataKindEnum.MISC_OPTIMISTIC_LOCK,
        getOptimisticLocking(propertiesMap.get(GROUP_OPTIMISTIC_LOCKING)));

    return rtnMap;
  }

  private AbstractRootInfo getSystemCommon(HashMap<String, String> props) {
    return new SystemCommonRootInfo(props.get("TEMPLATE_VERSION"), props.get("SYSTEM_NAME"),
        props.get("BASE_PACKAGE"),
        // props.get("PROJECT_KIND"),
        props.get("FRAMEWORK_KIND"), props.get("USES_SPRING_NAMING_CONVENTION"),
        props.get("USES_UTIL_JPA"), props.get("CHARSET"), props.get("LANG_DEFAULT"),
        props.get("LANG_SUPPORT_01"), props.get("LANG_SUPPORT_02"), props.get("LANG_SUPPORT_03"),
        props.get("PROHIBITED_CHARS"), props.get("PROHIBITED_CHARS_DESC_LANG_DEFAULT"),
        props.get("PROHIBITED_CHARS_DESC_LANG_SUPPORT_01"),
        props.get("PROHIBITED_CHARS_DESC_LANG_SUPPORT_02"),
        props.get("PROHIBITED_CHARS_DESC_LANG_SUPPORT_03"));
  }

  private AbstractRootInfo getLogicalDelete(HashMap<String, String> props) {
    MiscSoftDeleteRootInfo rootInfo = new MiscSoftDeleteRootInfo(props.get("COLUMN_NAME"),
        props.get("DATA_TYPE_NAME"), props.get("DEFAULT_VALUE"), props.get("METHOD_NAME"),
        props.get("UPDATE_VALUE"), props.get("ADDITIONAL_PARAMS"));
    return rootInfo;
  }

  private AbstractRootInfo getGroup(HashMap<String, String> props) {
    MiscGroupRootInfo rootInfo = new MiscGroupRootInfo(props.get("COLUMN_NAME"),
        props.get("DATA_TYPE_NAME"), props.get("TABLE_NAMES_WITHOUT_GROUPING"),
        props.get("NEEDS_NO_GROUPING_MODULE"), props.get("DIVIDES_DAO_MODULE"));
    return rootInfo;
  }

  private AbstractRootInfo getOptimisticLocking(HashMap<String, String> props) {
    MiscOptimisticLockRootInfo rootInfo =
        new MiscOptimisticLockRootInfo(props.get("COLUMN_NAME"), props.get("DATA_TYPE_NAME"));
    return rootInfo;
  }
}
