#!/bin/bash

# Installation script for CIT BKC Tool. 
# This script is responsible ONLY for ensuring all components are installed locally.
# It DOES NOT run the CIT BKC tool.

# Outline:
# 1. Install Attestation Service 
# 2. Install Trust Agent
# 3. Install CIT BKC Tool

#####

# load installer environment file, if present
if [ -f $HOME/cit.env ]; then
  echo "Loading environment variables from $HOME/cit.env"
  source $HOME/cit.env
  env_file_exports=$(cat $HOME/cit.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
fi

# load functions file, if present
if [ -f functions.sh ]; then
  source functions.sh
fi

#####
# INSTALL BKC TOOL
chmod +x cit-bkc-tool.sh
cp cit-bkc-tool.sh /usr/local/bin/cit-bkc-tool


#####
# INSTALL ATTESTATION SERVICE
export MTWILSON_LOG_LEVEL=DEBUG

# if Attestation Service is already installed, stop it while we upgrade/reinstall
if which mtwilson >/dev/null 2>&1; then
  mtwilson stop
fi
if which cit >/dev/null 2>&1; then
  cit stop
fi

# mtwilson-server-2.1-SNAPSHOT.bin

preconfigure_mtwilson() {
  if [ -f $HOME/mtwilson.env ]; then
    echo "using pre-configured mtwilson.env"
    return
  fi
  cp mtwilson.env $HOME/mtwilson.env
  local admin_passwd=$(generate_password 16)
  update_property_in_file MTWILSON_ADMIN_PASSWORD $HOME/mtwilson.env "$admin_passwd"
  local database_passwd=$(generate_password 16)
  update_property_in_file MTWILSON_DATABASE_PASSWORD $HOME/mtwilson.env "$database_passwd"
  local pca_passwd=$(generate_password 16)
  update_property_in_file MTWILSON_PRIVACYCA_PASSWORD $HOME/mtwilson.env "$pca_passwd"
  local tag_passwd=$(generate_password 16)
  update_property_in_file MTWILSON_TAG_ADMIN_PASSWORD $HOME/mtwilson.env "$tag_passwd"
  local tagxml_passwd=$(generate_password 16)
  update_property_in_file MTWILSON_TAG_XML_PASSWORD $HOME/mtwilson.env "$tagxml_passwd"
}

MTWILSON_BIN=`ls -1 mtwilson-server-*.bin | head -n 1`
if [ -n "$MTWILSON_BIN" ]; then
  preconfigure_mtwilson
  chmod +x $MTWILSON_BIN
  ./$MTWILSON_BIN
fi


#####
# INSTALL TRUST AGENT
export TAGENT_LOG_LEVEL=DEBUG

# if Trust Agent is already installed, stop it while we upgrade/reinstall
if which tagent >/dev/null 2>&1; then
  tagent stop
fi

# mtwilson-trustagent-rhel-2.1-20160518.001429-5.bin

preconfigure_trustagent() {
  if [ -f $HOME/trustagent.env ]; then
    echo "using pre-configured trustagent.env"
    return
  fi
  cp trustagent.env $HOME/trustagent.env
  local tls_sha1=$(/usr/bin/sha1sum /opt/mtwilson/configuration/ssl.crt | /usr/bin/awk '{print $1}')
  local tls_sha256=$(/usr/bin/sha256sum /opt/mtwilson/configuration/ssl.crt | /usr/bin/awk '{print $1}')
  update_property_in_file MTWILSON_TLS_CERT_SHA1 $HOME/trustagent.env "$tls_sha1"
  update_property_in_file MTWILSON_TLS_CERT_SHA256 $HOME/trustagent.env "$tls_sha256"
}


TAGENT_BIN=`ls -1 mtwilson-trustagent-*.bin | head -n 1`
if [ -n "$TAGENT_BIN" ]; then
  preconfigure_trustagent
  chmod +x $TAGENT_BIN
  ./$TAGENT_BIN
fi
