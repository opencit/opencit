#!/bin/bash

CIT_BKC_PACKAGE_PATH=${CIT_BKC_PACKAGE_PATH:-/usr/local/share/cit-bkc-tool}
CIT_BKC_CONF_PATH=${CIT_BKC_CONF_PATH:-/usr/local/etc/cit-bkc-tool}

parse_args() {
  case "$1" in
    help)
      shift
      cit_bkc_help
      return $?
      ;;
    clear)
      shift
      cit_bkc_clear
      return $?
      ;;
    report)
      shift
      cit_bkc_report $*
      return $?
      ;;
    status)
      shift
      cit_bkc_status $*
      return $?
      ;;
    uninstall)
      shift
      cit_bkc_uninstall $*
      return $?
      ;;
    print-env)
      shift
      cit_bkc_print_env $*
      return $?
      ;;
    *)
      cit_bkc_run $*
      return $?
      ;;
  esac
}

load_util() {
  if [ -f $CIT_BKC_PACKAGE_PATH/functions.sh ]; then
    source $CIT_BKC_PACKAGE_PATH/functions.sh
  fi
  if [ -f $CIT_BKC_PACKAGE_PATH/util.sh ]; then
    source $CIT_BKC_PACKAGE_PATH/util.sh
  fi
}

###################################################################################################

cit_bkc_help() {
  cat $CIT_BKC_PACKAGE_PATH/README.md
}

cit_bkc_print_env() {
  env | grep -E "^CIT_BKC|^http_proxy|^https_proxy"
}

cit_bkc_store_proxy_env() {
  if [ -n "$http_proxy" ]; then
    echo "http_proxy=$http_proxy" > $CIT_BKC_CONF_PATH/http_proxy.env
  fi
  if [ -n "$https_proxy" ]; then
    echo "https_proxy=$https_proxy" > $CIT_BKC_CONF_PATH/https_proxy.env
  fi
}

###################################################################################################

run_bkc_tool() {
    echo "Running BKC Tool for Intel(R) Cloud Integrity Technology..."
    rm -rf $CIT_BKC_MONITOR_PATH/run-bkc-tool
    mkdir -p $CIT_BKC_MONITOR_PATH/run-bkc-tool
    cp $CIT_BKC_PACKAGE_PATH/cit-bkc-tool.mark $CIT_BKC_MONITOR_PATH/run-bkc-tool/.markers
    export_cit_bkc_reboot_counter
    $CIT_BKC_PACKAGE_PATH/monitor.sh $CIT_BKC_PACKAGE_PATH/cit-bkc-validation.sh $CIT_BKC_MONITOR_PATH/run-bkc-tool/.markers $CIT_BKC_MONITOR_PATH/run-bkc-tool
    local result=$?
    if [ $result -eq 0 ]; then
      CIT_BKC_TEST_COMPLETE=yes
    elif [ $result -eq 1 ]; then
      CIT_BKC_TEST_COMPLETE=no
      CIT_BKC_TEST_ERROR=yes
      #echo_failure "Validation failed"
      #echo_info "Log file: $CIT_BKC_MONITOR_PATH/run-bkc-tool/stdout"
    elif [ $result -eq 255 ]; then
      CIT_BKC_TEST_COMPLETE=no
      mkdir -p $(dirname $CIT_BKC_REBOOT_FILE)
      touch $CIT_BKC_REBOOT_FILE
    fi
    return $result
}

