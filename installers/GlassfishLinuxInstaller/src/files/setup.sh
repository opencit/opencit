#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

#glassfish_required_version=4.0
#java_required_version=1.7.0_51

# detect the packages we have to install
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
if no_java ${JAVA_REQUIRED_VERSION:-1.7}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-1.7} or later"; exit 1; fi

#glassfish_install $GLASSFISH_PACKAGE
MTWILSON_HOME=${MTWILSON_HOME:-"/opt/mtwilson"}
MTWILSON_CONFIGURATION=${MTWILSON_CONFIGURATION:-"$MTWILSON_HOME/configuration"}
mtwilsonPropertiesFile="${MTWILSON_CONFIGURATION}/mtwilson.properties"
glassfish_detect
if glassfish_running; then glassfish_stop; fi
glassfishExistingInstallation=false
glassfishExistingKeystoreFile=
glassfishExistingKeystorePassword=
if [ -n "$GLASSFISH_HOME" ] && [[ "$GLASSFISH_HOME" != ${MTWILSON_HOME}* ]]; then
  echo_warning "Existing glassfish installation detected"
  glassfishExistingInstallation=true
  OIFS=$IFS
  IFS=$' \t\n' glassfishWebapps=(mtwilson mtwilson-portal AttestationService HisPrivacyCAWebServices2 ManagementService WLMService)
  IFS=$OIFS
  for i in "${glassfishWebapps[@]}"; do
    warFile=$(find "$GLASSFISH_HOME" -name "${i}.war")
    if [ -f "$warFile" ]; then
      if [ ! -w "$warFile" ]; then
        echo_failure "Current user does not have permission to remove ${i} from previous glassfish installation"
        exit 1
      fi
      echo "Removing ${i} from previous glassfish installation..."
      webservice_uninstall "${i}"
    fi
  done
  if ! glassfish_no_additional_webapps_exist_wait; then   #additional apps exist
    WEBSERVER_PORT_EVAL=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $3}' | awk -F: '{print $2}'`
    WEBSERVER_PORT=${WEBSERVER_PORT:-${WEBSERVER_PORT_EVAL:-"8181"}}
    glassfishDomainXmlFile=$(find "$GLASSFISH_HOME" -name domain.xml | head -1)
    OIFS=$IFS
    IFS=$' \t\n' read -ra glassfishPorts <<< $(cat "$glassfishDomainXmlFile" | grep "<network-listener" | grep "port=" | perl -p -e 's/.*?port=//' | awk '{print $1}' | sed 's/"//g')
    IFS=$OIFS
    if [[ " ${glassfishPorts[*]} " == *" $WEBSERVER_PORT "* ]]; then
      echo_failure "Existing glassfish installation running on configured port"
      echo_failure "Remove old installation or choose a different port"
      exit 1
    fi
  fi
  glassfishExistingKeystoreFile=$(find "$GLASSFISH_HOME" -name keystore.jks | head -1)
  glassfishExistingCacertsFile=$(find "$GLASSFISH_HOME" -name cacerts.jks | head -1)
  glassfishExistingKeystorePassword=$(read_property_from_file "mtwilson.tls.keystore.password" "${mtwilsonPropertiesFile}")
fi

if [ -z "$GLASSFISH_HOME" ] || [ -z "$glassfish" ] || [[ "$GLASSFISH_HOME" != ${MTWILSON_HOME}* ]]; then
  if [[ -z "$GLASSFISH_PACKAGE" || ! -f "$GLASSFISH_PACKAGE" ]]; then
    echo_failure "Missing Glassfish installer: $GLASSFISH_PACKAGE"
    exit 1
  fi
  echo "Installing $GLASSFISH_PACKAGE"
  unzip $GLASSFISH_PACKAGE 2>&1  >/dev/null
  mkdir -p "$MTWILSON_HOME/share"
  mv glassfish4 "${MTWILSON_HOME}/share" && export GLASSFISH_HOME="${MTWILSON_HOME}/share/glassfish4/glassfish"
  # Glassfish requires hostname to be mapped to 127.0.0.1 in /etc/hosts
  if [ -f "/etc/hosts" ]; then
    hostname=`hostname`
    found=`cat "/etc/hosts" | grep "^127.0.0.1" | grep "$hostname"`
    if [ -z "$found" ]; then
      datestr=`date +%Y-%m-%d.%H%M`
      cp /etc/hosts /etc/hosts.${datestr}
      updated=`sed -re "s/^(127.0.0.1\s.*)$/\1 ${hostname}/" /etc/hosts`
      echo "$updated" > /etc/hosts
    fi
  fi
  glassfish_detect
  if [[ -z "$GLASSFISH_HOME" || -z "$glassfish" ]]; then
    echo_failure "Unable to auto-install Glassfish"
    echo "Glassfish download URL:"
    echo "http://glassfish.java.net/"
    exit 1
  fi
else
  echo "Glassfish is already installed in $GLASSFISH_HOME"
fi
glassfish_detect
glassfish_permissions "${GLASSFISH_HOME}"
sleep 5
glassfish_start
glassfish_memory 2048 512
glassfish_logback

# set JAVA_HOME for glassfish
asenvFile=`find "$GLASSFISH_HOME" -name asenv.conf`
if [ -n "$asenvFile" ]; then
  if [ -f "$asenvFile" ] && ! grep -q "AS_JAVA=" "$asenvFile"; then
    echo "AS_JAVA=$JAVA_HOME" >> "$asenvFile"
  fi
else
  echo "warning: asenv.conf not found" >> $INSTALL_LOG_FILE
fi
echo "Increasing glassfish max thread pool size to 200..."
$glassfish set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200

