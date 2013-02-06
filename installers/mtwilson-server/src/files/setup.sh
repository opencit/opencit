#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export INSTALL_LOG_FILE=/tmp/mtwilson-install.log
cat /dev/null > $INSTALL_LOG_FILE

if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi
if [ -f mtwilson.env ]; then  . mtwilson.env; fi

# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default
export DATABASE_VENDOR=${DATABASE_VENDOR:-mysql}
export WEBSERVER_VENDOR=${WEBSERVER_VENDOR:-glassfish}
if [[ "${DATABASE_VENDOR}" -eq "mysql" ]]; then
    export mysql_required_version=${MYSQL_REQUIRED_VERSION:-5.0}
elif [[ "${DATABASE_VENDOR}" -eq "postgres" ]]; then
    export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-8.4}
fi
if [[ "${WEBSERVER_VENDOR}" -eq "glassfish" ]]; then
    export glassfish_required_version=${GLASSFISH_REQUIRED_VERSION:-3.0}
elif [[ "${WEBSERVER_VENDOR}" -eq "tomcat" ]]; then
    export tomcat_required_version=${TOMCAT_REQUIRED_VERSION:-8.4}
fi
export java_required_version=${JAVA_REQUIRED_VERSION:-1.6.0_29}


find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin | head -n 1`
  echo $binfile
}

if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

java_installer=`find_installer java`
monit_installer=`find_installer monit`
mtwilson_util=`find_installer MtWilsonLinuxUtil`
privacyca_service=`find_installer PrivacyCAService`
management_service=`find_installer ManagementService`
whitelist_service=`find_installer WLMService`
attestation_service=`find_installer AttestationService`
whitelist_portal=`find_installer WhiteListPortal`
management_console=`find_installer ManagementConsole`
trust_dashboard=`find_installer TrustDashBoard`

# Make sure the nodeploy flag is cleared, so service setup commands will deploy their .war files
export MTWILSON_SETUP_NODEPLOY=

# Gather default configuration
MTWILSON_SERVER_IP_ADDRESS=${MTWILSON_SERVER_IP_ADDRESS:-$(hostaddress)}

# Prompt for installation settings
echo "Configuring Mt Wilson Server Name..."
echo "Please enter the IP Address or Hostname that will identify the Mt Wilson server.
This address will be used in the server SSL certificate and in all Mt Wilson URLs.
For example, if you enter localhost then the Mt Wilson URL is https://localhost:8181
Detected the following options on this server:"
IFS=$'\n'; echo "$(hostaddress_list)"; IFS=' '; hostname;
prompt_with_default MTWILSON_SERVER "Mt Wilson Server:" $MTWILSON_SERVER_IP_ADDRESS
export MTWILSON_SERVER
echo

# XXX TODO ask about mysql vs postgres
# XXX TODO ask about glassfish vs tomcat

if [[ "${DATABASE_VENDOR}" -eq "mysql" ]]; then
  mysql_userinput_connection_properties
  export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD

  # Install MySQL server (if user selected localhost)
  if [[ "$MYSQL_HOSTNAME" == "127.0.0.1" || "$MYSQL_HOSTNAME" == "localhost" || -n `echo "${hostaddress_list}" | grep "$MYSQL_HOSTNAME"` ]]; then
    echo_warning "Installing mysql server..."
    # when we install mysql server on ubuntu it prompts us for root pw
    # we preset it so we can send all output to the log
    aptget_detect; dpkg_detect;
    if [[ -n "$aptget" ]]; then
     echo "mysql-server-5.1 mysql-server/root_password password $MYSQL_PASSWORD" | debconf-set-selections
     echo "mysql-server-5.1 mysql-server/root_password_again password $MYSQL_PASSWORD" | debconf-set-selections 
    fi 
    mysql_server_install 
    mysql_start & >> $INSTALL_LOG_FILE
    echo_warning "Installation of mysql server complete..."
  fi
  echo_warning "Installing mysql client..."
  mysql_install  
  mysql_create_database 
  echo_warning "Installation of mysql client complete..."
  export is_mysql_available mysql_connection_error
  if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi
elif [[ "${DATABASE_VENDOR}" -eq "postgres" ]]; then
  echo_warning "Relying on an existing Postgres installation"
fi


# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER



chmod +x *.bin
echo_warning "Installing Java..." | tee -a  $INSTALL_LOG_FILE
./$java_installer
echo_warning "Java installation done..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Mt Wilson Utils..." | tee -a  $INSTALL_LOG_FILE
./$mtwilson_util  >> $INSTALL_LOG_FILE
echo_warning "Mt Wilson Utils installation done..." | tee -a  $INSTALL_LOG_FILE

if [[ "${WEBSERVER_VENDOR}" -eq "glassfish" ]]; then
  glassfish_installer=`find_installer glassfish`
  echo_warning "Installing Glassfish..." | tee -a  $INSTALL_LOG_FILE
  ./$glassfish_installer  >> $INSTALL_LOG_FILE
  echo_warning "Glassfish installation complete..." | tee -a  $INSTALL_LOG_FILE
elif [[ "${WEBSERVER_VENDOR}" -eq "tomcat" ]]; then
  echo_warning "Relying on an existing Tomcat installation"
fi


echo_warning "Installing Privacy CA (this can take some time, please do not interrupt installer)..." | tee -a  $INSTALL_LOG_FILE
./$privacyca_service 
echo_warning "Privacy installation complete..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Restarting Privacy CA..." | tee -a  $INSTALL_LOG_FILE
/usr/local/bin/pcactl restart >> $INSTALL_LOG_FILE
echo_warning "Privacy CA restarted..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Attestation Service..." | tee -a  $INSTALL_LOG_FILE
./$attestation_service
echo_warning "Attestation Service installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Management Service..." | tee -a  $INSTALL_LOG_FILE
./$management_service
echo_warning "Management Service installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Whitelist Service..." | tee -a  $INSTALL_LOG_FILE
./$whitelist_service >> $INSTALL_LOG_FILE
echo_warning "Whitelist Service installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Management Console..." | tee -a  $INSTALL_LOG_FILE
./$management_console
echo_warning "Management Console installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing WhiteList Portal..." | tee -a  $INSTALL_LOG_FILE
./$whitelist_portal & >> $INSTALL_LOG_FILE
echo_warning "WhiteList Portal installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Trust Dashboard..." | tee -a  $INSTALL_LOG_FILE
./$trust_dashboard  & >> $INSTALL_LOG_FILE
echo_warning "Trust Dashboard installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Installing Monit..." | tee -a  $INSTALL_LOG_FILE
./$monit_installer  & >> $INSTALL_LOG_FILE
echo_warning "Monit installed..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Restarting Attestation Service..." | tee -a  $INSTALL_LOG_FILE
/usr/local/bin/asctl restart & >> $INSTALL_LOG_FILE
echo_warning "Attestation Service restarted..." | tee -a  $INSTALL_LOG_FILE

echo_warning "Log file for install is located at $INSTALL_LOG_FILE"
