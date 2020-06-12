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
package com.beanit.asn1bean.ber.types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beanit.asn1bean.ber.BerLength;
import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class BerIntegerTest {

  @Test
  public void encodeDecodeLargeLongs() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);
    BerInteger myInt = new BerInteger(BigInteger.valueOf(20093243433L));
    myInt.encode(os, true);

    ByteArrayInputStream berInputStream = new ByteArrayInputStream(os.getArray());
    BerInteger myInt2 = new BerInteger();
    myInt2.decode(berInputStream, true);
    assertEquals(20093243433L, myInt2.value.longValue());
  }

  @Test
  public void encodeDecodeLargeNegativeLongs() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);
    BerInteger myInt = new BerInteger(BigInteger.valueOf(-20093243433L));
    myInt.encode(berBAOStream, true);

    ByteArrayInputStream berInputStream = new ByteArrayInputStream(berBAOStream.getArray());
    BerInteger myInt2 = new BerInteger();
    myInt2.decode(berInputStream, true);
    assertEquals(-20093243433L, myInt2.value.longValue());
  }

  @Test
  public void implicitEncoding1() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    // 51 is the example in X.690
    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(51));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(2, length);

    byte[] expectedBytes = new byte[] {0x01, 0x33};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding2() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(256));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(3, length);

    byte[] expectedBytes = new byte[] {0x02, 0x01, 0x00};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding3() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(0));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(2, length);

    byte[] expectedBytes = new byte[] {0x01, 0x00};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding4() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(127));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(2, length);

    byte[] expectedBytes = new byte[] {0x01, 0x7f};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding5() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(128));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(3, length);

    byte[] expectedBytes = new byte[] {0x02, 0x00, (byte) 0x80};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding6() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(-128));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(2, length);

    byte[] expectedBytes = new byte[] {0x01, (byte) 0x80};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void implicitEncoding7() throws IOException {
    ReverseByteArrayOutputStream berBAOStream = new ReverseByteArrayOutputStream(50);

    IntegerUnivPrim testInteger = new IntegerUnivPrim(BigInteger.valueOf(-129));
    int length = testInteger.encode(berBAOStream, false);
    assertEquals(3, length);

    byte[] expectedBytes = new byte[] {0x02, (byte) 0xff, (byte) 0x7f};
    assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }

  @Test
  public void explicitEncoding() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    // 51 is the example in X.690
    BerInteger testInteger = new BerInteger(BigInteger.valueOf(51));
    int length = testInteger.encode(berStream, true);
    assertEquals(3, length);

    byte[] expectedBytes = new byte[] {0x02, 0x01, 0x33};
    assertArrayEquals(expectedBytes, berStream.getArray());
  }

  @Test
  public void explicitDecoding() throws IOException {
    byte[] byteCode = new byte[] {0x02, 0x01, 0x33};
    ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
    BerInteger asn1Integer = new BerInteger();
    asn1Integer.decode(berInputStream, true);
    assertEquals(51, asn1Integer.value.intValue());
  }

  @Test
  public void explicitEncoding2() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    BerInteger testInteger = new BerInteger(BigInteger.valueOf(5555));
    int length = testInteger.encode(berStream, true);
    assertEquals(4, length);

    byte[] expectedBytes = new byte[] {0x02, 0x02, 0x15, (byte) 0xb3};
    assertArrayEquals(expectedBytes, berStream.getArray());
  }

  @Test
  public void explicitDecoding2() throws IOException {
    byte[] byteCode = new byte[] {0x02, 0x02, 0x15, (byte) 0xb3};
    ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
    BerInteger asn1Integer = new BerInteger();
    asn1Integer.decode(berInputStream, true);
    assertEquals(5555, asn1Integer.value.intValue());
  }

  @Test
  public void explicitDecoding3() throws IOException {
    byte[] byteCode = new byte[] {0x02, 0x01, (byte) 0xc0};
    ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
    BerInteger asn1Integer = new BerInteger();
    asn1Integer.decode(berInputStream, true);
    assertEquals(-64, asn1Integer.value.intValue());
  }

  @Test
  public void explicitDecoding4() throws IOException {
    byte[] byteCode = new byte[] {0x02, 0x02, (byte) 0xff, 0x01};
    ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
    BerInteger asn1Integer = new BerInteger();
    asn1Integer.decode(berInputStream, true);
    assertEquals(-255, asn1Integer.value.intValue());
  }

  public static class IntegerUnivPrim extends BerInteger {

    // in the final version identifier needs to be static
    final BerTag identifier = new BerTag(BerTag.APPLICATION_CLASS, BerTag.PRIMITIVE, 2);

    IntegerUnivPrim(BigInteger val) {
      super(val);
    }

    @Override
    public int encode(OutputStream os, boolean withTag) throws IOException {
      int codeLength = super.encode(os, false);
      if (withTag) {
        codeLength += BerLength.encodeLength(os, codeLength);
        codeLength += identifier.encode(os);
      }

      return codeLength;
    }
  }
}
