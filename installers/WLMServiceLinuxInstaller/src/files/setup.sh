#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi

export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}

echo "ATTESTATION SERVICE setup.sh" >>$INSTALL_LOG_FILE

# this script is only run as part of the larger installation, so
# the following variables must be defined. exit early if there is
# a problem.
if [ -z "$MTWILSON_HOME" ]; then
  echo_failure "Missing environment variable: MTWILSON_HOME"
  exit 1
fi
if [ -z "$MTWILSON_USERNAME" ]; then
  echo_failure "Missing environment variable: MTWILSON_USERNAME"
  exit 1
fi

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=wlm-service
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env
#package_install_filename=${package_name}.install


# if there's already a previous version installed, uninstall it
wlmctl=`which wlmctl 2>/dev/null`
if [ -f "$wlmctl" ]; then
  echo "Uninstalling previous version..."
  $wlmctl uninstall
fi

# detect the packages we have to install
#JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
#GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
#WAR_PACKAGE=`ls -1 *.war 2>/dev/null | tail -n 1`

# copy application files to /opt
mkdir -p "${package_dir}"
#mkdir -p "${package_dir}"/database
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
#cp $WAR_PACKAGE "${package_dir}"
#cp sql/*.sql "${package_dir}"/database/
chmod 600 "${package_name}.properties"
cp "${package_name}.properties" "${package_dir}/${package_name}.properties.example"
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME "${package_dir}"

# copy configuration file template to /etc
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
chmod 600 "${package_name}.properties"
if [ -f "${package_config_filename}" ]; then
  echo_warning "Configuration file ${package_name}.properties already exists"
else
  cp "${package_name}.properties" "${package_config_filename}"
fi
chown $MTWILSON_USERNAME:$MTWILSON_USERNAME "${package_config_filename}"


# SCRIPT EXECUTION
#if using_mysql; then   
#    if [ -n "$mysql" ]; then
#      mysql_configure_connection "${package_config_filename}" mountwilson.as.db
#      mysql_create_database
#      mtwilson setup InitDatabase mysql
#    fi
#  elif using_postgres; then
#    if [ -n "$psql" ]; then
#      postgres_configure_connection "${package_config_filename}" mountwilson.as.db
#      postgres_create_database
#      mtwilson setup InitDatabase postgresql
#    else
#      echo "psql not defined"
#      exit 1
#    fi
#  fi
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE


# copy control script to /usr/local/bin and finish setup
mkdir -p /opt/mtwilson/bin
cp wlmctl.sh /opt/mtwilson/bin/wlmctl
chmod +x /opt/mtwilson/bin/wlmctl

#while changing owner of ${intel_conf_dir} need to put '/' at the end as ${intel_conf_dir} is a symbolic link
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME ${intel_conf_dir}/
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME ${package_dir}

/opt/mtwilson/bin/wlmctl setup
#register_startup_script /opt/mtwilson/bin/wlmctl wlmctl

if using_tomcat; then
  tomcat_permissions "${intel_conf_dir}"
  tomcat_permissions "${package_dir}"
fi
