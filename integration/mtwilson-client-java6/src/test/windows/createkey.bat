SET COMPONENT="APIClientTest"
SET ORG="Intel"
keytool -genkeypair -keystore keystore.jks -keyalg RSA -sigalg SHA256withRSA -keysize 1024 -dname "CN=%COMPONENT%, OU=Mt Wilson, O=%ORG%, L=Folsom, ST=CA, C=US"  -alias mykey -validity 3650
REM can also do -ext for extensions... where we put in the requested roles...
REM keytool -exportcert -keystore keystore.jks -alias mykey -file mykey.crt
REM keytool -printcert -file mykey.crt
