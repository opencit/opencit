#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export INSTALL_LOG_FILE=/tmp/mtwilson-install.log

#java_required_version=1.7.0_51

# detect the packages we have to install
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
java_install $JAVA_PACKAGE

if [ -f "/usr/local/bin/java" ]; then
  rm -f "/usr/local/bin/java"
fi
ln -s "$JAVA_HOME/bin/java" "/usr/local/bin/java"

if [ -f "/usr/local/bin/keytool" ]; then
  rm -f "/usr/local/bin/keytool"
fi
ln -s "$JAVA_HOME/bin/keytool" "/usr/local/bin/keytool"