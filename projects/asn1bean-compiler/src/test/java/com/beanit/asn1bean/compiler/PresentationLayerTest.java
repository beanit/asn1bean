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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerAny;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.compiler.iso8823_presentation.AbstractSyntaxName;
import com.beanit.asn1bean.compiler.iso8823_presentation.CPType;
import com.beanit.asn1bean.compiler.iso8823_presentation.CalledPresentationSelector;
import com.beanit.asn1bean.compiler.iso8823_presentation.CallingPresentationSelector;
import com.beanit.asn1bean.compiler.iso8823_presentation.ContextList;
import com.beanit.asn1bean.compiler.iso8823_presentation.FullyEncodedData;
import com.beanit.asn1bean.compiler.iso8823_presentation.ModeSelector;
import com.beanit.asn1bean.compiler.iso8823_presentation.PDVList;
import com.beanit.asn1bean.compiler.iso8823_presentation.PresentationContextDefinitionList;
import com.beanit.asn1bean.compiler.iso8823_presentation.PresentationContextIdentifier;
import com.beanit.asn1bean.compiler.iso8823_presentation.TransferSyntaxName;
import com.beanit.asn1bean.compiler.iso8823_presentation.UserData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PresentationLayerTest {

  @Test
  public void encodingDecoding() throws IOException {

    ReverseByteArrayOutputStream berOS = new ReverseByteArrayOutputStream(1000);

    List<TransferSyntaxName> berObjectIdentifierList = new ArrayList<>(1);
    berObjectIdentifierList.add(new TransferSyntaxName(new int[] {2, 1, 1}));

    ContextList.SEQUENCE.TransferSyntaxNameList tsnl =
        new ContextList.SEQUENCE.TransferSyntaxNameList(berObjectIdentifierList);

    ContextList.SEQUENCE context_listSubSeq =
        new ContextList.SEQUENCE(
            new PresentationContextIdentifier(1),
            new AbstractSyntaxName(new int[] {2, 2, 1, 0, 1}),
            tsnl);

    ContextList.SEQUENCE context_listSubSeq2 =
        new ContextList.SEQUENCE(
            new PresentationContextIdentifier(3),
            new AbstractSyntaxName(new int[] {1, 0, 9506, 2, 1}),
            tsnl);

    List<ContextList.SEQUENCE> context_listSubSeqList = new ArrayList<>(2);

    context_listSubSeqList.add(context_listSubSeq);
    context_listSubSeqList.add(context_listSubSeq2);

    PresentationContextDefinitionList context_list =
        new PresentationContextDefinitionList(context_listSubSeqList);

    PDVList.PresentationDataValues presDataValues =
        new PDVList.PresentationDataValues(new BerAny(new byte[] {2, 1, 1}), null, null);
    PDVList pdvList = new PDVList(null, new PresentationContextIdentifier(1), presDataValues);
    List<PDVList> pdvListList = new ArrayList<>(1);
    pdvListList.add(pdvList);
    FullyEncodedData fullyEncodedData = new FullyEncodedData(pdvListList);
    UserData userData = new UserData(null, fullyEncodedData);

    CPType.NormalModeParameters normalModeParameter =
        new CPType.NormalModeParameters(
            null,
            new CallingPresentationSelector(new byte[] {0, 0, 0, 1}),
            new CalledPresentationSelector(new byte[] {0, 0, 0, 1}),
            context_list,
            null,
            null,
            null,
            userData);

    ModeSelector modeSelector = new ModeSelector(new BerInteger(1));

    CPType cpType = new CPType(modeSelector, normalModeParameter);

    cpType.encode(berOS, true);

    ByteArrayInputStream bais = new ByteArrayInputStream(berOS.getArray());

    CPType cpType_decoded = new CPType();
    cpType_decoded.decode(bais, true);

    assertEquals(
        "2.2.1.0.1",
        cpType_decoded
            .normalModeParameters
            .presentationContextDefinitionList
            .seqOf
            .get(0)
            .abstractSyntaxName
            .toString());
  }
}
