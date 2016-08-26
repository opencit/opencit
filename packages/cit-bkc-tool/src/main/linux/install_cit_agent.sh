#!/bin/bash

# Installation script for CIT Trust Agent
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

tagent_stop() {
  if which tagent >/dev/null 2>&1; then
    tagent stop
  fi
}

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
  pca_passwd=$(read_property_from_file MTWILSON_PRIVACYCA_PASSWORD $HOME/mtwilson.env)
  update_property_in_file MTWILSON_API_PASSWORD $HOME/trustagent.env "$pca_passwd"
}

tagent_install_status() {
    if is_done $CIT_AGENT_MONITOR_PATH; then
        echo "CIT Trust Agent is already installed"
        return 0
    fi
    if is_error $CIT_AGENT_MONITOR_PATH; then
        echo "CIT Trust Agent installation failed"
        return 1
    fi
    if is_active $CIT_AGENT_MONITOR_PATH; then
        if is_running $CIT_AGENT_MONITOR_PATH; then
            echo "CIT Trust Agent installation is in progress"
            return 2
        fi
    fi
    # could do further checks here like if tagent service is up, etc. 
    echo "CIT Trust Agent is not installed"
    return 1
}

# Run the installer with console progress bar
tagent_install() {
    local tagent_bin=$(ls -1 $CIT_BKC_PACKAGE_PATH/mtwilson-trustagent-*.bin | head -n 1 2>/dev/null)
    if [ -z "$tagent_bin" ]; then
        echo_failure "CIT Trust Agent installer is missing"
        exit 1
    fi

    echo "Installing CIT Trust Agent..."
    tagent_preconfigure
    chmod +x $tagent_bin
    export TAGENT_LOG_LEVEL=DEBUG
    rm -rf $CIT_AGENT_MONITOR_PATH
    mkdir -p $CIT_AGENT_MONITOR_PATH
    $CIT_BKC_PACKAGE_PATH/monitor.sh $tagent_bin $CIT_BKC_PACKAGE_PATH/cit-agent.mark $CIT_AGENT_MONITOR_PATH
    result=$?
    if [ $result -eq 255 ] || [ -f "/opt/trustagent/var/reboot_required" ]; then
       rm -f /opt/trustagent/var/reboot_required
       echo_info "CIT Trust Agent requires reboot to continue";
       exit 255
    fi
    if [ $result -ne 0 ]; then
        echo_failure "Failed to install CIT Trust Agent";
        echo_info "Log file: $CIT_AGENT_MONITOR_PATH/stdout"
        exit 1
    fi
    exit 0
}

# INSTALL TRUST AGENT

load_util
load_env_dir /usr/local/etc/cit-bkc-tool

CIT_AGENT_MONITOR_PATH=$CIT_BKC_MONITOR_PATH/install_cit_agent

if [ "$1" == "print-monitor-path" ]; then
    echo "$CIT_AGENT_MONITOR_PATH"
    exit 0
fi
if [ "$1" == "status" ]; then
    tagent_install_status
    exit $?
fi

# if Trust Agent is already installed, stop it while we upgrade/reinstall
tagent_stop
tagent_install
