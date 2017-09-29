/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.types.BerObjectIdentifier;
import org.openmuc.jasn1.compiler.model.AsnAny;
import org.openmuc.jasn1.compiler.model.AsnBitString;
import org.openmuc.jasn1.compiler.model.AsnBoolean;
import org.openmuc.jasn1.compiler.model.AsnCharacterString;
import org.openmuc.jasn1.compiler.model.AsnChoice;
import org.openmuc.jasn1.compiler.model.AsnClassNumber;
import org.openmuc.jasn1.compiler.model.AsnConstructedType;
import org.openmuc.jasn1.compiler.model.AsnDefinedType;
import org.openmuc.jasn1.compiler.model.AsnElementType;
import org.openmuc.jasn1.compiler.model.AsnEmbeddedPdv;
import org.openmuc.jasn1.compiler.model.AsnEnum;
import org.openmuc.jasn1.compiler.model.AsnInformationObjectClass;
import org.openmuc.jasn1.compiler.model.AsnInteger;
import org.openmuc.jasn1.compiler.model.AsnModule;
import org.openmuc.jasn1.compiler.model.AsnModule.TagDefault;
import org.openmuc.jasn1.compiler.model.AsnNull;
import org.openmuc.jasn1.compiler.model.AsnObjectIdentifier;
import org.openmuc.jasn1.compiler.model.AsnOctetString;
import org.openmuc.jasn1.compiler.model.AsnParameter;
import org.openmuc.jasn1.compiler.model.AsnReal;
import org.openmuc.jasn1.compiler.model.AsnSequenceOf;
import org.openmuc.jasn1.compiler.model.AsnSequenceSet;
import org.openmuc.jasn1.compiler.model.AsnTag;
import org.openmuc.jasn1.compiler.model.AsnTaggedType;
import org.openmuc.jasn1.compiler.model.AsnType;
import org.openmuc.jasn1.compiler.model.AsnUniversalType;
import org.openmuc.jasn1.compiler.model.AsnValueAssignment;
import org.openmuc.jasn1.compiler.model.SymbolsFromModule;

public class BerClassWriter {

    private static Tag stdSeqTag = new Tag();
    private static Tag stdSetTag = new Tag();

    static {
        stdSeqTag.tagClass = TagClass.UNIVERSAL;
        stdSeqTag.value = 16;
        stdSeqTag.typeStructure = TypeStructure.CONSTRUCTED;

        stdSetTag.tagClass = TagClass.UNIVERSAL;
        stdSetTag.value = 17;
        stdSetTag.typeStructure = TypeStructure.CONSTRUCTED;
    }

    public enum TagClass {
        UNIVERSAL,
        APPLICATION,
        CONTEXT,
        PRIVATE
    };

    public enum TagType {
        EXPLICIT,
        IMPLICIT
    };

    public enum TypeStructure {
        PRIMITIVE,
        CONSTRUCTED
    }

    public static class Tag {
        public int value;
        public TagClass tagClass;
        public TagType type;
        public TypeStructure typeStructure;
    }

    private static final Set<String> reservedKeywords = Collections
            .unmodifiableSet(new TreeSet<>(Arrays.asList("public", "private", "protected", "final", "void", "int",
                    "short", "float", "double", "long", "byte", "char", "String", "throw", "throws", "new", "static",
                    "volatile", "if", "else", "for", "switch", "case", "enum", "this", "super", "boolean", "class",
                    "abstract", "package", "import", "null", "code", "getClass", "setClass")));

    private TagDefault tagDefault;
    private File outputBaseDir;
    private final String basePackageName;
    private int indentNum = 0;
    BufferedWriter out;

    private final boolean supportIndefiniteLength;
    private final boolean jaxbMode;
    private final HashMap<String, AsnModule> modulesByName;
    private AsnModule module;
    private File outputDirectory;

    BerClassWriter(HashMap<String, AsnModule> modulesByName, String outputBaseDir, String basePackageName,
            boolean jaxbMode, boolean supportIndefiniteLength) throws IOException {
        this.supportIndefiniteLength = supportIndefiniteLength;
        this.jaxbMode = jaxbMode;
        this.outputBaseDir = new File(outputBaseDir);

        if (basePackageName.isEmpty()) {
            this.basePackageName = "";
        }
        else {
            this.outputBaseDir = new File(this.outputBaseDir, basePackageName.replace('.', '/'));
            this.basePackageName = basePackageName + ".";
        }
        this.modulesByName = modulesByName;
    }

    public void translate() throws IOException {
        for (AsnModule module : modulesByName.values()) {
            for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
                if (modulesByName.get(symbolsFromModule.modref) == null) {
                    throw new IOException("Module \"" + module.moduleIdentifier.name + "\" imports missing module \""
                            + symbolsFromModule.modref + "\".");
                }
            }
        }

