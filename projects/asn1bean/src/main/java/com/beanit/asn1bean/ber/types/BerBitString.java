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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerBitString implements Serializable, BerType {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.BIT_STRING_TAG);
  private static final long serialVersionUID = 1L;
  public byte[] value = null;
  public int numBits;
  private byte[] code = null;

  public BerBitString() {}

  public BerBitString(byte[] value, int numBits) {

    if (value == null) {
      throw new NullPointerException("value cannot be null");
    }
    if (numBits < 0) {
      throw new IllegalArgumentException("numBits cannot be negative.");
    }
    if (numBits > (value.length * 8)) {
      throw new IllegalArgumentException("'value' is too short to hold all bits.");
    }

    this.value = value;
    this.numBits = numBits;
  }

  public BerBitString(boolean[] value) {

    if (value == null) {
      throw new NullPointerException("value cannot be null");
    }

    numBits = value.length;
    this.value = new byte[(numBits + 7) / 8];
    for (int i = 0; i < numBits; i++) {
      if (value[i]) {
        this.value[i / 8] = (byte) (this.value[i / 8] | (1 << (7 - (i % 8))));
      }
    }
  }

  public BerBitString(byte[] code) {
    this.code = code;
  }

  public boolean[] getValueAsBooleans() {
    if (value == null) {
      return null;
    }

    boolean[] booleans = new boolean[numBits];
    for (int i = 0; i < numBits; i++) {
      booleans[i] = ((value[i / 8] & (1 << (7 - (i % 8)))) > 0);
    }
    return booleans;
  }

  @Override
  public int encode(OutputStream reverseOS) throws IOException {
    return encode(reverseOS, true);
  }

  public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

    if (code != null) {
      reverseOS.write(code);
      if (withTag) {
        return tag.encode(reverseOS) + code.length;
      }
      return code.length;
    }

    for (int i = (value.length - 1); i >= 0; i--) {
      reverseOS.write(value[i]);
    }
    reverseOS.write(value.length * 8 - numBits);

    int codeLength = value.length + 1;

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
    // could be encoded in primitiv and constructed mode
    // only primitiv mode is implemented

    int codeLength = 0;

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    value = new byte[length.val - 1];

    int unusedBits = is.read();
    if (unusedBits == -1) {
      throw new EOFException("Unexpected end of input stream.");
    }
    if (unusedBits > 7) {
      throw new IOException(
          "Number of unused bits in bit string expected to be less than 8 but is: " + unusedBits);
    }

    numBits = (value.length * 8) - unusedBits;

    if (value.length > 0) {
      Util.readFully(is, value);
    }

    codeLength += value.length + 1;

    return codeLength;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (boolean bit : getValueAsBooleans()) {
      if (bit) {
        sb.append('1');
      } else {
        sb.append('0');
      }
    }
    return sb.toString();
  }
}
