spotless {
    java {
        targetExclude("src/main/java-gen/**", "src/test/java-gen/**")
    }
}

dependencies {
    implementation("antlr:antlr:2.7.7")
    implementation(project(":asn1bean"))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "src/main/java-gen"))
        }
    }
    test {
        java {
            setSrcDirs(listOf("src/test/java", "src/test/java-gen"))
        }
    }
}

project.extra["cfgModuleName"] = "com.beanit.asn1bean.compiler"

mavenPublishing {

    coordinates(
        project.group.toString(),
        project.name,
        project.version.toString()
    )

    pom {
        name.set("ASN1bean Compiler")
        description.set("ASN1bean Compiler generates Java classes out of ASN.1 code that can be used to encode/decode BER data.")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}
