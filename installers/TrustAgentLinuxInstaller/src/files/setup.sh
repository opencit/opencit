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
if [ -f /root/mtwilson.env ]; then  . /root/mtwilson.env; fi
#load_conf 2>&1 >/dev/null
#load_defaults 2>&1 >/dev/null


# Automatic install in 4 steps:
# 1. Install Mt Wilson Linux utilities (and use them in this script)
# 2. Install JDK
# 3. Compile TPM commands
# 4. Install Trust Agent files

# bug #288 we do not uninstall previous version because there are files including trustagent.jks  under the /opt tree and we need to keep them during an upgrade
# if there's already a previous version installed, uninstall it
# But if trust agent is already installed and running, stop it now (and start the new one later)
tagent=`which tagent 2>/dev/null`
if [ -f "$tagent" ]; then
  echo "Stopping trust agent..."
  $tagent stop
fi

# packages to install must be in current directory
JAR_PACKAGE=`ls -1 TrustAgent*.jar 2>/dev/null | tail -n 1`
#MTWILSON_UTIL_PACKAGE=`ls -1 mtwilson-util*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`

saveD=`pwd`
# copy application files to /opt
mkdir -p "${intel_conf_dir}"
chmod 700 "${intel_conf_dir}"
# bug #947 we do not replace trustagent.properties automatically because it contains important passwords that must not be clobbered.
# if any release adds new properties to that file, use update_property_in_file to add them safely.
if [ ! -f "${intel_conf_dir}/${package_name}.properties" ]; then
  cp ${package_name}.properties "${intel_conf_dir}"
  chmod 600 ${package_name}.properties
fi

# bug #947 if we are upgrading a previous install, move the trustagent.jks file from /opt to /etc
if [ ! -f "${intel_conf_dir}/trustagent.jks" ]; then
  if [ -f "${package_dir}/cert/trustagent.jks" ]; then
    mv "${package_dir}/cert/trustagent.jks" "${intel_conf_dir}/trustagent.jks"
  fi
fi

chmod 600 TPMModule.properties
cp TPMModule.properties "${intel_conf_dir}"/TPMModule.properties

mkdir -p "${package_dir}"
mkdir -p "${package_dir}"/bin
mkdir -p "${package_dir}"/cert
mkdir -p "${package_dir}"/data
mkdir -p "${package_dir}"/lib
chmod -R 700 "${package_dir}"
cp version "${package_dir}"
cp functions "${package_dir}"
cp $JAR_PACKAGE "${package_dir}"/lib/TrustAgent.jar

# copy default logging settings to /etc, but do not change it if it's already there (someone may have modified it)
if [ ! -f "${intel_conf_dir}/logback.xml" ]; then
  chmod 600 logback.xml
  cp logback.xml "${intel_conf_dir}"
fi

# copy control scripts to /usr/local/bin
chmod 700 tagent pcakey
mkdir -p /usr/local/bin
cp tagent pcakey /usr/local/bin

#module attestation script
chmod 755 module_analysis.sh
cp module_analysis.sh "${package_dir}"/bin
update_property_in_file module_script "${intel_conf_dir}/${package_name}.properties" "${package_dir}/bin/module_analysis.sh"

java_install $JAVA_PACKAGE

if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

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
  echo "TRUST_AGENT_RELEASE=\"${BUILD}\"" >> ${myinstall}
#  echo "TRUST_AGENT_ID=${WAR_NAME}" >> ${myinstall}


echo "Registering tagent in start up"
register_startup_script /usr/local/bin/tagent tagent

fix_existing_aikcert() {
  local aikdir=${intel_conf_dir}/cert
  if [ ! -f $aikdir/aikcert.pem ] && [ -f $aikdir/aikcert.cer ]; then
    # trust agent aikcert.cer is in broken PEM format... it needs newlines every 76 characters to be correct
    cat $aikdir/aikcert.cer | sed 's/.\{76\}/&\n/g' > $aikdir/aikcert.pem
    rm $aikdir/aikcert.cer
    if [ -f ${package_config_filename} ]; then 
       # update aikcert.filename=aikcert.cer to aikcert.filename=aikcert.pem
       update_property_in_file aikcert.filename ${package_config_filename} aikcert.pem
    fi
  fi
}

