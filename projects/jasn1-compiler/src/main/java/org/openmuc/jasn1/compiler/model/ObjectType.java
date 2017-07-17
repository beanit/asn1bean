/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.util.ArrayList;

public class ObjectType {
    public final String BUILTINTYPE = "OBJECT-TYPE";
    public String accessPart;
    public ArrayList elements = new ArrayList();
    public String statusPart;
    public Object type;
    public AsnValue value;
}
