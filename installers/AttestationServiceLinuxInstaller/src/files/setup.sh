#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=attestation-service
package_dir=/opt/intel/cloudsecurity/${package_name}
package_var_dir=/var/opt/intel/aikverifyhome
package_var_bin_dir=${package_var_dir}/bin
package_config_filename=${intel_conf_dir}/${package_name}.properties
#mysql_required_version=5.0
#glassfish_required_version=4.0
#java_required_version=1.7.0_51

export INSTALL_LOG_FILE=/tmp/mtwilson-install.log

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi


# if there's already a previous version installed, uninstall it
asctl=`which asctl 2>/dev/null`
if [ -f "$asctl" ]; then
  echo "Uninstalling previous version..."
  $asctl uninstall
fi

# detect the packages we have to install
#JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
#GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`
WAR_PACKAGE_GLASSFISH=`ls -1 mtwilson-glassfish.war 2>/dev/null | tail -n 1`
WAR_PACKAGE_TOMCAT=`ls -1 mtwilson-tomcat.war 2>/dev/null | tail -n 1`
WAR_PACKAGE_JETTY=`ls -1 mtwilson-jetty.war 2>/dev/null | tail -n 1`


# copy application files to /opt
mkdir -p "${package_dir}"
#mkdir -p "${package_dir}"/database
chmod 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"

# select appropriate war file
if using_glassfish; then
  cp $WAR_PACKAGE_GLASSFISH "${package_dir}/mtwilson.war"
elif using_tomcat; then
  cp $WAR_PACKAGE_TOMCAT "${package_dir}/mtwilson.war"
fi

#cp sql/*.sql "${package_dir}"/database/
chmod 600 "${package_name}.properties"
cp "${package_name}.properties" "${package_dir}/${package_name}.properties.example"
cp "audit-handler.properties" "${package_dir}/audit-handler.properties.example"

# copy configuration file template to /etc
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
if [ -f "${package_config_filename}" ]; then
  echo_warning "Configuration file ${package_name}.properties already exists"  >> $INSTALL_LOG_FILE
else
  cp "${package_name}.properties" "${package_config_filename}"
fi
if [ -f "${package_dir}/audit-handler.properties" ]; then
  echo_warning "Configuration file audit-handler.properties already exists" >> $INSTALL_LOG_FILE
else
  cp "audit-handler.properties" "${intel_conf_dir}/audit-handler.properties"
fi

# SCRIPT EXECUTION
#if using_mysql; then
#  mysql_server_install
#  mysql_install
#fi
#java_install $JAVA_PACKAGE
#glassfish_install $GLASSFISH_PACKAGE


# copy control script to $MTWILSON_HOME/bin and finish setup
mkdir -p /opt/mtwilson/bin
cp asctl.sh /opt/mtwilson/bin/asctl
chmod +x /opt/mtwilson/bin/asctl
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME ${intel_conf_dir}
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME ${package_dir}
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME /opt/mtwilson/bin/

/opt/mtwilson/bin/asctl setup

aikqverify_install_prereq() {
  DEVELOPER_YUM_PACKAGES="make gcc openssl libssl-dev "
  DEVELOPER_APT_PACKAGES="dpkg-dev make gcc openssl libssl-dev"
  auto_install "Developer tools" "DEVELOPER"   
}

# Compile aikqverify .   removed  mysql-client-5.1  from both yum and apt lists
compile_aikqverify() {
  AIKQVERIFY_OK=''
  cd /var/opt/intel/aikverifyhome/bin
  make  2>&1 > /dev/null
  rm -f aikqverify.o
  rm -f Makefile
  rm -f aikqverify.c
  if [ -e aikqverify ]; then
    AIKQVERIFY_OK=yes
  fi
}

if [ `whoami` == "root" ]; then
 #Code review shall we remove /usr/local/bin/asctl before creating symlink?
 ln -s /opt/mtwilson/bin/asctl /usr/local/bin/asctl
 aikqverify_install_prereq
fi
#register_startup_script /usr/local/bin/asctl asctl >> $INSTALL_LOG_FILE

mkdir -p ${package_var_dir}/bin
mkdir -p ${package_var_dir}/data
chmod +x aikqverify/openssl.sh
cp aikqverify/* ${package_var_dir}/bin/
echo "Compiling aikqverify (may take a long time)... " >> $INSTALL_LOG_FILE
compile_aikqverify >> $INSTALL_LOG_FILE
if [ -n "$AIKQVERIFY_OK" ]; then
  echo "Compile OK"   >> $INSTALL_LOG_FILE
else
  echo "Compile FAILED" >> $INSTALL_LOG_FILE
fi

#change user if root "CODE REVIEW"
if [ `whoami` == "root" ]; then
 chown -R $MTWILSON_USERNAME "${package_var_dir}"
 chmod -R 700 "${package_var_dir}"
 chmod -R 600 "${package_var_dir}/data"

 if using_glassfish; then
   glassfish_permissions "${intel_conf_dir}"
   glassfish_permissions "${package_dir}"
   #glassfish_permissions "${package_var_dir}"
   #glassfish_permissions "${package_var_bin_dir}"
 elif using_tomcat; then
   tomcat_permissions "${intel_conf_dir}"
   tomcat_permissions "${package_dir}"
   #tomcat_permissions "${package_var_dir}"
   #tomcat_permissions "${package_var_bin_dir}" 
 fi
fi
