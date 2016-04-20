#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export MTWILSON_HOME=${MTWILSON_HOME:-/opt/mtwilson}

if [ -f functions.sh ]; then . functions.sh; else echo "Missing file: functions.sh"; exit 1; fi

mkdir -p $MTWILSON_HOME/share/scripts
mkdir -p $MTWILSON_HOME/configuration
mkdir -p $MTWILSON_HOME/bin

#chmod 700 /usr/local/share/mtwilson/util
#functions.sh neeeds to be sourced from mtwilson.sh
cp functions.sh $MTWILSON_HOME/share/scripts/functions
cp version $MTWILSON_HOME/configuration/version
cp mtwilson.sh $MTWILSON_HOME/bin/mtwilson.sh
rm -f $MTWILSON_HOME/bin/mtwilson
ln -s $MTWILSON_HOME/bin/mtwilson.sh $MTWILSON_HOME/bin/mtwilson
chmod +x $MTWILSON_HOME/bin/*

#If user is root then create mtwilson symlink to /usr/local/bin otherwise export path '$MTWILSON_HOME/bin'
if [ `whoami` == "root" ]; then
 if [ ! -d /usr/local/bin ]; then
   mkdir -p /usr/local/bin
 fi
 #Remove symbolic link if already exist
 rm -f /usr/local/bin/mtwilson
 ln -s $MTWILSON_HOME/bin/mtwilson /usr/local/bin/mtwilson
fi
