#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# application defaults (these are not configurable and used only in this script so no need to export)
DEFAULT_TRUSTAGENT_HOME=/opt/trustagent
DEFAULT_TRUSTAGENT_USERNAME=tagent
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}

# default settings
export TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-$DEFAULT_TRUSTAGENT_HOME}

# the env directory is not configurable; it is defined as TRUSTAGENT_HOME/env and the
# administrator may use a symlink if necessary to place it anywhere else
export TRUSTAGENT_ENV=$TRUSTAGENT_HOME/env.d

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

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f version ]; then . version; else echo_warning "Missing file: version"; fi


# The version script is automatically generated at build time and looks like this:
#ARTIFACT=mtwilson-trustagent-installer
#VERSION=2.0.6
#BUILD="Fri, 5 Jun 2015 15:55:20 PDT (release-2.0.6)"



# determine if we are installing as root or non-root
if [ "$(whoami)" == "root" ]; then
  # create a trustagent user if there isn't already one created
  TRUSTAGENT_USERNAME=${TRUSTAGENT_USERNAME:-$DEFAULT_TRUSTAGENT_USERNAME}
  if ! getent passwd $TRUSTAGENT_USERNAME 2>&1 >/dev/null; then
    useradd --comment "Mt Wilson Trust Agent" --home $TRUSTAGENT_HOME --system --shell /bin/false $TRUSTAGENT_USERNAME
    usermod --lock $TRUSTAGENT_USERNAME
    # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $TRUSTAGENT_USERNAME"
  fi
else
  # already running as trustagent user
  TRUSTAGENT_USERNAME=$(whoami)
  if [ ! -w "$TRUSTAGENT_HOME" ] && [ ! -w $(dirname $TRUSTAGENT_HOME) ]; then
    TRUSTAGENT_HOME=$(cd ~ && pwd)
  fi
  echo_warning "Installing as $TRUSTAGENT_USERNAME into $TRUSTAGENT_HOME"  
fi

# ensure the home directory exists or can be created
mkdir -p $TRUSTAGENT_HOME
if [ $? -ne 0 ]; then
  echo_failure "Cannot create directory: $TRUSTAGENT_HOME"
  exit 1
fi

# define location variables but do not export them yet
TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-$TRUSTAGENT_HOME/configuration}
TRUSTAGENT_REPOSITORY=${TRUSTAGENT_REPOSITORY:-$TRUSTAGENT_HOME/repository}
TRUSTAGENT_VAR=${TRUSTAGENT_VAR:-$TRUSTAGENT_HOME/var}
TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-$TRUSTAGENT_HOME/logs}
TRUSTAGENT_BIN=${TRUSTAGENT_BIN:-$TRUSTAGENT_HOME/bin}
TRUSTAGENT_JAVA=${TRUSTAGENT_JAVA:-$TRUSTAGENT_HOME/java}
TRUSTAGENT_BACKUP=${TRUSTAGENT_BACKUP:-$TRUSTAGENT_REPOSITORY/backup}

# note that the env dir is not configurable; it is defined as "env.d" under home
TRUSTAGENT_ENV=$TRUSTAGENT_HOME/env.d


# ensure we have our own tagent programs in the path
export PATH=$TRUSTAGENT_BIN:$PATH

profile_dir=$HOME
if [ "$(whoami)" == "root" ] && [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ]; then
  profile_dir=$TRUSTAGENT_HOME
fi
profile_name=$profile_dir/$(basename $(getUserProfileFile))

appendToUserProfileFile "export PATH=$TRUSTAGENT_BIN:\$PATH" $profile_name
appendToUserProfileFile "export TRUSTAGENT_HOME=$TRUSTAGENT_HOME" $profile_name

# if there's a monit configuration for trustagent, remove it to prevent
# monit from trying to restart trustagent while we are setting up
if [ "$(whoami)" == "root" ] && [ -f /etc/monit/conf.d/ta.monit ]; then
  datestr=`date +%Y%m%d.%H%M`
  backupdir=$TRUSTAGENT_BACKUP/monit.configuration.$datestr
  mkdir -p $backupdir
  mv /etc/monit/conf.d/ta.monit $backupdir
  service monit restart
fi

# if trustagent is already installed, stop it while we upgrade/reinstall
existing_tagent=`which tagent 2>/dev/null`
if [ -f "$existing_tagent" ]; then
  $existing_tagent stop
fi

# now export the new settings to ensure they're available to subcommands
export TRUSTAGENT_CONFIGURATION TRUSTAGENT_REPOSITORY TRUSTAGENT_VAR TRUSTAGENT_LOGS TRUSTAGENT_BIN TRUSTAGENT_JAVA TRUSTAGENT_BACKUP TRUSTAGENT_ENV