        for (AsnModule module : modulesByName.values()) {
            translateModule(module);
        }

    }

    int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }

    public void translateModule(AsnModule module) throws IOException {

        System.out.println("Generating classes for module \"" + module.moduleIdentifier.name + "\"");

        outputDirectory = new File(outputBaseDir,
                sanitizeModuleName(module.moduleIdentifier.name).replace('-', '/').toLowerCase());

        this.module = module;
        tagDefault = module.tagDefault;

        for (AsnType typeDefinition : module.typesByName.values()) {

            if (typeDefinition instanceof AsnDefinedType) {
                if (getInformationObjectClass(((AsnDefinedType) typeDefinition).typeName, module) != null) {
                    continue;
                }
            }

            String typeName = cleanUpName(typeDefinition.name);

            writeClassHeader(typeName, module);

            if (typeDefinition instanceof AsnTaggedType) {

                AsnTaggedType asnTaggedType = (AsnTaggedType) typeDefinition;

                Tag tag = getTag(asnTaggedType);

                if (asnTaggedType.definedType != null) {
                    writeRetaggingTypeClass(typeName, asnTaggedType.definedType.typeName, typeDefinition, tag);
                }
                else {

                    AsnType assignedAsnType = asnTaggedType.typeReference;

                    if (assignedAsnType instanceof AsnConstructedType) {
                        writeConstructedTypeClass(typeName, assignedAsnType, tag, false, null);
                    }
                    else {
                        writeRetaggingTypeClass(typeName, getBerType(assignedAsnType), typeDefinition, tag);
                    }
                }

            }
            else if (typeDefinition instanceof AsnDefinedType) {
                writeRetaggingTypeClass(typeName, ((AsnDefinedType) typeDefinition).typeName, typeDefinition, null);
            }
            else if (typeDefinition instanceof AsnConstructedType) {
                writeConstructedTypeClass(typeName, typeDefinition, null, false, null);
            }
            else {
                writeRetaggingTypeClass(typeName, getBerType(typeDefinition), typeDefinition, null);
            }

            out.close();
        }

        writeOidValues(module);

    }

    private void writeOidValues(AsnModule module) throws IOException {
        boolean first = true;
        List<String> values = new ArrayList<>(module.asnValueAssignmentsByName.keySet());
        Collections.sort(values);
        for (String valueName : values) {
            if (module.asnValueAssignmentsByName.get(valueName).type instanceof AsnObjectIdentifier) {
                BerObjectIdentifier oid;
                try {
                    oid = parseObjectIdentfierValue(valueName, module);
                } catch (IllegalStateException e) {
                    System.out.println("Warning: could not parse object identifier value: " + e.getMessage());
                    continue;
                }
                StringBuilder sb = new StringBuilder("public static final BerObjectIdentifier " + cleanUpName(valueName)
                        + " = new BerObjectIdentifier(new int[]{");
                if (first == true) {
                    first = false;
                    writeClassHeader("OidValues", module);
                    write("public final class OidValues {");
                }

                boolean firstOidComponent = true;
                for (int i : oid.value) {
                    if (firstOidComponent) {
                        firstOidComponent = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(i);
                }
                sb.append("});");
                write(sb.toString());

            }
        }
        if (first == false) {
            write("}");
            out.close();
        }
    }

    private String sanitizeModuleName(String name) {
        String[] moduleParts = name.split("-");
        String toReturn = "";
        for (String part : moduleParts) {
            toReturn += sanitize(part.toLowerCase()) + "-";
        }
        if (!toReturn.isEmpty()) {
            toReturn = toReturn.substring(0, toReturn.length() - 1);
        }
        return toReturn;
    }

    private BerObjectIdentifier parseObjectIdentfierValue(String name, AsnModule module) throws IOException {

        AsnValueAssignment valueAssignment = module.asnValueAssignmentsByName.get(name);

        if (valueAssignment == null || !(valueAssignment.type instanceof AsnObjectIdentifier)) {
            return null;
            // throw new IOException(
            // "no object identifier named \"" + name + "\" in module \"" + module.moduleIdentifier.name);
        }

        if (!valueAssignment.value.isValueInBraces) {
            throw new IllegalStateException(
                    "value of object identifier \"" + valueAssignment.name + "\" is not defined in braces.");
        }
        List<Integer> oidComponents = new ArrayList<>();

        List<String> tokens = valueAssignment.value.valueInBracesTokens;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (Character.isDigit(token.charAt(0))) {
                oidComponents.add(Integer.parseInt(token));
            }
            else if (Character.isLetter(token.charAt(0))) {
                if ((tokens.size() == i + 1) || !tokens.get(i + 1).equals("(")) {
                    // this is either a value reference of another object identifier or a registered name
                    if (!parseRegisteredOidComponentName(oidComponents, token)) {

                        BerObjectIdentifier oid = parseObjectIdentfierValue(token, module);
                        if (oid == null) {
                            for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
                                for (String importedTypeName : symbolsFromModule.symbolList) {
                                    if (token.equals(importedTypeName)) {
                                        oid = parseObjectIdentfierValue(token,
                                                modulesByName.get(symbolsFromModule.modref));
                                    }
                                }
                            }
                        }
                        if (oid == null) {
                            throw new IllegalStateException("AsnValueAssigment \"" + token
                                    + "\" was not found in module \"" + module.moduleIdentifier.name + "\"");
                        }
                        for (int element : oid.value) {
                            oidComponents.add(element);
                        }
                    }
                }
            }
        }
        return new BerObjectIdentifier(toIntArray(oidComponents));
    }

    private boolean parseRegisteredOidComponentName(List<Integer> oidComponents, String token) throws IOException {
        if (oidComponents.size() == 0) {
            switch (token) {
            case "itu-t":
            case "ccitt":
                oidComponents.add(0);
                return true;
            case "iso":
                oidComponents.add(1);
                return true;
            case "joint-iso-itu-t":
            case "joint-iso-ccitt":
                oidComponents.add(2);
                return true;
            }
        }
        else if (oidComponents.size() == 1) {
            switch (oidComponents.get(0)) {
            case 0:
                switch (token) {
                case "recommendation":
                    oidComponents.add(0);
                    return true;
                case "question":
                    oidComponents.add(1);
                    return true;
                case "administration":
                    oidComponents.add(2);
                    return true;
                case "network-operator":
                    oidComponents.add(3);
                    return true;
                case "identified-organization":
                    oidComponents.add(4);
                    return true;
                }

            case 1:
                switch (token) {
                case "standard":
                    oidComponents.add(0);
                    return true;
                case "registration-authority":
                    oidComponents.add(1);
                    return true;
                case "member-body":
                    oidComponents.add(2);
                    return true;
                case "identified-organization":
                    oidComponents.add(3);
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Gets the tag from the AsnTaggedType structure. The returned tag will contain the correct class and type (explicit
     * or implicit). Return null if the passed tagged type does not have a tag.
     * 
     * @param asnTaggedType
     * @return the tag from the AsnTaggedType structure
     * @throws IOException
     */
    private Tag getTag(AsnTaggedType asnTaggedType) throws IOException {

        AsnTag asnTag = asnTaggedType.tag;

        if (asnTag == null) {
            return null;
        }

        Tag tag = new Tag();

        String tagClassString = asnTag.clazz;
        if (tagClassString.isEmpty() || "CONTEXT".equals(tagClassString)) {
            tag.tagClass = TagClass.CONTEXT;
        }
        else if ("APPLICATION".equals(tagClassString)) {
            tag.tagClass = TagClass.APPLICATION;
        }
        else if ("PRIVATE".equals(tagClassString)) {
            tag.tagClass = TagClass.PRIVATE;
        }
        else if ("UNIVERSAL".equals(tagClassString)) {
            tag.tagClass = TagClass.UNIVERSAL;
        }
        else {
            throw new IllegalStateException("unknown tag class: " + tagClassString);
        }

        String tagTypeString = asnTaggedType.tagType;

        if (tagTypeString.isEmpty()) {
            if (tagDefault == TagDefault.EXPLICIT) {
                tag.type = TagType.EXPLICIT;
            }
            else {
                tag.type = TagType.IMPLICIT;
            }
        }
        else if (tagTypeString.equals("IMPLICIT")) {
            tag.type = TagType.IMPLICIT;
        }
        else if (tagTypeString.equals("EXPLICIT")) {
            tag.type = TagType.EXPLICIT;
        }
        else {
            throw new IllegalStateException("unexpected tag type: " + tagTypeString);
        }

        if (tag.type == TagType.IMPLICIT) {
            if (isDirectAnyOrChoice(asnTaggedType)) {
                tag.type = TagType.EXPLICIT;
            }
        }

        if ((tag.type == TagType.IMPLICIT) && isPrimitive(asnTaggedType)) {
            tag.typeStructure = TypeStructure.PRIMITIVE;
        }
        else {
            tag.typeStructure = TypeStructure.CONSTRUCTED;
        }

        tag.value = asnTaggedType.tag.classNumber.num;

        return tag;

    }

    private String cleanUpName(String name) {

        name = replaceCharByCamelCase(name, '-');
        name = replaceCharByCamelCase(name, '_');

        return sanitize(name);
    }

    private String sanitize(String name) {
        String result = replaceCharByCamelCase(name, '.');
        if (reservedKeywords.contains(result)) {
            result += "_";
        }
        return result;
    }

    private String replaceCharByCamelCase(String name, char charToBeReplaced) {
        StringBuilder nameSb = new StringBuilder(name);

        int index = name.indexOf(charToBeReplaced);
        while (index != -1 && index != (name.length() - 1)) {
            if (!Character.isUpperCase(name.charAt(index + 1))) {
                nameSb.setCharAt(index + 1, Character.toUpperCase(name.charAt(index + 1)));
            }
            index = name.indexOf(charToBeReplaced, index + 1);
        }

        name = nameSb.toString();
        name = name.replace("" + charToBeReplaced, "");

        return name;
    }

    private void writeConstructedTypeClass(String className, AsnType asnType, Tag tag, boolean asInternalClass,
            List<String> listOfSubClassNames) throws IOException {

        if (listOfSubClassNames == null) {
            listOfSubClassNames = new ArrayList<>();
        }

        String isStaticStr = "";
        if (asInternalClass) {
            isStaticStr = " static";
        }

        if (asnType instanceof AsnSequenceSet) {
            writeSequenceOrSetClass(className, (AsnSequenceSet) asnType, tag, isStaticStr, listOfSubClassNames);
        }
        else if (asnType instanceof AsnSequenceOf) {
            writeSequenceOfClass(className, (AsnSequenceOf) asnType, tag, isStaticStr, listOfSubClassNames);
        }
        else if (asnType instanceof AsnChoice) {
            writeChoiceClass(className, (AsnChoice) asnType, tag, isStaticStr, listOfSubClassNames);
        }
    }

    private void writeChoiceClass(String className, AsnChoice asn1TypeElement, Tag tag, String isStaticStr,
            List<String> listOfSubClassNames) throws IOException {

        write("public" + isStaticStr + " class " + className + " implements Serializable {\n");

        write("private static final long serialVersionUID = 1L;\n");

        write("public byte[] code = null;");

        if (tag != null) {
            write("public static final BerTag tag = new BerTag(" + getBerTagParametersString(tag) + ");\n");

        }

        List<AsnElementType> componentTypes = asn1TypeElement.componentTypes;

        addAutomaticTagsIfNeeded(componentTypes);

        if (asn1TypeElement.parameters != null) {
            List<AsnParameter> parameters = asn1TypeElement.parameters;
            replaceParamtersByAnyTypes(componentTypes, parameters);
        }

        for (AsnElementType componentType : componentTypes) {
            if (componentType.typeReference != null && (componentType.typeReference instanceof AsnConstructedType)) {
                listOfSubClassNames.add(getClassNameOfComponent(componentType));
            }
        }

        for (AsnElementType componentType : componentTypes) {

            if (isInnerType(componentType)) {

                String subClassName = getClassNameOfComponent(componentType);
                writeConstructedTypeClass(subClassName, componentType.typeReference, null, true, listOfSubClassNames);

            }
        }

        setClassNamesOfComponents(listOfSubClassNames, componentTypes);

        writePublicMembers(componentTypes);

        writeEmptyConstructor(className);

        if (!jaxbMode) {
            writeEncodeConstructor(className, componentTypes);
        }

        if (jaxbMode) {
            writeGetterAndSetter(componentTypes);
        }

        writeChoiceEncodeFunction(componentTypes, tag != null);

        writeChoiceDecodeFunction(componentTypes, tag != null);

        writeEncodeAndSaveFunction(tag == null);

        writeChoiceToStringFunction(componentTypes);

        write("}\n");

    }

    private void setClassNamesOfComponents(List<String> listOfSubClassNames, List<AsnElementType> componentTypes) {
        for (AsnElementType element : componentTypes) {
            element.className = getClassNameOfComponent(listOfSubClassNames, element);
        }
    }

    private String getClassNameOfComponent(AsnElementType asnElementType) throws IOException {
        if (asnElementType.className != "") {
            return asnElementType.className;
        }
        return getClassNameOfComponent(null, asnElementType);
    }

    private String getClassNameOfComponent(List<String> listOfSubClassNames, AsnTaggedType element) {

        if (listOfSubClassNames == null) {
            listOfSubClassNames = new ArrayList<>();
        }

        if (element.typeReference == null) {

            if (element.definedType.isObjectClassField) {

                AsnInformationObjectClass informationObjectClass = getInformationObjectClass(
                        element.definedType.moduleOrObjectClassReference, module);
                if (informationObjectClass == null) {
                    throw new CompileException("no information object class of name \""
                            + element.definedType.moduleOrObjectClassReference + "\" found");
                }

                for (AsnElementType elementType : informationObjectClass.elementList) {
                    if (elementType.name.equals(element.definedType.typeName)) {
                        return getClassNameOfComponent(listOfSubClassNames, elementType);
                    }
                }

                throw new IllegalStateException(
                        "Could not find field \"" + element.definedType.typeName + "\" of information object class \""
                                + element.definedType.moduleOrObjectClassReference + "\"");
            }
            else {

                String cleanedUpClassName = cleanUpName(element.definedType.typeName);
                for (String subClassName : listOfSubClassNames) {
                    if (subClassName.equals(cleanedUpClassName)) {
                        String moduleName = module.moduleIdentifier.name;

                        for (SymbolsFromModule syms : this.module.importSymbolFromModuleList) {
                            if (syms.symbolList.contains(element.definedType.typeName)) {
                                moduleName = syms.modref;
                                break;
                            }
                        }

                        return basePackageName + sanitizeModuleName(moduleName).replace('-', '.').toLowerCase() + "."
                                + cleanedUpClassName;

                    }

                }
                return cleanedUpClassName;
            }
        }
        else {
            AsnType typeDefinition = element.typeReference;

            if (typeDefinition instanceof AsnConstructedType) {
                return cleanUpName(capitalizeFirstCharacter(element.name));
            }
            else {
                return getBerType(typeDefinition);
            }

        }

    }

    private AsnInformationObjectClass getInformationObjectClass(String objectClassReference, AsnModule module) {
        AsnInformationObjectClass ioClass = module.objectClassesByName.get(objectClassReference);
        if (ioClass == null) {
            AsnType asnType = module.typesByName.get(objectClassReference);
            if (asnType == null) {
                // TODO check for other modules
                return null;
            }
            else {
                if (asnType instanceof AsnDefinedType) {
                    return getInformationObjectClass(((AsnDefinedType) asnType).typeName, module);
                }
            }
        }
        return ioClass;
    }

    private void writeSequenceOrSetClass(String className, AsnSequenceSet asnSequenceSet, Tag tag, String isStaticStr,
            List<String> listOfSubClassNames) throws IOException {

        write("public" + isStaticStr + " class " + className + " implements Serializable {\n");

        write("private static final long serialVersionUID = 1L;\n");

        List<AsnElementType> componentTypes = asnSequenceSet.componentTypes;

        addAutomaticTagsIfNeeded(componentTypes);

        if (asnSequenceSet.parameters != null) {
            List<AsnParameter> parameters = asnSequenceSet.parameters;
            replaceParamtersByAnyTypes(componentTypes, parameters);
        }

        for (AsnElementType componentType : componentTypes) {
            if (componentType.typeReference != null && (componentType.typeReference instanceof AsnConstructedType)) {
                listOfSubClassNames.add(getClassNameOfComponent(componentType));
            }
        }

        for (AsnElementType componentType : componentTypes) {

            if (isInnerType(componentType)) {

                String subClassName = getClassNameOfComponent(componentType);

                writeConstructedTypeClass(subClassName, componentType.typeReference, null, true, listOfSubClassNames);

            }

        }

        Tag mainTag;
        if (tag == null) {
            if (asnSequenceSet.isSequence) {
                mainTag = stdSeqTag;
            }
            else {
                mainTag = stdSetTag;
            }
        }
        else {
            mainTag = tag;
        }

        write("public static final BerTag tag = new BerTag(" + getBerTagParametersString(mainTag) + ");\n");

        write("public byte[] code = null;");

        setClassNamesOfComponents(listOfSubClassNames, componentTypes);

        writePublicMembers(componentTypes);

        writeEmptyConstructor(className);

        if (!jaxbMode) {
            writeEncodeConstructor(className, componentTypes);
        }

        if (jaxbMode) {
            writeGetterAndSetter(componentTypes);
        }

        boolean hasExplicitTag = (tag != null) && (tag.type == TagType.EXPLICIT);

        writeSimpleEncodeFunction();

        writeSequenceOrSetEncodeFunction(componentTypes, hasExplicitTag, asnSequenceSet.isSequence);

        writeSimpleDecodeFunction("true");

        if (asnSequenceSet.isSequence) {
            writeSequenceDecodeFunction(componentTypes, hasExplicitTag);
        }
        else {
            writeSetDecodeFunction(componentTypes);
        }

        writeEncodeAndSaveFunction();

        writeSequenceOrSetToStringFunction(componentTypes);

        write("}\n");

    }

    private void replaceParamtersByAnyTypes(List<AsnElementType> componentTypes, List<AsnParameter> parameters) {
        for (AsnParameter parameter : parameters) {
            if (parameter.paramGovernor == null) {
                for (AsnElementType componentType : componentTypes) {
                    if (componentType.definedType != null
                            && componentType.definedType.typeName.equals(parameter.dummyReference)) {

                        componentType.typeReference = new AsnAny();
                        componentType.definedType = null;
                        componentType.isDefinedType = false;
                    }
                }
            }
        }
    }

    private void writeSimpleDecodeFunction(String param) throws IOException {
        write("public int decode(InputStream is) throws IOException {");
        write("return decode(is, " + param + ");");
        write("}\n");
    }

    private void writeSimpleEncodeFunction() throws IOException {
        write("public int encode(BerByteArrayOutputStream os) throws IOException {");
        write("return encode(os, true);");
        write("}\n");
    }

    private void writeSequenceOfClass(String className, AsnSequenceOf asnSequenceOf, Tag tag, String isStaticStr,
            List<String> listOfSubClassNames) throws IOException {

        write("public" + isStaticStr + " class " + className + " implements Serializable {\n");

        write("private static final long serialVersionUID = 1L;\n");

        AsnElementType componentType = asnSequenceOf.componentType;

        String referencedTypeName = getClassNameOfSequenceOfElement(componentType, listOfSubClassNames);

        if (isInnerType(componentType)) {
            writeConstructedTypeClass(referencedTypeName, componentType.typeReference, null, true, listOfSubClassNames);
        }

        Tag mainTag;
        if (tag == null) {
            if (asnSequenceOf.isSequenceOf) {
                mainTag = stdSeqTag;
            }
            else {
                mainTag = stdSetTag;
            }
        }
        else {
            mainTag = tag;
        }

        write("public static final BerTag tag = new BerTag(" + getBerTagParametersString(mainTag) + ");");

        write("public byte[] code = null;");

        if (jaxbMode) {
            write("private List<" + referencedTypeName + "> seqOf = null;\n");
        }
        else {
            write("public List<" + referencedTypeName + "> seqOf = null;\n");
        }

        write("public " + className + "() {");
        write("seqOf = new ArrayList<" + referencedTypeName + ">();");
        write("}\n");

        write("public " + className + "(byte[] code) {");
        write("this.code = code;");
        write("}\n");

        if (!jaxbMode) {
            write("public " + className + "(List<" + referencedTypeName + "> seqOf) {");
            write("this.seqOf = seqOf;");
            write("}\n");
        }

        if (jaxbMode) {
            writeGetterForSeqOf(referencedTypeName);
        }

        boolean hasExplicitTag = (tag != null) && (tag.type == TagType.EXPLICIT);

        writeSimpleEncodeFunction();

        writeSequenceOfEncodeFunction(componentType, hasExplicitTag, asnSequenceOf.isSequenceOf);

        writeSimpleDecodeFunction("true");

        writeSequenceOrSetOfDecodeFunction(componentType, referencedTypeName, hasExplicitTag,
                asnSequenceOf.isSequenceOf);

        writeEncodeAndSaveFunction();

        writeSequenceOrSetOfToStringFunction(referencedTypeName, componentType);

        write("}\n");

    }

    private void writeRetaggingTypeClass(String typeName, String assignedTypeName, AsnType typeDefinition, Tag tag)
            throws IOException {

        write("public class " + typeName + " extends " + cleanUpName(assignedTypeName) + " {\n");

        write("private static final long serialVersionUID = 1L;\n");

        if (tag != null) {

            write("public static final BerTag tag = new BerTag(" + getBerTagParametersString(tag) + ");\n");

            if (tag.type == TagType.EXPLICIT) {
                write("public byte[] code = null;\n");
            }
        }

        write("public " + typeName + "() {");
        write("}\n");

        String[] constructorParameters = getConstructorParameters(getUniversalType(typeDefinition));

        if (constructorParameters.length != 2 || constructorParameters[0] != "byte[]") {
            write("public " + typeName + "(byte[] code) {");
            write("super(code);");
            write("}\n");
        }

        if ((!jaxbMode || isPrimitiveOrRetaggedPrimitive(typeDefinition)) && (constructorParameters.length != 0)) {
            String constructorParameterString = "";
            String superCallParameterString = "";
            for (int i = 0; i < constructorParameters.length; i += 2) {
                if (i > 0) {
                    constructorParameterString += ", ";
                    superCallParameterString += ", ";
                }
                constructorParameterString += constructorParameters[i] + " " + constructorParameters[i + 1];
                superCallParameterString += constructorParameters[i + 1];
            }

            write("public " + typeName + "(" + constructorParameterString + ") {");
            write("super(" + superCallParameterString + ");");
            write("}\n");

            if (constructorParameters[0].equals("BigInteger")) {
                write("public " + typeName + "(long value) {");
                write("super(value);");
                write("}\n");
            }
            else if (constructorParameters.length == 4 && constructorParameters[3].equals("numBits")) {
                write("public " + typeName + "(boolean[] value) {");
                write("super(value);");
                write("}\n");
            }

        }

        if (tag != null) {

            if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
                writeSimpleEncodeFunction();
            }

            write("public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {\n");

            if (constructorParameters.length != 2 || constructorParameters[0] != "byte[]") {
                write("if (code != null) {");
                write("for (int i = code.length - 1; i >= 0; i--) {");
                write("os.write(code[i]);");
                write("}");
                write("if (withTag) {");
                write("return tag.encode(os) + code.length;");
                write("}");
                write("return code.length;");
                write("}\n");
            }

            write("int codeLength;\n");

            if (tag.type == TagType.EXPLICIT) {
                if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
                    write("codeLength = super.encode(os);");
                }
                else {
                    write("codeLength = super.encode(os, true);");
                }
                write("codeLength += BerLength.encodeLength(os, codeLength);");
            }
            else {
                write("codeLength = super.encode(os, false);");
            }

            write("if (withTag) {");
            write("codeLength += tag.encode(os);");
            write("}\n");

            write("return codeLength;");
            write("}\n");

            if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
                writeSimpleDecodeFunction("true");
            }

            write("public int decode(InputStream is, boolean withTag) throws IOException {\n");

            write("int codeLength = 0;\n");

            write("if (withTag) {");
            write("codeLength += tag.decodeAndCheck(is);");
            write("}\n");

            if (tag.type == TagType.EXPLICIT) {

                write("BerLength length = new BerLength();");
                write("codeLength += length.decode(is);\n");

                if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
                    write("codeLength += super.decode(is, null);\n");
                }
                else {
                    write("codeLength += super.decode(is, true);\n");
                }
            }
            else {
                write("codeLength += super.decode(is, false);\n");
            }

            write("return codeLength;");
            write("}\n");
        }

        write("}");

    }

    private void writeChoiceEncodeFunction(List<AsnElementType> componentTypes, boolean hasExplicitTag)
            throws IOException {
        if (hasExplicitTag) {
            writeSimpleEncodeFunction();
            write("public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {\n");
        }
        else {
            write("public int encode(BerByteArrayOutputStream os) throws IOException {\n");
        }

        write("if (code != null) {");
        write("for (int i = code.length - 1; i >= 0; i--) {");
        write("os.write(code[i]);");
        write("}");
        if (hasExplicitTag) {
            write("if (withTag) {");
            write("return tag.encode(os) + code.length;");
            write("}");
        }
        write("return code.length;");
        write("}\n");

        write("int codeLength = 0;");

        for (int j = componentTypes.size() - 1; j >= 0; j--) {
            if (isExplicit(getTag(componentTypes.get(j)))) {
                write("int sublength;\n");
                break;
            }
        }

        for (int j = componentTypes.size() - 1; j >= 0; j--) {

            AsnElementType componentType = componentTypes.get(j);

            Tag componentTag = getTag(componentType);

            write("if (" + getName(componentType) + " != null) {");

            String explicitEncoding = getExplicitEncodingParameter(componentType);

            if (isExplicit(componentTag)) {
                write("sublength = " + getName(componentType) + ".encode(os" + explicitEncoding + ");");
                write("codeLength += sublength;");
                write("codeLength += BerLength.encodeLength(os, sublength);");
            }
            else {
                write("codeLength += " + getName(componentType) + ".encode(os" + explicitEncoding + ");");
            }

            if (componentTag != null) {
                writeEncodeTag(componentTag);
            }

            if (hasExplicitTag) {
                write("codeLength += BerLength.encodeLength(os, codeLength);");
                write("if (withTag) {");
                write("codeLength += tag.encode(os);");
                write("}");
            }

            write("return codeLength;");
            write("}");

            write("");

        }

        write("throw new IOException(\"Error encoding CHOICE: No element of CHOICE was selected.\");");

        write("}\n");

    }

    private void writeSequenceOrSetEncodeFunction(List<AsnElementType> componentTypes, boolean hasExplicitTag,
            boolean isSequence) throws IOException {
        write("public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {\n");

        write("if (code != null) {");
        write("for (int i = code.length - 1; i >= 0; i--) {");
        write("os.write(code[i]);");
        write("}");
        write("if (withTag) {");
        write("return tag.encode(os) + code.length;");
        write("}");
        write("return code.length;");
        write("}\n");

        write("int codeLength = 0;");

        for (int j = componentTypes.size() - 1; j >= 0; j--) {
            if (isExplicit(getTag(componentTypes.get(j)))) {
                write("int sublength;\n");
                break;
            }
        }

        for (int j = componentTypes.size() - 1; j >= 0; j--) {

            AsnElementType componentType = componentTypes.get(j);

            Tag componentTag = getTag(componentType);

            if (isOptional(componentType)) {
                write("if (" + getName(componentType) + " != null) {");
            }

            String explicitEncoding = getExplicitEncodingParameter(componentType);

            if (isExplicit(componentTag)) {
                write("sublength = " + getName(componentType) + ".encode(os" + explicitEncoding + ");");
                write("codeLength += sublength;");
                write("codeLength += BerLength.encodeLength(os, sublength);");
            }
            else {
                write("codeLength += " + getName(componentType) + ".encode(os" + explicitEncoding + ");");
            }

            if (componentTag != null) {
                writeEncodeTag(componentTag);
            }
            if (isOptional(componentType)) {
                write("}");
            }

            write("");

        }

        if (hasExplicitTag) {
            write("codeLength += BerLength.encodeLength(os, codeLength);");
            if (isSequence) {
                write("os.write(0x30);");
            }
            else {
                write("os.write(0x31);");
            }
            write("codeLength++;\n");
        }

        write("codeLength += BerLength.encodeLength(os, codeLength);\n");

        write("if (withTag) {");
        write("codeLength += tag.encode(os);");
        write("}\n");

        write("return codeLength;\n");

        write("}\n");
    }

    private void writeSequenceOfEncodeFunction(AsnElementType componentType, boolean hasExplicitTag, boolean isSequence)
            throws IOException {
        write("public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {\n");

        write("if (code != null) {");
        write("for (int i = code.length - 1; i >= 0; i--) {");
        write("os.write(code[i]);");
        write("}");
        write("if (withTag) {");
        write("return tag.encode(os) + code.length;");
        write("}");
        write("return code.length;");
        write("}\n");

        write("int codeLength = 0;");

        write("for (int i = (seqOf.size() - 1); i >= 0; i--) {");

        Tag componentTag = getTag(componentType);
        String explicitEncoding = getExplicitEncodingParameter(componentType);

        if (componentTag != null) {

            if (componentTag.type == TagType.EXPLICIT) {
                write("int sublength = seqOf.get(i).encode(os" + explicitEncoding + ");");
                write("codeLength += sublength;");
                write("codeLength += BerLength.encodeLength(os, sublength);");
            }
            else {
                write("codeLength += seqOf.get(i).encode(os" + explicitEncoding + ");");
            }

            if (componentTag != null) {
                writeEncodeTag(componentTag);
            }
        }
        else {

            if (isDirectAnyOrChoice(componentType)) {
                write("codeLength += seqOf.get(i).encode(os);");
            }
            else {
                write("codeLength += seqOf.get(i).encode(os, true);");
            }
        }

        write("}\n");

        if (hasExplicitTag) {
            write("codeLength += BerLength.encodeLength(os, codeLength);");
            if (isSequence) {
                write("os.write(0x30);");
            }
            else {
                write("os.write(0x31);");
            }
            write("codeLength++;\n");
        }

        write("codeLength += BerLength.encodeLength(os, codeLength);\n");

        write("if (withTag) {");
        write("codeLength += tag.encode(os);");
        write("}\n");

        write("return codeLength;");
        write("}\n");

    }

    private String getExplicitEncodingParameter(AsnTaggedType componentType) throws IOException {
        Tag tag = getTag(componentType);

        if (tag != null && tag.type == TagType.IMPLICIT) {
            return ", false";
        }
        else {
            if (isDirectAnyOrChoice(componentType)) {
                return "";
            }
            else {
                return ", true";
            }
        }

    }

    private void writeChoiceDecodeFunction(List<AsnElementType> componentTypes, boolean hasExplicitTag)
            throws IOException {

        if (hasExplicitTag) {
            writeSimpleDecodeFunction("true");

            write("public int decode(InputStream is, boolean withTag) throws IOException {");
            write("int codeLength = 0;");
            write("BerLength length = new BerLength();");
            write("BerTag berTag = new BerTag();\n");

            write("if (withTag) {");
            write("codeLength += tag.decodeAndCheck(is);");
            write("}\n");

            write("codeLength += length.decode(is);");
            write("codeLength += berTag.decode(is);\n");
        }
        else {

            writeSimpleDecodeFunction("null");

            write("public int decode(InputStream is, BerTag berTag) throws IOException {\n");

            write("int codeLength = 0;");
            write("BerTag passedTag = berTag;\n");

            write("if (berTag == null) {");
            write("berTag = new BerTag();");
            write("codeLength += berTag.decode(is);");
            write("}\n");
        }

        // TODO rename "choiceDecodeLength" because it could also be of type ANY
        String initChoiceDecodeLength = "int ";

        for (int j = 0; j < componentTypes.size(); j++) {
            AsnElementType componentType = componentTypes.get(j);

            String explicitEncoding = getExplicitDecodingParameter(componentType);

            Tag componentTag = getTag(componentType);

            if (componentTag != null) {

                write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                if (componentTag.type == TagType.EXPLICIT) {
                    write("codeLength += BerLength.skip(is);");
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                write("codeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                write("return codeLength;");

                write("}\n");

            }
            else {
                if (isDirectAnyOrChoice(componentType)) {
                    write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                    write(initChoiceDecodeLength + "choiceDecodeLength = " + getName(componentType) + ".decode(is"
                            + explicitEncoding + ");");
                    initChoiceDecodeLength = "";
                    write("if (choiceDecodeLength != 0) {");
                    write("return codeLength + choiceDecodeLength;");
                    write("}");
                    write("else {");
                    write(getName(componentType) + " = null;");
                    write("}\n");

                }
                else {

                    write("if (berTag.equals(" + getClassNameOfComponent(componentType) + ".tag)) {");

                    write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                    write("codeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                    write("return codeLength;");

                    write("}\n");
                }
            }

        }

        if (!hasExplicitTag)

        {
            write("if (passedTag != null) {");
            write("return 0;");
            write("}\n");
        }

        write("throw new IOException(\"Error decoding CHOICE: Tag \" + berTag + \" matched to no item.\");");

        write("}\n");

    }

    private void writeSequenceDecodeFunction(List<AsnElementType> componentTypes, boolean hasExplicitTag)
            throws IOException {
        write("public int decode(InputStream is, boolean withTag) throws IOException {");
        write("int codeLength = 0;");
        write("int subCodeLength = 0;");
        write("BerTag berTag = new BerTag();\n");

        write("if (withTag) {");
        write("codeLength += tag.decodeAndCheck(is);");
        write("}\n");
        write("BerLength length = new BerLength();");
        write("codeLength += length.decode(is);\n");
        write("int totalLength = length.val;");

        if (supportIndefiniteLength == true) {
            writeSequenceDecodeIndefiniteLenghtPart(componentTypes);
        }

        write("codeLength += totalLength;\n");

        if (hasExplicitTag) {
            write("int nextByte = is.read();");
            write("if (nextByte == -1) {");
            write("throw new EOFException(\"Unexpected end of input stream.\");");
            write("}");
            write("if (nextByte != (0x30)) {");
            write("throw new IOException(\"Tag does not match!\");");
            write("}");
            write("length.decode(is);");
            write("totalLength = length.val;\n");
        }

        int lastNoneOptionalFieldIndex = -1;
        for (int j = 0; j < componentTypes.size(); j++) {
            AsnElementType componentType = componentTypes.get(componentTypes.size() - 1 - j);
            if (!isOptional(componentType)) {
                lastNoneOptionalFieldIndex = componentTypes.size() - 1 - j;
                break;
            }
        }

        if (lastNoneOptionalFieldIndex == -1) {
            write("if (totalLength == 0) {");
            write("return codeLength;");
            write("}");
        }

        write("subCodeLength += berTag.decode(is);");

        String initChoiceDecodeLength = "int ";

        for (int j = 0; j < componentTypes.size(); j++) {

            AsnElementType componentType = componentTypes.get(j);
            String explicitEncoding = getExplicitDecodingParameter(componentType);
            Tag componentTag = getTag(componentType);

            if (componentTag != null) {
                write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");
                if (isExplicit(componentTag)) {
                    write("subCodeLength += length.decode(is);");
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");
                write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                if (lastNoneOptionalFieldIndex <= j) {
                    write("if (subCodeLength == totalLength) {");
                    write("return codeLength;");
                    write("}");
                }
                if (j != (componentTypes.size() - 1)) {
                    write("subCodeLength += berTag.decode(is);");
                }

                write("}");

                if (j == (componentTypes.size() - 1)) {
                    write("throw new IOException(\"Unexpected end of sequence, length tag: \" + totalLength + \", actual sequence length: \" + subCodeLength);\n");
                }
                else if (!isOptional(componentType)) {
                    write("else {");
                    write("throw new IOException(\"Tag does not match the mandatory sequence element tag.\");");
                    write("}");
                }

            }
            else {

                if (isDirectAnyOrChoice(componentType)) {

                    write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                    if (isOptional(componentType)) {
                        write(initChoiceDecodeLength + "choiceDecodeLength = " + getName(componentType) + ".decode(is"
                                + explicitEncoding + ");");

                        initChoiceDecodeLength = "";

                        if (j != (componentTypes.size() - 1)) {

                            write("if (choiceDecodeLength != 0) {");
                            write("subCodeLength += choiceDecodeLength;");

                            if (lastNoneOptionalFieldIndex <= j) {
                                write("if (subCodeLength == totalLength) {");
                                write("return codeLength;");
                                write("}");
                            }
                            write("subCodeLength += berTag.decode(is);");
                            write("}");
                            write("else {");
                            write(getName(componentType) + " = null;");
                            write("}");

                        }
                        else {
                            // if last sequence element
                            write("subCodeLength += choiceDecodeLength;");
                            write("if (subCodeLength == totalLength) {");
                            write("return codeLength;");
                            write("}");
                        }
                    }
                    else {
                        write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");
                        if (j != (componentTypes.size() - 1)) {
                            if (lastNoneOptionalFieldIndex <= j) {
                                write("if (subCodeLength == totalLength) {");
                                write("return codeLength;");
                                write("}");
                            }
                            write("subCodeLength += berTag.decode(is);");
                        }
                        else {
                            write("if (subCodeLength == totalLength) {");
                            write("return codeLength;");
                            write("}");
                        }
                    }

                    if (j == (componentTypes.size() - 1)) {
                        write("throw new IOException(\"Unexpected end of sequence, length tag: \" + totalLength + \", actual sequence length: \" + subCodeLength);\n");
                    }

                }
                else {

                    write("if (berTag.equals(" + getClassNameOfComponent(componentType) + ".tag)) {");

                    write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                    write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                    if (lastNoneOptionalFieldIndex <= j) {
                        write("if (subCodeLength == totalLength) {");
                        write("return codeLength;");
                        write("}");
                    }
                    if (j != (componentTypes.size() - 1)) {
                        write("subCodeLength += berTag.decode(is);");
                    }

                    write("}");

                    if (j == (componentTypes.size() - 1)) {
                        write("throw new IOException(\"Unexpected end of sequence, length tag: \" + totalLength + \", actual sequence length: \" + subCodeLength);\n");
                    }
                    else if (!isOptional(componentType)) {
                        write("else {");
                        write("throw new IOException(\"Tag does not match the mandatory sequence element tag.\");");
                        write("}");
                    }

                }
            }

            write("");

        }

        if (componentTypes.isEmpty()) {
            write("return subCodeLength;");
        }
        write("}\n");

    }

    private void writeSequenceOrSetOfDecodeFunction(AsnElementType componentType, String classNameOfSequenceOfElement,
            boolean hasExplicitTag, boolean isSequence) throws IOException {

        Tag componentTag = getTag(componentType);

        write("public int decode(InputStream is, boolean withTag) throws IOException {");
        write("int codeLength = 0;");
        write("int subCodeLength = 0;");
        if (componentTag != null || supportIndefiniteLength) {
            write("BerTag berTag = new BerTag();");
        }

        write("if (withTag) {");
        write("codeLength += tag.decodeAndCheck(is);");
        write("}\n");

        write("BerLength length = new BerLength();");
        write("codeLength += length.decode(is);");
        write("int totalLength = length.val;\n");

        if (supportIndefiniteLength) {
            writeSequenceOfDecodeIndefiniteLenghtPart(componentType, classNameOfSequenceOfElement, hasExplicitTag);
        }

        if (hasExplicitTag) {
            write("int nextByte = is.read();");
            write("if (nextByte == -1) {");
            write("throw new EOFException(\"Unexpected end of input stream.\");");
            write("}");
            if (isSequence) {
                write("if (nextByte != (0x30)) {");
            }
            else {
                write("if (nextByte != (0x31)) {");
            }
            write("throw new IOException(\"Tag does not match!\");");
            write("}");
            write("length.decode(is);");
            write("totalLength = length.val;\n");
        }

        write("while (subCodeLength < totalLength) {");
        write(classNameOfSequenceOfElement + " element = new " + classNameOfSequenceOfElement + "();");

        String explicitEncoding = getExplicitDecodingParameter(componentType);

        if (componentTag != null) {

            if (componentTag.type == TagType.EXPLICIT) {
                write("subCodeLength += berTag.decode(is);");
                write("subCodeLength += length.decode(is);");
                explicitEncoding = ", true";
            }
            else {
                write("subCodeLength += berTag.decode(is);");
                if (isDirectAnyOrChoice(componentType)) {
                    explicitEncoding = ", berTag";
                }
                else {
                    explicitEncoding = ", false";
                }
            }
            write("subCodeLength += element.decode(is" + explicitEncoding + ");");
        }
        else {

            if (isDirectAnyOrChoice(componentType)) {
                write("subCodeLength += element.decode(is, null);");
            }
            else {
                write("subCodeLength += element.decode(is, true);");
            }
        }
        write("seqOf.add(element);");
        write("}");
        write("if (subCodeLength != totalLength) {");
        write("throw new IOException(\"Decoded SequenceOf or SetOf has wrong length. Expected \" + totalLength + \" but has \" + subCodeLength);\n");
        write("}");
        write("codeLength += subCodeLength;\n");

        write("return codeLength;");
        write("}\n");

    }

    private static String getBerTagParametersString(Tag tag) {
        return "BerTag." + tag.tagClass + "_CLASS, BerTag." + tag.typeStructure.toString() + ", " + tag.value;
    }

    private void writeToStringFunction() throws IOException {
        write("public String toString() {");
        write("StringBuilder sb = new StringBuilder();");
        write("appendAsString(sb, 0);");
        write("return sb.toString();");
        write("}\n");
    }

    private void writeChoiceToStringFunction(List<AsnElementType> componentTypes) throws IOException {
        writeToStringFunction();

        write("public void appendAsString(StringBuilder sb, int indentLevel) {\n");

        for (int j = 0; j < componentTypes.size(); j++) {
            AsnElementType componentType = componentTypes.get(j);

            write("if (" + getName(componentType) + " != null) {");

            if (!isPrimitive(getUniversalType(componentType))) {
                write("sb.append(\"" + getName(componentType) + ": \");");
                write(getName(componentType) + ".appendAsString(sb, indentLevel + 1);");
            }
            else {
                write("sb.append(\"" + getName(componentType) + ": \").append(" + getName(componentType) + ");");
            }
            write("return;");
            write("}\n");
        }

        write("sb.append(\"<none>\");");

        write("}\n");

    }

    private void writeSequenceOrSetToStringFunction(List<AsnElementType> componentTypes) throws IOException {

        writeToStringFunction();

        write("public void appendAsString(StringBuilder sb, int indentLevel) {\n");

        write("sb.append(\"{\");");

        boolean checkIfFirstSelectedElement = componentTypes.size() > 1;

        int j = 0;

        for (AsnElementType componentType : componentTypes) {

            if (isOptional(componentType)) {
                if (j == 0 && componentTypes.size() > 1) {
                    write("boolean firstSelectedElement = true;");
                }
                write("if (" + getName(componentType) + " != null) {");
            }

            if (j != 0) {
                if (checkIfFirstSelectedElement) {

                    write("if (!firstSelectedElement) {");

                }
                write("sb.append(\",\\n\");");
                if (checkIfFirstSelectedElement) {
                    write("}");
                }
            }
            else {
                write("sb.append(\"\\n\");");
            }

            write("for (int i = 0; i < indentLevel + 1; i++) {");
            write("sb.append(\"\\t\");");
            write("}");
            if (!isOptional(componentType)) {
                write("if (" + getName(componentType) + " != null) {");
            }
            if (!isPrimitive(getUniversalType(componentType))) {
                write("sb.append(\"" + getName(componentType) + ": \");");
                write(getName(componentType) + ".appendAsString(sb, indentLevel + 1);");
            }
            else {
                write("sb.append(\"" + getName(componentType) + ": \").append(" + getName(componentType) + ");");
            }
            if (!isOptional(componentType)) {
                write("}");
                write("else {");
                write("sb.append(\"" + getName(componentType) + ": <empty-required-field>\");");
                write("}");
            }

            if (isOptional(componentType)) {
                if (checkIfFirstSelectedElement) {
                    write("firstSelectedElement = false;");
                }
                write("}");
            }
            else {
                checkIfFirstSelectedElement = false;
            }

            write("");

            j++;

        }

        write("sb.append(\"\\n\");");
        write("for (int i = 0; i < indentLevel; i++) {");
        write("sb.append(\"\\t\");");
        write("}");
        write("sb.append(\"}\");");

        write("}\n");

    }

    private void writeSequenceOrSetOfToStringFunction(String referencedTypeName, AsnElementType componentType)
            throws IOException {

        writeToStringFunction();

        write("public void appendAsString(StringBuilder sb, int indentLevel) {\n");

        write("sb.append(\"{\\n\");");
        write("for (int i = 0; i < indentLevel + 1; i++) {");
        write("sb.append(\"\\t\");");
        write("}");

        write("if (seqOf == null) {");
        write("sb.append(\"null\");");
        write("}");
        write("else {");
        write("Iterator<" + referencedTypeName + "> it = seqOf.iterator();");
        write("if (it.hasNext()) {");

        if (!isPrimitive(getUniversalType(componentType))) {
            write("it.next().appendAsString(sb, indentLevel + 1);");
        }
        else {
            write("sb.append(it.next());");
        }

        write("while (it.hasNext()) {");
        write("sb.append(\",\\n\");");
        write("for (int i = 0; i < indentLevel + 1; i++) {");
        write("sb.append(\"\\t\");");
        write("}");

        if (!isPrimitive(getUniversalType(componentType))) {
            write("it.next().appendAsString(sb, indentLevel + 1);");
        }
        else {
            write("sb.append(it.next());");
        }

        write("}");
        write("}");
        write("}\n");

        write("sb.append(\"\\n\");");
        write("for (int i = 0; i < indentLevel; i++) {");
        write("sb.append(\"\\t\");");
        write("}");
        write("sb.append(\"}\");");

        write("}\n");

    }

    private String getExplicitDecodingParameter(AsnElementType componentType) throws IOException {

        Tag tag = getTag(componentType);

        String explicitEncoding;

        if (tag != null && tag.type == TagType.EXPLICIT) {
            if (isDirectAnyOrChoice(componentType)) {
                explicitEncoding = ", null";
            }
            else {
                explicitEncoding = ", true";
            }
        }
        else {
            if (isDirectAnyOrChoice(componentType)) {
                explicitEncoding = ", berTag";
            }
            else {
                explicitEncoding = ", false";
            }
        }
        return explicitEncoding;
    }

    private void writeSequenceOfDecodeIndefiniteLenghtPart(AsnElementType componentType,
            String classNameOfSequenceOfElement, boolean isTagExplicit) throws IOException {
        write("if (length.val == -1) {");
        write("while (true) {");
        write("subCodeLength += berTag.decode(is);\n");

        write("if (berTag.tagNumber == 0 && berTag.tagClass == 0 && berTag.primitive == 0) {");
        write("int nextByte = is.read();");
        write("if (nextByte != 0) {");
        write("if (nextByte == -1) {");
        write("throw new EOFException(\"Unexpected end of input stream.\");");
        write("}");
        write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
        write("}");
        write("codeLength += subCodeLength + 1;");
        write("return codeLength;");
        write("}\n");

        write(classNameOfSequenceOfElement + " element = new " + classNameOfSequenceOfElement + "();");

        if (isDirectAnyOrChoice(componentType)) {
            write("subCodeLength += element.decode(is, berTag);");
        }
        else {
            write("subCodeLength += element.decode(is, false);");
        }
        write("seqOf.add(element);");
        write("}");

        write("}");
    }

    private void addAutomaticTagsIfNeeded(List<AsnElementType> componentTypes) throws IOException {
        if (tagDefault != TagDefault.AUTOMATIC) {
            return;
        }
        for (AsnElementType element : componentTypes) {
            if (getTag(element) != null) {
                return;
            }
        }
        int i = 0;
        for (AsnElementType element : componentTypes) {
            element.tag = new AsnTag();
            element.tag.classNumber = new AsnClassNumber();
            element.tag.classNumber.num = i;
            i++;
        }

    }

    private void writeEncodeAndSaveFunction() throws IOException {
        writeEncodeAndSaveFunction(false);
    }

    private void writeEncodeAndSaveFunction(boolean isTagless) throws IOException {
        write("public void encodeAndSave(int encodingSizeGuess) throws IOException {");
        write("BerByteArrayOutputStream os = new BerByteArrayOutputStream(encodingSizeGuess);");
        if (isTagless) {
            write("encode(os);");
        }
        else {
            write("encode(os, false);");
        }
        write("code = os.getArray();");
        write("}\n");
    }

    private void writeSetDecodeFunction(List<AsnElementType> componentTypes) throws IOException {
        write("public int decode(InputStream is, boolean withTag) throws IOException {");
        write("int codeLength = 0;");
        write("int subCodeLength = 0;");
        write("BerTag berTag = new BerTag();\n");

        write("if (withTag) {");
        write("codeLength += tag.decodeAndCheck(is);");
        write("}\n");
        write("BerLength length = new BerLength();");
        write("codeLength += length.decode(is);\n");
        write("int totalLength = length.val;");

        if (supportIndefiniteLength == true) {
            writeSetDecodeIndefiniteLenghtPart(componentTypes);
        }

        write("while (subCodeLength < totalLength) {");
        write("subCodeLength += berTag.decode(is);");

        for (int j = 0; j < componentTypes.size(); j++) {
            AsnElementType componentType = componentTypes.get(j);

            Tag componentTag = getTag(componentType);

            String explicitEncoding = ", false";

            String elseString = "";

            if (j != 0) {
                elseString = "else ";
            }

            if (isDirectAnyOrChoice(componentType)) {

                if (!isExplicit(componentTag)) {
                    throw new IOException("choice or ANY within set has no explicit tag.");
                }
                write(elseString + "if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                write("subCodeLength += new BerLength().decode(is);");
                explicitEncoding = ", null";

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                write("}");

            }
            else {

                if (componentTag != null) {
                    if (isExplicit(componentTag)) {
                        write(elseString + "if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");
                    }
                    else {
                        write(elseString + "if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");
                    }
                    if (isExplicit(componentTag)) {
                        write("subCodeLength += new BerLength().decode(is);");
                        explicitEncoding = ", true";
                    }
                }
                else {
                    write(elseString + "if (berTag.equals(" + getClassNameOfComponent(componentType) + ".tag)) {");
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                if (", null".equals(explicitEncoding)) {
                    write("BerLength length2 = new BerLength();");
                    write("subCodeLength += length2.decode(is);");
                }

                write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");

                write("}");

            }

        }

        write("}");

        write("if (subCodeLength != totalLength) {");
        write("throw new IOException(\"Length of set does not match length tag, length tag: \" + totalLength + \", actual set length: \" + subCodeLength);\n");
        write("}");
        write("codeLength += subCodeLength;\n");

        write("return codeLength;");
        write("}\n");
    }

    private void writeSequenceDecodeIndefiniteLenghtPart(List<AsnElementType> componentTypes) throws IOException {
        write("if (totalLength == -1) {");
        write("subCodeLength += berTag.decode(is);\n");

        String initChoiceDecodeLength = "int ";

        for (AsnElementType componentType : componentTypes) {

            Tag componentTag = getTag(componentType);

            write("if (berTag.tagNumber == 0 && berTag.tagClass == 0 && berTag.primitive == 0) {");
            write("int nextByte = is.read();");
            write("if (nextByte != 0) {");
            write("if (nextByte == -1) {");
            write("throw new EOFException(\"Unexpected end of input stream.\");");
            write("}");
            write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
            write("}");
            write("codeLength += subCodeLength + 1;");
            write("return codeLength;");
            write("}");

            String explicitEncoding;

            if (isDirectAnyOrChoice(componentType)) {
                if (isExplicit(componentTag)) {
                    write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                    write("subCodeLength += length.decode(is);");
                    explicitEncoding = "null";
                }
                else {
                    explicitEncoding = "berTag";
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                write(initChoiceDecodeLength + "choiceDecodeLength = " + getName(componentType) + ".decode(is, "
                        + explicitEncoding + ");");
                if (!isExplicit(componentTag)) {
                    initChoiceDecodeLength = "";
                }
                write("if (choiceDecodeLength != 0) {");
                write("subCodeLength += choiceDecodeLength;");

                write("subCodeLength += berTag.decode(is);");
                write("}");
                write("else {");
                write(getName(componentType) + " = null;");
                write("}\n");

                if (isExplicit(componentTag)) {
                    write("}");
                }

            }
            else {

                explicitEncoding = ", false";

                if (componentTag != null) {
                    write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                    if (isExplicit(componentTag)) {
                        write("codeLength += length.decode(is);");
                        explicitEncoding = ", true";
                    }
                }
                else {
                    write("if (berTag.equals(" + getClassNameOfComponent(componentType) + ".tag)) {");
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");
                write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");
                write("subCodeLength += berTag.decode(is);");
                write("}");

            }

        }

        write("int nextByte = is.read();");
        write("if (berTag.tagNumber != 0 || berTag.tagClass != 0 || berTag.primitive != 0");
        write("|| nextByte != 0) {");
        write("if (nextByte == -1) {");
        write("throw new EOFException(\"Unexpected end of input stream.\");");
        write("}");
        write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
        write("}");
        write("codeLength += subCodeLength + 1;");

        write("return codeLength;");
        write("}\n");
    }

    private void writeSetDecodeIndefiniteLenghtPart(List<AsnElementType> componentTypes) throws IOException {
        write("if (totalLength == -1) {");
        write("subCodeLength += berTag.decode(is);\n");

        String initChoiceDecodeLength = "int ";

        for (AsnElementType componentType : componentTypes) {

            Tag componentTag = getTag(componentType);

            write("if (berTag.tagNumber == 0 && berTag.tagClass == 0 && berTag.primitive == 0) {");
            write("int nextByte = is.read();");
            write("if (nextByte != 0) {");
            write("if (nextByte == -1) {");
            write("throw new EOFException(\"Unexpected end of input stream.\");");
            write("}");
            write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
            write("}");
            write("codeLength += subCodeLength + 1;");
            write("return codeLength;");
            write("}");

            String explicitEncoding;

            if (isDirectAnyOrChoice(componentType)) {
                if (isExplicit(componentTag)) {
                    write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                    write("subCodeLength += length.decode(is);");
                    explicitEncoding = "null";
                }
                else {
                    explicitEncoding = "berTag";
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");

                write(initChoiceDecodeLength + "choiceDecodeLength = " + getName(componentType) + ".decode(is, "
                        + explicitEncoding + ");");
                if (!isExplicit(componentTag)) {
                    initChoiceDecodeLength = "";
                }
                write("if (choiceDecodeLength != 0) {");
                write("subCodeLength += choiceDecodeLength;");

                write("subCodeLength += berTag.decode(is);");
                write("}");
                write("else {");
                write(getName(componentType) + " = null;");
                write("}\n");

                if (isExplicit(componentTag)) {
                    write("}");
                }

            }
            else {

                explicitEncoding = ", false";

                if (componentTag != null) {
                    write("if (berTag.equals(" + getBerTagParametersString(componentTag) + ")) {");

                    if (isExplicit(componentTag)) {
                        write("codeLength += length.decode(is);");
                        explicitEncoding = ", true";
                    }
                }
                else {
                    write("if (berTag.equals(" + getClassNameOfComponent(componentType) + ".tag)) {");
                }

                write(getName(componentType) + " = new " + getClassNameOfComponent(componentType) + "();");
                write("subCodeLength += " + getName(componentType) + ".decode(is" + explicitEncoding + ");");
                write("subCodeLength += berTag.decode(is);");
                write("}");

            }

        }

        write("int nextByte = is.read();");
        write("if (berTag.tagNumber != 0 || berTag.tagClass != 0 || berTag.primitive != 0");
        write("|| nextByte != 0) {");
        write("if (nextByte == -1) {");
        write("throw new EOFException(\"Unexpected end of input stream.\");");
        write("}");
        write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
        write("}");
        write("codeLength += subCodeLength + 1;");

        write("return codeLength;");
        write("}\n");
    }

    private void writeEncodeTag(Tag tag) throws IOException {
        int typeStructure;

        if (tag.typeStructure == TypeStructure.CONSTRUCTED) {
            typeStructure = BerTag.CONSTRUCTED;
        }
        else {
            typeStructure = BerTag.PRIMITIVE;
        }

        BerTag berTag = new BerTag(getTagClassId(tag.tagClass.toString()), typeStructure, tag.value);

        write("// write tag: " + tag.tagClass + "_CLASS, " + tag.typeStructure + ", " + tag.value);
        for (int i = (berTag.tagBytes.length - 1); i >= 0; i--) {
            write("os.write(" + HexConverter.toHexString(berTag.tagBytes[i]) + ");");
        }

        write("codeLength += " + berTag.tagBytes.length + ";");

    }

    private int getTagClassId(String tagClass) {

        if (tagClass.equals("UNIVERSAL")) {
            return BerTag.UNIVERSAL_CLASS;
        }
        else if (tagClass.equals("APPLICATION")) {
            return BerTag.APPLICATION_CLASS;
        }
        else if (tagClass.equals("CONTEXT")) {
            return BerTag.CONTEXT_CLASS;
        }
        else if (tagClass.equals("PRIVATE")) {
            return BerTag.PRIVATE_CLASS;
        }
        else {
            throw new IllegalStateException("unknown tag class: " + tagClass);
        }

    }

    private String getName(AsnElementType componentType) {
        return cleanUpName(componentType.name);
    }

    private boolean isOptional(AsnElementType componentType) {
        return (componentType.isOptional || componentType.isDefault);
    }

    private boolean isExplicit(Tag tag) {
        return (tag != null) && (tag.type == TagType.EXPLICIT);
    }

    private void writeEncodeConstructor(String className, List<AsnElementType> componentTypes) throws IOException {

        if (componentTypes.isEmpty()) {
            return;
        }

        String line = "public " + className + "(";

        int j = 0;

        for (AsnElementType componentType : componentTypes) {

            if (j != 0) {
                line += ", ";
            }
            j++;
            line += (getClassNameOfComponent(componentType) + " " + cleanUpName(componentType.name));

        }

        write(line + ") {");

        for (AsnElementType componentType : componentTypes) {

            String elementName = cleanUpName(componentType.name);

            write("this." + elementName + " = " + elementName + ";");

        }

        write("}\n");
    }

    private void writeEmptyConstructor(String className) throws IOException {
        write("public " + className + "() {");
        write("}\n");

        write("public " + className + "(byte[] code) {");
        write("this.code = code;");
        write("}\n");
    }

    private void writePublicMembers(List<AsnElementType> componentTypes) throws IOException {
        for (AsnElementType element : componentTypes) {
            if (jaxbMode) {
                write("private " + element.className + " " + cleanUpName(element.name) + " = null;");
            }
            else {
                write("public " + element.className + " " + cleanUpName(element.name) + " = null;");
            }
        }
        write("");
    }

    private boolean isInnerType(AsnElementType element) {
        return element.typeReference != null && (element.typeReference instanceof AsnConstructedType);
    }

    private void writeGetterAndSetter(List<AsnElementType> componentTypes) throws IOException {
        for (AsnElementType element : componentTypes) {
            String typeName = getClassNameOfComponent(element);
            String getterName = cleanUpName("get" + capitalizeFirstCharacter(element.name));
            String setterName = cleanUpName("set" + capitalizeFirstCharacter(element.name));
            String variableName = cleanUpName(element.name);
            write("public void " + setterName + "(" + typeName + " " + variableName + ") {");
            write("this." + variableName + " = " + variableName + ";");
            write("}\n");
            write("public " + typeName + " " + getterName + "() {");
            write("return " + variableName + ";");
            write("}\n");
        }
    }

    private void writeGetterForSeqOf(String referencedTypeName) throws IOException {
        write("public List<" + referencedTypeName + "> get"
                + referencedTypeName.substring(referencedTypeName.lastIndexOf('.') + 1) + "() {");
        write("if (seqOf == null) {");
        write("seqOf = new ArrayList<" + referencedTypeName + ">();");
        write("}");
        write("return seqOf;");
        write("}\n");
    }

    private String getClassNameOfSequenceOfElement(AsnElementType componentType, List<String> listOfSubClassNames)
            throws IOException {
        String classNameOfSequenceElement = getClassNameOfSequenceOfElement(componentType);
        for (String subClassName : listOfSubClassNames) {
            if (classNameOfSequenceElement.equals(subClassName)) {
                String moduleName = module.moduleIdentifier.name;

                for (SymbolsFromModule syms : this.module.importSymbolFromModuleList) {
                    if (syms.symbolList.contains(classNameOfSequenceElement)) {
                        moduleName = syms.modref;
                        break;
                    }
                }

                return basePackageName + sanitizeModuleName(moduleName).replace('-', '.').toLowerCase() + "."
                        + classNameOfSequenceElement;
            }
        }
        return classNameOfSequenceElement;

    }

    private String getClassNameOfSequenceOfElement(AsnElementType componentType) throws IOException {
        if (componentType.typeReference == null) {
            return cleanUpName(componentType.definedType.typeName);
        }
        else {
            AsnType typeDefinition = componentType.typeReference;
            return getClassNameOfSequenceOfTypeReference(typeDefinition);
        }
    }

    private String getClassNameOfSequenceOfTypeReference(AsnType typeDefinition) {
        if (typeDefinition instanceof AsnConstructedType) {

            String subClassName;

            if (typeDefinition instanceof AsnSequenceSet) {

                if (((AsnSequenceSet) typeDefinition).isSequence) {
                    subClassName = "SEQUENCE";
                }
                else {
                    subClassName = "SET";
                }

            }
            else if (typeDefinition instanceof AsnSequenceOf) {
                if (((AsnSequenceOf) typeDefinition).isSequenceOf) {
                    subClassName = "SEQUENCEOF";
                }
                else {
                    subClassName = "SETOF";
                }

            }
            else {
                subClassName = "CHOICE";
            }

            return subClassName;

        }
        return getBerType(typeDefinition);
    }

    private String capitalizeFirstCharacter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String getBerType(AsnType asnType) {

        String fullClassName = asnType.getClass().getName();

        String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        if (className.equals("AsnCharacterString")) {
            AsnCharacterString asnCharacterString = (AsnCharacterString) asnType;
            if (asnCharacterString.stringtype.equals("ISO646String")) {
                return "BerVisibleString";
            }
            else if (asnCharacterString.stringtype.equals("T61String")) {
                return "BerTeletexString";
            }
            return "Ber" + ((AsnCharacterString) asnType).stringtype;
        }
        return "Ber" + className.substring(3);
    }

    private String[] getConstructorParameters(AsnUniversalType typeDefinition) throws IOException {

        if (typeDefinition instanceof AsnInteger || typeDefinition instanceof AsnEnum) {
            return new String[] { "BigInteger", "value" };
        }
        else if (typeDefinition instanceof AsnReal) {
            return new String[] { "double", "value" };
        }
        else if (typeDefinition instanceof AsnBoolean) {
            return new String[] { "boolean", "value" };
        }
        else if (typeDefinition instanceof AsnObjectIdentifier) {
            return new String[] { "int[]", "value" };
        }
        else if (typeDefinition instanceof AsnBitString) {
            return new String[] { "byte[]", "value", "int", "numBits" };
        }
        else if (typeDefinition instanceof AsnOctetString || typeDefinition instanceof AsnCharacterString) {
            return new String[] { "byte[]", "value" };
        }
        else if (typeDefinition instanceof AsnNull) {
            return new String[0];
        }
        else if ((typeDefinition instanceof AsnSequenceSet) || (typeDefinition instanceof AsnChoice)) {
            return getConstructorParametersFromConstructedElement((AsnConstructedType) typeDefinition);
        }
        else if (typeDefinition instanceof AsnSequenceOf) {
            return new String[] {
                    "List<" + getClassNameOfSequenceOfElement(((AsnSequenceOf) typeDefinition).componentType) + ">",
                    "seqOf" };
        }
        else if (typeDefinition instanceof AsnAny) {
            return new String[] { "byte[]", "value" };
        }
        else if (typeDefinition instanceof AsnEmbeddedPdv) {
            return new String[0];
        }
        else {
            throw new IllegalStateException("type of unknown class: " + typeDefinition.name);
        }

    }

    private String[] getConstructorParametersFromConstructedElement(AsnConstructedType assignedTypeDefinition)
            throws IOException {

        List<AsnElementType> componentTypes;

        if (assignedTypeDefinition instanceof AsnSequenceSet) {

            componentTypes = ((AsnSequenceSet) assignedTypeDefinition).componentTypes;
        }
        else {
            componentTypes = ((AsnChoice) assignedTypeDefinition).componentTypes;
        }

        String[] constructorParameters = new String[componentTypes.size() * 2];

        for (int j = 0; j < componentTypes.size(); j++) {
            AsnElementType componentType = componentTypes.get(j);

            constructorParameters[j * 2] = getClassNameOfComponent(componentType);
            constructorParameters[j * 2 + 1] = cleanUpName(componentType.name);

        }
        return constructorParameters;
    }

    private AsnType followAndGetNextTaggedOrUniversalType(AsnType asnType, AsnModule module) throws CompileException {
        return followAndGetNextTaggedOrUniversalType(asnType, module, true);
    }

    private AsnType followAndGetNextTaggedOrUniversalType(AsnType asnType, AsnModule module, boolean firstCall)
            throws CompileException {
        if (asnType instanceof AsnTaggedType) {
            if (!firstCall) {
                return asnType;
            }
            AsnTaggedType taggedType = (AsnTaggedType) asnType;
            if (taggedType.definedType != null) {
                return followAndGetNextTaggedOrUniversalType(taggedType.definedType, module, false);
            }
            else {
                return taggedType.typeReference;
            }
        }
        else if (asnType instanceof AsnDefinedType) {

            AsnDefinedType definedType = (AsnDefinedType) asnType;

            if (definedType.isObjectClassField) {

                AsnInformationObjectClass informationObjectClass = getInformationObjectClass(
                        definedType.moduleOrObjectClassReference, module);
                if (informationObjectClass == null) {
                    throw new CompileException("no information object class of name \""
                            + definedType.moduleOrObjectClassReference + "\" found");
                }

                for (AsnElementType elementType : informationObjectClass.elementList) {
                    if (elementType.name.equals(definedType.typeName)) {
                        return followAndGetNextTaggedOrUniversalType(elementType, module, true);
                    }
                }

                throw new IllegalStateException("Could not find field \"" + definedType.typeName
                        + "\" of information object class \"" + definedType.moduleOrObjectClassReference + "\"");
            }
            else {
                return followAndGetNextTaggedOrUniversalType(definedType.typeName, module, false);
            }
        }
        else if (asnType instanceof AsnUniversalType) {
            return asnType;
        }
        else {
            throw new IllegalStateException();
        }

    }

    private AsnType followAndGetNextTaggedOrUniversalType(String typeName, AsnModule module, boolean firstCall)
            throws CompileException {

        AsnType asnType = module.typesByName.get(typeName);
        if (asnType != null) {
            return followAndGetNextTaggedOrUniversalType(asnType, module, false);
        }
        for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
            for (String importedTypeName : symbolsFromModule.symbolList) {
                if (typeName.equals(importedTypeName)) {
                    return followAndGetNextTaggedOrUniversalType(typeName, getAsnModule(symbolsFromModule.modref),
                            false);
                }
            }
        }
        throw new IllegalStateException("Type definition \"" + typeName + "\" was not found in module \""
                + module.moduleIdentifier.name + "\"");

    }

    private AsnModule getAsnModule(String moduleName) {
        AsnModule asnModule = modulesByName.get(moduleName);
        if (asnModule == null) {
            throw new CompileException("Definition of imported module \"" + moduleName + "\" not found.");
        }
        return asnModule;
    }

    private boolean isDirectAnyOrChoice(AsnTaggedType taggedType) throws CompileException {

        AsnType followedType = followAndGetNextTaggedOrUniversalType(taggedType, module);
        return (followedType instanceof AsnAny) || (followedType instanceof AsnChoice);

    }

    private AsnUniversalType getUniversalType(AsnType asnType) throws IOException {
        return getUniversalType(asnType, module);
    }

    private AsnUniversalType getUniversalType(AsnType asnType, AsnModule module) throws IOException {
        while ((asnType = followAndGetNextTaggedOrUniversalType(asnType, module)) instanceof AsnTaggedType) {
        }
        return (AsnUniversalType) asnType;
    }

    private boolean isPrimitive(AsnTaggedType asnTaggedType) throws IOException {
        AsnType asnType = asnTaggedType;
        while ((asnType = followAndGetNextTaggedOrUniversalType(asnType, module)) instanceof AsnTaggedType) {
            if (isExplicit(getTag((AsnTaggedType) asnType))) {
                return false;
            }
        }
        return isPrimitive((AsnUniversalType) asnType);
    }

    private boolean isPrimitiveOrRetaggedPrimitive(AsnType asnType) throws IOException {
        return isPrimitive(getUniversalType(asnType));
    }

    private boolean isPrimitive(AsnUniversalType asnType) {
        return !(asnType instanceof AsnConstructedType || asnType instanceof AsnEmbeddedPdv);
    }

    private void writeClassHeader(String typeName, AsnModule module) throws IOException {

        outputDirectory.mkdirs();

        FileWriter fstream = new FileWriter(new File(outputDirectory, typeName + ".java"));
        out = new BufferedWriter(fstream);

        write("/**");
        write(" * This class file was automatically generated by jASN1 v" + Compiler.VERSION
                + " (http://www.openmuc.org)\n */\n");
        write("package " + basePackageName
                + sanitizeModuleName(module.moduleIdentifier.name).replace('-', '.').toLowerCase() + ";\n");

        write("import java.io.IOException;");
        write("import java.io.EOFException;");
        write("import java.io.InputStream;");
        write("import java.util.List;");
        write("import java.util.ArrayList;");
        write("import java.util.Iterator;");
        write("import java.io.UnsupportedEncodingException;");
        write("import java.math.BigInteger;");
        write("import java.io.Serializable;");

        write("import org.openmuc.jasn1.ber.*;");
        write("import org.openmuc.jasn1.ber.types.*;");
        write("import org.openmuc.jasn1.ber.types.string.*;\n");

        List<String> importedClassesFromOtherModules = new ArrayList<>();

        for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
            AsnModule importedModule = modulesByName.get(symbolsFromModule.modref);
            for (String importedSymbol : symbolsFromModule.symbolList) {
                if (Character.isUpperCase(importedSymbol.charAt(0))) {
                    if (importedModule.typesByName.get(importedSymbol) != null) {
                        importedClassesFromOtherModules.add(
                                sanitizeModuleName(importedModule.moduleIdentifier.name).replace('-', '.').toLowerCase()
                                        + "." + cleanUpName(importedSymbol) + ";");
                    }
                }
            }
        }
        Collections.sort(importedClassesFromOtherModules);
        for (String modulePackage : importedClassesFromOtherModules) {
            write("import " + basePackageName + modulePackage);
        }
        write("");

    }

    private void write(String line) throws IOException {
        if (line.startsWith("}")) {
            indentNum--;
        }
        for (int i = 0; i < indentNum; i++) {
            out.write("\t");
        }
        out.write(line + "\n");

        if (line.endsWith(" {") || line.endsWith(" {\n") || line.endsWith(" {\n\n")) {
            indentNum++;
        }
    }

}
