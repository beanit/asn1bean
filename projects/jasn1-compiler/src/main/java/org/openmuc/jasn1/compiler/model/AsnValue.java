/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.util.List;

public class AsnValue {
    String bStr;
    public AsnBitOrOctetStringValue bStrValue;
    public String cStr;
    public AsnCharacterStringValue cStrValue;
    public AsnChoiceValue chval;
    public AsnDefinedValue definedValue;
    String hStr, enumIntVal;
    public boolean isAsnOIDValue;
    boolean isBStrOrOstrValue;
    public boolean isCStrValue;
    public boolean isCString;
    public boolean isChoiceValue;
    public boolean isDefinedValue;
    boolean isEnumIntValue;
    public boolean isFalseKW;
    public boolean isMinusInfinity;
    public boolean isNullKW;
    public boolean isPlusInfinity;
    public boolean isSequenceOfValue;
    public boolean isSequenceValue;
    public boolean isSignedNumber;
    public boolean isTrueKW;
    public String name;
    public AsnOidComponentList oidval;
    public AsnSequenceOfValue seqOfVal;
    public AsnSequenceValue seqval;
    public AsnSignedNumber signedNumber;
    public boolean isValueInBraces = false;
    public List<String> valueInBracesTokens;

}
