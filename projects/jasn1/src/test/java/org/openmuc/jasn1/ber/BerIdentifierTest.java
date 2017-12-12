/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class BerIdentifierTest {

    @Test
    public void twoByteEncoding() throws IOException {
        ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

        BerTag berIdentifier = new BerTag(BerTag.APPLICATION_CLASS, BerTag.PRIMITIVE, 31);

        int length = berIdentifier.encode(berBAOStream);
        Assert.assertEquals(2, length);

        byte[] expectedBytes = new byte[] { 0x5f, 0x1f };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());
    }

}
