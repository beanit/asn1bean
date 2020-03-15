spotless {
    java {
        targetExclude("src/main/java-gen/**", "src/test/java-gen/**")
    }
}

dependencies {
    implementation("antlr:antlr:2.7.7")
    implementation(project(":jasn1"))
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

project.extra["cfgModuleName"] = "com.beanit.jasn1-compiler"

tasks["jar"].withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
    bnd("""
        Bundle-Name: jASN1 Compiler
        Bundle-SymbolicName: ${project.extra["cfgModuleName"]}
        -exportcontents: !*.internal.*,*
    """)
}

publishing {
    publications {
        maybeCreate<MavenPublication>("mavenJava").pom {
            name.set("jASN1 Compiler")
            description.set("jASN1 Compiler generates Java classes out of ASN.1 code that can be used to encode/decode BER data.")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }
}
