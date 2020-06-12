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

import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ReverseByteArrayOutputStream extends OutputStream {

  private final boolean automaticResize;
  public byte[] buffer;
  public int index;

  /**
   * Creates a <code>ReverseByteArrayOutputStream</code> with a byte array of size <code>bufferSize
   * </code>. The buffer will not be resized automatically. Use {@link
   * #ReverseByteArrayOutputStream(int, boolean)} instead if you want the buffer to be dynamically
   * resized.
   *
   * @param bufferSize the size of the underlying buffer
   */
  public ReverseByteArrayOutputStream(int bufferSize) {
    this(new byte[bufferSize], bufferSize - 1, false);
  }

  public ReverseByteArrayOutputStream(int bufferSize, boolean automaticResize) {
    this(new byte[bufferSize], bufferSize - 1, automaticResize);
  }

  public ReverseByteArrayOutputStream(byte[] buffer) {
    this(buffer, buffer.length - 1, false);
  }

  public ReverseByteArrayOutputStream(byte[] buffer, int startingIndex) {
    this(buffer, startingIndex, false);
  }

  public ReverseByteArrayOutputStream(byte[] buffer, int startingIndex, boolean automaticResize) {
    if (buffer.length <= 0) {
      throw new IllegalArgumentException("buffer size may not be <= 0");
    }
    this.buffer = buffer;
    index = startingIndex;
    this.automaticResize = automaticResize;
  }

  @Override
  public void write(int arg0) {
    write((byte) arg0);
  }

  public void write(byte arg0) {
    try {
      buffer[index] = arg0;
    } catch (ArrayIndexOutOfBoundsException e) {
      if (automaticResize) {
        resize();
        buffer[index] = arg0;
      } else {
        throw new ArrayIndexOutOfBoundsException("buffer.length = " + buffer.length);
      }
    }
    index--;
  }

  private void resize() {
    byte[] newBuffer = new byte[buffer.length * 2];
    System.arraycopy(
        buffer, index + 1, newBuffer, buffer.length + index + 1, buffer.length - index - 1);
    index += buffer.length;
    buffer = newBuffer;
  }

  @Override
  public void write(byte[] byteArray) {
    // check if there is enough space - resize otherwise
    while (index + 1 - byteArray.length < 0) {
      if (automaticResize) {
        resize();
      } else {
        throw new ArrayIndexOutOfBoundsException("buffer.length = " + buffer.length);
      }
    }

    System.arraycopy(byteArray, 0, buffer, index - byteArray.length + 1, byteArray.length);
    index -= byteArray.length;
  }

  /**
   * Returns a new array containing the subarray of the stream array that contains the coded
   * content.
   *
   * @return a new array containing the subarray of the stream array
   */
  public byte[] getArray() {
    if (index == -1) {
      return buffer;
    }
    int subBufferLength = buffer.length - index - 1;
    byte[] subBuffer = new byte[subBufferLength];
    System.arraycopy(buffer, index + 1, subBuffer, 0, subBufferLength);
    return subBuffer;
  }

  public ByteBuffer getByteBuffer() {
    return ByteBuffer.wrap(buffer, index + 1, buffer.length - (index + 1));
  }

  public void reset() {
    index = buffer.length - 1;
  }
}
