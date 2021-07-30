/*
 * Copyright 2021 The ASN1bean Authors
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
package com.beanit.asn1bean.ber;

import com.beanit.asn1bean.ber.internal.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DecodeUtil {

  private DecodeUtil() {
    throw new AssertionError();
  }

  public static int decodeUnknownComponent(InputStream is) throws IOException {
    int byteCount = 0;

    BerLength length = new BerLength();
    byteCount += length.decode(is);
    int lengthVal = length.val;

    BerTag berTag = new BerTag();
    if (lengthVal < 0) {
      byteCount += berTag.decode(is);
      while (!berTag.equals(0, 0, 0)) {
        byteCount += decodeUnknownComponent(is);
        byteCount += berTag.decode(is);
      }
      byteCount += BerLength.readEocByte(is);
      return byteCount;
    } else {
      Util.readFullyAndDiscard(is, lengthVal);
      return byteCount + lengthVal;
    }
  }

  public static int decodeUnknownComponent(InputStream is, OutputStream os) throws IOException {
    int byteCount = 0;

    BerLength length = new BerLength();
    byteCount += length.decode(is, os);
    int lengthVal = length.val;

    BerTag berTag = new BerTag();
    if (lengthVal < 0) {
      byteCount += berTag.decode(is, os);
      while (!berTag.equals(0, 0, 0)) {
        byteCount += decodeUnknownComponent(is, os);
        byteCount += berTag.decode(is, os);
      }
      byteCount += BerLength.readEocByte(is, os);
      return byteCount;
    } else {
      byte[] contentBytes = new byte[lengthVal];
      Util.readFully(is, contentBytes);
      os.write(contentBytes);
      return byteCount + lengthVal;
    }
  }
}
