#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
script_name=asctl
intel_conf_dir=/etc/intel/cloudsecurity
package_name=attestation-service
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install
scripts_dir=/opt/mtwilson/share/scripts
config_dir=/opt/mtwilson/configuration
#mysql_required_version=5.0
#mysql_setup_log=/var/log/intel.${package_name}.install.log
#mysql_script_dir=${package_dir}/database
webservice_application_name=mtwilson
#webservice_application_name=AttestationService
#java_required_version=1.7.0_51

export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f "${scripts_dir}/functions" ]; then . "${scripts_dir}/functions"; else echo "Missing file: ${scripts_dir}/functions"; exit 1; fi
if [ -f "${config_dir}/version" ]; then . "${config_dir}/version"; else echo_warning "Missing file: ${config_dir}/version"; fi
shell_include_files "${package_env_filename}" "${package_install_filename}"
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null
#if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi

# bug #509 the sql script setup is moved to a java program, this function is not needed anymore
#list_mysql_install_scripts() {
#    echo ${mysql_script_dir}/20120327214603_create_changelog.sql
#    echo ${mysql_script_dir}/20120328172740_create_0_5_1_schema.sql
##    echo ${mysql_script_dir}/20120403185252_add_procedure_Insert_GKV_Record.sql
##    echo 20120405021354_add_procedure_authenticate_user.sql
#    echo ${mysql_script_dir}/20120328173612_ta_db-0.5.1-data.sql
##    echo ${mysql_script_dir}/20120405161725_add_procedure_getuserfirstlastnames.sql
#    echo ${mysql_script_dir}/201207101_saml_cache_patch-ta_db-0.5-to-0.5.2.sql
#    echo ${mysql_script_dir}/20120829_audit_log.sql
#    echo ${mysql_script_dir}/20120831_patch_rc2.sql
#    echo ${mysql_script_dir}/20120920085200_patch_rc3.sql
#}


## bug #509 the sql script setup is moved to a java program, this function is not needed anymore
#list_mysql_upgrade_scripts() {
#    echo ${mysql_script_dir}/20120327214603_create_changelog.sql
#    echo ${mysql_script_dir}/20120328172740_create_0_5_1_schema.sql
#    echo ${mysql_script_dir}/20120328172802_patch-ta_db-0.5-to-0.5.1.sql
#    echo ${mysql_script_dir}/20120403185252_add_procedure_Insert_GKV_Record.sql
#    echo ${mysql_script_dir}/201207101_saml_cache_patch-ta_db-0.5-to-0.5.2.sql
#    echo ${mysql_script_dir}/20120829_audit_log.sql
#    echo ${mysql_script_dir}/20120831_patch_rc2.sql
#}




create_saml_key() {
  if no_java ${java_required_version:-1.7}; then echo "Cannot find Java ${java_required_version:-1.7} or later"; exit 1; fi
  # windows path: C:\Intel\CloudSecurity\SAML.jks
  # the saml.keystore.file property is just a file name and not an absolute path
  # prepend the configuration directory to the keystore filename
  if [ ! -f $SAML_KEYSTORE_FILE ]; then
    SAML_KEYSTORE_FILE=${intel_conf_dir}/${SAML_KEYSTORE_FILE}
  fi
  
  if [ -z "$SAML_KEYSTORE_PASSWORD" ]; then
    SAML_KEYSTORE_PASSWORD=$(generate_password 16)
    SAML_KEY_PASSWORD=$SAML_KEYSTORE_PASSWORD
    update_property_in_file saml.keystore.password "${package_config_filename}" "${SAML_KEYSTORE_PASSWORD}"
    update_property_in_file saml.key.password "${package_config_filename}" "${SAML_KEY_PASSWORD}"
  fi

  keytool=${JAVA_HOME}/bin/keytool
  samlkey_exists=`$keytool -list -keystore ${SAML_KEYSTORE_FILE} -storepass ${SAML_KEYSTORE_PASSWORD} | grep PrivateKeyEntry | grep "^${SAML_KEY_ALIAS}"`
  if [ -n "${samlkey_exists}" ]; then
    echo "SAML key with alias ${SAML_KEY_ALIAS} already exists in ${SAML_KEYSTORE_FILE}"
  else
    $keytool -genkey -alias ${SAML_KEY_ALIAS} -keyalg RSA  -keysize 2048 -keystore ${SAML_KEYSTORE_FILE} -storepass ${SAML_KEYSTORE_PASSWORD} -dname "CN=mtwilson, OU=Mt Wilson, O=Intel, L=Folsom, ST=CA, C=US" -validity 3650  -keypass ${SAML_KEY_PASSWORD}
  fi
  chmod 600 ${SAML_KEYSTORE_FILE}
  # export the SAML certificate so it can be easily provided to API clients
  $keytool -export -alias ${SAML_KEY_ALIAS} -keystore ${SAML_KEYSTORE_FILE}  -storepass ${SAML_KEYSTORE_PASSWORD} -file ${intel_conf_dir}/saml.crt
  openssl x509 -in ${intel_conf_dir}/saml.crt -inform der -out ${intel_conf_dir}/saml.crt.pem -outform pem
  chmod 600 ${intel_conf_dir}/saml.crt ${intel_conf_dir}/saml.crt.pem
  chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $SAML_KEYSTORE_FILE ${intel_conf_dir}/saml.crt ${intel_conf_dir}/saml.crt.pem 

  #saml.issuer=https://localhost:8181
  local saml_issuer=""
  saml_issuer="https://${MTWILSON_SERVER:-127.0.0.1}:8443"
    #saml_issuer=`echo $saml_issuer |  sed -e 's/\\//g'`
  
  update_property_in_file saml.issuer "${package_config_filename}" "${saml_issuer}"

}

