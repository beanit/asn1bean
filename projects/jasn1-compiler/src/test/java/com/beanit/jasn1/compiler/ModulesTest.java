/*
 * Copyright 2012 The jASN1 Authors
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
package com.beanit.jasn1.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream;
import com.beanit.jasn1.ber.types.BerEmbeddedPdv;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.string.BerVisibleString;
import com.beanit.jasn1.compiler.modules.module1.ChildInformation;
import com.beanit.jasn1.compiler.modules.module1.Date;
import com.beanit.jasn1.compiler.modules.module1.MyBitString;
import com.beanit.jasn1.compiler.modules.module1.MyDate1;
import com.beanit.jasn1.compiler.modules.module1.MyInt;
import com.beanit.jasn1.compiler.modules.module1.MyInt2;
import com.beanit.jasn1.compiler.modules.module1.Name;
import com.beanit.jasn1.compiler.modules.module1.PersonnelRecord;
import com.beanit.jasn1.compiler.modules.module1.TestChoice;
import com.beanit.jasn1.compiler.modules.module1.TestSequenceOf;
import com.beanit.jasn1.compiler.modules.module2.EmployeeNumberZ;
import com.beanit.jasn1.util.HexConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ModulesTest {

  @Test
  public void encodingDecoding() throws IOException {

    ReverseByteArrayOutputStream berOS = new ReverseByteArrayOutputStream(1000);

    MyDate1 dateOfHire = new MyDate1();
    // MyDate1 dateOfHire = new MyDate1("19710917");
    dateOfHire.value = new String("19710917").getBytes();

    dateOfHire.encode(berOS, true);

    MyInt2 myInt2Encode = new MyInt2(2);
    berOS.reset();
    myInt2Encode.encode(berOS, true);

    MyInt2 myInt2Decode = new MyInt2();
    byte[] code = HexConverter.fromShortHexString("a303020102");
    InputStream is = new ByteArrayInputStream(code);
    myInt2Decode.decode(is, true);
    assertEquals(myInt2Decode.value.intValue(), 2);

    PersonnelRecord pr = new PersonnelRecord();

    Name name = new Name();
    name.setGivenName(new BerVisibleString("givenName".getBytes()));
    name.setFamilyName(new BerVisibleString("familyName".getBytes()));
    name.setInitial(new BerVisibleString("initial".getBytes()));
    pr.setName(name);

    pr.setTitle(new BerVisibleString("title".getBytes()));

    pr.setNumber(new EmployeeNumberZ(1));

    pr.setDateOfHire(new Date("23121981".getBytes()));

    pr.setNameOfSpouse(name);

    ChildInformation child = new ChildInformation();
    child.setName(new Name("child name".getBytes()));
    child.setDateOfBirth(new Date("12121912".getBytes()));

    PersonnelRecord.Children children = new PersonnelRecord.Children();
    List<ChildInformation> childInformation = children.getChildInformation();
    childInformation.add(child);
    childInformation.add(child);

    pr.setTestBitString(new MyBitString(new byte[] {(byte) 0x80, (byte) 0xff}, 10));

    pr.setTest(new MyInt(3));

    TestChoice testChoice = new TestChoice();
    testChoice.setChoiceElement1(child);

    pr.setTest2(testChoice);

    pr.setTest3(testChoice);

    pr.setTest4(testChoice);

    pr.setTest5(testChoice);

    pr.setTest6(testChoice);

    TestSequenceOf testSequenceOf = new TestSequenceOf();
    List<BerInteger> berIntegers = testSequenceOf.getBerInteger();
    for (int i = 0; i < 10; i++) {
      berIntegers.add(new BerInteger(i));
    }
    pr.setTestSequenceOf(testSequenceOf);

    PersonnelRecord.TestSequenceOf2 testSequenceOf2 = new PersonnelRecord.TestSequenceOf2();
    List<PersonnelRecord.TestSequenceOf2.SEQUENCE> sequences = testSequenceOf2.getSEQUENCE();
    for (int i = 0; i < 10; i++) {
      PersonnelRecord.TestSequenceOf2.SEQUENCE sequence =
          new PersonnelRecord.TestSequenceOf2.SEQUENCE();
      sequence.setTest1(new BerInteger(i++));
      sequence.setTest2(new BerInteger(i));
      sequences.add(sequence);
    }
    pr.setTestSequenceOf2(testSequenceOf2);

    BerEmbeddedPdv berEmbeddedPdv = new BerEmbeddedPdv();
    pr.setEmbeddedPdv(berEmbeddedPdv);

    System.out.println("PersonnelRecord.toString():\n" + pr);
  }
}
