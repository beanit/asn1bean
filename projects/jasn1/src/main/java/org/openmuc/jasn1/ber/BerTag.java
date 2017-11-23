/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerTag implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int UNIVERSAL_CLASS = 0x00;
    public static final int APPLICATION_CLASS = 0x40;
    public static final int CONTEXT_CLASS = 0x80;
    public static final int PRIVATE_CLASS = 0xc0;

    public static final int PRIMITIVE = 0x00;
    public static final int CONSTRUCTED = 0x20;

    public static final int BOOLEAN_TAG = 1;
    public static final int INTEGER_TAG = 2;
    public static final int BIT_STRING_TAG = 3;
    public static final int OCTET_STRING_TAG = 4;
    public static final int NULL_TAG = 5;
    public static final int OBJECT_IDENTIFIER_TAG = 6;
    public static final int OBJECT_DESCRIPTOR_TAG = 7;
    public static final int REAL_TAG = 9;
    public static final int ENUMERATED_TAG = 10;
    public static final int EMBEDDED_PDV_TAG = 11;
    public static final int UTF8_STRING_TAG = 12;
    public static final int TIME_TAG = 14;
    public static final int NUMERIC_STRING_TAG = 18;
    public static final int PRINTABLE_STRING_TAG = 19;
    public static final int TELETEX_STRING_TAG = 20;
    public static final int VIDEOTEX_STRING_TAG = 21;
    public static final int IA5_STRING_TAG = 22;
    public static final int UTC_TIME_TAG = 23;
    public static final int GENERALIZED_TIME_TAG = 24;
    public static final int GRAPHIC_STRING_TAG = 25;
    public static final int VISIBLE_STRING_TAG = 26;
    public static final int GENERAL_STRING_TAG = 27;
    public static final int UNIVERSAL_STRING_TAG = 28;
    public static final int BMP_STRING_TAG = 30;
    public static final int DATE_TAG = 31;
    public static final int TIME_OF_DAY_TAG = 32;
    public static final int DATE_TIME_TAG = 33;
    public static final int DURATION_TAG = 34;

    public byte[] tagBytes = null;
    public int tagClass;
    public int primitive;
    public int tagNumber;

    public BerTag(int identifierClass, int primitive, int tagNumber) {
        this.tagClass = identifierClass;
        this.primitive = primitive;
        this.tagNumber = tagNumber;
        code();
    }

    public BerTag() {
    }

    private void code() {
        if (tagNumber < 31) {
            tagBytes = new byte[1];
            tagBytes[0] = (byte) (tagClass | primitive | tagNumber);
        }
        else {
            int tagLength = 1;
            while (tagNumber > (Math.pow(2, (7 * tagLength)) - 1)) {
                tagLength++;
            }

            tagBytes = new byte[1 + tagLength];
            tagBytes[0] = (byte) (tagClass | primitive | 31);

            for (int j = 1; j <= (tagLength - 1); j++) {
                tagBytes[j] = (byte) (((tagNumber >> (7 * (tagLength - j))) & 0xff) | 0x80);
            }

            tagBytes[tagLength] = (byte) (tagNumber & 0x7f);

        }
    }

    public int encode(OutputStream os) throws IOException {
        if (tagBytes == null) {
            code();
        }
        for (int i = (tagBytes.length - 1); i >= 0; i--) {
            os.write(tagBytes[i]);
        }
        return tagBytes.length;
    }

    public int decode(InputStream is) throws IOException {
        int nextByte = is.read();
        if (nextByte == -1) {
            throw new EOFException("Unexpected end of input stream.");
        }

        tagClass = nextByte & 0xC0;
        primitive = nextByte & 0x20;
        tagNumber = nextByte & 0x1f;

        int codeLength = 1;

        if (tagNumber == 0x1f) {
            tagNumber = 0;

            int counter = 0;

            do {
                nextByte = is.read();
                if (nextByte == -1) {
                    throw new EOFException("Unexpected end of input stream.");
                }

                codeLength++;
                if (counter >= 6) {
                    throw new IOException("Invalid Tag");
                }
                tagNumber = tagNumber << 7;
                tagNumber |= (nextByte & 0x7f);
                counter++;
            } while ((nextByte & 0x80) == 0x80);

        }

        return codeLength;
    }

    /**
     * Decodes the Identifier from the ByteArrayInputStream and throws an Exception if it is not equal to itself.
     * Returns the number of bytes read from the InputStream.
     * 
     * @param is
     *            the input stream to read the identifier from.
     * @return the length of the identifier read.
     * @throws IOException
     *             if an exception occurs reading the identifier from the stream.
     */
    public int decodeAndCheck(InputStream is) throws IOException {

        for (Byte identifierByte : tagBytes) {
            int nextByte = is.read();
            if (nextByte == -1) {
                throw new EOFException("Unexpected end of input stream.");
            }

            if (nextByte != (identifierByte & 0xff)) {
                throw new IOException("Identifier does not match!");
            }
        }
        return tagBytes.length;
    }

    public boolean equals(int identifierClass, int primitive, int tagNumber) {
        return (this.tagNumber == tagNumber && this.tagClass == identifierClass && this.primitive == primitive);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BerTag)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        BerTag berIdentifier = (BerTag) obj;
        return (tagNumber == berIdentifier.tagNumber && tagClass == berIdentifier.tagClass
                && primitive == berIdentifier.primitive);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + tagNumber;
        hash = hash * 31 + tagClass;
        hash = hash * 31 + primitive;
        return hash;
    }

    @Override
    public String toString() {
        return "identifier class: " + tagClass + ", primitive: " + primitive + ", tag number: " + tagNumber;
    }

}
