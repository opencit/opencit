#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=trust-dashboard
package_dir=/opt/intel/cloudsecurity/${package_name}
package_var_dir=/var/opt/intel/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
keystore_dir=/
#package_env_filename=${package_name}.env
#package_install_filename=${package_name}.install
#package_name_rpm=ManagementService
#package_name_deb=managementservice
#mysql_required_version=5.0
#glassfish_required_version=4.0
#java_required_version=1.7.0_51
#APPLICATION_YUM_PACKAGES="make gcc openssl libssl-dev mysql-client-5.1"
#APPLICATION_APT_PACKAGES="dpkg-dev make gcc openssl libssl-dev mysql-client-5.1"

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi

# if there's already a previous version installed, uninstall it
tdctl=`which tdctl 2>/dev/null`
if [ -f "$tdctl" ]; then
  echo "Uninstalling previous version..."
  $tdctl uninstall
fi

# detect the packages we have to install
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
WAR_PACKAGE=`ls -1 *.war 2>/dev/null | tail -n 1`

# copy application files to /opt
mkdir -p "${package_dir}"
mkdir -p "${package_dir}"/database
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
cp $WAR_PACKAGE "${package_dir}"
cp sql/*.sql "${package_dir}"/database/
chmod 600 "${package_name}.properties"
cp "${package_name}.properties" "${package_dir}/${package_name}.properties.example"

# copy configuration file template to /etc
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
chmod 600 "${package_name}.properties"
if [ -f "${package_config_filename}" ]; then
  echo_warning "Configuration file ${package_name}.properties already exists"
else
  cp "${package_name}.properties" "${package_config_filename}"
fi

# copy default user to /var/opt
mkdir -p "${package_var_dir}/users"
chmod 700 "${package_var_dir}"



# SCRIPT EXECUTION
  if using_mysql; then   
    if [ -n "$mysql" ]; then
      mysql_configure_connection "${package_config_filename}" mountwilson.tdbp.db
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
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE


# copy control script to /usr/local/bin and finish setup
mkdir -p /usr/local/bin
cp tdctl.sh /usr/local/bin/tdctl
chmod +x /usr/local/bin/tdctl
/usr/local/bin/tdctl setup
#register_startup_script /usr/local/bin/tdctl tdctl

if using_glassfish; then
  glassfish_permissions "${intel_conf_dir}"
  glassfish_permissions "${package_dir}"
  glassfish_permissions "${package_var_dir}"
elif using_tomcat; then
  tomcat_permissions "${intel_conf_dir}"
  tomcat_permissions "${package_dir}"
  tomcat_permissions "${package_var_dir}"
fi