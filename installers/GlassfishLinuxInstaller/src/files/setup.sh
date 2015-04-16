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
glassfish_install $GLASSFISH_PACKAGE

#cp jackson-core-asl.jar ${GLASSFISH_HOME}/modules/
#cp jackson-mapper-asl.jar ${GLASSFISH_HOME}/modules/
#cp jackson-xc.jar ${GLASSFISH_HOME}/modules/
cp jackson-annotations.jar ${GLASSFISH_HOME}/glassfish/modules/
cp jackson-core.jar ${GLASSFISH_HOME}/glassfish/modules/
cp jackson-databind.jar ${GLASSFISH_HOME}/glassfish/modules/

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
  cp $mysqlconnector_files ${GLASSFISH_HOME}/glassfish/modules/
fi

cp *.jar ${GLASSFISH_HOME}/glassfish/modules/

glassfish_stop

#change glassfish master password which is the keystore password
GF_CONFIG_PATH="${GLASSFISH_HOME}/glassfish/domains/domain1/config"
mv "${GF_CONFIG_PATH}/domain-passwords" "${GF_CONFIG_PATH}/domain-passwords_bkup"
touch "${GF_CONFIG_PATH}/master.passwd"
echo "AS_ADMIN_MASTERPASSWORD=changeit" > "${GF_CONFIG_PATH}/master.passwd"
echo "AS_ADMIN_NEWMASTERPASSWORD=$MTW_TLS_KEYSTORE_PASS" >> "${GF_CONFIG_PATH}/master.passwd"
$glassfish change-master-password --savemasterpassword=true --passwordfile="${GF_CONFIG_PATH}/master.passwd" domain1
rm "${GF_CONFIG_PATH}/master.passwd"

glassfish_start

echo
