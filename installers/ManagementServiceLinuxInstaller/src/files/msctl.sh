#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
script_name=msctl
intel_conf_dir=/etc/intel/cloudsecurity
package_name=management-service
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install
package_keystore_users_dir=/var/opt/intel/${package_name}/users
#mysql_required_version=5.0
#mysql_setup_log=/var/log/intel.${package_name}.install.log
#mysql_script_dir=${package_dir}/database
#glassfish_required_version=4.0
webservice_application_name=ManagementService
#java_required_version=1.7.0_51

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f "/usr/local/share/mtwilson/util/functions" ]; then . "/usr/local/share/mtwilson/util/functions"; else echo "Missing file: /usr/local/share/mtwilson/util/functions"; exit 1; fi
if [ -f "/usr/local/share/mtwilson/util/version" ]; then . "/usr/local/share/mtwilson/util/version"; else echo_warning "Missing file: /usr/local/share/mtwilson/util/version"; fi
shell_include_files "${package_env_filename}" "${package_install_filename}"
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null
#if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi



configure_keystore_dir() {
  # setup mtwilson.mc.keystore.dir
  local configured_keystore_dir="$MS_KEYSTORE_DIR"   #`read_property_from_file mtwilson.ms.keystore.dir ${package_config_filename}`
  package_keystore_users_dir=${configured_keystore_dir:-"${package_keystore_users_dir}"}
  if [[ "$configured_keystore_dir" != "$package_keystore_users_dir" ]]; then
    update_property_in_file mtwilson.ms.keystore.dir "${package_config_filename}" "${package_keystore_users_dir}"
  fi
  mkdir -p ${package_keystore_users_dir}
}

#configure_api_baseurl() {
#  # setup mtwilson.api.baseurl
#  if [ -n "${MTWILSON_API_BASEURL}" ]; then
#    mtwilson_api_baseurl="${MTWILSON_API_BASEURL}"
#  elif [ -n "${MTWILSON_SERVER}" ]; then
#    if [[
#    mtwilson_api_baseurl="https://${MTWILSON_SERVER}:$DEFAULT_API_PORT"
#  else
#    local configured_api_baseurl=`read_property_from_file mtwilson.api.baseurl ${package_config_filename}`
#    local input_api_baseurl
#    prompt_with_default input_api_baseurl "Mt Wilson Server:" "${configured_server_url}"
#    if [[ "$input_api_baseurl" == http* ]]; then
#      mtwilson_api_baseurl="$input_api_baseurl"
#    else
#      mtwilson_api_baseurl="https://${input_api_baseurl}:$DEFAULT_API_PORT"
#    fi
#  fi
#  update_property_in_file mtwilson.api.baseurl "${package_config_filename}" "${mtwilson_api_baseurl}"
#}


# Must be invoked AFTER mysql_configure_connection
bootstrap_ms_internal_user() {
  echo "Configuring Management Service API client..."

  local configured_keystore_dir="$MS_KEYSTORE_DIR"   #`read_property_from_file mtwilson.ms.keystore.dir ${package_config_filename}`
  package_keystore_users_dir=${package_keystore_users_dir:-"${configured_keystore_dir}"}
  
  ms_key_alias="$API_KEY_ALIAS"   #`read_property_from_file mtwilson.api.key.alias ${package_config_filename}`
  ms_key_password="$API_KEY_PASS"   #`read_property_from_file mtwilson.api.key.password ${package_config_filename}`
  export ms_key_alias=${ms_key_alias:-"ManagementServiceAutomation"}
  export ms_key_password=${ms_key_password:-"password"}

  local configured_api_baseurl="$CONFIGURED_API_BASEURL"   #`read_property_from_file mtwilson.api.baseurl ${package_config_filename}`
  mtwilson_api_baseurl=${mtwilson_api_baseurl:-"${configured_api_baseurl}"}

  # run the bootstrap command
  mtwilson=`which mtwilson 2>/dev/null`
  if [ -z "$mtwilson" ]; then
    echo_failure "Missing mtwilson command line tool"
    return 1
  fi
  # passing the password securely to the tool via the environment variable (must be exported)
  # we need to call setup BootstrapUser here so that the key is created in the DB not on disk
  # user keystores are now stored in the database so removed useless option: --keystore.users.dir="${package_keystore_users_dir}" 
echo "BootstrapUser: $ms_key_alias"
  mtwilson setup BootstrapUser --mtwilson.api.baseurl="${mtwilson_api_baseurl}" "${ms_key_alias}" env:ms_key_password >> $INSTALL_LOG_FILE
  #mtwilson api CreateUser ${package_keystore_users_dir} ${ms_key_alias} env:ms_key_password
  #mtwilson api RegisterUser ${package_keystore_users_dir}/${ms_key_alias}.jks "${mtwilson_api_baseurl}" Attestation,Whitelist env:ms_key_password
  #bootstrap_ms_approve_user ${package_keystore_users_dir} "${ms_key_alias}" "${ms_key_password}"
}

