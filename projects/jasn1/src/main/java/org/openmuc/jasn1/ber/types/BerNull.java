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

public class BerNull implements Serializable, BerType {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.NULL_TAG);

    public byte[] code = null;

    public BerNull() {
    }

    public BerNull(byte[] code) {
    }

    @Override
    public int encode(OutputStream reverseOS) throws IOException {
        return encode(reverseOS, true);
    }

    public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        int codeLength = BerLength.encodeLength(reverseOS, 0);

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

        if (length.val != 0) {
            throw new IOException("Decoded length of BerNull is not correct");
        }

        return codeLength;
    }

    @Override
    public String toString() {
        return "ASN1_NULL";
    }

}
