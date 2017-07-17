/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;

public class BerRealTest {

    @Test
    public void codingZero() throws IOException {
        BerByteArrayOutputStream berStream = new BerByteArrayOutputStream(50);

        BerReal berReal = new BerReal(0);
        berReal.encode(berStream, true);

        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("0900"), berStream.getArray());

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
        BerReal berRealDecoded = new BerReal();
        berRealDecoded.decode(berInputStream, true);

        Assert.assertEquals(0, berRealDecoded.value, 0.01);
    }

    @Test
    public void codingNegInf() throws IOException {
        BerByteArrayOutputStream berStream = new BerByteArrayOutputStream(50);

        BerReal berReal = new BerReal(Double.NEGATIVE_INFINITY);
        berReal.encode(berStream, true);

        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("090141"), berStream.getArray());

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
        BerReal berRealDecoded = new BerReal();
        berRealDecoded.decode(berInputStream, true);

        Assert.assertEquals(Double.NEGATIVE_INFINITY, berRealDecoded.value, 0.01);
    }

    @Test
    public void coding1dot5() throws IOException {
        BerByteArrayOutputStream berStream = new BerByteArrayOutputStream(50);

        BerReal berReal = new BerReal(1.5);
        berReal.encode(berStream, true);

        // System.out.println(DatatypeConverter.printHexBinary(berStream.getArray()));
        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("090380FF03"), berStream.getArray());

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
        BerReal berRealDecoded = new BerReal();
        berRealDecoded.decode(berInputStream, true);

        Assert.assertEquals(1.5, berRealDecoded.value, 0.01);
    }

    @Test
    public void coding0dot7() throws IOException {
        final BerReal orig = new BerReal(0.7);
        final BerByteArrayOutputStream baos = new BerByteArrayOutputStream(100, true);
        orig.encode(baos, true);

        // System.out.println(DatatypeConverter.printHexBinary(baos.getArray()));
        Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("090980CC0B333333333333"), baos.getArray());

        final BerReal decoded = new BerReal();
        decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

        Assert.assertEquals(orig.value, decoded.value, 0.001);
    }

    @Test
    public void coding0dot2() throws IOException {
        final BerReal orig = new BerReal(0.2);
        final BerByteArrayOutputStream baos = new BerByteArrayOutputStream(100, true);
        orig.encode(baos, true);

        final BerReal decoded = new BerReal();
        decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

        Assert.assertEquals(orig.value, decoded.value, 0.001);
    }

    @Test
    public void coding1dot0() throws IOException {
        final BerReal orig = new BerReal(1.0);
        final BerByteArrayOutputStream baos = new BerByteArrayOutputStream(100, true);
        orig.encode(baos, true);

        final BerReal decoded = new BerReal();
        decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

        Assert.assertEquals(orig.value, decoded.value, 0.001);
    }

    @Test
    public void coding2dot0() throws IOException {
        final BerReal orig = new BerReal(2.0);
        final BerByteArrayOutputStream baos = new BerByteArrayOutputStream(100, true);
        orig.encode(baos, true);

        final BerReal decoded = new BerReal();
        decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

        Assert.assertEquals(orig.value, decoded.value, 0.001);
    }

}
