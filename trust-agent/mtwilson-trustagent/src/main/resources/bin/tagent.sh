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
TRUSTAGENT_SETUP_TASKS="create-keystore-password create-tls-keypair create-admin-user create-tpm-owner-secret create-aik-secret take-ownership download-mtwilson-tls-certificate download-mtwilson-privacy-ca-certificate request-endorsement-certificate request-aik-certificate"
# not including register-tpm-password because we are prompting for it in the setup.sh
# not including configure-from-environment because it's included automatically when running "tagent setup" (but not when running "tagent start")
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS="-Dlogback.configurationFile=$TRUSTAGENT_CONF/logback.xml -Dfs.name=trustagent"

# TODO: since we are setting TRUSTAGENT_HOME and TRUSTAGENT_CONF environment
#       variables,  we should NOT be passing -Dfs.name to the application 
#       since our environment variables should be adequate for pointing the
#       application to the correct locations

###################################################################################################

# load environment variables (these may override the defaults set above)
if [ -d $TRUSTAGENT_ENV ]; then
  TRUSTAGENT_ENV_FILES=$(ls -1 $TRUSTAGENT_ENV/*)
  for env_file in $TRUSTAGENT_ENV_FILES; do
    . $env_file
  done
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
  local tasklist="$*"
  if [ -z "$tasklist" ]; then
    tasklist=$TRUSTAGENT_SETUP_TASKS
  fi
  java $JAVA_OPTS com.intel.dcsg.cpg.console.Main setup $tasklist
  return $?
}

trustagent_start() {
    # the subshell allows the java process to have a reasonable current working
    # directory without affecting the user's working directory. 
    # the last background process pid $! must be stored from the subshell.
    (
      cd /opt/trustagent
      java $JAVA_OPTS com.intel.dcsg.cpg.console.Main start-http-server >/dev/null 2>&1 &
      echo $! > $TRUSTAGENT_PID_FILE
    )
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
    TRUSTAGENT_PID=$(ps ww | grep -v grep | grep java | grep "com.intel.dcsg.cpg.console.Main start-http-server" | awk '{ print $1 }')
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
}

# stops monit and removes its configuration
# TODO if we didn't install monit we should uninstall it
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

function print_help() {
    echo "Usage: $0 start|stop|authorize|start-http-server"
    # TODO:  add the "register" command when it's implemented
    echo "Usage: $0 setup [--force|--noexec] [task1 task2 ...]"
    echo "Available setup tasks:"
    echo $TRUSTAGENT_SETUP_TASKS | tr ' ' '\n'
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
    #trousers_detect
    trousers=`which tcsd 2>/dev/null`
    if [ -z "$trousers" ]; then
      #echo_failure "trousers installation is required for trust agent to run successfully."
      echo "trousers installation is required for trust agent to run successfully."
      exit -1
    else
      $trousers
    fi

    # run setup before starting trust agent to allow taking ownership again if
    # the tpm has been cleared, or re-initializing the keystore if the server
    # ssl cert has changed and the user has updated the fingerprint in
    # the trustagent.properties file
    if trustagent_setup; then
      trustagent_start
    fi
    ;;
  stop)
    trustagent_stop
    ;;
  status)
    if trustagent_is_running; then
      echo "Trust agent is running"
    else
      echo "Trust agent is not running"
    fi
    ;;
  setup)
    shift
    trustagent_setup configure-from-environment $*
    ;;
  uninstall)
    trustagent_stop
    trustagent_uninstall
    monit_uninstall
    ;;

  *)
    if [ -z "$*" ]; then
      print_help
    else
      #echo "args: $*"
      # TODO: check java version against JAVA_REQUIRED_VERSION and exit if not
      #       acceptable; requires the functions file / linux utilities which
      #       isn't integrated into the new trustagent installer yet.
      java $JAVA_OPTS com.intel.dcsg.cpg.console.Main $*
    fi
    ;;
esac


exit $?

# constants
script_name=tagent

TRUSTAGENT_CONF=/etc/intel/cloudsecurity
package_name=trustagent
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${TRUSTAGENT_CONF}/${package_name}.properties
package_env_filename=/root/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f "${package_dir}/functions" ]; then . "${package_dir}/functions"; else echo "Missing file: ${package_dir}/functions"; exit 1; fi
if [ -f "${package_dir}/version" ]; then . "${package_dir}/version"; else echo_warning "Missing file: ${package_dir}/version"; fi
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null


# the install file should define TRUST_AGENT_HOME
# the env file should define JAVA_HOME (like /usr/local/jdk1.7.0_51) and java  ($JAVA_HOME/bin/java)
function read_local_config() {
    shell_include_files "${package_env_filename}" "${package_install_filename}"

    serviceNameLo="tagent"                                  # service name with the first letter in lowercase
    serviceName="Trust Agent"                                    # service name
    serviceUser="root"                                      # OS user name for the service
    serviceGroup="root"                                    # OS group name for the service
    CLASSPATH=$TRUST_AGENT_HOME/lib/TrustAgent.jar
    serviceUserHome="/home/$serviceUser"                       # home directory of the service user
    serviceLogFile="/var/log/$serviceNameLo.log"               # log file for StdOut/StdErr
    maxShutdownTime=15                                         # maximum number of seconds to wait for the daemon to terminate normally
    pidFile="/var/run/$serviceNameLo.pid"                      # name of PID file (PID = process ID number)
    storePass="$TRUSTAGENT_KEYSTORE_PASS"   #`read_property_from_file trustagent.keystore.password "${package_config_filename}"`
    export TaKeyStorePassword=$storePass
    javaVMArgs="-Dfs.root=/opt/trustagent -Dfs.conf=/opt/trustagent/configuration -Djavax.net.ssl.trustStore=$TRUSTAGENT_CONF/trustagent.jks -Djavax.net.ssl.keyStore=$TRUSTAGENT_CONF/trustagent.jks -Djavax.net.ssl.keyStorePassword=env:TaKeyStorePassword -Dapp.path=$package_dir -Ddebug=true"  # arguments for Java launcher
    javaArgs="-classpath $CLASSPATH com.intel.mountwilson.trustagent.TASecureServer"  # arguments for Java launcher
    javaCommandLine="$java $javaVMArgs  $javaArgs"                       # command line to start the Java service application
    javaCommandLineKeyword="TrustAgent.jar"                   # a keyword that occurs on the commandline, used to detect an already running service process and to distinguish it from others
}

read_local_config



# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable {
   local filename="$1"
   touch $filename || return 1
   chgrp $serviceGroup $filename || return 1
   chmod g+w $filename || return 1
   return 0; }
 
# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if [ ! -e /proc/$pid ]; then return 1; fi
   return 0; }
 
# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
   local pid="$1"
#   if [ "$(ps -p $pid --no-headers -o comm)" != "$javaCommand" ]; then return 1; fi
#   grep -q --binary -F "$javaCommandLineKeyword" /proc/$pid/cmdline
#   if [ $? -ne 0 ]; then return 1; fi
   isTrustAgent=`ps -p $pid -o cmd | grep "$javaCommandLineKeyword" | wc -l`
   if [ "$isTrustAgent" == "0" ]; then return 1; fi
   return 0; }

# Returns 0 when the service is running and sets the variable $pid to the PID.
function getServicePID {
   if [ ! -f $pidFile ]; then return 1; fi
   pid="$(<$pidFile)"
   echo $pid 
   checkProcessIsRunning $pid || return 1
   checkProcessIsOurService $pid || return 1
   return 0; }
 
function startServiceProcess {
   cd $package_dir || return 1
   rm -f $pidFile
   read_local_config
   makeFileWritable $pidFile || return 1
   makeFileWritable $serviceLogFile || return 1
   cmd="nohup $javaCommandLine >>$serviceLogFile 2>&1 & echo \$! >$pidFile"

   su -m $serviceUser -s $SHELL -c "$cmd" || return 1
   sleep 0.1
   pid="$(<$pidFile)"
   if checkProcessIsRunning $pid; then :; else
      echo
      echo "$serviceName start failed, see logfile $serviceLogFile"
      return 1
   fi
   return 0; }
 
function stopServiceProcess {
   kill $pid || return 1
   for ((i=0; i<maxShutdownTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo "$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   kill -s KILL $pid || return 1
   local killWaitTime=15
   for ((i=0; i<killWaitTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo "Error: $serviceName could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
   return 1; }
 
function startService {
   tcsd
    if [ -z "$java" ]; then
        if [ ! -f "${package_env_filename}" ]; then
          setup_env > "${package_env_filename}"
        fi
        . "${package_env_filename}"
        read_local_config
    fi
    if no_java $JAVA_REQUIRED_VERSION; then echo "Cannot find Java $JAVA_REQUIRED_VERSION or later"; exit 1; fi

   # make sure we have files from privacy ca
   report_files_exist /etc/intel/cloudsecurity/PrivacyCA.cer /etc/intel/cloudsecurity/hisprovisioner.properties
   if [ $? -ne 0 ]; then
     echo_failure "Privacy CA files are missing"
     return 1
   fi

   # take ownership if not taken
   tpm_owned=`cat /sys/class/misc/tpm0/device/owned`
   if [[ "${tpm_owned}" == "0" ]]; then
     $java -cp $CLASSPATH -DforceCreateEk=true com.intel.mountwilson.trustagent.commands.TakeOwnershipCmd
   fi

   getServicePID
   if [ $? -eq 0 ]; then echo "$serviceName is already running"; RETVAL=0; return 0; fi
   echo -n "Starting $serviceName   "
   startServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
   echo "started PID=$pid"
   RETVAL=0
   return 0; }
 
function stopService {
   getServicePID
   if [ $? -ne 0 ]; then echo "$serviceName is not running"; RETVAL=0; echo ""; return 0; fi
   echo -n "Stopping $serviceName   "
   stopServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
   echo "stopped PID=$pid"
   RETVAL=0
   return 0; }
 
function checkServiceStatus {
    TRUST_AGENT_RUNNING=""
   echo -n "Checking for $serviceName:   "
   if getServicePID; then
    TRUST_AGENT_RUNNING="$pid"
    echo "Running (PID=$pid)"
    RETVAL=0
   else
    echo "Stopped"
    RETVAL=3
   fi
   return 0;
}

setup_env() {
  yum_detect > /dev/null
  java_detect > /dev/null
  local datestr=`date +%Y-%m-%d.%H%M`
  echo "# environment on ${datestr}"
  echo "JAVA_HOME=$JAVA_HOME"
  echo "java=$java"
}

setup() {
  # bug #492 if a jdk/jre was already configured, we need to leave it alone because it may have customized security policy to allow bouncycastle
  read_local_config
  if no_java $JAVA_REQUIRED_VERSION; then echo "Cannot find Java $JAVA_REQUIRED_VERSION or later"; exit 1; fi
  yum_detect > /dev/null
  # create the env file first because pcakey in the interactive install depends on it
  setup_env > "${package_env_filename}"
  . "${package_env_filename}"
  read_local_config
  setup_interactive_install
}
   
setup_print_summary() {
  echo "Requirements summary:"
  if [ -n "$JAVA_HOME" ]; then
    echo "Java: $JAVA_VERSION"
  else
    echo "Java: not found"
  fi
}


function main {
   RETVAL=0
   case "$1" in
      version)
        echo "${package_name}"
	echo "Version ${VERSION:-Unknown}"
	echo "Build ${BUILD:-Unknown}"
        ;;
      start)                                               # starts the Java program as a Linux service
         startService
         ;;
      stop)                                                # stops the Java program service
         stopService
         ;;
      restart)                                             # stops and restarts the service
         stopService && startService
         ;;
      status)                                              # displays the service status
         checkServiceStatus
         ;;
      setup)                                               # completes service setup
         setup
         ;;
  edit)
        update_property_in_file "${2}" "${package_config_filename}" "${3}"
       ;;
  show)
        read_property_from_file "${2}" "${package_config_filename}"
        ;;
  fixek)
        java_detect > /dev/null
        if no_java $JAVA_REQUIRED_VERSION; then echo "Cannot find Java $JAVA_REQUIRED_VERSION or later"; exit 1; fi
        stopService
        pcakey
        $java -cp $CLASSPATH -DforceCreateEk=true com.intel.mountwilson.trustagent.commands.TakeOwnershipCmd
        startService
        ;;
  newid)
        java_detect > /dev/null
        if no_java $JAVA_REQUIRED_VERSION; then echo "Cannot find Java $JAVA_REQUIRED_VERSION or later"; exit 1; fi
        stopService
        if [ -f "$TRUSTAGENT_CONF/cert/aikblob.dat" ]; then
          backup_file "$TRUSTAGENT_CONF/cert/aikblob.dat"
          rm "$TRUSTAGENT_CONF/cert/aikblob.dat"
        fi
        if [ -f "$TRUSTAGENT_CONF/cert/aikcert.pem" ]; then
          backup_file "$TRUSTAGENT_CONF/cert/aikcert.pem"
          rm "$TRUSTAGENT_CONF/cert/aikcert.pem"
        fi
        pcakey
        $java -cp $CLASSPATH $javaVMArgs -DforceCreateEk=true com.intel.mountwilson.trustagent.commands.CreateIdentityCmd
        startService
        ;;
  tpmstatus)
        tpm_owned=`cat /sys/class/misc/tpm0/device/owned`
        tpm_enabled=`cat /sys/class/misc/tpm0/device/enabled`
        tpm_pcrs=`cat /sys/class/misc/tpm0/device/pcrs`
        if [[ "${tpm_owned}" == "1" ]]; then
          echo_success "TPM is owned"
        else
          echo_warning "TPM is not owned"
        fi
        if [[ "${tpm_enabled}" == "1" ]]; then
          echo_success "TPM is enabled"
        else
          echo_warning "TPM is not enabled"
        fi
        echo "${tpm_pcrs}"
        ;;
  uninstall)
        stopService
        datestr=`date +%Y-%m-%d.%H%M`
        mkdir -p "${TRUSTAGENT_CONF}"
        cp "${package_config_filename}" "${TRUSTAGENT_CONF}"/${package_name}.properties.${datestr}
        echo "Saved configuration file in ${TRUSTAGENT_CONF}/${package_name}.properties.${datestr}"
        # prevent disaster by ensuring that package_dir is inside /opt/intel
        if [[ "${package_dir}" == /opt/intel/* ]]; then
          rm -rf "${package_dir}"
        fi
	rm /usr/local/bin/${script_name}
        rm -fr /etc/monit/monitrc
	echo "Stopping Monit service..."
	service monit stop &> /dev/null
	if [ -f /etc/monit/monitrc ]; then
	 #remove monit config so when it starts back up it has nothing to monitor
	 # idealy here we should be uninstalling instead
	 rm -fr /etc/monit/monitrc
        fi
        ;;
      *)
         echo "Usage: $0 {version|restart|setup|edit|show|fixek|newid|tpmstatus|uninstall}"
         exit 1
         ;;
      esac
   exit $RETVAL
}
 
main $1
