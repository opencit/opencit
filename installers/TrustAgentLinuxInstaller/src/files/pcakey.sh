#!/bin/bash
#
# description: Get the PCA key and generate an SSL certificate for the trust agent (to answer attestation requests)
#

intel_conf_dir=/etc/intel/cloudsecurity
package_name=trustagent
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f "${package_dir}/functions" ]; then . "${package_dir}/functions"; else echo "Missing file: ${package_dir}/functions"; exit 1; fi
if [ -f "${package_dir}/version" ]; then . "${package_dir}/version"; else echo_warning "Missing file: ${package_dir}/version"; fi
if [ -f "${package_env_filename}" ]; then . "${package_env_filename}"; else echo_warning "Missing file: ${package_env_filename}"; fi
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null
if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi

# TODO: move this function to mtwilson-util. it's not the same as update_property_in_file because it's specific to the /etc/hosts format
# update the fqdn for an ip address in the /etc/hosts file, replacing the previous value and all its aliases
# parameters: ip address, fqdn or hostname
# example: update_property_in_file MYFLAG FILENAME true
replace_hostname_in_etc_hosts_NOT_COMPLETE__NOT_IN_USE_YET() {
  local hostname="${1}"
  local old_ipaddress="${2}"
  local new_ipaddress="${3}"
  local ispresent=`grep "^${old_ipaddress}" /etc/hosts`
  if [ -n "$ispresent" ]; then
      # replace the existing entry of hostname with old-hostname
      # add a new entry for hostname with new ip address
      # replace just that line in the file and save the file
      # TODO: need to decide if we want to remove the fqdn from its existing ip and move it to a new ip ??? or just change the ip under it ....
            updatedcontent=`sed -re "s/^(${ipaddress})(.*)/\1=${value}/" /etc/hosts`
      echo "$updatedcontent" > /etc/hosts
  else
      # property is not already in file so add it. extra newline in case the last line in the file does not have a newline
      echo "" >> /etc/hosts
      echo "${new_ipaddress}    ${hostname}" >> /etc/hosts
  fi
}

# Check if there is a username and password already configured 
server_pca_username="$PRIVACYCA_DOWNLOAD_USERNAME"   #`read_property_from_file ClientFilesDownloadUsername ${intel_conf_dir}/PrivacyCA.properties`
server_pca_password="$PRIVACYCA_DOWNLOAD_PASSWORD"   #`read_property_from_file ClientFilesDownloadPassword ${intel_conf_dir}/PrivacyCA.properties`
client_pca_username="${server_pca_username}" #:-"$PRIVACYCA_DOWNLOAD_USERNAME"}   #`read_property_from_file ClientFilesDownloadUsername ${intel_conf_dir}/privacyca-client.properties`}
client_pca_password="${server_pca_password}" #:-"$PRIVACYCA_DOWNLOAD_PASSWORD"}   #`read_property_from_file ClientFilesDownloadPassword ${intel_conf_dir}/privacyca-client.properties`}

prompt_with_default PRIVACYCA_SERVER "Privacy CA Server IP Address:"  "https://mtwilsonIP:port#"
echo "Login to download the Privacy CA client files"
prompt_with_default PRIVACYCA_DOWNLOAD_USERNAME "Username:" ${client_pca_username}
prompt_with_default_password PRIVACYCA_DOWNLOAD_PASSWORD "Password:" ${client_pca_password}

if [ -z "${PRIVACYCA_SERVER}" ]; then
  echo_failure "Cannot connect to Privacy CA: missing IP address"
  exit 1
fi

