/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.internal.Util;
import org.openmuc.jasn1.util.HexConverter;

public class BerAny implements Serializable {

    private static final long serialVersionUID = 1L;

    public byte[] value;

    public BerAny() {
    }

    public BerAny(byte[] value) {
        this.value = value;
    }

    public int encode(OutputStream os) throws IOException {
        os.write(value);
        return value.length;
    }

    public int decode(InputStream is) throws IOException {

        return decode(is, null);
    }

    public int decode(InputStream is, BerTag tag) throws IOException {

        int decodedLength = 0;

        int tagLength;

        if (tag == null) {
            tag = new BerTag();
            tagLength = tag.decode(is);
            decodedLength += tagLength;
        }
        else {
            tagLength = tag.encode(new ReverseByteArrayOutputStream(10));
        }

        BerLength lengthField = new BerLength();
        int lengthLength = lengthField.decode(is);
        decodedLength += lengthLength + lengthField.val;

        value = new byte[tagLength + lengthLength + lengthField.val];

        Util.readFully(is, value, tagLength + lengthLength, lengthField.val);
        ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(value, tagLength + lengthLength - 1);
        BerLength.encodeLength(os, lengthField.val);
        tag.encode(os);

        return decodedLength;
    }

    @Override
    public String toString() {
        return HexConverter.toShortHexString(value);
    }

}
