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
        "jp.ecuacion.lib.core.util.StringUtil", "jp.ecuacion.lib.core.annotation.ItemNameKeyClass");

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
   * entity-arg constructor. See {@code jp.ecuacion.splib.core.record.SplibRecord#getIds()} and
   * {@code jp.ecuacion.splib.core.record.SplibRecord#getOptimisticLockVersions()} for why ids
   * stay a pure snapshot independent of this record's own id setters for relations, while
   * versions are instead cascaded straight back into the live version fields by the overridden
   * {@code setOptimisticLockVersions()} generated below.
   */
  @Override
  protected void generateIdsAndVersionsInit(DbOrClassTableInfo ti) {
    List<DbOrClassColumnInfo> relColList = ti.getRelationColumnWithoutGroupList();
    // "," rather than "-": manually-created (e.g. seed) records are often given a negative
    // id/version on purpose to avoid colliding with the DB sequence, and "-" would then collide
    // with the leading minus sign.
    final String sep = ",";

    // ids snapshot: this record's own PK, then each relation's PK, in relColList order.
    DbOrClassColumnInfo pkCi = ti.getPkColumn();
    String pkGet = code.generateString(pkCi, ColFormat.GET);
    sb.append(T2 + "this.setIds(StringUtil.getSeparatedValuesString(new String[] {");
    if (pkCi.isRelation()) {
      // The PK is delegated through a relation (e.g. a @MapsId column), so pkGet itself is a
      // chained call like "getAcc().getId()" - the relation getter can return null and must be
      // guarded, same as each entry in the relColList loop below.
      sb.append("get" + pkCi.getEffectiveRelationObjVarNameCp() + "() == null || " + pkGet
          + " == null ? \"\" : " + pkGet);
    } else {
      sb.append(pkGet + " == null ? \"\" : " + pkGet);
    }
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
   * Generates {@code setIds()} (delegating this record's own PK only, never a relation's, and
   * exposing each related record's PK only through a snapshot accessor) and {@code
   * setOptimisticLockVersions()} (cascading every "," segment straight into this record's own
   * and each related record's live {@code version} field, since a version has no legitimate
   * independent, user-editable value to protect).
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
    DbOrClassColumnInfo setIdsPkCi = ti.getPkColumn();
    String pkSet = code.generateString(setIdsPkCi, ColFormat.SET, "ids[0]");
    if (setIdsPkCi.isRelation()) {
      // The PK is delegated through a relation (e.g. a @MapsId column). At the deepest level of
      // the record graph the relation object itself may not have been built (see
      // generateIdsAndVersionsInit's count cutoff), so guard against it being null.
      sb.append(
          T2 + "if (get" + setIdsPkCi.getEffectiveRelationObjVarNameCp() + "() != null) {" + RT);
      sb.append(T3 + pkSet + ";" + RT);
      sb.append(T2 + "}" + RT);
    } else {
      sb.append(T2 + pkSet + ";" + RT);
    }
    sb.append(T1 + "}" + RT2);

    // Per-relation id snapshot, for building a display value or an explicit
    // findAndOptimisticLockingCheck() call against a related record's original (pre-edit) id.
    int i = 0;
    while (relColList.size() > i) {
      DbOrClassColumnInfo ci = relColList.get(i);
      String relField = ci.getEffectiveRelationObjVarNameCp();
      int index = i + 1;

      sb.append(T1 + "public String get" + relField + "IdSnapshot() {" + RT);
      sb.append(T2 + "return getSnapshotSegment(getIds(), " + index + ");" + RT);
      sb.append(T1 + "}" + RT2);

      i++;
    }

    // setOptimisticLockVersions: unlike setIds(), every related record's version is cascaded
    // unconditionally, not just a PK-delegating one. A version column is never a directly-bound,
    // user-editable field, so there's nothing it could conflict with - each related record's live
    // version field can simply be kept in sync with the snapshot carried over the screen round
    // trip, and findAndOptimisticLockingCheck(XxxBaseRecord) (see BlGen) can then just read it
    // back out through the ordinary getVersionOfEntityDataType().
    sb.append(T1 + "@Override" + RT);
    sb.append(T1 + "public void setOptimisticLockVersions(String verCsv) {" + RT);
    sb.append(T2 + "super.setOptimisticLockVersions(verCsv);" + RT);
    sb.append(T2 + "String[] vers = verCsv.split(\",\", -1);" + RT);
    sb.append(T2 + "if (vers.length < 1) return;" + RT2);

    DbOrClassColumnInfo ownVerCi = ti.getVersionColumnIncludingSystemCommon();
    String ownVerSet = code.generateString(ownVerCi, ColFormat.SET, "vers[0]");
    sb.append(T2 + ownVerSet + ";" + RT);

    int v = 0;
    while (relColList.size() > v) {
      DbOrClassColumnInfo ci = relColList.get(v);
      String relField = ci.getEffectiveRelationObjVarNameCp();
      int index = v + 1;
      DbOrClassColumnInfo relVerCi =
          getInfo().getTableInfo(ci.getRelationRefTable()).getVersionColumnIncludingSystemCommon();
      String relVerSet = code.generateString(relVerCi, ColFormat.SET, "vers[" + index + "]");

      sb.append(T2 + "if (get" + relField + "() != null && vers.length > " + index + ") {" + RT);
      sb.append(T3 + "get" + relField + "()." + relVerSet + ";" + RT);
      sb.append(T2 + "}" + RT);

      v++;
    }
    sb.append(T1 + "}" + RT2);
  }
}
