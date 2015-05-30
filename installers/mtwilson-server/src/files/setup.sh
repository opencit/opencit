#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# equivalent to env_dir in mtwilson.sh
MTWILSON_ENV_DIR=/opt/mtwilson/env.d

currentUser=`whoami`

if [ ! -d $MTWILSON_ENV_DIR ]; then
  mkdir -p $MTWILSON_ENV_DIR
fi

rm -rf /opt/mtwilson/java 2>/dev/null

#load the functions file first so we can use the generatePassword function
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

#define defaults so that they can be overwriten 
#if the value appears in mtwilson.env
export INSTALLED_MARKER_FILE=/var/opt/intel/.mtwilsonInstalled
export LOG_ROTATION_PERIOD=daily
export LOG_COMPRESS=compress
export LOG_DELAYCOMPRESS=delaycompress
export LOG_COPYTRUNCATE=copytruncate
export LOG_SIZE=100M
export LOG_OLD=7
export AUTO_UPDATE_ON_UNTRUST=false
#export WEBSERVICE_MANAGER_USERNAME=mtwilsonAdmin
#export WEBSERVICE_MANAGER_USERNAME=admin
#export WEBSERVICE_MANAGER_PASSWORD=`generate_password 16`
export INSTALL_LOG_FILE=/tmp/mtwilson-install.log
date > $INSTALL_LOG_FILE

if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi
if [ -f mtwilson.env ]; then  . mtwilson.env; fi

export MTWILSON_OWNER=${MTWILSON_OWNER:-mtwilson}

mtw_props_path="/etc/intel/cloudsecurity/mtwilson.properties"
as_props_path="/etc/intel/cloudsecurity/attestation-service.properties"
#pca_props_path="/etc/intel/cloudsecurity/PrivacyCA.properties"
ms_props_path="/etc/intel/cloudsecurity/management-service.properties"
mp_props_path="/etc/intel/cloudsecurity/mtwilson-portal.properties"
hp_props_path="/etc/intel/cloudsecurity/clientfiles/hisprovisioner.properties"
ta_props_path="/etc/intel/cloudsecurity/trustagent.properties"
file_paths=("$mtw_props_path" "$as_props_path" "$ms_props_path" "$mp_props_path" "$hp_props_path" "$ta_props_path")

# disable upgrade if properties files are encrypted from a previous installation
for file in ${file_paths[*]}; do
  if [ -f $file ]; then
    if file_encrypted $file; then
      echo_failure "Please decrypt property files before proceeding with mtwilson installation or upgrade."
      exit -1
    fi
  fi
done

load_conf
load_defaults

if [[ $MTWILSON_OWNER == "glassfish" || $MTWILSON_OWNER == "tomcat" ]]; then
 echo_warning "Program files are writable by the web service container, this is a possible security issue"
else
 getent passwd $MTWILSON_OWNER >/dev/null
 if [ $? != 0 ]; then
  echo "Creating Mt Wilson owner account [$MTWILSON_OWNER]"
  #prompt_with_default_password MTWILSON_OWNER_PASSWORD "Password:" ${MTWILSON_OWNER_PASSWORD}
  #pass=$(perl -e 'print crypt($ARGV[0], "password")' $MTWILSON_OWNER_PASSWORD)
  useradd -s /bin/false -d /opt/mtwilson $MTWILSON_OWNER
  if [ $? != 0 ]; then
    echo_warning "Failed to create user '$MTWILSON_OWNER'"
  fi
 fi
fi

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
#prompt_with_default WEBSERVICE_VENDOR "Web App Server:" ${WEBSERVICE_VENDOR:-glassfish}
#if [ "$WEBSERVICE_VENDOR" != "tomcat" ] && [ "$WEBSERVICE_VENDOR" != "glassfish" ]; then
#  WEBSERVICE_VENDOR=tomcat
#  echo_warning "Unrecognized selection. Using $WEBSERVICE_VENDOR"
#fi

# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default
if [ ! -z "$opt_glassfish" ]; then
  WEBSERVICE_VENDOR=glassfish
elif [ ! -z "$opt_tomcat" ]; then
  WEBSERVICE_VENDOR=tomcat
fi

if [ ! -z "$opt_postgres" ]; then
  DATABASE_VENDOR=postgres
elif [ ! -z "$opt_mysql" ]; then
  DATABASE_VENDOR=mysql
fi

#export DATABASE_VENDOR=${DATABASE_VENDOR:-postgres}
#export WEBSERVICE_VENDOR=${WEBSERVICE_VENDOR:-tomcat}

if using_glassfish; then
  export DEFAULT_API_PORT=$DEFAULT_GLASSFISH_API_PORT; 
elif using_tomcat; then
  export DEFAULT_API_PORT=$DEFAULT_TOMCAT_API_PORT;
fi

# if customer selected mysql but there is no connector present, we abort the install 
if using_mysql ; then
  mysqlconnector_file=`ls ~ -1 2>/dev/null | grep -i "^mysql-connector-java"`
  if [ -n "$mysqlconnector_file" ]; then
    mkdir -p /opt/mtwilson/java
    cp ~/$mysqlconnector_file /opt/mtwilson/java
  fi
  mysqlconnector_file=`ls -1 /opt/mtwilson/java/* 2>/dev/null | grep -i mysql`
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
    export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-9.3}
fi
if using_glassfish; then
    export glassfish_required_version=${GLASSFISH_REQUIRED_VERSION:-4.0}
