/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class BerLengthTest {

    @Test
    public void encodeLength() throws IOException {
        BerByteArrayOutputStream os = new BerByteArrayOutputStream(50);

        int codedLength = BerLength.encodeLength(os, 128);

        Assert.assertEquals(2, codedLength);

        byte[] expectedBytes = new byte[] { (byte) 0x81, (byte) 128 };

        Assert.assertArrayEquals(expectedBytes, os.getArray());
    }

    @Test
    public void encodeLength2() throws IOException {
        BerByteArrayOutputStream os = new BerByteArrayOutputStream(50);

        int codedLength = BerLength.encodeLength(os, 128);

        Assert.assertEquals(2, codedLength);

        byte[] expectedBytes = new byte[] { (byte) 0x81, (byte) 128 };

        Assert.assertArrayEquals(expectedBytes, os.getArray());
    }

    @Test
    public void explicitDecoding() throws IOException {
        byte[] byteCode = new byte[] { (byte) 0x81, (byte) 128 };
        ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
        BerLength berLength = new BerLength();
        berLength.decode(berInputStream);
        Assert.assertEquals(128, berLength.val);
    }

}
