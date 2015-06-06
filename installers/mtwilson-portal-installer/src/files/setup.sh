#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=mtwilson-portal
package_dir=/opt/intel/cloudsecurity/${package_name}
package_var_dir=/var/opt/intel/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
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
mtwilson_portal=`which mtwilson-portal 2>/dev/null`
if [ -f "$mtwilson_portal" ]; then
  echo "Uninstalling previous version..."
  $mtwilson_portal uninstall
fi

# detect the packages we have to install
#APICLIENT_PACKAGE=`ls -1 MtWilsonLinuxUtil*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
WAR_PACKAGE_GLASSFISH=`ls -1 mtwilson-portal-glassfish.war 2>/dev/null | tail -n 1`
WAR_PACKAGE_TOMCAT=`ls -1 mtwilson-portal-tomcat.war 2>/dev/null | tail -n 1`

# copy application files to /opt
mkdir -p "${package_dir}"
mkdir -p "${package_dir}"/database
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"

# select appropriate war file
if using_glassfish; then
  cp $WAR_PACKAGE_GLASSFISH "${package_dir}/mtwilson-portal.war"
elif using_tomcat; then
  cp $WAR_PACKAGE_TOMCAT "${package_dir}/mtwilson-portal.war"
fi

#cp *.sql "${package_dir}"/database/
chmod 600 "${package_name}.properties"
cp "${package_name}.properties" "${package_dir}/${package_name}.properties.example"

# copy configuration file template to /etc
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
if [ -f "${package_config_filename}" ]; then
  echo_warning "Configuration file ${package_name}.properties already exists"
else
  cp "${package_name}.properties" "${package_config_filename}"
fi

# copy default user to /var/opt
mkdir -p "${package_var_dir}/users"
chmod 700 "${package_var_dir}"


# SCRIPT EXECUTION
#chmod +x $APICLIENT_PACKAGE
#./$APICLIENT_PACKAGE

#mysql_server_install
#mysql_install
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE


# copy control script to /usr/local/bin and finish setup
chmod +x mtwilson-portal.sh
mkdir -p /opt/mtwilson/bin
cp mtwilson-portal.sh /opt/mtwilson/bin/mtwilson-portal
/opt/mtwilson/bin/mtwilson-portal setup
#register_startup_script /opt/mtwilson/bin/mtwilson-portal mtwilson-portal >> $INSTALL_LOG_FILE

if using_glassfish; then
  glassfish_permissions "${intel_conf_dir}"
  glassfish_permissions "${package_dir}"
  glassfish_permissions "${package_var_dir}"
elif using_tomcat; then
  tomcat_permissions "${intel_conf_dir}"
  tomcat_permissions "${package_dir}"
  tomcat_permissions "${package_var_dir}"
fi

# Need to update property name (as it has changed) if doing an upgrade
update_property_in_file trustTrue "${package_config_filename}" "images/Trusted.png"
update_property_in_file trustUnknown "${package_config_filename}" "images/Unknown.png"