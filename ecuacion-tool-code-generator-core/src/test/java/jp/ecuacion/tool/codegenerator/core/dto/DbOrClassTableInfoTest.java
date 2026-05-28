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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DbOrClassTableInfoTest {

  @SuppressWarnings("null")
  private DbOrClassTableInfo tableInfo;

  @BeforeEach
  void setUp() {
    tableInfo = new DbOrClassTableInfo();
  }

  // ---------- getNonJavaOnlyColumns ----------

  @Test
  void getNonJavaOnlyColumns_returnsEmptyWhenColumnListIsEmpty() {
    assertThat(tableInfo.getNonJavaOnlyColumns()).isEmpty();
  }

  @Test
  void getNonJavaOnlyColumns_returnsEmptyWhenAllColumnsAreJavaOnly() {
    tableInfo.columnList.add(col(true));
    tableInfo.columnList.add(col(true));

    assertThat(tableInfo.getNonJavaOnlyColumns()).isEmpty();
  }

  @Test
  void getNonJavaOnlyColumns_returnsAllColumnsWhenNoneAreJavaOnly() {
    DbOrClassColumnInfo c1 = col(false);
    DbOrClassColumnInfo c2 = col(false);
    tableInfo.columnList.add(c1);
    tableInfo.columnList.add(c2);

    List<DbOrClassColumnInfo> result = tableInfo.getNonJavaOnlyColumns();

    assertThat(result).containsExactly(c1, c2);
  }

  @Test
  void getNonJavaOnlyColumns_returnsOnlyNonJavaOnlyColumnsWhenMixed() {
    DbOrClassColumnInfo javaOnly1 = col(true);
    DbOrClassColumnInfo normal1 = col(false);
    DbOrClassColumnInfo javaOnly2 = col(true);
    DbOrClassColumnInfo normal2 = col(false);
    tableInfo.columnList.add(javaOnly1);
    tableInfo.columnList.add(normal1);
    tableInfo.columnList.add(javaOnly2);
    tableInfo.columnList.add(normal2);

    List<DbOrClassColumnInfo> result = tableInfo.getNonJavaOnlyColumns();

    assertThat(result).containsExactly(normal1, normal2);
    assertThat(result).doesNotContain(javaOnly1, javaOnly2);
  }

  @Test
  void getNonJavaOnlyColumns_doesNotModifyOriginalColumnList() {
    tableInfo.columnList.add(col(false));
    tableInfo.columnList.add(col(true));

    tableInfo.getNonJavaOnlyColumns();

    assertThat(tableInfo.columnList).hasSize(2);
  }

  // ---------- helper ----------

  private DbOrClassColumnInfo col(boolean isJavaOnly) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.getIsJavaOnly()).thenReturn(isJavaOnly);
    return c;
  }
}