elif using_tomcat; then
    export tomcat_required_version=${TOMCAT_REQUIRED_VERSION:-7.0}
fi
export JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7.0_51}
export java_required_version=${JAVA_REQUIRED_VERSION}


echo "Installing packages: $LIST"
if [ $currentUser == "root" ]; then 
 APICLIENT_YUM_PACKAGES="zip unzip openssl"
 APICLIENT_APT_PACKAGES="zip unzip openssl"
 APICLIENT_YAST_PACKAGES="zip unzip openssl"
 APICLIENT_ZYPPER_PACKAGES="zip unzip openssl"
 auto_install "Installer requirements" "APICLIENT"
else
 echo "Assuming that packages zip, unzip and openssl are installed already"
fi

# api client: ensure destination exists and clean it before copying
#mkdir -p /usr/local/share/mtwilson/apiclient/java
#rm -rf /usr/local/share/mtwilson/apiclient/java/*
#unzip mtwilson-client-java6*.zip -d /usr/local/share/mtwilson/apiclient/java >> $INSTALL_LOG_FILE

# setup console: create folder and copy the executable jar
#mkdir -p /opt/intel/cloudsecurity/setup-console
#rm -rf /opt/intel/cloudsecurity/setup-console/mtwilson-console*.jar
#cp mtwilson-console*.jar /opt/intel/cloudsecurity/setup-console

# create or update mtwilson.properties
mkdir -p /etc/intel/cloudsecurity
if [ -f /etc/intel/cloudsecurity/mtwilson.properties ]; then
  default_mtwilson_tls_policy_id="$MTW_DEFAULT_TLS_POLICY_ID"   #`read_property_from_file "mtwilson.default.tls.policy.id" /etc/intel/cloudsecurity/mtwilson.properties`
  if [ "$default_mtwilson_tls_policy_id" == "INSECURE" ] || [ "$default_mtwilson_tls_policy_id" == "TRUST_FIRST_CERTIFICATE" ]; then
    #update_property_in_file "mtwilson.default.tls.policy.id" /etc/intel/cloudsecurity/mtwilson.properties "TRUST_FIRST_CERTIFICATE"
    echo_warning "Default TLS policy is insecure; the product guide contains information on enabling secure TLS policies"
  fi
  export mtwilson_tls_keystore_password="$MTW_TLS_KEYSTORE_PASS"   #`read_property_from_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties`
  #if [ -z "$mtwilson_tls_keystore_password" ]; then
    # if the configuration file already exists, it means we are doing an upgrade and we need to maintain backwards compatibility with the previous default password "password"
    #update_property_in_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties "password"
    # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
    # in a future release we will have a UI mechanism to manage this.
  #fi
else
    touch /etc/intel/cloudsecurity/mtwilson.properties
    chmod 666 /etc/intel/cloudsecurity/mtwilson.properties
    export mtwilson_tls_keystore_password=`generate_password 32`
    export MTW_TLS_KEYSTORE_PASS="$mtwilson_tls_keystore_password"
#    update_property_in_file "mtwilson.tls.policy.allow" /etc/intel/cloudsecurity/mtwilson.properties "certificate,certificate-digest"
    echo '#mtwilson.default.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  /etc/intel/cloudsecurity/mtwilson.properties
    echo '#mtwilson.global.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  /etc/intel/cloudsecurity/mtwilson.properties
    #update_property_in_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties "$mtwilson_tls_keystore_password"
    # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
    # in a future release we will have a UI mechanism to manage this.
fi

#MTW_TLS_POLICY_ALLOW
prompt_with_default MTW_TLS_POLICY_ALLOW "Mt Wilson Allowed TLS Policies: " "$MTW_TLS_POLICY_ALLOW"
MTW_TLS_POLICY_ALLOW=`echo $MTW_TLS_POLICY_ALLOW | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTW_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTW_TLS_POLICY_ALLOW=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "certificate" ] || [ "$i" == "certificate-digest" ] || [ "$i" == "public-key" ] || [ "$i" == "public-key-digest" ] || [ "$i" == "TRUST_FIRST_CERTIFICATE" ] || [ "$i" == "INSECURE" ]; then
    TMP_MTW_TLS_POLICY_ALLOW+="$i,"
  fi
done
MTW_TLS_POLICY_ALLOW=`echo "$TMP_MTW_TLS_POLICY_ALLOW" | sed 's/\(.*\),/\1/'`

if [ -n "$MTW_TLS_POLICY_ALLOW" ]; then
  update_property_in_file "mtwilson.tls.policy.allow" /etc/intel/cloudsecurity/mtwilson.properties "$MTW_TLS_POLICY_ALLOW"
else
  echo_failure "An allowed TLS policy must be defined."
  exit -1
fi

#MTW_DEFAULT_TLS_POLICY_ID
prompt_with_default MTW_DEFAULT_TLS_POLICY_ID "Mt Wilson Default TLS Policy ID: " "$MTW_DEFAULT_TLS_POLICY_ID"
MTW_DEFAULT_TLS_POLICY_ID=`echo $MTW_DEFAULT_TLS_POLICY_ID | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTW_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTW_DEFAULT_TLS_POLICY_ID=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "$MTW_DEFAULT_TLS_POLICY_ID" ]; then
    TMP_MTW_DEFAULT_TLS_POLICY_ID="$i"
  fi
done
MTW_DEFAULT_TLS_POLICY_ID=`echo "$TMP_MTW_DEFAULT_TLS_POLICY_ID"`

