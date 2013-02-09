#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=trustagent
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=${package_dir}/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install

#java_required_version=1.6.0_29
# commented out from yum packages: tpm-tools-devel curl-devel (not required because we're using NIARL Privacy CA and we don't need the identity command which used libcurl
APPLICATION_YUM_PACKAGES="openssl  trousers trousers-devel tpm-tools make gcc unzip"
# commented out from apt packages: libcurl4-openssl-dev 
APPLICATION_APT_PACKAGES="openssl libssl-dev libtspi-dev libtspi1 trousers make gcc unzip"
# commented out from YAST packages: libcurl-devel tpm-tools-devel.  also zlib and zlib-devel are dependencies of either openssl or trousers-devel
APPLICATION_YAST_PACKAGES="openssl libopenssl-devel trousers trousers-devel tpm-tools make gcc unzip"
# SUSE uses zypper:.  omitting libtspi1 because trousers-devel already depends on a specific version of it which will be isntalled automatically
APPLICATION_ZYPPER_PACKAGES="openssl libopenssl-devel libopenssl1_0_0 openssl-certs trousers-devel"
# other packages in suse:  libopenssl0_9_8 

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi


# Automatic install in 4 steps:
# 1. Install Mt Wilson Linux utilities (and use them in this script)
# 2. Install JDK
# 3. Compile TPM commands
# 4. Install Trust Agent files

# if there's already a previous version installed, uninstall it
tagent=`which tagent`
if [ -f "$tagent" ]; then
  echo "Uninstalling previous version..."
  $tagent uninstall
fi


# packages to install must be in current directory
JAR_PACKAGE=`ls -1 TrustAgent*.jar 2>/dev/null | tail -n 1`
#MTWILSON_UTIL_PACKAGE=`ls -1 mtwilson-util*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`


# copy application files to /opt
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
chmod 600 ${package_name}.properties
cp ${package_name}.properties "${intel_conf_dir}"
chmod 600 TPMModule.properties
cp TPMModule.properties "${intel_conf_dir}"/TPMModule.properties
if [ -f "${package_config_filename}" ]; then
  echo_warning "Copying sample configuration file to ${package_config_filename}.example"
  cp "${package_name}.properties" "${package_config_filename}.example"
else
  cp "${package_name}.properties" "${package_config_filename}"  
fi
mkdir -p "${package_dir}"
mkdir -p "${package_dir}"/bin
mkdir -p "${package_dir}"/cert
mkdir -p "${package_dir}"/data
mkdir -p "${package_dir}"/lib
chmod -R 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
#cp trustagent.jks "${package_dir}"/cert
cp $JAR_PACKAGE "${package_dir}"/lib/TrustAgent.jar
#cp *.sql "${package_dir}"/database/

# copy control scripts to /usr/local/bin
chmod 700 tagent pcakey
mkdir -p /usr/local/bin
cp tagent pcakey /usr/local/bin

#rc3Begin skaja
#chmod 700 module_analysis.sh
#cp module_analysis.sh "${package_dir}"/bin
#echo "module_script=${package_dir}/bin/module_analysis.sh" >> "${intel_conf_dir}/${package_name}.properties"
#rc3End

java_install $JAVA_PACKAGE

auto_install "TrustAgent requirements" "APPLICATION"


# REDHAT ISSUE:
# After installing libcrypto via the package manager, the library cannot be
# found for linking. Solution is to create a missing symlink in /usr/lib64.
# So in general, what we want to do is:
# 1. identify the best version of libcrypto (choose 1.0.0 over 0.9.8)
# 2. identify which lib directory it's in (/usr/lib64, etc)
# 3. create a symlink from libcrypto.so to libcrypto.so.1.0.0 
# 4. run ldconfig to capture it
# 5. run ldconfig -p to ensure it is found
# XXX TODO for now we are not doing the general steps, just solving for a specific system.
fix_redhat_libcrypto() {
  local has_libcrypto=`find / -name libcrypto.so.1.0.0`
  local has_symlink=`find / -name libcrypto.so`
  if [[ -n "$has_libcrypto" && -z "$has_symlink" ]]; then
    echo "Creating missing symlink for $has_libcrypto"
    local libdir=`dirname $has_libcrypto`
    ln -s $libdir/libcrypto.so.1.0.0 $libdir/libcrypto.so
    ldconfig
  fi
}

fix_redhat_libcrypto


  is_citrix_xen=`lsb_release -a | grep "^Distributor ID" | grep XenServer`
  if [ -n "$is_citrix_xen" ]; then
    # we have precompiled binaries for citrix-xen
    echo "Installing TPM commands... "
    cd commands-citrix-xen
    chmod +x aikquote NIARL_TPM_Module openssl.sh
    cp aikquote NIARL_TPM_Module openssl.sh ${package_dir}/bin
    cd ..
  else
    # compile and install tpm commands
    echo "Compiling TPM commands... "
    cd commands
    COMPILE_OK=''
    make 2>&1 > /dev/null
    # identity and takeownership commands not needed with NIARL PRIVACY CA
    if [ -e aikquote ]; then
      cp aikquote ${package_dir}/bin
      COMPILE_OK=yes
      echo_success "OK"
    else
      echo_failure "FAILED"
    fi
    chmod +x aikquote NIARL_TPM_Module openssl.sh
    cp aikquote NIARL_TPM_Module openssl.sh ${package_dir}/bin
    cd ..
  fi
  cd ..
  # create trustagent.install file
  datestr=`date +%Y-%m-%d.%H%M`
  myinstall=${package_install_filename}
  touch ${myinstall}
  chmod 600 ${myinstall}
  echo "" > ${myinstall}
  echo "# Installed Trust Agent on ${datestr}" >> ${myinstall}
  echo "TRUST_AGENT_HOME=${package_dir}" >> ${myinstall}
  echo "TRUST_AGENT_NAME=${ARTIFACT}" >> ${myinstall}
  echo "TRUST_AGENT_VERSION=${VERSION}" >> ${myinstall}
  echo "TRUST_AGENT_RELEASE=${BUILD}" >> ${myinstall}
#  echo "TRUST_AGENT_ID=${WAR_NAME}" >> ${myinstall}


echo "Registering tagent in start up"
register_startup_script /usr/local/bin/tagent tagent


# give tagent a chance to do any other setup (such as the .env file and pcakey) and start tagent when done
/usr/local/bin/tagent setup
