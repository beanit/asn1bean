/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.util.ArrayList;

public class ConstraintElements {
    public AsnConstraint constraint;
    public ElementSetSpec elespec;
    public boolean isAlphabetConstraint;
    public boolean isElementSetSpec;
    public boolean isIncludeType;
    public boolean isLEndLess;
    public boolean isMaxKw;
    public boolean isMinKw;
    public boolean isPatternValue;
    public boolean isSizeConstraint;
    public boolean isTypeConstraint;
    public boolean isUEndLess;
    public boolean isValue;
    public boolean isValueRange;
    public boolean isWithComponent;
    public boolean isWithComponents;
    public AsnValue lEndValue, uEndValue;
    public Object type;
    public ArrayList typeConstraintList = new ArrayList();
    public AsnValue value;
}
