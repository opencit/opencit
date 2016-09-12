#!/bin/bash

# Postconditions:
# * exit with error code 1 only if there was a fatal error:
#   functions.sh not found (must be adjacent to this file in the package)
#   

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
# 28. create trustagent-version file
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

# application defaults (these are not configurable and used only in this script so no need to export)
DEFAULT_TRUSTAGENT_HOME=/opt/trustagent
DEFAULT_TRUSTAGENT_USERNAME=tagent
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}

# default settings
export TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-$DEFAULT_TRUSTAGENT_HOME}
TRUSTAGENT_LAYOUT=${TRUSTAGENT_LAYOUT:-home}

# the env directory is not configurable; it is defined as TRUSTAGENT_HOME/env.d and the
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

directory_layout() {
if [ "$TRUSTAGENT_LAYOUT" == "linux" ]; then
  export TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-/etc/trustagent}
  export TRUSTAGENT_REPOSITORY=${TRUSTAGENT_REPOSITORY:-/var/opt/trustagent}
  export TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-/var/log/trustagent}
elif [ "$TRUSTAGENT_LAYOUT" == "home" ]; then
  export TRUSTAGENT_CONFIGURATION=${TRUSTAGENT_CONFIGURATION:-$TRUSTAGENT_HOME/configuration}
  export TRUSTAGENT_REPOSITORY=${TRUSTAGENT_REPOSITORY:-$TRUSTAGENT_HOME/repository}
  export TRUSTAGENT_LOGS=${TRUSTAGENT_LOGS:-$TRUSTAGENT_HOME/logs}
fi
export TRUSTAGENT_VAR=${TRUSTAGENT_VAR:-$TRUSTAGENT_HOME/var}
export TRUSTAGENT_BIN=${TRUSTAGENT_BIN:-$TRUSTAGENT_HOME/bin}
export TRUSTAGENT_JAVA=${TRUSTAGENT_JAVA:-$TRUSTAGENT_HOME/java}
export TRUSTAGENT_BACKUP=${TRUSTAGENT_BACKUP:-$TRUSTAGENT_REPOSITORY/backup}
export INSTALL_LOG_FILE=$TRUSTAGENT_LOGS/install.log
}

# identify tpm version
# postcondition:
#   variable TPM_VERSION is set to 1.2 or 2.0
detect_tpm_version() {
  export TPM_VERSION
  if [[ -f "/sys/class/misc/tpm0/device/caps" || -f "/sys/class/tpm/tpm0/device/caps" ]]; then
    TPM_VERSION=1.2
  else
  #  if [[ -f "/sys/class/tpm/tpm0/device/description" && `cat /sys/class/tpm/tpm0/device/description` == "TPM 2.0 Device" ]]; then
    TPM_VERSION=2.0
  fi
}

detect_tpm_version

# The version script is automatically generated at build time and looks like this:
#ARTIFACT=mtwilson-trustagent-installer
#VERSION=3.0
#BUILD="Fri, 5 Jun 2015 15:55:20 PDT (release-3.0)"

directory_layout

if [ "${TRUSTAGENT_SETUP_PREREQS:-yes}" == "yes" ]; then
  # set TRUSTAGENT_REBOOT=no (in trustagent.env) if you want to ensure it doesn't reboot
  # set TRUSTAGENT_SETUP_PREREQS=no (in trustagent.env) if you want to skip this step 
  source setup_prereqs.sh
fi

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

# define application directory layout
directory_layout


# before we start, clear the install log (directory must already exist; created above)
mkdir -p $(dirname $INSTALL_LOG_FILE)
if [ $? -ne 0 ]; then
  echo_failure "Cannot write to log directory: $(dirname $INSTALL_LOG_FILE)"
  exit 1
fi
date > $INSTALL_LOG_FILE
if [ $? -ne 0 ]; then
  echo_failure "Cannot write to log file: $INSTALL_LOG_FILE"
  exit 1
fi
chown $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $INSTALL_LOG_FILE
logfile=$INSTALL_LOG_FILE

# create application directories (chown will be repeated near end of this script, after setup)
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_VAR $TRUSTAGENT_LOGS; do
  # mkdir -p will return 0 if directory exists or is a symlink to an existing directory or directory and parents can be created
  mkdir -p $directory
  if [ $? -ne 0 ]; then
    echo_failure "Cannot create directory: $directory"
    exit 1
  fi
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
  chmod 700 $directory
