#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

mkdir -p /usr/local/share/mtwilson/util
chmod 700 /usr/local/share/mtwilson/util
cp functions /usr/local/share/mtwilson/util
cp version /usr/local/share/mtwilson/util

# api client: ensure destination exists and clean it before copying
mkdir -p /usr/local/share/mtwilson/apiclient/java
rm -rf /usr/local/share/mtwilson/apiclient/java/*
cp lib/*.jar /usr/local/share/mtwilson/apiclient/java

# setup console: create folder and copy the executable jar
mkdir -p /opt/intel/cloudsecurity/setup-console
rm -rf /opt/intel/cloudsecurity/setup-console/*.jar
cp setup-console*.jar /opt/intel/cloudsecurity/setup-console

mkdir -p /usr/local/bin
cp mtwilson /usr/local/bin
chmod +x /usr/local/bin/mtwilson