# Try to download the Privacy CA files using TLSv1, then SSLv3, then any protocol (SSLv2 with protocol negotiation allowed, which could still result in a more recent protocol selection)
echo "Connecting to PCA..."
zipfile=/tmp/clientfiles.zip
touch $zipfile
chmod 600 $zipfile
encodedPW=$(echo "${PRIVACYCA_DOWNLOAD_PASSWORD}" | sed -e 's/%/%25/g' -e 's/ /%20/g' -e 's/!/%21/g' -e 's/"/%22/g' -e 's/#/%23/g' -e 's/\$/%24/g' -e 's/\&/%26/g' -e 's/'\''/%27/g' -e 's/(/%28/g' -e 's/)/%29/g' -e 's/\*/%2a/g' -e 's/+/%2b/g' -e 's/,/%2c/g' -e 's/-/%2d/g' -e 's/\./%2e/g' -e 's/\//%2f/g' -e 's/:/%3a/g' -e 's/;/%3b/g' -e 's//%3e/g' -e 's/?/%3f/g' -e 's/@/%40/g' -e 's/\[/%5b/g' -e 's/\\/%5c/g' -e 's/\]/%5d/g' -e 's/\^/%5e/g' -e 's/_/%5f/g' -e 's/`/%60/g' -e 's/{/%7b/g' -e 's/|/%7c/g' -e 's/}/%7d/g' -e 's/~/%7e/g')
wget --secure-protocol=TLSv1 --no-proxy --no-check-certificate -q "${PRIVACYCA_SERVER}/HisPrivacyCAWebServices2/clientfiles.zip?user=${PRIVACYCA_DOWNLOAD_USERNAME}&password=${encodedPW}" -O $zipfile
if [[ -s $zipfile ]]; then
  unzip -o $zipfile -d /etc/intel/cloudsecurity
  rm $zipfile
else
    wget --secure-protocol=SSLv3 --no-proxy --no-check-certificate -q "${PRIVACYCA_SERVER}/HisPrivacyCAWebServices2/clientfiles.zip?user=${PRIVACYCA_DOWNLOAD_USERNAME}&password=${encodedPW}" -O $zipfile
  if [[ -s $zipfile ]]; then
    unzip -o $zipfile -d /etc/intel/cloudsecurity
    rm $zipfile
  else
    wget --no-proxy --no-check-certificate -q "${PRIVACYCA_SERVER}/HisPrivacyCAWebServices2/clientfiles.zip?user=${PRIVACYCA_DOWNLOAD_USERNAME}&password=${encodedPW}" -O $zipfile
    if [[ -s $zipfile ]]; then
      unzip -o $zipfile -d /etc/intel/cloudsecurity
      rm $zipfile
    else
      echo_failure "Unable to download Privacy CA files: try 'pcakey' again, check server address, username, and password"
      exit 1
    fi
  fi
fi

chmod 600 /etc/intel/cloudsecurity/hisprovisioner.properties
# these are created AFTER the trust agent registers with privacy ca:
#chmod 700 /etc/intel/cloudsecurity/cert
#chmod 600 /etc/intel/cloudsecurity/cert/*

keytool=${JAVA_HOME}/bin/keytool
keystore=$intel_conf_dir/trustagent.jks
storepass="$TRUSTAGENT_KEYSTORE_PASS"   #`read_property_from_file trustagent.keystore.password "${package_config_filename}"`
if [ -z "$storepass" ]; then
  if [ -f "$keystore" ]; then 
    backupKeystoreFilename=`backup_file $keystore`
    echo_warning "Trust Agent keystore exists but password is not configured; saved to $backupKeystoreFilename";
    # now remove the old keystore so we can create a new one below
    rm $keystore
  fi
  storepass=$(generate_password 32)
  update_property_in_file trustagent.keystore.password "${package_config_filename}" $storepass
  export TRUSTAGENT_KEYSTORE_PASS="$storepass"
fi

generate_ta_ssl() {
  # Ensure the Keystore exists and generate Trust Agent's SSL Certificate
  # TODO: This certificate does not have the alternative name extension. Get the localhost IP address and use Mt Wilson's SSL Certificate creation function (Java) to create a better certificate, or openssl, or Java 7's keytool (Java 6 and earlier do not support the extension)
  if [ -f "${keystore}" ]; then
    has_ssl=`$keytool -list -keystore "${keystore}" -storepass "${storepass}" | grep PrivateKeyEntry | grep -i "^TrustAgentSSL"`
  fi
  if [ -z "$has_ssl" ]; then
    $keytool -genkey -alias "TrustAgentSSL" -keyalg RSA  -keysize 2048 -keystore "${keystore}" -storepass "${storepass}" -dname "CN=TrustAgent, OU=Mt Wilson, O=Customer, C=US" -validity 3650  -keypass "${storepass}"
  fi
  chmod 600 "$keystore"
}

get_privacyca_ssl() {
  ipPortAddr=`echo ${PRIVACYCA_SERVER} | sed 's/https:\/\///g'`
  openssl s_client -tls1 -connect $ipPortAddr </dev/null >${package_dir}/cert/privacyca.ssl.txt 2>/dev/null
  openssl x509 -in ${package_dir}/cert/privacyca.ssl.txt -inform pem -out ${package_dir}/cert/privacyca.ssl.crt -outform der
  if [ $? == 1 ]; then
    openssl s_client -ssl3 -connect $ipPortAddr </dev/null >${package_dir}/cert/privacyca.ssl.txt 2>/dev/null
    openssl x509 -in ${package_dir}/cert/privacyca.ssl.txt -inform pem -out ${package_dir}/cert/privacyca.ssl.crt -outform der
    if [ $? == 1 ]; then
      openssl s_client -connect $ipPortAddr </dev/null >${package_dir}/cert/privacyca.ssl.txt 2>/dev/null
      openssl x509 -in ${package_dir}/cert/privacyca.ssl.txt -inform pem -out ${package_dir}/cert/privacyca.ssl.crt -outform der
    fi
  fi
  if [ -f "${package_dir}/cert/privacyca.ssl.crt" ]; then rm ${package_dir}/cert/privacyca.ssl.txt; fi
  echo "Importing Privacy CA SSL Certificate into Trust Agent's Keystore..."
  local datestr=`date +%Y%m%d.%H%M`
  $keytool -import -noprompt -keystore $keystore -storepass "${storepass}" -file ${package_dir}/cert/privacyca.ssl.crt -alias "privacyca-$datestr"
}

import_privacyca_files() {
  if [[ -f /etc/intel/cloudsecurity/PrivacyCA.cer && -f /etc/intel/cloudsecurity/endorsement.p12 && -f /etc/intel/cloudsecurity/hisprovisioner.properties ]]; then
    echo_success "Installed Privacy CA client files"
  else
    echo_failure "Privacy CA setup is not complete"
    report_files_exist /etc/intel/cloudsecurity/PrivacyCA.cer /etc/intel/cloudsecurity/endorsement.p12 /etc/intel/cloudsecurity/hisprovisioner.properties
  fi
}

generate_ta_ssl
get_privacyca_ssl
import_privacyca_files