fix_existing_aikcert

# give tagent a chance to do any other setup (such as the .env file and pcakey) and start tagent when done
/usr/local/bin/tagent setup
/usr/local/bin/tagent start

# now install monit
monit_required_version=5.5

# detect the packages we have to install
MONIT_PACKAGE=`ls -1 monit-*.tar.gz 2>/dev/null | tail -n 1`

# SCRIPT EXECUTION
monit_clear() {
  #MONIT_HOME=""
  monit=""
}

monit_detect() {
  local monitrc=`ls -1 /etc/monitrc 2>/dev/null | tail -n 1`
  monit=`which monit 2>/dev/null`
}

monit_install() {
  MONIT_YUM_PACKAGES="monit"
  MONIT_APT_PACKAGES="monit"
  MONIT_YAST_PACKAGES=""
  MONIT_ZYPPER_PACKAGES="monit"
  auto_install "Monit" "MONIT"
  monit_clear; monit_detect;
    if [[ -z "$monit" ]]; then
      echo_failure "Unable to auto-install Monit"
      echo "  Monit download URL:"
      echo "  http://www.mmonit.com"
    else
      echo_success "Monit installed in $monit"
    fi
}

monit_src_install() {
  local MONIT_PACKAGE="${1:-monit-5.5-linux-src.tar.gz}"
#  DEVELOPER_YUM_PACKAGES="make gcc openssl libssl-dev"
#  DEVELOPER_APT_PACKAGES="dpkg-dev make gcc openssl libssl-dev"
  DEVELOPER_YUM_PACKAGES="make gcc"
  DEVELOPER_APT_PACKAGES="dpkg-dev make gcc"
  auto_install "Developer tools" "DEVELOPER"
  monit_clear; monit_detect;
  if [[ -z "$monit" ]]; then
    if [[ -z "$MONIT_PACKAGE" || ! -f "$MONIT_PACKAGE" ]]; then
      echo_failure "Missing Monit installer: $MONIT_PACKAGE"
      return 1
    fi
    local monitfile=$MONIT_PACKAGE
    echo "Installing $monitfile"
    is_targz=`echo $monitfile | grep ".tar.gz$"`
    is_tgz=`echo $monitfile | grep ".tgz$"`
    if [[ -n "$is_targz" || -n "$is_tgz" ]]; then
      gunzip -c $monitfile | tar xf -
    fi
    local monit_unpacked=`ls -1d monit-* 2>/dev/null`
    local monit_srcdir
    for f in $monit_unpacked
    do
      if [ -d "$f" ]; then
        monit_srcdir="$f"
      fi
    done
    if [[ -n "$monit_srcdir" && -d "$monit_srcdir" ]]; then
      echo "Compiling monit..."
      cd $monit_srcdir
      ./configure --without-pam --without-ssl 2>&1 >/dev/null
      make 2>&1 >/dev/null
      make install  2>&1 >/dev/null
    fi
    monit_clear; monit_detect
    if [[ -z "$monit" ]]; then
      echo_failure "Unable to auto-install Monit"
      echo "  Monit download URL:"
      echo "  http://www.mmonit.com"
    else
      echo_success "Monit installed in $monit"
    fi
  else
    echo "Monit is already installed"
  fi
}

monit_install $MONIT_PACKAGE

cd $saveD
if [ ! -d /etc/monit ]; then
 mkdir /etc/monit
fi

if [ -f /etc/monit/monitrc ]; then
    echo_warning "Monit configuration already exists in /etc/monit/monitrc; backing up"
    backup_file /etc/monit/monitrc
else
    cp monitrc /etc/monit/monitrc
fi

if ! grep -q "include /etc/monit/conf.d/*" /etc/monit/monitrc; then 
 echo "include /etc/monit/conf.d/*" >> /etc/monit/monitrc
fi

if [ ! -d /etc/monit/conf.d ]; then
 mkdir -p /etc/monit/conf.d
fi

if [ ! -f /etc/monit/conf.d/ta.monit ]; then
 cp ta.monit /etc/monit/conf.d/ta.monit
fi

chmod 700 /etc/monit/monitrc
service monit restart

echo "monit installed and monitoring tagent"

sleep 2
/usr/local/bin/tagent start