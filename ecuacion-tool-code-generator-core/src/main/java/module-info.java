module jp.ecuacion.tool.codegenerator.core {
  exports jp.ecuacion.tool.codegenerator.core.controller;
  exports jp.ecuacion.tool.codegenerator.core.constant;
  exports jp.ecuacion.tool.codegenerator.core.dto;
  exports jp.ecuacion.tool.codegenerator.core.enums;
  exports jp.ecuacion.tool.codegenerator.core.generator;
  exports jp.ecuacion.tool.codegenerator.core.generator.annotation;
  exports jp.ecuacion.tool.codegenerator.core.generator.annotation.param;
  exports jp.ecuacion.tool.codegenerator.core.generator.annotation.validator;
  exports jp.ecuacion.tool.codegenerator.core.generator.entity;
  exports jp.ecuacion.tool.codegenerator.core.validation;

  opens jp.ecuacion.tool.codegenerator.core.config;
  opens jp.ecuacion.tool.codegenerator.core.dto;

  requires org.apache.commons.io;
  requires transitive jakarta.validation;
  requires java.xml;
  requires transitive jp.ecuacion.lib.core;
  requires org.apache.poi.poi;
  requires spring.context;
  requires spring.beans;
  requires jp.ecuacion.splib.core;
  requires jp.ecuacion.util.poi;
  requires java.sql;
  requires org.apache.commons.lang3;
  requires jakarta.annotation;
  requires org.apache.commons.collections4;

  provides jp.ecuacion.lib.core.spi.ApplicationCoreProvider
      with jp.ecuacion.tool.codegenerator.core.spi.impl.internal.ApplicationCoreProviderImpl;
  provides jp.ecuacion.lib.core.spi.ApplicationCoreProfileProvider
      with jp.ecuacion.tool.codegenerator.core.spi.impl.internal.ApplicationCoreProfileProviderImpl;
  provides jp.ecuacion.lib.core.spi.MessagesCoreProvider
      with jp.ecuacion.tool.codegenerator.core.spi.impl.internal.MessagesCoreProviderImpl;
  provides jp.ecuacion.lib.core.spi.EnumNamesCoreProvider
      with jp.ecuacion.tool.codegenerator.core.spi.impl.internal.EnumNamesCoreProviderImpl;
}
