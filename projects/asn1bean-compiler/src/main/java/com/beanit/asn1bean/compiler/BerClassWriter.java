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

import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.types.BerObjectIdentifier;
import com.beanit.asn1bean.compiler.model.AsnAny;
import com.beanit.asn1bean.compiler.model.AsnBitString;
import com.beanit.asn1bean.compiler.model.AsnBoolean;
import com.beanit.asn1bean.compiler.model.AsnCharacterString;
import com.beanit.asn1bean.compiler.model.AsnChoice;
import com.beanit.asn1bean.compiler.model.AsnClassNumber;
import com.beanit.asn1bean.compiler.model.AsnConstructedType;
import com.beanit.asn1bean.compiler.model.AsnDefinedType;
import com.beanit.asn1bean.compiler.model.AsnElementType;
import com.beanit.asn1bean.compiler.model.AsnEmbeddedPdv;
import com.beanit.asn1bean.compiler.model.AsnEnum;
import com.beanit.asn1bean.compiler.model.AsnInformationObjectClass;
import com.beanit.asn1bean.compiler.model.AsnInteger;
import com.beanit.asn1bean.compiler.model.AsnModule;
import com.beanit.asn1bean.compiler.model.AsnModule.TagDefault;
import com.beanit.asn1bean.compiler.model.AsnNull;
import com.beanit.asn1bean.compiler.model.AsnObjectIdentifier;
import com.beanit.asn1bean.compiler.model.AsnOctetString;
import com.beanit.asn1bean.compiler.model.AsnParameter;
import com.beanit.asn1bean.compiler.model.AsnReal;
import com.beanit.asn1bean.compiler.model.AsnSequenceOf;
import com.beanit.asn1bean.compiler.model.AsnSequenceSet;
import com.beanit.asn1bean.compiler.model.AsnTag;
import com.beanit.asn1bean.compiler.model.AsnTaggedType;
import com.beanit.asn1bean.compiler.model.AsnType;
import com.beanit.asn1bean.compiler.model.AsnUniversalType;
import com.beanit.asn1bean.compiler.model.AsnValueAssignment;
import com.beanit.asn1bean.compiler.model.SymbolsFromModule;
import com.beanit.asn1bean.util.HexString;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BerClassWriter {

  private static final Set<String> reservedKeywords =
      Collections.unmodifiableSet(
          new TreeSet<>(
              Arrays.asList(
                  "public",
                  "private",
                  "protected",
                  "final",
                  "void",
                  "int",
                  "short",
                  "float",
                  "double",
                  "long",
                  "byte",
                  "char",
                  "String",
                  "throw",
                  "throws",
                  "new",
                  "static",
                  "volatile",
                  "if",
                  "else",
                  "for",
                  "switch",
                  "case",
                  "enum",
                  "this",
                  "super",
                  "boolean",
                  "class",
                  "abstract",
                  "package",
                  "import",
                  "null",
                  "code",
                  "getClass",
                  "setClass")));
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

  private final String basePackageName;
  private final boolean jaxbMode;
  private final HashMap<String, AsnModule> modulesByName;
  BufferedWriter out;
  private TagDefault tagDefault;
  private File outputBaseDir;
  private int indentNum = 0;
  private boolean insertVersion;
  private AsnModule module;
  private File outputDirectory;
  private String berTypeInterfaceString = "BerType, ";

  BerClassWriter(
      HashMap<String, AsnModule> modulesByName,
      String outputBaseDir,
      String basePackageName,
      boolean jaxbMode,
      boolean disableWritingVersion) {
    this.jaxbMode = jaxbMode;
    this.outputBaseDir = new File(outputBaseDir);

    insertVersion = !disableWritingVersion;

    if (basePackageName.isEmpty()) {
      this.basePackageName = "";
    } else {
      this.outputBaseDir = new File(this.outputBaseDir, basePackageName.replace('.', '/'));
      this.basePackageName = basePackageName + ".";
    }
    this.modulesByName = modulesByName;
  }

  private static String getBerTagParametersString(Tag tag) {
    return "BerTag."
        + tag.tagClass
        + "_CLASS, BerTag."
        + tag.typeStructure.toString()
        + ", "
        + tag.value;
  }

  public void translate() throws IOException {
    for (AsnModule module : modulesByName.values()) {
      for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
        if (modulesByName.get(symbolsFromModule.modref) == null) {
          throw new IOException(
              "Module \""
                  + module.moduleIdentifier.name
                  + "\" imports missing module \""
                  + symbolsFromModule.modref
                  + "\".");
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

    outputDirectory =
        new File(
            outputBaseDir, moduleToPackageName(module.moduleIdentifier.name).replace('.', '/'));

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
          writeRetaggingTypeClass(
              typeName, asnTaggedType.definedType.typeName, typeDefinition, tag);
        } else {

          AsnType assignedAsnType = asnTaggedType.typeReference;

          if (assignedAsnType instanceof AsnConstructedType) {
            writeConstructedTypeClass(typeName, assignedAsnType, tag, false, null);
          } else {
            writeRetaggingTypeClass(typeName, getBerType(assignedAsnType), typeDefinition, tag);
          }
        }

      } else if (typeDefinition instanceof AsnDefinedType) {
        writeRetaggingTypeClass(
            typeName, ((AsnDefinedType) typeDefinition).typeName, typeDefinition, null);
      } else if (typeDefinition instanceof AsnConstructedType) {
        writeConstructedTypeClass(typeName, typeDefinition, null, false, null);
      } else {
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
          oid = parseObjectIdentifierValue(valueName, module);
        } catch (IllegalStateException e) {
          System.out.println("Warning: could not parse object identifier value: " + e.getMessage());
          continue;
        }
        StringBuilder sb =
            new StringBuilder(
                "public static final BerObjectIdentifier "
                    + cleanUpName(valueName)
                    + " = new BerObjectIdentifier(new int[]{");
        if (first) {
          first = false;
          writeClassHeader("OidValues", module);
          write("public final class OidValues {");
        }

        boolean firstOidComponent = true;
        if (oid != null) {
          for (int i : oid.value) {
            if (firstOidComponent) {
              firstOidComponent = false;
            } else {
              sb.append(", ");
            }
            sb.append(i);
          }
        }
        sb.append("});");
        write(sb.toString());
      }
    }
    if (!first) {
      write("}");
      out.close();
    }
  }

  private String moduleToPackageName(String moduleName) {
    String[] moduleParts = moduleName.split("-", -1);
    StringBuilder packageName = new StringBuilder();
    for (String part : moduleParts) {
      if (packageName.length() > 0) {
        packageName.append(".");
      }
      packageName.append(sanitize(part.toLowerCase()));
    }
    return packageName.toString();
  }

  private BerObjectIdentifier parseObjectIdentifierValue(String name, AsnModule module) {

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
      } else if (Character.isLetter(token.charAt(0))) {
        if ((tokens.size() == i + 1) || !tokens.get(i + 1).equals("(")) {
          // this is either a value reference of another object identifier or a registered name
          if (!parseRegisteredOidComponentName(oidComponents, token)) {

            BerObjectIdentifier oid = parseObjectIdentifierValue(token, module);
            if (oid == null) {
              for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
                for (String importedTypeName : symbolsFromModule.symbolList) {
                  if (token.equals(importedTypeName)) {
                    oid =
                        parseObjectIdentifierValue(
                            token, modulesByName.get(symbolsFromModule.modref));
                  }
                }
              }
            }
            if (oid == null) {
              throw new IllegalStateException(
                  "AsnValueAssignment \""
                      + token
                      + "\" was not found in module \""
                      + module.moduleIdentifier.name
                      + "\"");
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

  private boolean parseRegisteredOidComponentName(List<Integer> oidComponents, String token) {
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
    } else if (oidComponents.size() == 1) {
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
          break;
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
   * Gets the tag from the AsnTaggedType structure. The returned tag will contain the correct class
   * and type (explicit or implicit). Return null if the passed tagged type does not have a tag.
   *
   * @param asnTaggedType the tagged type
   * @return the tag from the AsnTaggedType structure
   */
  private Tag getTag(AsnTaggedType asnTaggedType) {

    AsnTag asnTag = asnTaggedType.tag;

    if (asnTag == null) {
      return null;
    }

    Tag tag = new Tag();

    String tagClassString = asnTag.clazz;
    if (tagClassString.isEmpty() || "CONTEXT".equals(tagClassString)) {
      tag.tagClass = TagClass.CONTEXT;
    } else if ("APPLICATION".equals(tagClassString)) {
      tag.tagClass = TagClass.APPLICATION;
    } else if ("PRIVATE".equals(tagClassString)) {
      tag.tagClass = TagClass.PRIVATE;
    } else if ("UNIVERSAL".equals(tagClassString)) {
      tag.tagClass = TagClass.UNIVERSAL;
    } else {
      throw new IllegalStateException("unknown tag class: " + tagClassString);
    }

    String tagTypeString = asnTaggedType.tagType;

    if (tagTypeString.isEmpty()) {
      if (tagDefault == TagDefault.EXPLICIT) {
        tag.type = TagType.EXPLICIT;
      } else {
        tag.type = TagType.IMPLICIT;
      }
    } else if (tagTypeString.equals("IMPLICIT")) {
      tag.type = TagType.IMPLICIT;
    } else if (tagTypeString.equals("EXPLICIT")) {
      tag.type = TagType.EXPLICIT;
    } else {
      throw new IllegalStateException("unexpected tag type: " + tagTypeString);
    }

    if (tag.type == TagType.IMPLICIT) {
      if (isDirectAnyOrChoice(asnTaggedType)) {
        tag.type = TagType.EXPLICIT;
      }
    }

    if ((tag.type == TagType.IMPLICIT) && isPrimitive(asnTaggedType)) {
      tag.typeStructure = TypeStructure.PRIMITIVE;
    } else {
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
    if (name.isEmpty()) return name;
    String result = replaceCharByCamelCase(name, '.');
    if (Character.isDigit(result.charAt(0))) {
      result = "_" + result;
    }
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

  private void writeConstructedTypeClass(
      String className,
      AsnType asnType,
      Tag tag,
      boolean asInternalClass,
      List<String> listOfSubClassNames)
      throws IOException {

    if (listOfSubClassNames == null) {
      listOfSubClassNames = new ArrayList<>();
    }

    String isStaticStr = "";
    if (asInternalClass) {
      isStaticStr = " static";
    }

    if (asnType instanceof AsnSequenceSet) {
      writeSequenceOrSetClass(
          className, (AsnSequenceSet) asnType, tag, isStaticStr, listOfSubClassNames);
    } else if (asnType instanceof AsnSequenceOf) {
      writeSequenceOfClass(
          className, (AsnSequenceOf) asnType, tag, isStaticStr, listOfSubClassNames);
    } else if (asnType instanceof AsnChoice) {
      writeChoiceClass(className, (AsnChoice) asnType, tag, isStaticStr, listOfSubClassNames);
    }
  }

  private void writeChoiceClass(
      String className,
      AsnChoice asn1TypeElement,
      Tag tag,
      String isStaticStr,
      List<String> listOfSubClassNames)
      throws IOException {

    write(
        "public"
            + isStaticStr
            + " class "
            + className
            + " implements "
            + berTypeInterfaceString
            + "Serializable {\n");

    write("private static final long serialVersionUID = 1L;\n");

    write("private byte[] code = null;");

    if (tag != null) {
      write(
          "public static final BerTag tag = new BerTag(" + getBerTagParametersString(tag) + ");\n");
    }

    List<AsnElementType> componentTypes = asn1TypeElement.componentTypes;

    addAutomaticTagsIfNeeded(componentTypes);

    if (asn1TypeElement.parameters != null) {
      List<AsnParameter> parameters = asn1TypeElement.parameters;
      replaceParametersByAnyTypes(componentTypes, parameters);
    }

    for (AsnElementType componentType : componentTypes) {
      if ((componentType.typeReference instanceof AsnConstructedType)) {
        listOfSubClassNames.add(getClassName(componentType, className));
      }
    }

    for (AsnElementType componentType : componentTypes) {

      if (isInnerType(componentType)) {

        String subClassName = getClassName(componentType, className);
        writeConstructedTypeClass(
            subClassName, componentType.typeReference, null, true, listOfSubClassNames);
      }
    }

    setClassNamesOfComponents(listOfSubClassNames, componentTypes, className);

    writePublicMembers(componentTypes);

    writeEmptyConstructor(className);

    if (!jaxbMode) {
      writeEncodeConstructor(className, componentTypes);
    }

    if (jaxbMode) {
      writeGetterAndSetter(componentTypes);
    }

    writeChoiceEncodeFunction(componentTypes, tag != null);

    writeChoiceDecodeMethod(convertToComponentInfos(componentTypes), tag != null);

    writeEncodeAndSaveFunction(tag == null);

    writeChoiceToStringFunction(componentTypes);

    write("}\n");
  }

  private void setClassNamesOfComponents(
      List<String> listOfSubClassNames, List<AsnElementType> componentTypes, String parentClass) {
    for (AsnElementType element : componentTypes) {
      element.className = getClassName(listOfSubClassNames, element, parentClass);
    }
  }

  private String getClassName(AsnElementType asnElementType) {
    if (!asnElementType.className.equals("")) {
      return asnElementType.className;
    }
    return getClassName(null, asnElementType, null);
  }

  private String getClassName(AsnElementType asnElementType, String parentClass) {
    if (!asnElementType.className.isEmpty()) {
      return asnElementType.className;
    }
    return getClassName(null, asnElementType, parentClass);
  }

  private String getClassName(
      List<String> listOfSubClassNames, AsnTaggedType element, String parentClass) {

    if (listOfSubClassNames == null) {
      listOfSubClassNames = new ArrayList<>();
    }

    if (element.typeReference == null) {

      if (element.definedType.isObjectClassField) {

        AsnInformationObjectClass informationObjectClass =
            getInformationObjectClass(element.definedType.moduleOrObjectClassReference, module);
        if (informationObjectClass == null) {
          throw new CompileException(
              "no information object class of name \""
                  + element.definedType.moduleOrObjectClassReference
                  + "\" found in asn.1 module "
                  + module.moduleIdentifier.name);
        }

        for (AsnElementType elementType : informationObjectClass.elementList) {
          if (elementType.name.equals(element.definedType.typeName)) {
            return getClassName(listOfSubClassNames, elementType, parentClass);
          }
        }

        throw new IllegalStateException(
            "Could not find field \""
                + element.definedType.typeName
                + "\" of information object class \""
                + element.definedType.moduleOrObjectClassReference
                + "\"");
      } else {

        String cleanedUpClassName = cleanUpName(element.definedType.typeName);
        for (String subClassName : listOfSubClassNames) {
          if (subClassName.equals(cleanedUpClassName)) {
            String moduleName = module.moduleIdentifier.name;

            for (SymbolsFromModule symbols : this.module.importSymbolFromModuleList) {
              if (symbols.symbolList.contains(element.definedType.typeName)) {
                moduleName = symbols.modref;
                break;
              }
            }

            return basePackageName + moduleToPackageName(moduleName) + "." + cleanedUpClassName;
          }
        }
        return cleanedUpClassName;
      }
    } else {
      AsnType typeDefinition = element.typeReference;

      if (typeDefinition instanceof AsnConstructedType) {
        String cleanedUpName = cleanUpName(capitalizeFirstCharacter(element.name));
        if (parentClass != null) {
          if (cleanedUpName.equals(parentClass)) {
            cleanedUpName = cleanedUpName + "_";
          }
        }
        return cleanedUpName;
      } else {
        return getBerType(typeDefinition);
      }
    }
  }

  private AsnInformationObjectClass getInformationObjectClass(
      String objectClassReference, AsnModule module) {

    AsnInformationObjectClass ioClass = module.objectClassesByName.get(objectClassReference);
    if (ioClass == null) {
      AsnType asnType = module.typesByName.get(objectClassReference);
      if (asnType == null) {
        for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
          for (String importedTypeName : symbolsFromModule.symbolList) {
            if (objectClassReference.equals(importedTypeName)) {
              return getInformationObjectClass(
                  objectClassReference, getAsnModule(symbolsFromModule.modref));
            }
          }
        }
        return null;
      } else {
        if (asnType instanceof AsnDefinedType) {

          // error handling: if the reference and the type name are the same, this is an infinite
          // loop
          if (objectClassReference.equals(((AsnDefinedType) asnType).typeName)) {
            throw new CompileException(
                "Self reference "
                    + objectClassReference
                    + " to "
                    + ((AsnDefinedType) asnType).typeName);
          }
          return getInformationObjectClass(((AsnDefinedType) asnType).typeName, module);
        }
      }
    }
    return ioClass;
  }

  private void writeSequenceOrSetClass(
      String className,
      AsnSequenceSet asnSequenceSet,
      Tag tag,
      String isStaticStr,
      List<String> listOfSubClassNames)
      throws IOException {

    write(
        "public"
            + isStaticStr
            + " class "
            + className
            + " implements "
            + berTypeInterfaceString
            + "Serializable {\n");

    write("private static final long serialVersionUID = 1L;\n");

    List<AsnElementType> componentTypes = asnSequenceSet.componentTypes;

    addAutomaticTagsIfNeeded(componentTypes);

    if (asnSequenceSet.parameters != null) {
      List<AsnParameter> parameters = asnSequenceSet.parameters;
      replaceParametersByAnyTypes(componentTypes, parameters);
    }

    for (AsnElementType componentType : componentTypes) {
      if ((componentType.typeReference instanceof AsnConstructedType)) {
        listOfSubClassNames.add(getClassName(componentType));
      }
    }

    for (AsnElementType componentType : componentTypes) {

      if (isInnerType(componentType)) {

        String subClassName = getClassName(componentType, className);

        writeConstructedTypeClass(
            subClassName, componentType.typeReference, null, true, listOfSubClassNames);
      }
    }

    Tag mainTag;
    if (tag == null) {
      if (asnSequenceSet.isSequence) {
        mainTag = stdSeqTag;
      } else {
        mainTag = stdSetTag;
      }
    } else {
      mainTag = tag;
    }

    write(
        "public static final BerTag tag = new BerTag("
            + getBerTagParametersString(mainTag)
            + ");\n");

    write("private byte[] code = null;");

    setClassNamesOfComponents(listOfSubClassNames, componentTypes, className);

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
      writeSequenceDecodeMethod(convertToComponentInfos(componentTypes), hasExplicitTag);
    } else {
      writeSetDecodeFunction(convertToComponentInfos(componentTypes), hasExplicitTag);
    }

    writeEncodeAndSaveFunction();

    writeSequenceOrSetToStringFunction(componentTypes);

    write("}\n");
  }

  private List<ComponentInfo> convertToComponentInfos(List<AsnElementType> asnElementTypes) {
    int lastRequiredComponentIndex = getLastRequiredComponentIndex(asnElementTypes);

    List<ComponentInfo> componentInfos = new ArrayList<>(asnElementTypes.size());
    int i = 0;
    for (AsnElementType asnElementType : asnElementTypes) {
      boolean mayBeLastComponent = lastRequiredComponentIndex <= i;
      componentInfos.add(convertToComponentInfo(asnElementType, mayBeLastComponent, null));
      i++;
    }
    return componentInfos;
  }

  private ComponentInfo convertToComponentInfo(
      AsnElementType asnElementType, boolean mayBeLastComponent, String sequenceOrSetOfClassName) {
    String variableName = getVariableName(asnElementType);
    String className;
    if (sequenceOrSetOfClassName != null) {
      className = sequenceOrSetOfClassName;
    } else {
      className = getClassName(asnElementType);
    }
    Tag componentTag = getTag(asnElementType);
    boolean isOptional = isOptional(asnElementType);
    boolean isUntaggedAnyOrChoice = isDirectAnyOrChoice(asnElementType);

    return new ComponentInfo(
        variableName,
        className,
        componentTag,
        mayBeLastComponent,
        isOptional,
        isUntaggedAnyOrChoice);
  }

  private void replaceParametersByAnyTypes(
      List<AsnElementType> componentTypes, List<AsnParameter> parameters) {
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
    write("@Override public int decode(InputStream is) throws IOException {");
    write("return decode(is, " + param + ");");
    write("}\n");
  }

  private void writeSimpleEncodeFunction() throws IOException {
    write("@Override public int encode(OutputStream reverseOS) throws IOException {");
    write("return encode(reverseOS, true);");
    write("}\n");
  }

  private void writeSequenceOfClass(
      String className,
      AsnSequenceOf asnSequenceOf,
      Tag tag,
      String isStaticStr,
      List<String> listOfSubClassNames)
      throws IOException {

    write(
        "public"
            + isStaticStr
            + " class "
            + className
            + " implements "
            + berTypeInterfaceString
            + "Serializable {\n");

    write("private static final long serialVersionUID = 1L;\n");

    AsnElementType componentType = asnSequenceOf.componentType;

    String referencedTypeName = getClassNameOfSequenceOfElement(componentType, listOfSubClassNames);

    if (isInnerType(componentType)) {
      writeConstructedTypeClass(
          referencedTypeName, componentType.typeReference, null, true, listOfSubClassNames);
    }

    Tag mainTag;
    if (tag == null) {
      if (asnSequenceOf.isSequenceOf) {
        mainTag = stdSeqTag;
      } else {
        mainTag = stdSetTag;
      }
    } else {
      mainTag = tag;
    }

    write(
        "public static final BerTag tag = new BerTag(" + getBerTagParametersString(mainTag) + ");");

    write("private byte[] code = null;");

    if (jaxbMode) {
      write("private List<" + referencedTypeName + "> seqOf = null;\n");
    } else {
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

    writeSequenceOrSetOfDecodeFunction(
        convertToComponentInfo(componentType, false, referencedTypeName),
        hasExplicitTag,
        asnSequenceOf.isSequenceOf);

    writeEncodeAndSaveFunction();

    writeSequenceOrSetOfToStringFunction(referencedTypeName, componentType);

    write("}\n");
  }

  private void writeRetaggingTypeClass(
      String typeName, String assignedTypeName, AsnType typeDefinition, Tag tag)
      throws IOException {

    write("public class " + typeName + " extends " + cleanUpName(assignedTypeName) + " {\n");

    write("private static final long serialVersionUID = 1L;\n");

    String[] constructorParameters = getConstructorParameters(getUniversalType(typeDefinition));

    if (tag != null) {
      write(
          "public static final BerTag tag = new BerTag(" + getBerTagParametersString(tag) + ");\n");
    }
    if (constructorParameters.length != 2 || !constructorParameters[0].equals("byte[]")) {
      if (tag != null) {
        write("private byte[] code = null;\n");
      }
    }

    write("public " + typeName + "() {");
    write("}\n");

    if (constructorParameters.length != 2 || !constructorParameters[0].equals("byte[]")) {
      write("public " + typeName + "(byte[] code) {");
      if (tag != null) {
        write("this.code = code;");
      } else {
        write("super(code);");
      }
      write("}\n");
    }

    if ((!jaxbMode || isPrimitiveOrRetaggedPrimitive(typeDefinition))
        && (constructorParameters.length != 0)) {
      StringBuilder constructorParameterString = new StringBuilder();
      StringBuilder superCallParameterString = new StringBuilder();
      for (int i = 0; i < constructorParameters.length; i += 2) {
        if (i > 0) {
          constructorParameterString.append(", ");
          superCallParameterString.append(", ");
        }
        constructorParameterString
            .append(constructorParameters[i])
            .append(" ")
            .append(constructorParameters[i + 1]);
        superCallParameterString.append(constructorParameters[i + 1]);
      }

      write("public " + typeName + "(" + constructorParameterString + ") {");
      write("super(" + superCallParameterString + ");");
      write("}\n");

      if (constructorParameters[0].equals("BigInteger")) {
        write("public " + typeName + "(long value) {");
        write("super(value);");
        write("}\n");
      } else if (constructorParameters.length == 4 && constructorParameters[3].equals("numBits")) {
        write("public " + typeName + "(boolean[] value) {");
        write("super(value);");
        write("}\n");
      }
    }

    if (tag != null) {

      String overrideAnnotationString = "@Override ";
      if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
        writeSimpleEncodeFunction();
        overrideAnnotationString = "";
      }

      write(
          overrideAnnotationString
              + "public int encode(OutputStream reverseOS, boolean withTag) throws IOException {\n");

      if (constructorParameters.length != 2 || !constructorParameters[0].equals("byte[]")) {
        writeEncodingIfCodeIsNull();
      }

      write("int codeLength;\n");

      if (tag.type == TagType.EXPLICIT) {
        if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
          write("codeLength = super.encode(reverseOS);");
        } else {
          write("codeLength = super.encode(reverseOS, true);");
        }
        write("codeLength += BerLength.encodeLength(reverseOS, codeLength);");
      } else {
        write("codeLength = super.encode(reverseOS, false);");
      }

      write("if (withTag) {");
      write("codeLength += tag.encode(reverseOS);");
      write("}\n");

      write("return codeLength;");
      write("}\n");

      if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
        writeSimpleDecodeFunction("true");
      }

      write(
          overrideAnnotationString
              + "public int decode(InputStream is, boolean withTag) throws IOException {\n");

      write("int codeLength = 0;\n");

      write("if (withTag) {");
      write("codeLength += tag.decodeAndCheck(is);");
      write("}\n");

      if (tag.type == TagType.EXPLICIT) {

        write("BerLength length = new BerLength();");
        write("codeLength += length.decode(is);\n");

        if (isDirectAnyOrChoice((AsnTaggedType) typeDefinition)) {
          write("codeLength += super.decode(is, null);");
        } else {
          write("codeLength += super.decode(is, true);");
        }
        write("codeLength += length.readEocIfIndefinite(is);\n");
      } else {
        write("codeLength += super.decode(is, false);\n");
      }

      write("return codeLength;");
      write("}\n");
    }

    write("}");
  }

  private void writeEncodingIfCodeIsNull() throws IOException {
    write("if (code != null) {");
    write("reverseOS.write(code);");
    write("if (withTag) {");
    write("return tag.encode(reverseOS) + code.length;");
    write("}");
    write("return code.length;");
    write("}\n");
  }

  private void writeChoiceEncodeFunction(
      List<AsnElementType> componentTypes, boolean hasExplicitTag) throws IOException {
    if (hasExplicitTag) {
      writeSimpleEncodeFunction();
      write("public int encode(OutputStream reverseOS, boolean withTag) throws IOException {\n");
    } else {
      write("@Override public int encode(OutputStream reverseOS) throws IOException {\n");
    }

    write("if (code != null) {");
    write("reverseOS.write(code);");
    if (hasExplicitTag) {
      write("if (withTag) {");
      write("return tag.encode(reverseOS) + code.length;");
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

      write("if (" + getVariableName(componentType) + " != null) {");

      String explicitEncoding = getExplicitEncodingParameter(componentType);

      if (isExplicit(componentTag)) {
        write(
            "sublength = "
                + getVariableName(componentType)
                + ".encode(reverseOS"
                + explicitEncoding
                + ");");
        write("codeLength += sublength;");
        write("codeLength += BerLength.encodeLength(reverseOS, sublength);");
      } else {
        write(
            "codeLength += "
                + getVariableName(componentType)
                + ".encode(reverseOS"
                + explicitEncoding
                + ");");
      }

      if (componentTag != null) {
        writeEncodeTag(componentTag);
      }

      if (hasExplicitTag) {
        write("codeLength += BerLength.encodeLength(reverseOS, codeLength);");
        write("if (withTag) {");
        write("codeLength += tag.encode(reverseOS);");
        write("}");
      }

      write("return codeLength;");
      write("}");

      write("");
    }

    write("throw new IOException(\"Error encoding CHOICE: No element of CHOICE was selected.\");");

    write("}\n");
  }

  private void writeSequenceOrSetEncodeFunction(
      List<AsnElementType> componentTypes, boolean hasExplicitTag, boolean isSequence)
      throws IOException {
    write("public int encode(OutputStream reverseOS, boolean withTag) throws IOException {\n");

    writeEncodingIfCodeIsNull();

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
        write("if (" + getVariableName(componentType) + " != null) {");
      }

      String explicitEncoding = getExplicitEncodingParameter(componentType);

      if (isExplicit(componentTag)) {
        write(
            "sublength = "
                + getVariableName(componentType)
                + ".encode(reverseOS"
                + explicitEncoding
                + ");");
        write("codeLength += sublength;");
        write("codeLength += BerLength.encodeLength(reverseOS, sublength);");
      } else {
        write(
            "codeLength += "
                + getVariableName(componentType)
                + ".encode(reverseOS"
                + explicitEncoding
                + ");");
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
      write("codeLength += BerLength.encodeLength(reverseOS, codeLength);");
      if (isSequence) {
        write("reverseOS.write(0x30);");
      } else {
        write("reverseOS.write(0x31);");
      }
      write("codeLength++;\n");
    }

    write("codeLength += BerLength.encodeLength(reverseOS, codeLength);\n");

    write("if (withTag) {");
    write("codeLength += tag.encode(reverseOS);");
    write("}\n");

    write("return codeLength;\n");

    write("}\n");
  }

  private void writeSequenceOfEncodeFunction(
      AsnElementType componentType, boolean hasExplicitTag, boolean isSequence) throws IOException {
    write("public int encode(OutputStream reverseOS, boolean withTag) throws IOException {\n");

    writeEncodingIfCodeIsNull();

    write("int codeLength = 0;");

    write("for (int i = (seqOf.size() - 1); i >= 0; i--) {");

    Tag componentTag = getTag(componentType);
    String explicitEncoding = getExplicitEncodingParameter(componentType);

    if (componentTag != null) {

      if (componentTag.type == TagType.EXPLICIT) {
        write("int sublength = seqOf.get(i).encode(reverseOS" + explicitEncoding + ");");
        write("codeLength += sublength;");
        write("codeLength += BerLength.encodeLength(reverseOS, sublength);");
      } else {
        write("codeLength += seqOf.get(i).encode(reverseOS" + explicitEncoding + ");");
      }

      writeEncodeTag(componentTag);
    } else {

      if (isDirectAnyOrChoice(componentType)) {
        write("codeLength += seqOf.get(i).encode(reverseOS);");
      } else {
        write("codeLength += seqOf.get(i).encode(reverseOS, true);");
      }
    }

    write("}\n");

    if (hasExplicitTag) {
      write("codeLength += BerLength.encodeLength(reverseOS, codeLength);");
      if (isSequence) {
        write("reverseOS.write(0x30);");
      } else {
        write("reverseOS.write(0x31);");
      }
      write("codeLength++;\n");
    }

    write("codeLength += BerLength.encodeLength(reverseOS, codeLength);\n");

    write("if (withTag) {");
    write("codeLength += tag.encode(reverseOS);");
    write("}\n");

    write("return codeLength;");
    write("}\n");
  }

  private String getExplicitEncodingParameter(AsnTaggedType componentType) {
    Tag tag = getTag(componentType);

    if (tag != null && tag.type == TagType.IMPLICIT) {
      return ", false";
    } else {
      if (isDirectAnyOrChoice(componentType)) {
        return "";
      } else {
        return ", true";
      }
    }
  }

  private void writeSequenceDecodeMethod(List<ComponentInfo> components, boolean hasExplicitTag)
      throws IOException {
    write("public int decode(InputStream is, boolean withTag) throws IOException {");
    write("int tlByteCount = 0;");
    write("int vByteCount = 0;");
    if (containsUntaggedChoiceOrAny(components)) {
      write("int numDecodedBytes;");
    }
    write("BerTag berTag = new BerTag();\n");

    write("if (withTag) {");
    write("tlByteCount += tag.decodeAndCheck(is);");
    write("}\n");

    if (hasExplicitTag) {
      write("BerLength explicitTagLength = new BerLength();");
      write("tlByteCount += explicitTagLength.decode(is);");
      write("tlByteCount += BerTag.SEQUENCE.decodeAndCheck(is);\n");
    }

    write("BerLength length = new BerLength();");
    write("tlByteCount += length.decode(is);");
    write("int lengthVal = length.val;");

    if (allOptionalOrDefault(components)) {
      write("if (lengthVal == 0) {");
      write("return tlByteCount;");
      write("}");
    }
    write("vByteCount += berTag.decode(is);\n");

    for (ComponentInfo component : components) {
      if (component.isDirectChoiceOrAny && (component.tag == null)) {
        writeSequenceComponentDecodeUntaggedChoiceOrAny(component);
      } else {
        writeSequenceComponentDecodeRegular(component);
        write("");
      }
    }

    write("if (lengthVal < 0) {");
    write("if (!berTag.equals(0, 0, 0)) {");
    write("throw new IOException(\"Decoded sequence has wrong end of contents octets\");");
    write("}");
    write("vByteCount += BerLength.readEocByte(is);");
    if (hasExplicitTag) {
      write("vByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }
    write("return tlByteCount + vByteCount;");
    write("}\n");

    write(
        "throw new IOException(\"Unexpected end of sequence, length tag: \" + lengthVal + \", bytes decoded: \" + vByteCount);\n");

    write("}\n");
  }

  private void writeChoiceDecodeMethod(List<ComponentInfo> components, boolean hasExplicitTag)
      throws IOException {

    if (hasExplicitTag) {
      writeSimpleDecodeFunction("true");

      write("public int decode(InputStream is, boolean withTag) throws IOException {");
      write("int tlvByteCount = 0;");
      write("BerTag berTag = new BerTag();\n");

      write("if (withTag) {");
      write("tlvByteCount += tag.decodeAndCheck(is);");
      write("}\n");

      write("BerLength explicitTagLength = new BerLength();");
      write("tlvByteCount += explicitTagLength.decode(is);");
      write("tlvByteCount += berTag.decode(is);\n");
    } else {

      writeSimpleDecodeFunction("null");

      write("public int decode(InputStream is, BerTag berTag) throws IOException {\n");

      write("int tlvByteCount = 0;");
      write("boolean tagWasPassed = (berTag != null);\n");

      write("if (berTag == null) {");
      write("berTag = new BerTag();");
      write("tlvByteCount += berTag.decode(is);");
      write("}\n");
    }

    if (containsUntaggedChoiceOrAny(components)) {
      write("int numDecodedBytes;\n");
    }

    for (ComponentInfo component : components) {
      if (component.isDirectChoiceOrAny && (component.tag == null)) {
        writeChoiceComponentDecodeUntaggedChoiceOrAny(component, hasExplicitTag);
      } else {
        writeChoiceComponentDecodeRegular(component, hasExplicitTag);
      }
    }

    if (!hasExplicitTag) {
      write("if (tagWasPassed) {");
      write("return 0;");
      write("}\n");
    }

    write(
        "throw new IOException(\"Error decoding CHOICE: Tag \" + berTag + \" matched to no item.\");");

    write("}\n");
  }

  private void writeSetDecodeFunction(List<ComponentInfo> components, boolean hasExplicitTag)
      throws IOException {

    write("public int decode(InputStream is, boolean withTag) throws IOException {");
    write("int tlByteCount = 0;");
    write("int vByteCount = 0;");
    write("BerTag berTag = new BerTag();\n");

    write("if (withTag) {");
    write("tlByteCount += tag.decodeAndCheck(is);");
    write("}\n");

    if (hasExplicitTag) {
      write("BerLength explicitTagLength = new BerLength();");
      write("tlByteCount += explicitTagLength.decode(is);");
      write("tlByteCount += BerTag.SET.decodeAndCheck(is);\n");
    }

    write("BerLength length = new BerLength();");
    write("tlByteCount += length.decode(is);");
    write("int lengthVal = length.val;\n");

    if (allOptionalOrDefault(components)) {
      write("if (lengthVal == 0) {");
      write("return tlByteCount;");
      write("}\n");
    }

    write("while (vByteCount < lengthVal || lengthVal < 0) {");
    write("vByteCount += berTag.decode(is);");

    boolean first = true;
    for (ComponentInfo component : components) {
      if (component.isDirectChoiceOrAny && (component.tag == null)) {
        throw new IOException("choice or ANY within set has no explicit tag.");
      } else {
        writeSetComponentDecodeRegular(component, first);
      }
      first = false;
    }

    write("else if (lengthVal < 0 && berTag.equals(0, 0, 0)) {");
    write("vByteCount += BerLength.readEocByte(is);");
    if (hasExplicitTag) {
      write("vByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }
    write("return tlByteCount + vByteCount;");
    write("}");

    write("else {");
    write("throw new IOException(\"Tag does not match any set component: \" + berTag);");
    write("}");

    write("}");

    write("if (vByteCount != lengthVal) {");
    write(
        "throw new IOException(\"Length of set does not match length tag, length tag: \" + lengthVal + \", actual set length: \" + vByteCount);");
    write("}");
    if (hasExplicitTag) {
      write("vByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }

    write("return tlByteCount + vByteCount;");
    write("}\n");
  }

  private void writeSequenceOrSetOfDecodeFunction(
      ComponentInfo component, boolean hasExplicitTag, boolean isSequence) throws IOException {

    write("public int decode(InputStream is, boolean withTag) throws IOException {");
    write("int tlByteCount = 0;");
    write("int vByteCount = 0;");
    if (containsUntaggedChoiceOrAny(Collections.singletonList(component))) {
      write("int numDecodedBytes;");
    }
    write("BerTag berTag = new BerTag();");

    write("if (withTag) {");
    write("tlByteCount += tag.decodeAndCheck(is);");
    write("}\n");

    if (hasExplicitTag) {
      write("BerLength explicitTagLength = new BerLength();");
      write("tlByteCount += explicitTagLength.decode(is);");
      if (isSequence) {
        write("tlByteCount += BerTag.SEQUENCE.decodeAndCheck(is);\n");
      } else {
        write("tlByteCount += BerTag.SET.decodeAndCheck(is);\n");
      }
    }

    write("BerLength length = new BerLength();");
    write("tlByteCount += length.decode(is);");
    write("int lengthVal = length.val;\n");

    write("while (vByteCount < lengthVal || lengthVal < 0) {");
    write("vByteCount += berTag.decode(is);\n");

    write("if (lengthVal < 0 && berTag.equals(0, 0, 0)) {");
    write("vByteCount += BerLength.readEocByte(is);");
    write("break;");
    write("}\n");

    if (component.isDirectChoiceOrAny && (component.tag == null)) {
      writeSequenceOfComponentDecodeUntaggedChoiceOrAny(component);
    } else {
      writeSequenceOfComponentDecodeRegular(component);
    }

    write("}");

    write("if (lengthVal >= 0 && vByteCount != lengthVal) {");
    write(
        "throw new IOException(\"Decoded SequenceOf or SetOf has wrong length. Expected \" + lengthVal + \" but has \" + vByteCount);\n");
    write("}");

    if (hasExplicitTag) {
      write("vByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }
    write("return tlByteCount + vByteCount;");
    write("}\n");
  }

  private void writeSequenceComponentDecodeRegular(ComponentInfo component) throws IOException {

    if (component.tag != null) {
      write("if (berTag.equals(" + getBerTagParametersString(component.tag) + ")) {");
    } else {
      write("if (berTag.equals(" + component.className + ".tag)) {");
    }

    if (isExplicit(component.tag)) {
      write("vByteCount += length.decode(is);");
    }

    write(component.variableName + " = new " + component.className + "();");
    write(
        "vByteCount += "
            + component.variableName
            + ".decode(is, "
            + getDecodeTagParameter(component)
            + ");");

    if (isExplicit(component.tag)) {
      write("vByteCount += length.readEocIfIndefinite(is);");
    }

    if (component.mayBeLast) {
      writeReturnIfDefiniteLengthMatchesDecodedBytes();
    }
    write("vByteCount += berTag.decode(is);");
    write("}");
    if (!component.isOptionalOrDefault) {
      writeElseThrowTagMatchingException();
    }
  }

  private void writeSequenceOfComponentDecodeRegular(ComponentInfo component) throws IOException {
    if (component.tag != null) {
      write("if (!berTag.equals(" + getBerTagParametersString(component.tag) + ")) {");
    } else {
      write("if (!berTag.equals(" + component.className + ".tag)) {");
    }
    write("throw new IOException(\"Tag does not match mandatory sequence of/set of component.\");");
    write("}");

    if (isExplicit(component.tag)) {
      write("vByteCount += length.decode(is);");
    }
    write(component.className + " element = new " + component.className + "();");
    write("vByteCount += " + "element.decode(is, " + getDecodeTagParameter(component) + ");");

    write("seqOf.add(element);");
    if (isExplicit(component.tag)) {
      write("vByteCount += length.readEocIfIndefinite(is);");
    }
  }

  private void writeSetComponentDecodeRegular(ComponentInfo component, boolean first)
      throws IOException {

    String elseString = first ? "" : "else ";
    if (component.tag != null) {
      write(elseString + "if (berTag.equals(" + getBerTagParametersString(component.tag) + ")) {");
    } else {
      write(elseString + "if (berTag.equals(" + component.className + ".tag)) {");
    }

    if (isExplicit(component.tag)) {
      write("vByteCount += length.decode(is);");
    }

    write(component.variableName + " = new " + component.className + "();");
    write(
        "vByteCount += "
            + component.variableName
            + ".decode(is, "
            + getDecodeTagParameter(component)
            + ");");

    if (isExplicit(component.tag)) {
      write("vByteCount += length.readEocIfIndefinite(is);");
    }
    write("}");
  }

  private void writeChoiceComponentDecodeRegular(ComponentInfo component, boolean taggedChoice)
      throws IOException {
    if (component.tag != null) {
      write("if (berTag.equals(" + getBerTagParametersString(component.tag) + ")) {");
    } else {
      write("if (berTag.equals(" + component.className + ".tag)) {");
    }

    if (isExplicit(component.tag)) {
      write("BerLength length = new BerLength();");
      write("tlvByteCount += length.decode(is);");
    }

    write(component.variableName + " = new " + component.className + "();");
    write(
        "tlvByteCount += "
            + component.variableName
            + ".decode(is, "
            + getDecodeTagParameter(component)
            + ");");

    if (isExplicit(component.tag)) {
      write("tlvByteCount += length.readEocIfIndefinite(is);");
    }
    if (taggedChoice) {
      write("tlvByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }
    write("return tlvByteCount;");
    write("}\n");
  }

  private void writeSequenceComponentDecodeUntaggedChoiceOrAny(ComponentInfo component)
      throws IOException {
    write(component.variableName + " = new " + component.className + "();");
    write(
        "numDecodedBytes = "
            + component.variableName
            + ".decode(is, "
            + getDecodeTagParameter(component)
            + ");");

    write("if (numDecodedBytes != 0) {");
    write("vByteCount += numDecodedBytes;");

    if (component.mayBeLast) {
      writeReturnIfDefiniteLengthMatchesDecodedBytes();
    }
    write("vByteCount += berTag.decode(is);");
    write("}");
    if (component.isOptionalOrDefault) {
      write("else {");
      write(component.variableName + " = null;");
      write("}");
    } else {
      writeElseThrowTagMatchingException();
    }
  }

  private void writeSequenceOfComponentDecodeUntaggedChoiceOrAny(ComponentInfo component)
      throws IOException {
    write(component.className + " element = new " + component.className + "();");
    write("numDecodedBytes = " + "element.decode(is, " + getDecodeTagParameter(component) + ");");
    write("if (numDecodedBytes == 0) {");
    write("throw new IOException(\"Tag did not match\");");
    write("}");
    write("vByteCount += numDecodedBytes;");
    write("seqOf.add(element);");
  }

  private void writeChoiceComponentDecodeUntaggedChoiceOrAny(
      ComponentInfo component, boolean taggedChoice) throws IOException {
    write(component.variableName + " = new " + component.className + "();");
    write(
        "numDecodedBytes = "
            + component.variableName
            + ".decode(is, "
            + getDecodeTagParameter(component)
            + ");");
    write("if (numDecodedBytes != 0) {");
    if (taggedChoice) {
      write("tlvByteCount += explicitTagLength.readEocIfIndefinite(is);");
    }
    write("return tlvByteCount + numDecodedBytes;");
    write("}");
    write("else {");
    write(component.variableName + " = null;");
    write("}\n");
  }

  private boolean containsUntaggedChoiceOrAny(List<ComponentInfo> components) {
    for (ComponentInfo component : components) {
      if (component.isDirectChoiceOrAny && (component.tag == null)) {
        return true;
      }
    }
    return false;
  }

  private String getDecodeTagParameter(ComponentInfo component) {
    if (component.isDirectChoiceOrAny) {
      return isExplicit(component.tag) ? "null" : "berTag";
    } else {
      return isExplicit(component.tag) ? "true" : "false";
    }
  }

  private boolean allOptionalOrDefault(List<ComponentInfo> componentTypes) {
    return componentTypes.size() == 0
        || (componentTypes.get(0).mayBeLast && componentTypes.get(0).isOptionalOrDefault);
  }

  private void writeElseThrowTagMatchingException() throws IOException {
    write("else {");
    write("throw new IOException(\"Tag does not match mandatory sequence component.\");");
    write("}");
  }

  private int getLastRequiredComponentIndex(List<AsnElementType> componentTypes) {
    int lastNoneOptionalFieldIndex = -1;
    for (int j = 0; j < componentTypes.size(); j++) {
      AsnElementType componentType = componentTypes.get(componentTypes.size() - 1 - j);
      if (!isOptional(componentType)) {
        lastNoneOptionalFieldIndex = componentTypes.size() - 1 - j;
        break;
      }
    }
    return lastNoneOptionalFieldIndex;
  }

  private void writeReturnIfDefiniteLengthMatchesDecodedBytes() throws IOException {
    write("if (lengthVal >= 0 && vByteCount == lengthVal) {");
    write("return tlByteCount + vByteCount;");
    write("}");
  }

  private void writeToStringFunction() throws IOException {
    write("@Override public String toString() {");
    write("StringBuilder sb = new StringBuilder();");
    write("appendAsString(sb, 0);");
    write("return sb.toString();");
    write("}\n");
  }

  private void writeChoiceToStringFunction(List<AsnElementType> componentTypes) throws IOException {
    writeToStringFunction();

    write("public void appendAsString(StringBuilder sb, int indentLevel) {\n");

    for (AsnElementType componentType : componentTypes) {
      write("if (" + getVariableName(componentType) + " != null) {");

      if (!isPrimitive(getUniversalType(componentType))) {
        write("sb.append(\"" + getVariableName(componentType) + ": \");");
        write(getVariableName(componentType) + ".appendAsString(sb, indentLevel + 1);");
      } else {
        write(
            "sb.append(\""
                + getVariableName(componentType)
                + ": \").append("
                + getVariableName(componentType)
                + ");");
      }
      write("return;");
      write("}\n");
    }

    write("sb.append(\"<none>\");");

    write("}\n");
  }

  private void writeSequenceOrSetToStringFunction(List<AsnElementType> componentTypes)
      throws IOException {

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
        write("if (" + getVariableName(componentType) + " != null) {");
      }

      if (j != 0) {
        if (checkIfFirstSelectedElement) {

          write("if (!firstSelectedElement) {");
        }
        write("sb.append(\",\\n\");");
        if (checkIfFirstSelectedElement) {
          write("}");
        }
      } else {
        write("sb.append(\"\\n\");");
      }

      write("for (int i = 0; i < indentLevel + 1; i++) {");
      write("sb.append(\"\\t\");");
      write("}");
      if (!isOptional(componentType)) {
        write("if (" + getVariableName(componentType) + " != null) {");
      }
      if (!isPrimitive(getUniversalType(componentType))) {
        write("sb.append(\"" + getVariableName(componentType) + ": \");");
        write(getVariableName(componentType) + ".appendAsString(sb, indentLevel + 1);");
      } else {
        write(
            "sb.append(\""
                + getVariableName(componentType)
                + ": \").append("
                + getVariableName(componentType)
                + ");");
      }
      if (!isOptional(componentType)) {
        write("}");
        write("else {");
        write("sb.append(\"" + getVariableName(componentType) + ": <empty-required-field>\");");
        write("}");
      }

      if (isOptional(componentType)) {
        if (checkIfFirstSelectedElement) {
          write("firstSelectedElement = false;");
        }
        write("}");
      } else {
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

  private void writeSequenceOrSetOfToStringFunction(
      String referencedTypeName, AsnElementType componentType) throws IOException {

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
    } else {
      write("sb.append(it.next());");
    }

    write("while (it.hasNext()) {");
    write("sb.append(\",\\n\");");
    write("for (int i = 0; i < indentLevel + 1; i++) {");
    write("sb.append(\"\\t\");");
    write("}");

    if (!isPrimitive(getUniversalType(componentType))) {
      write("it.next().appendAsString(sb, indentLevel + 1);");
    } else {
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

  private void addAutomaticTagsIfNeeded(List<AsnElementType> componentTypes) {
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
    write(
        "ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);");
    if (isTagless) {
      write("encode(reverseOS);");
    } else {
      write("encode(reverseOS, false);");
    }
    write("code = reverseOS.getArray();");
    write("}\n");
  }

  private void writeEncodeTag(Tag tag) throws IOException {
    int typeStructure;

    if (tag.typeStructure == TypeStructure.CONSTRUCTED) {
      typeStructure = BerTag.CONSTRUCTED;
    } else {
      typeStructure = BerTag.PRIMITIVE;
    }

    BerTag berTag = new BerTag(getTagClassId(tag.tagClass.toString()), typeStructure, tag.value);

    write("// write tag: " + tag.tagClass + "_CLASS, " + tag.typeStructure + ", " + tag.value);
    for (int i = (berTag.tagBytes.length - 1); i >= 0; i--) {
      write("reverseOS.write(0x" + HexString.fromByte(berTag.tagBytes[i]) + ");");
    }

    write("codeLength += " + berTag.tagBytes.length + ";");
  }

  private int getTagClassId(String tagClass) {

    switch (tagClass) {
      case "UNIVERSAL":
        return BerTag.UNIVERSAL_CLASS;
      case "APPLICATION":
        return BerTag.APPLICATION_CLASS;
      case "CONTEXT":
        return BerTag.CONTEXT_CLASS;
      case "PRIVATE":
        return BerTag.PRIVATE_CLASS;
      default:
        throw new IllegalStateException("unknown tag class: " + tagClass);
    }
  }

  private String getVariableName(AsnElementType componentType) {
    return cleanUpName(componentType.name);
  }

  private boolean isOptional(AsnElementType componentType) {
    return (componentType.isOptional || componentType.isDefault);
  }

  private boolean isExplicit(Tag tag) {
    return (tag != null) && (tag.type == TagType.EXPLICIT);
  }

  private void writeEncodeConstructor(String className, List<AsnElementType> componentTypes)
      throws IOException {

    if (componentTypes.isEmpty()) {
      return;
    }

    StringBuilder line = new StringBuilder("public " + className + "(");

    int j = 0;

    for (AsnElementType componentType : componentTypes) {

      if (j != 0) {
        line.append(", ");
      }
      j++;
      line.append(getClassName(componentType)).append(" ").append(cleanUpName(componentType.name));
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
      } else {
        write("public " + element.className + " " + cleanUpName(element.name) + " = null;");
      }
    }
    write("");
  }

  private boolean isInnerType(AsnElementType element) {
    return (element.typeReference instanceof AsnConstructedType);
  }

  private void writeGetterAndSetter(List<AsnElementType> componentTypes) throws IOException {
    for (AsnElementType element : componentTypes) {
      String typeName = getClassName(element);
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
    write(
        "public List<"
            + referencedTypeName
            + "> get"
            + referencedTypeName.substring(referencedTypeName.lastIndexOf('.') + 1)
            + "() {");
    write("if (seqOf == null) {");
    write("seqOf = new ArrayList<" + referencedTypeName + ">();");
    write("}");
    write("return seqOf;");
    write("}\n");
  }

  private String getClassNameOfSequenceOfElement(
      AsnElementType componentType, List<String> listOfSubClassNames) {
    String classNameOfSequenceElement = getClassNameOfSequenceOfElement(componentType);
    for (String subClassName : listOfSubClassNames) {
      if (classNameOfSequenceElement.equals(subClassName)) {
        String moduleName = module.moduleIdentifier.name;

        for (SymbolsFromModule symbols : this.module.importSymbolFromModuleList) {
          if (symbols.symbolList.contains(classNameOfSequenceElement)) {
            moduleName = symbols.modref;
            break;
          }
        }

        return basePackageName + moduleToPackageName(moduleName) + "." + classNameOfSequenceElement;
      }
    }
    return classNameOfSequenceElement;
  }

  private String getClassNameOfSequenceOfElement(AsnElementType componentType) {
    if (componentType.typeReference == null) {
      return cleanUpName(componentType.definedType.typeName);
    } else {
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
        } else {
          subClassName = "SET";
        }

      } else if (typeDefinition instanceof AsnSequenceOf) {
        if (((AsnSequenceOf) typeDefinition).isSequenceOf) {
          subClassName = "SEQUENCEOF";
        } else {
          subClassName = "SETOF";
        }

      } else {
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
      } else if (asnCharacterString.stringtype.equals("T61String")) {
        return "BerTeletexString";
      }
      return "Ber" + ((AsnCharacterString) asnType).stringtype;
    }
    return "Ber" + className.substring(3);
  }

  private String[] getConstructorParameters(AsnUniversalType typeDefinition) {

    if (typeDefinition instanceof AsnInteger || typeDefinition instanceof AsnEnum) {
      return new String[] {"BigInteger", "value"};
    } else if (typeDefinition instanceof AsnReal) {
      return new String[] {"double", "value"};
    } else if (typeDefinition instanceof AsnBoolean) {
      return new String[] {"boolean", "value"};
    } else if (typeDefinition instanceof AsnObjectIdentifier) {
      return new String[] {"int[]", "value"};
    } else if (typeDefinition instanceof AsnBitString) {
      return new String[] {"byte[]", "value", "int", "numBits"};
    } else if (typeDefinition instanceof AsnOctetString
        || typeDefinition instanceof AsnCharacterString) {
      return new String[] {"byte[]", "value"};
    } else if (typeDefinition instanceof AsnNull) {
      return new String[0];
    } else if ((typeDefinition instanceof AsnSequenceSet)
        || (typeDefinition instanceof AsnChoice)) {
      return getConstructorParametersFromConstructedElement((AsnConstructedType) typeDefinition);
    } else if (typeDefinition instanceof AsnSequenceOf) {
      return new String[] {
        "List<"
            + getClassNameOfSequenceOfElement(((AsnSequenceOf) typeDefinition).componentType)
            + ">",
        "seqOf"
      };
    } else if (typeDefinition instanceof AsnAny) {
      return new String[] {"byte[]", "value"};
    } else if (typeDefinition instanceof AsnEmbeddedPdv) {
      return new String[0];
    } else {
      throw new IllegalStateException("type of unknown class: " + typeDefinition.name);
    }
  }

  private String[] getConstructorParametersFromConstructedElement(
      AsnConstructedType assignedTypeDefinition) {

    List<AsnElementType> componentTypes;

    if (assignedTypeDefinition instanceof AsnSequenceSet) {

      componentTypes = ((AsnSequenceSet) assignedTypeDefinition).componentTypes;
    } else {
      componentTypes = ((AsnChoice) assignedTypeDefinition).componentTypes;
    }

    String[] constructorParameters = new String[componentTypes.size() * 2];

    for (int j = 0; j < componentTypes.size(); j++) {
      AsnElementType componentType = componentTypes.get(j);

      constructorParameters[j * 2] = getClassName(componentType);
      constructorParameters[j * 2 + 1] = cleanUpName(componentType.name);
    }
    return constructorParameters;
  }

  private AsnType followAndGetNextTaggedOrUniversalType(AsnType asnType, AsnModule module)
      throws CompileException {
    return followAndGetNextTaggedOrUniversalType(asnType, module, true);
  }

  private AsnType followAndGetNextTaggedOrUniversalType(
      AsnType asnType, AsnModule module, boolean firstCall) throws CompileException {
    if (asnType instanceof AsnTaggedType) {
      if (!firstCall) {
        return asnType;
      }
      AsnTaggedType taggedType = (AsnTaggedType) asnType;
      if (taggedType.definedType != null) {
        return followAndGetNextTaggedOrUniversalType(taggedType.definedType, module, false);
      } else {
        return taggedType.typeReference;
      }
    } else if (asnType instanceof AsnDefinedType) {

      AsnDefinedType definedType = (AsnDefinedType) asnType;

      if (definedType.isObjectClassField) {

        AsnInformationObjectClass informationObjectClass =
            getInformationObjectClass(definedType.moduleOrObjectClassReference, module);
        if (informationObjectClass == null) {
          throw new CompileException(
              "no information object class of name \""
                  + definedType.moduleOrObjectClassReference
                  + "\" found");
        }

        for (AsnElementType elementType : informationObjectClass.elementList) {
          if (elementType.name.equals(definedType.typeName)) {
            return followAndGetNextTaggedOrUniversalType(elementType, module, true);
          }
        }

        throw new IllegalStateException(
            "Could not find field \""
                + definedType.typeName
                + "\" of information object class \""
                + definedType.moduleOrObjectClassReference
                + "\"");
      } else {
        return followAndGetNextTaggedOrUniversalType(definedType.typeName, module);
      }
    } else if (asnType instanceof AsnUniversalType) {
      return asnType;
    } else {
      throw new IllegalStateException();
    }
  }

  private AsnType followAndGetNextTaggedOrUniversalType(String typeName, AsnModule module)
      throws CompileException {

    AsnType asnType = module.typesByName.get(typeName);
    if (asnType != null) {
      return followAndGetNextTaggedOrUniversalType(asnType, module, false);
    }
    for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
      for (String importedTypeName : symbolsFromModule.symbolList) {
        if (typeName.equals(importedTypeName)) {
          return followAndGetNextTaggedOrUniversalType(
              typeName, getAsnModule(symbolsFromModule.modref));
        }
      }
    }
    throw new IllegalStateException(
        "Type definition \""
            + typeName
            + "\" was not found in module \""
            + module.moduleIdentifier.name
            + "\"");
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

  private AsnUniversalType getUniversalType(AsnType asnType) {
    return getUniversalType(asnType, module);
  }

  private AsnUniversalType getUniversalType(AsnType asnType, AsnModule module) {
    while (true) {
      if (!((asnType = followAndGetNextTaggedOrUniversalType(asnType, module))
          instanceof AsnTaggedType)) {
        break;
      }
    }
    return (AsnUniversalType) asnType;
  }

  private boolean isPrimitive(AsnTaggedType asnTaggedType) {
    AsnType asnType = asnTaggedType;
    while ((asnType = followAndGetNextTaggedOrUniversalType(asnType, module))
        instanceof AsnTaggedType) {
      if (isExplicit(getTag((AsnTaggedType) asnType))) {
        return false;
      }
    }
    return isPrimitive((AsnUniversalType) asnType);
  }

  private boolean isPrimitiveOrRetaggedPrimitive(AsnType asnType) {
    return isPrimitive(getUniversalType(asnType));
  }

  private boolean isPrimitive(AsnUniversalType asnType) {
    return !(asnType instanceof AsnConstructedType || asnType instanceof AsnEmbeddedPdv);
  }

  private void writeClassHeader(String typeName, AsnModule module) throws IOException {

    //noinspection ResultOfMethodCallIgnored
    outputDirectory.mkdirs();

    Writer fileWriter =
        Files.newBufferedWriter(new File(outputDirectory, typeName + ".java").toPath(), UTF_8);
    out = new BufferedWriter(fileWriter);

    String versionString = "";
    if (insertVersion) {
      versionString = " v" + Compiler.VERSION;
    }

    write("/*");
    write(
        " * This class file was automatically generated by ASN1bean"
            + versionString
            + " (http://www.beanit.com)\n */\n");
    write("package " + basePackageName + moduleToPackageName(module.moduleIdentifier.name) + ";\n");

    write("import java.io.IOException;");
    write("import java.io.EOFException;");
    write("import java.io.InputStream;");
    write("import java.io.OutputStream;");
    write("import java.util.List;");
    write("import java.util.ArrayList;");
    write("import java.util.Iterator;");
    write("import java.io.UnsupportedEncodingException;");
    write("import java.math.BigInteger;");
    write("import java.io.Serializable;");

    write("import com.beanit.asn1bean.ber.*;");
    write("import com.beanit.asn1bean.ber.types.*;");
    write("import com.beanit.asn1bean.ber.types.string.*;\n");

    List<String> importedClassesFromOtherModules = new ArrayList<>();

    for (SymbolsFromModule symbolsFromModule : module.importSymbolFromModuleList) {
      AsnModule importedModule = modulesByName.get(symbolsFromModule.modref);
      for (String importedSymbol : symbolsFromModule.symbolList) {
        if (Character.isUpperCase(importedSymbol.charAt(0))) {
          if (importedModule.typesByName.get(importedSymbol) != null) {
            importedClassesFromOtherModules.add(
                moduleToPackageName(importedModule.moduleIdentifier.name)
                    + "."
                    + cleanUpName(importedSymbol)
                    + ";");
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

  public enum TagClass {
    UNIVERSAL,
    APPLICATION,
    CONTEXT,
    PRIVATE
  }

  public enum TagType {
    EXPLICIT,
    IMPLICIT
  }

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
}
