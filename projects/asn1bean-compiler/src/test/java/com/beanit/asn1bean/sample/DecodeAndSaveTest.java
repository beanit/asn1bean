package com.beanit.asn1bean.sample;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.compiler.rspdefinitions.EUICCInfo2;
import com.beanit.asn1bean.util.HexString;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DecodeAndSaveTest {
    @Test
    public void decodeAndSaveForConstructedTypeMaintainsAdditionalPropertiesNotDefinedInTheAsn1Structure() throws IOException {
        /*
        This binary payload is from SGP.32 which contains 2 new additional fields:
        - ipaMode
        - iotSpecificInfo
        However, these fields are not defined in the EUICCInfo2 of SGP.22
         */
        byte[] euiccInfo2Bytes = HexString.toBytes("BF22819A810302030182030205008303010A01840F81010082040006FB8083040000355C8505006B86D7D18603110000870302030088020294A916041481370F5125D0B1D408D4C3B232E6D25E795BEBFBAA16041481370F5125D0B1D408D4C3B232E6D25E795BEBFB9902064004030201000C0D534D2D43412D55502D30353236AC0480008100AF050403030301900100B40BA005040301020081008200");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(euiccInfo2Bytes);
        EUICCInfo2 euiccInfo2 = new EUICCInfo2();
        euiccInfo2.decodeAndSave(byteArrayInputStream, true);
        try(ReverseByteArrayOutputStream outputStream = new ReverseByteArrayOutputStream(euiccInfo2Bytes.length)) {
            euiccInfo2.encode(outputStream);
            assertArrayEquals(euiccInfo2Bytes, outputStream.getArray());
        }
    }
}
