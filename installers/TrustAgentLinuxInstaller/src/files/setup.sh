#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_name=trustagent
package_dir=/opt/intel/cloudsecurity/${package_name}
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_env_filename=/root/${package_name}.env
package_install_filename=${package_dir}/${package_name}.install
ASSET_TAG_SETUP="y"

logfile=/var/log/trustagent/install.log

#java_required_version=1.7.0_51
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
if [ -f ~/trustagent.env ]; then  . ~/trustagent.env; fi

# this is a list of all the variables we expect to find in trustagent.env
TRUSTAGENT_ENV_VARS="MTWILSON_API_URL MTWILSON_TLS_CERT_SHA1 MTWILSON_API_USERNAME MTWILSON_API_PASSWORD TPM_OWNER_SECRET TPM_SRK_SECRET AIK_SECRET AIK_INDEX TPM_QUOTE_IPV4 TRUSTAGENT_HTTP_TLS_PORT TRUSTAGENT_TLS_CERT_DN TRUSTAGENT_TLS_CERT_IP TRUSTAGENT_TLS_CERT_DNS TRUSTAGENT_KEYSTORE_PASSWORD DAA_ENABLED TRUSTAGENT_PASSWORD JAVA_REQUIRED_VERSION"

export_vars $TRUSTAGENT_ENV_VARS

# before we start, clear the install log
mkdir -p $(dirname $logfile)
date > $logfile

# Automatic install steps:
# 1. Backup old files
# 2. Create directory structure
# 1. Install Mt Wilson Linux utilities (and use them in this script)
# 2. Install JDK
# 3. Compile TPM commands
# 4. Install Trust Agent files


##### stop existing trust agent if running


# bug #288 we do not uninstall previous version because there are files including trustagent.jks  under the /opt tree and we need to keep them during an upgrade
# if there's already a previous version installed, uninstall it
# But if trust agent is already installed and running, stop it now (and start the new one later)
existing_tagent=`which tagent 2>/dev/null`
if [ -f "$existing_tagent" ]; then
  echo "Stopping trust agent..."
  $existing_tagent stop
fi


##### backup old files

# backup configuration directory before unzipping our package
backupdate=`date +%Y-%m-%d.%H%M`
if [ -d /opt/trustagent/configuration ]; then  
  cp -r /opt/trustagent/configuration /opt/trustagent/configuration.$backupdate
fi

##### create directory structure


# packages to install must be in current directory
#JAR_PACKAGE=`ls -1 TrustAgent*.jar 2>/dev/null | tail -n 1`
#MTWILSON_UTIL_PACKAGE=`ls -1 mtwilson-util*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
ZIP_PACKAGE=`ls -1 trustagent*.zip 2>/dev/null | tail -n 1`

# TODO: check if trustagent exists before trying to add it; allow user name to be
#       specified by environment variable
groupadd trustagent >> $logfile  2>&1
useradd -d /opt/trustagent -r -s /bin/false -G trustagent trustagent >> $logfile  2>&1

mkdir -p /opt/trustagent
unzip -o $ZIP_PACKAGE -d /opt/trustagent >> $logfile  2>&1
mkdir -p /opt/trustagent/var
chown -R trustagent:trustagent /opt/trustagent
chown -R root /opt/trustagent/bin
chown -R root /opt/trustagent/java
chown -R root /opt/trustagent/configuration
mkdir -p /var/log/trustagent
chown trustagent:trustagent /var/log/trustagent
chmod -R 770 /opt/trustagent/bin


# Migrate any old data to the new locations  (should be rewritten in java)
v1_aik=/etc/intel/cloudsecurity/cert
v2_aik=/opt/trustagent/configuration
v1_conf=/etc/intel/cloudsecurity
v2_conf=/opt/trustagent/configuration
if [ -d "$v1_aik" ]; then
  cp $v1_aik/aikblob.dat $v2_aik/aik.blob
  cp $v1_aik/aikcert.pem $v2_aik/aik.pem
