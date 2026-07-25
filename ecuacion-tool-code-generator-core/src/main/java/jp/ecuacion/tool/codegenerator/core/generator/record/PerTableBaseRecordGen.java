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
package jp.ecuacion.tool.codegenerator.core.generator.record;

import java.util.List;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassColumnInfo;
import jp.ecuacion.tool.codegenerator.core.dto.DbOrClassTableInfo;
import jp.ecuacion.tool.codegenerator.core.enums.DataKindEnum;
import jp.ecuacion.tool.codegenerator.core.generatorhelper.util.ColumnGenUtil.ColFormat;

/**
 * Generates a per-table base record class that extends {@code SystemCommonBaseRecord} and
 * implements {@code ItemContainer}.
 */
public class PerTableBaseRecordGen extends AbstractBaseRecordGen {

  /** Constructs an instance for the given data kind. */
  public PerTableBaseRecordGen(DataKindEnum dataKind) {
    super(dataKind);
  }

  @Override
  public void generate() {
    internalGenerate(getInfo().getDbRootInfo().tableList, false);
  }

  /**
   * Generates the class header with imports for {@code ItemContainer} and the {@code
   * ItemNameKeyClass} annotation.
   */
  @Override
  public void generateHeader(DbOrClassTableInfo ti) {

    generateHeaderCommon(ti, rootBasePackage + ".base.entity." + ti.getNameCpCamel(),
        "jp.ecuacion.splib.core.container.*", "jp.ecuacion.lib.core.item.*",
        "jp.ecuacion.lib.core.util.StringUtil",
        "jp.ecuacion.lib.core.annotation.ItemNameKeyClass");

    sb.append("@ItemNameKeyClass(\"" + ti.getNameCamel() + "\")" + RT);
    sb.append("public abstract class " + ti.getNameCpCamel()
        + "BaseRecord extends SystemCommonBaseRecord implements ItemContainer {" + RT2);
  }

  @Override
  protected void generateMethods(DbOrClassTableInfo ti) {
    createIdsAndOptimisticLockVersionsAccessors(ti);
  }

  /**
   * Generates ids/optimisticLockVersions snapshot init code appended at the end of the
   * entity-arg constructor. See {@code jp.ecuacion.splib.core.record.SplibRecord#getIds()} for
   * why this is a pure snapshot, independent of this record's own id/version setters for
   * relations.
   */
  @Override
  protected void generateIdsAndVersionsInit(DbOrClassTableInfo ti) {
    List<DbOrClassColumnInfo> relColList = ti.getRelationColumnWithoutGroupList();
    // "," rather than "-": manually-created (e.g. seed) records are often given a negative
    // id/version on purpose to avoid colliding with the DB sequence, and "-" would then collide
    // with the leading minus sign.
    final String sep = ",";

    // ids snapshot: this record's own PK, then each relation's PK, in relColList order.
    String pkGet = code.generateString(ti.getPkColumn(), ColFormat.GET);
    sb.append(T2 + "this.setIds(StringUtil.getSeparatedValuesString(new String[] {" + pkGet
        + " == null ? \"\" : " + pkGet);
    for (DbOrClassColumnInfo ci : relColList) {
      String relField = ci.getEffectiveRelationObjVarNameCp();
      DbOrClassColumnInfo pk =
          getInfo().getTableInfo(ci.getRelationRefTable()).getPkColumnIncludingSystemCommon();
      String refPkGet = code.generateString(pk, ColFormat.GET);
      sb.append(", get" + relField + "() == null || get" + relField + "()." + refPkGet + " == null"
          + " ? \"\" : get" + relField + "()." + refPkGet);
    }
    sb.append("}, \"" + sep + "\"));" + RT);

    // optimisticLockVersions snapshot: same order as ids.
    String ver = ti.getVersionColumnIncludingSystemCommon().getNameCpCamel();
    String verGet = "get" + ver + "()";
    sb.append(
        T2 + "this.setOptimisticLockVersions(StringUtil.getSeparatedValuesString(new String[] {"
            + verGet + " == null ? \"\" : " + verGet);
    for (DbOrClassColumnInfo ci : relColList) {
      String relFieldGet = "get" + ci.getEffectiveRelationObjVarNameCp() + "()";
      DbOrClassColumnInfo v =
          getInfo().getTableInfo(ci.getRelationRefTable()).getVersionColumnIncludingSystemCommon();
      String refVerGet = code.generateString(v, ColFormat.GET);
      sb.append(", " + relFieldGet + " == null || " + relFieldGet + "." + refVerGet
          + " == null ? \"\" : " + relFieldGet + "." + refVerGet);
    }
    sb.append("}, \"" + sep + "\"));" + RT);
  }

  /**
   * Generates {@code setIds()} (delegating this record's own PK only, never a relation's) and the
   * snapshot accessors used to read this record's own and each related record's optimistic-lock
   * id/version for an explicit {@code findAndOptimisticLockingCheck()} call.
   */
  private void createIdsAndOptimisticLockVersionsAccessors(DbOrClassTableInfo ti) {
    final List<DbOrClassColumnInfo> relColList = ti.getRelationColumnWithoutGroupList();

    // setIds: only this record's own PK is delegated. Related records' PKs stay pure snapshot
    // values (read via get<Relation>IdSnapshot()) so they never conflict with a directly-bound,
    // user-editable field for the same relation.
    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public void setIds(String idCsv) {" + RT);
    sb.append(T2 + "super.setIds(idCsv);" + RT);
    sb.append(T2 + "String[] ids = idCsv.split(\",\", -1);" + RT);
    sb.append(T2 + "if (ids.length < 1) return;" + RT2);
    sb.append(T2 + code.generateString(ti.getPkColumn(), ColFormat.SET, "ids[0]") + ";" + RT);
    sb.append(T1 + "}" + RT2);

    // This record's own version snapshot, for findAndOptimisticLockingCheck() of itself.
    sb.append(T1 + "public String getVersionSnapshot() {" + RT);
    sb.append(T2 + "return getSnapshotSegment(getOptimisticLockVersions(), 0);" + RT);
    sb.append(T1 + "}" + RT2);

    // Per-relation id/version snapshots, for an explicit findAndOptimisticLockingCheck() of a
    // related record when the caller opts into checking it.
    int i = 0;
    while (relColList.size() > i) {
      DbOrClassColumnInfo ci = relColList.get(i);
      String relField = ci.getEffectiveRelationObjVarNameCp();
      int index = i + 1;

      sb.append(T1 + "public String get" + relField + "IdSnapshot() {" + RT);
      sb.append(T2 + "return getSnapshotSegment(getIds(), " + index + ");" + RT);
      sb.append(T1 + "}" + RT2);

      sb.append(T1 + "public String get" + relField + "VersionSnapshot() {" + RT);
      sb.append(T2 + "return getSnapshotSegment(getOptimisticLockVersions(), " + index + ");" + RT);
      sb.append(T1 + "}" + RT2);

      i++;
    }
  }
}
