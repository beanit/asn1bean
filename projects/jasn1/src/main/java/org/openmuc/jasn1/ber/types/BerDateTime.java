/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.openmuc.jasn1.ber.BerTag;

public class BerDateTime extends BerTime {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.DATE_TIME_TAG);

    public BerDateTime() {
    }

    public BerDateTime(byte[] value) {
        super(value);
    }

    public BerDateTime(String valueAsString) throws UnsupportedEncodingException {
        super(valueAsString);
    }

    @Override
    public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        int codeLength = super.encode(reverseOS, false);

        if (withTag) {
            codeLength += tag.encode(reverseOS);
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