fi
if [ -d "$v1_conf" ]; then
  # find the existing tpm owner and aik secrets
  TpmOwnerAuth_121=`read_property_from_file TpmOwnerAuth ${v1_conf}/hisprovisioner.properties`
  HisIdentityAuth_121=`read_property_from_file HisIdentityAuth ${v1_conf}/hisprovisioner.properties`
  TpmOwnerAuth_122=`read_property_from_file TpmOwnerAuth ${v1_conf}/trustagent.properties`
  HisIdentityAuth_122=`read_property_from_file HisIdentityAuth ${v1_conf}/trustagent.properties`
  if [ -z "$TpmOwnerAuth_122" ] && [ -n "$TpmOwnerAuth_121" ]; then
    export TPM_OWNER_SECRET=$TpmOwnerAuth_121
  elif [ -n "$TpmOwnerAuth_122" ]; then
    export TPM_OWNER_SECRET=$TpmOwnerAuth_122
  fi
  if [ -z "$HisIdentityAuth_122" ] && [ -n "$HisIdentityAuth_121" ]; then
    export AIK_SECRET=$HisIdentityAuth_121
  elif [ -n "$HisIdentityAuth_122" ]; then
    export AIK_SECRET=$HisIdentityAuth_122
  fi

  # now copy the keystore and the keystore password
  KeystorePassword_122=`read_property_from_file trustagent.keystore.password ${v1_conf}/trustagent.properties`
  if [ -n "$KeystorePassword_122" ]; then
    export TRUSTAGENT_KEYSTORE_PASSWORD=$KeystorePassword_122
    if [ -f "$v1_conf/trustagent.jks" ]; then
      cp $v1_conf/trustagent.jks $v2_conf
    fi
  fi
fi

# Redefine the variables to the new locations

intel_conf_dir=/opt/trustagent/configuration
package_dir=/opt/trustagent
package_config_filename=${intel_conf_dir}/${package_name}.properties
package_install_filename=${package_dir}/${package_name}.install



#tpm_nvinfo
tpmnvinfo=`which tpm_nvinfo 2>/dev/null`
if [[ ! -h "${package_dir}/bin/tpm_nvinfo" ]]; then
  ln -s "$tpmnvinfo" "${package_dir}/bin"
fi

#tpm_nvrelease
tpmnvrelease=`which tpm_nvrelease 2>/dev/null`
if [[ ! -h "${package_dir}/bin/tpm_nvrelease" ]]; then
  ln -s "$tpmnvrelease" "${package_dir}/bin"
fi

#tpm_nvwrite
tpmnvwrite=`which tpm_nvwrite 2>/dev/null`
if [[ ! -h "${package_dir}/bin/tpm_nvwrite" ]]; then
  ln -s "$tpmnvwrite" "${package_dir}/bin"
fi

#tpm_nvread
tpmnvread=`which tpm_nvread 2>/dev/null`
if [[ ! -h "${package_dir}/bin/tpm_nvread" ]]; then
  ln -s "$tpmnvread" "${package_dir}/bin"
fi

#tpm_nvdefine
tpmnvdefine=`which tpm_nvdefine 2>/dev/null`
if [[ ! -h "${package_dir}/bin/tpm_nvdefine" ]]; then
  ln -s "$tpmnvdefine" "${package_dir}/bin"
fi

hex2bin_install() {
  return_dir=`pwd`
  cd hex2bin
  make && cp hex2bin /usr/local/bin
  cd $return_dir
}

hex2bin_install

hex2bin=`which hex2bin 2>/dev/null`
if [[ ! -h "${package_dir}/bin/hex2bin" ]]; then
  ln -s "$hex2bin" "${package_dir}/bin"
fi

mkdir -p "${package_dir}"/linux-util
chmod -R 700 "${package_dir}"/linux-util
cp version "${package_dir}"/linux-util
cp functions "${package_dir}"/linux-util


# copy control scripts to /usr/local/bin
mkdir -p /usr/local/bin
if [ -f /usr/local/bin/tagent ]; then rm /usr/local/bin/tagent; fi
ln -s /opt/trustagent/bin/tagent.sh /usr/local/bin/tagent
if [[ ! -h /opt/trustagent/bin/tagent ]]; then
  ln -s /opt/trustagent/bin/tagent.sh /opt/trustagent/bin/tagent