if $glassfishExistingInstallation; then
  domain_found=$($glassfish list-domains | head -n 1 | awk '{ print $1 }')
  GF_CONFIG_PATH="$GLASSFISH_HOME/domains/${domain_found}/config"
  glassfishKeystoreFile="$GF_CONFIG_PATH/keystore.jks"
  glassfishCacertsFile="$GF_CONFIG_PATH/cacerts.jks"
  keytool="${JAVA_HOME}/bin/keytool"
  
  glassfishKeystorePasswordOld=$(read_property_from_file "mtwilson.tls.keystore.password" "${mtwilsonPropertiesFile}")
  glassfishKeystorePasswordOld=${glassfishKeystorePasswordOld:-"changeit"}

  has_incorrect_password=$($keytool -list -v -alias s1as -keystore "$glassfishKeystoreFile" -storepass "$glassfishKeystorePasswordOld" 2>&1 | grep "password was incorrect")
  if [ -n "$has_incorrect_password" ]; then
    glassfishKeystorePasswordOld="changeit"
    has_incorrect_password=$($keytool -list -v -alias s1as -keystore "$glassfishKeystoreFile" -storepass "$glassfishKeystorePasswordOld" 2>&1 | grep "password was incorrect")
    if [ -n "$has_incorrect_password" ]; then
      echo_failure "Current SSL keystore password is incorrect"
      exit -1
    fi
  fi
  if [ -n "$glassfishExistingKeystorePassword" ] && [ "$glassfishKeystorePasswordOld" != "$glassfishExistingKeystorePassword" ]; then
    echo "Saving existing glassfish keystore password for new glassfish installation..."
    glassfish_stop >/dev/null
    glassfishMaster=$(echo "$glassfish" | sed -e 's/--user=.*\b//g' | sed -e 's/--passwordfile=.*\b//g')
    mv "${GF_CONFIG_PATH}/domain-passwords" "${GF_CONFIG_PATH}/domain-passwords_bkup" 2>/dev/null
    touch "${GF_CONFIG_PATH}/master.passwd"
    echo "AS_ADMIN_MASTERPASSWORD=$glassfishKeystorePasswordOld" > "${GF_CONFIG_PATH}/master.passwd"
    echo "AS_ADMIN_NEWMASTERPASSWORD=$glassfishExistingKeystorePassword" >> "${GF_CONFIG_PATH}/master.passwd"
    $glassfishMaster change-master-password --savemasterpassword=true --passwordfile="${GF_CONFIG_PATH}/master.passwd" "${domain_found}"
    rm -f "${GF_CONFIG_PATH}/master.passwd"
    glassfish_start >/dev/null
  fi
  if [ -n "$glassfishExistingKeystoreFile" ] && [ -f "$glassfishExistingKeystoreFile" ]; then
    echo "Copying existing glassfish keystore file to new glassfish installation location..."
    rm -f "$glassfishKeystoreFile"
    cp "$glassfishExistingKeystoreFile" "$glassfishKeystoreFile"
  fi
  if [ -n "$glassfishExistingCacertsFile" ] && [ -f "$glassfishExistingCacertsFile" ]; then
    echo "Copying existing glassfish cacerts file to new glassfish installation location..."
    rm -f "$glassfishCacertsFile"
    cp "$glassfishExistingCacertsFile" "$glassfishCacertsFile"
  fi
  glassfish_restart 2>&1 >/dev/null
fi

#cp jackson-core-asl.jar ${GLASSFISH_HOME}/modules/
#cp jackson-mapper-asl.jar ${GLASSFISH_HOME}/modules/
#cp jackson-xc.jar ${GLASSFISH_HOME}/modules/
cp jackson-annotations.jar ${GLASSFISH_HOME}/modules/
cp jackson-core.jar ${GLASSFISH_HOME}/modules/
cp jackson-databind.jar ${GLASSFISH_HOME}/modules/

# on installations configured to use mysql, the customer is responsible for 
# providing the java mysql connector before starting the mt wilson installer.
# due to its GPLv2 license we cannot integrate it in any way with what we
# distribute so it cannot be even considered that our product is "based on"
# or is a "derivative work" of mysql.
# here is what the customer is supposed to execute before installing mt wilson:
# # mkdir -p /opt/intel/cloudsecurity/setup-console
# # cp mysql-connector-java-5.1.x.jar /opt/intel/cloudsecurity/setup-console
# so now we check to see if it's there, and copy it to glassfish so the apps
# can use it:
mysqlconnector_files=`ls -1 /opt/intel/cloudsecurity/setup-console/* 2>/dev/null | grep -i mysql`
if [[ -n "$mysqlconnector_files" ]]; then
  cp $mysqlconnector_files ${GLASSFISH_HOME}/modules/
fi

cp *.jar ${GLASSFISH_HOME}/modules/

#glassfish_stop

# moved to glassfish_create_ssl_cert method
##change glassfish master password which is the keystore password
#GF_CONFIG_PATH="${GLASSFISH_HOME}/glassfish/domains/domain1/config"
#mv "${GF_CONFIG_PATH}/domain-passwords" "${GF_CONFIG_PATH}/domain-passwords_bkup"
#touch "${GF_CONFIG_PATH}/master.passwd"
#echo "AS_ADMIN_MASTERPASSWORD=changeit" > "${GF_CONFIG_PATH}/master.passwd"
#echo "AS_ADMIN_NEWMASTERPASSWORD=$MTW_TLS_KEYSTORE_PASS" >> "${GF_CONFIG_PATH}/master.passwd"
#$glassfish change-master-password --savemasterpassword=true --passwordfile="${GF_CONFIG_PATH}/master.passwd" domain1
#rm "${GF_CONFIG_PATH}/master.passwd"

#glassfish_start

echo