trustagent_backup_configuration() {
  if [ -n "$TRUSTAGENT_CONFIGURATION" ] && [ -d "$TRUSTAGENT_CONFIGURATION" ]; then
    mkdir -p $TRUSTAGENT_BACKUP
    if [ $? -ne 0 ]; then
      echo_warning "Cannot create backup directory: $TRUSTAGENT_BACKUP"
      echo_warning "Backup will be stored in /tmp"
      TRUSTAGENT_BACKUP=/tmp
    fi
    datestr=`date +%Y%m%d.%H%M`
    backupdir=$TRUSTAGENT_BACKUP/trustagent.configuration.$datestr
    cp -r $TRUSTAGENT_CONFIGURATION $backupdir
  fi
}

# backup current configuration, if present
trustagent_backup_configuration

# create application directories (chown will be repeated near end of this script, after setup)
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_VAR $TRUSTAGENT_LOGS; do
  mkdir -p $directory
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
  chmod 700 $directory
done

# before we start, clear the install log (directory must already exist; created above)
logfile=$TRUSTAGENT_LOGS/install.log
date > $logfile

# store directory layout in env file
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_HOME=$TRUSTAGENT_HOME" >> $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_CONFIGURATION=$TRUSTAGENT_CONFIGURATION" >> $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_JAVA=$TRUSTAGENT_JAVA" >> $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_BIN=$TRUSTAGENT_BIN" >> $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_REPOSITORY=$TRUSTAGENT_REPOSITORY" >> $TRUSTAGENT_ENV/trustagent-layout
echo "TRUSTAGENT_LOGS=$TRUSTAGENT_LOGS" >> $TRUSTAGENT_ENV/trustagent-layout

# store trustagent username in env file
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-username
echo "TRUSTAGENT_USERNAME=$TRUSTAGENT_USERNAME" >> $TRUSTAGENT_ENV/trustagent-username

# store log level in env file, if it's set
if [ -n "$TRUSTAGENT_LOG_LEVEL" ]; then
  echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-logging
  echo "TRUSTAGENT_LOG_LEVEL=$TRUSTAGENT_LOG_LEVEL" >> $TRUSTAGENT_ENV/trustagent-logging
fi

# store the auto-exported environment variables in temporary env file
# to make them available after the script uses sudo to switch users;
# we delete that file later
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "export $env_file_var_name='$env_file_var_value'" >> $TRUSTAGENT_ENV/trustagent-setup
done


# ORIGINAL SCRIPT CONFIGURATION:
TRUSTAGENT_V_1_2_HOME=/opt/intel/cloudsecurity/trustagent
TRUSTAGENT_V_1_2_CONFIGURATION=/etc/intel/cloudsecurity
package_config_filename=${TRUSTAGENT_V_1_2_CONFIGURATION}/trustagent.properties
ASSET_TAG_SETUP="y"
logfile=$TRUSTAGENT_LOGS/install.log

#java_required_version=1.7.0_51
# commented out from yum packages: tpm-tools-devel curl-devel (not required because we're using NIARL Privacy CA and we don't need the identity command which used libcurl
APPLICATION_YUM_PACKAGES="openssl  trousers trousers-devel tpm-tools make gcc unzip authbind"
# commented out from apt packages: libcurl4-openssl-dev 
APPLICATION_APT_PACKAGES="openssl libssl-dev libtspi-dev libtspi1 trousers make gcc unzip authbind"
# commented out from YAST packages: libcurl-devel tpm-tools-devel.  also zlib and zlib-devel are dependencies of either openssl or trousers-devel
APPLICATION_YAST_PACKAGES="openssl libopenssl-devel trousers trousers-devel tpm-tools make gcc unzip authbind"
# SUSE uses zypper:.  omitting libtspi1 because trousers-devel already depends on a specific version of it which will be isntalled automatically
APPLICATION_ZYPPER_PACKAGES="openssl libopenssl-devel libopenssl1_0_0 openssl-certs trousers-devel authbind"
# other packages in suse:  libopenssl0_9_8 


# Automatic install steps:
# 1. Install prereqs
# 2. Backup old files
# 3. Create directory structure
# 4. Install Mt Wilson Linux utilities (and use them in this script)
# 5. Install JDK
# 6. Compile TPM commands
# 7. Install Trust Agent files

##### install prereqs can only be done as root
if [ "$(whoami)" == "root" ]; then
  auto_install "TrustAgent requirements" "APPLICATION"
else
  echo_warning "Required packages:"
  auto_install_preview "TrustAgent requirements" "APPLICATION"
fi

##### create directory structure