done

# ensure we have our own tagent programs in the path
export PATH=$TRUSTAGENT_BIN:$PATH

# ensure that trousers and tpm tools are in the path
export PATH=$PATH:/usr/sbin:/usr/local/sbin

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

# if an existing tagent is already running, stop it while we install
existing_tagent=`which tagent 2>/dev/null`
if [ -f "$existing_tagent" ]; then
  $existing_tagent stop
fi

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
trustagent_backup_repository() {
  if [ -n "$TRUSTAGENT_REPOSITORY" ] && [ -d "$TRUSTAGENT_REPOSITORY" ]; then
    mkdir -p $TRUSTAGENT_BACKUP
    if [ $? -ne 0 ]; then
      echo_warning "Cannot create backup directory: $TRUSTAGENT_BACKUP"
      echo_warning "Backup will be stored in /tmp"
      TRUSTAGENT_BACKUP=/tmp
    fi
    datestr=`date +%Y%m%d.%H%M`
    backupdir=$TRUSTAGENT_BACKUP/trustagent.repository.$datestr
    cp -r $TRUSTAGENT_REPOSITORY $backupdir
  fi
}

# backup current configuration and data, if they exist
trustagent_backup_configuration
#trustagent_backup_repository

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

# make sure unzip and authbind are installed
#java_required_version=1.7.0_51
#Adding redhat-lsb libvirt for bug 5289
#Adding net-tools for bug 5285
#adding openssl-devel for bug 5284
TRUSTAGENT_YUM_PACKAGES="zip unzip authbind make gcc vim-common"
TRUSTAGENT_APT_PACKAGES="zip unzip authbind make gcc dpkg-dev vim-common"
TRUSTAGENT_YAST_PACKAGES="zip unzip authbind make gcc vim-common"
TRUSTAGENT_ZYPPER_PACKAGES="zip unzip authbind make gcc vim-common"
# save tpm version in trust agent configuration directory
echo -n "$TPM_VERSION" > $TRUSTAGENT_CONFIGURATION/tpm-version

##### install prereqs can only be done as root
if [ "$(whoami)" == "root" ]; then
  auto_install "Installer requirements" "TRUSTAGENT"
  if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi
else
  echo_warning "Required packages:"
  auto_install_preview "TrustAgent requirements" "TRUSTAGENT"
fi

if [ "$TPM_VERSION" == "2.0" ]; then
  # install tss2 and tpm2-tools for tpm2.0
  ./mtwilson-tpm2-packages-2.2-SNAPSHOT.bin
fi

# If VIRSH_DEFAULT_CONNECT_URI is defined in environment (likely from ~/.bashrc) 
# copy it to our new env folder so it will be available to tagent on startup
if [ -n "$LIBVIRT_DEFAULT_URI" ]; then
  echo "LIBVIRT_DEFAULT_URI=$LIBVIRT_DEFAULT_URI" > $TRUSTAGENT_ENV/virsh
elif [ -n "$VIRSH_DEFAULT_CONNECT_URI" ]; then
  echo "VIRSH_DEFAULT_CONNECT_URI=$VIRSH_DEFAULT_CONNECT_URI" > $TRUSTAGENT_ENV/virsh
fi

cp version $TRUSTAGENT_CONFIGURATION/trustagent-version

# delete existing java files, to prevent a situation where the installer copies
# a newer file but the older file is also there
if [ -d $TRUSTAGENT_HOME/java ]; then
  rm -f $TRUSTAGENT_HOME/java/*.jar 2>/dev/null
fi

# extract trustagent  (trustagent-zip-0.1-SNAPSHOT.zip)
echo "Extracting application..."
TRUSTAGENT_ZIPFILE=`ls -1 trustagent-*.zip 2>/dev/null | head -n 1`
unzip -oq $TRUSTAGENT_ZIPFILE -d $TRUSTAGENT_HOME

# update logback.xml with configured trustagent log directory
if [ -f "$TRUSTAGENT_CONFIGURATION/logback.xml" ]; then
  sed -e "s|<file>.*/trustagent.log</file>|<file>$TRUSTAGENT_LOGS/trustagent.log</file>|" $TRUSTAGENT_CONFIGURATION/logback.xml > $TRUSTAGENT_CONFIGURATION/logback.xml.edited
  if [ $? -eq 0 ]; then
    mv $TRUSTAGENT_CONFIGURATION/logback.xml.edited $TRUSTAGENT_CONFIGURATION/logback.xml
  fi
