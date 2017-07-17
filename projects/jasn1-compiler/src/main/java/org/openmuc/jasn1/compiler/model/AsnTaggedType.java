/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

public class AsnTaggedType extends AsnType {
    public boolean isDefinedType;
    public AsnTag tag = null;
    public String tagType = "";
    public AsnDefinedType definedType = null;
    // public String typeName = "";
    public String className = "";
    public AsnType typeReference = null;

}
