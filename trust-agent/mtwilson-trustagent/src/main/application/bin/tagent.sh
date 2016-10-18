#!/bin/bash

# chkconfig: 2345 80 30
# description: Intel TrustAgent Service

### BEGIN INIT INFO
# Provides:          tagent
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Should-Start:      $portmap
# Should-Stop:       $portmap
# X-Start-Before:    nis
# X-Stop-After:      nis
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: trust agent script
# Description:       Main script to run trust agent tasks
### END INIT INFO
DESC="Trust Agent"
NAME=tagent
DAEMON=/opt/trustagent/bin/$NAME

###################################################################################################
#Set environment specific variables here 
###################################################################################################

# the home directory must be defined before we load any environment or
# configuration files; it is explicitly passed through the sudo command
export TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-/opt/trustagent}

# the env directory is not configurable; it is defined as TRUSTAGENT_HOME/env.d and the
# administrator may use a symlink if necessary to place it anywhere else
export TRUSTAGENT_ENV=$TRUSTAGENT_HOME/env.d

trustagent_load_env() {
  local env_files="$@"
  local env_file_exports
  for env_file in $env_files; do
    if [ -n "$env_file" ] && [ -f "$env_file" ]; then
      . $env_file
      env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
      if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
    fi
  done
}

# load environment variables; these override any existing environment variables.
# the idea is that if someone wants to override these, they must have write
# access to the environment files that we load here. 
if [ -d $TRUSTAGENT_ENV ]; then
  trustagent_load_env $(ls -1 $TRUSTAGENT_ENV/*)
fi

###################################################################################################

# if non-root execution is specified, and we are currently root, start over; the TRUSTAGENT_SUDO variable limits this to one attempt
# we make an exception for the following commands:
# - 'uninstall' may require root access to delete users and certain directories
# - 'update-system-info' requires root access to use dmidecode and virsh commands
if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ $(whoami) == "root" ] && [ -z "$TRUSTAGENT_SUDO" ] && [ "$1" != "uninstall" -a "$1" != "update-system-info" ]; then

  # before we switch to non-root, check if tcsd is running and start it if necessary
  trousers=$(which tcsd 2>/dev/null)
  if [ -n "$trousers" ]; then $trousers; fi

  export TRUSTAGENT_SUDO=true
  sudo -u $TRUSTAGENT_USERNAME -H -E $TRUSTAGENT_BIN/tagent $*
  exit $?
fi

###################################################################################################


# default directory layout follows the 'home' style
TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-${TRUSTAGENT_CONF:-$TRUSTAGENT_HOME/configuration}}
TRUSTAGENT_JAVA=${TRUSTAGENT_JAVA:-$TRUSTAGENT_HOME/java}
TRUSTAGENT_BIN=${TRUSTAGENT_BIN:-$TRUSTAGENT_HOME/bin}
TRUSTAGENT_ENV=${TRUSTAGENT_ENV:-$TRUSTAGENT_HOME/env.d}
TRUSTAGENT_VAR=${TRUSTAGENT_VAR:-$TRUSTAGENT_HOME/var}
TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-$TRUSTAGENT_HOME/logs}

###################################################################################################

# load linux utility
if [ -f "$TRUSTAGENT_HOME/share/scripts/functions.sh" ]; then
  . $TRUSTAGENT_HOME/share/scripts/functions.sh
fi

# stored master password
if [ -z "$TRUSTAGENT_PASSWORD" ] && [ -f $TRUSTAGENT_CONFIGURATION/.trustagent_password ]; then
  export TRUSTAGENT_PASSWORD=$(cat $TRUSTAGENT_CONFIGURATION/.trustagent_password)
fi

###################################################################################################

# all other variables with defaults
TRUSTAGENT_PID_FILE=$TRUSTAGENT_HOME/trustagent.pid
TRUSTAGENT_HTTP_LOG_FILE=$TRUSTAGENT_LOGS/http.log
TRUSTAGENT_AUTHORIZE_TASKS="download-mtwilson-tls-certificate download-mtwilson-privacy-ca-certificate download-mtwilson-saml-certificate request-endorsement-certificate request-aik-certificate"
TRUSTAGENT_TPM_TASKS="create-tpm-owner-secret create-tpm-srk-secret create-aik-secret take-ownership"
TRUSTAGENT_START_TASKS="create-keystore-password create-tls-keypair take-ownership"
#TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS="create-binding-key certify-binding-key create-signing-key certify-signing-key"
TRUSTAGENT_SETUP_TASKS="update-extensions-cache-file create-keystore-password create-tls-keypair create-admin-user $TRUSTAGENT_TPM_TASKS $TRUSTAGENT_AUTHORIZE_TASKS $TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS login-register"
# not including configure-from-environment because we are running it always before the user-chosen tasks
# not including register-tpm-password because we are prompting for it in the setup.sh
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS="-Dlogback.configurationFile=$TRUSTAGENT_CONFIGURATION/logback.xml -Dfs.name=trustagent"

###################################################################################################

# ensure that our commands can be found
export PATH=$TRUSTAGENT_BIN/bin:$PATH

# ensure that trousers (/usr/sbin) and tpm tools (/usr/local/sbin) are found
export PATH=$PATH:/usr/sbin:/usr/local/sbin

# java command
if [ -z "$JAVA_CMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD=$JAVA_HOME/bin/java
  else
    JAVA_CMD=`which java`
  fi
fi

if [ -z "$JAVA_CMD" ]; then
  echo_failure "Cannot find java binary from values in $TRUSTAGENT_ENV"
  exit -1
fi

###################################################################################################

# generated variables
JARS=$(ls -1 $TRUSTAGENT_JAVA/*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')

# the classpath is long and if we use the java -cp option we will not be
# able to see the full command line in ps because the output is normally
# truncated at 4096 characters. so we export the classpath to the environment
export CLASSPATH

###################################################################################################

# run a trustagent command
trustagent_run() {
  local args="$*"
  $JAVA_CMD $JAVA_OPTS com.intel.dcsg.cpg.console.Main $args
  return $?
}

# arguments are optional, if provided they are the names of the tasks to run, in order
trustagent_setup() {
  export HARDWARE_UUID=$(trustagent_system_info "dmidecode -s system-uuid")
  local tasklist="$*"
  if [ -z "$tasklist" ]; then
    tasklist=$TRUSTAGENT_SETUP_TASKS
  elif [ "$tasklist" == "--force" ]; then
    tasklist="$TRUSTAGENT_SETUP_TASKS --force"
  fi
  "$JAVA_CMD" $JAVA_OPTS com.intel.mtwilson.launcher.console.Main setup configure-from-environment $tasklist
  return $?
}

trustagent_authorize() {
  export HARDWARE_UUID=$(trustagent_system_info "dmidecode -s system-uuid")
  local authorize_vars="TPM_OWNER_SECRET TPM_SRK_SECRET MTWILSON_API_URL MTWILSON_API_USERNAME MTWILSON_API_PASSWORD MTWILSON_TLS_CERT_SHA256"
  local default_value
  for v in $authorize_vars
  do
    default_value=$(eval "echo \$$v")
    prompt_with_default $v "Required: $v" $default_value
  done
  export_vars $authorize_vars
  trustagent_setup --force $TRUSTAGENT_AUTHORIZE_TASKS
  return $?
}

trustagent_start() {

    # check if we're already running - don't start a second instance
    if trustagent_is_running; then
        echo "Trust Agent is running"
        return 0
    fi

    # regenerate Measurement log when trustagent is started
    rm -rf $TRUSTAGENT_HOME/var/measureLog.xml
    $TRUSTAGENT_HOME/bin/module_analysis.sh

    # check if we need to use authbind or if we can start java directly
    prog="$JAVA_CMD"
    if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ $(whoami) != "root" ] && [ -n "$(which authbind 2>/dev/null)" ]; then
      prog="authbind $JAVA_CMD"
      JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
    fi

    # the subshell allows the java process to have a reasonable current working
    # directory without affecting the user's working directory. 
    # the last background process pid $! must be stored from the subshell.
    (
      cd /opt/trustagent
      "$JAVA_CMD" $JAVA_OPTS com.intel.mtwilson.launcher.console.Main start-http-server >>$TRUSTAGENT_HTTP_LOG_FILE 2>&1 &
      echo $! > $TRUSTAGENT_PID_FILE
    )
    if trustagent_is_running; then
      echo_success "Started trust agent"
    else
      echo_failure "Failed to start trust agent"
    fi
}

# returns 0 if trust agent is running, 1 if not running
# side effects: sets TRUSTAGENT_PID if trust agent is running, or to empty otherwise
trustagent_is_running() {
  TRUSTAGENT_PID=
  if [ -f $TRUSTAGENT_PID_FILE ]; then
    TRUSTAGENT_PID=$(cat $TRUSTAGENT_PID_FILE)
    local is_running=`ps -eo pid | grep "^\s*${TRUSTAGENT_PID}$"`
    if [ -z "$is_running" ]; then
      # stale PID file
      TRUSTAGENT_PID=
    fi
  fi
  if [ -z "$TRUSTAGENT_PID" ]; then
    # check the process list just in case the pid file is stale
    TRUSTAGENT_PID=$(ps ww | grep -v grep | grep java | grep "com.intel.mtwilson.launcher.console.Main start-http-server" | awk '{ print $1 }')
  fi
  if [ -z "$TRUSTAGENT_PID" ]; then
    # trust agent is not running
    return 1
  fi
  # trust agent is running and TRUSTAGENT_PID is set
  return 0
}


trustagent_stop() {
  if trustagent_is_running; then
    kill -9 $TRUSTAGENT_PID
    if [ $? ]; then
      echo "Stopped trust agent"
      rm $TRUSTAGENT_PID_FILE
    else
      echo "Failed to stop trust agent"
    fi
  fi
}

vrtm_uninstall() {
  VRTM_UNINSTALL_SCRIPT="/opt/vrtm/bin/vrtm-uninstall.sh"
  if [ -f "$VRTM_UNINSTALL_SCRIPT" ]; then
    "$VRTM_UNINSTALL_SCRIPT"
  fi
}

tbootxm_uninstall() {
  TBOOTXM_UNINSTALL_SCRIPT="/opt/tbootxm/bin/tboot-xm-uninstall.sh"
  if [ -f "$TBOOTXM_UNINSTALL_SCRIPT" ]; then
    "$TBOOTXM_UNINSTALL_SCRIPT"
  fi
}

policyagent_uninstall() {
  POLICYAGENT_UNINSTALL_SCRIPT="/opt/policyagent/bin/policyagent.py"
  if [ -f "$POLICYAGENT_UNINSTALL_SCRIPT" ]; then
    "$POLICYAGENT_UNINSTALL_SCRIPT" uninstall
  fi
}

openstack_extensions_uninstall() {
  OPENSTACK_EXTENSIONS_UNINSTALL_SCRIPT="/opt/openstack-ext/bin/mtwilson-openstack-node-uninstall.sh"
  if [ -f "$OPENSTACK_EXTENSIONS_UNINSTALL_SCRIPT" ]; then
    "$OPENSTACK_EXTENSIONS_UNINSTALL_SCRIPT"
  fi
}

# backs up the configuration directory and removes all trustagent files,
# except for configuration files which are saved and restored
trustagent_uninstall() {
    datestr=`date +%Y-%m-%d.%H%M`
    mkdir -p /tmp/trustagent.configuration.$datestr
    chmod 500 /tmp/trustagent.configuration.$datestr
    cp -r /opt/trustagent/configuration/* /tmp/trustagent.configuration.$datestr
	rm -f /usr/local/bin/tagent
    if [ -n "$TRUSTAGENT_HOME" ] && [ -d "$TRUSTAGENT_HOME" ]; then
      rm -rf $TRUSTAGENT_HOME
    fi
    remove_startup_script tagent
    rm -rf /opt/tbootxm
}

# stops monit and removes its configuration
monit_uninstall() {
  echo "Stopping Monit service..."
  service monit stop &> /dev/null
  #if [ -f /etc/monit/monitrc ]; then
    ##remove monit config so when it starts back up it has nothing to monitor
    #rm -rf /etc/monit/monitrc
  #fi
  if [ -d /etc/monit/conf.d ]; then
    # remove only the trust agent monit config
    rm -f /etc/monit/conf.d/ta.monit*
  fi
}

print_help() {
    echo "Usage: $0 start|stop|authorize|start-http-server|version"
    echo "Usage: $0 setup [--force|--noexec] [task1 task2 ...]"
    echo "Available setup tasks:"
    echo configure-from-environment
    echo $TRUSTAGENT_SETUP_TASKS | tr ' ' '\n'
    echo register-tpm-password
}

# writes system information into files non-root trustagenta can read 
# (root access required to run)
trustagent_update_system_info() {
  if [ "$(whoami)" == "root" ]; then
    # user is root, so run the commands and cache the output
    mkdir -p $TRUSTAGENT_VAR/system-info
    dmidecode -s bios-vendor > $TRUSTAGENT_VAR/system-info/dmidecode.bios-vendor
    dmidecode -s bios-version > $TRUSTAGENT_VAR/system-info/dmidecode.bios-version
    dmidecode -s system-uuid > $TRUSTAGENT_VAR/system-info/dmidecode.system-uuid
    dmidecode --type processor > $TRUSTAGENT_VAR/system-info/dmidecode.processor
    lsb_release -a > $TRUSTAGENT_VAR/system-info/lsb_release
    virsh version > $TRUSTAGENT_VAR/system-info/virsh.version
    chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $TRUSTAGENT_VAR
  else
    echo_failure "Must run 'tagent update-system-info' as root"
  fi
}

trustagent_system_info_get() {
  local filename=$1
  if [ -f "$filename" ]; then
    cat $filename
  else
    echo_failure "File not found: $filename"
    echo "Run 'tagent update-system-info' as root to fix"
  fi
}

# uses only information cached by trustagent_update_system_info 
# (root access not required)
trustagent_system_info() {
    case "$*" in
      "dmidecode -s bios-vendor")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/dmidecode.bios-vendor
          ;;
      "dmidecode -s bios-version")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/dmidecode.bios-version
          ;;
      "dmidecode -s system-uuid")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/dmidecode.system-uuid
          ;;
      "dmidecode --type processor")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/dmidecode.processor
          ;;
      "lsb_release -a")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/lsb_release
          ;;
      "virsh version")
          trustagent_system_info_get $TRUSTAGENT_VAR/system-info/virsh.version
          ;;
      *)
          echo_failure "tagent system-info command not supported: $*"
          ;;
    esac
    return $?
}

trousers_detect_and_run() {
  # non-root users typically don't have /usr/sbin in their path, so append it
  # here;  does not affect root user who would have /usr/sbin earlier in the PATH,
  # and also this change only affects our process
  PATH=$PATH:/usr/sbin

  TPM_VERSION=`cat $TRUSTAGENT_CONFIGURATION/tpm-version`
  if [ "TPM_VERSION" == "1.2" ]; then 
    trousers=`which tcsd 2>/dev/null`
    if [ -z "$trousers" ]; then
      #echo_failure "trousers installation is required for trust agent to run successfully."
      echo "trousers is required for trust agent to run"
      exit -1
    else
      $trousers
    fi
  fi
}

###################################################################################################

# here we look for specific commands first that we will handle in the
# script, and anything else we send to the java application

case "$1" in
  help)
    print_help
    ;;
  start)
    # need to start trousers before we can run tagent
    trousers_detect_and_run

    # run setup before starting trust agent to allow taking ownership again if
    # the tpm has been cleared, or re-initializing the keystore if the server
    # ssl cert has changed and the user has updated the fingerprint in
    # the trustagent.properties file
    if trustagent_setup $TRUSTAGENT_START_TASKS; then
      trustagent_start
    fi
    ;;
  stop)
    trustagent_stop
    ;;
  restart)
    trousers_detect_and_run
    trustagent_stop
    if trustagent_setup $TRUSTAGENT_START_TASKS; then
      trustagent_start
    fi
    ;;
  authorize)
    trousers_detect_and_run
    if trustagent_authorize; then
      trustagent_stop
      trustagent_start
    fi
    ;;
  status)
    if trustagent_is_running; then
      echo "Trust agent is running"
      exit 0
    else
      echo "Trust agent is not running"
      exit 1
    fi
    ;;
  update-system-info)
    shift;
    trustagent_update_system_info $*
    ;;
  system-info)
    shift;
    trustagent_system_info $*
    ;;
  setup)
    trousers_detect_and_run
    shift
    trustagent_setup $*
    ;;
  localhost-integration)
    #shiro_localhost_integration "/opt/trustagent/configuration/shiro.ini"
    #/opt/trustagent/bin/tagent.sh restart
    if [ -f "/opt/trustagent/configuration/shiro-localhost.ini" ]; then
      mv /opt/trustagent/configuration/shiro.ini /opt/trustagent/configuration/shiro.ini.bkup 2>/dev/null
      mv /opt/trustagent/configuration/shiro-localhost.ini /opt/trustagent/configuration/shiro.ini 2>/dev/null
      /opt/trustagent/bin/tagent.sh restart
    fi
    ;;
  uninstall)
    trustagent_stop
    vrtm_uninstall
	tbootxm_uninstall
    policyagent_uninstall
    openstack_extensions_uninstall
    trustagent_uninstall
    groupdel trustagent > /dev/null 2>&1
    userdel trustagent > /dev/null 2>&1
    monit_uninstall
    ;;
  *)
    if [ -z "$*" ]; then
      print_help
    else
      #echo "args: $*"
      trousers_detect_and_run
      "$JAVA_CMD" $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $*
    fi
    ;;
esac


exit $?