if [[ "$MTW_DEFAULT_TLS_POLICY_ID" == "INSECURE" || "$MTW_DEFAULT_TLS_POLICY_ID" == "TRUST_FIRST_CERTIFICATE" ]]; then
  update_property_in_file "mtwilson.default.tls.policy.id" /etc/intel/cloudsecurity/mtwilson.properties "$MTW_DEFAULT_TLS_POLICY_ID"
else
  echo_warning "Unable to determine default TLS policy."
#  exit -1
fi

update_property_in_file "mtwilson.as.autoUpdateHost" /etc/intel/cloudsecurity/mtwilson.properties "$AUTO_UPDATE_ON_UNTRUST"
update_property_in_file "mtwilson.locales" /etc/intel/cloudsecurity/mtwilson.properties "en-US"

#Save variables to properties file
if using_mysql; then   
  mysql_write_connection_properties /etc/intel/cloudsecurity/mtwilson.properties mtwilson.db
elif using_postgres; then
  postgres_write_connection_properties /etc/intel/cloudsecurity/mtwilson.properties mtwilson.db
fi

# default connection pool settings
update_property_in_file "dbcp.validation.query" /etc/intel/cloudsecurity/mtwilson.properties "select 1"
update_property_in_file "dbcp.validation.on.borrow" /etc/intel/cloudsecurity/mtwilson.properties "true"
update_property_in_file "dbcp.validation.on.return" /etc/intel/cloudsecurity/mtwilson.properties "false"


# copy default logging settings to /etc
chmod 666 logback.xml
cp logback.xml /etc/intel/cloudsecurity
chmod 666 logback-stderr.xml
cp logback-stderr.xml /etc/intel/cloudsecurity

# copy shiro.ini api security file
if [ ! -f /etc/intel/cloudsecurity/shiro.ini ]; then
  chmod 666 shiro.ini shiro-localhost.ini
  cp shiro.ini shiro-localhost.ini /etc/intel/cloudsecurity
fi

# add MTWILSON_SERVER to shiro trust file
# use "hostFilter.allow" when using the access-denying filter (any clients not from that list of ip's will be denied)
# use "iniHostRealm.allow" when using the access-allowing filter (any clients from that list of ip's will be allowed access but clients from other ip's can still try password or x509 authentication) - this is the current default
hostAllowPropertyName=iniHostRealm.allow
sed -i '/'"$hostAllowPropertyName"'/ s/^#//g' /etc/intel/cloudsecurity/shiro.ini
hostAllow=`read_property_from_file $hostAllowPropertyName /etc/intel/cloudsecurity/shiro.ini`
if [[ $hostAllow != *$MTWILSON_SERVER* ]]; then
  update_property_in_file "$hostAllowPropertyName" /etc/intel/cloudsecurity/shiro.ini "$hostAllow,$MTWILSON_SERVER";
fi
hostAllow=`read_property_from_file $hostAllowPropertyName /etc/intel/cloudsecurity/shiro.ini`
if [[ $hostAllow != *$MTWILSON_IP* ]]; then
  update_property_in_file "$hostAllowPropertyName" /etc/intel/cloudsecurity/shiro.ini "$hostAllow,$MTWILSON_IP";
fi
sed -i '/'"$hostAllowPropertyName"'/ s/^\([^#]\)/#\1/g' /etc/intel/cloudsecurity/shiro.ini

# This property is needed by the UpdateSslPort command to determine the port # that should be used in the shiro.ini file
 update_property_in_file "mtwilson.api.url" /etc/intel/cloudsecurity/mtwilson.properties "$MTWILSON_API_BASEURL"

echo "Adding symlink for /opt/mtwilson/configuration..."
# temp symlink -- SAVY added 2014-02-26
if [[ ! -h "/opt/mtwilson/configuration" ]]; then
  mkdir -p /opt/mtwilson
  ln -s "/etc/intel/cloudsecurity" "/opt/mtwilson/configuration"
fi

# copy extensions.cache file
if [ ! -f /opt/mtwilson/configuration/extensions.cache ]; then
  chmod 666 extensions.cache
  cp extensions.cache /opt/mtwilson/configuration
fi


find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin 2>/dev/null | head -n 1`
  echo $binfile
}

java_installer=`find_installer java`
monit_installer=`find_installer monit`
logrotate_installer=`find_installer logrotate`
mtwilson_util=`find_installer mtwilson-linux-util` #MtWilsonLinuxUtil`
management_service=`find_installer mtwilson-management-service` #ManagementService`
whitelist_service=`find_installer mtwilson-whitelist-service` #WLMService`
attestation_service=`find_installer mtwilson-attestation-service` #AttestationService`
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
For example, if you enter '$MTWILSON_SERVER_IP_ADDRESS' then the Mt Wilson URL is 
https://$MTWILSON_SERVER_IP_ADDRESS:8181 (for Glassfish deployments) or 
https://$MTWILSON_SERVER_IP_ADDRESS:8443 (for Tomcat deployments)
Detected the following options on this server:"
for h in $(hostaddress_list); do echo "+ $h"; done; echo "+ "`hostname`
prompt_with_default MTWILSON_SERVER "Mt Wilson Server:" $MTWILSON_SERVER_IP_ADDRESS
export MTWILSON_SERVER

echo


if [[ -z "$opt_postgres" && -z "$opt_mysql" ]]; then
 echo_warning "Relying on an existing database installation"
