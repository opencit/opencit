#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export INSTALL_LOG_FILE=/tmp/mtwilson-install.log

#java_required_version=1.6.0_29

# detect the packages we have to install
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
chmod +x MtWilsonLinuxUtil.bin
./MtWilsonLinuxUtil.bin
if [ -f /usr/share/mtwilson/script/functions ]; then . /usr/share/mtwilson/script/functions; else echo "Missing file: /usr/share/mtwilson/script/functions"; exit 1; fi

# SCRIPT EXECUTION
mtwilson java-install-file $JAVA_PACKAGE

if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

if [ -f "/etc/environment" ] && [ -n ${JAVA_HOME} ]; then
  echo "JAVA_HOME=${JAVA_HOME}" >> /etc/environment
fi