# packages to install must be in current directory
#JAR_PACKAGE=`ls -1 TrustAgent*.jar 2>/dev/null | tail -n 1`
#MTWILSON_UTIL_PACKAGE=`ls -1 mtwilson-util*.bin 2>/dev/null | tail -n 1`
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
ZIP_PACKAGE=`ls -1 trustagent*.zip 2>/dev/null | tail -n 1`

unzip -DD -o $ZIP_PACKAGE -d "$TRUSTAGENT_HOME" >> $logfile  2>&1
chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME "$TRUSTAGENT_HOME"

# update logback.xml with configured trustagent log directory
if [ -f "$TRUSTAGENT_CONFIGURATION/logback.xml" ]; then
  sed -e "s|<file>.*/trustagent.log</file>|<file>$TRUSTAGENT_LOGS/trustagent.log</file>|" $TRUSTAGENT_CONFIGURATION/logback.xml > $TRUSTAGENT_CONFIGURATION/logback.xml.edited
  if [ $? -eq 0 ]; then
    mv $TRUSTAGENT_CONFIGURATION/logback.xml.edited $TRUSTAGENT_CONFIGURATION/logback.xml
  fi
else
  echo_warning "Logback configuration not found: $TRUSTAGENT_CONFIGURATION/logback.xml"
fi

# If VIRSH_DEFAULT_CONNECT_URI is defined in environment (likely from ~/.bashrc) 
# copy it to our new env.d folder so it will be available to tagent on startup
if [ -n "$LIBVIRT_DEFAULT_URI" ]; then
  echo "LIBVIRT_DEFAULT_URI=$LIBVIRT_DEFAULT_URI" > $TRUSTAGENT_ENV/virsh
elif [ -n "$VIRSH_DEFAULT_CONNECT_URI" ]; then
  echo "VIRSH_DEFAULT_CONNECT_URI=$VIRSH_DEFAULT_CONNECT_URI" > $TRUSTAGENT_ENV/virsh
fi

# Migrate any old data to the new locations  (should be rewritten in java)
v1_aik=$TRUSTAGENT_V_1_2_CONFIGURATION/cert
v2_aik=$TRUSTAGENT_CONFIGURATION
v1_conf=$TRUSTAGENT_V_1_2_CONFIGURATION
v2_conf=$TRUSTAGENT_CONFIGURATION
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
package_config_filename=$TRUSTAGENT_CONFIGURATION/trustagent.properties


# setup authbind to allow non-root trustagent to listen on port 1443
mkdir -p /etc/authbind/byport
if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ -d /etc/authbind/byport ]; then
  touch /etc/authbind/byport/1443
  chmod 500 /etc/authbind/byport/1443
  chown $TRUSTAGENT_USERNAME /etc/authbind/byport/1443
fi


hex2bin_install() {
  return_dir=`pwd`
  cd hex2bin
  make && cp hex2bin $TRUSTAGENT_BIN
  chmod +x $TRUSTAGENT_BIN/hex2bin
  cd $return_dir
}

hex2bin_install

mkdir -p "$TRUSTAGENT_HOME"/share/scripts
cp version "$TRUSTAGENT_HOME"/share/scripts/version.sh
cp functions "$TRUSTAGENT_HOME"/share/scripts/functions.sh
chmod -R 700 "$TRUSTAGENT_HOME"/share/scripts
chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME "$TRUSTAGENT_HOME"/share/scripts

# if prior version had control script in /usr/local/bin, delete it
if [ "$(whoami)" == "root" ] && [ -f tagent ]; then
  rm tagent
fi
if [[ ! -h $TRUSTAGENT_BIN/tagent ]]; then
  ln -s $TRUSTAGENT_BIN/tagent.sh $TRUSTAGENT_BIN/tagent
