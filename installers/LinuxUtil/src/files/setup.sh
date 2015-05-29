#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /opt/mtwilson/share/scripts
mkdir -p /opt/mtwilson/bin
#chmod 700 /usr/local/share/mtwilson/util
#functions.sh neeeds to be sourced from mtwilson.sh
cp functions.sh /opt/mtwilson/share/scripts/functions
cp version /opt/mtwilson/bin/version
cp mtwilson.sh /opt/mtwilson/bin/mtwilson
chmod +x /opt/mtwilson/bin/mtwilson

#If user is root then create mtwilson symlink to /usr/local/bin otherwise export path '$MTWILSON_HOME/bin'
if [ `whoami` == "root" ]; then
 mkdir -p /usr/local/bin
 #Remove symbolic link if already exist
 rm /usr/local/bin/mtwilson
 ln -s /opt/mtwilson/bin/mtwilson /usr/local/bin/mtwilson
else
 export PATH=/opt/mtwilson/bin:$PATH
fi