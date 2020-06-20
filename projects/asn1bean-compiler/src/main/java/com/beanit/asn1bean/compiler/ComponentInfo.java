/*
 * Copyright 2020 The ASN1bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
