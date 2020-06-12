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

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class BerRealTest {

  @Test
  public void codingZero() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    BerReal berReal = new BerReal(0);
    berReal.encode(berStream, true);

    assertArrayEquals(HexString.toBytes("0900"), berStream.getArray());

    ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
    BerReal berRealDecoded = new BerReal();
    berRealDecoded.decode(berInputStream, true);

    assertEquals(0, berRealDecoded.value, 0.01);
  }

  @Test
  public void codingNegInf() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    BerReal berReal = new BerReal(Double.NEGATIVE_INFINITY);
    berReal.encode(berStream, true);

    assertArrayEquals(HexString.toBytes("090141"), berStream.getArray());

    ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
    BerReal berRealDecoded = new BerReal();
    berRealDecoded.decode(berInputStream, true);

    assertEquals(Double.NEGATIVE_INFINITY, berRealDecoded.value, 0.01);
  }

  @Test
  public void coding1dot5() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    BerReal berReal = new BerReal(1.5);
    berReal.encode(berStream, true);

    // System.out.println(DatatypeConverter.printHexBinary(berStream.getArray()));
    assertArrayEquals(HexString.toBytes("090380FF03"), berStream.getArray());

    ByteArrayInputStream berInputStream = new ByteArrayInputStream(berStream.getArray());
    BerReal berRealDecoded = new BerReal();
    berRealDecoded.decode(berInputStream, true);

    assertEquals(1.5, berRealDecoded.value, 0.01);
  }

  @Test
  public void coding0dot7() throws IOException {
    final BerReal orig = new BerReal(0.7);
    final ReverseByteArrayOutputStream baos = new ReverseByteArrayOutputStream(100, true);
    orig.encode(baos, true);

    // System.out.println(DatatypeConverter.printHexBinary(baos.getArray()));
    assertArrayEquals(HexString.toBytes("090980CC0B333333333333"), baos.getArray());

    final BerReal decoded = new BerReal();
    decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

    assertEquals(orig.value, decoded.value, 0.001);
  }

  @Test
  public void coding0dot2() throws IOException {
    final BerReal orig = new BerReal(0.2);
    final ReverseByteArrayOutputStream baos = new ReverseByteArrayOutputStream(100, true);
    orig.encode(baos, true);

    final BerReal decoded = new BerReal();
    decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

    assertEquals(orig.value, decoded.value, 0.001);
  }

  @Test
  public void coding1dot0() throws IOException {
    final BerReal orig = new BerReal(1.0);
    final ReverseByteArrayOutputStream baos = new ReverseByteArrayOutputStream(100, true);
    orig.encode(baos, true);

    final BerReal decoded = new BerReal();
    decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

    assertEquals(orig.value, decoded.value, 0.001);
  }

  @Test
  public void coding2dot0() throws IOException {
    final BerReal orig = new BerReal(2.0);
    final ReverseByteArrayOutputStream baos = new ReverseByteArrayOutputStream(100, true);
    orig.encode(baos, true);

    final BerReal decoded = new BerReal();
    decoded.decode(new ByteArrayInputStream(baos.getArray()), true);

    assertEquals(orig.value, decoded.value, 0.001);
  }
}