fi
if [ $currentUser == "root" ]; then
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
        if ! mysql_create_database; then
          mysql_install_db --user=mysql --basedir=/usr --datadir=/var/lib/mysql --defaults-file=/etc/mysql/my.cnf
          mysqladmin -u "$MYSQL_USERNAME" password "$MYSQL_PASSWORD"
          mysql_create_database
        fi
        # mysql db init end
      else
        echo_warning "Skipping init of database"
      fi 
      
      export is_mysql_available mysql_connection_error
      if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi
      
    elif using_postgres; then
      postgres_installed=1
      # Copy the www.postgresql.org PGP public key so add_postgresql_install_packages can add it later if needed
      if [ -d "/etc/apt" ]; then
        mkdir -p /etc/apt/trusted.gpg.d
        chmod 755 /etc/apt/trusted.gpg.d
        cp ACCC4CF8.asc "/etc/apt/trusted.gpg.d"
        POSTGRES_SERVER_APT_PACKAGES="postgresql-9.3"
        add_postgresql_install_packages "POSTGRES_SERVER"
      fi

      postgres_userinput_connection_properties
      touch ~/.pgpass
      chmod 0600 ~/.pgpass
      export POSTGRES_HOSTNAME POSTGRES_PORTNUM POSTGRES_DATABASE POSTGRES_USERNAME POSTGRES_PASSWORD
      if [ "$POSTGRES_HOSTNAME" == "127.0.0.1" ] || [ "$POSTGRES_HOSTNAME" == "localhost" ]; then
        PGPASS_HOSTNAME=localhost
      else
        PGPASS_HOSTNAME="$POSTGRES_HOSTNAME"
      fi
      echo "$POSTGRES_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" > ~/.pgpass
      echo "$PGPASS_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" >> ~/.pgpass

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
          postgres_installed=0 #postgres is being installed
          # don't need to restart postgres server unless the install script says we need to (by returning zero)
          if postgres_server_install; then
            postgres_restart >> $INSTALL_LOG_FILE
            sleep 10
          fi
          # postgres server end
        fi 
        # postgres client install here
          echo "Installing postgres client..."
          postgres_install
          # do not need to restart postgres server after installing the client.
          #postgres_restart >> $INSTALL_LOG_FILE
          #sleep 10
          echo "Installation of postgres client complete..." 
          # postgres client install end
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
      if [ $postgres_installed -eq 0 ]; then
        postgres_server_detect
        has_local_peer=`grep "^local.*all.*all.*peer" $postgres_pghb_conf`
        if [ -n "$has_local_peer" ]; then
          echo "Replacing PostgreSQL local 'peer' authentication with 'password' authentication..."
          sed -i 's/^local.*all.*all.*peer/local all all password/' $postgres_pghb_conf
        fi
        #if [ "$POSTGRESQL_KEEP_PGPASS" != "true" ]; then   # Use this line after 2.0 GA, and verify compatibility with other commands
        if [ "${POSTGRESQL_KEEP_PGPASS:-true}" == "false" ]; then
          if [ -f /root/.pgpass ]; then
            echo "Removing .pgpass file to prevent insecure database password storage in plaintext..."
            rm /root/.pgpass
          fi
        fi
      fi
    fi
else
 if using_mysql; then
  echo "mysql"
  #TODO
 elif using_postgres; then
  postgres_detect
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

java_detect

echo "JAVA_HOME=$JAVA_HOME" > $MTWILSON_ENV_DIR/java
echo "java=$java" >> $MTWILSON_ENV_DIR/java

# Post java install setup and configuration
if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

# following code causes the current JAVA_HOME value to be appended to
# whatever PATH is there (if it's not already there), which means that
# if one time we detect openjdk, and add it, then next time detect oracle jdk,
# it will always be added after any previously found jdk, so when using
# that PATH and running "java" it will never start the most recently found jdk
# unless we actually delete the other ones. 
#if [ -f "/etc/environment" ] && [ -n "${JAVA_HOME}" ]; then
#  if ! grep "PATH" /etc/environment | grep -q "${JAVA_HOME}/bin"; then
#    sed -i '/PATH/s/\(.*\)\"$/\1/g' /etc/environment
#    sed -i '/PATH/s,$,:'"$JAVA_HOME"'/\bin\",' /etc/environment
#  fi
#  if ! grep -q "JAVA_HOME" /etc/environment; then
#    echo "JAVA_HOME=${JAVA_HOME}" >> /etc/environment
#  fi
#  
#  . /etc/environment
#fi

echo "Installing Mt Wilson linux utility..." | tee -a  $INSTALL_LOG_FILE
./$mtwilson_util  >> $INSTALL_LOG_FILE
echo "Mt Wilson Utils installation done..." | tee -a  $INSTALL_LOG_FILE

echo "Installing Mt Wilson application code..." | tee -a $INSTALL_LOG_FILE
ZIP_PACKAGE=`ls -1 mtwilson-server*.zip 2>/dev/null | tail -n 1`
unzip -DD -o $ZIP_PACKAGE -d /opt/mtwilson >> $INSTALL_LOG_FILE  2>&1
mkdir -p /opt/mtwilson/var
#chown -R $MTWILSON_OWNER:$MTWILSON_OWNER /opt/mtwilson
#chown -R root /opt/mtwilson/bin
#chown -R root /opt/mtwilson/java
#chown -R root /opt/mtwilson/configuration
#mkdir -p /var/log/mtwilson
#chown $MTWILSON_OWNER:$MTWILSON_OWNER /var/log/mtwilson
chmod -R 770 /opt/mtwilson/bin
mkdir -p /opt/mtwilson/env.d
#chown -R root /opt/mtwilson/env.d