# Parameters:
# - User directory (such as /var/opt/intel/management-console/users)
# - Key alias (such as "admin", corresponding to "admin.jks" in the user directory)
# - Key password
bootstrap_ms_approve_user() {
  if no_java ${java_required_version:-1.7}; then echo "Cannot find Java ${java_required_version:-1.7} or later"; exit 1; fi
  keytool=${JAVA_HOME}/bin/keytool
  local key_dir="${1}"
  local key_alias="${2}"
  local key_password="${3}"
  local key_file="${key_dir}/${key_alias}.jks"
  local key_cert="${key_dir}/${key_alias}.crt"

  mtwilson=`which mtwilson 2>/dev/null`
  if [ -z "$mtwilson" ]; then
    echo_failure "Missing mtwilson command line tool"
    return 1
  fi
  #local encoded_key_alias=`echo "${key_alias}" | mtwilson api EncodeUsername -`
  if [ -f "${key_dir}/${key_alias}" ]; then encoded_key_alias="${key_alias}"; else encoded_key_alias=`echo "${key_alias}" | call_setupcommand EncodeUsername -`; fi

  $keytool -export -alias ${key_alias} -keystore ${key_dir}/${encoded_key_alias}.jks  -storepass ${key_password} -file ${key_dir}/${encoded_key_alias}.crt
  local datestr=`date +%Y-%m-%d.%H%M`
  local cert_id=`shasum -a 256 -b ${key_dir}/${encoded_key_alias}.crt | awk '{ print $1 }'`
  echo "UPDATE mw_api_client_x509 SET enabled=b'1', status='APPROVED', comment='Approved by `whoami` @ Management Service; do not remove' WHERE fingerprint=UNHEX('$cert_id')" > ${mysql_script_dir}/ms_apiclient_autoapprove.${datestr}.sql
  #mysql_configure_connection "${package_config_filename}" mountwilson.ms.db
  #mysql_install_scripts ${mysql_script_dir}/ms_apiclient_autoapprove.${datestr}.sql
}

setup_interactive_install() {
  if using_mysql; then   
    if [ -n "$mysql" ]; then
      mysql_configure_connection "${package_config_filename}" mountwilson.ms.db
      # NOTE: the InitDatabase command is being migrated from a mtwilson-console Command to a mtwilson-setup SetupTask;
      #       if this line stops working, revise to "mtwilson setup init-database mysql"
      mtwilson setup InitDatabase mysql
    fi
  elif using_postgres; then
    if [ -n "$psql" ]; then
      postgres_configure_connection "${package_config_filename}" mountwilson.ms.db
      postgres_create_database
      # NOTE: the InitDatabase command is being migrated from a mtwilson-console Command to a mtwilson-setup SetupTask;
      #       if this line stops working, revise to "mtwilson setup init-database postgresql"
      mtwilson setup InitDatabase postgresql
    else
      echo "psql not defined"
      exit 1
    fi
  fi

  configure_api_baseurl "${package_config_filename}"
  configure_keystore_dir
  if [ -n "$GLASSFISH_HOME" ]; then
    glassfish_running
    if [ -z "$GLASSFISH_RUNNING" ]; then
      #glassfish_start_report
      /opt/mtwilson/bin/mtwilson start
    fi
  elif [ -n "$TOMCAT_HOME" ]; then
    tomcat_running
    if [ -z "$TOMCAT_RUNNING" ]; then
      #tomcat_start_report
      /opt/mtwilson/bin/mtwilson start
    fi
  fi
   
  #if [ -n "$MTWILSON_SETUP_NODEPLOY" ]; then
  #  webservice_start_report "${webservice_application_name}"
  #else
  if [ -z "$MTWILSON_SETUP_NODEPLOY" ]; then
    webservice_uninstall "${webservice_application_name}"
    #webservice_install "${webservice_application_name}" "${package_dir}"/ManagementService.war
    #webservice_running_report "${webservice_application_name}"
  fi
  # no need to create the management automation user as it is not being used.
  #bootstrap_ms_internal_user
}


setup() {
  #mysql_clear; java_clear; glassfish_clear;
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
  #start)
  #      webservice_start_report "${webservice_application_name}"
  #      ;;
  #stop)
  #      webservice_stop_report "${webservice_application_name}"
  #      ;;
  #status)
  #      #if using_glassfish; then  
  #      #  glassfish_running_report
  #      #elif using_tomcat; then
  #      #  tomcat_running_report
  #      #fi
  #      webservice_running_report "${webservice_application_name}"
  #      ;;
  #restart)
  #      webservice_stop_report "${webservice_application_name}"
  #      sleep 2
  #      webservice_start_report "${webservice_application_name}"
  #      ;;
  glassfish-restart)
        glassfish_restart
        ;;
  glassfish-stop)
        glassfish_shutdown
        ;;
  approve-user)
        shift
        bootstrap_ms_approve_user "$@"
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
  #echo "Saved environment in ${myenvdir}/${package_env_filename}"
        ;;
  edit)
        update_property_in_file "${2}" "${package_config_filename}" "${3}"
        ;;
  show)
        read_property_from_file "${2}" "${package_config_filename}"
        ;;
  uninstall)
  #      datestr=`date +%Y-%m-%d.%H%M`
  #      webservice_uninstall "${webservice_application_name}"
  #      mkdir -p "${intel_conf_dir}"
  #      cp "${package_config_filename}" "${intel_conf_dir}"/${package_name}.properties.${datestr}
  #      echo "Saved configuration file in ${intel_conf_dir}/${package_name}.properties.${datestr}"
  #      # prevent disaster by ensuring that package_dir is inside /opt/intel
  #      if [[ "${package_dir}" == /opt/intel/* ]]; then
  #        rm -rf "${package_dir}"
  #      fi
        rm -f /usr/local/bin/${script_name} 2>/dev/null
        ;;
  help)
        echo "Usage: ${script_name} {setup}"
        ;;
  *)
        echo "Usage: ${script_name} {setup}"
        exit 1
esac

exit $RETVAL
