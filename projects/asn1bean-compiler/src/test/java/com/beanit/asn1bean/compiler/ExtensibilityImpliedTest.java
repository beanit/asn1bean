package com.beanit.asn1bean.compiler;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.asn1bean.compiler.modules.module1.ExtendedSequence;
import com.beanit.asn1bean.compiler.modules.module1.ExtendedSequenceAndMore;
import com.beanit.asn1bean.compiler.modules.module1.NonExtensibleSequence;
import com.beanit.asn1bean.compiler.modules.module1.NonExtensibleSequenceAndMore;
import com.beanit.asn1bean.compiler.modules.module2.ExtensibleSequenceAndMore;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensibilityImpliedTest {

  private byte[] getExtendedSequenceCode() throws IOException {
    ExtendedSequence extendedSequence = new ExtendedSequence();
    extendedSequence.setAge(new BerInteger(5));
    extendedSequence.setName(new BerVisibleString("name"));
    NonExtensibleSequence subAge = new NonExtensibleSequence();
    subAge.setAge(new BerInteger(4));
    extendedSequence.setSubAge(subAge);

    ExtendedSequenceAndMore extendedSequenceAndMore = new ExtendedSequenceAndMore();
    extendedSequenceAndMore.setExtendedSequence(extendedSequence);
    extendedSequenceAndMore.setMore(new BerVisibleString("more"));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(100);
    extendedSequenceAndMore.encode(os);
    System.out.println(HexString.fromBytes(os.getArray()));
    return os.getArray();
  }

  @Test
  void testThatDecodingNonExtensibleSequenceFails() {
    NonExtensibleSequenceAndMore nonExtensibleSequenceAndMore = new NonExtensibleSequenceAndMore();
    Assertions.assertThrows(
        IOException.class,
        () ->
            nonExtensibleSequenceAndMore.decode(
                new ByteArrayInputStream(getExtendedSequenceCode())));
  }

  @Test
  void testThatDecodingExtensibleSequenceSucceeds() throws IOException {
    ExtensibleSequenceAndMore extensibleSequenceAndMore = new ExtensibleSequenceAndMore();
    extensibleSequenceAndMore.decode(new ByteArrayInputStream(getExtendedSequenceCode()));
    Assertions.assertEquals(
        5, extensibleSequenceAndMore.getExtensibleSequence().getAge().value.intValue());
    Assertions.assertEquals("more", extensibleSequenceAndMore.getMore().toString());
  }

  @Test
  void testThatDecodingExtensibleSequenceIndefLengthSucceeds() throws IOException {
    String code = "308030800201051A046E616D65AA80020104000000001A046D6F72650000";
    ExtensibleSequenceAndMore extensibleSequenceAndMore = new ExtensibleSequenceAndMore();
    extensibleSequenceAndMore.decode(new ByteArrayInputStream(HexString.toBytes(code)));
    Assertions.assertEquals(
        5, extensibleSequenceAndMore.getExtensibleSequence().getAge().value.intValue());
    Assertions.assertEquals("more", extensibleSequenceAndMore.getMore().toString());
  }
}
