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
import com.beanit.asn1bean.util.HexString;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerAny implements Serializable, BerType {

  private static final long serialVersionUID = 1L;

  public byte[] value;

  public BerAny() {}

  public BerAny(byte[] value) {
    this.value = value;
  }

  @Override
  public int encode(OutputStream reverseOS) throws IOException {
    reverseOS.write(value);
    return value.length;
  }

  @Override
  public int decode(InputStream is) throws IOException {

    return decode(is, null);
  }

  public int decode(InputStream is, BerTag tag) throws IOException {

    int decodedLength = 0;

    int tagLength;

    if (tag == null) {
      tag = new BerTag();
      tagLength = tag.decode(is);
      decodedLength += tagLength;
    } else {
      tagLength = tag.encode(new ReverseByteArrayOutputStream(10));
    }

    BerLength lengthField = new BerLength();
    int lengthLength = lengthField.decode(is);
    decodedLength += lengthLength + lengthField.val;

    value = new byte[tagLength + lengthLength + lengthField.val];

    if (lengthFiled.val != 0) {
      Util.readFully(is, value, tagLength + lengthLength, lengthField.val);
    }
    ReverseByteArrayOutputStream os =
        new ReverseByteArrayOutputStream(value, tagLength + lengthLength - 1);
    BerLength.encodeLength(os, lengthField.val);
    tag.encode(os);

    return decodedLength;
  }

  @Override
  public String toString() {
    return HexString.fromBytes(value);
  }
}
