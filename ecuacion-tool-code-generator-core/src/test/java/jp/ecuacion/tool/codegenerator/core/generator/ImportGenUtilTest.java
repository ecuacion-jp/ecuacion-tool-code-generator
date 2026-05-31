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
package jp.ecuacion.tool.codegenerator.core.generator;

import static org.assertj.core.api.Assertions.assertThat;
import jp.ecuacion.tool.codegenerator.core.generator.AbstractGen.ImportBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ImportBlock}. */
@DisplayName("ImportBlock")
public class ImportGenUtilTest {

  @Nested
  @DisplayName("outputStr()")
  class OutputStr {

    @Test
    @DisplayName("empty import set produces empty string")
    void empty() {
      ImportBlock sut = new ImportBlock();
      assertThat(sut.outputStr()).isEmpty();
    }

    @Test
    @DisplayName("single import is output as an import statement")
    void singleImport() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.C");
      assertThat(sut.outputStr()).isEqualTo("import a.b.C;\r\n");
    }

    @Test
    @DisplayName("multiple imports are sorted alphabetically")
    void multipleImportsSorted() {
      ImportBlock sut = new ImportBlock();
      sut.add("x.y.Z");
      sut.add("a.b.C");
      assertThat(sut.outputStr()).isEqualTo("import a.b.C;\r\nimport x.y.Z;\r\n");
    }

    @Test
    @DisplayName("specific import is removed when a wildcard for the same package is present")
    void wildcardSuppressesSpecificImport() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.*");
      sut.add("a.b.C");
      assertThat(sut.outputStr()).isEqualTo("import a.b.*;\r\n");
    }

    @Test
    @DisplayName("wildcard does not suppress imports from a different package")
    void wildcardDoesNotAffectOtherPackages() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.*");
      sut.add("x.y.Z");
      assertThat(sut.outputStr()).isEqualTo("import a.b.*;\r\nimport x.y.Z;\r\n");
    }

    @Test
    @DisplayName("duplicate adds are deduplicated")
    void duplicatesAreDeduped() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.C", "a.b.C");
      assertThat(sut.outputStr()).isEqualTo("import a.b.C;\r\n");
    }
  }

  @Nested
  @DisplayName("removeIfContains()")
  class RemoveIfContains {

    @Test
    @DisplayName("existing entry is removed")
    void removesExistingEntry() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.C");
      sut.removeIfContains("a.b.C");
      assertThat(sut.outputStr()).isEmpty();
    }

    @Test
    @DisplayName("absent entry is silently ignored")
    void ignoresAbsentEntry() {
      ImportBlock sut = new ImportBlock();
      sut.add("a.b.C");
      sut.removeIfContains("x.y.Z");
      assertThat(sut.outputStr()).isEqualTo("import a.b.C;\r\n");
    }
  }
}
