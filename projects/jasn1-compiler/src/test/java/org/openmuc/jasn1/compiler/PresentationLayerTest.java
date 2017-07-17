/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerAny;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.compiler.iso8823_presentation.AbstractSyntaxName;
import org.openmuc.jasn1.compiler.iso8823_presentation.CPType;
import org.openmuc.jasn1.compiler.iso8823_presentation.CalledPresentationSelector;
import org.openmuc.jasn1.compiler.iso8823_presentation.CallingPresentationSelector;
import org.openmuc.jasn1.compiler.iso8823_presentation.ContextList;
import org.openmuc.jasn1.compiler.iso8823_presentation.FullyEncodedData;
import org.openmuc.jasn1.compiler.iso8823_presentation.ModeSelector;
import org.openmuc.jasn1.compiler.iso8823_presentation.PDVList;
import org.openmuc.jasn1.compiler.iso8823_presentation.PresentationContextDefinitionList;
import org.openmuc.jasn1.compiler.iso8823_presentation.PresentationContextIdentifier;
import org.openmuc.jasn1.compiler.iso8823_presentation.TransferSyntaxName;
import org.openmuc.jasn1.compiler.iso8823_presentation.UserData;

public class PresentationLayerTest {

    @Test
    public void encodingDecoding() throws IOException {

        BerByteArrayOutputStream berOS = new BerByteArrayOutputStream(1000);

        List<TransferSyntaxName> berObjectIdentifierList = new ArrayList<>(1);
        berObjectIdentifierList.add(new TransferSyntaxName(new int[] { 2, 1, 1 }));

        ContextList.SEQUENCE.TransferSyntaxNameList tsnl = new ContextList.SEQUENCE.TransferSyntaxNameList(
                berObjectIdentifierList);

        ContextList.SEQUENCE context_listSubSeq = new ContextList.SEQUENCE(new PresentationContextIdentifier(1),
                new AbstractSyntaxName(new int[] { 2, 2, 1, 0, 1 }), tsnl);

        ContextList.SEQUENCE context_listSubSeq2 = new ContextList.SEQUENCE(new PresentationContextIdentifier(3),
                new AbstractSyntaxName(new int[] { 1, 0, 9506, 2, 1 }), tsnl);

        List<ContextList.SEQUENCE> context_listSubSeqList = new ArrayList<>(2);

        context_listSubSeqList.add(context_listSubSeq);
        context_listSubSeqList.add(context_listSubSeq2);

        PresentationContextDefinitionList context_list = new PresentationContextDefinitionList(context_listSubSeqList);

        PDVList.PresentationDataValues presDataValues = new PDVList.PresentationDataValues(
                new BerAny(new byte[] { 2, 1, 1 }), null, null);
        PDVList pdvList = new PDVList(null, new PresentationContextIdentifier(1), presDataValues);
        List<PDVList> pdvListList = new ArrayList<>(1);
        pdvListList.add(pdvList);
        FullyEncodedData fullyEncodedData = new FullyEncodedData(pdvListList);
        UserData userData = new UserData(null, fullyEncodedData);

        CPType.NormalModeParameters normalModeParameter = new CPType.NormalModeParameters(null,
                new CallingPresentationSelector(new byte[] { 0, 0, 0, 1 }),
                new CalledPresentationSelector(new byte[] { 0, 0, 0, 1 }), context_list, null, null, null, userData);

        ModeSelector modeSelector = new ModeSelector(new BerInteger(1));

        CPType cpType = new CPType(modeSelector, normalModeParameter);

        cpType.encode(berOS, true);

        ByteArrayInputStream bais = new ByteArrayInputStream(berOS.getArray());

        CPType cpType_decoded = new CPType();
        cpType_decoded.decode(bais, true);

        Assert.assertEquals("2.2.1.0.1",
                cpType_decoded.normalModeParameters.presentationContextDefinitionList.seqOf.get(0).abstractSyntaxName
                        .toString());

    }

}
