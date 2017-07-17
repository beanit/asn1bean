/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.util.ArrayList;
import java.util.List;

public class AsnInformationObjectClass {

    public String name = "";
    public List<AsnElementType> elementList = new ArrayList<>();
    public List<String> syntaxTokens = new ArrayList<>();

}
