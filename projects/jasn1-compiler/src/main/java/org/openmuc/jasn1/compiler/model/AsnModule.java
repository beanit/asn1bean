/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AsnModule {

    public enum TagDefault {
        EXPLICIT,
        IMPLICIT,
        AUTOMATIC;
    }

    public ArrayList exportSymbolList;
    public boolean exported;
    public boolean extensible;
    public List<SymbolsFromModule> importSymbolFromModuleList = new ArrayList<>();
    ArrayList importSymbolList;
    public boolean imported;
    public AsnModuleIdentifier moduleIdentifier;
    public boolean tag;
    public TagDefault tagDefault = TagDefault.EXPLICIT;
    public final HashMap<String, AsnType> typesByName = new HashMap<>();
    public final HashMap<String, AsnValueAssignment> asnValueAssignmentsByName = new HashMap<>();
    public final HashMap<String, AsnInformationObjectClass> objectClassesByName = new HashMap<>();

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
}
