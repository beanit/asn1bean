package com.beanit.asn1bean.ber;

import com.beanit.asn1bean.ber.internal.Util;
import java.io.IOException;
import java.io.InputStream;

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
}