if [[ -z "$opt_glassfish" && -z "$opt_tomcat" ]]; then
 echo_warning "Relying on an existing webservice installation"
fi

if using_glassfish; then
  if [ ! -z "$opt_glassfish" ] && [ -n "$glassfish_installer" ]; then
    if [ ! glassfish_detect >/dev/null ]; then
      portInUse=`netstat -lnput | grep -E "8080|8181"`
      if [ -n "$portInUse" ]; then 
        #glassfish ports in use. exit install
        echo_failure "Glassfish ports in use. Aborting install."
        exit 1
      fi
    fi
    
    echo "Installing Glassfish..." | tee -a  $INSTALL_LOG_FILE
    # glassfish install here
    ./$glassfish_installer  >> $INSTALL_LOG_FILE
    glassfish_create_ssl_cert_prompt
    echo "Glassfish installation complete..." | tee -a  $INSTALL_LOG_FILE
    # end glassfish installer
  else
    echo_warning "Relying on an existing glassfish installation" 
  fi

  glassfish_detect

  echo "GLASSFISH_HOME=$GLASSFISH_HOME" > $MTWILSON_ENV_DIR/glassfish
  echo "glassfish=\"$glassfish\"" >> $MTWILSON_ENV_DIR/glassfish
  echo "glassfish_bin=$glassfish_bin" >> $MTWILSON_ENV_DIR/glassfish
  
  if [ -e $glassfish_bin ]; then
    echo "Disabling glassfish log rotation in place of system wide log rotation"
      #$glassfish set-log-attributes --target server com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0   ### THIS COMMAND DOES NOT WORK
      gf_logging_properties=$(find "$GLASSFISH_HOME" -name logging.properties | head -1)
      sed -i "s/com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=.*/com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0/g" "$gf_logging_properties"
  else
    echo_warning "Unable to locate asadmin, please run the following command on your system to disable glassfish log rotation: "
    echo_warning "asadmin set-log-attributes --target server com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0"
  fi

  #if [ -e $glassfish_bin ]; then
  #  echo "Increasing glassfish max thread pool size to 200..."
  #    $glassfish_bin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200
  #else
  #  echo_warning "Unable to locate asadmin, please run the following command on your system to increase HTTP-max-thread-size: "
  #  echo_warning "asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200"
  #fi
  
  ### WHAT IS THIS USED FOR???
  #if [ -z "$SKIP_WEBSERVICE_INIT" ]; then 
  #  # glassfish init code here
  #  mtwilson glassfish-sslcert
  #  # glassfish init end
  #else
  #  echo_warning "Skipping webservice init"
  #fi
  # end glassfish setup
elif using_tomcat; then
  if [ ! -z "$opt_tomcat" ] && [ -n "$tomcat_installer" ]; then
    if [ ! tomcat_detect >/dev/null ]; then
      portInUse=`netstat -lnput | grep -E "8080|8443"`
      if [ -n "$portInUse" ]; then 
        #tomcat ports in use. exit install
        echo_failure "Tomcat ports in use. Aborting install."
        exit 1
      fi
    fi

    # tomcat install here
    echo "Installing Tomcat..." | tee -a  $INSTALL_LOG_FILE
    ./$tomcat_installer  >> $INSTALL_LOG_FILE
    tomcat_create_ssl_cert_prompt
    echo "Tomcat installation complete..." | tee -a  $INSTALL_LOG_FILE
  # end tomcat install
  else
    echo_warning "Relying on an existing Tomcat installation"
  fi
 
  tomcat_detect

  echo "TOMCAT_HOME=$TOMCAT_HOME" > $MTWILSON_ENV_DIR/tomcat
  echo "TOMCAT_CONF=$TOMCAT_CONF" >> $MTWILSON_ENV_DIR/tomcat
  echo "tomcat=\"$tomcat\"" >> $MTWILSON_ENV_DIR/tomcat
  echo "tomcat_bin=$tomcat_bin" >> $MTWILSON_ENV_DIR/tomcat
  
  ### WHAT IS THIS USED FOR???
  #if [ -z "$SKIP_WEBSERVICE_INIT" ]; then 
  #  # tomcat init code here
  #  #mtwilson tomcat-sslcert
  #  if tomcat_running; then 
  #    echo "Restarting Tomcat ..."
  #    tomcat_restart >> $INSTALL_LOG_FILE 2>&1
  #  else
  #    echo "Starting Tomcat ..."
  #    tomcat_start >> $INSTALL_LOG_FILE 2>&1
  #  fi
  ## opt_tomcat init end
  #else
  #  echo_warning "Skipping webservice init"
  #fi
 
fi


if [[ -n "opt_attservice"  && -f "$attestation_service" ]]; then
  echo "Installing mtwilson service..." | tee -a  $INSTALL_LOG_FILE
  ./$attestation_service 
  echo "mtwilson service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_mangservice" && -f "$management_service"  ]]; then
  echo "Installing Management Service..." | tee -a  $INSTALL_LOG_FILE
  ./$management_service
  echo "Management Service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_wlmservice" && -f "$whitelist_service" ]]; then
  echo "Installing Whitelist Service..." | tee -a  $INSTALL_LOG_FILE
  ./$whitelist_service >> $INSTALL_LOG_FILE
  echo "Whitelist Service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_mtwportal" && "$mtw_portal" ]]; then
  echo "Installing Mtw Combined Portal..." | tee -a  $INSTALL_LOG_FILE
  ./$mtw_portal 
  echo "Mtw Combined Portal installed" | tee -a  $INSTALL_LOG_FILE
