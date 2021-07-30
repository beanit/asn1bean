/*
 * Copyright 2021 The ASN1bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.asn1bean.compiler;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.asn1bean.compiler.extension_test.extensible.ExtensibleSequenceAndMore;
import com.beanit.asn1bean.compiler.extension_test.extensible_with_access.ExtensibleAccessSequence;
import com.beanit.asn1bean.compiler.extension_test.extensible_with_access.ExtensibleAccessSequenceAndMore;
import com.beanit.asn1bean.compiler.extension_test.non_extensible.ExtendedSequence;
import com.beanit.asn1bean.compiler.extension_test.non_extensible.ExtendedSequenceAndMore;
import com.beanit.asn1bean.compiler.extension_test.non_extensible.NonExtensibleSequence;
import com.beanit.asn1bean.compiler.extension_test.non_extensible.NonExtensibleSequenceAndMore;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExtensibilityImpliedTest {

  private static byte[] extendedSequenceAndMoreCode;
  private static byte[] extensionCode;

  @BeforeAll
  static void computeCode() throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(100, true);

    NonExtensibleSequence nonExtensibleSequence = new NonExtensibleSequence();
    nonExtensibleSequence.setAge(new BerInteger(5));

    os.reset();
    nonExtensibleSequence.encode(os);
    byte[] nonExtensibleSequenceCode = os.getArray();

    ExtendedSequence extendedSequence = new ExtendedSequence();
    extendedSequence.setAge(new BerInteger(5));
    extendedSequence.setName(new BerVisibleString("name"));
    extendedSequence.setSubAge(nonExtensibleSequence);

    os.reset();
    extendedSequence.encode(os);
    byte[] extendedSequenceCode = os.getArray();
    extensionCode =
        Arrays.copyOfRange(
            extendedSequenceCode, nonExtensibleSequenceCode.length, extendedSequenceCode.length);

    ExtendedSequenceAndMore extendedSequenceAndMore = new ExtendedSequenceAndMore();
    extendedSequenceAndMore.setExtendedSequence(extendedSequence);
    extendedSequenceAndMore.setMore(new BerVisibleString("more"));

    os.reset();
    extendedSequenceAndMore.encode(os);
    extendedSequenceAndMoreCode = os.getArray();
  }

  @Test
  void testThatDecodingNonExtensibleSequenceFails() {
    NonExtensibleSequenceAndMore nonExtensibleSequenceAndMore = new NonExtensibleSequenceAndMore();
    Assertions.assertThrows(
        IOException.class,
        () ->
            nonExtensibleSequenceAndMore.decode(
                new ByteArrayInputStream(extendedSequenceAndMoreCode)));
  }

  @Test
  void testThatDecodingExtensibleSequenceSucceeds() throws IOException {
    ExtensibleSequenceAndMore extensibleSequenceAndMore = new ExtensibleSequenceAndMore();
    extensibleSequenceAndMore.decode(new ByteArrayInputStream(extendedSequenceAndMoreCode));
    Assertions.assertEquals(
        5, extensibleSequenceAndMore.getExtensibleSequence().getAge().value.intValue());
    Assertions.assertEquals("more", extensibleSequenceAndMore.getMore().toString());
  }

  @Test
  void testThatDecodingExtensibleSequenceIndefLengthSucceeds() throws IOException {
    String code = "308030800201051A046E616D65AA80020105000000001A046D6F72650000";
    ExtensibleSequenceAndMore extensibleSequenceAndMore = new ExtensibleSequenceAndMore();
    extensibleSequenceAndMore.decode(new ByteArrayInputStream(HexString.toBytes(code)));
    Assertions.assertEquals(
        5, extensibleSequenceAndMore.getExtensibleSequence().getAge().value.intValue());
    Assertions.assertEquals("more", extensibleSequenceAndMore.getMore().toString());
  }

  @Test
  void accessExtensionBytes() throws IOException {
    ExtensibleAccessSequenceAndMore extensibleSequenceAndMore =
        new ExtensibleAccessSequenceAndMore();
    extensibleSequenceAndMore.decode(new ByteArrayInputStream(extendedSequenceAndMoreCode));
    Assertions.assertEquals(
        5, extensibleSequenceAndMore.getExtensibleSequence().getAge().value.intValue());
    Assertions.assertEquals("more", extensibleSequenceAndMore.getMore().toString());
    Assertions.assertArrayEquals(
        extensionCode, extensibleSequenceAndMore.getExtensibleSequence().getExtensionBytes());
  }

  @Test
  void setExtensionBytes() throws IOException {

    NonExtensibleSequence nonExtensibleSequence = new NonExtensibleSequence();
    nonExtensibleSequence.setAge(new BerInteger(5));

    ExtensibleAccessSequence extensibleSequence = new ExtensibleAccessSequence();
    extensibleSequence.setAge(new BerInteger(5));
    extensibleSequence.setExtensionBytes(extensionCode);

    ExtensibleAccessSequenceAndMore extensibleSequenceAndMore =
        new ExtensibleAccessSequenceAndMore();
    extensibleSequenceAndMore.setExtensibleSequence(extensibleSequence);
    extensibleSequenceAndMore.setMore(new BerVisibleString("more"));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(100, true);
    extensibleSequenceAndMore.encode(os);
    Assertions.assertArrayEquals(extendedSequenceAndMoreCode, os.getArray());
  }
}
