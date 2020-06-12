apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")
apply(plugin = "eclipse")
apply(plugin = "biz.aQute.bnd.builder")
apply(plugin = "com.diffplug.gradle.spotless")


project.extra["cfgModuleName"] = "com.beanit.asn1bean"

tasks["jar"].withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
    bnd("""
        Bundle-Name: ASN1bean
        Bundle-SymbolicName: ${project.extra["cfgModuleName"]}
        -exportcontents: !*.internal.*,*
    """)
}

publishing {
    publications {
        maybeCreate<MavenPublication>("mavenJava").pom {
            name.set("ASN1bean")
            description.set("ASN1bean is a library used for encoding and decoding ASN.1 BER messages.")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }
}