# precondition:
# variable CIT_BKC_REBOOT is set to 'yes' for auto-reboot or 'no' (or anything else) for interactive mode
# postcondition:
# crontab is edited to reflect auto-reboot or interactive mode
# unit test:
# crontab -l  # see what you already have
# ( export CIT_BKC_REBOOT=yes && cit_bkc_setup_reboot && crontab -l )
# ( export CIT_BKC_REBOOT=no && cit_bkc_setup_reboot && crontab -l )
cit_bkc_setup_reboot() {
    touch /tmp/cit-bkc-tool.crontab && chmod 600 /tmp/cit-bkc-tool.crontab
    echo "# cit-bkc-tool auto-resume after reboot" > /tmp/cit-bkc-tool.crontab
    echo "@reboot /usr/local/bin/cit-bkc-tool >>${CIT_BKC_LOG_PATH}/headless.log 2>&1" >> /tmp/cit-bkc-tool.crontab
    # remove any existing lines with cit-bkc-tool and then append new lines with cit-bkc-tool we just prepared
    crontab -u root -l 2>/dev/null | grep -v cit-bkc-tool | cat - /tmp/cit-bkc-tool.crontab | crontab -u root - 2>/dev/null
    rm -f /tmp/cit-bkc-tool.crontab
}

cit_bkc_setup_reboot_clear() {
    # remove any existing lines with cit-bkc-tool
    crontab -u root -l 2>/dev/null | grep -v cit-bkc-tool | crontab -u root - 2>/dev/null
}

# precondition: ~/.bash_profile exists
# postcondition:  the line 'cit-bkc-tool status' is added to it
# NOTES:
#   the .bashrc file must be silent or else sftp connections will fail
#   so the echo messages must be in .bash_profile, not in .bashrc
cit_bkc_setup_notification() {
    SCRIPT=$HOME/.bash_profile
    notification=$(grep cit-bkc-tool $SCRIPT)
    if [ -z "$notification" ]; then
      echo >> $HOME/.bash_profile
      echo "/usr/local/bin/cit-bkc-tool status" >> $HOME/.bash_profile
    fi
}

# removes the notification from bash profile, called from uninstall
cit_bkc_setup_notification_clear() {
    SCRIPT=$HOME/.bash_profile
    notification=$(grep cit-bkc-tool $SCRIPT)
    if [ -n "$notification" ]; then
        sed -i '/cit-bkc-tool status/d' $SCRIPT
    fi
}


# precondition:  CIT_BKC_REPORTS_PATH variable is defined, for EXAMPLE /usr/local/var/cit-bkc-tool
# postcondition: LATEST set to filename of most recent report
# return code: 0 if report is available, 1 if not available
cit_bkc_report_is_available() {
    if [ ! -d $CIT_BKC_REPORTS_PATH ]; then
      return 1
    fi
    # look for most recent report
    LATEST=$(ls -1t $CIT_BKC_REPORTS_PATH | head -n 1)
    if [ -n "$LATEST" ]; then
      return 0
    else
      return 1
    fi
}

# returns 0 if CIT BKC tool is running, 1 if not running
# side effects: sets CIT_BKC_PID if CIT is running, or to empty otherwise
cit_bkc_is_running() {
  CIT_BKC_PID=
  if [ -f $CIT_BKC_PID_FILE ]; then
    CIT_BKC_PID=$(cat $CIT_BKC_PID_FILE)
    local is_running=`ps -A -o pid | grep "^\s*${CIT_BKC_PID}$"`
    if [ -z "$is_running" ]; then
      # stale PID file
      CIT_BKC_PID=
    fi
  fi
  if [ -z "$CIT_BKC_PID" ]; then
    # check the process list just in case the pid file is stale
    CIT_BKC_PID=$(ps -A ww | grep -v grep | grep "cit-bkc-tool run" | awk '{ print $1 }')
  fi
  if [ -z "$CIT_BKC_PID" ]; then
    # CIT is not running
    return 1
  fi
  # CIT is running and CIT_BKC_PID is set
  return 0
}

###################################################################################################

cit_bkc_clear() {
    rm -rf $CIT_BKC_DATA_PATH $CIT_BKC_REPORTS_PATH $CIT_BKC_RUN_PATH
}

