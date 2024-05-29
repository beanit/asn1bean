# ASN1bean
ASN1bean (previously known as jASN1) is a Java ASN.1 BER and DER encoding/decoding library

For detailed information on ASN1bean visit https://www.beanit.com/asn1/.


## Using the generator

*Note*: Use Java 8!

If you have changed the grammar file [asn1.g](projects/asn1bean-compiler/antlr/asn1.g) you should first run:
```bash
cd projects/asn1bean-compiler/antlr/
./compile.sh
cd -
```

Build it and publish it:
```bash
./gradlew clean publishToMavenLocal -x signMavenJavaPublication
```