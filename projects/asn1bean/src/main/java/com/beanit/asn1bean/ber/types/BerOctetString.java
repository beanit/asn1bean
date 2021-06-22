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

import com.beanit.asn1bean.ber.BerLength;
import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.internal.Util;
import com.beanit.asn1bean.util.HexString;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerOctetString implements Serializable, BerType {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.OCTET_STRING_TAG);
  private static final long serialVersionUID = 1L;
  public byte[] value;

  public BerOctetString() {}

  public BerOctetString(byte[] value) {
    this.value = value;
  }

  @Override
  public int encode(OutputStream reverseOS) throws IOException {
    return encode(reverseOS, true);
  }

  public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

    reverseOS.write(value);
    int codeLength = value.length;

    codeLength += BerLength.encodeLength(reverseOS, codeLength);

    if (withTag) {
      codeLength += tag.encode(reverseOS);
    }

    return codeLength;
  }

  @Override
  public int decode(InputStream is) throws IOException {
    return decode(is, true);
  }

  public int decode(InputStream is, boolean withTag) throws IOException {

    if (withTag) {
      int nextByte = is.read();
      switch (nextByte) {
        case -1:
          throw new EOFException("Unexpected end of input stream.");
        case 0x04:
          return 1 + decodePrimitiveOctetString(is);
        case 0x24:
          return 1 + decodeConstructedOctetString(is);
        default:
          throw new IOException(
              "Octet String identifier does not match, expected: 0x04 or 0x24, received: 0x"
                  + HexString.fromByte((byte) nextByte));
      }
    }
    return decodePrimitiveOctetString(is);
  }

  private int decodeConstructedOctetString(InputStream is) throws IOException {

    BerLength length = new BerLength();
    int lengthLength = length.decode(is);

    value = new byte[0];
    int vLength = 0;

    if (length.val < 0) {
      BerTag berTag = new BerTag();
      vLength += berTag.decode(is);
      while (!berTag.equals(0, 0, 0)) {
        BerOctetString subOctetString = new BerOctetString();
        vLength += subOctetString.decode(is, false);
        value = concatenate(value, subOctetString.value);
        vLength += berTag.decode(is);
      }
      vLength += BerLength.readEocByte(is);
    } else {
      while (vLength < length.val) {
        BerOctetString subOctetString = new BerOctetString();
        vLength += subOctetString.decode(is);
        value = concatenate(value, subOctetString.value);
      }
    }
    return lengthLength + vLength;
  }

  private byte[] concatenate(byte[] a, byte[] b) {
    int aLen = a.length;
    int bLen = b.length;

    byte[] c = new byte[aLen + bLen];
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
  }

  private int decodePrimitiveOctetString(InputStream is) throws IOException {
    int codeLength = 0;

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    value = new byte[length.val];

    if (length.val != 0) {
      Util.readFully(is, value);
      codeLength += length.val;
    }

    return codeLength;
  }

  @Override
  public String toString() {
    return HexString.fromBytes(value);
  }
}
