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

#tomcat_install $TOMCAT_PACKAGE
MTWILSON_HOME=${MTWILSON_HOME:-"/opt/mtwilson"}
tomcat_detect
if tomcat_running; then tomcat_stop; fi
tomcatExistingInstallation=false
tomcatExistingKeystoreFile=
tomcatExistingKeystorePassword=
if [ -n "$TOMCAT_HOME" ] && [[ "$TOMCAT_HOME" != ${MTWILSON_HOME}* ]]; then
  echo_warning "Existing tomcat installation detected"
  tomcatExistingInstallation=true
  OIFS=$IFS
  IFS=$' \t\n' tomcatWebapps=(mtwilson mtwilson-portal AttestationService HisPrivacyCAWebServices2 ManagementService WLMService)
  IFS=$OIFS
  for i in "${tomcatWebapps[@]}"; do
    warFile="$TOMCAT_HOME/webapps/${i}.war"
    if [ -f "$warFile" ]; then
      if [ ! -w "$warFile" ]; then
        echo_failure "Current user does not have permission to remove ${i} from previous tomcat installation"
        exit 1
      fi
      echo "Removing ${i} from previous tomcat installation..."
      webservice_uninstall "${i}"
    fi
  done
  if ! tomcat_no_additional_webapps_exist_wait; then   #additional apps exist
    WEBSERVER_PORT_EVAL=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $3}' | awk -F: '{print $2}'`
    WEBSERVER_PORT=${WEBSERVER_PORT:-${WEBSERVER_PORT_EVAL:-"8443"}}
    OIFS=$IFS
    IFS=$' \t\n' read -ra tomcatPorts <<< $(cat "$TOMCAT_CONF/server.xml" | grep "port=" | sed 's/.*port=//' | awk '{print $1}' | sed 's/"//g')
    IFS=$OIFS
    if [[ " ${tomcatPorts[*]} " == *" $WEBSERVER_PORT "* ]]; then
      echo_failure "Existing tomcat installation running on configured port"
      echo_failure "Remove old installation or choose a different port"
      exit 1
    fi
  fi
  tomcatExistingKeystoreFile=$(cat "$TOMCAT_CONF/server.xml" | grep "keystoreFile=" | sed 's/.*keystoreFile=//' | awk '{print $1}' | sed 's/"//g')
  tomcatExistingKeystorePassword=$(cat "$TOMCAT_CONF/server.xml" | grep "keystorePass=" | sed 's/.*keystorePass=//' | awk '{print $1}' | sed 's/"//g')
fi

if [ -z "$TOMCAT_HOME" ] || [ -z "$tomcat" ] || [[ "$TOMCAT_HOME" != ${MTWILSON_HOME}* ]]; then
  if [[ -n "$TOMCAT_PACKAGE" && -f "$TOMCAT_PACKAGE" ]]; then
    gunzip -c $TOMCAT_PACKAGE | tar xf - 2>&1  >/dev/null
    tomcat_folder=`echo $TOMCAT_PACKAGE | awk -F .tar.gz '{ print $1 }'`
    tomcat_folder_path="${MTWILSON_HOME}/share/$tomcat_folder"
    if [ -d "$tomcat_folder" ]; then
      if [ -d "$tomcat_folder_path" ]; then
        echo "Tomcat already installed at $tomcat_folder_path"
        export TOMCAT_HOME="$tomcat_folder_path"
      else
        echo "Installing tomcat in $tomcat_folder_path..."
        mkdir -p "${MTWILSON_HOME}/share"
        mv $tomcat_folder "${MTWILSON_HOME}/share" && export TOMCAT_HOME="$tomcat_folder_path"
      fi
    fi
    tomcat_detect
  else
    TOMCAT_YUM_PACKAGES="tomcat7"
    TOMCAT_APT_PACKAGES="tomcat7"
    auto_install "Tomcat via package manager" "TOMCAT"
    tomcat_detect
  fi
fi

tomcat_detect
if [[ -z "$TOMCAT_HOME" || -z "$tomcat" ]]; then
  echo "Unable to auto-install Tomcat"
  echo "  Tomcat download URL:"
  echo "  http://tomcat.apache.org/"
fi

