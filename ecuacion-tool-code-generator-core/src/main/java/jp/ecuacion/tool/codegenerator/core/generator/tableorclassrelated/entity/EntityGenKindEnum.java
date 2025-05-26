package jp.ecuacion.tool.codegenerator.core.generator.tableorclassrelated.entity;

/**
 * クラス構成の階層としては、Entity / Recordなどでまず分ける。 Entityの中で、共通の記述方法が多いので、EntityGenの中に共通処理を書くイメージ。
 * 共通項目を扱うAbstractBaseRecord, LibEntityにも共通項はあるが、 これは親子関係はあきらめて、必要に応じutility的なメソッドを作ることにする。
 */
public enum EntityGenKindEnum {
  ENTITY_SYSTEM_COMMON, ENTITY_BODY, CONTAINER;
}
