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
import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.internal.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;

public class BerInteger implements Serializable, BerType {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.INTEGER_TAG);
  private static final long serialVersionUID = 1L;
  public BigInteger value;
  private byte[] code = null;

  public BerInteger() {}

  public BerInteger(byte[] code) {
    this.code = code;
  }

  public BerInteger(BigInteger val) {
    this.value = val;
  }

  public BerInteger(long val) {
    this.value = BigInteger.valueOf(val);
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

    byte[] encoded = value.toByteArray();
    int codeLength = encoded.length;
    reverseOS.write(encoded);

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

    int codeLength = 0;

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    if (length.val < 1) {
      throw new IOException("Decoded length of BerInteger is not correct");
    }

    byte[] byteCode = new byte[length.val];
    Util.readFully(is, byteCode);

    codeLength += length.val;

    value = new BigInteger(byteCode);

    return codeLength;
  }

  public void encodeAndSave(int encodingSizeGuess) throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(encodingSizeGuess);
    encode(os, false);
    code = os.getArray();
  }

  @Override
  public String toString() {
    return "" + value;
  }

  public byte byteValue() {
    return value.byteValue();
  }

  public short shortValue() {
    return value.shortValue();
  }

  public int intValue() {
    return value.intValue();
  }

  public long longValue() {
    return value.longValue();
  }
}
