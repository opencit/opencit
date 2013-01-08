#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

glassfish_required_version=3.0
java_required_version=1.6.0_29

# detect the packages we have to install
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
java_require
glassfish_install $GLASSFISH_PACKAGE

cp jackson-mapper-asl.jar ${GLASSFISH_HOME}/modules/
glassfish_stop
glassfish_start

echo
