# ASN1bean User Guide

* unnumbered toc
{:toc}



## Intro

ASN1bean can be used for ASN.1 BER/DER encoding/decoding using Java. It consists of two projects: asn1bean-compiler and the asn1bean library (both licensed under the Apache 2.0 license). The asn1bean-compiler is an application that creates Java classes from ASN.1 syntax. These generated classes can then be used together with the asn1bean library to efficiently encode and decode messages using the Basic Encoding Rules (BER). The encoded bytes also conform to the Distinguished Encoding Rules (DER) which is a subset of BER.

The generated Java classes may be put under a license of your choice. The software has been tested under Linux and Windows.

## Distribution

After extracting the distribution tar file the ASN1bean libraries can be found in the directory *build/libs-all*. For license information check the *license* directory in the distribution. Scripts to run the ASN.1 compiler are found in the *bin* directory.

### Dependencies

Besides the ASN1bean libraries the *build/libs-all/* directory contains the following external library:

ANTLR, License: The BSD License, http://www.antlr.org Copyright (c) 2012 Terence Parr and Sam Harwell All rights reserved.

The asn1bean-compiler uses ANTLR to parse the ASN.1 definitions. The grammar to generate the ANTLR parser was taken from the BinaryNotes ASN.1 Framework (http://bnotes.sourceforge.net/ licensed under the Apache 2.0 license).  Copyright 2006-2011 Abdulla Abdurakhmanov

## How to use ASN1bean

In a first step you have to compile the ASN.1 code of your choice to Java classes. To run the compiler you can use the run scripts for Linux and Windows found in the _bin_ folder. Executing the scripts without any parameters will print help information to the screen.

The generated classes can then be used to easily encode and decode BER/DER encoded data. For an example on how to use the generated classes see the folder _projects/asn1bean-compiler/src/test/java/com/beanit/asn1bean/sample/_.


## Modifying and Compiling ASN1bean

ASN1bean is built using the Gradle build automation tool. The distribution contains a fully functional gradle build file (_build.gradle.kts_). Thus if you changed code and want to rebuild a library you can do so easily with Gradle.
