package org.openmuc.jasn1.ber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openmuc.jasn1.ber.types.BerType;

public interface BerSerializable extends BerType {

    public int encode(OutputStream reverseOS, boolean withTag) throws IOException;

    public int decode(InputStream is, boolean withTag) throws IOException;

}