fi


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
fix_libcrypto() {
  #yum_detect; yast_detect; zypper_detect; rpm_detect; aptget_detect; dpkg_detect;
  local has_libcrypto=`find / -name libcrypto.so.1.0.0 -1`
  local libdir=`dirname $has_libcrypto`
  local has_libdir_symlink=`find $libdir -name libcrypto.so`
  local has_usrbin_symlink=`find /usr/bin -name libcrypto.so`
  if [[ -n "$has_libcrypto" ]]; then
    if [ -z "$has_libdir_symlink" ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 $libdir/libcrypto.so
    fi
    if [ -z "$has_usrbin_symlink" ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 /usr/lib/libcrypto.so
    fi
    
    #if [ -n "$yum" ]; then #RHEL
    #elif [[ -n "$zypper" || -n "$yast" ]]; then #SUSE
    #fi

    ldconfig
  fi
}

fix_libcrypto

return_dir=`pwd`

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

cd $return_dir

echo "Registering tagent in start up"
register_startup_script /usr/local/bin/tagent tagent 21
# trousers has N=20 startup number, need to lookup and do a N+1

fix_existing_aikcert() {
  local aikdir=${intel_conf_dir}/cert
  if [ -f $aikdir/aikcert.cer ]; then
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

mkdir -p /etc/monit/conf.d
backup_file /etc/monit/conf.d/ta.monit
cp ta.monit /etc/monit/conf.d/ta.monit

backup_file /etc/monit/monitrc
cp monitrc /etc/monit/monitrc

if ! grep -q "include /etc/monit/conf.d/*" /etc/monit/monitrc; then 
 echo "include /etc/monit/conf.d/*" >> /etc/monit/monitrc
fi



# TODO INSECURE need to rewrite this as a java setup task and leverage the
#      existing tls policy for known mtwilson ssl cert 
prompt_with_default REGISTER_TPM_PASSWORD       "Register TPM password with service to support asset tag automation? [y/n]" ${ASSET_TAG_SETUP}
if [[ "$REGISTER_TPM_PASSWORD" == "y" || "$REGISTER_TPM_PASSWORD" == "Y" ]]; then 
	prompt_with_default ASSET_TAG_URL "Asset Tag Server URL: (https://[SERVER]:[PORT]/mtwilson/v2)" ${ASSET_TAG_URL}
	prompt_with_default ASSET_TAG_USERNAME "Username:" ${ASSET_TAG_USERNAME}
	prompt_with_default_password ASSET_TAG_PASSWORD "Password:" ${ASSET_TAG_PASSWORD}
	# json='[{ "subject": "'$UUID'", "selection": "'$selectionUUID'"}]'
	# wget --secure-protocol=SSLv3 --no-proxy --ca-certificate=$CERT_FILE_LOCATION --password=$password --user=$username --header="Content-Type: application/json" --post-data="$json"
	TPM_PASSWORD=`read_property_from_file TpmOwnerAuth ${intel_conf_dir}/${package_name}.properties`
	UUID=`dmidecode |grep UUID | awk '{print $2}'`
	echo "registering $TPM_PASSWORD to $UUID"
	wget --secure-protocol=SSLv3 --no-proxy --no-check-certificate --auth-no-challenge --password=$ASSET_TAG_PASSWORD --user=$ASSET_TAG_USERNAME --header="Content-Type: application/json" --post-data='{"id":"'$UUID'","password":"'$TPM_PASSWORD'"}' "$ASSET_TAG_URL/host-tpm-passwords"
fi


chmod 700 /etc/monit/monitrc
service monit restart

echo "monit installed and monitoring tagent"

sleep 2

# collect all the localhost ip addresses and make the list available as the
# default if the user has not already set the TRUSTAGENT_TLS_CERT_IP variable
DEFAULT_TRUSTAGENT_TLS_CERT_IP=`hostaddress_list_csv`
if [ -n "$TRUSTAGENT_TLS_CERT_IP" ]; then
  export TRUSTAGENT_TLS_CERT_IP=$DEFAULT_TRUSTAGENT_TLS_CERT_IP
fi
# TODO: look up each ip address in /etc/hosts and generate the list of 
# corresponding hostnames to be a default for TRUSTAGENT_TLS_CERT_DNS
#DEFAULT_TRUSTAGENT_TLS_CERT_DNS=`hostaddress_list_csv`
#if [ -n "$TRUSTAGENT_TLS_CERT_DNS" ]; then
#  export TRUSTAGENT_TLS_CERT_DNS=$DEFAULT_TRUSTAGENT_TLS_CERT_DNS
#fi

# give tagent a chance to do any other setup (such as the .env file and pcakey)
# and make sure it's successful before trying to start the trust agent
# NOTE: only the output from start-http-server is redirected to the logfile;
#       the stdout from the setup command will be displayed
/usr/local/bin/tagent setup && (/usr/local/bin/tagent start-http-server &) >> $logfile  2>&1

# create a trustagent username "mtwilson" with no password and all privileges
# which allows mtwilson to access it until mtwilson UI is updated to allow
# entering username and password for accessing the trust agent
/usr/local/bin/tagent password mtwilson --nopass *:*
