#!/bin/bash

# TRUSTAGENT install script
# Outline:
# 1. load application environment variables if already defined from env directory
# 2. load installer environment file, if present
# 3. source the utility script file "functions.sh":  mtwilson-linux-util-3.0-SNAPSHOT.sh
# 4. source the version script file "version"
# 5. determine if we are installing as root or non-root, create groups and users accordingly
# 6. remove tagent from the monit config and stop tagent
# 7. define application directory layout
# 8. backup current configuration and data, if they exist
# 9. create application directories
# 10. store directory layout in env file
# 11. store trustagent username in env file
# 12. store log level in env file, if it's set
# 13. If VIRSH_DEFAULT_CONNECT_URI is defined in environment copy it to env directory
# 14. install java
# 15. install prerequisites
# 16. setup authbind to allow non-root trustagent to listen on ports 80 and 443
# 17. delete existing dependencies from java folder, to prevent duplicate copies
# 18. extract trustagent zip
# 19. copy utilities script file to application folder
# 20. set additional permissions
# 21. symlink tagent
# 22. register tagent as a startup script
# 23. install measurement agent
# 24. migrate any old data to the new locations (v1 - v3)
# 25. create tpm-tools and additional binary symlinks
# 26. fix_libcrypto for RHEL
# 27. Install TPM commands
# 28. create trustagent.version file
# 29. fix_existing_aikcert
# 30. install monit
# 31. create TRUSTAGENT_TLS_CERT_IP list of system host addresses
# 32. update the extensions cache file
# 33. create a trustagent username "mtwilson" with no password and all privileges for mtwilson access
# 34. tagent setup
# 35. register tpm password with mtwilson
# 36. ensure the trustagent owns all the content created during setup
# 37. tagent start
# 38. restart monit

#####


# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# SCRIPT CONFIGURATION:
intel_conf_dir=/etc/intel/cloudsecurity
package_dir=/opt/intel/cloudsecurity/trustagent
package_config_filename=${intel_conf_dir}/trustagent.properties
package_version_filename=/opt/trustagent/env.d/trustagent.version

# default settings
# note the layout setting is used only by this script
# and it is not saved or used by the app script
export TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-/opt/trustagent}
TRUSTAGENT_LAYOUT=${TRUSTAGENT_LAYOUT:-home}

# the env directory is not configurable; it is defined as TRUSTAGENT_HOME/env and the
# administrator may use a symlink if necessary to place it anywhere else
export TRUSTAGENT_ENV=$TRUSTAGENT_HOME/env

