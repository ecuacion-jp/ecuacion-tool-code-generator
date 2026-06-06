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
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.tool.codegenerator.core.dto.AbstractRootInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.util.excel.table.reader.concrete.StringOneLineHeaderExcelTableReader;
import org.apache.poi.EncryptedDocumentException;
import org.jspecify.annotations.NonNull;

/**
 * Template base class for Excel readers that produce a two-level grouped structure
 * (root → group → item).
 *
 * <p>Subclasses implement the factory methods to supply concrete types; this class owns the
 * shared {@code readAndGetMap()} loop.
 *
 * @param <R> the root info type (e.g. {@code DbOrClassRootInfo}, {@code EnumRootInfo})
 * @param <G> the group type (e.g. {@code DbOrClassTableInfo}, {@code EnumClassInfo})
 */
public abstract class AbstractExcelGroupedReader<R extends AbstractRootInfo, G>
    extends StringOneLineHeaderExcelTableReader {

  /** Constructs an instance for the given sheet name and header labels. */
  protected AbstractExcelGroupedReader(String sheetName,
      @NonNull String[] headerLabels) {
    super(sheetName, headerLabels);
  }

  /** Reads the Excel file at the given path and returns a data-kind-to-root-info map. */
  public HashMap<DataKindEnum, AbstractRootInfo> readAndGetMap(String excelPath)
      throws EncryptedDocumentException, IOException {

    HashMap<DataKindEnum, AbstractRootInfo> rtnMap = new HashMap<>();
    R rootInfo = createRootInfo();
    rtnMap.put(getRootDataKind(), rootInfo);

    List<List<String>> rowList = read(excelPath);
    Map<String, G> existingMap = new HashMap<>();

    for (List<String> colList : rowList) {
      String key = colList.get(getKeyColumnIndex());
      if (!existingMap.containsKey(key)) {
        G group = createGroup(colList);
        existingMap.put(key, group);
        addGroupToRoot(rootInfo, group);
      }
      addItemToGroup(Objects.requireNonNull(existingMap.get(key)), colList);
    }
    return rtnMap;
  }

  /** Creates and returns the root info object for this reader. */
  protected abstract R createRootInfo();

  /** Returns the {@link DataKindEnum} key used when putting the root info into the result map. */
  protected abstract DataKindEnum getRootDataKind();

  /** Returns the column index that identifies the group key in each row. */
  protected abstract int getKeyColumnIndex();

  /** Creates a new group object from the first row of that group. */
  protected abstract G createGroup(List<String> colList);

  /** Adds a group to the root info's list. */
  protected abstract void addGroupToRoot(@NonNull R rootInfo, @NonNull G group);

  /** Creates an item from {@code colList} and adds it to {@code group}. */
  protected abstract void addItemToGroup(@NonNull G group, List<String> colList);
}
