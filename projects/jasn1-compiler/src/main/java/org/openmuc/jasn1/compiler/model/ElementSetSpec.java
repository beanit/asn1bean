/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Iterator;

//~--- classes ----------------------------------------------------------------

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
