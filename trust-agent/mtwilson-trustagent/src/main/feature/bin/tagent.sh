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

TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-/opt/trustagent}
TRUSTAGENT_CONF=${TRUSTAGENT_CONF:-/opt/trustagent/configuration}
TRUSTAGENT_JAVA=${TRUSTAGENT_JAVA:-/opt/trustagent/java}
TRUSTAGENT_BIN=${TRUSTAGENT_BIN:-/opt/trustagent/bin}
TRUSTAGENT_ENV=${TRUSTAGENT_ENV:-/opt/trustagent/env.d}
TRUSTAGENT_VAR=${TRUSTAGENT_VAR:-/opt/trustagent/var}
TRUSTAGENT_PID_FILE=/var/run/trustagent.pid
TRUSTAGENT_HTTP_LOG_FILE=/var/log/trustagent/http.log
TRUSTAGENT_AUTHORIZE_TASKS="download-mtwilson-tls-certificate download-mtwilson-privacy-ca-certificate download-mtwilson-saml-certificate request-endorsement-certificate request-aik-certificate"
TRUSTAGENT_TPM_TASKS="create-tpm-owner-secret create-tpm-srk-secret create-aik-secret take-ownership"
TRUSTAGENT_START_TASKS="create-keystore-password create-tls-keypair create-admin-user take-ownership"
TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS="create-binding-key certify-binding-key create-signing-key certify-signing-key"
TRUSTAGENT_SETUP_TASKS="update-extensions-cache-file create-keystore-password create-tls-keypair create-admin-user $TRUSTAGENT_TPM_TASKS $TRUSTAGENT_AUTHORIZE_TASKS $TRUSTAGENT_VM_ATTESTATION_SETUP_TASKS"
# not including configure-from-environment because we are running it always before the user-chosen tasks
# not including register-tpm-password because we are prompting for it in the setup.sh
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS="-Dlogback.configurationFile=$TRUSTAGENT_CONF/logback.xml -Dfs.name=trustagent"

###################################################################################################

# load environment variables (these may override the defaults set above)
if [ -d $TRUSTAGENT_ENV ]; then
  TRUSTAGENT_ENV_FILES=$(ls -1 $TRUSTAGENT_ENV/*)
  for env_file in $TRUSTAGENT_ENV_FILES; do
    . $env_file
  done
fi

# load linux utility
if [ -f "$TRUSTAGENT_HOME/linux-util/functions" ]; then
  . $TRUSTAGENT_HOME/linux-util/functions
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

# arguments are optional, if provided they are the names of the tasks to run, in order
trustagent_setup() {
  export HARDWARE_UUID=`dmidecode |grep UUID | awk '{print $2}'`
  local tasklist="$*"
  if [ -z "$tasklist" ]; then
    tasklist=$TRUSTAGENT_SETUP_TASKS
  elif [ "$tasklist" == "--force" ]; then
    tasklist="$TRUSTAGENT_SETUP_TASKS --force"
  fi
  java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main setup configure-from-environment $tasklist
  return $?
}

trustagent_authorize() {
  export HARDWARE_UUID=`dmidecode |grep UUID | awk '{print $2}'`
  local authorize_vars="TPM_OWNER_SECRET TPM_SRK_SECRET MTWILSON_API_URL MTWILSON_API_USERNAME MTWILSON_API_PASSWORD MTWILSON_TLS_CERT_SHA1"
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
    # the subshell allows the java process to have a reasonable current working
    # directory without affecting the user's working directory. 
    # the last background process pid $! must be stored from the subshell.
    (
      cd /opt/trustagent
      java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main start-http-server >>$TRUSTAGENT_HTTP_LOG_FILE 2>&1 &
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

# backs up the configuration directory and removes all trustagent files
trustagent_uninstall() {
    datestr=`date +%Y-%m-%d.%H%M`
    mkdir -p /var/backup/trustagent.configuration.$datestr
    cp -r /opt/trustagent/configuration/* /var/backup/trustagent.configuration.$datestr
	rm /usr/local/bin/tagent
    rm -rf /opt/trustagent
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

trousers_detect_and_run() {
  trousers=`which tcsd 2>/dev/null`
  if [ -z "$trousers" ]; then
    #echo_failure "trousers installation is required for trust agent to run successfully."
    echo "trousers is required for trust agent to run"
    exit -1
  else
    $trousers
  fi
}
trousers_detect_and_run
###################################################################################################

# here we look for specific commands first that we will handle in the
# script, and anything else we send to the java application

case "$1" in
  help)
    print_help
    ;;
  start)
    # need to start trousers before we can run tagent
    #trousers_detect
    #trousers_detect_and_run

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
    trustagent_stop
    if trustagent_setup $TRUSTAGENT_START_TASKS; then
      trustagent_start
    fi
    ;;
  authorize)
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
  setup)
    trousers_detect_and_run
    shift
    trustagent_setup $*
    ;;
  uninstall)
    trustagent_stop
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
      java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $*
    fi
    ;;
esac


exit $?
