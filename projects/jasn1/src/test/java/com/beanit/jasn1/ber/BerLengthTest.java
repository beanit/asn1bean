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
package com.beanit.jasn1.ber;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class BerLengthTest {

  @Test
  public void encodeLength() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);

    int codedLength = BerLength.encodeLength(os, 128);

    Assert.assertEquals(2, codedLength);

    byte[] expectedBytes = new byte[] {(byte) 0x81, (byte) 128};

    Assert.assertArrayEquals(expectedBytes, os.getArray());
  }

  @Test
  public void encodeLength2() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);

    int codedLength = BerLength.encodeLength(os, 128);

    Assert.assertEquals(2, codedLength);

    byte[] expectedBytes = new byte[] {(byte) 0x81, (byte) 128};

    Assert.assertArrayEquals(expectedBytes, os.getArray());
  }

  @Test
  public void encodeLength3() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);

    int codedLength = BerLength.encodeLength(os, 65536);

    Assert.assertEquals(4, codedLength);

    byte[] expectedBytes = new byte[] {(byte) 0x83, 1, 0, 0};

    Assert.assertArrayEquals(expectedBytes, os.getArray());
  }

  @Test
  public void encodeLength4() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);

    int codedLength = BerLength.encodeLength(os, 256);

    Assert.assertEquals(3, codedLength);

    byte[] expectedBytes = new byte[] {(byte) 0x82, 1, 0};

    Assert.assertArrayEquals(expectedBytes, os.getArray());
  }

  @Test
  public void encodeLength5() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(50);

    int codedLength = BerLength.encodeLength(os, 16777216);

    Assert.assertEquals(5, codedLength);

    byte[] expectedBytes = new byte[] {(byte) 0x84, 1, 0, 0, 0};

    Assert.assertArrayEquals(expectedBytes, os.getArray());
  }

  @Test
  public void explicitDecoding() throws IOException {
    byte[] byteCode = new byte[] {(byte) 0x81, (byte) 128};
    ByteArrayInputStream berInputStream = new ByteArrayInputStream(byteCode);
    BerLength berLength = new BerLength();
    berLength.decode(berInputStream);
    Assert.assertEquals(128, berLength.val);
  }
}