else
  echo_warning "Logback configuration not found: $TRUSTAGENT_CONFIGURATION/logback.xml"
fi

# set permissions
chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $TRUSTAGENT_HOME
chmod 755 $TRUSTAGENT_BIN/*

# if prior version had control script in /usr/local/bin, delete it
if [ "$(whoami)" == "root" ] && [ -f /usr/local/bin/tagent ]; then
  rm /usr/local/bin/tagent
fi
EXISTING_TAGENT_COMMAND=`which tagent 2>/dev/null`
if [ -n "$EXISTING_TAGENT_COMMAND" ]; then
  rm -f "$EXISTING_TAGENT_COMMAND"
fi
# link /usr/local/bin/tagent -> /opt/trustagent/bin/tagent
ln -s $TRUSTAGENT_BIN/tagent.sh /usr/local/bin/tagent
if [[ ! -h $TRUSTAGENT_BIN/tagent ]]; then
  ln -s $TRUSTAGENT_BIN/tagent.sh $TRUSTAGENT_BIN/tagent
fi

### INSTALL MEASUREMENT AGENT --comment out for now for cit 2.2
#echo "Installing measurement agent..."
#TBOOTXM_PACKAGE=`ls -1 tbootxm-*.bin 2>/dev/null | tail -n 1`
#if [ -z "$TBOOTXM_PACKAGE" ]; then
#  echo_failure "Failed to find measurement agent installer package"
#  exit -1
#fi
#./$TBOOTXM_PACKAGE
#if [ $? -ne 0 ]; then echo_failure "Failed to install measurement agent"; exit -1; fi

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
if [ ! -f /etc/authbind/byport/1443 ]; then
  if [ "$(whoami)" == "root" ]; then
    if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ -d /etc/authbind/byport ]; then
      touch /etc/authbind/byport/1443
      chmod 500 /etc/authbind/byport/1443
      chown $TRUSTAGENT_USERNAME /etc/authbind/byport/1443
    fi
  else
    echo_warning "You must be root to setup authbind configuration"
  fi
fi
# setup authbind to allow non-root trustagent to listen on ports 80 and 443
if [ -n "$TRUSTAGENT_USERNAME" ] && [ "$TRUSTAGENT_USERNAME" != "root" ] && [ -d /etc/authbind/byport ]; then
  touch /etc/authbind/byport/80 /etc/authbind/byport/443
  chmod 500 /etc/authbind/byport/80 /etc/authbind/byport/443
  chown $TRUSTAGENT_USERNAME /etc/authbind/byport/80 /etc/authbind/byport/443
fi

if [ "$(whoami)" == "root" ]; then
  # this section adds tagent sudoers file so that user can execute txt-stat command
  txtStat=$(which txt-stat 2>/dev/null)
  if [ -z "$txtStat" ]; then
    echo_failure "cannot find command: txt-stat (from tboot)"
    exit 1
  else
    echo -e "Cmnd_Alias PACKAGE_MANAGER = ${txtStat}\nDefaults:${TRUSTAGENT_USERNAME} "'!'"requiretty\n${TRUSTAGENT_USERNAME} ALL=(root) NOPASSWD: PACKAGE_MANAGER" > "/etc/sudoers.d/${TRUSTAGENT_USERNAME}"
    chmod 440 "/etc/sudoers.d/${TRUSTAGENT_USERNAME}"
  fi
fi


if [ "$TPM_VERSION" == "1.2" ]; then
### symlinks
#tpm_nvinfo
tpmnvinfo=`which tpm_nvinfo 2>/dev/null`
if [ -z "$tpmnvinfo" ]; then
  echo_failure "cannot find command: tpm_nvinfo (from tpm-tools)"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvinfo" ]]; then
    ln -s "$tpmnvinfo" "$TRUSTAGENT_BIN"
  fi
fi

#tpm_nvrelease
tpmnvrelease=`which tpm_nvrelease 2>/dev/null`
if [ -z "$tpmnvrelease" ]; then
  echo_failure "cannot find command: tpm_nvrelease (from tpm-tools)"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvrelease" ]]; then
    ln -s "$tpmnvrelease" "$TRUSTAGENT_BIN"
  fi
fi

#tpm_nvwrite
tpmnvwrite=`which tpm_nvwrite 2>/dev/null`
if [ -z "$tpmnvwrite" ]; then
  echo_failure "cannot find command: tpm_nvwrite (from tpm-tools)"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvwrite" ]]; then
    ln -s "$tpmnvwrite" "$TRUSTAGENT_BIN"
  fi
fi

#tpm_nvread
tpmnvread=`which tpm_nvread 2>/dev/null`
if [ -z "$tpmnvread" ]; then
  echo_failure "cannot find command: tpm_nvread (from tpm-tools)"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvread" ]]; then
    ln -s "$tpmnvread" "$TRUSTAGENT_BIN"
  fi
fi

#tpm_nvdefine
tpmnvdefine=`which tpm_nvdefine 2>/dev/null`
if [ -z "$tpmnvdefine" ]; then
  echo_failure "cannot find command: tpm_nvdefine (from tpm-tools)"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/tpm_nvdefine" ]]; then
    ln -s "$tpmnvdefine" "$TRUSTAGENT_BIN"
  fi
fi

    #tpm_bindaeskey
    if [ -h "/usr/local/bin/tpm_bindaeskey" ]; then
      rm -f "/usr/local/bin/tpm_bindaeskey"
    fi
    ln -s "$TRUSTAGENT_BIN/tpm_bindaeskey" /usr/local/bin/tpm_bindaeskey

    #tpm_unbindaeskey
    if [ -h "/usr/local/bin/tpm_unbindaeskey" ]; then
      rm -f "/usr/local/bin/tpm_unbindaeskey"
    fi
    ln -s "$TRUSTAGENT_BIN/tpm_unbindaeskey" /usr/local/bin/tpm_unbindaeskey

    #tpm_createkey
    if [ -h "/usr/local/bin/tpm_createkey" ]; then
      rm -f "/usr/local/bin/tpm_createkey"
    fi
    ln -s "$TRUSTAGENT_BIN/tpm_createkey" /usr/local/bin/tpm_createkey

    #tpm_signdata
    if [ -h "/usr/local/bin/tpm_signdata" ]; then
      rm -f "/usr/local/bin/tpm_signdata"
    fi
    ln -s "$TRUSTAGENT_BIN/tpm_signdata" /usr/local/bin/tpm_signdata

fi
# end if [ "$TPM_VERSION" == "1.2" ]


hex2bin_install() {
  # build hex2bin in sub-shell so we change of directory is temporary
  (
    cd hex2bin
    make && cp hex2bin $TRUSTAGENT_BIN
    chmod +x $TRUSTAGENT_BIN/hex2bin
  )
}

hex2bin_install

hex2bin=`which hex2bin 2>/dev/null`
if [ -z "$hex2bin" ]; then
  echo_failure "cannot find command: hex2bin"
  exit 1
else
  if [[ ! -h "$TRUSTAGENT_BIN/hex2bin" ]] && [[ ! -f "$TRUSTAGENT_BIN/hex2bin" ]]; then
    ln -s "$hex2bin" "$TRUSTAGENT_BIN"
  fi
fi

mkdir -p "$TRUSTAGENT_HOME"/share/scripts
cp version "$TRUSTAGENT_HOME"/share/scripts/version.sh
cp functions "$TRUSTAGENT_HOME"/share/scripts/functions.sh
chmod -R 700 "$TRUSTAGENT_HOME"/share/scripts
chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME "$TRUSTAGENT_HOME"/share/scripts
chmod +x $TRUSTAGENT_BIN/*

# in 3.0, java home is now under trustagent home by default
JAVA_HOME=${JAVA_HOME:-$TRUSTAGENT_HOME/share/jdk1.7.0_51}
mkdir -p "$TRUSTAGENT_HOME/share"   #$JAVA_HOME
#java_install $JAVA_PACKAGE
JAVA_PACKAGE=$(ls -1 jdk-* jre-* java-* 2>/dev/null | tail -n 1)
java_install_in_home $JAVA_PACKAGE

# store java location in env file
echo "# $(date)" > $TRUSTAGENT_ENV/trustagent-java
echo "export JAVA_HOME=$JAVA_HOME" >> $TRUSTAGENT_ENV/trustagent-java
echo "export JAVA_CMD=$JAVA_HOME/bin/java" >> $TRUSTAGENT_ENV/trustagent-java
echo "export JAVA_REQUIRED_VERSION=$JAVA_REQUIRED_VERSION" >> $TRUSTAGENT_ENV/trustagent-java

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
  local has_libcrypto=`find / -name libcrypto.so.1.0.0 2>/dev/null | head -1`
  local libdir=`dirname $has_libcrypto`
  local has_libdir_symlink=`find $libdir -name libcrypto.so`
  local has_usrbin_symlink=`find /usr/bin -name libcrypto.so`
  local has_usrlib_symlink=`find /usr/lib -name libcrypto.so`
  if [ -n "$has_libcrypto" ]; then
    if [ -z "$has_libdir_symlink" ] && [ ! -h $libdir/libcrypto.so ]; then
      echo "Creating missing symlink for $has_libcrypto"
      ln -s $libdir/libcrypto.so.1.0.0 $libdir/libcrypto.so
    fi
    #if [ -z "$has_usrbin_symlink" ] && [ ! -h /usr/lib/libcrypto.so ]; then
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
    if [ "$TPM_VERSION" == "1.2" ]; then
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
    else
      cd commands
      chmod 755 NIARL_TPM_Module openssl.sh
      cp NIARL_TPM_Module openssl.sh $TRUSTAGENT_HOME/bin
      cd ..      
    fi 
  fi
  cd ..
  # create trustagent-version file
  package_version_filename=$TRUSTAGENT_ENV/trustagent-version
  datestr=`date +%Y-%m-%d.%H%M`
  touch $package_version_filename
  chmod 600 $package_version_filename
  chown $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $package_version_filename
  echo "# Installed Trust Agent on ${datestr}" > $package_version_filename
  echo "TRUSTAGENT_VERSION=${VERSION}" >> $package_version_filename
  echo "TRUSTAGENT_RELEASE=\"${BUILD}\"" >> $package_version_filename

cd $return_dir

if [ "$(whoami)" == "root" ]; then
  if [ "$TPM_VERSION" == "1.2" ]; then
    tcsdBinary=$(which tcsd)
    if [ -z "$tcsdBinary" ]; then
      echo_failure "Not able to resolve trousers binary location. trousers installed?"
      exit 1
    fi
    # systemd enable trousers for RHEL 7.2 startup
    systemctlCommand=`which systemctl 2>/dev/null`
    if [ -d "/etc/systemd/system" ] && [ -n "$systemctlCommand" ]; then
      echo "systemctl enabling trousers service..."
      "$systemctlCommand" enable tcsd.service 2>/dev/null
      "$systemctlCommand" start tcsd.service 2>/dev/null
    fi
  fi
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
  if [ $? -ne 0 ]; then echo_failure "Failed to install monit through package installer"; return 1; fi
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
  if [ $? -ne 0 ]; then echo_failure "Failed to install developer tools through package installer"; return 1; fi
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
  echo "chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory" >>$logfile
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory 2>>$logfile
done

if [ "$(whoami)" == "root" ]; then
  echo "Updating system information"
  tagent update-system-info 2>/dev/null
else
  echo_warning "Skipping updating system information"
fi

# before running any tagent commands update the extensions cache file
tagent setup update-extensions-cache-file --force 2>/dev/null

# for tpm2.0, check if we own the tpm and if not just clear ownership here so
# other setup steps will succeed
maybe_clear_tpm2() {
    local is_owned=$($TRUSTAGENT_HOME/bin/tpm2-isowned)
    if [ "$is_owned" == "1" ]; then
        # it's owned, do we have the password?
        local tpm_passwd="$TPM_OWNER_SECRET"
        if [ -z "$tpm_passwd" ]; then
            tpm_passwd=$(tagent config tpm.owner.secret)
        fi
        if [ -n "$tpm_passwd" ]; then
            local is_owner=$(TRUSTAGENT_HOME/bin/tpm2-isowner "$tpm_passwd")
            if [ "$is_owner" == "0" ]; then
                # we are not the owner. clear it.
                tpm2_takeownership -c
                return $?
            fi
        else
            # we don't have the password. clear it.
            tpm2_takeownership -c
            return $?
        fi
    fi
}

if [ "$TPM_VERSION" == "2.0" ]; then
    maybe_clear_tpm2
fi

  # create a trustagent username "mtwilson" with no password and all privileges
  # which allows mtwilson to access it until mtwilson UI is updated to allow
  # entering username and password for accessing the trust agent
  
  # Starting with 3.0, we have a separate task that creates a new user name and password per host
  # So we do not need to create this user without password. This is would address the security issue as well
  #/usr/local/bin/tagent password mtwilson --nopass *:*

# give tagent a chance to do any other setup (such as the .env file and pcakey)
# and make sure it's successful before trying to start the trust agent
# NOTE: only the output from start-http-server is redirected to the logfile;
#       the stdout from the setup command will be displayed
tagent setup
#tagent start >>$logfile  2>&1

# optional: register tpm password with mtwilson so pull provisioning can
#           be accomplished with less reboots (no ownership transfer)
#           default is not to register the password.
prompt_with_default REGISTER_TPM_PASSWORD       "Register TPM password with service to support asset tag automation? [y/n]" ${REGISTER_TPM_PASSWORD:-no}
if [[ "$REGISTER_TPM_PASSWORD" == "y" || "$REGISTER_TPM_PASSWORD" == "Y" || "$REGISTER_TPM_PASSWORD" == "yes" ]]; then 
  #prompt_with_default ASSET_TAG_URL "Asset Tag Server URL: (https://[SERVER]:[PORT]/mtwilson/v2)" ${ASSET_TAG_URL}
  prompt_with_default MTWILSON_API_USERNAME "Username:" ${MTWILSON_API_USERNAME}
  prompt_with_default_password MTWILSON_API_PASSWORD "Password:" ${MTWILSON_API_PASSWORD}
  export MTWILSON_API_USERNAME MTWILSON_API_PASSWORD
  export HARDWARE_UUID=`dmidecode |grep UUID | awk '{print $2}'`
  tagent setup register-tpm-password
fi

# ensure the trustagent owns all the content created during setup
for directory in $TRUSTAGENT_HOME $TRUSTAGENT_CONFIGURATION $TRUSTAGENT_JAVA $TRUSTAGENT_BIN $TRUSTAGENT_ENV $TRUSTAGENT_REPOSITORY $TRUSTAGENT_LOGS; do
  chown -R $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $directory
done

# start the server, unless the NOSETUP variable is defined
if [ -z "$TRUSTAGENT_NOSETUP" ]; then
  # the master password is required
  # if already user provided we assume user will also provide later for restarts
  # otherwise, we generate and store the password
  if [ -z "$TRUSTAGENT_PASSWORD" ] && [ ! -f $TRUSTAGENT_CONFIGURATION/.trustagent_password ]; then
    touch $TRUSTAGENT_CONFIGURATION/.trustagent_password
    chown $TRUSTAGENT_USERNAME:$TRUSTAGENT_USERNAME $TRUSTAGENT_CONFIGURATION/.trustagent_password
    tagent generate-password > $TRUSTAGENT_CONFIGURATION/.trustagent_password
  fi
  
  if [ "${LOCALHOST_INTEGRATION}" == "yes" ]; then
    /opt/trustagent/bin/tagent.sh localhost-integration
  fi

  tagent import-config --in="${TRUSTAGENT_CONFIGURATION}/trustagent.properties" --out="${TRUSTAGENT_CONFIGURATION}/trustagent.properties"
  #tagent config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,trustagent,jersey-media-multipart}" >/dev/null
  #tagent config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null

  ## dashboard
  #tagent config mtwilson.navbar.buttons trustagent-keys,mtwilson-configuration-settings-ws-v2,mtwilson-core-html5 >/dev/null
  #tagent config mtwilson.navbar.hometab keys >/dev/null

  #tagent config jetty.port ${JETTY_PORT:-80} >/dev/null
  #tagent config jetty.secure.port ${JETTY_SECURE_PORT:-443} >/dev/null

  #tagent setup
  tagent start
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

# remove the temporary setup env file
rm -f $TRUSTAGENT_ENV/trustagent-setup
