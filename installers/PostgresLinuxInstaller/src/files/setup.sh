#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# detect the packages we have to install
POSTGRES_PACKAGE=`ls -1 apache-tomcat*.tgz 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
#if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
chmod +x MtWilsonLinuxUtil.bin
./MtWilsonLinuxUtil.bin
if [ -f /usr/share/mtwilson/script/functions ]; then . /usr/share/mtwilson/script/functions; else echo "Missing file: /usr/share/mtwilson/script/functions"; exit 1; fi

# SCRIPT EXECUTION
if no_java ${JAVA_REQUIRED_VERSION:-1.6}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-1.6} or later"; exit 1; fi
tomcat_install $POSTGRES_PACKAGE


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
mysqlconnector_files=`ls -1 /opt/intel/cloudsecurity/setup-console/* | grep -i mysql`
if [[ -n "$mysqlconnector_files" ]]; then
  cp $mysqlconnector_files ${TOMCAT_HOME}/endorsed/
fi

# not sure if this is needed for tomcat... if it's installed before the web apps,
# maybe the new jars will be picked up for the new web apps that deploy
#tomcat_stop
#tomcat_start

echo
