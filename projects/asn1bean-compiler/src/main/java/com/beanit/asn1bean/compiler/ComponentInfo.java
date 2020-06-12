package com.beanit.asn1bean.compiler;

import com.beanit.asn1bean.compiler.BerClassWriter.Tag;

class ComponentInfo {

  final String variableName;
  final String className;
  final Tag tag;
  final boolean mayBeLast;
  final boolean isOptionalOrDefault;
  final boolean isDirectChoiceOrAny;

  public ComponentInfo(
      String variableName,
      String className,
      Tag tag,
      boolean mayBeLast,
      boolean isOptionalOrDefault,
      boolean isDirectChoiceOrAny) {
    this.variableName = variableName;
    this.className = className;
    this.tag = tag;
    this.mayBeLast = mayBeLast;
    this.isOptionalOrDefault = isOptionalOrDefault;
    this.isDirectChoiceOrAny = isDirectChoiceOrAny;
  }
}
