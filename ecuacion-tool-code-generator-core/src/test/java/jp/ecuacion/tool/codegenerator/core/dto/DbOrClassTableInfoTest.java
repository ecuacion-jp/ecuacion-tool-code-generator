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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

  // ---------- getName ----------

  @Nested
  @DisplayName("getName()")
  class GetName {

    @ParameterizedTest
    @CsvSource({"MY_TABLE,MY_TABLE", "acc_group,acc_group", "Foo,Foo"})
    @DisplayName("normal names are returned unchanged")
    void normalNamesReturnedUnchanged(String input, String expected) {
      assertThat(new DbOrClassTableInfo(input).getName()).isEqualTo(expected);
    }

    @Test
    @DisplayName("SYSTEM_COMMON_ENTITY is mapped to SYSTEM_COMMON")
    void systemCommonEntityIsMapped() {
      assertThat(new DbOrClassTableInfo("SYSTEM_COMMON_ENTITY").getName())
          .isEqualTo("SYSTEM_COMMON");
    }
  }

  // ---------- getNameCpCamel ----------

  @Nested
  @DisplayName("getNameCpCamel()")
  class GetNameCpCamel {

    @ParameterizedTest
    @CsvSource({"MY_TABLE,MyTable", "acc_group,AccGroup", "ACC,Acc"})
    @DisplayName("converts table name to upper-camel")
    void converts(String tableName, String expected) {
      assertThat(new DbOrClassTableInfo(tableName).getNameCpCamel()).isEqualTo(expected);
    }
  }

  // ---------- getNameCamel ----------

  @Nested
  @DisplayName("getNameCamel()")
  class GetNameCamel {

    @ParameterizedTest
    @CsvSource({"MY_TABLE,myTable", "acc_group,accGroup", "ACC,acc"})
    @DisplayName("converts table name to lower-camel")
    void converts(String tableName, String expected) {
      assertThat(new DbOrClassTableInfo(tableName).getNameCamel()).isEqualTo(expected);
    }
  }

  // ---------- hasColumn / getColumn ----------

  @Nested
  @DisplayName("hasColumn() / getColumn()")
  class ColumnLookup {

    @Test
    @DisplayName("hasColumn returns true when the column exists")
    void hasColumnTrue() {
      tableInfo.columnList.add(colWithName("MY_COL"));
      assertThat(tableInfo.hasColumn("MY_COL")).isTrue();
    }

    @Test
    @DisplayName("hasColumn returns false when the column is absent")
    void hasColumnFalse() {
      tableInfo.columnList.add(colWithName("MY_COL"));
      assertThat(tableInfo.hasColumn("OTHER_COL")).isFalse();
    }

    @Test
    @DisplayName("getColumn returns the matching column")
    void getColumnFound() {
      DbOrClassColumnInfo c = colWithName("MY_COL");
      tableInfo.columnList.add(c);
      assertThat(tableInfo.getColumn("MY_COL")).isSameAs(c);
    }

    @Test
    @DisplayName("getColumn returns null when the column is absent")
    void getColumnNotFound() {
      tableInfo.columnList.add(colWithName("MY_COL"));
      assertThat(tableInfo.getColumn("OTHER_COL")).isNull();
    }
  }

  // ---------- getPkColumn / hasPkColumn ----------

  @Nested
  @DisplayName("getPkColumn() / hasPkColumn()")
  class PkColumn {

    @Test
    @DisplayName("getPkColumn returns the PK column when present")
    void getPkColumnFound() {
      DbOrClassColumnInfo nonPk = colWithPk(false);
      DbOrClassColumnInfo pk = colWithPk(true);
      tableInfo.columnList.add(nonPk);
      tableInfo.columnList.add(pk);
      assertThat(tableInfo.getPkColumn()).isSameAs(pk);
    }

    @Test
    @DisplayName("getPkColumn returns null when no PK column exists")
    void getPkColumnNotFound() {
      tableInfo.columnList.add(colWithPk(false));
      assertThat(tableInfo.getPkColumn()).isNull();
    }

    @Test
    @DisplayName("hasPkColumn returns true when PK column exists")
    void hasPkColumnTrue() {
      tableInfo.columnList.add(colWithPk(true));
      assertThat(tableInfo.hasPkColumn()).isTrue();
    }

    @Test
    @DisplayName("hasPkColumn returns false when no PK column exists")
    void hasPkColumnFalse() {
      tableInfo.columnList.add(colWithPk(false));
      assertThat(tableInfo.hasPkColumn()).isFalse();
    }
  }

  // ---------- hasUniqueConstraint / setHasUniqueConstraint ----------

  @Nested
  @DisplayName("hasUniqueConstraint() / setHasUniqueConstraint()")
  class UniqueConstraint {

    @Test
    @DisplayName("returns false by default")
    void defaultFalse() {
      assertThat(tableInfo.hasUniqueConstraint()).isFalse();
    }

    @Test
    @DisplayName("returns true after set to true")
    void setTrue() {
      tableInfo.setHasUniqueConstraint(true);
      assertThat(tableInfo.hasUniqueConstraint()).isTrue();
    }

    @Test
    @DisplayName("returns false after being reset to false")
    void resetToFalse() {
      tableInfo.setHasUniqueConstraint(true);
      tableInfo.setHasUniqueConstraint(false);
      assertThat(tableInfo.hasUniqueConstraint()).isFalse();
    }
  }

  // ---------- getRelationColumnList / hasRelationColumn ----------

  @Nested
  @DisplayName("getRelationColumnList() / hasRelationColumn()")
  class RelationColumns {

    @Test
    @DisplayName("returns empty list when no relation columns exist")
    void emptyWhenNoRelations() {
      tableInfo.columnList.add(colWithRelation(false));
      assertThat(tableInfo.getRelationColumnList()).isEmpty();
    }

    @Test
    @DisplayName("returns only relation columns when mixed")
    void returnsOnlyRelationColumns() {
      DbOrClassColumnInfo rel = colWithRelation(true);
      DbOrClassColumnInfo nonRel = colWithRelation(false);
      tableInfo.columnList.add(nonRel);
      tableInfo.columnList.add(rel);
      assertThat(tableInfo.getRelationColumnList()).containsExactly(rel);
    }

    @Test
    @DisplayName("hasRelationColumn returns true when a relation column exists")
    void hasRelationColumnTrue() {
      tableInfo.columnList.add(colWithRelation(true));
      assertThat(tableInfo.hasRelationColumn()).isTrue();
    }

    @Test
    @DisplayName("hasRelationColumn returns false when no relation column exists")
    void hasRelationColumnFalse() {
      tableInfo.columnList.add(colWithRelation(false));
      assertThat(tableInfo.hasRelationColumn()).isFalse();
    }
  }

  // ---------- getCustomGroupColumn / hasCustomGroupColumn ----------

  @Nested
  @DisplayName("getCustomGroupColumn() / hasCustomGroupColumn()")
  class CustomGroupColumn {

    @Test
    @DisplayName("getCustomGroupColumn returns null when no custom group column exists")
    void getCustomGroupColumnNotFound() {
      tableInfo.columnList.add(colWithCustomGroup(false));
      assertThat(tableInfo.getCustomGroupColumn()).isNull();
    }

    @Test
    @DisplayName("getCustomGroupColumn returns the custom group column when present")
    void getCustomGroupColumnFound() {
      DbOrClassColumnInfo cg = colWithCustomGroup(true);
      tableInfo.columnList.add(colWithCustomGroup(false));
      tableInfo.columnList.add(cg);
      assertThat(tableInfo.getCustomGroupColumn()).isSameAs(cg);
    }

    @Test
    @DisplayName("hasCustomGroupColumn returns true when custom group column exists")
    void hasCustomGroupColumnTrue() {
      tableInfo.columnList.add(colWithCustomGroup(true));
      assertThat(tableInfo.hasCustomGroupColumn()).isTrue();
    }

    @Test
    @DisplayName("hasCustomGroupColumn returns false when no custom group column exists")
    void hasCustomGroupColumnFalse() {
      tableInfo.columnList.add(colWithCustomGroup(false));
      assertThat(tableInfo.hasCustomGroupColumn()).isFalse();
    }
  }

  // ---------- helpers ----------

  private DbOrClassColumnInfo col(boolean isJavaOnly) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.getIsJavaOnly()).thenReturn(isJavaOnly);
    return c;
  }

  private DbOrClassColumnInfo colWithName(String name) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.getName()).thenReturn(name);
    return c;
  }

  private DbOrClassColumnInfo colWithPk(boolean isPk) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.isPk()).thenReturn(isPk);
    return c;
  }

  private DbOrClassColumnInfo colWithRelation(boolean isRelation) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.isRelation()).thenReturn(isRelation);
    return c;
  }

  private DbOrClassColumnInfo colWithCustomGroup(boolean isCustomGroup) {
    @SuppressWarnings("null")
    DbOrClassColumnInfo c = mock(DbOrClassColumnInfo.class);
    when(c.isCustomGroupColumn()).thenReturn(isCustomGroup);
    return c;
  }
}
