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
package com.beanit.asn1bean.ber.types.string;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.asn1bean.ber.BerLength;
import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.internal.Util;
import com.beanit.asn1bean.ber.types.BerType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerVisibleString implements Serializable, BerType {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.VISIBLE_STRING_TAG);
  private static final long serialVersionUID = 1L;
  public byte[] value;

  public BerVisibleString() {}

  public BerVisibleString(byte[] value) {
    this.value = value;
  }

  public BerVisibleString(String valueAsString) {
    value = valueAsString.getBytes(UTF_8);
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

    int codeLength = 0;

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

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
    return new String(value, UTF_8);
  }
}
