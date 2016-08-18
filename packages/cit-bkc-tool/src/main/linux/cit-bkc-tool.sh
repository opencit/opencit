#!/bin/bash

# chkconfig: 2345 80 30
# description: CIT BKC TOOL

### BEGIN INIT INFO
# Provides:          cit
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Should-Start:      $portmap
# Should-Stop:       $portmap
# X-Start-Before:    nis
# X-Stop-After:      nis
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: cit
# Description:       Main script to run cit-bkc commands
### END INIT INFO

export CIT_BKC_TOOL=/usr/local/bin/cit-bkc-tool
export CIT_BKC_CONF_PATH=/usr/local/etc/cit-bkc-tool
export CIT_BKC_DATA_PATH=/usr/local/var/cit-bkc-tool/data
export CIT_BKC_REPORTS_PATH=/usr/local/var/cit-bkc-tool/reports
export CIT_BKC_RUN_PATH=/run/cit-bkc-tool
export CIT_BKC_PID_FILE=/run/cit-bkc-tool/pid


###################################################################################################

cit_bkc_help() {
    echo "Available commands:"
    echo "    cit-bkc-tool help                 display available commands"
    echo "    cit-bkc-tool                      run CIT BKC self-test but do not reboot"
    echo "    cit-bkc-tool --reboot             run CIT BKC self-test and automatically reboot as needed"
    echo "    cit-bkc-tool clear                clear CIT BKC self-test data (must do this to start a new self-test)"
    echo "    cit-bkc-tool status               prints current status (not started, in progress, report is ready, etc.)"
    echo "    cit-bkc-tool report               prints most recent report to stdout"
    echo "    cit-bkc-tool uninstall            remove CIT components and the CIT BKC tool (but keep logs)"
    echo "    cit-bkc-tool uninstall --purge    remove CIT components and the CIT BKC tool including logs"
}

###################################################################################################


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
    if [ "$CIT_BKC_REBOOT" == "yes" ]; then
        echo "# cit-bkc-tool auto-reboot mode" > /tmp/cit-bkc-tool.crontab
        echo "@reboot /usr/local/bin/cit-bkc-tool --reboot" >> /tmp/cit-bkc-tool.crontab
    else
        echo "# cit-bkc-tool interactive mode" > /tmp/cit-bkc-tool.crontab
    fi
    # remove any existing lines with cit-bkc-tool and then append new lines with cit-bkc-tool we just prepared
    crontab -u root -l 2>/dev/null | grep -v cit-bkc-tool | cat - /tmp/cit-bkc-tool.crontab | crontab -u root - 2>/dev/null
}

# precondition: ~/.bashrc exists
# postcondition:  the line 'cit-bkc-tool status' is added to it
cit_bkc_setup_notification() {
    SCRIPT=$HOME/.bashrc
    notification=$(grep cit-bkc-tool $SCRIPT)
    if [ -z "$notification" ]; then
      echo >> $HOME/.bashrc
      echo "/usr/local/bin/cit-bkc-tool status" >> $HOME/.bashrc
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
    CIT_BKC_PID=$(ps -A ww | grep -v grep | grep "$CIT_BKC_TOOL run" | awk '{ print $1 }')
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

# precondition:
#   for new self-test the data has already been cleared with cit_bkc_clear()
#   for continue self-test the data from prior run is stored in CIT_BKC_DATA_PATH
# parameters:
#   optional parameter '--reboot' if user wants automatic reboot
# postcondition:
#   test data is stored in CIT_BKC_DATA_PATH
#   if test is complete, report is in CIT_BKC_REPORTS_PATH
cit_bkc_run() {
    if [ "$1" == "--reboot" ]; then export CIT_BKC_REBOOT=yes; else export CIT_BKC_REBOOT=no; fi
    mkdir -p $CIT_BKC_DATA_PATH $CIT_BKC_REPORTS_PATH
    cit_bkc_setup_notification
    cit_bkc_setup_reboot

    cit_bkc_run_next

    # check if test is complete
    if [ "$CIT_BKC_TEST_COMPLETE" == "yes" ]; then
      if cit_bkc_run_next_report; then
        cit_bkc_report
      fi
    fi
}

# precondition:
#    directory CIT_BKC_DATA_PATH exists (may be empty)
# postcondition:
#    test data is stored in CIT_BKC_DATA_PATH
#    if test is complete, variable is set CIT_BKC_TEST_COMPLETE=yes
cit_bkc_run_next() {
    #  TODO: run next step based on current state
    echo "TODO: run next step based on current state; when complete set CIT_BKC_TEST_COMPLETE=yes"
}

# precondition:
#    self-test has already completed by cit_bkc_run_next and CIT_BKC_TEST_COMPLETE=yes
cit_bkc_run_next_report() {
    # TODO:  generate the report based on test results
    echo "TODO:  generate the report based on test results"
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

cit_bkc_status() {
    echo "CIT BKC Tool:"
#    if [ ! -d $CIT_BKC_DATA_PATH ]; then
#        echo "* Ready for self-test; type 'cit-bkc-tool --help' for more information"
#    fi
    if cit_bkc_is_running; then
      echo "* CIT BKC tool is running"
    fi

    if cit_bkc_report_is_available; then
        echo "* A report is available; type 'cit-bkc-tool report' to display"
    else
        echo "* No reports available; type 'cit-bkc-tool' to run the tool"
    fi
    
}

cit_bkc_uninstall() {
    # clear data, reports, runtime info
    cit_bkc_clear
    # purge configuration
    if [ "$1" == "--purge" ] || [ "$2" == "--purge" ]; then
      rm -rf $CIT_BKC_CONF_PATH
      mtwilson_args="uninstall --purge"
    else
      mtwilson_args="uninstall"
    fi
    if [ "$1" == "--clear-tpm" ] || [ "$2" == "--clear-tpm" ]; then
      rm -rf $CIT_BKC_CONF_PATH
      tagent_args="uninstall"
    else
      tagent_args="uninstall"
      # TODO:  save tpm password for next bkc install
    fi


      mtwilson_ctl=$(which mtwilson)
      if [ -n "$mtwilson_ctl" ]; then
        $mtwilson_ctl $mtwilson_args
      fi
      tagent_ctl=$(which tagent)
      if [ -n "$tagent_ctl" ]; then
        $tagent_ctl $tagent_args
      fi

    # trustagent uninstall copies configuration backup to datestr=`date +%Y-%m-%d.%H%M`   /tmp/trustagent.configuration.$datestr
    # so when unisntalling WITHOUT --purge, need to keep that and bkc installer can look for it on next install to read tpm password and set it in the trustagent.env file 
    rm -f $CIT_BKC_TOOL
}

###################################################################################################

# here we look for specific commands first that we will handle in the
# script, and anything else we send to the java application

case "$1" in
  --help)
    cit_bkc_help
    ;;
  help)
    cit_bkc_help
    ;;
  clear)
    cit_bkc_clear
    ;;
  run)
    cit_bkc_run $*
    ;;
  report)
    cit_bkc_report $*
    ;;
  status)
    cit_bkc_status $*
    ;;
  uninstall)
    shift
    cit_bkc_uninstall $*
    ;;
  *)
    # start a new process with 'run' command
    $CIT_BKC_TOOL run $*
    ;;
esac


exit $?
