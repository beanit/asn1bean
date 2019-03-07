/*
 * Copyright 2012 The jASN1 Authors
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
package com.beanit.jasn1.ber.types;

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
