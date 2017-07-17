/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

public class AsnEnum extends AsnUniversalType {
    final String BUILTINTYPE = "ENUMERATED";
    public AsnNamedNumberList namedNumberList;
    public String snaccName = ""; // specifically added for snacc code generation
    public Boolean isEnum = true;

}
