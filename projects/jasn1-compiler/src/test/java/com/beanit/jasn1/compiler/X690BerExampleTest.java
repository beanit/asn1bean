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

import com.beanit.jasn1.ber.ReverseByteArrayOutputStream;
import com.beanit.jasn1.ber.types.string.BerVisibleString;
import com.beanit.jasn1.compiler.x690_ber_example.ChildInformation;
import com.beanit.jasn1.compiler.x690_ber_example.Date;
import com.beanit.jasn1.compiler.x690_ber_example.EmployeeNumber;
import com.beanit.jasn1.compiler.x690_ber_example.Name;
import com.beanit.jasn1.compiler.x690_ber_example.PersonnelRecord;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class X690BerExampleTest {

  public static String getByteArrayString(byte[] byteArray) {
    StringBuilder builder = new StringBuilder();
    int l = 1;
    for (byte b : byteArray) {
      if ((l != 1) && ((l - 1) % 8 == 0)) {
        builder.append(' ');
      }
      if ((l != 1) && ((l - 1) % 16 == 0)) {
        builder.append('\n');
      }
      l++;
      builder.append("0x");
      String hexString = Integer.toHexString(b & 0xff);
      if (hexString.length() == 1) {
        builder.append(0);
      }
      builder.append(hexString + " ");
    }
    return builder.toString();
  }

  @Test
  public void encodingDecoding() throws IOException {

    ReverseByteArrayOutputStream berOS = new ReverseByteArrayOutputStream(1000);

    // Name name = new Name(new BerVisibleString("John"), new
    // BerVisibleString("P"), new BerVisibleString("Smith"));
    Name name = new Name();
    name.code =
        new byte[] {
          (byte) 0x10,
          (byte) 0x1A,
          (byte) 0x04,
          (byte) 0x4a,
          (byte) 0x6f,
          (byte) 0x68,
          (byte) 0x6e,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x50,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x6d,
          (byte) 0x69,
          (byte) 0x74,
          (byte) 0x68
        };
    BerVisibleString title = new BerVisibleString("Director".getBytes("US-ASCII"));
    // EmployeeNumber number = new EmployeeNumber(51);
    EmployeeNumber number = new EmployeeNumber();
    number.code = new byte[] {0x01, 0x33};
    Date dateOfHire = new Date("19710917".getBytes());
    Name nameOfSpouse = new Name();
    nameOfSpouse.setGivenName(new BerVisibleString("Mary"));
    nameOfSpouse.setInitial(new BerVisibleString("T"));
    nameOfSpouse.setFamilyName(new BerVisibleString("Smith"));

    Name child1Name = new Name();
    child1Name.setGivenName(new BerVisibleString("Ralph"));
    child1Name.setInitial(new BerVisibleString("T"));
    child1Name.setFamilyName(new BerVisibleString("Smith"));

    ChildInformation child1 = new ChildInformation();
    child1.setName(child1Name);
    child1.setDateOfBirth(new Date("19571111".getBytes()));

    System.out.println("192: " + HexConverter.toShortHexString("19571111".getBytes()));

    child1.encodeAndSave(80);

    System.out.println("geneCode: " + HexConverter.toShortHexString(child1.code));
    System.out.println(
        "realCode: "
            + HexConverter.toShortHexString(
                new byte[] {
                  (byte) 0x1f,
                  (byte) 0x61,
                  (byte) 0x11,
                  (byte) 0x1A,
                  (byte) 0x05,
                  (byte) 0x52,
                  (byte) 0x61,
                  (byte) 0x6c,
                  (byte) 0x70,
                  (byte) 0x68,
                  (byte) 0x1A,
                  (byte) 0x01,
                  (byte) 0x54,
                  (byte) 0x1A,
                  (byte) 0x05,
                  (byte) 0x53,
                  (byte) 0x6d,
                  (byte) 0x69,
                  (byte) 0x74,
                  (byte) 0x68,
                  (byte) 0xa0,
                  (byte) 0x0a,
                  (byte) 0x43,
                  (byte) 0x08,
                  (byte) 0x31,
                  (byte) 0x39,
                  (byte) 0x35,
                  (byte) 0x37,
                  (byte) 0x31,
                  (byte) 0x31,
                  (byte) 0x31,
                  (byte) 0x31
                }));

    Assert.assertArrayEquals(
        new byte[] {
          (byte) 0x1f,
          (byte) 0x61,
          (byte) 0x11,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x52,
          (byte) 0x61,
          (byte) 0x6c,
          (byte) 0x70,
          (byte) 0x68,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x54,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x6d,
          (byte) 0x69,
          (byte) 0x74,
          (byte) 0x68,
          (byte) 0xa0,
          (byte) 0x0a,
          (byte) 0x43,
          (byte) 0x08,
          (byte) 0x31,
          (byte) 0x39,
          (byte) 0x35,
          (byte) 0x37,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x31
        },
        child1.code);

    Name child2Name = new Name();
    child2Name.setGivenName(new BerVisibleString("Susan"));
    child2Name.setInitial(new BerVisibleString("B"));
    child2Name.setFamilyName(new BerVisibleString("Jones"));

    ChildInformation child2 = new ChildInformation();
    child2.setName(child2Name);
    child2.setDateOfBirth(new Date("19590717".getBytes()));

    PersonnelRecord.Children childrenSeq = new PersonnelRecord.Children();
    List<ChildInformation> childList = childrenSeq.getChildInformation();
    childList.add(child1);
    childList.add(child2);

    PersonnelRecord personnelRecord = new PersonnelRecord();
    personnelRecord.setName(name);
    personnelRecord.setTitle(title);
    personnelRecord.setNumber(number);
    personnelRecord.setDateOfHire(dateOfHire);
    personnelRecord.setNameOfSpouse(nameOfSpouse);
    personnelRecord.setChildren(childrenSeq);

    personnelRecord.encode(berOS, true);

    byte[] expectedBytes =
        new byte[] {
          (byte) 0x60,
          (byte) 0x81,
          (byte) 0x85,
          (byte) 0x61,
          (byte) 0x10,
          (byte) 0x1A,
          (byte) 0x04,
          (byte) 0x4a,
          (byte) 0x6f,
          (byte) 0x68,
          (byte) 0x6e,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x50,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x6d,
          (byte) 0x69,
          (byte) 0x74,
          (byte) 0x68,
          (byte) 0xa0,
          (byte) 0x0a,
          (byte) 0x1A,
          (byte) 0x08,
          (byte) 0x44,
          (byte) 0x69,
          (byte) 0x72,
          (byte) 0x65,
          (byte) 0x63,
          (byte) 0x74,
          (byte) 0x6f,
          (byte) 0x72,
          (byte) 0x42,
          (byte) 0x01,
          (byte) 0x33,
          (byte) 0xa1,
          (byte) 0x0a,
          (byte) 0x43,
          (byte) 0x08,
          (byte) 0x31,
          (byte) 0x39,
          (byte) 0x37,
          (byte) 0x31,
          (byte) 0x30,
          (byte) 0x39,
          (byte) 0x31,
          (byte) 0x37,
          (byte) 0xa2,
          (byte) 0x12,
          (byte) 0x61,
          (byte) 0x10,
          (byte) 0x1A,
          (byte) 0x04,
          (byte) 0x4d,
          (byte) 0x61,
          (byte) 0x72,
          (byte) 0x79,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x54,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x6d,
          (byte) 0x69,
          (byte) 0x74,
          (byte) 0x68,
          (byte) 0xa3,
          (byte) 0x42,
          (byte) 0x31,
          (byte) 0x1f,
          (byte) 0x61,
          (byte) 0x11,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x52,
          (byte) 0x61,
          (byte) 0x6c,
          (byte) 0x70,
          (byte) 0x68,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x54,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x6d,
          (byte) 0x69,
          (byte) 0x74,
          (byte) 0x68,
          (byte) 0xa0,
          (byte) 0x0a,
          (byte) 0x43,
          (byte) 0x08,
          (byte) 0x31,
          (byte) 0x39,
          (byte) 0x35,
          (byte) 0x37,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x31,
          (byte) 0x1f,
          (byte) 0x61,
          (byte) 0x11,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x53,
          (byte) 0x75,
          (byte) 0x73,
          (byte) 0x61,
          (byte) 0x6e,
          (byte) 0x1A,
          (byte) 0x01,
          (byte) 0x42,
          (byte) 0x1A,
          (byte) 0x05,
          (byte) 0x4a,
          (byte) 0x6f,
          (byte) 0x6e,
          (byte) 0x65,
          (byte) 0x73,
          (byte) 0xa0,
          (byte) 0x0a,
          (byte) 0x43,
          (byte) 0x08,
          (byte) 0x31,
          (byte) 0x39,
          (byte) 0x35,
          (byte) 0x39,
          (byte) 0x30,
          (byte) 0x37,
          (byte) 0x31,
          (byte) 0x37
        };

    System.out.println("encoded structure:");
    System.out.println(getByteArrayString(berOS.getArray()));

    Assert.assertArrayEquals(expectedBytes, berOS.getArray());

    ByteBuffer byteBuffer = berOS.getByteBuffer();
    Assert.assertEquals((byte) 0x60, byteBuffer.get());
    Assert.assertEquals((byte) 0x37, byteBuffer.get(byteBuffer.limit() - 1));

    ByteArrayInputStream bais = new ByteArrayInputStream(berOS.getArray());

    PersonnelRecord personnelRecord_decoded = new PersonnelRecord();
    personnelRecord_decoded.decode(bais, true);

    Assert.assertEquals("John", new String(personnelRecord_decoded.getName().getGivenName().value));

    // System.out
    // .println("presentation_context_identifier= "
    // +
    // cpType_decoded.normal_mode_parameters.presentation_context_definition_list.seqOf.get(0).abstract_syntax_name);

  }
}
