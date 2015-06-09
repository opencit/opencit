#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=management-service
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_name}.env
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
load_conf 2>&1 >/dev/null
load_defaults 2>&1 >/dev/null

# if there's already a previous version installed, uninstall it
msctl=`which msctl 2>/dev/null`
if [ -f "$msctl" ]; then
  echo "Uninstalling previous version..."
  $msctl uninstall 2>/dev/null
fi

# detect the packages we have to install
#JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
#GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
#WAR_PACKAGE=`ls -1 *.war 2>/dev/null | tail -n 1`

# copy application files to /opt
mkdir -p "${package_dir}"
mkdir -p "${package_dir}"/database
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
#cp $WAR_PACKAGE "${package_dir}"
#cp sql/*.sql "${package_dir}"/database/
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
chown $MTWILSON_USERNAME:$MTWILSON_USERNAME ${package_config_filename}

# Create a random password and update the property file of the management service
mypassword16=`generate_password 16`
update_property_in_file mtwilson.api.key.password "${package_config_filename}" "$mypassword16"
export API_KEY_PASS="$mypassword16"
username="$API_KEY_ALIAS"   #`read_property_from_file mtwilson.api.key.alias "${package_config_filename}"`
mtwilson=`which mtwilson 2>/dev/null`
#redirect the output to dev null since this will fail the first time if mtwilson doesn't already exist
$mtwilson erase-users --user="$username" > /dev/null 2>&1

# SCRIPT EXECUTION
#if using_mysql; then
#  mysql_server_install
#  mysql_install
#fi
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE


# copy control script to /usr/local/bin and finish setup
mkdir -p /opt/mtwilson/bin
cp msctl.sh /opt/mtwilson/bin/msctl
chmod +x /opt/mtwilson/bin/msctl
/opt/mtwilson/bin/msctl setup
#register_startup_script /opt/mtwilson/bin/msctl msctl >> $INSTALL_LOG_FILE

if using_glassfish; then
  glassfish_permissions "${intel_conf_dir}"
  glassfish_permissions "${package_dir}"
elif using_tomcat; then
  tomcat_permissions "${intel_conf_dir}"
  tomcat_permissions "${package_dir}"
fi