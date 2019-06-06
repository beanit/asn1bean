/*
 * Copyright 2012 The jASN1 Authors
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
package com.beanit.jasn1.ber;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerLength implements Serializable {

  private static final long serialVersionUID = 1L;

  public int val;

  public BerLength() {}

  public static int skip(InputStream is) throws IOException {

    int val = is.read();
    if (val == -1) {
      throw new EOFException("Unexpected end of input stream.");
    }

    if ((val & 0x80) == 0) {
      return 1;
    }

    int lengthLength = val & 0x7f;

    // check for indefinite length
    if (lengthLength == 0) {
      return 1;
    }

    if (lengthLength > 4) {
      throw new IOException("Length is out of bound!");
    }

    for (int i = 0; i < lengthLength; i++) {
      int nextByte = is.read();
      if (nextByte == -1) {
        throw new EOFException("Unexpected end of input stream.");
      }
    }

    return lengthLength + 1;
  }

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

  public int decode(InputStream is) throws IOException {

    val = is.read();
    if (val == -1) {
      throw new EOFException("Unexpected end of input stream.");
    }

    if ((val & 0x80) == 0) {
      return 1;
    }

    int lengthLength = val & 0x7f;

    // check for indefinite length
    if (lengthLength == 0) {
      val = -1;
      return 1;
    }

    if (lengthLength > 4) {
      throw new IOException("Length is out of bound!");
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
}
