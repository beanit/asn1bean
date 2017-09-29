/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import org.junit.Assert;
import org.junit.Test;

public class BerBitStringTest {

    @Test
    public void toStringTest() {
        BerBitString bitString = new BerBitString(new byte[] { 1, 2, 7 }, 23);
        Assert.assertEquals("00000001000000100000011", bitString.toString());
    }

    @Test
    public void toString2Test() {
        BerBitString bitString = new BerBitString(new boolean[] { false, false, false, false, false, false, false, true,
                false, false, false, false, false, false, true, false, false, false, false, false, false, true, true });
        Assert.assertEquals("00000001000000100000011", bitString.toString());
    }

}
