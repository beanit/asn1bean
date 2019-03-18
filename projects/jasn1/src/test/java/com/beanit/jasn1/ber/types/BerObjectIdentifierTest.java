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

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class BerObjectIdentifierTest {

  private final byte[] expectedBytes = new byte[] {0x06, 0x05, 0x28, (byte) 0xca, 0x22, 0x02, 0x03};
  private final byte[] expectedBytes2 =
      new byte[] {0x06, 0x07, 0x60, (byte) 0x85, 0x74, 0x05, 0x08, 0x01, 0x01};
  private final int[] objectIdentifierComponents = new int[] {1, 0, 9506, 2, 3};

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
