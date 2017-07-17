package org.openmuc.jasn1.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;
import org.openmuc.jasn1.compiler.x690_ber_example.ChildInformation;
import org.openmuc.jasn1.compiler.x690_ber_example.Date;
import org.openmuc.jasn1.compiler.x690_ber_example.EmployeeNumber;
import org.openmuc.jasn1.compiler.x690_ber_example.Name;
import org.openmuc.jasn1.compiler.x690_ber_example.PersonnelRecord;

public class Jasn1Sample {

    public static void main(String[] args) throws IOException {

        BerByteArrayOutputStream os = new BerByteArrayOutputStream(1000);

        // Name name = new Name(new BerVisibleString("John"), new
        // BerVisibleString("P"), new BerVisibleString("Smith"));
        // instead of creating the Name object as in previous statement you can
        // assign the byte code directly as in the following statement. The
        // encode function of the name object will then simply insert this byte
        // array in the BerOutputStream. This can speed up things if the code
        // for certain structures is known and does not change.
        Name name = new Name();
        name.code = new byte[] { (byte) 0x10, (byte) 0x1A, (byte) 0x04, (byte) 0x4a, (byte) 0x6f, (byte) 0x68,
                (byte) 0x6e, (byte) 0x1A, (byte) 0x01, (byte) 0x50, (byte) 0x1A, (byte) 0x05, (byte) 0x53, (byte) 0x6d,
                (byte) 0x69, (byte) 0x74, (byte) 0x68 };

        BerVisibleString title = new BerVisibleString("Director".getBytes());
        EmployeeNumber number = new EmployeeNumber(51);
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

        personnelRecord.encode(os);

        System.out.println("Encoded bytes:");
        System.out.println(DatatypeConverter.printHexBinary(os.getArray()));

        InputStream is = new ByteArrayInputStream(os.getArray());

        PersonnelRecord personnelRecord_decoded = new PersonnelRecord();
        personnelRecord_decoded.decode(is);

        System.out.println("\nDecoded structure:");
        System.out.println(personnelRecord_decoded);

        System.out.println("Given name = " + personnelRecord_decoded.getName().getGivenName());

    }
}
