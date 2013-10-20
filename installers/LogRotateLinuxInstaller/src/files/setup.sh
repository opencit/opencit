#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION

logRotate_clear() {
  logrotate=""
}

logRotate_detect() {
  local logrotaterc=`ls -1 /etc/logrotate.conf 2>/dev/null | tail -n 1`
  logrotate=`which logrotate 2>/dev/null`
}

logRotate_install() {
  LOGROTATE_YUM_PACKAGES="logrotate"
  LOGROTATE_APT_PACKAGES="logrotate"
  LOGROTATE_YAST_PACKAGES=""
  LOGROTATE_ZYPPER_PACKAGES="logrotate"
  auto_install "Log Rotate" "LOGROTATE"
  logRotate_clear; logRotate_detect;
    if [[ -z "$logrotate" ]]; then
      echo_failure "Unable to auto-install Log Rotate"     
    else
      echo_success "Log Rotate installed in $logrotate"
    fi
}

logRotate_install
