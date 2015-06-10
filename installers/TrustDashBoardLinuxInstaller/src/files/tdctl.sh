#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
script_name=tdctl
intel_conf_dir=/etc/intel/cloudsecurity
package_name=trust-dashboard
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install
package_keystore_users_dir=/var/opt/intel/${package_name}/users
#mysql_required_version=5.0
#mysql_setup_log=/var/log/intel.${package_name}.install.log
#mysql_script_dir=${package_dir}/database
#glassfish_required_version=4.0
webservice_application_name=TrustDashBoard
#java_required_version=1.7.0_51

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f "${package_dir}/functions" ]; then . "${package_dir}/functions"; else echo "Missing file: ${package_dir}/functions"; exit 1; fi
if [ -f "${package_dir}/version" ]; then . "${package_dir}/version"; else echo_warning "Missing file: ${package_dir}/version"; fi
shell_include_files "${package_env_filename}" "${package_install_filename}"
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null
#if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi


#configure_api_baseurl() {
#  # setup mtwilson.api.baseurl
#  local default_server_url=`read_property_from_file mtwilson.api.baseurl ${package_config_filename}`
#  local server_url_in
#  if [ -n "${MTWILSON_API_BASEURL}" ]; then
#    server_url_in="${MTWILSON_API_BASEURL}"
#  elif [ -n "${MTWILSON_SERVER}" ]; then
#    server_url_in="https://${MTWILSON_SERVER}:8181"
#  else
#    prompt_with_default server_url_in "Mt Wilson Server URL:" "${default_server_url}"
#    #server_url_in="https://${server_url_in}:8181"
#  fi
#  update_property_in_file mtwilson.api.baseurl "${package_config_filename}" "${server_url_in}"
#  mtwilson_api_baseurl=${server_url_in}
#}

configure_keystore_dir() {
  # setup mtwilson.mc.keystore.dir
  local configured_keystore_dir="$TDBP_KEYSTORE_DIR"   #`read_property_from_file mtwilson.tdbp.keystore.dir ${package_config_filename}`
  package_keystore_users_dir=${configured_keystore_dir:-"${package_keystore_users_dir}"}
  if [[ "$configured_keystore_dir" != "$package_keystore_users_dir" ]]; then
    update_property_in_file mtwilson.tdbp.keystore.dir "${package_config_filename}" "${package_keystore_users_dir}"
  fi
  mkdir -p ${package_keystore_users_dir}
}



setup_interactive_install() {
  configure_api_baseurl "${package_config_filename}"
  configure_keystore_dir

  if using_mysql; then   
    if [ -n "$mysql" ]; then
      mysql_configure_connection "${package_config_filename}" mountwilson.tdbp.db
      mysql_create_database
      mtwilson setup InitDatabase mysql
    fi
  elif using_postgres; then
    if [ -n "$psql" ]; then
      postgres_configure_connection "${package_config_filename}" mountwilson.tdbp.db
      postgres_create_database
      mtwilson setup InitDatabase postgresql
    else
      echo "psql not defined"
      exit 1
    fi
  fi
  
  if [ -n "$GLASSFISH_HOME" ]; then
    glassfish_running
    if [ -z "$GLASSFISH_RUNNING" ]; then
      glassfish_start_report
    fi
  elif [ -n "$TOMCAT_HOME" ]; then
    tomcat_running
    if [ -z "$TOMCAT_RUNNING" ]; then
      tomcat_start_report
    fi
  fi  
 
  if [ -n "$MTWILSON_SETUP_NODEPLOY" ]; then
    webservice_start_report "${webservice_application_name}"
  else
    webservice_uninstall "${webservice_application_name}"
    webservice_install "${webservice_application_name}" "${package_dir}"/TrustDashBoard.war
    webservice_running_report "${webservice_application_name}"
  fi
}


setup() {
  #mysql_clear; java_clear; glassfish_clear;
  mtwilson setup-env > "${package_env_filename}"
  . "${package_env_filename}"
#  if [[ -z "$JAVA_HOME" || -z "$GLASSFISH_HOME"  ]]; then
#      echo_warning "Missing one or more required packages"
#      setup_print_summary
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
        if using_glassfish; then  
          glassfish_running_report
        elif using_tomcat; then
          tomcat_running_report
        fi
        webservice_running_report "${webservice_application_name}"
        ;;
  restart)
        webservice_stop_report "${webservice_application_name}"
        sleep 2
        webservice_start_report "${webservice_application_name}"
        ;;
  glassfish-restart)
        glassfish_restart
        ;;
  glassfish-stop)
        glassfish_shutdown
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
  rm -f /usr/local/bin/${script_name}
        ;;
  help)
        echo "Usage: ${script_name} {setup|start|stop|status|uninstall}"
        ;;
  *)
        echo "Usage: ${script_name} {setup|start|stop|status|uninstall}"
        exit 1
esac

exit $RETVAL