fi

##############################################################################################################################################################################
##tag service installation
CONFIG_DIR=/opt/mtwilson/configuration
prompt_with_default MTWILSON_TAG_SERVER_PRIVATE "Mt Wilson Tag Private Server: " $MTWILSON_SERVER
WEBSERVER_PREFIX=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $1}'`
WEBSERVER_PORT=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $3}' | awk -F: '{print $2}'`
MTWILSON_TAG_URL="$WEBSERVER_PREFIX//$MTWILSON_TAG_SERVER_PRIVATE:$WEBSERVER_PORT/mtwilson/v2"
MTWILSON_API_TAG_URL="$WEBSERVER_PREFIX//$MTWILSON_TAG_SERVER_PRIVATE:$WEBSERVER_PORT/mtwilson/v1/AttestationService/resources/assetTagCert"
update_property_in_file "mtwilson.atag.url" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_URL"
update_property_in_file "mtwilson.atag.mtwilson.baseurl" $CONFIG_DIR/mtwilson.properties "$MTWILSON_API_TAG_URL"

prompt_with_default MTWILSON_TAG_ADMIN_USERNAME "Mt Wilson Asset Tag Admin User: " ${MTWILSON_TAG_ADMIN_USERNAME:-tagadmin}
prompt_with_default_password MTWILSON_TAG_ADMIN_PASSWORD "Mt Wilson Asset Tag Admin Password: " $MTWILSON_TAG_ADMIN_PASSWORD

MTWILSON_TAG_API_USERNAME=${MTWILSON_TAG_API_USERNAME:-"tagservice"}
MTWILSON_TAG_API_PASSWORD=${MTWILSON_TAG_API_PASSWORD:-$(generate_password 16)}
update_property_in_file "mtwilson.tag.api.username" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_API_USERNAME"
update_property_in_file "mtwilson.tag.api.password" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_API_PASSWORD"

#prompt_with_default MTWILSON_TAG_KEYSTORE "Mt Wilson Tag Keystore Path: " ${MTWILSON_TAG_KEYSTORE:-/opt/mtwilson/configuration/serverAtag.jks}
#prompt_with_default_password MTWILSON_TAG_KEYSTORE_PASSWORD "Mt Wilson Tag Keystore Password: " $MTWILSON_TAG_KEYSTORE_PASSWORD
#prompt_with_default_password MTWILSON_TAG_KEY_PASSWORD "Mt Wilson Tag Key Password: " $MTWILSON_TAG_KEY_PASSWORD
#update_property_in_file "mtwilson.atag.keystore" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_KEYSTORE"
#update_property_in_file "mtwilson.atag.keystore.password" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_KEYSTORE_PASSWORD"
#update_property_in_file "mtwilson.atag.key.password" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_KEY_PASSWORD"

if [ ! -z "$opt_portals" ]; then
  MTWILSON_TAG_HTML5_DIR_TEMP=`find /usr/share/ -name tag`
  prompt_with_default MTWILSON_TAG_HTML5_DIR "Mt Wilson Tag HTML5 Path: " ${MTWILSON_TAG_HTML5_DIR:-$MTWILSON_TAG_HTML5_DIR_TEMP}
  if ! validate_path_executable "$MTWILSON_TAG_HTML5_DIR"; then exit -1; fi
fi

prompt_with_default MTWILSON_TAG_CERT_IMPORT_AUTO "Mt Wilson Tag Certificate Auto Import: " ${MTWILSON_TAG_CERT_IMPORT_AUTO:-true}
update_property_in_file "mtwilson.atag.html5.dir" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_HTML5_DIR"
update_property_in_file "tag.provision.autoimport" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_CERT_IMPORT_AUTO"

# remaining properties
prompt_with_default TAG_PROVISION_EXTERNAL "Use external CA instead of the built-in CA? " ${TAG_PROVISION_EXTERNAL:-false}
prompt_with_default TAG_PROVISION_XML_ENCRYPTION_REQUIRED "XML encryption required? " ${TAG_PROVISION_XML_ENCRYPTION_REQUIRED:-false}
prompt_with_default_password TAG_PROVISION_XML_PASSWORD "XML encryption password: " ${TAG_PROVISION_XML_PASSWORD:-$(generate_password 16)}
#prompt_with_default TAG_PROVISION_SELECTION_DEFAULT "Default tag provisioning selection: " ${TAG_PROVISION_SELECTION_DEFAULT:-default}
prompt_with_default TAG_VALIDITY_SECONDS "Tag certificate validity duration: " ${TAG_VALIDITY_SECONDS:-31536000}
prompt_with_default TAG_ISSUER_DN "Tag issuer distinguished name: " ${TAG_ISSUER_DN:-"CN=mtwilson-tag-ca"}
update_property_in_file "tag.provision.external" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_EXTERNAL"
update_property_in_file "tag.provision.xml.encryption.required" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_XML_ENCRYPTION_REQUIRED"
update_property_in_file "tag.provision.xml.encryption.password" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_XML_PASSWORD"
#update_property_in_file "tag.provision.selection.default" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_SELECTION_DEFAULT"
update_property_in_file "tag.validity.seconds" $CONFIG_DIR/mtwilson.properties "$TAG_VALIDITY_SECONDS"
update_property_in_file "tag.issuer.dn" $CONFIG_DIR/mtwilson.properties "$TAG_ISSUER_DN"

