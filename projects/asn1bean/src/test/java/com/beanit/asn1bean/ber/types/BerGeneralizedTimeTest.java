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
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class BerGeneralizedTimeTest {

  @Test
  public void explicitEncoding() throws IOException {
    ReverseByteArrayOutputStream berStream = new ReverseByteArrayOutputStream(50);

    byte[] byteArray = new byte[] {0x01, 0x02, 0x03};
    BerGeneralizedTime berGeneralizedTime = new BerGeneralizedTime(byteArray);
    int length = berGeneralizedTime.encode(berStream, true);
    assertEquals(5, length);

    byte[] expectedBytes = new byte[] {24, 0x03, 0x01, 0x02, 0x03};
    assertArrayEquals(expectedBytes, berStream.getArray());
  }
}
