/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

public class AsnSequenceOf extends AsnConstructedType {

    public AsnConstraint constraint = null;
    public boolean isSequenceOf = false; // Differentiates between SEQUENCE OF and SET OF types
    public boolean isSizeConstraint = false;
    public AsnElementType componentType = null;

}