fi
chmod +x $TRUSTAGENT_BIN/*

# in 2.0.6, java home is now under trustagent home by default
JAVA_HOME=${JAVA_HOME:-$TRUSTAGENT_HOME/share/jdk1.7.0_51}
mkdir -p $JAVA_HOME
#java_install $JAVA_PACKAGE
java_install_in_home $JAVA_PACKAGE
if java_ready_report; then
  # store java location in env file
  echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-java
  echo "export JAVA_HOME=$JAVA_HOME" >> $TRUSTAGENT_ENV/trustagent-java
  echo "export JAVA_CMD=$java" >> $TRUSTAGENT_ENV/trustagent-java
  echo "export JAVA_REQUIRED_VERSION=$JAVA_REQUIRED_VERSION" >> $TRUSTAGENT_ENV/trustagent-java
else
  echo_failure "Java $JAVA_REQUIRED_VERSION not found"
  exit 1
fi

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
    if [ -z "$has_libdir_symlink" ] && [ ! -h $libdir/libcrypto.so ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 $libdir/libcrypto.so
    fi
    if [ -z "$has_usrbin_symlink" ] && [ ! -h /usr/lib/libcrypto.so ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 /usr/lib/libcrypto.so
    fi
    
    #if [ -n "$yum" ]; then #RHEL
    #elif [[ -n "$zypper" || -n "$yast" ]]; then #SUSE
    #fi

    ldconfig
  fi
}

if [ "$(whoami)" == "root" ]; then
  fix_libcrypto
fi

return_dir=`pwd`

  is_citrix_xen=`lsb_release -a | grep "^Distributor ID" | grep XenServer`
  if [ -n "$is_citrix_xen" ]; then
    # we have precompiled binaries for citrix-xen
    echo "Installing TPM commands... "
    cd commands-citrix-xen
    chmod 755 aikquote NIARL_TPM_Module openssl.sh
    cp aikquote NIARL_TPM_Module openssl.sh $TRUSTAGENT_HOME/bin
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
      cp aikquote $TRUSTAGENT_HOME/bin
      COMPILE_OK=yes
      echo_success "OK"
    else
      echo_failure "FAILED"
    fi
    chmod 755 aikquote NIARL_TPM_Module openssl.sh
    cp aikquote NIARL_TPM_Module openssl.sh $TRUSTAGENT_HOME/bin
    cd ..
  fi
  cd ..
  # create trustagent.version file
  package_version_filename=$TRUSTAGENT_ENV/trustagent.version
  datestr=`date +%Y-%m-%d.%H%M`
  touch $package_version_filename
  chmod 600 $package_version_filename
  chown $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $package_version_filename
  echo "# Installed Trust Agent on ${datestr}" > $package_version_filename
  echo "TRUSTAGENT_VERSION=${VERSION}" >> $package_version_filename
  echo "TRUSTAGENT_RELEASE=\"${BUILD}\"" >> $package_version_filename

cd $return_dir

if [ "$(whoami)" == "root" ]; then
  echo "Registering tagent in start up"
  register_startup_script $TRUSTAGENT_BIN/tagent tagent 21 >>$logfile 2>&1
  # trousers has N=20 startup number, need to lookup and do a N+1
else
  echo_warning "Skipping startup script registration"
fi

fix_existing_aikcert() {
  local aikdir=$TRUSTAGENT_CONFIGURATION/cert
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

if [ "$(whoami)" == "root" ]; then
  monit_install $MONIT_PACKAGE

  mkdir -p /etc/monit/conf.d
  # ta.monit is already backed up at the beginning of setup.sh
  # not using backup_file /etc/monit/conf.d/ta.monit because we want it in a different folder to prevent monit from reading the new ta.monit AND all the backups and complaining about duplicates
  cp ta.monit /etc/monit/conf.d/ta.monit

  if [ -f /etc/monit/monitrc ]; then
    backupdir=$TRUSTAGENT_BACKUP/monitrc.$backupdate
    mkdir -p $backupdir
    cp /etc/monit/monitrc $backupdir
  fi
  cp monitrc /etc/monit/monitrc
  chmod 700 /etc/monit/monitrc

  if ! grep -q "include /etc/monit/conf.d/*" /etc/monit/monitrc; then 
   echo "include /etc/monit/conf.d/*" >> /etc/monit/monitrc
  fi

else
  echo_warning "Skipping monit installation"
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

# Ensure we have given trustagent access to its files
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_VAR $TRUSTAGENT_LOGS; do
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
done


if [ "$(whoami)" == "root" ]; then
  echo "Updating system information"
  tagent update-system-info 2>/dev/null
else
  echo_warning "Skipping updating system information"
fi

# create a trustagent username "mtwilson" with no password and all privileges
# which allows mtwilson to access it until mtwilson UI is updated to allow
# entering username and password for accessing the trust agent
tagent password mtwilson --nopass *:*

# give tagent a chance to do any other setup (such as the .env file and pcakey)
# and make sure it's successful before trying to start the trust agent
# NOTE: only the output from start-http-server is redirected to the logfile;
#       the stdout from the setup command will be displayed
tagent setup
tagent start >>$logfile  2>&1

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
    tagent setup register-tpm-password
fi

# NOTE:  monit should only be restarted AFTER trustagent is up and running
#        so that it doesn't try to start it before we're done with our setup
#        tasks.
if [ "$(whoami)" == "root" ]; then
  tagent status > /dev/null
  if [ $? ]; then
    service monit restart
  else
    echo "Trust agent not running; skipping monit restart"
  fi
fi

