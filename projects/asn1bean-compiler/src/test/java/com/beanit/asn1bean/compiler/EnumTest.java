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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EnumTest {

  @TempDir Path outputDirectory;

  @Test
  public void generatesNamedEnumValues() throws Exception {
    Compiler.main(
        new String[] {
          "-o",
          outputDirectory.toString(),
          "-p",
          "com.beanit.asn1bean.compiler",
          "-f",
          "src/test/resources/enum-test.asn",
          "-dv"
        });

    Path generatedSource =
        outputDirectory.resolve("com/beanit/asn1bean/compiler/enum_test/DeviceTypeEnum.java");
    String source = Files.readString(generatedSource);

    assertTrue(
        source.contains(
            "public static final DeviceTypeEnum otherwiseSpecified = new DeviceTypeEnum(0L);"));
    assertTrue(
        source.contains(
            "public static final DeviceTypeEnum registrationTerminal = new DeviceTypeEnum(1L);"));
    assertTrue(
        source.contains(
            "public static final DeviceTypeEnum outsideLongRange = new DeviceTypeEnum(new BigInteger(\"9223372036854775808\"));"));
  }
}