create_data_encryption_key() {
#  # first check to see if there is a key already set
#  data_encryption_key=`read_property_from_file mtwilson.as.dek ${package_config_filename}`
#  if [[ -n "${data_encryption_key}" ]]; then
#    echo "Data encryption key already exists"
#  else
#    echo "Creating data encryption key"
#####  this is now also called from "mtwilson setup"
    # NOTE: if this line stops working, revise to "mtwilson encrypt-database"
    mtwilson setup EncryptDatabase
#  fi
}

bootstrap_first_user() {
  echo "Configuring Mt Wilson Portal administrator username and password..."

  # run the bootstrap command
  mtwilson=`which mtwilson 2>/dev/null`
  if [ -z "$mtwilson" ]; then
    echo_failure "Missing mtwilson command line tool"
    return 1
  fi

  # bootstrap administrator user with all privileges
  prompt_with_default MC_FIRST_USERNAME "Username:" "admin"
  prompt_with_default_password MC_FIRST_PASSWORD
  export MC_FIRST_USERNAME
  export MC_FIRST_PASSWORD
  echo "asctl setup create-certificate-authority-key..." >>$INSTALL_LOG_FILE
  mtwilson setup V2 create-certificate-authority-key
  cat /etc/intel/cloudsecurity/cacerts.pem >> /etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem
  chown $MTWILSON_USERNAME:$MTWILSON_USERNAME /etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem
  echo "asctl setup create-admin-user..." >>$INSTALL_LOG_FILE
  mtwilson setup V2 create-admin-user
}


configure_privacyca_user() {
  # Now prompt for the client files download username and password
  echo "You need to set a username and password for administrators installing Trust Agents to access the Privacy CA service."
  prompt_with_default PRIVACYCA_DOWNLOAD_USERNAME "PrivacyCA Administrator Username:" admin
  prompt_with_default_password PRIVACYCA_DOWNLOAD_PASSWORD "PrivacyCA Administrator Password:"
  export PRIVACYCA_DOWNLOAD_USERNAME PRIVACYCA_DOWNLOAD_PASSWORD
  #export PRIVACYCA_DOWNLOAD_USERNAME="$PRIVACYCA_DOWNLOAD_USERNAME"
  #export PRIVACYCA_DOWNLOAD_PASSWORD="$PRIVACYCA_DOWNLOAD_PASSWORD"
  #PRIVACYCA_DOWNLOAD_PASSWORD_HASH=`mtwilson setup HashPassword --env-password=PRIVACYCA_DOWNLOAD_PASSWORD`
  #update_property_in_file ClientFilesDownloadUsername "${intel_conf_dir}/PrivacyCA.properties" "${PRIVACYCA_DOWNLOAD_USERNAME}"
  #update_property_in_file ClientFilesDownloadPassword "${intel_conf_dir}/PrivacyCA.properties" "${PRIVACYCA_DOWNLOAD_PASSWORD_HASH}"
  mtwilson login-password $PRIVACYCA_DOWNLOAD_USERNAME env:PRIVACYCA_DOWNLOAD_PASSWORD host_aiks:certify tpm_endorsements:create tpm_endorsements:search tpm_passwords:create tpm_passwords:retrieve tpm_passwords:search tpm_passwords:store tpms:endorse host_signing_key_certificates:create store_host_pre_registration_details:create vm_attestations:create
}


create_privacyca_keys() {
   mtwilson setup-manager create-endorsement-ca >> /dev/null 2>&1
   mtwilson setup-manager create-privacy-ca >> /dev/null 2>&1
}

# The PrivacyCA creates PrivacyCA.p12 on start-up if it's missing; so we ensure it has safe permissions
protect_privacyca_files() {
  local PRIVACYCA_FILES="${intel_conf_dir}/EndorsementCA.p12 ${intel_conf_dir}/PrivacyCA.p12"
  # removed  ${intel_conf_dir}/cacerts.pem  from the list because at this time it isn't created
  # yet, it will be created later with 644 permissions which is fine since it's for public key
  # certificates.
  chmod 600 $PRIVACYCA_FILES
 if using_tomcat; then
    tomcat_permissions $PRIVACYCA_FILES
  fi
}

