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

import com.beanit.asn1bean.compiler.cli.CliParameter;
import com.beanit.asn1bean.compiler.cli.CliParameterBuilder;
import com.beanit.asn1bean.compiler.cli.CliParseException;
import com.beanit.asn1bean.compiler.cli.CliParser;
import com.beanit.asn1bean.compiler.cli.FlagCliParameter;
import com.beanit.asn1bean.compiler.cli.StringCliParameter;
import com.beanit.asn1bean.compiler.cli.StringListCliParameter;
import com.beanit.asn1bean.compiler.model.AsnModel;
import com.beanit.asn1bean.compiler.model.AsnModule;
import com.beanit.asn1bean.compiler.parser.ASNLexer;
import com.beanit.asn1bean.compiler.parser.ASNParser;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Compiler {

  public static final String VERSION = "1.12.0";

  public static void main(String[] args) throws Exception {

    StringCliParameter outputBaseDir =
        new CliParameterBuilder("-o")
            .setDescription(
                "The base directory for the generated Java classes. Often this is the root folder for Java classes (e.g. src/main/java). The class files will be saved in subfolders of the base directory corresponding to the base package name and the name of the defined modules.")
            .buildStringParameter("output_base_dir", "./");

    StringCliParameter basePackageName =
        new CliParameterBuilder("-p")
            .setDescription(
                "The base package name. Added to this will be a name generated from the module name.")
            .buildStringParameter("package_base_name", "");

    FlagCliParameter disableWritingVersion =
        new CliParameterBuilder("-dv")
            .setDescription(
                "By default the ASN1bean version is written in the header of the generated classes. Using this flag this behavior can be disabled.")
            .buildFlagParameter();

    FlagCliParameter legacyMode =
        new CliParameterBuilder("-l")
            .setDescription(
                "Enable legacy mode. Earlier versions of the ASN1bean compiler generated classes that had public member variables instead of getters and setters. This flag enables the old kind of classes.")
            .buildFlagParameter();

    StringListCliParameter asn1Files =
        new CliParameterBuilder("-f")
            .setMandatory()
            .setDescription("ASN.1 files defining one or more modules.")
            .buildStringListParameter("file");

    List<CliParameter> cliParameters = new ArrayList<>();
    cliParameters.add(asn1Files);
    cliParameters.add(outputBaseDir);
    cliParameters.add(basePackageName);
    cliParameters.add(disableWritingVersion);
    cliParameters.add(legacyMode);

    CliParser cliParser =
        new CliParser(
            "asn1bean-compiler",
            "The compiler reads the ASN.1 definitions from the given files and generates corresponding Java classes that can be used to conveniently encode and decode BER data. v"
                + VERSION);
    cliParser.addParameters(cliParameters);

    try {
      cliParser.parseArguments(args);
    } catch (CliParseException e1) {
      System.err.println("Error parsing command line parameters: " + e1.getMessage());
      System.out.println(cliParser.getUsageString());
      System.exit(1);
    }

    System.out.println("Generated code will be saved in " + outputBaseDir.getValue());

    HashMap<String, AsnModule> modulesByName = new HashMap<>();

    for (String asn1File : asn1Files.getValue()) {
      System.out.println("Parsing \"" + asn1File + "\"");
      AsnModel model = getJavaModelFromAsn1File(asn1File);
      modulesByName.putAll(model.modulesByName);
    }

    BerClassWriter classWriter =
        new BerClassWriter(
            modulesByName,
            outputBaseDir.getValue(),
            basePackageName.getValue(),
            !legacyMode.isSelected(),
            disableWritingVersion.isSelected());

    classWriter.translate();
    System.out.println("done");
  }

  private static AsnModel getJavaModelFromAsn1File(String inputFileName) throws Exception {
    InputStream stream = new FileInputStream(inputFileName);
    ASNLexer lexer = new ASNLexer(stream);
    ASNParser parser = new ASNParser(lexer);

    AsnModel model = new AsnModel();
    parser.module_definitions(model);

    return model;
  }
}
