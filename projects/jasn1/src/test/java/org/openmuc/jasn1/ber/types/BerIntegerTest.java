/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;

public class BerIntegerTest {

    public class IntegerUnivPrim extends BerInteger {

        // in the final version identifier needs to be static
        protected final BerTag identifier = new BerTag(BerTag.APPLICATION_CLASS, BerTag.PRIMITIVE, 2);

        IntegerUnivPrim(BigInteger val) {
            super(val);
        }

        @Override
        public int encode(OutputStream berBAOStream, boolean withTag) throws IOException {
            int codeLength = super.encode(berBAOStream, false);
            if (withTag) {
                codeLength += BerLength.encodeLength((BerByteArrayOutputStream)berBAOStream, codeLength);
                codeLength += identifier.encode(berBAOStream);
            }

            return codeLength;
        }

    }

    @Test
    public void encodeDecodeLargeLongs() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);
        BerInteger myInt = new BerInteger(BigInteger.valueOf(20093243433l));
        myInt.encode(berBAOStream, true);

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(berBAOStream.getArray());
        BerInteger myInt2 = new BerInteger();
        myInt2.decode(berInputStream, true);
        Assert.assertEquals(20093243433l, myInt2.value.longValue());
    }

    @Test
    public void encodeDecodeLargeNegativeLongs() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);
        BerInteger myInt = new BerInteger(BigInteger.valueOf(-20093243433l));
        myInt.encode(berBAOStream, true);

        ByteArrayInputStream berInputStream = new ByteArrayInputStream(berBAOStream.getArray());
        BerInteger myInt2 = new BerInteger();
        myInt2.decode(berInputStream, true);
        Assert.assertEquals(-20093243433l, myInt2.value.longValue());
    }

    @Test
    public void implicitEncoding1() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        // 51 is the example in X.690
        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(51));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(2, length);

        byte[] expectedBytes = new byte[] { 0x01, 0x33 };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding2() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(256));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(3, length);

        byte[] expectedBytes = new byte[] { 0x02, 0x01, 0x00 };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding3() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(0));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(2, length);

        byte[] expectedBytes = new byte[] { 0x01, 0x00 };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding4() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(127));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(2, length);

        byte[] expectedBytes = new byte[] { 0x01, 0x7f };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding5() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(128));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(3, length);

        byte[] expectedBytes = new byte[] { 0x02, 0x00, (byte) 0x80 };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding6() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(-128));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(2, length);

        byte[] expectedBytes = new byte[] { 0x01, (byte) 0x80 };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void implicitEncoding7() throws IOException {
        BerByteArrayOutputStream berBAOStream = new BerByteArrayOutputStream(50);

        IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(-129));
        int length = testInteger.encode(berBAOStream, false);
        Assert.assertEquals(3, length);

        byte[] expectedBytes = new byte[] { 0x02, (byte) 0xff, (byte) 0x7f };
        Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());

    }

    @Test
    public void explicitEncoding() throws IOException {
        BerByteArrayOutputStream berStream = new BerByteArrayOutputStream(50);

        // 51 is the example in X.690
        BerInteger testInteger = new BerInteger(BigInteger.valueOf(51));
        int length = testInteger.encode(berStream, true);
        Assert.assertEquals(3, length);

        byte[] expectedBytes = new byte[] { 0x02, 0x01, 0x33 };
        Assert.assertArrayEquals(expectedBytes, berStream.getArray());
    }

    @Test
    public void explicitDecoding() throws IOException {
        byte[] byteCode = new byte[] { 0x02, 0x01, 0x33 };
        ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
        BerInteger asn1Integer = new BerInteger();
        asn1Integer.decode(berInputStream, true);
        Assert.assertEquals(51, asn1Integer.value.intValue());
    }

    @Test
    public void explicitEncoding2() throws IOException {
        BerByteArrayOutputStream berStream = new BerByteArrayOutputStream(50);

        BerInteger testInteger = new BerInteger(BigInteger.valueOf(5555));
        int length = testInteger.encode(berStream, true);
        Assert.assertEquals(4, length);

        byte[] expectedBytes = new byte[] { 0x02, 0x02, 0x15, (byte) 0xb3 };
        Assert.assertArrayEquals(expectedBytes, berStream.getArray());
    }

    @Test
    public void explicitDecoding2() throws IOException {
        byte[] byteCode = new byte[] { 0x02, 0x02, 0x15, (byte) 0xb3 };
        ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
        BerInteger asn1Integer = new BerInteger();
        asn1Integer.decode(berInputStream, true);
        Assert.assertEquals(5555, asn1Integer.value.intValue());
    }

    @Test
    public void explicitDecoding3() throws IOException {
        byte[] byteCode = new byte[] { 0x02, 0x01, (byte) 0xc0 };
        ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
        BerInteger asn1Integer = new BerInteger();
        asn1Integer.decode(berInputStream, true);
        Assert.assertEquals(-64, asn1Integer.value.intValue());
    }

    @Test
    public void explicitDecoding4() throws IOException {
        byte[] byteCode = new byte[] { 0x02, 0x02, (byte) 0xff, 0x01 };
        ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
        BerInteger asn1Integer = new BerInteger();
        asn1Integer.decode(berInputStream, true);
        Assert.assertEquals(-255, asn1Integer.value.intValue());
    }

}
