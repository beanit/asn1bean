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
package com.beanit.asn1bean.ber;

import com.beanit.asn1bean.util.HexString;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerLength implements Serializable {

  private static final long serialVersionUID = 1L;

  public int val;

  public BerLength() {}

  public static int encodeLength(OutputStream reverseOS, int length) throws IOException {

    if (length <= 127) {
      // this is the short form
      reverseOS.write(length);
      return 1;
    }

    if (length <= 255) {
      reverseOS.write(length);
      reverseOS.write(0x81);
      return 2;
    }

    if (length <= 65535) {
      reverseOS.write(length);
      reverseOS.write(length >> 8);
      reverseOS.write(0x82);
      return 3;
    }

    if (length <= 16777215) {
      reverseOS.write(length);
      reverseOS.write(length >> 8);
      reverseOS.write(length >> 16);
      reverseOS.write(0x83);
      return 4;
    }

    int numLengthBytes = 1;
    while (((int) (Math.pow(2, 8 * numLengthBytes) - 1)) < length) {
      numLengthBytes++;
    }

    for (int i = 0; i < numLengthBytes; i++) {
      reverseOS.write(length >> (8 * i));
    }
    reverseOS.write(0x80 | numLengthBytes);

    return 1 + numLengthBytes;
  }

  public static int readEocByte(InputStream is) throws IOException {
    int b = is.read();
    if (b != 0) {
      if (b == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
      throw new IOException(
          "Byte " + HexString.fromByte(b) + " does not match end of contents octet of zero.");
    }
    return 1;
  }

  public static int readEocByte(InputStream is, OutputStream os) throws IOException {
    int b = is.read();
    if (b != 0) {
      if (b == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
      os.write(b);
      throw new IOException(
          "Byte " + HexString.fromByte(b) + " does not match end of contents octet of zero.");
    }
    os.write(b);
    return 1;
  }

  public int decode(InputStream is) throws IOException {

    val = is.read();
    // check for short form
    if (val < 128) {
      if (val == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
      return 1;
    }

    int lengthLength = val & 0x7f;
    // check for indefinite length
    if (lengthLength == 0) {
      val = -1;
      return 1;
    }

    if (lengthLength > 4) {
      throw new IOException("Length is out of bounds: " + lengthLength);
    }

    val = 0;
    for (int i = 0; i < lengthLength; i++) {
      int nextByte = is.read();
      if (nextByte == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
      val |= nextByte << (8 * (lengthLength - i - 1));
    }

    return lengthLength + 1;
  }

  public int decode(InputStream is, OutputStream os) throws IOException {

    val = is.read();
    if (val == -1) {
      throw new EOFException("Unexpected end of input stream.");
    }
    os.write(val);

    // check for short form
    if (val < 128) {
      return 1;
    }

    int lengthLength = val & 0x7f;
    // check for indefinite length
    if (lengthLength == 0) {
      val = -1;
      return 1;
    }

    if (lengthLength > 4) {
      throw new IOException("Length is out of bounds: " + lengthLength);
    }

    val = 0;
    for (int i = 0; i < lengthLength; i++) {
      int nextByte = is.read();
      if (nextByte == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
      os.write(nextByte);
      val |= nextByte << (8 * (lengthLength - i - 1));
    }

    return lengthLength + 1;
  }

  /**
   * Reads the end of contents octets from the given input stream if this length object has the
   * indefinite form.
   *
   * @param is the input stream
   * @return the number of bytes read from the input stream
   * @throws IOException if an error occurs while reading from the input stream
   */
  public int readEocIfIndefinite(InputStream is) throws IOException {
    if (val >= 0) {
      return 0;
    }
    readEocByte(is);
    readEocByte(is);
    return 2;
  }
}
