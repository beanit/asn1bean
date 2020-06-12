package com.beanit.asn1bean.sample;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import java.io.InputStream;
import java.util.List;

public class ASN1beanSample {

  public static void main(String[] args) throws IOException {

    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(1000);

    // Name name = new Name(new BerVisibleString("John"), new
    // BerVisibleString("P"), new BerVisibleString("Smith"));
    // instead of creating the Name object as in previous statement you can
    // assign the byte code directly as in the following statement. The
    // encode function of the name object will then simply insert this byte
    // array in the OutputStream. This can speed up things if the code
    // for certain structures is known and does not change.
    Name name = new Name(HexString.toBytes("101A044a6f686e1A01501A05536d697468"));

    BerVisibleString title = new BerVisibleString("Director".getBytes(UTF_8));
    EmployeeNumber number = new EmployeeNumber(51);
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

    // encodeAndSave will start the encoding and save the result in
    // child1.code. This is useful if the same structure will have to be
    // encoded several times as part of different structures. Using this
    // function will make sure that the real encoding is only done once.
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

    personnelRecord.encode(os);

    System.out.println("Encoded bytes:");
    System.out.println(HexString.fromBytes(os.getArray()));
    byte[] encodedBytes = os.getArray();

    InputStream is = new ByteArrayInputStream(encodedBytes);

    PersonnelRecord personnelRecord_decoded = new PersonnelRecord();
    personnelRecord_decoded.decode(is);

    System.out.println("\nDecoded structure:");
    System.out.println(personnelRecord_decoded);

    System.out.println("Given name = " + personnelRecord_decoded.getName().getGivenName());
  }
}
