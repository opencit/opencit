#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export INSTALL_LOG_FILE=/tmp/mtwilson-install.log
cat /dev/null > $INSTALL_LOG_FILE

if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi
if [ -f mtwilson.env ]; then  . mtwilson.env; fi

if [ -z "$INSTALL_PKGS" ]; then
              #opt_postgres|opt_mysql opt_java opt_tomcat|opt_glassfish opt_privacyca [opt_SERVICES| opt_attservice opt_mangservice opt_wlmservice] [opt_PORTALS | opt_mangportal opt_trustportal opt_wlmportal opt_mtwportal ] opt_monit
 INSTALL_PKGS="postgres java glassfish privacyca SERVICES PORTALS"
fi

FIRST=0
#loop through INSTALL_PKG and set each entry to true
for i in $INSTALL_PKGS; do
 pkg=`echo $i | tr '[A-Z]' '[a-z]'`
 eval opt_$pkg="true"
 if [ $FIRST == 0 ]; then
  FIRST=1;
  LIST=$pkg
 else
  LIST=$LIST", "$pkg
 fi
done

# if a group is defined, then make all sub parts == true
if [ ! -z "$opt_portals" ]; then
  #eval mangportal="true"
  #eval trustportal="true"
  #eval wlmportal="true"
  eval opt_mtwportal="true"
fi
# if a group is defined, then make all sub parts == true
if [ ! -z "$opt_services" ]; then
  eval opt_attservice="true"
  eval opt_mangservice="true"
  eval opt_wlmservice="true"
fi



# ask about mysql vs postgres
#echo "Supported database systems are:"
#echo "postgres"
#echo "mysql"
#prompt_with_default DATABASE_VENDOR "Database System:" ${DATABASE_VENDOR:-mysql}
#if [ "$DATABASE_VENDOR" != "postgres" ] && [ "$DATABASE_VENDOR" != "mysql" ]; then
#  DATABASE_VENDOR=postgres
#  echo_warning "Unrecognized selection. Using $DATABASE_VENDOR"
#fi

#ask about glassfish vs tomcat
#echo "Supported web servers are:"
#echo "tomcat"
#echo "glassfish"
#prompt_with_default WEBSERVER_VENDOR "Web App Server:" ${WEBSERVER_VENDOR:-glassfish}
#if [ "$WEBSERVER_VENDOR" != "tomcat" ] && [ "$WEBSERVER_VENDOR" != "glassfish" ]; then
#  WEBSERVER_VENDOR=tomcat
#  echo_warning "Unrecognized selection. Using $WEBSERVER_VENDOR"
#fi

# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default
if [ ! -z "$opt_glassfish" ]; then
  WEBSERVER_VENDOR=glassfish
elif [ ! -z "$opt_tomcat" ]; then
  WEBSERVER_VENDOR=tomcat
fi

if [ ! -z "$opt_postgres" ]; then
  DATABASE_VENDOR=postgres
elif [ ! -z "$opt_mysql" ]; then
  DATABASE_VENDOR=mysql
fi

export DATABASE_VENDOR=${DATABASE_VENDOR:-postgres}
export WEBSERVER_VENDOR=${WEBSERVER_VENDOR:-glassfish}

if using_glassfish; then
  export DEFAULT_API_PORT=$DEFAULT_GLASSFISH_API_PORT; 
elif using_tomcat; then
  export DEFAULT_API_PORT=$DEFAULT_TOMCAT_API_PORT;
fi

# if customer selected mysql but there is no connector present, we abort the install 
if [ "$DATABASE_VENDOR" == "mysql" ] ; then
  mysqlconnector_file=`ls ~ -1 2>/dev/null | grep -i mysql`
  if [ -n "$mysqlconnector_file" ]; then
    mkdir -p /opt/intel/cloudsecurity/setup-console
    cp ~/$mysqlconnector_file /opt/intel/cloudsecurity/setup-console
  fi
  mysqlconnector_file=`ls -1 /opt/intel/cloudsecurity/setup-console/* 2>/dev/null | grep -i mysql`
  if [ -z "$mysqlconnector_file" ]; then
    echo_failure "Cannot find MySQL Connector/J"
    echo "Recommended steps:"
    echo "1. Download MySQL Connector/J, available free at www.mysql.com"
    echo "2. Copy the .jar from MySQL Connector/J to your home directory"
    echo "3. Run this installer again"
    exit 1
  fi
