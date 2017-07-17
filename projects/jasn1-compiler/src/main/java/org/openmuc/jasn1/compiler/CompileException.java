/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler;

public class CompileException extends RuntimeException {

    private static final long serialVersionUID = 6164836597802536594L;

    public CompileException() {
        super();
    }

    public CompileException(String s) {
        super(s);
    }

    public CompileException(Throwable cause) {
        super(cause);
    }

    public CompileException(String s, Throwable cause) {
        super(s, cause);
    }

}
