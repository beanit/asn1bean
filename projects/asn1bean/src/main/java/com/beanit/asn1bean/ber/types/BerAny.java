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

import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.DecodeUtil;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayOutputStream;
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

    int byteCount = 0;

    ByteArrayOutputStream os = new ByteArrayOutputStream();

    if (tag == null) {
      tag = new BerTag();
      byteCount = tag.decode(is, os);
    } else {
      tag.encode(os);
    }
    byteCount += DecodeUtil.decodeUnknownComponent(is, os);
    value = os.toByteArray();
    return byteCount;
  }

  @Override
  public String toString() {
    return HexString.fromBytes(value);
  }
}
