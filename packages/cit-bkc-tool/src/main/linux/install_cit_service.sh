#!/bin/bash

# Installation script for CIT Attestation Service
# This script is responsible ONLY for ensuring all components are installed locally.
# It DOES NOT run the CIT BKC tool.

# Preconditions:
# CIT BKC Tool must be installed in /usr/local

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

mtwilson_install_status() {
    if is_done $CIT_SERVICE_MONITOR_PATH; then
        echo "CIT Attestation Service is already installed"
        return 0
    fi
    if is_error $CIT_SERVICE_MONITOR_PATH; then
        echo "CIT Attestation Service installation failed"
        return 1
    fi
    if is_active $CIT_SERVICE_MONITOR_PATH; then
        if is_running $CIT_SERVICE_MONITOR_PATH; then
            echo "CIT Attestation Service installation is in progress"
            return 2
        fi
    fi
    # could do further checks here like if mtwilson service is up, etc. 
    echo "CIT Attestation Service is not installed"
    return 1
}


# Run the installer with console progress bar
mtwilson_install() {

    local mtwilson_bin=$(ls -1 $CIT_BKC_PACKAGE_PATH/mtwilson-server-*.bin | head -n 1 2>/dev/null)
    if [ -z "$mtwilson_bin" ]; then
        echo_failure "CIT Attestation Service installer is missing"
        exit 1
    fi

    echo "Installing CIT Attestation Service..."
    mtwilson_preconfigure
    chmod +x $mtwilson_bin
    export MTWILSON_LOG_LEVEL=DEBUG
    rm -rf $CIT_SERVICE_MONITOR_PATH
    mkdir -p $CIT_SERVICE_MONITOR_PATH
    $CIT_BKC_PACKAGE_PATH/monitor.sh $mtwilson_bin $CIT_BKC_PACKAGE_PATH/cit-service.mark $CIT_SERVICE_MONITOR_PATH
    result=$?
    if [ $result -eq 255 ] || [ -f "/opt/mtwilson/var/reboot_required" ]; then
       rm -f /opt/mtwilson/var/reboot_required
       echo_info "CIT Attestation Service requires reboot to continue";
       exit 255
    fi
    if [ $result -ne 0 ]; then
        echo_failure "Failed to install CIT Attestation Service";
        echo_info "Log file: $CIT_SERVICE_MONITOR_PATH/stdout"
        exit 1
    fi
}


load_util
load_env_dir /usr/local/etc/cit-bkc-tool

CIT_SERVICE_MONITOR_PATH=$CIT_BKC_MONITOR_PATH/install_cit_service

if [ "$1" == "print-monitor-path" ]; then
    echo "$CIT_SERVICE_MONITOR_PATH"
    exit 0
fi
if [ "$1" == "status" ]; then
    mtwilson_install_status
    exit $?
fi

# INSTALL ATTESTATION SERVICE

# if Attestation Service is already installed, stop it while we upgrade/reinstall
mtwilson_stop
mtwilson_install
