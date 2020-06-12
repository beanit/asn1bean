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
import java.util.List;

public class AsnConstraint {

  public List<String> tokens = new ArrayList<>();

  public ElementSetSpec addElemSetSpec;
  public AsnDefinedValue definedValue;
  public ConstraintElements cnsElem;
  public ElementSetSpec elemSetSpec;
  public boolean isAdditionalElementSpec;
  public boolean isColonValue;
  public boolean isCommaDotDot;
  public boolean isDefinedValue;
  public boolean isElementSetSpecs;
  public boolean isExceptionSpec;
  public boolean isSignedNumber;
  public AsnSignedNumber signedNumber;
  public Object type;
  public AsnValue value;

  // Return AllExcept additional Constraint Elements
  public ConstraintElements addElemSS_allExceptConstraintElem() {
    return addElemSetSpec.allExceptCnselem;
  }

  // Return if All Except additional Constraint Elements exists
  public boolean addElemSS_isAllExcept() {
    return addElemSetSpec.isAllExcept;
  }

  // Return AllExcept Constraint Elements
  public ConstraintElements elemSS_allExceptConstraintElem() {
    return elemSetSpec.allExceptCnselem;
  }

  // Return if All Except Constraint Elements exists
  public boolean elemSS_isAllExcept() {
    return elemSetSpec.isAllExcept;
  }

  // Return the required additional intersection element
  public Intersection get_addElemSS_IntsectElem(int i) {
    return (Intersection) addElemSetSpec.intersectionList.get(i);
  }

  // Returns first additional constraint Element in the first Intersection
  // List
  public ConstraintElements get_addElemSS_firstConstraintElem() {
    Intersection intersect = get_addElemSS_firstIntsectElem();

    return (ConstraintElements) intersect.cnsElemList.get(0);
  }

  // Return the first additional intersection element
  public Intersection get_addElemSS_firstIntsectElem() {
    return (Intersection) addElemSetSpec.intersectionList.get(0);
  }

  public ConstraintElements get_addElemSS_intersectionConstraintElems(
      int intersectElem, int constraintElem) {
    Intersection intersect = get_addElemSS_IntsectElem(intersectElem);

    return (ConstraintElements) intersect.cnsElemList.get(constraintElem);
  }

  // Return the required intersection element
  public Intersection get_elemSS_IntsectElem(int i) {
    return (Intersection) elemSetSpec.intersectionList.get(i);
  }

  // Returns first constraint Element in the first Intersection
  // List
  public ConstraintElements get_elemSS_firstConstraintElem() {
    Intersection intersect = get_elemSS_firstIntsectElem();

    return (ConstraintElements) intersect.cnsElemList.get(0);
  }

  // Return the first intersection element
  public Intersection get_elemSS_firstIntsectElem() {
    return (Intersection) elemSetSpec.intersectionList.get(0);
  }

  public ConstraintElements get_elemSS_intersectionConstraintElems(
      int intersectElem, int constraintElem) {
    Intersection intersect = get_elemSS_IntsectElem(intersectElem);

    return (ConstraintElements) intersect.cnsElemList.get(constraintElem);
  }

  // ---------For additionalElementSetSpecs
  // Return the total intersection elements in the add element
  // Spec list
  public int sz_addElemSS_IntsectList() {
    return addElemSetSpec.intersectionList.size();
  }

  // Returns Number of additional Constraint Elements in the
  // specified IntersectionList
  public int sz_addElemSS_intersectionConstraintElems(int i) {
    Intersection intersect = get_addElemSS_IntsectElem(i);

    if (intersect != null) {
      return intersect.cnsElemList.size();
    } else {
      return -1;
    }
  }

  // Returns specified additional Constraint Elements in the specified IntersectionList

  // Return the total intersection elements in the element
  // Spec list
  public int sz_elemSS_IntsectList() {
    return elemSetSpec.intersectionList.size();
  }

  // Returns Number of Constraint Elements in the
  // specified IntersectionList
  public int sz_elemSS_intersectionConstraintElems(int i) {
    Intersection intersect = get_elemSS_IntsectElem(i);

    if (intersect != null) {
      return intersect.cnsElemList.size();
    } else {
      return -1;
    }
  }
}
