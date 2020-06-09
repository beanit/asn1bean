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
package com.beanit.jasn1.ber.types;

import com.beanit.jasn1.ber.BerLength;
import com.beanit.jasn1.ber.BerTag;
import com.beanit.jasn1.ber.ReverseByteArrayOutputStream;
import com.beanit.jasn1.ber.types.string.BerObjectDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class BerEmbeddedPdv implements BerType, Serializable {

  public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 11);
  private static final long serialVersionUID = 1L;
  private byte[] code = null;
  private Identification identification = null;
  private BerObjectDescriptor dataValueDescriptor = null;
  private BerOctetString dataValue = null;

  public BerEmbeddedPdv() {}

  public BerEmbeddedPdv(byte[] code) {
    this.code = code;
  }

  public Identification getIdentification() {
    return identification;
  }

  public void setIdentification(Identification identification) {
    this.identification = identification;
  }

  public BerObjectDescriptor getDataValueDescriptor() {
    return dataValueDescriptor;
  }

  public void setDataValueDescriptor(BerObjectDescriptor dataValueDescriptor) {
    this.dataValueDescriptor = dataValueDescriptor;
  }

  public BerOctetString getDataValue() {
    return dataValue;
  }

  public void setDataValue(BerOctetString dataValue) {
    this.dataValue = dataValue;
  }

  @Override
  public int encode(OutputStream reverseOS) throws IOException {
    return encode(reverseOS, true);
  }

  public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

    if (code != null) {
      reverseOS.write(code);
      if (withTag) {
        return tag.encode(reverseOS) + code.length;
      }
      return code.length;
    }

    int codeLength = 0;
    int sublength;

    codeLength += dataValue.encode(reverseOS, false);
    // write tag: CONTEXT_CLASS, PRIMITIVE, 2
    reverseOS.write(0x82);
    codeLength += 1;

    if (dataValueDescriptor != null) {
      codeLength += dataValueDescriptor.encode(reverseOS, false);
      // write tag: CONTEXT_CLASS, PRIMITIVE, 1
      reverseOS.write(0x81);
      codeLength += 1;
    }

    sublength = identification.encode(reverseOS);
    codeLength += sublength;
    codeLength += BerLength.encodeLength(reverseOS, sublength);
    // write tag: CONTEXT_CLASS, CONSTRUCTED, 0
    reverseOS.write(0xA0);
    codeLength += 1;

    codeLength += BerLength.encodeLength(reverseOS, codeLength);

    if (withTag) {
      codeLength += tag.encode(reverseOS);
    }

    return codeLength;
  }

  @Override
  public int decode(InputStream is) throws IOException {
    return decode(is, true);
  }

  public int decode(InputStream is, boolean withTag) throws IOException {
    int codeLength = 0;
    int subCodeLength = 0;
    BerTag berTag = new BerTag();

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    int totalLength = length.val;
    codeLength += totalLength;

    subCodeLength += berTag.decode(is);
    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
      subCodeLength += length.decode(is);
      identification = new Identification();
      subCodeLength += identification.decode(is, null);
      subCodeLength += berTag.decode(is);
    } else {
      throw new IOException("Tag does not match the mandatory sequence element tag.");
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
      dataValueDescriptor = new BerObjectDescriptor();
      subCodeLength += dataValueDescriptor.decode(is, false);
      subCodeLength += berTag.decode(is);
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
      dataValue = new BerOctetString();
      subCodeLength += dataValue.decode(is, false);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
    }
    throw new IOException(
        "Unexpected end of sequence, length tag: "
            + totalLength
            + ", actual sequence length: "
            + subCodeLength);
  }

  public void encodeAndSave(int encodingSizeGuess) throws IOException {
    ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
    encode(reverseOS, false);
    code = reverseOS.getArray();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendAsString(sb, 0);
    return sb.toString();
  }

  public void appendAsString(StringBuilder sb, int indentLevel) {

    sb.append("{");
    sb.append("\n");
    for (int i = 0; i < indentLevel + 1; i++) {
      sb.append("\t");
    }
    if (identification != null) {
      sb.append("identification: ");
      identification.appendAsString(sb, indentLevel + 1);
    } else {
      sb.append("identification: <empty-required-field>");
    }

    if (dataValueDescriptor != null) {
      sb.append(",\n");
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("dataValueDescriptor: ").append(dataValueDescriptor);
    }

    sb.append(",\n");
    for (int i = 0; i < indentLevel + 1; i++) {
      sb.append("\t");
    }
    if (dataValue != null) {
      sb.append("dataValue: ").append(dataValue);
    } else {
      sb.append("dataValue: <empty-required-field>");
    }

    sb.append("\n");
    for (int i = 0; i < indentLevel; i++) {
      sb.append("\t");
    }
    sb.append("}");
  }

  public static class Identification implements BerType, Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] code = null;
    private Syntaxes syntaxes = null;
    private BerObjectIdentifier syntax = null;
    private BerInteger presentationContextId = null;
    private ContextNegotiation contextNegotiation = null;
    private BerObjectIdentifier transferSyntax = null;
    private BerNull fixed = null;

    public Identification() {}

    public Identification(byte[] code) {
      this.code = code;
    }

    public Syntaxes getSyntaxes() {
      return syntaxes;
    }

    public void setSyntaxes(Syntaxes syntaxes) {
      this.syntaxes = syntaxes;
    }

    public BerObjectIdentifier getSyntax() {
      return syntax;
    }

    public void setSyntax(BerObjectIdentifier syntax) {
      this.syntax = syntax;
    }

    public BerInteger getPresentationContextId() {
      return presentationContextId;
    }

    public void setPresentationContextId(BerInteger presentationContextId) {
      this.presentationContextId = presentationContextId;
    }

    public ContextNegotiation getContextNegotiation() {
      return contextNegotiation;
    }

    public void setContextNegotiation(ContextNegotiation contextNegotiation) {
      this.contextNegotiation = contextNegotiation;
    }

    public BerObjectIdentifier getTransferSyntax() {
      return transferSyntax;
    }

    public void setTransferSyntax(BerObjectIdentifier transferSyntax) {
      this.transferSyntax = transferSyntax;
    }

    public BerNull getFixed() {
      return fixed;
    }

    public void setFixed(BerNull fixed) {
      this.fixed = fixed;
    }

    @Override
    public int encode(OutputStream reverseOS) throws IOException {

      if (code != null) {
        reverseOS.write(code);
        return code.length;
      }

      int codeLength = 0;
      if (fixed != null) {
        codeLength += fixed.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 5
        reverseOS.write(0x85);
        codeLength += 1;
        return codeLength;
      }

      if (transferSyntax != null) {
        codeLength += transferSyntax.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 4
        reverseOS.write(0x84);
        codeLength += 1;
        return codeLength;
      }

      if (contextNegotiation != null) {
        codeLength += contextNegotiation.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, CONSTRUCTED, 3
        reverseOS.write(0xA3);
        codeLength += 1;
        return codeLength;
      }

      if (presentationContextId != null) {
        codeLength += presentationContextId.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 2
        reverseOS.write(0x82);
        codeLength += 1;
        return codeLength;
      }

      if (syntax != null) {
        codeLength += syntax.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 1
        reverseOS.write(0x81);
        codeLength += 1;
        return codeLength;
      }

      if (syntaxes != null) {
        codeLength += syntaxes.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, CONSTRUCTED, 0
        reverseOS.write(0xA0);
        codeLength += 1;
        return codeLength;
      }

      throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
    }

    @Override
    public int decode(InputStream is) throws IOException {
      return decode(is, null);
    }

    public int decode(InputStream is, BerTag berTag) throws IOException {

      int codeLength = 0;
      BerTag passedTag = berTag;

      if (berTag == null) {
        berTag = new BerTag();
        codeLength += berTag.decode(is);
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
        syntaxes = new Syntaxes();
        codeLength += syntaxes.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
        syntax = new BerObjectIdentifier();
        codeLength += syntax.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
        presentationContextId = new BerInteger();
        codeLength += presentationContextId.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 3)) {
        contextNegotiation = new ContextNegotiation();
        codeLength += contextNegotiation.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 4)) {
        transferSyntax = new BerObjectIdentifier();
        codeLength += transferSyntax.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 5)) {
        fixed = new BerNull();
        codeLength += fixed.decode(is, false);
        return codeLength;
      }

      if (passedTag != null) {
        return 0;
      }

      throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
      ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
      encode(reverseOS);
      code = reverseOS.getArray();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      appendAsString(sb, 0);
      return sb.toString();
    }

    public void appendAsString(StringBuilder sb, int indentLevel) {

      if (syntaxes != null) {
        sb.append("syntaxes: ");
        syntaxes.appendAsString(sb, indentLevel + 1);
        return;
      }

      if (syntax != null) {
        sb.append("syntax: ").append(syntax);
        return;
      }

      if (presentationContextId != null) {
        sb.append("presentationContextId: ").append(presentationContextId);
        return;
      }

      if (contextNegotiation != null) {
        sb.append("contextNegotiation: ");
        contextNegotiation.appendAsString(sb, indentLevel + 1);
        return;
      }

      if (transferSyntax != null) {
        sb.append("transferSyntax: ").append(transferSyntax);
        return;
      }

      if (fixed != null) {
        sb.append("fixed: ").append(fixed);
        return;
      }

      sb.append("<none>");
    }

    public static class Syntaxes implements BerType, Serializable {

      public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
      private static final long serialVersionUID = 1L;
      private byte[] code = null;
      private BerObjectIdentifier abstract_ = null;
      private BerObjectIdentifier transfer = null;

      public Syntaxes() {}

      public Syntaxes(byte[] code) {
        this.code = code;
      }

      public BerObjectIdentifier getAbstract() {
        return abstract_;
      }

      public void setAbstract(BerObjectIdentifier abstract_) {
        this.abstract_ = abstract_;
      }

      public BerObjectIdentifier getTransfer() {
        return transfer;
      }

      public void setTransfer(BerObjectIdentifier transfer) {
        this.transfer = transfer;
      }

      @Override
      public int encode(OutputStream reverseOS) throws IOException {
        return encode(reverseOS, true);
      }

      public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        if (code != null) {
          reverseOS.write(code);
          if (withTag) {
            return tag.encode(reverseOS) + code.length;
          }
          return code.length;
        }

        int codeLength = 0;
        codeLength += transfer.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 1
        reverseOS.write(0x81);
        codeLength += 1;

        codeLength += abstract_.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 0
        reverseOS.write(0x80);
        codeLength += 1;

        codeLength += BerLength.encodeLength(reverseOS, codeLength);

        if (withTag) {
          codeLength += tag.encode(reverseOS);
        }

        return codeLength;
      }

      @Override
      public int decode(InputStream is) throws IOException {
        return decode(is, true);
      }

      public int decode(InputStream is, boolean withTag) throws IOException {
        int codeLength = 0;
        int subCodeLength = 0;
        BerTag berTag = new BerTag();

        if (withTag) {
          codeLength += tag.decodeAndCheck(is);
        }

        BerLength length = new BerLength();
        codeLength += length.decode(is);

        int totalLength = length.val;
        codeLength += totalLength;

        subCodeLength += berTag.decode(is);
        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
          abstract_ = new BerObjectIdentifier();
          subCodeLength += abstract_.decode(is, false);
          subCodeLength += berTag.decode(is);
        } else {
          throw new IOException("Tag does not match the mandatory sequence element tag.");
        }

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
          transfer = new BerObjectIdentifier();
          subCodeLength += transfer.decode(is, false);
          if (subCodeLength == totalLength) {
            return codeLength;
          }
        }
        throw new IOException(
            "Unexpected end of sequence, length tag: "
                + totalLength
                + ", actual sequence length: "
                + subCodeLength);
      }

      public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream reverseOS =
            new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(reverseOS, false);
        code = reverseOS.getArray();
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        appendAsString(sb, 0);
        return sb.toString();
      }

      public void appendAsString(StringBuilder sb, int indentLevel) {

        sb.append("{");
        sb.append("\n");
        for (int i = 0; i < indentLevel + 1; i++) {
          sb.append("\t");
        }
        if (abstract_ != null) {
          sb.append("abstract_: ").append(abstract_);
        } else {
          sb.append("abstract_: <empty-required-field>");
        }

        sb.append(",\n");
        for (int i = 0; i < indentLevel + 1; i++) {
          sb.append("\t");
        }
        if (transfer != null) {
          sb.append("transfer: ").append(transfer);
        } else {
          sb.append("transfer: <empty-required-field>");
        }

        sb.append("\n");
        for (int i = 0; i < indentLevel; i++) {
          sb.append("\t");
        }
        sb.append("}");
      }
    }

    public static class ContextNegotiation implements BerType, Serializable {

      public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
      private static final long serialVersionUID = 1L;
      private byte[] code = null;
      private BerInteger presentationContextId = null;
      private BerObjectIdentifier transferSyntax = null;

      public ContextNegotiation() {}

      public ContextNegotiation(byte[] code) {
        this.code = code;
      }

      public BerInteger getPresentationContextId() {
        return presentationContextId;
      }

      public void setPresentationContextId(BerInteger presentationContextId) {
        this.presentationContextId = presentationContextId;
      }

      public BerObjectIdentifier getTransferSyntax() {
        return transferSyntax;
      }

      public void setTransferSyntax(BerObjectIdentifier transferSyntax) {
        this.transferSyntax = transferSyntax;
      }

      @Override
      public int encode(OutputStream reverseOS) throws IOException {
        return encode(reverseOS, true);
      }

      public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        if (code != null) {
          reverseOS.write(code);
          if (withTag) {
            return tag.encode(reverseOS) + code.length;
          }
          return code.length;
        }

        int codeLength = 0;
        codeLength += transferSyntax.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 1
        reverseOS.write(0x81);
        codeLength += 1;

        codeLength += presentationContextId.encode(reverseOS, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 0
        reverseOS.write(0x80);
        codeLength += 1;

        codeLength += BerLength.encodeLength(reverseOS, codeLength);

        if (withTag) {
          codeLength += tag.encode(reverseOS);
        }

        return codeLength;
      }

      @Override
      public int decode(InputStream is) throws IOException {
        return decode(is, true);
      }

      public int decode(InputStream is, boolean withTag) throws IOException {
        int codeLength = 0;
        int subCodeLength = 0;
        BerTag berTag = new BerTag();

        if (withTag) {
          codeLength += tag.decodeAndCheck(is);
        }

        BerLength length = new BerLength();
        codeLength += length.decode(is);

        int totalLength = length.val;
        codeLength += totalLength;

        subCodeLength += berTag.decode(is);
        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
          presentationContextId = new BerInteger();
          subCodeLength += presentationContextId.decode(is, false);
          subCodeLength += berTag.decode(is);
        } else {
          throw new IOException("Tag does not match the mandatory sequence element tag.");
        }

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
          transferSyntax = new BerObjectIdentifier();
          subCodeLength += transferSyntax.decode(is, false);
          if (subCodeLength == totalLength) {
            return codeLength;
          }
        }
        throw new IOException(
            "Unexpected end of sequence, length tag: "
                + totalLength
                + ", actual sequence length: "
                + subCodeLength);
      }

      public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream reverseOS =
            new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(reverseOS, false);
        code = reverseOS.getArray();
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        appendAsString(sb, 0);
        return sb.toString();
      }

      public void appendAsString(StringBuilder sb, int indentLevel) {

        sb.append("{");
        sb.append("\n");
        for (int i = 0; i < indentLevel + 1; i++) {
          sb.append("\t");
        }
        if (presentationContextId != null) {
          sb.append("presentationContextId: ").append(presentationContextId);
        } else {
          sb.append("presentationContextId: <empty-required-field>");
        }

        sb.append(",\n");
        for (int i = 0; i < indentLevel + 1; i++) {
          sb.append("\t");
        }
        if (transferSyntax != null) {
          sb.append("transferSyntax: ").append(transferSyntax);
        } else {
          sb.append("transferSyntax: <empty-required-field>");
        }

        sb.append("\n");
        for (int i = 0; i < indentLevel; i++) {
          sb.append("\t");
        }
        sb.append("}");
      }
    }
  }
}
