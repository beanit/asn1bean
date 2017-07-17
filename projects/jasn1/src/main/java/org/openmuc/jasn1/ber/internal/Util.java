/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static void readFully(InputStream is, byte[] buffer) throws IOException {
        readFully(is, buffer, 0, buffer.length);
    }

    public static void readFully(InputStream is, byte[] buffer, int off, int len) throws IOException {
        do {
            int bytesRead = is.read(buffer, off, len);
            if (bytesRead == -1) {
                throw new EOFException("Unexpected end of input stream.");
            }

            len -= bytesRead;
            off += bytesRead;
        } while (len > 0);
    }

}
