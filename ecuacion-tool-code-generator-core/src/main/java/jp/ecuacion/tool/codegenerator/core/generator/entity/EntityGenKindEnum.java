package jp.ecuacion.tool.codegenerator.core.generator.entity;

/**
 * Enum representing the kind of entity generator: system common, per-table body, or container.
 *
 * <p>Entity and Record generators are first separated by class hierarchy. Common logic is
 * centralized in EntityGen. AbstractBaseRecord and LibEntity share common parts via utility methods
 * rather than inheritance.
 * </p>
 */
public enum EntityGenKindEnum {
  ENTITY_SYSTEM_COMMON, ENTITY_BODY, CONTAINER;
}