#if [ ! -f $MTWILSON_TAG_KEYSTORE ]; then
#  if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
#  keytool=${JAVA_HOME}/bin/keytool
#  $keytool -genkey -v -alias "$MTWILSON_TAG_SERVER_PRIVATE" -dname "CN=$MTWILSON_TAG_SERVER_PRIVATE" -keypass $MTWILSON_TAG_KEY_PASSWORD -keystore $MTWILSON_TAG_KEYSTORE -storepass $MTWILSON_TAG_KEYSTORE_PASSWORD -keyalg "RSA" -sigalg "MD5withRSA" -keysize 2048 -validity 3650
#  $keytool -export -v -alias "$MTWILSON_TAG_SERVER_PRIVATE" -file $CONFIG_DIR/serverAtag.cer -keystore $MTWILSON_TAG_KEYSTORE -storepass $MTWILSON_TAG_KEYSTORE_PASSWORD
#  openssl x509 -inform der -in $CONFIG_DIR/serverAtag.cer -outform pem >> $CONFIG_DIR/tag-cacerts.pem
#fi

#call_setupcommand create-database
call_tag_setupcommand setup-manager update-extensions-cache-file 2> /dev/null
call_tag_setupcommand setup-manager initialize-db --force

call_tag_setupcommand tag-init-database
call_tag_setupcommand tag-create-ca-key "CN=assetTagService"
call_tag_setupcommand tag-export-file cacerts | grep -v ":" >> $CONFIG_DIR/tag-cacerts.pem
call_tag_setupcommand tag-create-mtwilson-client --url="$MTWILSON_TAG_URL" --username="$MTWILSON_TAG_API_USERNAME" --password="$MTWILSON_TAG_API_PASSWORD"
if [ -n "$MTWILSON_TAG_ADMIN_PASSWORD" ]; then
  export MTWILSON_TAG_ADMIN_PASSWORD
  call_tag_setupcommand login-password ${MTWILSON_TAG_ADMIN_USERNAME:-tagadmin} env:MTWILSON_TAG_ADMIN_PASSWORD --permissions tag_certificates:create tag_certificates:deploy tag_certificates:import tag_certificates:search tpm_passwords:retrieve hosts:search
else
  echo_warning "Skipping creation of tag admin user because MTWILSON_TAG_ADMIN_PASSWORD is not set"
fi

#user is approved directly in TagCreateMtWilsonClient now
#fingerprint=`openssl dgst -sha256 $CONFIG_DIR/serverAtag.cer | awk -F= '{print $2}' | sed -e 's/^ *//' -e 's/ *$//'`
#call_tag_setupcommand ApproveMtWilsonClient --fingerprint="$fingerprint"

#for tag encryption
mkdir -p /opt/mtwilson/features/tag/var
mkdir -p /opt/mtwilson/features/tag/bin
cp encrypt.sh /opt/mtwilson/features/tag/bin
cp decrypt.sh /opt/mtwilson/features/tag/bin
chmod 755 /opt/mtwilson/features/tag/bin/encrypt.sh
chmod 755 /opt/mtwilson/features/tag/bin/decrypt.sh

##############################################################################################################################################################################


if [ ! -z "$opt_logrotate" ]; then
  echo "Installing Log Rotate..." | tee -a  $INSTALL_LOG_FILE
  ./$logrotate_installer
  echo "Log Rotate installed" | tee -a  $INSTALL_LOG_FILE
fi

mkdir -p /etc/logrotate.d

if [ ! -a /etc/logrotate.d/mtwilson ]; then
 echo "/usr/share/glassfish4/glassfish/domains/domain1/logs/server.log {
    missingok
    notifempty
    rotate $LOG_OLD
    size $LOG_SIZE
    $LOG_ROTATION_PERIOD
    $LOG_COMPRESS
    $LOG_DELAYCOMPRESS
    $LOG_COPYTRUNCATE
}

/usr/share/apache-tomcat-7.0.34/logs/catalina.out {
    missingok
    notifempty
    rotate $LOG_OLD
    size $LOG_SIZE
    $LOG_ROTATION_PERIOD
    $LOG_COMPRESS
    $LOG_DELAYCOMPRESS
    $LOG_COPYTRUNCATE
}" > /etc/logrotate.d/mtwilson.logrotate
fi

if [ ! -z "$opt_monit" ] && [ -n "$monit_installer" ]; then
  echo "Installing Monit..." | tee -a  $INSTALL_LOG_FILE
  ./$monit_installer  >> $INSTALL_LOG_FILE 
  echo "Monit installed" | tee -a  $INSTALL_LOG_FILE
fi

mkdir -p /etc/monit/conf.d

# create the monit rc files

#glassfish.mtwilson
if [ -z "$NO_GLASSFISH_MONIT" ]; then 
  if [ ! -a /etc/monit/conf.d/glassfish.mtwilson ]; then
    echo "# Verify glassfish is installed (change path if Glassfish is installed to a different directory)
      check file gf_installed with path "/usr/share/glassfish4/bin/asadmin"
      group gf_server
      if does not exist then unmonitor

      # MtWilson Glassfish services
      check host mtwilson-version-glassfish with address 127.0.0.1
      group gf_server
      start program = \"/usr/local/bin/mtwilson start\" with timeout 120 seconds
      stop program = \"/usr/local/bin/mtwilson stop\" with timeout 120 seconds
      if failed port 8181 TYPE TCPSSL PROTOCOL HTTP
        and request "/mtwilson/v2/version" for 2 cycles
      then restart
      if 3 restarts within 10 cycles then timeout
      depends on gf_installed" > /etc/monit/conf.d/glassfish.mtwilson
  fi
