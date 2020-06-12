rootProject.name = "asn1bean"

include("asn1bean-compiler")
include("asn1bean")

project(":asn1bean-compiler").projectDir = file("projects/asn1bean-compiler")
project(":asn1bean").projectDir = file("projects/asn1bean")