# return: 0 if validation complete and ready for reporting, 1 otherwise
cit_bkc_run_validation() {
    local result
    # did the validation script run already? check status
    if [ ! -d "$CIT_BKC_MONITOR_PATH/run-bkc-tool" ]; then
        # no monitor files, so run the installer with progress bar
        run_bkc_tool
        result=$?
    elif is_active $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        if is_running $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
            echo "cit-bkc-tool already running, run 'cit-bkc-tool status' to monitor"
            result=1
        else
            run_bkc_tool
            result=$?
        fi
    elif is_done $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        #echo "cit-bkc-tool validation complete; run 'cit-bkc-tool report' to see report"
        result=0
    elif is_error $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        #echo "cit-bkc-tool validation error; run 'cit-bkc-tool report' to see report"
        rm_dir "$CIT_BKC_MONITOR_PATH/run-bkc-tool"
        run_bkc_tool
        result=$?
    else
        rm_dir "$CIT_BKC_MONITOR_PATH/run-bkc-tool"
        run_bkc_tool
        result=$?
    fi
    return $result
}

is_reboot_required() {
  if [ -f "$CIT_BKC_REBOOT_FILE" ]; then
    return 0
  fi
  return 1
}

# reads the reboot counter and exports CIT_BKC_REBOOT_COUNTER
export_cit_bkc_reboot_counter() {
    local reboot_counter_file=$CIT_BKC_DATA_PATH/.reboot_counter
    if [ -f $reboot_counter_file ]; then
        export CIT_BKC_REBOOT_COUNTER=$(cat $reboot_counter_file)
    else
        export CIT_BKC_REBOOT_COUNTER=0
    fi
}

# depends on export_cit_bkc_reboot_counter
increment_cit_bkc_reboot_counter() {
    local reboot_counter_file=$CIT_BKC_DATA_PATH/.reboot_counter
    mkdir -p ${CIT_BKC_DATA_PATH}
    ((CIT_BKC_REBOOT_COUNTER+=1))
    echo "$CIT_BKC_REBOOT_COUNTER" > $reboot_counter_file
}

cit_bkc_reboot() {
    shutdown_cmd=$(which shutdown 2>/dev/null)
    if [ -z "${shutdown_cmd}" ]; then
        shutdown_cmd=/usr/sbin/shutdown
    fi
    if [ ! -f "${shutdown_cmd}" ]; then
        shutdown_cmd=/sbin/shutdown
    fi
    
    cit_bkc_setup_reboot
    if [ "$CIT_BKC_REBOOT" == "yes" ]; then
        rm -f $CIT_BKC_REBOOT_FILE
        export_cit_bkc_reboot_counter
        increment_cit_bkc_reboot_counter
        # a reboot is needed
        if [ ! -f "${shutdown_cmd}" ]; then
            echo_warning "cit-bkc-tool: \"shutdown\" command not found, cannot reboot system automatically"
            exit 255
        fi
        echo
        echo_info "Rebooting in 1 minute... 'shutdown -c' to cancel";
        echo
        $shutdown_cmd --reboot +1 >/dev/null 2>&1
        exit 255
    else
      echo_warning "cit-bkc-tool: reboot required, run 'cit-bkc-tool' after reboot to continue"
      exit 255
    fi
}

# precondition:
#   for new self-test the data has already been cleared with cit_bkc_clear()
#   for continue self-test the data from prior run is stored in CIT_BKC_DATA_PATH
# parameters:
#   none; export CIT_BKC_REBOOT=no to turn off automatic reboots
# postcondition:
#   test data is stored in CIT_BKC_DATA_PATH
#   if test is complete, report is in CIT_BKC_REPORTS_PATH
cit_bkc_run() {
    export CIT_BKC_REBOOT=${CIT_BKC_REBOOT:-yes}
    local result

    cit_bkc_setup_notification
    mkdir -p ${CIT_BKC_LOG_PATH}

    # is mtwilson installed?
    $CIT_BKC_PACKAGE_PATH/install_cit_service.sh status
    result=$?
    if [ $result -eq 1 ]; then
        cit_bkc_run_mtwilson_installation
        result=$?
    fi
    if [ $result -eq 255 ] || is_reboot_required; then
        cit_bkc_reboot
        return $?
    elif [ $result -ne 0 ]; then
        return $result
    fi

    # is tagent installed?
    $CIT_BKC_PACKAGE_PATH/install_cit_agent.sh status
    result=$?
    if [ $result -eq 1 ]; then
        cit_bkc_run_tagent_installation
        result=$?
    fi
    if [ $result -eq 255 ] || is_reboot_required; then
        cit_bkc_reboot
        return $?
    elif [ $result -ne 0 ]; then
        return $result
    fi

    cit_bkc_run_validation
    result=$?
    cit_bkc_run_next_report
    cit_bkc_report
    if [ $result -eq 255 ] || is_reboot_required; then
        cit_bkc_reboot
    fi

    return $result
}