fi

#tomcat.mtwilson
if [ -z "$NO_TOMCAT_MONIT" ]; then 
  if [ ! -a /etc/monit/conf.d/tomcat.mtwilson ]; then
    echo "# Verify tomcat is installed (change path if Tomcat is installed to a different directory)
      check file tc_installed with path \"/usr/share/apache-tomcat-7.0.34/bin/catalina.sh\"
      group tc_server
      if does not exist then unmonitor
    
      # MtWilson Tomcat services
      check host mtwilson-version-tomcat with address 127.0.0.1
      group tc_server
      start program = \"/usr/local/bin/mtwilson start\" with timeout 120 seconds
      stop program = \"/usr/local/bin/mtwilson stop\" with timeout 120 seconds
      if failed port 8443 TYPE TCPSSL PROTOCOL HTTP
        and request "/mtwilson/v2/version" for 2 cycles
      then restart
      if 3 restarts within 10 cycles then timeout
      depends on tc_installed" > /etc/monit/conf.d/tomcat.mtwilson
  fi
fi

if [ -z "$NO_POSTGRES_MONIT" ]; then 
  if [ ! -a /etc/monit/conf.d/postgres.mtwilson ]; then 
    echo "check process postgres matching \"postgresql\"
      group pg-db
      start program = \"/usr/sbin/service postgresql start\"
      stop program = \"/usr/sbin/service postgresql stop\"
      if failed unixsocket /var/run/postgresql/.s.PGSQL.${POSTGRES_PORTNUM:-5432} protocol pgsql 
      then restart
      if failed host 127.0.0.1 port ${POSTGRES_PORTNUM:-5432} protocol pgsql then restart
      if 5 restarts within 5 cycles then timeout
      depends on pg_bin
  
      check file pg_bin with path \"/usr/bin/psql\"
      group pg-db
      if does not exist then unmonitor" > /etc/monit/conf.d/postgres.mtwilson
  fi
fi

if [ -z "$NO_MYSQL_MONIT" ]; then 
  if [ ! -a /etc/monit/conf.d/mysql.mtwilson ]; then 
    echo "check process mysql matching \"mysql\"
      group mysql_db
      start program = \"/usr/sbin/service mysql start\"
      stop program = \"/usr/sbin/service mysql stop\"
      if failed host 127.0.0.1 port ${MYSQL_PORTNUM:-3306} protocol mysql then restart
      if 5 restarts within 5 cycles then timeout
      depends on mysql_bin
      depends on mysql_rc

      check file mysql_bin with path /usr/sbin/mysqld
      group mysql_db
      if does not exist then unmonitor

      check file mysql_rc with path /etc/init.d/mysql
      group mysql_db
      if does not exist then unmonitor" > /etc/monit/conf.d/mysql.mtwilson
  fi
fi

echo  -n "Restarting monit service so new configs take effect... "
service monit restart > /dev/null 2>&1
echo "Done"

if [ "${LOCALHOST_INTEGRATION}" == "yes" ]; then
  mtwilson localhost-integration 127.0.0.1 "$MTWILSON_SERVER_IP_ADDRESS"
fi

#Register mtwilson as a startup script, remove previous service startup scripts if they exist
register_startup_script /usr/local/bin/mtwilson mtwilson >> $INSTALL_LOG_FILE
remove_startup_script "asctl" >> $INSTALL_LOG_FILE
remove_startup_script "msctl" >> $INSTALL_LOG_FILE
remove_startup_script "mtwilson-portal" >> $INSTALL_LOG_FILE
remove_startup_script "tdctl" >> $INSTALL_LOG_FILE
remove_startup_script "wlmctl" >> $INSTALL_LOG_FILE

#Save variables to properties file
#if using_mysql; then   
#  mysql_write_connection_properties /etc/intel/cloudsecurity/mtwilson.properties mtwilson.db
#elif using_postgres; then
#  postgres_write_connection_properties /etc/intel/cloudsecurity/mtwilson.properties mtwilson.db
#fi

# last chance to set permissions
chmod 600 /etc/intel/cloudsecurity/*.properties 2>/dev/null

echo "Restarting webservice for all changes to take effect"
#Restart webserver
if using_glassfish; then
  update_property_in_file "mtwilson.webserver.vendor" /etc/intel/cloudsecurity/mtwilson.properties "glassfish"
  update_property_in_file "glassfish.admin.username" /etc/intel/cloudsecurity/mtwilson.properties "$WEBSERVICE_MANAGER_USERNAME"
  update_property_in_file "glassfish.admin.password" /etc/intel/cloudsecurity/mtwilson.properties "$WEBSERVICE_MANAGER_PASSWORD"
  glassfish_admin_user
  glassfish_restart
elif using_tomcat; then
  update_property_in_file "mtwilson.webserver.vendor" /etc/intel/cloudsecurity/mtwilson.properties "tomcat"
  update_property_in_file "tomcat.admin.username" /etc/intel/cloudsecurity/mtwilson.properties "$WEBSERVICE_MANAGER_USERNAME"
  update_property_in_file "tomcat.admin.password" /etc/intel/cloudsecurity/mtwilson.properties "$WEBSERVICE_MANAGER_PASSWORD"
  tomcat_restart
fi

echo "Log file for install is located at $INSTALL_LOG_FILE"
if [ -n "$INSTALLED_MARKER_FILE" ]; then
 touch $INSTALLED_MARKER_FILE
fi



