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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.asn1bean.compiler.x690_ber_example.ChildInformation;
import com.beanit.asn1bean.compiler.x690_ber_example.Date;
import com.beanit.asn1bean.compiler.x690_ber_example.EmployeeNumber;
import com.beanit.asn1bean.compiler.x690_ber_example.Name;
import com.beanit.asn1bean.compiler.x690_ber_example.PersonnelRecord;
import com.beanit.asn1bean.util.HexString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;

public class X690BerExampleTest {

  @Test
  public void encodingDecoding() throws IOException {

    ReverseByteArrayOutputStream berOS = new ReverseByteArrayOutputStream(1000);

    // Name name = new Name(new BerVisibleString("John"), new
    // BerVisibleString("P"), new BerVisibleString("Smith"));
    Name name = new Name(HexString.toBytes("101A044a6f686e1A01501A05536d697468"));
    BerVisibleString title = new BerVisibleString("Director".getBytes(UTF_8));
    // EmployeeNumber number = new EmployeeNumber(51);
    EmployeeNumber number = new EmployeeNumber(new byte[] {0x01, 0x33});
    Date dateOfHire = new Date("19710917".getBytes(UTF_8));
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
    child1.setDateOfBirth(new Date("19571111".getBytes(UTF_8)));

    child1.encodeAndSave(80);

    Name child2Name = new Name();
    child2Name.setGivenName(new BerVisibleString("Susan"));
    child2Name.setInitial(new BerVisibleString("B"));
    child2Name.setFamilyName(new BerVisibleString("Jones"));

    ChildInformation child2 = new ChildInformation();
    child2.setName(child2Name);
    child2.setDateOfBirth(new Date("19590717".getBytes(UTF_8)));

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
        HexString.toBytes(
            "60818561101A044a6f686e1A01501A05536d697468a00a1A084469726563746f72420133a10a43083139373130393137a21261101A044d6172791A01541A05536d697468a342311f61111A0552616c70681A01541A05536d697468a00a43083139353731313131311f61111A05537573616e1A01421A054a6f6e6573a00a43083139353930373137");

    assertArrayEquals(expectedBytes, berOS.getArray());

    ByteBuffer byteBuffer = berOS.getByteBuffer();
    assertEquals((byte) 0x60, byteBuffer.get());
    assertEquals((byte) 0x37, byteBuffer.get(byteBuffer.limit() - 1));

    ByteArrayInputStream bais = new ByteArrayInputStream(berOS.getArray());

    PersonnelRecord personnelRecord_decoded = new PersonnelRecord();
    personnelRecord_decoded.decode(bais, true);

    assertEquals("John", new String(personnelRecord_decoded.getName().getGivenName().value, UTF_8));
  }
}