if $tomcatExistingInstallation; then
  tomcatServerXml="$TOMCAT_CONF/server.xml"
  tomcatKeystoreFile="$TOMCAT_HOME/ssl/.keystore"
  tomcatKeystorePassword=${tomcatExistingKeystorePassword:-"changeit"}
  if [ -n "$tomcatExistingKeystoreFile" ] && [ -f "$tomcatExistingKeystoreFile" ]; then
    echo "Copying existing tomcat keystore and saving keystore location in server.xml..."
    mkdir -p "$TOMCAT_HOME/ssl"
    cp "$tomcatExistingKeystoreFile" "$tomcatKeystoreFile"
    sed -i.bak 's|sslProtocol=\"TLS\" />|sslEnabledProtocols=\"TLSv1.2\" keystoreFile=\"'"$tomcatKeystoreFile"'\" keystorePass=\"'"$tomcatKeystorePassword"'\" />|g' "$tomcatServerXml"
    #sed -i 's/keystoreFile=.*\b/keystoreFile=\"'"$tomcatKeystoreFile"'/g' "$tomcatServerXml"
    perl -p -i -e 's/keystoreFile=.*?\s/keystoreFile=\"'"$(sed_escape $tomcatKeystoreFile)"'\" /g' "$tomcatServerXml"
  fi
  if [ -n "$tomcatExistingKeystorePassword" ]; then
    echo "Saving existing tomcat keystore password in server.xml..."
    sed -i.bak 's|sslProtocol=\"TLS\" />|sslEnabledProtocols=\"TLSv1.2\" keystoreFile=\"'"$tomcatKeystoreFile"'\" keystorePass=\"'"$tomcatKeystorePassword"'\" />|g' "$tomcatServerXml"
    sed -i 's/keystorePass=.*\b/keystorePass=\"'"$tomcatKeystorePassword"'/g' "$tomcatServerXml"
  fi
fi

# the Tomcat "endorsed" folder is not present by default, we have to create it.
if [ ! -d ${TOMCAT_HOME}/endorsed ]; then
 mkdir -p ${TOMCAT_HOME}/endorsed
fi
cp *.jar ${TOMCAT_HOME}/endorsed/
cp setenv.sh ${TOMCAT_HOME}/bin/
chmod +x $TOMCAT_HOME/bin/setenv.sh

# remove jackson jars
rm -f "${TOMCAT_HOME}"/endorsed/jackson-* 2>/dev/null

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
mysqlconnector_files=`ls -1 ${MTWILSON_HOME}/java/* | grep -i mysql`
if [[ -n "$mysqlconnector_files" ]]; then
  rm -f "${TOMCAT_HOME}"/endorsed/mtwilson-mysql-* 2>/dev/null
  cp $mysqlconnector_files ${TOMCAT_HOME}/endorsed/
fi

echo "Adding manager roles to tomcat admin account in tomcat-users.xml..."
cd $TOMCAT_CONF
userExists=`grep "username=\"$WEBSERVICE_MANAGER_USERNAME\"" tomcat-users.xml`
mv tomcat-users.xml tomcat-users.xml.old
if [ -z "$userExists" ]; then
  sed 's/<\/tomcat-users>/\n  <role rolename="manager-gui"\/>\n  <role rolename="manager"\/>\n  <user username="'$WEBSERVICE_MANAGER_USERNAME'" password="'$WEBSERVICE_MANAGER_PASSWORD'" roles="manager,manager-gui,manager-script"\/>\n<\/tomcat-users>/g' tomcat-users.xml.old > tomcat-users.xml
else
  sed -i 's/.*username="'$WEBSERVICE_MANAGER_USERNAME'".*/  <user username="'$WEBSERVICE_MANAGER_USERNAME'" password="'$WEBSERVICE_MANAGER_PASSWORD'" roles="manager,manager-gui,manager-script"\/>/g' tomcat-users.xml.old
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

# moved to tomcat_create_ssl_cert method
#sed -i.bak 's/sslProtocol=\"TLS\" \/>/sslEnabledProtocols=\"TLSv1,TLSv1.1,TLSv1.2\" keystoreFile=\"\/usr\/share\/apache-tomcat-7.0.34\/ssl\/.keystore\" keystorePass=\"'"$MTW_TLS_KEYSTORE_PASS"'\" \/>/g' server.xml

# alternative is to use xsltproc:  xsltproc -o server.xml tomcat-https.xsl server.xml.bak

xmlstarlet ed --insert '/Server/Service/Connector[@SSLEnabled="true"][@protocol="HTTP/1.1"][not(@ciphers)]' --type attr -n ciphers -v 'TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA' server.xml > server_temp.xml
mv server_temp.xml server.xml

tomcat_permissions ${TOMCAT_HOME}
rm -rf "${TOMCAT_HOME}/webapps/docs" "${TOMCAT_HOME}/webapps/examples" "${TOMCAT_HOME}/webapps/ROOT"