rootProject.name = "jasn1"

include("jasn1-compiler")
include("jasn1")

project(":jasn1-compiler").projectDir = file("projects/jasn1-compiler")
project(":jasn1").projectDir = file("projects/jasn1")
