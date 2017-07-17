/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

public class AsnCharacterString extends AsnUniversalType {
    public final String BUILTINTYPE = "CHARACTER STRING";
    public AsnConstraint constraint;
    public boolean isUCSType; // Is Unrestricted Character String Type
    public String stringtype = "";

}
