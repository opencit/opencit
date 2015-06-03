#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /opt/mtwilson/share/scripts
mkdir -p /opt/mtwilson/configuration
mkdir -p /opt/mtwilson/bin

#chmod 700 /usr/local/share/mtwilson/util
#functions.sh neeeds to be sourced from mtwilson.sh
cp functions.sh /opt/mtwilson/share/scripts/functions
cp version /opt/mtwilson/configuration/version
cp mtwilson.sh /opt/mtwilson/bin/mtwilson
chmod +x /opt/mtwilson/bin/mtwilson

#If user is root then create mtwilson symlink to /usr/local/bin otherwise export path '$MTWILSON_HOME/bin'
if [ `whoami` == "root" ]; then
 if [ ! -d /usr/local/bin ]; then
   mkdir -p /usr/local/bin
 fi
 #Remove symbolic link if already exist
 rm -f /usr/local/bin/mtwilson
 ln -s /opt/mtwilson/bin/mtwilson /usr/local/bin/mtwilson
else
 export PATH=/opt/mtwilson/bin:$PATH
fi