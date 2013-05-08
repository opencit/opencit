#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

#glassfish_required_version=3.0
#java_required_version=1.6.0_29

# detect the packages we have to install
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
chmod +x MtWilsonLinuxUtil.bin
./MtWilsonLinuxUtil.bin
if [ -f /usr/share/mtwilson/script/functions ]; then . /usr/share/mtwilson/script/functions; else echo "Missing file: /usr/share/mtwilson/script/functions"; exit 1; fi

# SCRIPT EXECUTION
if no_java ${JAVA_REQUIRED_VERSION:-1.6}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-1.6} or later"; exit 1; fi
glassfish_install $GLASSFISH_PACKAGE

cp jackson-core-asl.jar ${GLASSFISH_HOME}/modules/
cp jackson-mapper-asl.jar ${GLASSFISH_HOME}/modules/
cp jackson-xc.jar ${GLASSFISH_HOME}/modules/
glassfish_stop
glassfish_start

echo
