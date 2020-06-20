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

import java.util.List;

public class AsnValue {
  public AsnBitOrOctetStringValue bStrValue;
  public String cStr;
  public AsnCharacterStringValue cStrValue;
  public AsnChoiceValue chval;
  public AsnDefinedValue definedValue;
  public boolean isAsnOIDValue;
  public boolean isCStrValue;
  public boolean isBString;
  public boolean isCString;
  public boolean isHString;
  public boolean isChoiceValue;
  public boolean isDefinedValue;
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
  public String bStr;
  public String hStr, enumIntVal;
  public AsnNamedValue namedval;
  public AsnScientificNumber scientificNumber;
  boolean isBStrOrOstrValue;
  boolean isEnumIntValue;
}
