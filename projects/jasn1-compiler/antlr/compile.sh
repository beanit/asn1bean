#!/bin/sh

DIR_NAME=`dirname $0`
cd $DIR_NAME/

rm ../src/main/java-gen/org/openmuc/jasn1/compiler/parser/*
java -cp antlr-2.7.7.jar antlr.Tool -o ../src/main/java-gen/org/openmuc/jasn1/compiler/parser/ asn1.g
