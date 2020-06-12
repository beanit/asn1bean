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
  public ArrayList<AsnValue> values = new ArrayList<AsnValue>();
}
