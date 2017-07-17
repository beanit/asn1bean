/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler.model;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;

public class BerLength {

    public int val;

    public BerLength() {
    }

    public int decode(InputStream is) throws IOException {

        val = is.read();
        if (val == -1) {
            throw new EOFException("Unexpected end of input stream.");
        }
        int length = 1;

        if ((val & 0x80) != 0) {
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

            length += lengthLength;

            for (int i = 0; i < lengthLength; i++) {
                int nextByte = is.read();
                if (nextByte == -1) {
                    throw new EOFException("Unexpected end of input stream.");
                }
                val |= nextByte << (8 * (lengthLength - i - 1));
            }
        }

        return length;
    }

    public static int encodeLength(BerByteArrayOutputStream os, int length) throws IOException {
        // the indefinite form is ignored for now

        if (length <= 127) {
            // this is the short form, it is coded differently than the long
            // form for values > 127
            os.write((byte) length);
            return 1;
        }
        else {
            int numLengthBytes = 1;

            while (((int) (Math.pow(2, 8 * numLengthBytes) - 1)) < length) {
                numLengthBytes++;
            }

            for (int i = 0; i < numLengthBytes; i++) {
                os.write((length >> 8 * i) & 0xff);
            }

            os.write(0x80 | numLengthBytes);

            return 1 + numLengthBytes;
        }

    }

}
