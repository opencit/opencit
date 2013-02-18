#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /usr/local/share/mtwilson/util
chmod 700 /usr/local/share/mtwilson/util
cp functions /usr/local/share/mtwilson/util
cp version /usr/local/share/mtwilson/util

mkdir -p /usr/local/bin
cp mtwilson /usr/local/bin
chmod +x /usr/local/bin/mtwilson

