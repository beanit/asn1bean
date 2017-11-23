/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.internal.Util;

public class BerReal implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.REAL_TAG);

    public byte[] code = null;

    public double value;

    public BerReal() {
    }

    public BerReal(byte[] code) {
        this.code = code;
    }

    public BerReal(double value) {
        this.value = value;
    }

    public int encode(OutputStream os) throws IOException {
        return encode(os, true);
    }

    public int encode(OutputStream os, boolean withTag) throws IOException {

        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                os.write(code[i]);
            }
            if (withTag) {
                return tag.encode(os) + code.length;
            }
            return code.length;
        }

        int codeLength = encodeValue(os);

        codeLength += BerLength.encodeLength(os, codeLength);

        if (withTag) {
            codeLength += tag.encode(os);
        }

        return codeLength;
    }

    private int encodeValue(OutputStream os) throws IOException {

        // explained in Annex C and Ch. 8.5 of X.690

        // we use binary encoding, with base 2 and F==0
        // F is only needed when encoding with base 8 or 16

        long longBits = Double.doubleToLongBits(value);

        boolean isNegative = (longBits & 0x8000000000000000L) == 0x8000000000000000L;

        int exponent = ((int) (longBits >> 52)) & 0x7ff;

        long mantissa = (longBits & 0x000fffffffffffffL) | 0x0010000000000000L;

        if (exponent == 0x7ff) {
            if (mantissa == 0x0010000000000000L) {
                if (isNegative) {
                    // - infinity
                    os.write(0x41);
                }
                else {
                    // + infinity
                    os.write(0x40);
                }
                return 1;
            }
            else {
                throw new IOException("NAN not supported");
            }
        }

        if ((exponent == 0 && mantissa == 0x0010000000000000L)) {
            // zero
            return 0;
        }

        // because IEEE double-precision format is (-1)^sign * 1.b51b50..b0 * 2^(e-1023) we need to subtract 1023 and 52
        // from the exponent to get an exponent corresponding to an integer matissa as need here.
        exponent -= 1075; // 1023 + 52 = 1075

        // trailing zeros of the mantissa should be removed. Therefor find out how much the mantissa can be shifted and
        // the exponent can be increased
        int exponentIncr = 0;
        while (((mantissa >> exponentIncr) & 0xff) == 0x00) {
            exponentIncr += 8;
        }
        while (((mantissa >> exponentIncr) & 0x01) == 0x00) {
            exponentIncr++;
        }

        exponent += exponentIncr;
        mantissa >>= exponentIncr;

        int mantissaLength = (Long.SIZE - Long.numberOfLeadingZeros(mantissa) + 7) / 8;

        for (int i = 0; i < mantissaLength; i++) {
            os.write((int) (mantissa >> (8 * i)));
        }
        int codeLength = mantissaLength;

        byte[] exponentBytes = BigInteger.valueOf(exponent).toByteArray();
        os.write(exponentBytes);
        codeLength += exponentBytes.length;

        byte exponentFormat = 0;
        if (exponentBytes.length < 4) {
            exponentFormat = (byte) (exponentBytes.length - 1);
        }
        else {
            os.write(exponentBytes.length);
            codeLength++;
            exponentFormat = 0x03;
        }

        if (isNegative) {
            os.write(0x80 | 0x40 | exponentFormat);
        }
        else {
            os.write(0x80 | exponentFormat);
        }

        codeLength++;

        return codeLength;
    }

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
            value = 0;
            return codeLength;
        }

        if (length.val == 1) {
            int nextByte = is.read();
            if (nextByte == -1) {
                throw new EOFException("Unexpected end of input stream.");
            }
            if (nextByte == 0x40) {
                value = Double.POSITIVE_INFINITY;
            }
            else if (nextByte == 0x41) {
                value = Double.NEGATIVE_INFINITY;
            }
            else {
                throw new IOException("invalid real encoding");
            }
            return codeLength + 1;
        }

        byte[] byteCode = new byte[length.val];
        Util.readFully(is, byteCode);

        if ((byteCode[0] & 0x80) != 0x80) {
            throw new IOException("Only binary REAL encoding is supported");
        }

        codeLength += length.val;
        int tempLength = 1;

        int sign = 1;
        if ((byteCode[0] & 0x40) == 0x40) {
            sign = -1;
        }

        int exponentLength = (byteCode[0] & 0x03) + 1;
        if (exponentLength == 4) {
            exponentLength = byteCode[1];
            tempLength++;
        }

        tempLength += exponentLength;

        int exponent = 0;
        for (int i = 0; i < exponentLength; i++) {
            exponent |= byteCode[1 + i] << (8 * (exponentLength - i - 1));
        }

        long mantissa = 0;
        for (int i = 0; i < length.val - tempLength; i++) {
            mantissa |= (byteCode[i + tempLength] & 0xffL) << (8 * (length.val - tempLength - i - 1));
        }

        value = sign * mantissa * Math.pow(2, exponent);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        BerByteArrayOutputStream os = new BerByteArrayOutputStream(encodingSizeGuess);
        encode(os, false);
        code = os.getArray();
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
