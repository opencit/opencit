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
package_version_filename=/opt/trustagent/env.d/trustagent.version
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
if [ -f ~/trustagent.env ]; then 
  . ~/trustagent.env
  env_file_exports=$(cat ~/trustagent.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  eval export $env_file_exports
fi

# this is a list of all the variables we expect to find in trustagent.env
TRUSTAGENT_ENV_VARS="MTWILSON_API_URL MTWILSON_TLS_CERT_SHA1 MTWILSON_API_USERNAME MTWILSON_API_PASSWORD TPM_OWNER_SECRET TPM_SRK_SECRET AIK_SECRET AIK_INDEX TPM_QUOTE_IPV4 TRUSTAGENT_HTTP_TLS_PORT TRUSTAGENT_TLS_CERT_DN TRUSTAGENT_TLS_CERT_IP TRUSTAGENT_TLS_CERT_DNS TRUSTAGENT_KEYSTORE_PASSWORD DAA_ENABLED TRUSTAGENT_PASSWORD JAVA_REQUIRED_VERSION HARDWARE_UUID"

export_vars $TRUSTAGENT_ENV_VARS

# before we start, clear the install log
mkdir -p $(dirname $logfile)
date > $logfile

# Automatic install steps:
# 1. Install prereqs
# 2. Backup old files
# 3. Create directory structure
# 4. Install Mt Wilson Linux utilities (and use them in this script)
# 5. Install JDK
# 6. Compile TPM commands
# 7. Install Trust Agent files

##### install prereqs
auto_install "TrustAgent requirements" "APPLICATION"

##### backup old files

# backup configuration directory before unzipping our package
backupdate=`date +%Y-%m-%d.%H%M`
if [ -d /opt/trustagent/configuration ]; then  
  cp -r /opt/trustagent/configuration /opt/trustagent/configuration.$backupdate
fi


##### stop existing trust agent if running

# before we stop the trust agent, remove it from the monit config (if applicable)
# to prevent monit from trying to restart it while we are setting up.
if [ -f /etc/monit/conf.d/ta.monit ]; then
  mkdir -p /etc/monit.bak
  mv /etc/monit/conf.d/ta.monit /etc/monit.bak/ta.monit.$backupdate
  service monit restart
fi

# bug #288 we do not uninstall previous version because there are files including trustagent.jks  under the /opt tree and we need to keep them during an upgrade
# But if trust agent is already installed and running, stop it now (and start the new one later)
existing_tagent=`which tagent 2>/dev/null`
if [ -f "$existing_tagent" ]; then
  echo "Stopping trust agent..."
  $existing_tagent stop
fi



##### create directory structure


# packages to install must be in current directory
#JAR_PACKAGE=`ls -1 TrustAgent*.jar 2>/dev/null | tail -n 1`
#MTWILSON_UTIL_PACKAGE=`ls -1 mtwilson-util*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
ZIP_PACKAGE=`ls -1 trustagent*.zip 2>/dev/null | tail -n 1`

groupadd trustagent >> $logfile  2>&1
useradd -d /opt/trustagent -r -s /bin/false -g trustagent trustagent >> $logfile  2>&1

mkdir -p /opt/trustagent
unzip -DD -o $ZIP_PACKAGE -d /opt/trustagent >> $logfile  2>&1
mkdir -p /opt/trustagent/var
chown -R trustagent:trustagent /opt/trustagent
chown -R root /opt/trustagent/bin
chown -R root /opt/trustagent/java
chown -R root /opt/trustagent/configuration
mkdir -p /var/log/trustagent
chown trustagent:trustagent /var/log/trustagent
chmod -R 755 /opt/trustagent/bin
mkdir -p /opt/trustagent/env.d
chown -R root /opt/trustagent/env.d

# If VIRSH_DEFAULT_CONNECT_URI is defined in environment (likely from ~/.bashrc) 
# copy it to our new env.d folder so it will be available to tagent on startup
if [ -n "$LIBVIRT_DEFAULT_URI" ]; then
  echo "export LIBVIRT_DEFAULT_URI=$LIBVIRT_DEFAULT_URI" > /opt/trustagent/env.d/virsh
elif [ -n "$VIRSH_DEFAULT_CONNECT_URI" ]; then
  echo "export VIRSH_DEFAULT_CONNECT_URI=$VIRSH_DEFAULT_CONNECT_URI" > /opt/trustagent/env.d/virsh
fi

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

# REDHAT ISSUE:
# After installing libcrypto via the package manager, the library cannot be
# found for linking. Solution is to create a missing symlink in /usr/lib64.
# So in general, what we want to do is:
# 1. identify the best version of libcrypto (choose 1.0.0 over 0.9.8)
# 2. identify which lib directory it's in (/usr/lib64, etc)
# 3. create a symlink from libcrypto.so to libcrypto.so.1.0.0 
# 4. run ldconfig to capture it
# 5. run ldconfig -p to ensure it is found
fix_libcrypto() {
  #yum_detect; yast_detect; zypper_detect; rpm_detect; aptget_detect; dpkg_detect;
  local has_libcrypto=`find / -name libcrypto.so.1.0.0 | head -1`
  local libdir=`dirname $has_libcrypto`
  local has_libdir_symlink=`find $libdir -name libcrypto.so`
  local has_usrbin_symlink=`find /usr/bin -name libcrypto.so`
  if [ -n "$has_libcrypto" ]; then
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
    chmod 755 aikquote NIARL_TPM_Module openssl.sh
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
      chmod 755 aikquote
      cp aikquote ${package_dir}/bin
      COMPILE_OK=yes
      echo_success "OK"
    else
      echo_failure "FAILED"
    fi
    chmod 755 aikquote NIARL_TPM_Module openssl.sh
    cp aikquote NIARL_TPM_Module openssl.sh ${package_dir}/bin
    cd ..
  fi
  cd ..
  # create trustagent.version file
  datestr=`date +%Y-%m-%d.%H%M`
  touch $package_version_filename
  chmod 600 $package_version_filename
  echo "# Installed Trust Agent on ${datestr}" > $package_version_filename
  echo "TRUSTAGENT_VERSION=${VERSION}" >> $package_version_filename
  echo "TRUSTAGENT_RELEASE=\"${BUILD}\"" >> $package_version_filename

cd $return_dir

echo "Registering tagent in start up"
register_startup_script /usr/local/bin/tagent tagent 21 >>$logfile 2>&1
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
# ta.monit is already backed up at the beginning of setup.sh
# not using backup_file /etc/monit/conf.d/ta.monit because we want it in a different folder to prevent monit from reading the new ta.monit AND all the backups and complaining about duplicates
cp ta.monit /etc/monit/conf.d/ta.monit

if [ -f /etc/monit/monitrc ]; then
  cp /etc/monit/monitrc /etc/monit.bak/monitrc.$backupdate
fi
# backup_file /etc/monit/monitrc
cp monitrc /etc/monit/monitrc
chmod 700 /etc/monit/monitrc

if ! grep -q "include /etc/monit/conf.d/*" /etc/monit/monitrc; then 
 echo "include /etc/monit/conf.d/*" >> /etc/monit/monitrc
fi





# collect all the localhost ip addresses and make the list available as the
# default if the user has not already set the TRUSTAGENT_TLS_CERT_IP variable
DEFAULT_TRUSTAGENT_TLS_CERT_IP=`hostaddress_list_csv`
if [ -n "$TRUSTAGENT_TLS_CERT_IP" ]; then
  export TRUSTAGENT_TLS_CERT_IP=$DEFAULT_TRUSTAGENT_TLS_CERT_IP
fi
# corresponding hostnames to be a default for TRUSTAGENT_TLS_CERT_DNS
#DEFAULT_TRUSTAGENT_TLS_CERT_DNS=`hostaddress_list_csv`
#if [ -n "$TRUSTAGENT_TLS_CERT_DNS" ]; then
#  export TRUSTAGENT_TLS_CERT_DNS=$DEFAULT_TRUSTAGENT_TLS_CERT_DNS
#fi

# before running any tagent commands update the extensions cache file
/usr/local/bin/tagent setup update-extensions-cache-file --force 2>/dev/null

# create a trustagent username "mtwilson" with no password and all privileges
# which allows mtwilson to access it until mtwilson UI is updated to allow
# entering username and password for accessing the trust agent
/usr/local/bin/tagent password mtwilson --nopass *:*

# give tagent a chance to do any other setup (such as the .env file and pcakey)
# and make sure it's successful before trying to start the trust agent
# NOTE: only the output from start-http-server is redirected to the logfile;
#       the stdout from the setup command will be displayed
/usr/local/bin/tagent setup
/usr/local/bin/tagent start >>$logfile  2>&1

# optional: register tpm password with mtwilson so pull provisioning can
#           be accomplished with less reboots (no ownership transfer)
prompt_with_default REGISTER_TPM_PASSWORD       "Register TPM password with service to support asset tag automation? [y/n]" ${REGISTER_TPM_PASSWORD}
if [[ "$REGISTER_TPM_PASSWORD" == "y" || "$REGISTER_TPM_PASSWORD" == "Y" || "$REGISTER_TPM_PASSWORD" == "yes" ]]; then 
#	prompt_with_default ASSET_TAG_URL "Asset Tag Server URL: (https://[SERVER]:[PORT]/mtwilson/v2)" ${ASSET_TAG_URL}
	prompt_with_default MTWILSON_API_USERNAME "Username:" ${MTWILSON_API_USERNAME}
	prompt_with_default_password MTWILSON_API_PASSWORD "Password:" ${MTWILSON_API_PASSWORD}
    export MTWILSON_API_USERNAME MTWILSON_API_PASSWORD
#	# json='[{ "subject": "'$UUID'", "selection": "'$selectionUUID'"}]'
#	# wget --secure-protocol=SSLv3 --no-proxy --ca-certificate=$CERT_FILE_LOCATION --password=$password --user=$username --header="Content-Type: application/json" --post-data="$json"
#	TPM_PASSWORD=`read_property_from_file tpm.owner.secret /opt/trustagent/configuration/trustagent.properties`
	export HARDWARE_UUID=`dmidecode |grep UUID | awk '{print $2}'`
#	echo "registering $TPM_PASSWORD to $UUID"
#	wget --secure-protocol=SSLv3 --no-proxy --no-check-certificate --auth-no-challenge --password=$ASSET_TAG_PASSWORD --user=$ASSET_TAG_USERNAME --header="Content-Type: application/json" --post-data='{"id":"'$UUID'","password":"'$TPM_PASSWORD'"}' "$ASSET_TAG_URL/host-tpm-passwords"
    /usr/local/bin/tagent setup register-tpm-password
fi


# NOTE:  monit should only be restarted AFTER trustagent is up and running
#        so that it doesn't try to start it before we're done with our setup
#        tasks.
/usr/local/bin/tagent status > /dev/null
if [ $? ]; then
  service monit restart
else
  echo "Trust agent not running; skipping monit restart"
fi