# return codes (forwarding the install_cit_agent exit code)
# 0 if already installed
# 1 if installation failed
# 2 if another process is already installing
# 255 if reboot required to continue installation
cit_bkc_run_mtwilson_installation() {
    $CIT_BKC_PACKAGE_PATH/install_cit_service.sh
}

# return codes (forwarding the install_cit_agent exit code)
# 0 if already installed
# 1 if installation failed
# 2 if another process is already installing
# 255 if reboot required to continue installation
cit_bkc_run_tagent_installation() {
    $CIT_BKC_PACKAGE_PATH/install_cit_agent.sh
}


cit_bkc_status_installation() {
    # get status of trust agent installation
    $CIT_BKC_PACKAGE_PATH/install_cit_service.sh status
    local result=$?
    if [ $result -eq 2 ]; then
        local monitor_path=$($CIT_BKC_PACKAGE_PATH/install_cit_service.sh print-monitor-path)
        $CIT_BKC_PACKAGE_PATH/monitor.sh --noexec $monitor_path
    fi

    # get status of attestation service installation
    $CIT_BKC_PACKAGE_PATH/install_cit_agent.sh status
    local result=$?
    if [ $result -eq 2 ]; then
        local monitor_path=$($CIT_BKC_PACKAGE_PATH/install_cit_agent.sh print-monitor-path)
        $CIT_BKC_PACKAGE_PATH/monitor.sh --noexec $monitor_path
    fi

    return $result
}

cit_bkc_status_validation() {
    # assume mtwilson and tagent are installed, now check on validation tests
    if [ ! -d "$CIT_BKC_MONITOR_PATH/run-bkc-tool" ]; then
        echo "cit-bkc-tool ready; run 'cit-bkc-tool' to continue"
    elif is_active $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        if is_running $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
          # test in progress, so monitor the other process
          echo "Running BKC Tool for Intel(R) Cloud Integrity Technology..."
          $CIT_BKC_PACKAGE_PATH/monitor.sh --noexec $CIT_BKC_MONITOR_PATH/run-bkc-tool
          if is_done $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
              echo "cit-bkc-tool validation complete; run 'cit-bkc-tool report' to see report"
              return 0
          fi
        else
          echo "cit-bkc-tool was interrupted during validation; run 'cit-bkc-tool' to continue"
        fi
    elif is_done $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        echo "cit-bkc-tool validation complete; run 'cit-bkc-tool report' to see report"
        return 0
    elif is_error $CIT_BKC_MONITOR_PATH/run-bkc-tool; then
        echo "cit-bkc-tool validation error; run 'cit-bkc-tool report' to see report"
        return 1
    else
        rm_dir "$CIT_BKC_MONITOR_PATH/run-bkc-tool"
        echo "cit-bkc-tool validation status unknown; run 'cit-bkc-tool' to continue"
    fi
    return 1
}

# displays current status only, does not install or test anything
cit_bkc_status() {
    if cit_bkc_status_installation; then
      if cit_bkc_status_validation; then
        if cit_bkc_report_is_available; then
          cit_bkc_report
          return 0
        fi
      fi
    fi
}

