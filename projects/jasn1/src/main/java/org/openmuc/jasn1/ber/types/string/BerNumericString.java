/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types.string;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.types.BerOctetString;

public class BerNumericString extends BerOctetString {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.NUMERIC_STRING_TAG);

    public BerNumericString() {
    }

    public BerNumericString(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new String(value);
    }

    @Override
    public int encode(OutputStream os, boolean withTag) throws IOException {

        int codeLength = super.encode(os, false);

        if (withTag) {
            codeLength += tag.encode(os);
        }

        return codeLength;
    }

    @Override
    public int decode(InputStream is, boolean withTag) throws IOException {

        int codeLength = 0;

        if (withTag) {
            codeLength += tag.decodeAndCheck(is);
        }

        codeLength += super.decode(is, false);

        return codeLength;
    }

}
