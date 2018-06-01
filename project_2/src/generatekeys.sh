#!/bin/bash

# Generate client public/private key pair into private keystore
echo Generating tracker public private key pair
keytool -genkey -alias trackerprivate -keystore tracker.private -storetype JKS -keyalg rsa -dname "CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country" -storepass trackerpw -keypass trackerpw

# Export client public key and import it into public keystore
echo Generating tracker public key file
keytool -export -alias trackerprivate -keystore tracker.private -file temp.key -storepass trackerpw
keytool -import -noprompt -alias trackerpublic -keystore tracker.public -file temp.key -storepass public
rm -f temp.key