fi


if using_mysql; then
    export mysql_required_version=${MYSQL_REQUIRED_VERSION:-5.0}
elif using_postgres; then
    export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-8.4}
fi
if using_glassfish; then
    export glassfish_required_version=${GLASSFISH_REQUIRED_VERSION:-3.0}
elif using_tomcat; then
    export tomcat_required_version=${TOMCAT_REQUIRED_VERSION:-8.4}
fi
export JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.6.0_29}
export java_required_version=${JAVA_REQUIRED_VERSION}


echo "Installing packages: $LIST"

APICLIENT_YUM_PACKAGES="unzip"
APICLIENT_APT_PACKAGES="unzip"
APICLIENT_YAST_PACKAGES="unzip"
APICLIENT_ZYPPER_PACKAGES="unzip"
auto_install "Installer requirements" "APICLIENT"

# api client: ensure destination exists and clean it before copying
mkdir -p /usr/local/share/mtwilson/apiclient/java
rm -rf /usr/local/share/mtwilson/apiclient/java/*
unzip api-client*.zip -d /usr/local/share/mtwilson/apiclient/java >> $INSTALL_LOG_FILE

# setup console: create folder and copy the executable jar
mkdir -p /opt/intel/cloudsecurity/setup-console
rm -rf /opt/intel/cloudsecurity/setup-console/*.jar
cp setup-console*.jar /opt/intel/cloudsecurity/setup-console

# create or update mtwilson.properties
mkdir -p /etc/intel/cloudsecurity
if [ -f /etc/intel/cloudsecurity/mtwilson.properties ]; then
  default_mtwilson_tls_policy_name=`read_property_from_file "mtwilson.default.tls.policy.name" /etc/intel/cloudsecurity/mtwilson.properties`
  if [ -z "$default_mtwilson_tls_policy_name" ]; then
    update_property_in_file "mtwilson.default.tls.policy.name" /etc/intel/cloudsecurity/mtwilson.properties "TRUST_FIRST_CERTIFICATE"
    echo_warning "Default per-host TLS policy is to trust the first certificate. You can change it in /etc/intel/cloudsecurity/mtwilson.properties"
  fi
  mtwilson_tls_keystore_password=`read_property_from_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties`
  if [ -z "$mtwilson_tls_keystore_password" ]; then
    # if the configuration file already exists, it means we are doing an upgrade and we need to maintain backwards compatibility with the previous default password "password"
    update_property_in_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties "password"
    # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
    # in a future release we will have a UI mechanism to manage this.
  fi
else
    update_property_in_file "mtwilson.default.tls.policy.name" /etc/intel/cloudsecurity/mtwilson.properties "TRUST_FIRST_CERTIFICATE"
    echo_warning "Default per-host TLS policy is to trust the first certificate. You can change it in /etc/intel/cloudsecurity/mtwilson.properties"
    # for a new install we generate a random password to protect all the tls keystores. (each host record has a tls policy and tls keystore field)
    mtwilson_tls_keystore_password=`generate_password 32`
    update_property_in_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties "$mtwilson_tls_keystore_password"
    # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
    # in a future release we will have a UI mechanism to manage this.
fi

find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin 2>/dev/null | head -n 1`
  echo $binfile
}

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
mtw_portal=`find_installer mtwilson-portal-installer`
glassfish_installer=`find_installer glassfish`
tomcat_installer=`find_installer tomcat`

# Verify the installers we need are present before we start installing
if [ ! -z "$opt_java" ]; then
  if [ ! -e $java_installer ]; then
    echo_warning "Java installer marked for install but missing. Please verify you are using the right installer";
    exit -1;
  fi
fi

if [ ! -e $mtwilson_util ]; then
  echo_warning "Mt Wilson Utils installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi

if [ ! -z "$opt_glassfish" ]; then
  if [ ! -e $glassfish_installer ]; then
    echo_warning "Glassfish installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_tomcat" ]; then
  if [ ! -e $tomcat_installer ]; then
    echo_warning "Tomcat installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi


if [ ! -z "$opt_privacyca" ]; then
  if [ ! -e $privacyca_service ]; then
  echo_warning "Privacy CA installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
  fi
fi

if [ ! -z "$opt_attservice" ]; then
  if [ ! -e $attestation_service ]; then
    echo_warning "Attestation Service installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi


if [ ! -z "$opt_mangservice" ]; then
  if [ ! -e $management_service ]; then
    echo_warning "Management Service installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_wlmservice" ]; then
  if [ ! -e $whitelist_service ]; then
    echo_warning "WhiteList Service installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_mangportal" ]; then
  if [ ! -e $management_console ]; then
    echo_warning "Management Console installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_wlmportal" ]; then
  if [ ! -e $whitelist_portal ]; then
    echo_warning "WhiteList Portal installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_trustportal" ]; then
  if [ ! -e $trust_dashboard ]; then
    echo_warning "Trust DashBoard installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_mtwportal" ]; then
  if [ ! -e $mtw_portal ]; then
    echo_warning "Mtw Combined Portal installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

if [ ! -z "$opt_monit" ]; then
  if [ ! -e $monit_installer ]; then
    echo_warning "Monit installer marked for install but missing. Please verify you are using the right installer"
    exit -1;
  fi
fi

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


if [[ -z "$opt_postgres" && -z "$opt_mysql" ]]; then
 echo_warning "Relying on an existing database installation"
fi

if using_mysql; then
  mysql_userinput_connection_properties
  export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD

  # Install MySQL server (if user selected localhost)
  if [[ "$MYSQL_HOSTNAME" == "127.0.0.1" || "$MYSQL_HOSTNAME" == "localhost" || -n `echo "${hostaddress_list}" | grep "$MYSQL_HOSTNAME"` ]]; then
  if [ ! -z "$opt_mysql" ]; then
      # Place mysql server install code here
    echo "Installing mysql server..."
    aptget_detect; dpkg_detect;
    if [[ -n "$aptget" ]]; then
      echo "mysql-server-5.1 mysql-server/root_password password $MYSQL_PASSWORD" | debconf-set-selections
      echo "mysql-server-5.1 mysql-server/root_password_again password $MYSQL_PASSWORD" | debconf-set-selections 
    fi 
    mysql_server_install 
    mysql_start >> $INSTALL_LOG_FILE
      echo "Installation of mysql server complete..."
    # End mysql server install code here
  else
    echo_warning "Using existing mysql install"
  fi
  fi
  # mysql client install here
  echo "Installing mysql client..."
  mysql_install  
  echo "Installation of mysql client complete..."
  # mysql client end 
  
  if [ -z "$SKIP_DATABASE_INIT" ]; then
    # mysql db init here
  mysql_create_database 
  # mysql db init end
  else
    echo_warning "Skipping init of database"
  fi 
  
  export is_mysql_available mysql_connection_error
  if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi
  
elif using_postgres; then
  postgres_userinput_connection_properties
  export POSTGRES_HOSTNAME POSTGRES_PORTNUM POSTGRES_DATABASE POSTGRES_USERNAME POSTGRES_PASSWORD
  echo "$POSTGRES_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" > $HOME/.pgpass
  chmod 0600 $HOME/.pgpass

  if [ ! -z "$opt_postgres" ]; then
    # postgres server install 

    # Install Postgres server (if user selected localhost)
    if [[ "$POSTGRES_HOSTNAME" == "127.0.0.1" || "$POSTGRES_HOSTNAME" == "localhost" || -n `echo "$(hostaddress_list)" | grep "$POSTGRES_HOSTNAME"` ]]; then
      echo "Installing postgres server..."
      # when we install postgres server on ubuntu it prompts us for root pw
      # we preset it so we can send all output to the log
      aptget_detect; dpkg_detect; yum_detect;
      if [[ -n "$aptget" ]]; then
       echo "postgresql app-pass password $POSTGRES_PASSWORD" | debconf-set-selections 
      fi 
      postgres_server_install 
      postgres_restart >> $INSTALL_LOG_FILE
      sleep 10
      # postgres server end
    fi 
    # postgres client install here
      echo "Installing postgres client..."
      postgres_install
      postgres_restart >> $INSTALL_LOG_FILE
      sleep 10
      echo "Installation of postgres client complete..." 
      # postgres clinet install end
  else
    echo_warning "Relying on an existing Postgres installation"
  fi 
 
 if [ -z "$SKIP_DATABASE_INIT" ]; then
    # postgres db init here
  postgres_create_database
    #postgres_restart >> $INSTALL_LOG_FILE
    #sleep 10
    #export is_postgres_available postgres_connection_error
    if [ -z "$is_postgres_available" ]; then 
      echo_warning "Run 'mtwilson setup' after a database is available"; 
    fi
  # postgress db init end
  else
    echo_warning "Skipping init of database"
  fi 
 
fi

# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER

chmod +x *.bin
if [ ! -z "$opt_java" ] && [ -n "$java_installer" ]; then
  echo "Installing Java..." | tee -a  $INSTALL_LOG_FILE
  ./$java_installer
  echo "Java installation done..." | tee -a  $INSTALL_LOG_FILE
else
    echo "Using existing java installation" | tee -a  $INSTALL_LOG_FILE
fi

echo "Installing Mt Wilson Utils..." | tee -a  $INSTALL_LOG_FILE
./$mtwilson_util  >> $INSTALL_LOG_FILE
echo "Mt Wilson Utils installation done..." | tee -a  $INSTALL_LOG_FILE

if [[ -z "$opt_glassfish" && -z "$opt_tomcat" ]]; then
 echo_warning "Relying on an existing webservice installation"
fi

if using_glassfish; then
  if [ ! -z "$opt_glassfish" ] && [ -n "$glassfish_installer" ]; then
  # glassfish install here
  
  echo "Installing Glassfish..." | tee -a  $INSTALL_LOG_FILE
  ./$glassfish_installer  >> $INSTALL_LOG_FILE
  echo "Glassfish installation complete..." | tee -a  $INSTALL_LOG_FILE
  # end glassfish installer
  else
    echo_warning "Relying on an existing glassfish installation" 
  fi
  
  if [ -z "$SKIP_WEBSERVICE_INIT" ]; then 
    # glassfish init code here
    mtwilson glassfish-sslcert
  # glassfish init end
  else
    echo_warning "Skipping webservice init"
  fi
  # end glassfish setup
elif using_tomcat; then
  if [ ! -z "$opt_tomcat" ] && [ -n "$tomcat_installer" ]; then
    # tomcat install here
    echo "Installing Tomcat..." | tee -a  $INSTALL_LOG_FILE

    ./$tomcat_installer  >> $INSTALL_LOG_FILE
       
    echo "Tomcat installation complete..." | tee -a  $INSTALL_LOG_FILE
  # end tomcat install
  else
    echo_warning "Relying on an existing Tomcat installation"
  fi
 
  if [ -z "$SKIP_WEBSERVICE_INIT" ]; then 
    # tomcat init code here
    #mtwilson tomcat-sslcert
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  # opt_tomcat init end
  else
    echo_warning "Skipping webservice init"
  fi
 
fi

if [ ! -z "$opt_privacyca" ]; then
  echo "Installing Privacy CA (this can take some time, please do not interrupt installer)..." | tee -a  $INSTALL_LOG_FILE
  ./$privacyca_service 
  echo "Privacy installation complete..." | tee -a  $INSTALL_LOG_FILE
  #echo "Restarting Privacy CA..." | tee -a  $INSTALL_LOG_FILE
  #/usr/local/bin/pcactl restart >> $INSTALL_LOG_FILE
  #echo "Privacy CA restarted..." | tee -a  $INSTALL_LOG_FILE
fi

if using_tomcat; then
  if [ ! -z "$opt_tomcat" ]; then
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  fi
fi
if [ ! -z "opt_attservice" ]; then
  echo "Installing Attestation Service..." | tee -a  $INSTALL_LOG_FILE
  ./$attestation_service 
  echo "Attestation Service installed..." | tee -a  $INSTALL_LOG_FILE
fi
if using_tomcat; then
  if [ ! -z "$opt_tomcat" ]; then
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  fi
fi
if [ ! -z "$opt_mangservice" ]; then
  echo "Installing Management Service..." | tee -a  $INSTALL_LOG_FILE
  ./$management_service
  echo "Management Service installed..." | tee -a  $INSTALL_LOG_FILE
fi
if using_tomcat; then
  if [ ! -z "$opt_tomcat" ]; then
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  fi
fi
if [ ! -z "$opt_wlmservice" ]; then
  echo "Installing Whitelist Service..." | tee -a  $INSTALL_LOG_FILE
  ./$whitelist_service >> $INSTALL_LOG_FILE
  echo "Whitelist Service installed..." | tee -a  $INSTALL_LOG_FILE
fi
if using_tomcat; then
  if [ ! -z "$opt_tomcat" ]; then
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  fi
fi
#if [ ! -z "$mangportal" ]; then
#  echo "Installing Management Console..." | tee -a  $INSTALL_LOG_FILE
#  ./$management_console
#  echo "Management Console installed..." | tee -a  $INSTALL_LOG_FILE
#fi

#if [ ! -z "$wlmportal" ]; then
#  echo "Installing WhiteList Portal..." | tee -a  $INSTALL_LOG_FILE
#  ./$whitelist_portal >> $INSTALL_LOG_FILE
#  echo "WhiteList Portal installed..." | tee -a  $INSTALL_LOG_FILE
#fi

#if [ ! -z "$trustportal" ]; then
#  echo "Installing Trust Dashboard..." | tee -a  $INSTALL_LOG_FILE
#  ./$trust_dashboard >> $INSTALL_LOG_FILE
#  echo "Trust Dashboard installed..." | tee -a  $INSTALL_LOG_FILE
#fi

if [ ! -z "$opt_mtwportal" ]; then
  echo "Installing Mtw Combined Portal .." | tee -a  $INSTALL_LOG_FILE
  ./$mtw_portal 
  echo "Mtw Combined Portal installed..." | tee -a  $INSTALL_LOG_FILE
fi
if using_tomcat; then
  if [ ! -z "$opt_tomcat" ]; then
    if tomcat_running; then 
      echo "Restarting Tomcat ..."
      tomcat_restart
    else
      echo "Starting Tomcat ..."
      tomcat_start
    fi
  fi
fi
#TODO-stdale monitrc needs to be customized depending on what is installed
if [ ! -z "$opt_monit" ] && [ -n "$monit_installer" ]; then
  echo "Installing Monit..." | tee -a  $INSTALL_LOG_FILE
  ./$monit_installer  >> $INSTALL_LOG_FILE 
  echo "Monit installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ "${LOCALHOST_INTEGRATION}" == "yes" ]; then
  mtwilson localhost-integration 127.0.0.1 "$MTWILSON_SERVER_IP_ADDRESS"
fi

if using_glassfish; then
  mtwilson glassfish-restart
elif using_tomcat; then
  mtwilson tomcat-restart
fi

echo "Log file for install is located at $INSTALL_LOG_FILE"
