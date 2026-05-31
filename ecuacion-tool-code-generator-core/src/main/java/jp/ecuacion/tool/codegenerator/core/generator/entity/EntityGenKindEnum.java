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