# precondition:
#    self-test has already completed by cit_bkc_run_next and CIT_BKC_TEST_COMPLETE=yes
# postcondition:
#    generates two files:  report.date (color) and report.date.txt (plain)
cit_bkc_run_next_report() {
    mkdir -p "$CIT_BKC_DATA_PATH" "$CIT_BKC_REPORTS_PATH"
    local current_date=$(date +%Y%m%d.%H%M%S)
    local report_file_name="$CIT_BKC_REPORTS_PATH/report.$current_date"
    local report_inputs=$(cd $CIT_BKC_DATA_PATH && ls -1tr *.report 2>/dev/null)
    if [ -n "$report_inputs" ]; then
      # echo "# $report_file_name" > $report_file_name
      rm -rf $report_file_name
      for filename in $report_inputs
      do
        reportname=$(basename $filename .report)
        reportcontent=$(cat $CIT_BKC_DATA_PATH/$filename)
        echo "$reportname: $reportcontent" >> $report_file_name.txt
        case "$reportcontent" in
            OK*)
                echo -e "$TERM_COLOR_GREEN$reportname: $reportcontent$TERM_COLOR_NORMAL" >> $report_file_name
                ;;
            REBOOT*)
                echo -e "$TERM_COLOR_CYAN$reportname: $reportcontent$TERM_COLOR_NORMAL" >> $report_file_name
                ;;
            ERROR*)
                echo -e "$TERM_COLOR_RED$reportname: $reportcontent$TERM_COLOR_NORMAL" >> $report_file_name
                ;;
            SKIP*)
                echo -e "$TERM_COLOR_YELLOW$reportname: $reportcontent$TERM_COLOR_NORMAL" >> $report_file_name
                ;;
        esac
      done
      return 0
    else
      echo "No data available to report" >&2
      return 1
    fi
}

cit_bkc_report() {
    if cit_bkc_report_is_available; then
        # filename in $LATEST
        cat $CIT_BKC_REPORTS_PATH/$LATEST
    else
        echo "No reports available" >&2
        exit 1
    fi
}


cit_bkc_stop() {
  if cit_bkc_is_running; then
    kill -9 $CIT_BKC_PID
    return $?
  fi
}

cit_bkc_uninstall_mtwilson() {
    # uninstall mtwilson
    if which mtwilson >/dev/null 2>&1; then
      mtwilson uninstall --purge
    fi
    rm -rf /etc/intel/ /opt/intel/ /opt/mtwilson/
    # completely remove postgresql to avoid problems when reinstalling mtwilson
    if yum_detect; then
      yum -y remove postgresql*
    elif apt_detect; then
      apt-get -y purge postgresql*
    fi
    rm -rf /var/lib/pgsql/
}

cit_bkc_uninstall_tagent() {
  # clear tpm
  local istpm2=$(which tpm2_takeownership 2>/dev/null)
  if [ -n "$istpm2" ]; then
    tpm2_takeownership -c
  fi
  # uninstall tagent
  local is_tagent=$(which tagent 2>/dev/null)
  if which tagent >/dev/null 2>&1; then
    TPM_OWNER_SECRET=$(tagent config tpm.owner.secret)
    if [ -n "$TPM_OWNER_SECRET" ]; then      
      update_property_in_file TPM_OWNER_SECRET /root/trustagent.env "$TPM_OWNER_SECRET"
    fi
    tagent uninstall --purge
  fi
}

cit_bkc_uninstall() {
    cit_bkc_stop

    cit_bkc_setup_notification_clear

    # clear data, reports, runtime info
    rm_file "$CIT_BKC_BIN_PATH/cit-bkc-tool"
    rm_dir "$CIT_BKC_CONF_PATH"
    rm_dir "$CIT_BKC_DATA_PATH"
    rm_dir "$CIT_BKC_REPORTS_PATH"
    rm_dir "$CIT_BKC_MONITOR_PATH"
    rm_dir "$CIT_BKC_PACKAGE_PATH"
    rm_dir "$CIT_BKC_RUN_PATH"

    cit_bkc_uninstall_mtwilson
    cit_bkc_uninstall_tagent

    rm_file "$HOME/mtwilson.env"
    rm_file "$HOME/trustagent.env"

}

###################################################################################################

cit_bkc_store_proxy_env
load_util
load_env_dir "$CIT_BKC_CONF_PATH"
parse_args $*
exit $?