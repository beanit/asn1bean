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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BerObjectIdentifier implements Serializable, BerType {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.OBJECT_IDENTIFIER_TAG);
  private static final long serialVersionUID = 1L;
  public int[] value;
  private byte[] code = null;

  public BerObjectIdentifier() {}

  public BerObjectIdentifier(byte[] code) {
    this.code = code;
  }

  public BerObjectIdentifier(int[] value) {
    if ((value.length < 2)
        || ((value[0] == 0 || value[0] == 1) && (value[1] > 39))
        || value[0] > 2) {
      throw new IllegalArgumentException("invalid object identifier components");
    }
    for (int objectIdentifierComponent : value) {
      if (objectIdentifierComponent < 0) {
        throw new IllegalArgumentException("invalid object identifier components");
      }
    }

    this.value = value;
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

    int firstSubidentifier = 40 * value[0] + value[1];

    int subidentifier;

    int codeLength = 0;

    for (int i = (value.length - 1); i > 0; i--) {

      if (i == 1) {
        subidentifier = firstSubidentifier;
      } else {
        subidentifier = value[i];
      }

      // get length of subidentifier
      int subIDLength = 1;
      while (subidentifier > (Math.pow(2, (7 * subIDLength)) - 1)) {
        subIDLength++;
      }

      reverseOS.write(subidentifier & 0x7f);

      for (int j = 1; j <= (subIDLength - 1); j++) {
        reverseOS.write(((subidentifier >> (7 * j)) & 0xff) | 0x80);
      }

      codeLength += subIDLength;
    }

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

    if (length.val == 0) {
      value = new int[0];
      return codeLength;
    }

    byte[] byteCode = new byte[length.val];
    Util.readFully(is, byteCode);

    codeLength += length.val;

    List<Integer> objectIdentifierComponentsList = new ArrayList<>();

    int subIDEndIndex = 0;
    while ((byteCode[subIDEndIndex] & 0x80) == 0x80) {
      if (subIDEndIndex >= (length.val - 1)) {
        throw new IOException("Invalid Object Identifier");
      }
      subIDEndIndex++;
    }

    int subidentifier = 0;
    for (int i = 0; i <= subIDEndIndex; i++) {
      subidentifier |= ((byteCode[i] & 0x7f) << ((subIDEndIndex - i) * 7));
    }

    if (subidentifier < 40) {
      objectIdentifierComponentsList.add(0);
      objectIdentifierComponentsList.add(subidentifier);
    } else if (subidentifier < 80) {
      objectIdentifierComponentsList.add(1);
      objectIdentifierComponentsList.add(subidentifier - 40);
    } else {
      objectIdentifierComponentsList.add(2);
      objectIdentifierComponentsList.add(subidentifier - 80);
    }

    subIDEndIndex++;

    while (subIDEndIndex < length.val) {
      int subIDStartIndex = subIDEndIndex;

      while ((byteCode[subIDEndIndex] & 0x80) == 0x80) {
        if (subIDEndIndex == (length.val - 1)) {
          throw new IOException("Invalid Object Identifier");
        }
        subIDEndIndex++;
      }
      subidentifier = 0;
      for (int j = subIDStartIndex; j <= subIDEndIndex; j++) {
        subidentifier |= ((byteCode[j] & 0x7f) << ((subIDEndIndex - j) * 7));
      }
      objectIdentifierComponentsList.add(subidentifier);
      subIDEndIndex++;
    }

    value = new int[objectIdentifierComponentsList.size()];
    for (int i = 0; i < objectIdentifierComponentsList.size(); i++) {
      value[i] = objectIdentifierComponentsList.get(i);
    }

    return codeLength;
  }

  @Override
  public String toString() {
    if (value == null || value.length == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder(Integer.toString(value[0]));
    for (int i = 1; i < value.length; i++) {
      sb.append(".").append(value[i]);
    }
    return sb.toString();
  }
}
