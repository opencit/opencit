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
              #postgres|mysql java tomcat|glassfish privacyca [SERVICES| attservice mangservice wlmservice] [PORTALS | mangportal trustportal wlmportal]
 INSTALL_PKGS="mysql java glassfish privacyca SERVICES PORTALS"
fi

FIRST=0
for i in $INSTALL_PKGS; do
 pkg=`echo $i | tr '[A-Z]' '[a-z]'`
 eval $pkg="true"
 if [ $FIRST == 0 ]; then
  FIRST=1;
  LIST=$pkg
 else
  LIST=$LIST", "$pkg
 fi
done

# if a group is defined, then make all sub parts == true
if [ ! -z "$portals" ]; then
  eval mangportal="true"
  eval trustportal="true"
  eval wlmportal="true"
fi
# if a group is defined, then make all sub parts == true
if [ ! -z "$services" ]; then
  eval attservice="true"
  eval mangservice="true"
  eval wlmservice="true"
fi


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


# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default

export DATABASE_VENDOR=${DATABASE_VENDOR:-mysql}
export WEBSERVER_VENDOR=${WEBSERVER_VENDOR:-glassfish}
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


find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin | head -n 1`
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

if [[ -z "$postgres" && -z "$mysql" ]]; then
 echo_warning "Relying on an existing database installation"
fi

if using_mysql; then
  mysql_userinput_connection_properties
  export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD

  # Install MySQL server (if user selected localhost)
  if [[ "$MYSQL_HOSTNAME" == "127.0.0.1" || "$MYSQL_HOSTNAME" == "localhost" || -n `echo "${hostaddress_list}" | grep "$MYSQL_HOSTNAME"` ]]; then
	if [ ! -z "$mysql" ]; then
	    # Place mysql server install code here
		echo "Installing mysql server..."
		aptget_detect; dpkg_detect;
		if [[ -n "$aptget" ]]; then
			echo "mysql-server-5.1 mysql-server/root_password password $MYSQL_PASSWORD" | debconf-set-selections
			echo "mysql-server-5.1 mysql-server/root_password_again password $MYSQL_PASSWORD" | debconf-set-selections 
		fi 
		mysql_server_install 
		mysql_start & >> $INSTALL_LOG_FILE
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
  
  if [ ! -z "$SKIP_DATABASE_INIT" ]; then
    # mysql db init here
	mysql_create_database 
	# mysql db init end
  else
    echo_warning "Skipping init of database"
  fi 
  
  export is_mysql_available mysql_connection_error
  if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi
  
elif using_postgres; then
 if [ ! -z "$postgres" ]; then
  # postgres server install 
  echo_warning "Relying on an existing Postgres installation"
  # postgres server end
 else
  echo_warning "Relying on an existing Postgres installation"
 fi 
 # postgres client install here
 
 # postgres clinet install end
 
 if [ ! -z "$SKIP_DATABASE_INIT" ]; then
    # postgres db init here
	echo_warning "Init of postgres db currently not supported"
	# postgress db init end
  else
    echo_warning "Skipping init of database"
  fi 
 
fi

# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER

chmod +x *.bin
if [ ! -z "$java" ]; then
	echo "Installing Java..." | tee -a  $INSTALL_LOG_FILE
	./$java_installer
	echo "Java installation done..." | tee -a  $INSTALL_LOG_FILE
else
    echo "Using existing java installation" | tee -a  $INSTALL_LOG_FILE
fi

echo "Installing Mt Wilson Utils..." | tee -a  $INSTALL_LOG_FILE
./$mtwilson_util  >> $INSTALL_LOG_FILE
echo "Mt Wilson Utils installation done..." | tee -a  $INSTALL_LOG_FILE

if [[ -z "$glassfish" && -z "$tomcat" ]]; then
 echo_warning "Relying on an existing webservice installation"
fi

if using_glassfish; then
  if [ ! -z "$glassfish" ]; then
  # glassfish install here
	glassfish_installer=`find_installer glassfish`
	echo "Installing Glassfish..." | tee -a  $INSTALL_LOG_FILE
	./$glassfish_installer  >> $INSTALL_LOG_FILE
	echo "Glassfish installation complete..." | tee -a  $INSTALL_LOG_FILE
  # end glassfish installer
  else
    echo_warning "Relying on an existing glassfish installation" 
  fi
  
  if [ ! -z "$SKIP_WEBSERVICE_INIT" ]; then 
    # glassfish init code here
    mtwilson glassfish-sslcert
	# glassfish init end
  else
    echo_warning "Skipping webservice init"
  fi
  # end glassfish setup
elif using_tomcat; then
 if [ ! -z "$tomcat" ]; then
  # tomcat install here
  echo_warning "Relying on an existing Tomcat installation"
  # end tomcat install
 else
  echo_warning "Relying on an existing Tomcat installation"
 fi
 
 if [ ! -z "$SKIP_WEBSERVICE_INIT" ]; then 
    # tomcat init code here
    echo_warning "Init of tomcat currently not supported"
	# tomcat init end
  else
    echo_warning "Skipping webservice init"
  fi
 
fi

if [ ! -z "$privacyca" ]; then
	echo "Installing Privacy CA (this can take some time, please do not interrupt installer)..." | tee -a  $INSTALL_LOG_FILE
	./$privacyca_service 
	echo "Privacy installation complete..." | tee -a  $INSTALL_LOG_FILE
	echo "Restarting Privacy CA..." | tee -a  $INSTALL_LOG_FILE
	/usr/local/bin/pcactl restart >> $INSTALL_LOG_FILE
	echo "Privacy CA restarted..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$attservice" ]; then
	echo "Installing Attestation Service..." | tee -a  $INSTALL_LOG_FILE
	./$attestation_service
	echo "Attestation Service installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$mangservice" ]; then
	echo "Installing Management Service..." | tee -a  $INSTALL_LOG_FILE
	./$management_service
	echo "Management Service installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$wlmservice" ]; then
	echo "Installing Whitelist Service..." | tee -a  $INSTALL_LOG_FILE
	./$whitelist_service >> $INSTALL_LOG_FILE
	echo "Whitelist Service installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$mangportal" ]; then
	echo "Installing Management Console..." | tee -a  $INSTALL_LOG_FILE
	./$management_console
	echo "Management Console installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$wlmportal" ]; then
	echo "Installing WhiteList Portal..." | tee -a  $INSTALL_LOG_FILE
	./$whitelist_portal >> $INSTALL_LOG_FILE
	echo "WhiteList Portal installed..." | tee -a  $INSTALL_LOG_FILE
fi

if [ ! -z "$trustportal" ]; then
	echo "Installing Trust Dashboard..." | tee -a  $INSTALL_LOG_FILE
	./$trust_dashboard >> $INSTALL_LOG_FILE
	echo "Trust Dashboard installed..." | tee -a  $INSTALL_LOG_FILE
fi

#TODO-stdale monitrc needs to be customized depending on what is installed
echo "Installing Monit..." | tee -a  $INSTALL_LOG_FILE
./$monit_installer  >> $INSTALL_LOG_FILE
echo "Monit installed..." | tee -a  $INSTALL_LOG_FILE



if using_glassfish; then
  echo "Restarting Glassfish..." | tee -a  $INSTALL_LOG_FILE
  mtwilson glassfish-restart >> $INSTALL_LOG_FILE
  echo "Glassfish restarted..." | tee -a  $INSTALL_LOG_FILE
else
  echo "Restarting Attestation Service..." | tee -a  $INSTALL_LOG_FILE
  /usr/local/bin/asctl restart >> $INSTALL_LOG_FILE
  echo "Attestation Service restarted..." | tee -a  $INSTALL_LOG_FILE
fi

echo "Log file for install is located at $INSTALL_LOG_FILE"
