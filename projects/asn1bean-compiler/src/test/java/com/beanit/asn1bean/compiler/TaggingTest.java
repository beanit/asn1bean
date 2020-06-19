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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerAny;
import com.beanit.asn1bean.ber.types.BerBoolean;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.compiler.tagging_test.ExplicitlyTaggedInteger;
import com.beanit.asn1bean.compiler.tagging_test.ExplicitlyTaggedSeqOf;
import com.beanit.asn1bean.compiler.tagging_test.ExplicitlyTaggedSequence;
import com.beanit.asn1bean.compiler.tagging_test.ExplicitlyTaggedSet;
import com.beanit.asn1bean.compiler.tagging_test.ExplicitlyTaggedSetOf;
import com.beanit.asn1bean.compiler.tagging_test.ImplicitlyRetaggedTaggedChoice;
import com.beanit.asn1bean.compiler.tagging_test.ImplicitlyTaggedInteger;
import com.beanit.asn1bean.compiler.tagging_test.RetaggedUntaggedChoice;
import com.beanit.asn1bean.compiler.tagging_test.SeqOfExplicitlyTaggedType;
import com.beanit.asn1bean.compiler.tagging_test.SequenceOfDirectTypes;
import com.beanit.asn1bean.compiler.tagging_test.TaggedChoice;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TaggingTest {

  @Test
  public void seqOfExplicitlyTaggedTypeTest() throws Exception {

    SeqOfExplicitlyTaggedType seqOf = new SeqOfExplicitlyTaggedType();
    List<BerInteger> integerList = seqOf.getBerInteger();

    integerList.add(new BerInteger(3));
    integerList.add(new BerInteger(4));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    seqOf.encode(os);

    assertArrayEquals(toBytes("300AA303020103A303020104"), os.getArray());

    seqOf = new SeqOfExplicitlyTaggedType();
    seqOf.decode(new ByteArrayInputStream(os.getArray()));
  }

  @Test
  public void retaggedChoiceTest() throws Exception {

    RetaggedUntaggedChoice choice = new RetaggedUntaggedChoice();
    choice.setMyInteger(new BerInteger(1));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    choice.encode(os);

    assertArrayEquals(toBytes("BF2103830101"), os.getArray());

    choice = new RetaggedUntaggedChoice();
    choice.decode(new ByteArrayInputStream(os.getArray()));

    assertNotNull(choice.getMyInteger());
    assertNull(choice.getMyBoolean());
  }

  @Test
  public void explicitlyTaggedSequenceTest() throws Exception {

    ExplicitlyTaggedSequence sequence = new ExplicitlyTaggedSequence();
    sequence.setMyInteger(new BerInteger(1));
    sequence.setMyBoolean(new BerBoolean(true));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    sequence.encode(os);

    byte[] code = toBytes("BF210830060201010101FF");
    assertArrayEquals(code, os.getArray());

    sequence = new ExplicitlyTaggedSequence();
    int numDecodedBytes = sequence.decode(new ByteArrayInputStream(os.getArray()));

    assertNotNull(sequence.getMyInteger());
    assertNotNull(sequence.getMyBoolean());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void explicitlyTaggedSequenceIndefiniteTest() throws Exception {
    byte[] code = toBytes("BF218030800201010101FF00000000");
    ExplicitlyTaggedSequence sequence = new ExplicitlyTaggedSequence();
    InputStream is = new ByteArrayInputStream(code);
    int numDecodedBytes = sequence.decode(is);

    assertNotNull(sequence.getMyInteger());
    assertNotNull(sequence.getMyBoolean());
    assertEquals(-1, is.read());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void explicitlyTaggedSetTest() throws Exception {

    ExplicitlyTaggedSet set = new ExplicitlyTaggedSet();
    set.setMyInteger(new BerInteger(1));
    set.setMyBoolean(new BerBoolean(true));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    set.encode(os);

    assertArrayEquals(toBytes("BF210a3108a1030201010101FF"), os.getArray());

    set = new ExplicitlyTaggedSet();
    set.decode(new ByteArrayInputStream(os.getArray()));

    assertNotNull(set.getMyInteger());
    assertNotNull(set.getMyBoolean());
  }

  @Test
  public void explicitlyTaggedSetIndefiniteTest() throws Exception {

    byte[] code = toBytes("BF21803180a18002010100000101FF00000000");
    ExplicitlyTaggedSet set = new ExplicitlyTaggedSet();
    InputStream is = new ByteArrayInputStream(code);
    int numDecodedBytes = set.decode(is);

    assertNotNull(set.getMyInteger());
    assertNotNull(set.getMyBoolean());
    assertEquals(-1, is.read());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void explicitlyTaggedSeqOfTest() throws Exception {

    ExplicitlyTaggedSeqOf seqOf = new ExplicitlyTaggedSeqOf();
    List<BerInteger> integerList = seqOf.getBerInteger();

    integerList.add(new BerInteger(3));
    integerList.add(new BerInteger(4));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    seqOf.encode(os);

    assertArrayEquals(toBytes("BF21083006020103020104"), os.getArray());

    seqOf = new ExplicitlyTaggedSeqOf();
    seqOf.decode(new ByteArrayInputStream(os.getArray()));
  }

  @Test
  public void explicitlyTaggedSeqOfIndefiniteTest() throws Exception {
    byte[] code = toBytes("BF2180308002010302010400000000");
    ExplicitlyTaggedSeqOf seqOf = new ExplicitlyTaggedSeqOf();
    InputStream is = new ByteArrayInputStream(code);
    int numDecodedBytes = seqOf.decode(is);

    assertNotNull(seqOf.getBerInteger());
    assertEquals(-1, is.read());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void explicitlyTaggedSetOfTest() throws Exception {

    ExplicitlyTaggedSetOf setOf = new ExplicitlyTaggedSetOf();
    List<BerInteger> integerList = setOf.getBerInteger();

    integerList.add(new BerInteger(3));
    integerList.add(new BerInteger(4));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    setOf.encode(os);

    assertArrayEquals(toBytes("BF21083106020103020104"), os.getArray());

    setOf = new ExplicitlyTaggedSetOf();
    setOf.decode(new ByteArrayInputStream(os.getArray()));
  }

  @Test
  public void taggedChoiceTest() throws Exception {

    TaggedChoice choice = new TaggedChoice();
    choice.setMyInteger(new BerInteger(1));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    choice.encode(os);

    assertArrayEquals(toBytes("BF2205A403020101"), os.getArray());

    choice = new TaggedChoice();
    choice.decode(new ByteArrayInputStream(os.getArray()));

    assertNotNull(choice.getMyInteger());
    assertNull(choice.getMyBoolean());
  }

  @Test
  public void taggedChoiceIndefiniteTest() throws Exception {
    byte[] code = toBytes("BF2280A48002010100000000");
    TaggedChoice taggedChoice = new TaggedChoice();
    InputStream is = new ByteArrayInputStream(code);
    int numDecodedBytes = taggedChoice.decode(is);

    assertNotNull(taggedChoice.getMyInteger());
    assertEquals(-1, is.read());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void implicitlyTaggedIntegerTest() throws Exception {

    ImplicitlyTaggedInteger implicitlyTaggedInteger = new ImplicitlyTaggedInteger(1);

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    implicitlyTaggedInteger.encode(os);

    assertArrayEquals(toBytes("9F210101"), os.getArray());
  }

  @Test
  public void explicitlyRetaggedIntegerTest() throws Exception {

    ExplicitlyTaggedInteger explicitlyTaggedInteger = new ExplicitlyTaggedInteger(1);

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    explicitlyTaggedInteger.encode(os);

    assertArrayEquals(toBytes("BF2103020101"), os.getArray());
  }

  @Test
  public void explicitlyRetaggedIntegerIndefiniteTest() throws Exception {
    byte[] code = toBytes("BF21800201010000");
    ExplicitlyTaggedInteger explicitlyTaggedInteger = new ExplicitlyTaggedInteger();
    InputStream is = new ByteArrayInputStream(code);
    int numDecodedBytes = explicitlyTaggedInteger.decode(is);

    assertNotNull(explicitlyTaggedInteger.value);
    assertEquals(-1, is.read());
    assertEquals(code.length, numDecodedBytes);
  }

  @Test
  public void implicitlyRetaggedTaggedChoiceTest() throws Exception {

    ImplicitlyRetaggedTaggedChoice choice = new ImplicitlyRetaggedTaggedChoice();
    choice.setMyInteger(new BerInteger(1));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    choice.encode(os);

    assertArrayEquals(toBytes("A305A403020101"), os.getArray());
  }

  @Test
  public void sequenceOfDirectTypesTest() throws Exception {

    SequenceOfDirectTypes sequence = new SequenceOfDirectTypes();
    sequence.setUntaggedInt(new BerInteger(1));
    sequence.setExplicitlyTaggedInt(new BerInteger(2));
    sequence.setImplicitlyTaggedInt(new BerInteger(3));

    SequenceOfDirectTypes.UntaggedChoice untaggedChoice =
        new SequenceOfDirectTypes.UntaggedChoice();
    untaggedChoice.setMyBoolean(new BerBoolean(true));
    sequence.setUntaggedChoice(untaggedChoice);

    SequenceOfDirectTypes.TaggedChoice taggedChoice = new SequenceOfDirectTypes.TaggedChoice();
    taggedChoice.setMyInteger(new BerInteger(4));
    sequence.setTaggedChoice(taggedChoice);

    sequence.setTaggedAny(new BerAny(new byte[] {2, 1, 1}));

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);
    sequence.encode(os);

    assertArrayEquals(
        toBytes("BF2B18020101A1030201028201038401FFA503830104A603020101"), os.getArray());

    sequence = new SequenceOfDirectTypes();
    sequence.decode(new ByteArrayInputStream(os.getArray()));

    assertEquals(1, sequence.getUntaggedInt().value.intValue());
    assertEquals(2, sequence.getExplicitlyTaggedInt().value.intValue());
    assertEquals(3, sequence.getImplicitlyTaggedInt().value.intValue());
    assertTrue(untaggedChoice.getMyBoolean().value);
    assertEquals(4, sequence.getTaggedChoice().getMyInteger().value.intValue());
    assertArrayEquals(toBytes("020101"), sequence.getTaggedAny().value);
    assertNull(sequence.getUntaggedChoice2());
  }

  @Test
  public void sequenceOfDirectTypesIndefiniteTest() throws Exception {

    byte[] encodedBytes = toBytes("BF2B80020101A18002010200008201038401FFA503830104A6030201010000");

    SequenceOfDirectTypes sequence = new SequenceOfDirectTypes();
    sequence.decode(new ByteArrayInputStream(encodedBytes));

    assertEquals(1, sequence.getUntaggedInt().value.intValue());
    assertEquals(2, sequence.getExplicitlyTaggedInt().value.intValue());
    assertEquals(3, sequence.getImplicitlyTaggedInt().value.intValue());
    assertTrue(sequence.getUntaggedChoice().getMyBoolean().value);
    assertEquals(4, sequence.getTaggedChoice().getMyInteger().value.intValue());
    assertArrayEquals(toBytes("020101"), sequence.getTaggedAny().value);
    assertNull(sequence.getUntaggedChoice2());
  }
}
