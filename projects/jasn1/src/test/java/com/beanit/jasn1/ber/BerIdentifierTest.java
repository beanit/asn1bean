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

    byte[] expectedBytes = new byte[] {0x5f, 0x1f};
    Assert.assertArrayEquals(expectedBytes, berBAOStream.getArray());
  }
}
