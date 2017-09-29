/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.internal.Util;

public class BerBitString implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.BIT_STRING_TAG);

    public byte[] code = null;

    public byte[] value = null;
    public int numBits;

    public BerBitString() {
    }

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
                this.value[i / 8] |= (1 << (7 - (i % 8)));
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

    public int encode(BerByteArrayOutputStream os) throws IOException {
        return encode(os, true);
    }

    public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {

        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                os.write(code[i]);
            }
            if (withTag) {
                return tag.encode(os) + code.length;
            }
            return code.length;
        }

        for (int i = (value.length - 1); i >= 0; i--) {
            os.write(value[i]);
        }
        os.write(value.length * 8 - numBits);

        int codeLength = value.length + 1;

        codeLength += BerLength.encodeLength(os, codeLength);

        if (withTag) {
            codeLength += tag.encode(os);
        }

        return codeLength;
    }

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
            }
            else {
                sb.append('0');
            }
        }
        return sb.toString();
    }
}
