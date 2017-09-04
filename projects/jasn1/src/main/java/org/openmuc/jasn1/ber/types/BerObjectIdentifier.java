/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.internal.Util;

public class BerObjectIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.OBJECT_IDENTIFIER_TAG);

    public byte[] code = null;

    public int[] value;

    public BerObjectIdentifier() {
    }

    public BerObjectIdentifier(byte[] code) {
        this.code = code;
    }

    public BerObjectIdentifier(int[] value) {
        if ((value.length < 2) || ((value[0] == 0 || value[0] == 1) && (value[1] > 39)) || value[0] > 2) {
            throw new IllegalArgumentException("invalid object identifier components");
        }
        for (int objectIdentifierComponent : value) {
            if (objectIdentifierComponent < 0) {
                throw new IllegalArgumentException("invalid object identifier components");
            }
        }

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

        int firstSubidentifier = 40 * value[0] + value[1];

        int subidentifier;

        int codeLength = 0;

        for (int i = (value.length - 1); i > 0; i--) {

            if (i == 1) {
                subidentifier = firstSubidentifier;
            }
            else {
                subidentifier = value[i];
            }

            // get length of subidentifier
            int subIDLength = 1;
            while (subidentifier > (Math.pow(2, (7 * subIDLength)) - 1)) {
                subIDLength++;
            }

            os.write(subidentifier & 0x7f);

            for (int j = 1; j <= (subIDLength - 1); j++) {
                os.write(((subidentifier >> (7 * j)) & 0xff) | 0x80);
            }

            codeLength += subIDLength;
        }

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

        int codeLength = 0;

        if (withTag) {
            codeLength += tag.decodeAndCheck(is);
        }

        BerLength length = new BerLength();
        codeLength += length.decode(is);

        if (length.val == 0) {
            value = new int[0];
            return codeLength;
        }

        byte[] byteCode = new byte[length.val];
        Util.readFully(is, byteCode);

        codeLength += length.val;

        List<Integer> objectIdentifierComponentsList = new ArrayList<>();

        int subIDEndIndex = 0;
        while ((byteCode[subIDEndIndex] & 0x80) == 0x80) {
            if (subIDEndIndex >= (length.val - 1)) {
                throw new IOException("Invalid Object Identifier");
            }
            subIDEndIndex++;
        }

        int subidentifier = 0;
        for (int i = 0; i <= subIDEndIndex; i++) {
            subidentifier |= (byteCode[i] << ((subIDEndIndex - i) * 7));
        }

        if (subidentifier < 40) {
            objectIdentifierComponentsList.add(0);
            objectIdentifierComponentsList.add(subidentifier);
        }
        else if (subidentifier < 80) {
            objectIdentifierComponentsList.add(1);
            objectIdentifierComponentsList.add(subidentifier - 40);
        }
        else {
            objectIdentifierComponentsList.add(2);
            objectIdentifierComponentsList.add(subidentifier - 80);
        }

        subIDEndIndex++;

        while (subIDEndIndex < length.val) {
            int subIDStartIndex = subIDEndIndex;

            while ((byteCode[subIDEndIndex] & 0x80) == 0x80) {
                if (subIDEndIndex == (length.val - 1)) {
                    throw new IOException("Invalid Object Identifier");
                }
                subIDEndIndex++;
            }
            subidentifier = 0;
            for (int j = subIDStartIndex; j <= subIDEndIndex; j++) {
                subidentifier |= ((byteCode[j] & 0x7f) << ((subIDEndIndex - j) * 7));
            }
            objectIdentifierComponentsList.add(subidentifier);
            subIDEndIndex++;
        }

        value = new int[objectIdentifierComponentsList.size()];
        for (int i = 0; i < objectIdentifierComponentsList.size(); i++) {
            value[i] = objectIdentifierComponentsList.get(i);
        }

        return codeLength;

    }

    @Override
    public String toString() {
        if (value == null || value.length == 0) {
            return "";
        }

        String objIDString = "";
        objIDString += value[0];
        for (int i = 1; i < value.length; i++) {
            objIDString += "." + value[i];
        }
        return objIDString;
    }

    public BerObjectIdentifier append(int value) {
        if (this.value == null) {
            return new BerObjectIdentifier(new int[] { value });
        }
        int[] values = new int[this.value.length + 1];
        for (int i = 0; i < this.value.length; ++i) {
            values[i] = this.value[i];
        }
        values[values.length - 1] = value;
        return new BerObjectIdentifier(values);
    }

}
