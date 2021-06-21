/*
 * Copyright 2012 The ASN1bean Authors
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

import static com.beanit.asn1bean.util.HexString.toBytes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerAny;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.compiler.various_tests.SeqContainingAnyDefinedBy;
import com.beanit.asn1bean.compiler.various_tests.SimpleSeq;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnyTypeTest {

  @Test
  public void encodingAny() throws Exception {

    SimpleSeq simpleSeq = new SimpleSeq(new BerInteger(2));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    simpleSeq.encode(os);

    System.out.println(HexString.fromBytes(os.getArray()));

    assertArrayEquals(toBytes("3003800102"), os.getArray());

    SeqContainingAnyDefinedBy seqContainingAnyDefinedBy =
        new SeqContainingAnyDefinedBy(new BerAny(os.getArray()));
    os.reset();
    seqContainingAnyDefinedBy.encode(os);

    System.out.println(HexString.fromBytes(os.getArray()));

    assertArrayEquals(toBytes("3007A0053003800102"), os.getArray());
  }

  @Test
  public void decodingAnyDefiniteLength() throws Exception {
    decodeAny(toBytes("3007A0053003800102"));
  }

  @Test
  public void decodingAnyIndefiniteLength() throws Exception {
    decodeAny(toBytes("3009A00730808001020000"));
  }

  private void decodeAny(byte[] encodedBytes) throws IOException {
    SeqContainingAnyDefinedBy seqContainingAnyDefinedBy = new SeqContainingAnyDefinedBy();
    seqContainingAnyDefinedBy.decode(new ByteArrayInputStream(encodedBytes));

    byte[] bytesContainedByAny = seqContainingAnyDefinedBy.myAny.value;

    SimpleSeq simpleSeq = new SimpleSeq();
    simpleSeq.decode(new ByteArrayInputStream(bytesContainedByAny));

    Assertions.assertEquals(2, simpleSeq.myElement.value.intValue());
  }
}
