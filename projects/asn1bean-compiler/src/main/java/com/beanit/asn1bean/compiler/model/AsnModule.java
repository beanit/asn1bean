/*
 * Copyright 2012 The ASN1bean Authors
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
package com.beanit.asn1bean.compiler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AsnModule {

  public final HashMap<String, AsnType> typesByName = new HashMap<>();
  public final HashMap<String, AsnValueAssignment> asnValueAssignmentsByName = new HashMap<>();
  public final HashMap<String, AsnInformationObjectClass> objectClassesByName = new HashMap<>();
  public ArrayList exportSymbolList;
  public boolean exported;
  public boolean extensible;
  public List<SymbolsFromModule> importSymbolFromModuleList = new ArrayList<>();
  public boolean imported;
  public AsnModuleIdentifier moduleIdentifier;
  public boolean tag;
  public TagDefault tagDefault = TagDefault.EXPLICIT;
  ArrayList importSymbolList;

  public AsnModule() {
    exportSymbolList = new ArrayList<>();
    importSymbolList = new ArrayList<>();

    AsnElementType idElement = new AsnElementType();
    idElement.name = "id";
    idElement.typeReference = new AsnObjectIdentifier();
    AsnElementType typeElement = new AsnElementType();
    typeElement.name = "Type";
    typeElement.typeReference = new AsnAny();
    AsnInformationObjectClass typeIdentifier = new AsnInformationObjectClass();
    typeIdentifier.elementList.add(idElement);
    typeIdentifier.elementList.add(typeElement);

    objectClassesByName.put("TYPE-IDENTIFIER", typeIdentifier);

    AsnElementType propertyElement = new AsnElementType();
    propertyElement.name = "property";
    propertyElement.typeReference = new AsnBitString();
    AsnInformationObjectClass abstractSyntax = new AsnInformationObjectClass();
    abstractSyntax.elementList.add(idElement);
    abstractSyntax.elementList.add(typeElement);
    abstractSyntax.elementList.add(propertyElement);

    objectClassesByName.put("ABSTRACT-SYNTAX", abstractSyntax);
  }

  public enum TagDefault {
    EXPLICIT,
    IMPLICIT,
    AUTOMATIC
  }
}
