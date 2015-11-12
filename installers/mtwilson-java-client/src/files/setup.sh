#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

export mysql_required_version=5.0
#export glassfish_required_version=4.0
#export java_required_version=1.7.0_51

find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin | head -n 1`
  echo $binfile
}

if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

java_installer=`find_installer java`
glassfish_installer=`find_installer glassfish`
monit_installer=`find_installer monit`
mtwilson_util=`find_installer MtWilsonLinuxUtil`
management_service=`find_installer ManagementService`
whitelist_service=`find_installer WLMService`
attestation_service=`find_installer AttestationService`

# Gather default configuration
MTWILSON_SERVER_IP_ADDRESS=${MTWILSON_SERVER_IP_ADDRESS:-$(hostaddress)}
MTWILSON_SERVER=${MTWILSON_SERVER:-$MTWILSON_SERVER_IP_ADDRESS}

# Prompt for installation settings
echo "Please enter the IP Address or Hostname that will identify the Mt Wilson server.
This address will be used in the server SSL certificate and in all Mt Wilson URLs,
such as https://${MTWILSON_SERVER:-127.0.0.1}.
Detected the following options on this server:"
IFS=$'\n'; echo "$(hostaddress_list)"; IFS=' '; hostname;
prompt_with_default MTWILSON_SERVER "Mt Wilson Server:"
export MTWILSON_SERVER
echo

mysql_userinput_connection_properties
export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD

# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER

# Install MySQL server (if user selected localhost)
if [[ "$MYSQL_HOSTNAME" == "127.0.0.1" || "$MYSQL_HOSTNAME" == "localhost" || -n `echo "${hostaddress_list}" | grep "$MYSQL_HOSTNAME"` ]]; then
  mysql_server_install
  mysql_start
fi
mysql_install
export is_mysql_available mysql_connection_error
if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi

chmod +x *.bin
./$java_installer
./$mtwilson_util
./$glassfish_installer
./$attestation_service
./$management_service
./$whitelist_service
./$monit_installer
