package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BerType {

    public int encode(OutputStream reverseOS) throws IOException;

    public int decode(InputStream is) throws IOException;

}
