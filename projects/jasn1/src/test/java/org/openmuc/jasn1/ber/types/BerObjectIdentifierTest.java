/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;

public class BerObjectIdentifierTest {

    private final byte[] expectedBytes = new byte[] { 0x06, 0x05, 0x28, (byte) 0xca, 0x22, 0x02, 0x03 };
    private final byte[] expectedBytes2 = new byte[] { 0x06, 0x07, 0x60, (byte) 0x85, 0x74, 0x05, 0x08, 0x01, 0x01 };
    private final int[] objectIdentifierComponents = new int[] { 1, 0, 9506, 2, 3 };

    @Test
    public void explicitEncoding() throws IOException {
        ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

        BerObjectIdentifier oi = new BerObjectIdentifier(objectIdentifierComponents);

        int length = oi.encode(berBAOStream, true);

        Assert.assertEquals(7, length);

        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void explicitDecoding() throws IOException {

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(expectedBytes);
        BerObjectIdentifier oi = new BerObjectIdentifier();

        oi.decode(berInputStream, true);

        Assert.assertArrayEquals(objectIdentifierComponents, oi.value);

        ByteArrayInputStream berInputStream2 = new ByteArrayInputStream(expectedBytes2);
        BerObjectIdentifier oi2 = new BerObjectIdentifier();
        oi2.decode(berInputStream2, true);
    }
}
