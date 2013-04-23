#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=privacyca
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
#mysql_required_version=5.0
#glassfish_required_version=3.0
#java_required_version=1.6.0_29
#tomcat_required_version=6.0.29
#tomcat_parent_dir=/usr/lib
#tomcat_name=apache-tomcat-6.0.29
#APPLICATION_YUM_PACKAGES="make gcc openssl libssl-dev mysql-client-5.1"
#APPLICATION_APT_PACKAGES="dpkg-dev make gcc openssl libssl-dev mysql-client-5.1"
webservice_application_name=HisPrivacyCAWebServices2

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi


# if there's already a previous version installed, uninstall it
pcactl=`which pcactl 2>/dev/null`
if [ -f "$asctl" ]; then
  echo "Uninstalling previous version..."
  $pcactl uninstall
fi

# detect the packages we have to install
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
WAR_PACKAGE=`ls -1 *.war 2>/dev/null | tail -n 1`

# copy application files to /opt
mkdir -p "${package_dir}"
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
cp $WAR_PACKAGE "${package_dir}"
chmod 600 privacyca-client.properties
cp privacyca-client.properties "${package_dir}/privacyca-client.properties.example"

#copy jars to endorsed
echo "TOMCAT_HOME:${TOMCAT_HOME}"
if [[ -f ! ${TOMCAT_HOME}/endorsed ]]; then
  mkdir ${TOMCAT_HOME}/endorsed
fi
cp jaxws-api.jar ${TOMCAT_HOME}/endorsed/
cp jaxws-rt.jar ${TOMCAT_HOME}/endorsed/
cp jaxws-tools.jar ${TOMCAT_HOME}/endorsed/

# copy configuration file template to /etc
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
if [ -f "${intel_conf_dir}/privacyca-client.properties" ]; then
  echo_warning "Configuration file privacyca-client.properties already exists"
else
  cp privacyca-client.properties "${intel_conf_dir}/privacyca-client.properties"
fi
#chmod 600 "${package_name}.properties"
#if [ -f "${package_config_filename}" ]; then
#  echo "Copying sample configuration file to ${package_config_filename}.example"
#  cp "${package_name}.properties" "${package_config_filename}.example"
#else
#  cp "${package_name}.properties" "${package_config_filename}"
#fi



# Environment:
# - glassfish_required_version
glassfish_privacyca_install() {
  local GLASSFISH_PACKAGE="${1:-glasfish3-with-privacyca.tgz}"
  GLASSFISH_YUM_PACKAGES="unzip"
  GLASSFISH_APT_PACKAGES="unzip"
  glassfish_detect
  if [[ -z "$GLASSFISH_HOME" || -z "$glassfish" ]]; then
    if [ -d /usr/share/glassfish3 ]; then
      # we do not remove it automatically in case there are applications or data in there that the user wants to save!!
      echo_warning "Glassfish not detected but /usr/share/glassfish3 exists"
      echo "Remove /usr/share/glassfish3 and try again"
      return 1
    fi
    if [[ -z "$GLASSFISH_PACKAGE" || ! -f "$GLASSFISH_PACKAGE" ]]; then
      echo_failure "Missing Glassfish installer: $GLASSFISH_PACKAGE"
      return 1
    fi
    auto_install "Glassfish requirements" "GLASSFISH"
    echo "Installing $GLASSFISH_PACKAGE"
    gunzip -c $GLASSFISH_PACKAGE | tar xf -
    mv glassfish3 /usr/share/
    # Glassfish requires hostname to be mapped to 127.0.0.1 in /etc/hosts
    if [ -f "/etc/hosts" ]; then
        local hostname=`hostname`
        local found=`cat "/etc/hosts" | grep "^127.0.0.1" | grep "$hostname"`
        if [ -z "$found" ]; then
          local datestr=`date +%Y-%m-%d.%H%M`
          cp /etc/hosts /etc/hosts.${datestr}
          local updated=`sed -re "s/^(127.0.0.1\s.*)$/\1 ${hostname}/" /etc/hosts`
          echo "$updated" > /etc/hosts
        fi
    fi
    glassfish_detect
    if [[ -z "$GLASSFISH_HOME" || -z "$glassfish" ]]; then
      echo_failure "Unable to auto-install Glassfish"
      echo "Glassfish download URL:"
      echo "http://glassfish.java.net/"
    fi
  else
    echo "Glassfish is already installed in $GLASSFISH_HOME"
  fi
}



# SCRIPT EXECUTION
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE

# copy control script to /usr/local/bin and finish setup
chmod 700 pcactl
mkdir -p /usr/local/bin
cp pcactl /usr/local/bin
/usr/local/bin/pcactl setup
register_startup_script /usr/local/bin/pcactl pcactl >> $INSTALL_LOG_FILE


if using_glassfish; then
  glassfish_permissions "${intel_conf_dir}"
  glassfish_permissions "${package_dir}"
fi
