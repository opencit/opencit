#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /usr/local/share/mtwilson/util
chmod 700 /usr/local/share/mtwilson/util
cp functions.sh /usr/local/share/mtwilson/util/functions
cp version /usr/local/share/mtwilson/util

mkdir -p /usr/local/bin
cp mtwilson.sh /usr/local/bin/mtwilson
chmod +x /usr/local/bin/mtwilson
