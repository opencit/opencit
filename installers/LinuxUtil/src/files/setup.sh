#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /usr/share/mtwilson/script
chmod 700 /usr/share/mtwilson/script
cp -r plugins /usr/share/mtwilson/script

# Combine all plugins into a single file "functions" which
# any script can just source in order to import all the utility
# functions to use directly instead of through the "mtwilson"
# command.
cat /usr/share/mtwilson/script/plugins/* > /usr/share/mtwilson/script/functions
chmod 700 /usr/share/mtwilson/script/functions
#cp functions /usr/local/share/mtwilson/util
#cp version /usr/local/share/mtwilson/util

mkdir -p /usr/local/bin
cp mtwilson /usr/local/bin
chmod 700 /usr/local/bin/mtwilson

