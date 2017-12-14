/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.internal.Util;
import org.openmuc.jasn1.util.HexConverter;

public class BerOctetString implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.OCTET_STRING_TAG);

    public byte[] value;

    public BerOctetString() {
    }

    public BerOctetString(byte[] value) {
        this.value = value;
    }

    public int encode(OutputStream reverseOS) throws IOException {
        return encode(reverseOS, true);
    }

    public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        reverseOS.write(value);
        int codeLength = value.length;

        codeLength += BerLength.encodeLength(reverseOS, codeLength);

        if (withTag) {
            codeLength += tag.encode(reverseOS);
        }

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

        value = new byte[length.val];

        if (length.val != 0) {
            Util.readFully(is, value);
            codeLength += length.val;
        }

        return codeLength;

    }

    @Override
    public String toString() {
        return HexConverter.toShortHexString(value);
    }

}