# load application environment variables if already defined
if [ -d $TRUSTAGENT_ENV ]; then
  TRUSTAGENT_ENV_FILES=$(ls -1 $TRUSTAGENT_ENV/*)
  for env_file in $TRUSTAGENT_ENV_FILES; do
    . $env_file
    env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
    if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
  done
fi

# load installer environment file, if present
if [ -f ~/trustagent.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/trustagent.env"
  . ~/trustagent.env
  env_file_exports=$(cat ~/trustagent.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
fi

## functions script (mtwilson-linux-util-3.0-SNAPSHOT.sh) is required
## we use the following functions:
## java_detect java_ready_report 
## echo_failure echo_warning
## register_startup_script
#UTIL_SCRIPT_FILE=`ls -1 mtwilson-linux-util-*.sh | head -n 1`
#if [ -n "$UTIL_SCRIPT_FILE" ] && [ -f "$UTIL_SCRIPT_FILE" ]; then
#  . $UTIL_SCRIPT_FILE
#fi

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi

# determine if we are installing as root or non-root
if [ "$(whoami)" == "root" ]; then
  # create a trustagent user if there isn't already one created
  TRUSTAGENT_USERNAME=${TRUSTAGENT_USERNAME:-trustagent}
  if ! getent passwd $TRUSTAGENT_USERNAME 2>&1 >/dev/null; then
    groupadd $TRUSTAGENT_USERNAME
    useradd --comment "Mt Wilson Trust Agent" --home $TRUSTAGENT_HOME --system --shell /bin/false -g $TRUSTAGENT_USERNAME $TRUSTAGENT_USERNAME
    usermod --lock $TRUSTAGENT_USERNAME
    # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $TRUSTAGENT_USERNAME"
  fi
else
  # already running as trustagent user
  TRUSTAGENT_USERNAME=$(whoami)
  echo_warning "Running as $TRUSTAGENT_USERNAME; if installation fails try again as root"
  if [ ! -w "$TRUSTAGENT_HOME" ] && [ ! -w $(dirname $TRUSTAGENT_HOME) ]; then
    export TRUSTAGENT_HOME=$(cd ~ && pwd)
  fi
fi

# before we stop the trust agent, remove it from the monit config (if applicable)
# to prevent monit from trying to restart it while we are setting up.
if [ -f /etc/monit/conf.d/ta.monit ]; then
  mkdir -p /etc/monit.bak
  mv /etc/monit/conf.d/ta.monit /etc/monit.bak/ta.monit.$backupdate
  service monit restart
fi

# if an existing tagent is already running, stop it while we install
if which tagent; then
  tagent stop
fi

# define application directory layout
if [ "$TRUSTAGENT_LAYOUT" == "linux" ]; then
  export TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-/etc/trustagent}
  export TRUSTAGENT_REPOSITORY=${TRUSTAGENT_REPOSITORY:-/var/opt/trustagent}
  export TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-/var/log/trustagent}
elif [ "$TRUSTAGENT_LAYOUT" == "home" ]; then
  export TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-$TRUSTAGENT_HOME/configuration}
  export TRUSTAGENT_REPOSITORY=${TRUSTAGENT_REPOSITORY:-$TRUSTAGENT_HOME/repository}
  export TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-$TRUSTAGENT_HOME/logs}
fi
export TRUSTAGENT_BIN=${TRUSTAGENT_BIN:-$TRUSTAGENT_HOME/bin}
export TRUSTAGENT_JAVA=${TRUSTAGENT_JAVA:-$TRUSTAGENT_HOME/java}

# note that the env dir is not configurable; it is defined as "env" under home
export TRUSTAGENT_ENV=$TRUSTAGENT_HOME/env

trustagent_backup_configuration() {
  if [ -n "$TRUSTAGENT_CONFIGURATION" ] && [ -d "$TRUSTAGENT_CONFIGURATION" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir=/var/backup/trustagent.configuration.$datestr
    cp -r $TRUSTAGENT_CONFIGURATION $backupdir
  fi
}

trustagent_backup_repository() {
  if [ -n "$TRUSTAGENT_REPOSITORY" ] && [ -d "$TRUSTAGENT_REPOSITORY" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir=/var/backup/trustagent.repository.$datestr
    cp -r $TRUSTAGENT_REPOSITORY $backupdir
  fi
}

# backup current configuration and data, if they exist
trustagent_backup_configuration
trustagent_backup_repository

# create application directories (chown will be repeated near end of this script, after setup)
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_LOGS $TRUSTAGENT_BIN $TRUSTAGENT_JAVA; do
  mkdir -p $directory
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
  chmod 700 $directory
done

# store directory layout in env file
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_HOME=$TRUSTAGENT_HOME" >> $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_CONFIGURATION=$TRUSTAGENT_CONFIGURATION" >> $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_JAVA=$TRUSTAGENT_JAVA" >> $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_BIN=$TRUSTAGENT_BIN" >> $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_REPOSITORY=$TRUSTAGENT_REPOSITORY" >> $TRUSTAGENT_ENV/trustagent-layout
echo "export TRUSTAGENT_LOGS=$TRUSTAGENT_LOGS" >> $TRUSTAGENT_ENV/trustagent-layout

# store trustagent username in env file
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-username
echo "export TRUSTAGENT_USERNAME=$TRUSTAGENT_USERNAME" >> $TRUSTAGENT_ENV/trustagent-username

# store log level in env file, if it's set
if [ -n "$TRUSTAGENT_LOG_LEVEL" ]; then
  echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-logging
  echo "export TRUSTAGENT_LOG_LEVEL=$TRUSTAGENT_LOG_LEVEL" >> $TRUSTAGENT_ENV/trustagent-logging
fi

# If VIRSH_DEFAULT_CONNECT_URI is defined in environment (likely from ~/.bashrc) 
# copy it to our new env folder so it will be available to tagent on startup
if [ -n "$LIBVIRT_DEFAULT_URI" ]; then
  echo "export LIBVIRT_DEFAULT_URI=$LIBVIRT_DEFAULT_URI" > $TRUSTAGENT_ENV/trustagent-virsh
elif [ -n "$VIRSH_DEFAULT_CONNECT_URI" ]; then
  echo "export VIRSH_DEFAULT_CONNECT_URI=$VIRSH_DEFAULT_CONNECT_URI" > $TRUSTAGENT_ENV/trustagent-virsh
fi

cp version $TRUSTAGENT_CONFIGURATION/trustagent.version

# store the auto-exported environment variables in env file
# to make them available after the script uses sudo to switch users;
# we delete that file later
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "export $env_file_var_name=$env_file_var_value" >> $TRUSTAGENT_ENV/trustagent-setup
done

# install java
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
java_detect 2>&1 >/dev/null
if ! java_ready; then
  # java not installed, check if we have the bundle
  JAVA_INSTALL_REQ_BUNDLE=`ls -1 java-*.bin 2>/dev/null | head -n 1`
  JAVA_INSTALL_REQ_TGZ=`ls -1 jdk*.tar.gz 2>/dev/null | head -n 1`
  if [ -n "$JAVA_INSTALL_REQ_BUNDLE" ]; then
    chmod +x $JAVA_INSTALL_REQ_BUNDLE
    ./$JAVA_INSTALL_REQ_BUNDLE
    java_detect
  elif [ -n "$JAVA_INSTALL_REQ_TGZ" ]; then
    tar xzf $JAVA_INSTALL_REQ_TGZ
    JAVA_INSTALL_REQ_TGZ_UNPACKED=`ls -1d jdk* jre* 2>/dev/null`
    for f in $JAVA_INSTALL_REQ_TGZ_UNPACKED
    do
      #echo "$f"
      if [ -d "$f" ]; then
        if [ -d "/usr/share/$f" ]; then
          echo "Java already installed at /usr/share/$f"
          export JAVA_HOME="/usr/share/$f"
        else
          mv "$f" /usr/share && export JAVA_HOME="/usr/share/$f"
        fi
      fi
    done
    java_detect
  fi
fi
if java_ready_report; then
  # store java location in env file
  echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-java
  echo "export JAVA_HOME=$JAVA_HOME" >> $TRUSTAGENT_ENV/trustagent-java
  echo "export JAVA_CMD=$java" >> $TRUSTAGENT_ENV/trustagent-java
else
  echo_failure "Java $JAVA_REQUIRED_VERSION not found"
  exit 1
fi

if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

TRUSTAGENT_REMOVE_YUM_PACKAGES="trousers trousers-devel"
TRUSTAGENT_REMOVE_APT_PACKAGES="trousers trousers-dbg"
TRUSTAGENT_REMOVE_YAST_PACKAGES="trousers trousers-devel"
TRUSTAGENT_REMOVE_ZYPPER_PACKAGES="trousers trousers-devel"
auto_uninstall "Removing Incompatible Packages" "TRUSTAGENT_REMOVE"
if [ $? -ne 0 ]; then echo_failure "Failed to remove incompatible packages through package manager"; exit -1; fi

# make sure unzip and authbind are installed
#java_required_version=1.7.0_51
# commented out from yum packages: tpm-tools-devel curl-devel (not required because we're using NIARL Privacy CA and we don't need the identity command which used libcurl
TRUSTAGENT_YUM_PACKAGES="zip unzip authbind openssl tpm-tools make gcc"
# commented out from apt packages: libcurl4-openssl-dev 
TRUSTAGENT_APT_PACKAGES="zip unzip authbind openssl libssl-dev libtspi-dev libtspi1 make gcc"
# commented out from YAST packages: libcurl-devel tpm-tools-devel.  also zlib and zlib-devel are dependencies of either openssl
TRUSTAGENT_YAST_PACKAGES="zip unzip authbind openssl libopenssl-devel tpm-tools make gcc"
# SUSE uses zypper:.  omitting libtspi1 because already depends on a specific version of it which will be isntalled automatically
TRUSTAGENT_ZYPPER_PACKAGES="zip unzip authbind openssl libopenssl-devel libopenssl1_0_0 openssl-certs"
# other packages in suse:  libopenssl0_9_8
auto_install "Installer requirements" "TRUSTAGENT"
if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi

# setup authbind to allow non-root trustagent to listen on ports 80 and 443
if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ -d /etc/authbind/byport ]; then
  touch /etc/authbind/byport/80 /etc/authbind/byport/443
  chmod 500 /etc/authbind/byport/80 /etc/authbind/byport/443
  chown $TRUSTAGENT_USERNAME /etc/authbind/byport/80 /etc/authbind/byport/443
fi

# delete existing java files, to prevent a situation where the installer copies
# a newer file but the older file is also there
if [ -d $TRUSTAGENT_HOME/java ]; then
  rm $TRUSTAGENT_HOME/java/*.jar 2>/dev/null
fi

# extract trustagent  (trustagent-zip-0.1-SNAPSHOT.zip)
echo "Extracting application..."
TRUSTAGENT_ZIPFILE=`ls -1 trustagent-*.zip 2>/dev/null | head -n 1`
unzip -oq $TRUSTAGENT_ZIPFILE -d $TRUSTAGENT_HOME

## copy utilities script file to application folder
#cp $UTIL_SCRIPT_FILE $TRUSTAGENT_HOME/bin/functions.sh
cp functions $TRUSTAGENT_BIN

# set permissions
chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $TRUSTAGENT_HOME
chmod 755 $TRUSTAGENT_BIN/*

# link /usr/local/bin/tagent -> /opt/trustagent/bin/tagent
EXISTING_TAGENT_COMMAND=`which tagent 2>/dev/null`
if [ -n "$EXISTING_TAGENT_COMMAND" ]; then
  rm -f "$EXISTING_TAGENT_COMMAND"
fi
ln -s $TRUSTAGENT_BIN/tagent.sh /usr/local/bin/tagent
if [[ ! -h $TRUSTAGENT_BIN/tagent ]]; then
  ln -s $TRUSTAGENT_BIN/tagent.sh $TRUSTAGENT_BIN/tagent
fi

# register linux startup script
register_startup_script $TRUSTAGENT_BIN/tagent.sh tagent 21
# trousers has N=20 startup number, need to lookup and do a N+1

### INSTALL MEASUREMENT AGENT
echo "Installing measurement agent..."
TBOOTXM_PACKAGE=`ls -1 tbootxm-linux-makeself-*.bin 2>/dev/null | tail -n 1`
if [ -z "$TBOOTXM_PACKAGE" ]; then
  echo_failure "Failed to find measurement agent installer package"
  exit -1
fi
./$TBOOTXM_PACKAGE
if [ $? -ne 0 ]; then echo_failure "Failed to install measurement agent"; exit -1; fi

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
package_config_filename=${intel_conf_dir}/trustagent.properties

### symlinks
#tpm_nvinfo
tpmnvinfo=`which tpm_nvinfo 2>/dev/null`
if [ -z "$tpmnvinfo" ]; then
  echo_failure "Cannot find tpm_nvinfo"
  echo_failure "tpm-tools must be installed"
  exit -1
fi
if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvinfo" ]]; then
  ln -s "$tpmnvinfo" "$TRUSTAGENT_BIN"
fi

#tpm_nvrelease
tpmnvrelease=`which tpm_nvrelease 2>/dev/null`
if [ -z "$tpmnvrelease" ]; then
  echo_failure "Cannot find tpm_nvrelease"
  echo_failure "tpm-tools must be installed"
  exit -1
fi
if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvrelease" ]]; then
  ln -s "$tpmnvrelease" "$TRUSTAGENT_BIN"
fi

#tpm_nvwrite
tpmnvwrite=`which tpm_nvwrite 2>/dev/null`
if [ -z "$tpmnvwrite" ]; then
  echo_failure "Cannot find tpm_nvwrite"
  echo_failure "tpm-tools must be installed"
  exit -1
fi
if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvwrite" ]]; then
  ln -s "$tpmnvwrite" "$TRUSTAGENT_BIN"
fi

#tpm_nvread
tpmnvread=`which tpm_nvread 2>/dev/null`
if [ -z "$tpmnvread" ]; then
  echo_failure "Cannot find tpm_nvread"
  echo_failure "tpm-tools must be installed"
  exit -1
fi
if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvread" ]]; then
  ln -s "$tpmnvread" "$TRUSTAGENT_BIN"
fi

#tpm_nvdefine
tpmnvdefine=`which tpm_nvdefine 2>/dev/null`
if [ -z "$tpmnvdefine" ]; then
  echo_failure "Cannot find tpm_nvdefine"
  echo_failure "tpm-tools must be installed"
  exit -1
fi
if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvdefine" ]]; then
  ln -s "$tpmnvdefine" "$TRUSTAGENT_BIN"
fi

#tpm_bindaeskey
tpmbindaeskey=`which tpm_bindaeskey 2>/dev/null`
if [ -n "$tpmbindaeskey" ]; then
  rm -f "$tpmbindaeskey"
fi
ln -s "$TRUSTAGENT_BIN/tpm_bindaeskey" /usr/local/bin/tpm_bindaeskey

#tpm_unbindaeskey
tpmunbindaeskey=`which tpm_unbindaeskey 2>/dev/null`
if [ -n "$tpmunbindaeskey" ]; then
  rm -f "$tpmunbindaeskey"
fi
ln -s "$TRUSTAGENT_BIN/tpm_unbindaeskey" /usr/local/bin/tpm_unbindaeskey

#tpm_createkey
tpmcreatekey=`which tpm_createkey 2>/dev/null`
if [ -n "$tpmcreatekey" ]; then
  rm -f "$tpmcreatekey"
fi
ln -s "$TRUSTAGENT_BIN/tpm_createkey" /usr/local/bin/tpm_createkey

#tpm_signdata
tpmsigndata=`which tpm_signdata 2>/dev/null`
if [ -n "$tpmsigndata" ]; then
  rm -f "$tpmsigndata"
fi
ln -s "$TRUSTAGENT_BIN/tpm_signdata" /usr/local/bin/tpm_signdata

hex2bin_install() {
  return_dir=`pwd`
  cd hex2bin
  make && cp hex2bin /usr/local/bin
  cd $return_dir
}

hex2bin_install

hex2bin=`which hex2bin 2>/dev/null`
if [[ ! -h "$TRUSTAGENT_BIN/hex2bin" ]]; then
  ln -s "$hex2bin" "$TRUSTAGENT_BIN"
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
  local has_usrlib_symlink=`find /usr/lib -name libcrypto.so`
  if [ -n "$has_libcrypto" ]; then
    if [ -z "$has_libdir_symlink" ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 $libdir/libcrypto.so
    fi
    if [ -z "$has_usrbin_symlink" ] && [ -z "$has_usrlib_symlink" ]; then
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
  mkdir -p /etc/monit.bak
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

# setup the trustagent, unless the NOSETUP variable is defined
if [ -z "$TRUSTAGENT_NOSETUP" ]; then

#  # the master password is required
#  if [ -z "$TRUSTAGENT_PASSWORD" ]; then
#    echo_failure "Master password required in environment variable TRUSTAGENT_PASSWORD"
#    echo 'To generate a new master password, run the following command:
#
#  TRUSTAGENT_PASSWORD=$(tagent generate-password) && echo TRUSTAGENT_PASSWORD=$TRUSTAGENT_PASSWORD
#
#The master password must be stored in a safe place, and it must
#be exported in the environment for all other tagent commands to work.
#
#LOSS OF MASTER PASSWORD WILL RESULT IN LOSS OF PROTECTED KEYS AND RELATED DATA
#
#After you set TRUSTAGENT_PASSWORD, run the following command to complete installation:
#
#  tagent setup
#
#'
#    exit 1
#  fi
#
#  tagent config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,trustagent}" >/dev/null
#  tagent setup

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

  # optional: register tpm password with mtwilson so pull provisioning can
  #           be accomplished with less reboots (no ownership transfer)
  prompt_with_default REGISTER_TPM_PASSWORD "Register TPM password with service to support asset tag automation? [y/n]" ${REGISTER_TPM_PASSWORD}
  if [[ "$REGISTER_TPM_PASSWORD" == "y" || "$REGISTER_TPM_PASSWORD" == "Y" || "$REGISTER_TPM_PASSWORD" == "yes" ]]; then
    prompt_with_default MTWILSON_API_USERNAME "Username:" ${MTWILSON_API_USERNAME}
    prompt_with_default_password MTWILSON_API_PASSWORD "Password:" ${MTWILSON_API_PASSWORD}
    export MTWILSON_API_USERNAME MTWILSON_API_PASSWORD
    export HARDWARE_UUID=`dmidecode |grep UUID | awk '{print $2}'`
    /usr/local/bin/tagent setup register-tpm-password
  fi
fi

# delete the temporary setup environment variables file
rm -f $TRUSTAGENT_ENV/trustagent-setup

# ensure the trustagent owns all the content created during setup
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_JAVA $TRUSTAGENT_BIN $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_LOGS; do
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
done

# start the server, unless the NOSETUP variable is defined
if [ -z "$TRUSTAGENT_NOSETUP" ]; then tagent start; fi

# NOTE:  monit should only be restarted AFTER trustagent is up and running
#        so that it doesn't try to start it before we're done with our setup
#        tasks.
/usr/local/bin/tagent status > /dev/null
if [ $? ]; then
  service monit restart
else
  echo "Trust agent not running; skipping monit restart"
fi
