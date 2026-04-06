apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")
apply(plugin = "eclipse")
apply(plugin = "com.diffplug.spotless")


project.extra["cfgModuleName"] = "com.beanit.asn1bean"

mavenPublishing {

    coordinates(
        project.group.toString(),
        project.name,
        project.version.toString()
    )

    pom {
        name.set("ASN1bean")
        description.set("ASN1bean is a library used for encoding and decoding ASN.1 BER messages.")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}