update_ssl_port() {
  configure_api_baseurl "${package_config_filename}"
  mtwilson setup-manager update-ssl-port >> /dev/null 2>&1
}

setup_interactive_install() {
  if using_mysql; then   
    if [ -n "$mysql" ]; then
      mysql_configure_connection "${package_config_filename}" mountwilson.as.db
      mysql_configure_connection "${intel_conf_dir}/audit-handler.properties" mountwilson.audit.db
      # NOTE: the InitDatabase command is being migrated from a mtwilson-console Command to a mtwilson-setup SetupTask;
      #       if this line stops working, revise to "mtwilson setup init-database mysql"
      mtwilson setup InitDatabase mysql
    fi
  elif using_postgres; then
    if [ -n "$psql" ]; then
      #echo "inside psql: $psql"
      postgres_configure_connection "${package_config_filename}" mountwilson.as.db
      postgres_configure_connection "${intel_conf_dir}/audit-handler.properties" mountwilson.audit.db
      postgres_create_database
      # NOTE: the InitDatabase command is being migrated from a mtwilson-console Command to a mtwilson-setup SetupTask;
      #       if this line stops working, revise to "mtwilson setup init-database postgresql"
      mtwilson setup InitDatabase postgresql
    else
      echo "psql not defined"
      exit 1
    fi
  fi
  update_ssl_port
  create_saml_key 

  configure_privacyca_user
  create_privacyca_keys
  protect_privacyca_files

  create_data_encryption_key
  bootstrap_first_user

 if [ -n "$TOMCAT_HOME" ]; then
    tomcat_running
    if [ -z "$TOMCAT_RUNNING" ]; then
      #tomcat_start_report
      /opt/mtwilson/bin/mtwilson start
    fi
  fi
  
  if [ -n "$MTWILSON_SETUP_NODEPLOY" ]; then
    webservice_start_report "${webservice_application_name}"
  else
    webservice_uninstall "${webservice_application_name}"
    webservice_install "${webservice_application_name}" "${package_dir}"/mtwilson.war
   
      webservice_running_report_wait "${webservice_application_name}"
      mtwilson_running_report_wait
  fi
}

setup() {
#  mysql_clear; java_clear; glassfish_clear;
  mtwilson setup-env > "${package_env_filename}"
  . "${package_env_filename}"
#  if [[ -z "$JAVA_HOME" || -z "$GLASSFISH_HOME" || -z "$mysql" ]]; then
#      echo_warning "Missing one or more required packages"
#      print_env_summary_report
#      exit 1
#  fi
  setup_interactive_install
}



RETVAL=0




# See how we were called.
case "$1" in
  version)
        echo "${package_name}"
  echo "Version ${VERSION:-Unknown}"
  echo "Build ${BUILD:-Unknown}"
        ;;
  start)
        webservice_start_report "${webservice_application_name}"
        ;;
  stop)
        webservice_stop_report "${webservice_application_name}"
        ;;
  status)
      if using_tomcat; then
        tomcat_clear
        tomcat_detect > /dev/null
      fi
        webservice_running_report_wait "${webservice_application_name}"
        ;;
  restart)
        webservice_stop_report "${webservice_application_name}"
        sleep 2
        webservice_start_report "${webservice_application_name}"
        ;;
 
  setup)
        setup
        ;;
  setup-env)
  # for sysadmin convenience
        mtwilson setup-env
        ;;
  setup-env-write)
  # for sysadmin convenience
        mtwilson setup-env > "${package_env_filename}"
        ;;
  edit)
        update_property_in_file "${2}" "${package_config_filename}" "${3}"
        ;;
  show)
        read_property_from_file "${2}" "${package_config_filename}"
        ;;
  uninstall)
        datestr=`date +%Y-%m-%d.%H%M`
        webservice_uninstall "${webservice_application_name}"
        if [ -f "${package_config_filename}" ]; then
          mkdir -p "${intel_conf_dir}"
          cp "${package_config_filename}" "${intel_conf_dir}"/${package_name}.properties.${datestr}
          echo "Saved configuration file in ${intel_conf_dir}/${package_name}.properties.${datestr}"
        fi
        # prevent disaster by ensuring that package_dir is inside /opt/intel
        if [[ "${package_dir}" == /opt/intel/* ]]; then
          rm -rf "${package_dir}"
        fi
        rm /opt/mtwilson/bin/${script_name} 2>/dev/null
        ;;
  saml-createkey)
        create_saml_key
        ;;
  privacyca-setup)
        configure_privacyca_user
        create_privacyca_keys
        protect_privacyca_files
        ;;
  help)
        echo "Usage: ${script_name} {setup|start|stop|status|uninstall|saml-createkey|privacyca-setup}"
        ;;
  *)
        echo "Usage: ${script_name} {setup|start|stop|status|uninstall|saml-createkey|privacyca-setup}"
        exit 1
esac

exit $RETVAL
