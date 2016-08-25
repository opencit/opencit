#!/bin/bash

# Installation script for CIT BKC Tool. 
# This script is responsible ONLY for ensuring all components are installed locally.
# It DOES NOT run the CIT BKC tool.

# NOTE:  \cp escapes alias, needed because some systems alias cp to always prompt before override

# Outline:
# 1. Install Attestation Service 
# 2. Install Trust Agent
# 3. Install CIT BKC Tool

#####


load_util() {
  if [ -f /usr/local/share/cit-bkc-tool/functions.sh ]; then
    source /usr/local/share/cit-bkc-tool/functions.sh
  fi
  if [ -f /usr/local/share/cit-bkc-tool/util.sh ]; then
    source /usr/local/share/cit-bkc-tool/util.sh
  fi
}

mtwilson_stop() {
  if which mtwilson >/dev/null 2>&1; then
    mtwilson stop
  fi
  if which cit >/dev/null 2>&1; then
    cit stop
  fi
}

tagent_stop() {
  if which tagent >/dev/null 2>&1; then
    tagent stop
  fi
}


# mtwilson-server-2.1-SNAPSHOT.bin

mtwilson_preconfigure() {
  if [ -f $HOME/mtwilson.env ]; then
    echo "using pre-configured mtwilson.env"
    return
  fi
  cp $CIT_BKC_PACKAGE_PATH/mtwilson.env $HOME/mtwilson.env
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

mtwilson_install() {
  local mtwilson_bin=$(ls -1 $CIT_BKC_PACKAGE_PATH/mtwilson-server-*.bin | head -n 1 2>/dev/null)
  if [ -n "$mtwilson_bin" ]; then
    mtwilson_preconfigure
    chmod +x $mtwilson_bin
    export MTWILSON_LOG_LEVEL=DEBUG
    $mtwilson_bin
    if [ $? -ne 0 ]; then echo_failure "Failed to install CIT Attestation Service"; exit 1; fi
  fi
}


# mtwilson-trustagent-rhel-2.1-20160518.001429-5.bin

tagent_preconfigure() {
  if [ -f $HOME/trustagent.env ]; then
    echo "using pre-configured trustagent.env"
    return
  fi
  cp $CIT_BKC_PACKAGE_PATH/trustagent.env $HOME/trustagent.env
  local tls_sha1=$(/usr/bin/sha1sum /opt/mtwilson/configuration/ssl.crt | /usr/bin/awk '{print $1}')
  local tls_sha256=$(/usr/bin/sha256sum /opt/mtwilson/configuration/ssl.crt | /usr/bin/awk '{print $1}')
  update_property_in_file MTWILSON_TLS_CERT_SHA1 $HOME/trustagent.env "$tls_sha1"
  update_property_in_file MTWILSON_TLS_CERT_SHA256 $HOME/trustagent.env "$tls_sha256"
}

tagent_install() {
  local tagent_bin=$(ls -1 $CIT_BKC_PACKAGE_PATH/mtwilson-trustagent-*.bin | head -n 1 2>/dev/null)
  if [ -n "$tagent_bin" ]; then
    tagent_preconfigure
    chmod +x $tagent_bin
    export TAGENT_LOG_LEVEL=DEBUG
    $tagent_bin
    result=$?
    if [ -f "/opt/trustagent/var/reboot_required" ]; then
       echo_info "CIT Trust Agent requires reboot to continue";
       exit 255
    fi
    if [ $result -ne 0 ]; then echo_failure "Failed to install CIT Trust Agent"; exit 1; fi
  fi
}


load_util
load_env_dir /usr/local/etc/cit-bkc-tool

# INSTALL ATTESTATION SERVICE

# if Attestation Service is already installed, stop it while we upgrade/reinstall
mtwilson_stop
mtwilson_install

# INSTALL TRUST AGENT

# if Trust Agent is already installed, stop it while we upgrade/reinstall
tagent_stop
tagent_install

