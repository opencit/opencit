#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# detect the packages we have to install
TOMCAT_PACKAGE=`ls -1 apache-tomcat*.tar.gz 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
tomcat_install $TOMCAT_PACKAGE

echo "Setting up Tomcat in $TOMCAT_HOME..."

# the Tomcat "endorsed" folder is not present by default, we have to create it.
if [ ! -d ${TOMCAT_HOME}/endorsed ]; then
 mkdir -p ${TOMCAT_HOME}/endorsed
fi
cp *.jar ${TOMCAT_HOME}/endorsed/
cp setenv.sh ${TOMCAT_HOME}/bin/
chmod +x $TOMCAT_HOME/bin/setenv.sh
#Create SSL cert ###NOW DONE IN tomcat_install
#tomcat_create_ssl_cert $MTWILSON_SERVER


# on installations configured to use mysql, the customer is responsible for 
# providing the java mysql connector before starting the mt wilson installer.
# due to its GPLv2 license we cannot integrate it in any way with what we
# distribute so it cannot be even considered that our product is "based on"
# or is a "derivative work" of mysql.
# here is what the customer is supposed to execute before installing mt wilson:
# # mkdir -p /opt/intel/cloudsecurity/setup-console
# # cp mysql-connector-java-5.1.x.jar /opt/intel/cloudsecurity/setup-console
# so now we check to see if it's there, and copy it to TOMCAT so the apps
# can use it:
mysqlconnector_files=`ls -1 /opt/mtwilson/java/* | grep -i mysql`
if [[ -n "$mysqlconnector_files" ]]; then
  cp $mysqlconnector_files ${TOMCAT_HOME}/endorsed/
fi

# Add the manager role give access to the tomcat user in the tomcat-users.xml
echo "Editing tomcat-users.xml in $TOMCAT_CONF..."

cd $TOMCAT_CONF
userExists=`grep "username=\"$WEBSERVICE_USERNAME\"" tomcat-users.xml`
mv tomcat-users.xml tomcat-users.xml.old
if [ -z "$userExists" ]; then
  sed 's/<\/tomcat-users>/\n  <role rolename="manager-gui"\/>\n  <role rolename="manager"\/>\n  <user username="'$WEBSERVICE_USERNAME'" password="'$WEBSERVICE_PASSWORD'" roles="manager,manager-gui,manager-script"\/>\n<\/tomcat-users>/g' tomcat-users.xml.old > tomcat-users.xml
else
  sed -i 's/.*username="'$WEBSERVICE_USERNAME'".*/  <user username="'$WEBSERVICE_USERNAME'" password="'$WEBSERVICE_PASSWORD'" roles="manager,manager-gui,manager-script"\/>/g' tomcat-users.xml.old
  cp tomcat-users.xml.old tomcat-users.xml
fi
rm  -f tomcat-users.xml.old

# Here is what the connector string should look like
#<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
#               maxThreads="150" scheme="https" secure="true"
#               clientAuth="false" sslProtocol="TLS"
#               keystoreFile="/usr/share/apache-tomcat-7.0.34/ssl/keystore.jks" keystorePass="changeit" />
# release the connectors!
echo "Editing server.xml in $TOMCAT_CONF..."

cd $TOMCAT_CONF
cat server.xml | sed '{/<!--*/ {N; /<Connector port=\"8080\"/ {D; }}}' | sed '{/-->/ {N; /<!-- A \"Connector\" using the shared thread pool-->/ {D; }}}' | sed '{/<!--*/ {N; /<Connector port=\"8443\"/ {D; }}}' | sed '{/-->/ {N;N; /<!-- Define an AJP 1.3 Connector on port 8009 -->/ {D; }}}' > server_temp.xml
mv server_temp.xml server.xml

#sed -i.bak 's/sslProtocol=\"TLS\" \/>/sslEnabledProtocols=\"TLSv1,TLSv1.1,TLSv1.2\" keystoreFile=\"\/usr\/share\/apache-tomcat-7.0.34\/ssl\/.keystore\" keystorePass=\"'"${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"'\" \/>/g' server.xml
sed -i.bak 's/sslProtocol=\"TLS\" \/>/sslEnabledProtocols=\"TLSv1,TLSv1.1,TLSv1.2\" keystoreFile=\"\/opt\/mtwilson\/configuration\/mtwilson-tls.jks\" keystorePass=\"'"${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"'\" \/>/g' server.xml
# alternative is to use xsltproc:  xsltproc -o server.xml tomcat-https.xsl server.xml.bak

tomcat_permissions ${TOMCAT_HOME}
