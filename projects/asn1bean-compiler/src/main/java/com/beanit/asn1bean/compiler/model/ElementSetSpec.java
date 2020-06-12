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

// ~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Iterator;

// ~--- classes ----------------------------------------------------------------

public class ElementSetSpec {
  public ConstraintElements allExceptCnselem;
  public ArrayList intersectionList;
  public boolean isAllExcept;

  // ~--- constructors -------------------------------------------------------

  // Default Constructor
  public ElementSetSpec() {
    intersectionList = new ArrayList();
  }

  // ~--- methods ------------------------------------------------------------

  // toString Method
  @Override
  public String toString() {
    String ts = "";
    Iterator e = intersectionList.iterator();

    if (e != null) {
      while (e.hasNext()) {
        ts += e.next();
        ts += "|";
      }
    }

    if (isAllExcept) {
      ts += "ALL EXCEPT  " + allExceptCnselem;
    }

    return ts;
  }
}
