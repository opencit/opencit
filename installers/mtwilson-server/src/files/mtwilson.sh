#!/bin/bash

### BEGIN INIT INFO
# Provides:          mtwilson
# Required-Start:    $local_fs $network $remote_fs
# Required-Stop:     $local_fs $network $remote_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start mtwilson webserver
# Description:       Enable mtwilson services
### END INIT INFO

# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions


# the home directory must be defined before we load any environment or
# configuration files; it is explicitly passed through the sudo command
export MTWILSON_HOME=${MTWILSON_HOME:-/opt/mtwilson}

# the env directory is not configurable; it is defined as KMS_HOME/env and the
# administrator may use a symlink if necessary to place it anywhere else
export MTWILSON_ENV=$MTWILSON_HOME/env.d

mtw_load_env() {
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

if [ -z "$MTWILSON_USERNAME" ]; then
  mtw_load_env $MTWILSON_HOME/env.d/mtwilson-username
fi

export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}

if [ -d $MTWILSON_ENV ]; then
   mtw_load_env $(ls -1 $MTWILSON_ENV/*)
fi

###################################################################################################

# if non-root execution is specified, and we are currently root, start over; the MTW_SUDO variable limits this to one attempt
# we make an exception for the uninstall command, which may require root access to delete users and certain directories
if [ -n "$MTWILSON_USERNAME" ] && [ "$MTWILSON_USERNAME" != "root" ] && [ $(whoami) == "root" ] && [ -z "$MTWILSON_SUDO" ] && [ "$1" != "uninstall" ]; then
  export MTWILSON_SUDO=true
  sudo -u $MTWILSON_USERNAME -H -E "$MTWILSON_BIN/mtwilson" $*
  exit $?
fi

###################################################################################################

# default directory layout follows the 'home' style
export MTWILSON_CONFIGURATION=${MTWILSON_CONFIGURATION:-${MTWILSON_CONF:-$MTWILSON_HOME/configuration}}
export MTWILSON_JAVA=${MTWILSON_JAVA:-$MTWILSON_HOME/java}
export MTWILSON_BIN=${MTWILSON_BIN:-$MTWILSON_HOME/bin}
export MTWILSON_REPOSITORY=${MTWILSON_REPOSITORY:-$MTWILSON_HOME/repository}
export MTWILSON_LOGS=${MTWILSON_LOGS:-$MTWILSON_HOME/logs}

# stored master password
if [ -z "$MTWILSON_PASSWORD" ] && [ -f $MTWILSON_CONFIGURATION/.mtwilson_password ]; then
  export MTWILSON_PASSWORD=$(cat $MTWILSON_CONFIGURATION/.mtwilson_password)
fi

# SCRIPT CONFIGURATION:
#share_dir=/usr/local/share/mtwilson/util
#setupconsole_dir=/opt/intel/cloudsecurity/setup-console
apiclient_dir=/opt/mtwilson/share/apiclient
setupconsole_dir=/opt/mtwilson/java
apiclient_java=${apiclient_dir}/java
env_dir=/opt/mtwilson/env.d
conf_dir=/etc/intel/cloudsecurity
pid_dir=/var/run/mtwilson
#apiclient_shell=${apiclient_dir}/shell
#mysql_required_version=5.0
#glassfish_required_version=4.0
#java_required_version=1.7.0_51    
MTWILSON_PID_FILE="" 
if [ -z $MTWILSON_PID_FILE ]; then
    if [ -d $pid_dir ] && [ -w $pid_dir ]; then
        MTWILSON_PID_FILE=$pid_dir/mtwilson.pid
        MTWILSON_PID_WAIT_FILE=${MTWILSON_PID_FILE}.wait
	#elif [ -f ./mtwilson.env ]; then
	      #Look up in the mtwilson.env file
   	#      MTWILSON_PID_FILE=$(cat ~/mtwilson.env | grep -E 'MTWILSON_PID_FILE' | cut -d = -f 2)
	#      MTWILSON_PID_WAIT_FILE=${MTWILSON_PID_FILE}.wait
	else
        #create a directory in MTWILSON_HOME/var/run/
        if [ ! -d $MTWILSON_HOME/var/run ]; then
	        mkdir -p $MTWILSON_HOME/var/run
         fi
            MTWILSON_PID_FILE=$MTWILSON_HOME/var/run/mtwilson.pid
            MTWILSON_PID_WAIT_FILE=${MTWILSON_PID_FILE}.wait
	fi
fi		

# FUNCTION LIBRARY and VERSION INFORMATION
if [ -f /opt/mtwilson/share/scripts/functions ]; then  . /opt/mtwilson/share/scripts/functions; else echo "Missing file: /opt/mtwilson/share/scripts/functions";   exit 1; fi
if [ -f /opt/mtwilson/configuration/version ]; then  . /opt/mtwilson/configuration/version; else  echo_warning "Missing file: /opt/mtwilson/configuration/version"; fi
shell_include_files ${env_dir}/*
if [[ "$@" != *"ExportConfig"* ]]; then   # NEED TO DEBUG FURTHER. load_conf runs ExportConfig and if that same command is passed in from 'mtwilson setup', it won't work
  load_conf 2>&1 >/dev/null
  if [ $? -ne 0 ]; then
    if [ $? -eq 2 ]; then echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."; fi
    exit -1
  fi
fi
load_defaults 2>&1 >/dev/null
#if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi

# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default
#export DATABASE_VENDOR=${DATABASE_VENDOR:-postgres}
#export WEBSERVICE_VENDOR=${WEBSERVICE_VENDOR:-glassfish}

if using_mysql; then
    export mysql_required_version=${MYSQL_REQUIRED_VERSION:-5.0}
elif using_postgres; then
    export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-9.3}
fi
if using_glassfish; then
    export GLASSFISH_REQUIRED_VERSION=${GLASSFISH_REQUIRED_VERSION:-4.0}
elif using_tomcat; then
    export tomcat_required_version=${TOMCAT_REQUIRED_VERSION:-7.0}
fi
export JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7.0_51}
export java_required_version=${JAVA_REQUIRED_VERSION}

call_apiclient() {
  if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
  APICLIENT_JARS=$(JARS=(${apiclient_java}/*.jar); IFS=:; echo "${JARS[*]}")
  mainclass=com.intel.mtwilson.client.TextConsole
  $java -cp "$APICLIENT_JARS" -Dlogback.configurationFile=${conf_dir}/logback.xml $mainclass $@
}

find_ctl_commands() {
  pcactl=`which pcactl 2>/dev/null`
  msctl=`which msctl 2>/dev/null`
  wlmctl=`which wlmctl 2>/dev/null`
  asctl=`which asctl 2>/dev/null`
  mpctl=`which mtwilson-portal 2>/dev/null`
}

mtwilson_saml_cert_report() {
  local keystore="$SAML_KEYSTORE_FILE"   #`read_property_from_file saml.keystore.file ${conf_dir}/attestation-service.properties`
  local keystorePassword=$SAML_KEYSTORE_PASSWORD   #`read_property_from_file saml.keystore.password ${conf_dir}/attestation-service.properties`
  local keyalias=$SAML_KEY_ALIAS   #`read_property_from_file saml.key.alias ${conf_dir}/attestation-service.properties`
  java_keystore_cert_report "${conf_dir}/$keystore" "$keystorePassword" "$keyalias"
}


# called by installer to automatically configure the server for localhost integration,
# if the customer has enabled this option
# arguments:   currently supports just one optional argument, IP address or hostname, to enable as trusted IN ADDITION TO 127.0.0.1
mtwilson_localhost_integration() {
  local iplist;
  local finalIps;
  if [ -n "$2" ]; then
    iplist="127.0.0.1,$2"
  else
    iplist="127.0.0.1"
  fi
#  update_property_in_file mtwilson.api.trust /etc/intel/cloudsecurity/management-service.properties "$iplist"
#  update_property_in_file mtwilson.ssl.required /etc/intel/cloudsecurity/management-service.properties "false"

  OIFS=$IFS
  IFS=',' read -ra newIps <<< "$iplist"
  IFS=$OIFS

  hostAllowPropertyName=iniHostRealm.allow
  sed -i '/'"$hostAllowPropertyName"'/ s/^#//g' /opt/mtwilson/configuration/shiro.ini
  hostAllow=`read_property_from_file $hostAllowPropertyName /opt/mtwilson/configuration/shiro.ini`
  finalIps="$hostAllow"
  if [ -z "$hostAllow" ]; then
    iniHostRealmValue=`read_property_from_file iniHostRealm /opt/mtwilson/configuration/shiro.ini`
    update_property_in_file iniHostRealm /opt/mtwilson/configuration/shiro.ini "$iniHostRealmValue\n$hostAllowPropertyName="
  fi
  for i in "${newIps[@]}"; do
    OIFS=$IFS
    IFS=',' read -ra oldIps <<< "$finalIps"
    IFS=$OIFS
    if [[ "${oldIps[*]}" != *"$i"* ]]; then
      if [ -z "$finalIps" ]; then
        finalIps="$i"
      else
        finalIps+=",$i"
      fi
    fi
  done
  update_property_in_file "$hostAllowPropertyName" /opt/mtwilson/configuration/shiro.ini "$finalIps";
}


setup() {
  #java_detect; java_env_report > ${env_dir}/java
  #if using_glassfish; then
  #  glassfish_detect; glassfish_env_report > ${env_dir}/glassfish
  #elif using_tomcat; then
  #  tomcat_detect; tomcat_env_report > ${env_dir}/tomcat
  #fi
  find_ctl_commands

  # Set the "setup" flag so that service setup commands to not attempt to re-deploy their application (in order to preserve any customized version of the app that has been deployed to glassfish directly)
  export MTWILSON_SETUP_NODEPLOY=1

  # Gather default configuration
  MTWILSON_SERVER_IP_ADDRESS=${MTWILSON_SERVER_IP_ADDRESS:-$(hostaddress)}
  MTWILSON_SERVER=${MTWILSON_SERVER:-$MTWILSON_SERVER_IP_ADDRESS}
  GLASSFISH_SSL_CERT_CN=${GLASSFISH_SSL_CERT_CN:-"$MTWILSON_SERVER"}
  TOMCAT_SSL_CERT_CN=${TOMCAT_SSL_CERT_CN:-"$MTWILSON_SERVER"}
  
  # Prompt for installation settings
  echo "Please enter the IP Address or Hostname that will identify the Mt Wilson server.
This address will be used in the server SSL certificate and in all Mt Wilson URLs,
such as https://${MTWILSON_SERVER:-127.0.0.1}.
Detected the following options on this server:"
  IFS=$'\n'; echo "$(hostaddress_list)"; IFS=' '; hostname;
  prompt_with_default MTWILSON_SERVER "Mt Wilson Server:"
  prompt_with_default MC_FIRST_USERNAME "Username:" "admin"
  prompt_with_default_password MC_FIRST_PASSWORD
  export MTWILSON_SERVER
  export MC_FIRST_USERNAME
  export MC_FIRST_PASSWORD
  echo
  if using_mysql; then
    mysql_userinput_connection_properties
    export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD
  elif using_postgres; then
    postgres_userinput_connection_properties
    export POSTGRES_HOSTNAME POSTGRES_PORTNUM POSTGRES_DATABASE POSTGRES_USERNAME POSTGRES_PASSWORD
    if [ "$POSTGRES_HOSTNAME" == "127.0.0.1" ] || [ "$POSTGRES_HOSTNAME" == "localhost" ]; then
      PGPASS_HOSTNAME=localhost
    else
      PGPASS_HOSTNAME="$POSTGRES_HOSTNAME"
    fi
    echo "$POSTGRES_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" > ~/.pgpass
    echo "$PGPASS_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" >> ~/.pgpass
    chmod 0600 ~/.pgpass
  fi

  # Attestation service auto-configuration
  export PRIVACYCA_SERVER=${MTWILSON_SERVER}

  if using_glassfish; then
    if [ -n "${GLASSFISH_SSL_CERT_CN}" ]; then
      glassfish_create_ssl_cert "${GLASSFISH_SSL_CERT_CN}"
    else
      glassfish_create_ssl_cert_prompt
    fi
  elif using_tomcat; then
    if [ -n "${TOMCAT_SSL_CERT_CN}" ]; then
      tomcat_create_ssl_cert "${TOMCAT_SSL_CERT_CN}"
    else
      tomcat_create_ssl_cert_prompt
    fi
  fi

  # new setup commands in mtwilson 2.0
  # mtwilson setup setup-manager update-extensions-cache-file
  # mtwilson setup setup-manager create-admin-user
  call_tag_setupcommand setup-manager update-extensions-cache-file --force --no-ext-cache
  # NOTE:  in order to run create-admin-user successfully you MUST have
  #        the environment variable MC_FIRST_PASSWORD defined; this is already
  #        done when running from the installer but if user runs 'mtwilson setup'
  #        outside the installer the may have to export MC_FIRST_PASSWORD first
  echo "mtwilson setup tasks: create-certificate-authority-key create-admin-user..." >>$INSTALL_LOG_FILE
  call_tag_setupcommand setup-manager create-certificate-authority-key create-admin-user

  call_setupcommand EncryptDatabase

  # setup web services:
  if [ -n "$pcactl" ]; then $pcactl setup; $pcactl restart; fi
  if [ -n "$asctl" ]; then $asctl setup; fi
  if [ -n "$msctl" ]; then $msctl setup; fi
  if [ -n "$wlmctl" ]; then $wlmctl setup; fi
  if [ -n "$mpctl" ]; then $mpctl setup; fi

  # java setup tool - right now just checks database encryption, in the future it will take over some of the setup functions from the *ctl scripts which can be done in java
  shift
}

all_status() {
  if using_glassfish; then
    glassfish_clear
    #glassfish_detect > /dev/null
  elif using_tomcat; then
    tomcat_clear
    #tomcat_detect > /dev/null
  fi

  if using_glassfish; then
    glassfish_running_report
  elif using_tomcat; then
    tomcat_running_report
  fi
  
  find_ctl_commands
  if [ -n "$pcactl" ]; then $pcactl status; fi
  if [ -n "$asctl" ]; then $asctl status; fi
  if [ -n "$mpctl" ]; then $mpctl status; fi
}

setup_env() {
  local datestr=`date +%Y-%m-%d.%H%M`
  echo "# environment on ${datestr}"
  echo "MTWILSON_HOME=$MTWILSON_HOME"
  echo "MTWILSON_USERNAME=$MTWILSON_USERNAME"
#  java_detect > /dev/null
#  echo "JAVA_HOME=$JAVA_HOME"
#  echo "java_bindir=$java_bindir"
#  echo "java=$java"
  #export JAVA_HOME java_bindir java
  if using_mysql; then
    mysql_detect > /dev/null
    echo "mysql=$mysql"
    #export mysql
  elif using_postgres; then
    postgres_detect > /dev/null
    #echo "postgres=$psql"
    #the actuall veriable for postgres commands is psql
    echo "psql=$psql"
  fi
  echo "WEBSERVICE_VENDOR=$WEBSERVICE_VENDOR"
  echo "DATABASE_VENDOR=$DATABASE_VENDOR"
  if using_glassfish; then
    glassfish_detect > /dev/null
    echo "GLASSFISH_HOME=$GLASSFISH_HOME"
    echo "glassfish_bin=$glassfish_bin"
    echo "glassfish=\"$glassfish\""
    #export GLASSFISH_HOME glassfish_bin glassfish
  elif using_tomcat; then
    tomcat_detect > /dev/null
    echo "TOMCAT_HOME=$TOMCAT_HOME"
    echo "tomcat_bin=$tomcat_bin"
    echo "tomcat=\"$tomcat\""
  fi
  echo "MTWILSON_SERVER=$MTWILSON_SERVER"
  echo "DEFAULT_API_PORT=$DEFAULT_API_PORT"
}

print_help() {
        echo -e "Usage: mtwilson {change-db-pass|erase-data|erase-users|fingerprint|help|\n" \
          "\t\tglassfish-detect|glassfish-enable-logging|glassfish-sslcert|glassfish-status|\n" \
          "\t\tjava-detect|mysql-detect|mysql-sslcert|tomcat-detect|tomcat-sslcert|tomcat-status|\n" \
          "\t\tkey-backup|key-restore|restart|setup|start|status|stop|uninstall|version|zeroize}"
}

RETVAL=0

# See how we were called.
case "$1" in
  version)
        echo "MtWilson Linux Utility"
        echo "Version ${VERSION}"
        echo "Build ${BUILD}"
        ;;
  setup-env)
        setup_env
        ;;
  start)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        if [ -f $MTWILSON_PID_WAIT_FILE ]; then
          any_mtwilson_pid=`ps gauxww | grep mtwilson | grep -v grep | awk '{ print $2 }' | tr [:space:] ' ' | sed -e 's/ *$//g'`
          if [ -n "$any_mtwilson_pid" ] && [ "$2" != "--force" ]; then
            # if the mtwilson.pid.wait file was touched less than 2 minutes ago, assume there is something in progress:
            if test `find $MTWILSON_PID_WAIT_FILE -mmin -2`; then
              echo "Mt Wilson may already be launching [PID: $any_mtwilson_pid]"
              echo "Use 'mtwilson start --force' to launch anyway"
              exit 1
            fi
            # otherwise if the mtwilson.pid.wait file is more than 2 minutes old, we ignore it and continue
          fi
        fi
        touch $MTWILSON_PID_WAIT_FILE
        if using_glassfish; then
          glassfish_start
          if [ -n "$GLASSFISH_PID" ]; then
            echo $GLASSFISH_PID > $MTWILSON_PID_FILE
          fi
        elif using_tomcat; then
          tomcat_start
          if [ -n "$TOMCAT_PID" ]; then
            echo $TOMCAT_PID > $MTWILSON_PID_FILE
          fi
        elif using_jetty; then
          jetty_start
          if [ -n "$JETTY_PID" ]; then
            echo $JETTY_PID > $MTWILSON_PID_FILE
          fi
        fi
        if [ -f $MTWILSON_PID_WAIT_FILE ]; then rm $MTWILSON_PID_WAIT_FILE; fi
        ;;
  stop)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        touch $MTWILSON_PID_WAIT_FILE
        if using_glassfish; then
          glassfish_stop
          #glassfish_shutdown
          if [ -f $MTWILSON_PID_FILE ]; then
            rm -f $MTWILSON_PID_FILE
          fi
        elif using_tomcat; then
          tomcat_stop
          #tomcat_shutdown
          if [ -f $MTWILSON_PID_FILE ]; then
            rm -f $MTWILSON_PID_FILE
          fi
        fi
        if [ -f $MTWILSON_PID_WAIT_FILE ]; then rm $MTWILSON_PID_WAIT_FILE; fi
        ;;
  restart)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        if [ -f $MTWILSON_PID_WAIT_FILE ]; then
          any_mtwilson_pid=`ps gauxww | grep mtwilson | grep -v grep | awk '{ print $2 }' | tr [:space:] ' ' | sed -e 's/ *$//g'`
          if [ -n "$any_mtwilson_pid" ] && [ "$2" != "--force" ]; then
            # if the mtwilson.pid.wait file was touched less than 2 minutes ago, assume there is something in progress:
            if test `find $MTWILSON_PID_WAIT_FILE -mmin -2`; then
              echo "Mt Wilson may already be launching [PID: $any_mtwilson_pid]"
              echo "Use 'mtwilson start --force' to launch anyway"
              exit 1
            fi
            # otherwise if the mtwilson.pid.wait file is more than 2 minutes old, we ignore it and continue
          fi
        fi
        touch $MTWILSON_PID_WAIT_FILE
        if using_glassfish; then
          glassfish_restart
          if [ -n "$GLASSFISH_PID" ]; then
            echo $GLASSFISH_PID > $MTWILSON_PID_FILE
          fi
        elif using_tomcat; then
          tomcat_restart
          if [ -n "$TOMCAT_PID" ]; then
            echo $TOMCAT_PID > $MTWILSON_PID_FILE
          fi
        fi
        if [ -f $MTWILSON_PID_WAIT_FILE ]; then rm $MTWILSON_PID_WAIT_FILE; fi
        ;;
  glassfish-detect)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        glassfish_detect ${2:-$GLASSFISH_REQUIRED_VERSION}
        glassfish_env_report
        ;;
  glassfish-start)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        glassfish_require
        glassfish_start_report
        ;;
  glassfish-stop)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        glassfish_require
        glassfish_shutdown
        ;;
  glassfish-restart)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        glassfish_require
        glassfish_restart
        ;;
  glassfish-status)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        glassfish_require
        glassfish_running_report
        glassfish_sslcert_report
        ;;
  glassfish-sslcert)
        #echo_warning "This feature has been disabled: mtwilson glassfish-sslcert"
        glassfish_create_ssl_cert_prompt
        ;;
  glassfish-enable-logging)
        glassfish_enable_logging
        ;;
  tomcat-detect)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        tomcat_detect ${2:-$TOMCAT_REQUIRED_VERSION}
        tomcat_env_report
        ;;
  tomcat-start)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        tomcat_require
        tomcat_start_report
        ;;
  tomcat-stop)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        tomcat_require
        tomcat_shutdown
        ;;
  tomcat-restart)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        tomcat_require
        tomcat_restart
        ;;
  tomcat-status)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        tomcat_require
        tomcat_running_report
        tomcat_sslcert_report
        ;;
  tomcat-sslcert)
        tomcat_create_ssl_cert_prompt
        ;;  
  mysql-detect)
        mysql_detect $2
        mysql_env_report
        ;;
  mysql-sslcert)
        mysql_create_ca
        mysql_create_ssl "MySQL Server"
        mysql_create_ssl "Attestation Service"
        mysql_create_ssl "Management Service"
        mysql_create_ssl "Whitelist Service"
        mysql_create_ssl "Trust Dashboard"
        ;;
  java-detect)
        java_detect $2
        java_env_report
        ;;
  localhost-integration)
        #mtwilson_localhost_integration $@
        shiro_localhost_integration "/opt/mtwilson/configuration/shiro.ini"
        /opt/mtwilson/bin/mtwilson.sh restart
        ;;
  #api)
  #      shift
  #      call_apiclient $@
  #      ;;
  setup)
        if [ $# -gt 1 ]; then
          shift
          # old setup commands are here
          # new setup commands invoked via "mtwilson setup-manager <command>" which is handled by the *) case at the bottom of this script
          call_setupcommand $@
        else
          #if [ -f $MTWILSON_HOME/mtwilson.env ]; then  . $MTWILSON_HOME/mtwilson.env; fi
          setup
        fi
        ;;
  fingerprint)
        # show server ssh fingerprint, glassfish ssl fingerprint, and saml cert fingerprint
        echo "== SSH HOST KEYS =="
        ssh_fingerprints
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        if using_glassfish; then
          glassfish_require
          echo "== GLASSFISH SSL CERTIFICATE =="
          glassfish_sslcert_report
        fi
        echo "== MT WILSON SAML CERTIFICATE =="
        mtwilson_saml_cert_report
        ;;
  status)
        all_status
        ;;
  erase-data)
        #load_default_env 1>/dev/null
        erase_data
        if [ $? -ne 0 ]; then exit 1; fi
        #call_setupcommand EraseLogs
        #call_setupcommand EraseHostRegistrationData
        #call_setupcommand EraseWhitelistData        
        ;;
  erase-users)
        #load_default_env 1>/dev/null
        ### the EraseUserAccounts command now detects superusers automatically by looking for users with permission *:*:* 
        #if [[ -z "$MC_FIRST_USERNAME" && "$@" != *"--all"* && "$@" != *"--user"* ]]; then
        #  echo_warning "Please specify the admin username."
        #  prompt_with_default MC_FIRST_USERNAME "Mt Wilson Portal Admin Username:" "admin"
        #fi
        #export MC_FIRST_USERNAME=$MC_FIRST_USERNAME
        call_setupcommand EraseUserAccounts $@
        ;;
  zeroize)
        configDir="/opt/mtwilson/configuration"
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        if using_glassfish; then
          glassfish_require
          glassfish_async_stop
        elif using_tomcat; then
          tomcat_require
          tomcat_async_stop
        fi
        echo "Removing Mt Wilson configuration in $configDir..."
        cd /tmp && find "$configDir/" -type f -exec shred -uzn 3 {} \;
        ;;
  key-backup)
        key_backup $@
        ;;
  key-restore)
        key_restore $@
        ;;
  change-db-pass)
        if [ $# -ne 1 ]; then
          echo_failure "Usage: $0 $1"; exit -1;
        fi
        change_db_pass
        ;;
  uninstall)
        if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi
        #load_default_env 1>/dev/null
        if using_glassfish; then
          glassfish_require
          echo "Stopping Glassfish..."
          glassfish_shutdown
          #rm -rf /usr/share/glassfish4
        elif using_tomcat; then
          tomcat_require
          echo "Stopping Tomcat..."
          tomcat_shutdown
          #rm -rf "$TOMCAT_HOME"
        fi
        # application files
        echo "Removing mtwilson applications from webserver..."
        webservice_uninstall HisPrivacyCAWebServices2
        webservice_uninstall mtwilson-portal
        webservice_uninstall mtwilson
        webservice_uninstall AttestationService 2>&1 > /dev/null
        webservice_uninstall ManagementService 2>&1 > /dev/null
        webservice_uninstall WLMService 2>&1 > /dev/null

        echo "Removing Mt Wilson applications in /opt/intel/cloudsecurity, /etc/intel/cloudsecurity, /opt/mtwilson..."
        if [ -w "/opt/intel/cloudsecurity" ] && [ -w "/opt/mtwilson" ] && [ "$(whoami)" == "root" ]; then
            rm -rf /opt/intel/cloudsecurity
            rm -rf /opt/mtwilson
            rm -rf /etc/intel/cloudsecurity
            if [ -d /var/opt/intel ]; then
                rm -rf /var/opt/intel
            fi
            echo "Removing Mt Wilson utilities in /usr/local/share/mtwilson..."
            # rm -rf /usr/local/share/mtwilson
            remove_startup_script "mtwilson"
        else
          echo_warning "You must be root to remove directories: /etc/intel/cloudsecurity, /opt/intel/cloudsecurity, /opt/mtwilson"
          echo_warning "You must be root to remove mtwilson startup script"
        fi
        rm -rf /opt/mtwilson/* 2>/dev/null  #for non-root user; can't delete $HOME
        # configuration files
        #echo "Removing Mt Wilson configuration in /etc/intel/cloudsecurity..."
        #rm -rf /etc/intel/cloudsecurity
        # data files
        # control scripts
        echo "Removing Mt Wilson control scripts..."
		if [ "$(whoami)" == "root" ]; then
            echo mtwilson-portal asctl wlmctl msctl pcactl mtwilson | tr ' ' '\n' | xargs -I file rm -rf /usr/local/bin/file
		else
		    echo_warning "You must be root to remove the /usr/local/bin/mtwilson file"
		fi
            # only remove the config files we added to conf.d, not anything else
            echo "Removing mtwilson monit config files"
            rm -fr /etc/monit/conf.d/*.mtwilson
            rm -fr /opt/mtwilson/monit/conf.d/*.mtwilson
            echo "Restarting monit after removing configs"
            service monit stop &> /dev/null
            service monit start &> /dev/null
            echo "Removing mtwilson logrotate files"
            rm -fr /etc/logrotate.d/mtwilson
        # java:  rm -rf /usr/share/jdk1.7.0_51
        # Finally, clear variables so that detection will work properly if mt wilson is re-installed  
        java_clear; export JAVA_HOME=""; export java=""; export JAVA_VERSION=""
        tomcat_clear; export TOMCAT_HOME=""; export tomcat_bin=""; export tomcat=""
        glassfish_clear; export GLASSFISH_HOME=""; export glassfish_bin=""; export glassfish=""
        postgres_clear; export POSTGRES_HOME=""; export psql=""
        mysql_clear; export MYSQL_HOME=""; export mysql=""
        echo_success "Done"
        if [ -n "$INSTALLED_MARKER_FILE" ]; then
          rm -fr $INSTALLED_MARKER_FILE
        fi
        ;;
  help)
        print_help
        ;;
  *)
        if [ $# -eq 0 ]; then
          print_help
        else
          call_tag_setupcommand $@
          #if [ $? -eq 1 ]; then print_help; fi
        fi
esac

exit $RETVAL
