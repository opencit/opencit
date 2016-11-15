#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# default settings; can override by exporting them before running
# the installer or by including them in mtwilson.env
export MTWILSON_HOME=${MTWILSON_HOME:-/opt/mtwilson}

export INSTALLED_MARKER_FILE=${INSTALLED_MARKER_FILE:-/var/opt/intel/.mtwilsonInstalled}
export LOG_ROTATION_PERIOD=${LOG_ROTATION_PERIOD:-daily}
export LOG_COMPRESS=${LOG_COMPRESS:-compress}
export LOG_DELAYCOMPRESS=${LOG_DELAYCOMPRESS:-delaycompress}
export LOG_COPYTRUNCATE=${LOG_COPYTRUNCATE:-copytruncate}
export LOG_SIZE=${LOG_SIZE:-100M}
export LOG_OLD=${LOG_OLD:-7}
export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}

# the layout setting is used only by this script
# and it is not saved or used by the app script
MTWILSON_LAYOUT=${MTWILSON_LAYOUT:-home}

###########################################################

# ensure we can write to the log file
touch $INSTALL_LOG_FILE >/dev/null 2>&1
if [ -f $INSTALL_LOG_FILE ] && [ ! -w $INSTALL_LOG_FILE ]; then
  echo "Cannot write to install log file: $INSTALL_LOG_FILE"
  exit 1
fi

if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# determine if we are installing as root or non-root
if [ "$(whoami)" == "root" ]; then
  # create a mtwilson user if there isn't already one created
  export MTWILSON_USERNAME=${MTWILSON_USERNAME:-mtwilson}
  if ! getent passwd $MTWILSON_USERNAME >/dev/null 2>&1; then
    useradd --comment "Mt Wilson" --home $MTWILSON_HOME --system --shell /bin/false $MTWILSON_USERNAME
    usermod --lock $MTWILSON_USERNAME
    # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $MTWILSON_USERNAME"
  fi
else
  # already running as mtwilson user
  export MTWILSON_USERNAME=$(whoami)
  echo_warning "Running as $MTWILSON_USERNAME; if installation fails try again as root"
  if [ ! -w "$MTWILSON_HOME" ] && [ ! -w $(dirname $MTWILSON_HOME) ]; then
    export MTWILSON_HOME=$(cd ~ && pwd)
  fi
  echo_warning "Installing as $MTWILSON_USERNAME into $MTWILSON_HOME"  
fi

chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $INSTALL_LOG_FILE
date > $INSTALL_LOG_FILE

# computed values
export PATH=$MTWILSON_HOME/bin:$PATH

# define application directory layout
if [ "$MTWILSON_LAYOUT" == "linux" ]; then
  export MTWILSON_CONFIGURATION=${MTWILSON_CONFIGURATION:-/etc/mtwilson}
  export MTWILSON_REPOSITORY=${MTWILSON_REPOSITORY:-/var/opt/mtwilson}
  export MTWILSON_LOGS=${MTWILSON_LOGS:-/var/log/mtwilson}
elif [ "$MTWILSON_LAYOUT" == "home" ]; then
  export MTWILSON_CONFIGURATION=${MTWILSON_CONFIGURATION:-$MTWILSON_HOME/configuration}
  export MTWILSON_REPOSITORY=${MTWILSON_REPOSITORY:-$MTWILSON_HOME/repository}
  export MTWILSON_LOGS=${MTWILSON_LOGS:-$MTWILSON_HOME/logs}
fi
export MTWILSON_BIN=${MTWILSON_BIN:-$MTWILSON_HOME/bin}
export MTWILSON_JAVA=${MTWILSON_JAVA:-$MTWILSON_HOME/java}
export MTWILSON_BACKUP=${MTWILSON_BACKUP:-$MTWILSON_REPOSITORY/backup}

# the env directory is not configurable; it is defined as MTWILSON_HOME/env.d
# and the administrator may use a symlink if necessary to place it anywhere else
export MTWILSON_ENV=$MTWILSON_HOME/env.d

# load application environment variables if already defined
if [ -d $MTWILSON_ENV ]; then
  MTWILSON_ENV_FILES=$(ls -1 $MTWILSON_ENV/*)
  for env_file in $MTWILSON_ENV_FILES; do
    . $env_file
    env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
    if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
  done
fi

# load installer environment file, if present
if [ -f ~/mtwilson.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/mtwilson.env"
  . ~/mtwilson.env
  env_file_exports=$(cat ~/mtwilson.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
fi

#If user is non root make sure all prereq directories are created and owned by nonroot user
if [ "$(whoami)" != "root" ]; then
  if [ ! -d $MTWILSON_HOME ]; then
   echo_failure "$MTWILSON_HOME is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${home=`stat -c '%U' $MTWILSON_HOME`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "$MTWILSON_HOME is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d /opt/intel ]; then
   echo_failure "/opt/intel is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${optintel=`stat -c '%U' /opt/intel`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/opt/intel is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d /etc/intel ]; then
   echo_failure "/etc/intel is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${etcintel=`stat -c '%U' /etc/intel`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/etc/intel is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d /opt/mtwilson ]; then
   echo_failure "/opt/mtwilson is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${optmtw=`stat -c '%U' /opt/mtwilson`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/opt/mtwilson is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d /var/opt/intel ]; then
   echo_failure "/var/opt/intel is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${varoptintel=`stat -c '%U' /var/opt/intel`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/var/opt/intel is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d /usr/local/share/mtwilson ]; then
   echo_failure "/usr/local/share/mtwilson is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${ulsmtw=`stat -c '%U' /usr/local/share/mtwilson`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/usr/local/share/mtwilson is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif ! [[ -d /etc/intel/cloudsecurity || -L /etc/intel/cloudsecurity ]]; then
   echo_failure "/etc/intel/cloudsecurity is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${eics=`stat -c '%U' /etc/intel/cloudsecurity`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "/etc/intel/cloudsecurity is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  elif [ ! -d $MTWILSON_HOME ]; then
   echo_failure "$MTWILSON_HOME is not available. Please create one and change its owner to $MTWILSON_USERNAME."
   exit 1
  elif [ "${mtwhome=`stat -c '%U' $MTWILSON_HOME`}" != "$MTWILSON_USERNAME" ]; then
   echo_failure "$MTWILSON_HOME is not owned by $MTWILSON_USERNAME. Please update the owner."
   exit 1
  else 
   echo "Prerequisite check is successful"
  fi
fi

# if monit is running and
# if there's a monit configuration for mtwilson, remove it to prevent
# monit from trying to restart mtwilson while we are setting up
echo "Checking for active Monit service..." >>$INSTALL_LOG_FILE
service monit status >>$INSTALL_LOG_FILE 2>&1
if [ $? -eq 0 ] && [ "$(whoami)" == "root" ] && [ -d /etc/monit/conf.d ]; then
  datestr=`date +%Y%m%d.%H%M`
  backupdir=$MTWILSON_BACKUP/monit.configuration.$datestr
  echo "Backing up Monit configuration files to $backupdir" >> $INSTALL_LOG_FILE
  mkdir -p $backupdir 2>>$INSTALL_LOG_FILE
  mv /etc/monit/conf.d/*.mtwilson $backupdir 2>>$INSTALL_LOG_FILE
  service monit restart
fi

# if an existing mtwilson is already running, stop it while we install
echo "Checking for previously-installed Mt Wilson..." >>$INSTALL_LOG_FILE
prev_mtwilson="$(which mtwilson 2>/dev/null)"
if [ -n "$prev_mtwilson" ] && [ "$(whoami)" == "root" ]; then
  # stop mtwilson; this sometimes does not work
  $prev_mtwilson stop
  echo "After '$prev_mtwilson stop', checking status again..." >>$INSTALL_LOG_FILE
  $prev_mtwilson status >>$INSTALL_LOG_FILE
  # remove previous mtwilson script
  rm -f $prev_mtwilson
fi


# if upgrading as non-root user, admin must grant read/write permission to /etc/intel/clousecurity before running installer
if [ -L $MTWILSON_CONFIGURATION ]; then rm -f $MTWILSON_CONFIGURATION; fi
if [ -L /etc/intel/cloudsecurity ]; then rm -f /etc/intel/cloudsecurity; fi
if [ -d /etc/intel/cloudsecurity ]; then
  echo "Prior configuration exists:" >>$INSTALL_LOG_FILE
  ls -l /etc/intel >>$INSTALL_LOG_FILE
  if [ -w /etc/intel/cloudsecurity ]; then
    echo "Migrating configuration from /etc/intel/cloudsecurity to $MTWILSON_CONFIGURATION" >>$INSTALL_LOG_FILE
    mkdir -p $MTWILSON_CONFIGURATION
    cp -r /etc/intel/cloudsecurity/* $MTWILSON_CONFIGURATION
    chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME $MTWILSON_CONFIGURATION
    rm -rf /etc/intel/cloudsecurity
    ln -s $MTWILSON_CONFIGURATION /etc/intel/cloudsecurity
  else
    echo_failure "Cannot migrate configuration from /etc/intel/cloudsecurity to $MTWILSON_CONFIGURATION"
    exit 1
  fi
else  
  mkdir -p $MTWILSON_CONFIGURATION
  if [ $? -ne 0 ]; then
    echo_failure "Cannot create directory: $MTWILSON_CONFIGURATION"
    exit 1
  fi
  mkdir -p /etc/intel
  ln -s $MTWILSON_CONFIGURATION /etc/intel/cloudsecurity
  if [ $? -ne 0 ]; then
    echo_failure "Cannot link configuration from /etc/intel/cloudsecurity to $MTWILSON_CONFIGURATION"
    exit 1
  fi
fi

export MTWILSON_SERVICE_PROPERTY_FILES=/etc/intel/cloudsecurity
export MTWILSON_OPT_INTEL=/opt/intel
export MTWILSON_ETC_INTEL=/etc/intel
# If configuration is already in /etc/intel/cloudsecurity (upgrade or reinstall)
# then symlink /opt/mtwilson/configuration -> /etc/intel/cloudsecurity 

# If configuration is in /opt/mtwilson/configuration and there is no symlink
# in /etc/intel/cloudsecurity then we create one now
if [ -d "$MTWILSON_CONFIGURATION" ] && [ ! -L "$MTWILSON_CONFIGURATION" ] && [ "/etc/intel/clousecurity" != "$MTWILSON_CONFIGURATION" ] && [ ! -d /etc/intel/cloudsecurity ] && [ ! -L /etc/intel/cloudsecurity ]; then
  ln -s $MTWILSON_CONFIGURATION /etc/intel/cloudsecurity
fi

# Check for incorrect link and remove it 
if [ -L "$MTWILSON_CONFIGURATION/cloudsecurity" ]; then
  rm "$MTWILSON_CONFIGURATION/cloudsecurity"
fi

set_owner_for_mtwilson_directories() {
  for directory in $MTWILSON_HOME $MTWILSON_CONFIGURATION $MTWILSON_JAVA $MTWILSON_BIN $MTWILSON_ENV $MTWILSON_REPOSITORY $MTWILSON_LOGS $MTWILSON_SERVICE_PROPERTY_FILES $MTWILSON_OPT_INTEL $MTWILSON_ETC_INTEL; do
    chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME $directory
  done
}

# ensure application directories exist (chown will be repeated near end of this script, after setup)
for directory in $MTWILSON_HOME $MTWILSON_CONFIGURATION $MTWILSON_ENV $MTWILSON_REPOSITORY $MTWILSON_LOGS $MTWILSON_BIN $MTWILSON_JAVA $MTWILSON_SERVICE_PROPERTY_FILES $MTWILSON_OPT_INTEL $MTWILSON_ETC_INTEL; do
  # mkdir -p will return 0 if directory exists or is a symlink to an existing directory or directory and parents can be created
  mkdir -p $directory
  if [ $? -ne 0 ]; then
    echo_failure "Cannot create directory: $directory"
    exit 1
  fi
  chmod 700 $directory
done
set_owner_for_mtwilson_directories

#cp version script to configuration directory
cp version $MTWILSON_HOME/configuration/version

#cp mtwilson control script and setup symlinks
cp mtwilson.sh $MTWILSON_HOME/bin/mtwilson.sh
rm -f $MTWILSON_HOME/bin/mtwilson
ln -s $MTWILSON_HOME/bin/mtwilson.sh $MTWILSON_HOME/bin/mtwilson
chmod +x $MTWILSON_HOME/bin/*

#If user is root then create mtwilson symlink to /usr/local/bin otherwise export path '$MTWILSON_HOME/bin'
if [ "$(whoami)" == "root" ]; then
 if [ ! -d /usr/local/bin ]; then
   mkdir -p /usr/local/bin
 fi
 #Remove symbolic link if already exist
 rm -f /usr/local/bin/mtwilson
 ln -s $MTWILSON_HOME/bin/mtwilson /usr/local/bin/mtwilson
fi

# make aikverify directories, set ownership and permissions
if [ "$(whoami)" == "root" ]; then
  mkdir -p "/var/opt/intel"
fi
if [ -w "/var/opt/intel" ]; then
  mkdir -p "/var/opt/intel/aikverifyhome/bin" "/var/opt/intel/aikverifyhome/data"
  chown -R ${MTWILSON_USERNAME}:${MTWILSON_USERNAME} "/var/opt/intel"
  chmod 700 "/var/opt/intel" "/var/opt/intel/aikverifyhome/bin" "/var/opt/intel/aikverifyhome/data"
fi

mtwilson_backup_configuration() {
  if [ -n "$MTWILSON_CONFIGURATION" ] && [ -d "$MTWILSON_CONFIGURATION" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir="$MTWILSON_HOME/backup/mtwilson.configuration.$datestr"
    mkdir -p "$backupdir"
    cp -r $MTWILSON_CONFIGURATION $backupdir
  fi
}

mtwilson_backup_repository() {
  if [ -n "$MTWILSON_REPOSITORY" ] && [ -d "$MTWILSON_REPOSITORY" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir="$MTWILSON_HOME/backup/mtwilson.repository.$datestr"
    mkdir -p "$backupdir"
    cp -r $MTWILSON_REPOSITORY $backupdir
  fi
}

# backup current configuration and data, if they exist
mtwilson_backup_configuration
mtwilson_backup_repository

# store directory layout in env file
echo "# $(date)" > $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_HOME=$MTWILSON_HOME" >> $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_CONFIGURATION=$MTWILSON_CONFIGURATION" >> $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_JAVA=$MTWILSON_JAVA" >> $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_BIN=$MTWILSON_BIN" >> $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_REPOSITORY=$MTWILSON_REPOSITORY" >> $MTWILSON_ENV/mtwilson-layout
echo "export MTWILSON_LOGS=$MTWILSON_LOGS" >> $MTWILSON_ENV/mtwilson-layout

# store mtwilson username in env file
echo "# $(date)" > $MTWILSON_ENV/mtwilson-username
echo "export MTWILSON_USERNAME=$MTWILSON_USERNAME" >> $MTWILSON_ENV/mtwilson-username

# store log level in env file, if it's set
if [ -n "$MTWILSON_LOG_LEVEL" ]; then
  echo "# $(date)" > $MTWILSON_ENV/mtwilson-logging
  echo "export MTWILSON_LOG_LEVEL=$MTWILSON_LOG_LEVEL" >> $MTWILSON_ENV/mtwilson-logging
fi

# store the auto-exported environment variables in env file
# to make them available after the script uses sudo to switch users;
# we delete that file later
echo "# $(date)" > $MTWILSON_ENV/mtwilson-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "writing $env_file_var_name to mtwilson-setup with value: $env_file_var_value" >> $INSTALL_LOG_FILE
  echo "export $env_file_var_name=$env_file_var_value" >> $MTWILSON_ENV/mtwilson-setup
done

profile_dir=$HOME
if [ "$(whoami)" == "root" ] && [ -n "$MTWILSON_USERNAME" ] && [ "$MTWILSON_USERNAME" != "root" ]; then
  profile_dir=$MTWILSON_HOME
fi
profile_name=$profile_dir/$(basename $(getUserProfileFile))

echo "Updating profile: $profile_name" >> $INSTALL_LOG_FILE
appendToUserProfileFile "export PATH=$MTWILSON_BIN:\$PATH" $profile_name
appendToUserProfileFile "export MTWILSON_HOME=$MTWILSON_HOME" $profile_name


mtw_props_path="$MTWILSON_CONFIGURATION/mtwilson.properties"
as_props_path="$MTWILSON_CONFIGURATION/attestation-service.properties"
#pca_props_path="$MTWILSON_CONFIGURATION/PrivacyCA.properties"
ms_props_path="$MTWILSON_CONFIGURATION/management-service.properties"
mp_props_path="$MTWILSON_CONFIGURATION/mtwilson-portal.properties"
hp_props_path="$MTWILSON_CONFIGURATION/clientfiles/hisprovisioner.properties"
ta_props_path="$MTWILSON_CONFIGURATION/trustagent.properties"
file_paths=("$mtw_props_path" "$as_props_path" "$ms_props_path" "$mp_props_path" "$hp_props_path" "$ta_props_path")

mtwilson_password_file="$MTWILSON_CONFIGURATION/.mtwilson_password"
if [ -f "$mtwilson_password_file" ]; then
  export MTWILSON_PASSWORD=$(cat $mtwilson_password_file)
fi

## disable upgrade if properties files are encrypted from a previous installation
#for file in ${file_paths[*]}; do
#  echo "Checking for encrypted configuration file: $file" >> $INSTALL_LOG_FILE
#  if [ -f $file ]; then
#    if file_encrypted $file; then
#      echo_failure "Please decrypt property files before proceeding with mtwilson installation or upgrade."
#      exit -1
#    fi
#  fi
#done

echo "Loading configuration settings and defaults" >> $INSTALL_LOG_FILE
load_conf
load_defaults

# mtwilson requires java 1.7 or later
# detect or install java (jdk-1.7.0_51-linux-x64.tar.gz)
echo "Installing Java..."
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
# in 3.0, java home is now under trustagent home by default
JAVA_PACKAGE=`ls -1 jdk-* jre-* 2>/dev/null | tail -n 1`
# check if java is readable to the non-root user
#if [ -z "$JAVA_HOME" ]; then
#  java_detect >> $INSTALL_LOG_FILE
#fi
if [ -n "$JAVA_HOME" ]; then
  if [ $(whoami) == "root" ]; then
    JAVA_USER_READABLE=$(sudo -u $MTWILSON_USERNAME /bin/bash -c "if [ -r $JAVA_HOME ]; then echo 'yes'; fi")
  else
    JAVA_USER_READABLE=$(/bin/bash -c "if [ -r $JAVA_HOME ]; then echo 'yes'; fi")
  fi
fi
if [ -z "$JAVA_HOME" ] || [ -z "$JAVA_USER_READABLE" ]; then
  JAVA_HOME=$MTWILSON_HOME/share/jdk1.7.0_51
fi
echo "Installing Java ($JAVA_PACKAGE) into $JAVA_HOME..." >> $INSTALL_LOG_FILE
mkdir -p $JAVA_HOME
java_install_in_home $JAVA_PACKAGE
# store java location in env file
echo "# $(date)" > $MTWILSON_ENV/mtwilson-java
echo "export JAVA_HOME=$JAVA_HOME" >> $MTWILSON_ENV/mtwilson-java
echo "export JAVA_CMD=$JAVA_HOME/bin/java" >> $MTWILSON_ENV/mtwilson-java
echo "export JAVA_REQUIRED_VERSION=$JAVA_REQUIRED_VERSION" >> $MTWILSON_ENV/mtwilson-java

if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

# enable epel-release repository for rhel
flavor=$(getFlavour)
case $flavor in
  "rhel")
    addRepoRequired=$(yum list xmlstarlet 2>/dev/null | grep -E 'Available Packages|Installed Packages')
    repo_url="https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm"
    #if xmlstarlet package already available, break; no need to add repo
    if [ -z "$addRepoRequired" ]; then
      prompt_with_default ADD_EPEL_RELEASE_REPO "Add EPEL Release repository to local package manager? " "no"
      if [ "$ADD_EPEL_RELEASE_REPO" == "no" ]; then
        echo_failure "User declined to add EPEL Release repository to local package manager."
        exit -1
      fi
      add_package_repository "${repo_url}"
    fi
    ;;
esac
  
# install prerequisites
if [ "$(whoami)" == "root" ]; then
  MTWILSON_YUM_PACKAGES="zip unzip authbind openssl xmlstarlet wget net-tools"
  MTWILSON_APT_PACKAGES="zip unzip authbind openssl xmlstarlet"
  MTWILSON_YAST_PACKAGES="zip unzip authbind openssl xmlstarlet"
  MTWILSON_ZYPPER_PACKAGES="zip unzip authbind openssl xmlstarlet"
  auto_install "Installer requirements" "MTWILSON"
  if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi
else
  echo_warning "You must be root to install prerequisites through package manager"
fi

# setup authbind to allow non-root mtwilson to listen on ports 80 and 443
if [ ! -f /etc/authbind/byport/80 ] || [ ! -f /etc/authbind/byport/443 ]; then
  if [ "$(whoami)" == "root" ]; then
    if [ -n "$MTWILSON_USERNAME" ] && [ -d /etc/authbind/byport ]; then
      touch /etc/authbind/byport/80 /etc/authbind/byport/443
      chmod 500 /etc/authbind/byport/80 /etc/authbind/byport/443
      chown $MTWILSON_USERNAME /etc/authbind/byport/80 /etc/authbind/byport/443
    fi
  else
    echo_warning "You must be root to setup authbind configuration"
  fi
fi

# delete existing java files, to prevent a situation where the installer copies
# a newer file but the older file is also there
if [ -d $MTWILSON_JAVA ]; then
  rm $MTWILSON_JAVA/*.jar 2>/dev/null
fi

if [ -z "$INSTALL_PKGS" ]; then
  #opt_postgres|opt_mysql opt_java opt_tomcat|opt_glassfish opt_privacyca [opt_SERVICES| opt_attservice opt_mangservice opt_wlmservice] [opt_PORTALS | opt_mangportal opt_trustportal opt_wlmportal opt_mtwportal ] opt_monit
 INSTALL_PKGS="postgres tomcat privacyca SERVICES PORTALS"
fi

FIRST=0
#loop through INSTALL_PKG and set each entry to true
for i in $INSTALL_PKGS; do
  pkg=`echo $i | tr '[A-Z]' '[a-z]'`
  eval opt_$pkg="true"
  if [ $FIRST == 0 ]; then
    FIRST=1;
    LIST=$pkg
  else
    LIST=$LIST", "$pkg
  fi
done

# if a group is defined, then make all sub parts == true
if [ ! -z "$opt_portals" ]; then
  eval opt_mtwportal="true"
fi
# if a group is defined, then make all sub parts == true
if [ ! -z "$opt_services" ]; then
  eval opt_attservice="true"
  eval opt_mangservice="true"
  eval opt_wlmservice="true"
fi

# ensure we have some global settings available before we continue so the rest of the code doesn't have to provide a default
if [ -n "$opt_glassfish" ]; then
  WEBSERVICE_VENDOR=glassfish
elif [ -n "$opt_tomcat" ]; then
  WEBSERVICE_VENDOR=tomcat
fi
if [ -n "$opt_postgres" ]; then
  DATABASE_VENDOR=postgres
elif [ -n "$opt_mysql" ]; then
  DATABASE_VENDOR=mysql
fi

if using_glassfish; then
  export DEFAULT_API_PORT=$DEFAULT_GLASSFISH_API_PORT; 
elif using_tomcat; then
  export DEFAULT_API_PORT=$DEFAULT_TOMCAT_API_PORT;
fi

# if customer selected mysql but there is no connector present, we abort the install 
if using_mysql ; then
  mysqlconnector_file=`ls ~ -1 2>/dev/null | grep -i "^mysql-connector-java"`
  if [ -n "$mysqlconnector_file" ]; then
    mkdir -p /opt/mtwilson/java
    cp ~/$mysqlconnector_file /opt/mtwilson/java
  fi
  mysqlconnector_file=`ls -1 /opt/mtwilson/java/* 2>/dev/null | grep -i mysql`
  if [ -z "$mysqlconnector_file" ]; then
    echo_failure "Cannot find MySQL Connector/J"
    echo "Recommended steps:"
    echo "1. Download MySQL Connector/J, available free at www.mysql.com"
    echo "2. Copy the .jar from MySQL Connector/J to your home directory"
    echo "3. Run this installer again"
    exit 1
  fi
fi

export mysql_required_version=${MYSQL_REQUIRED_VERSION:-5.0}
export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-9.3}
export glassfish_required_version=${GLASSFISH_REQUIRED_VERSION:-4.0}
export tomcat_required_version=${TOMCAT_REQUIRED_VERSION:-7.0}

# extract mtwilson
echo "Extracting application..."
MTWILSON_ZIPFILE=`ls -1 mtwilson-server*.zip 2>/dev/null | tail -n 1`
unzip -oq $MTWILSON_ZIPFILE -d $MTWILSON_HOME >>$INSTALL_LOG_FILE 2>&1

# copy utilities script file to application folder
mkdir -p $MTWILSON_HOME/share/scripts

#this is now done in LinuxUtil setup.sh
cp functions "$MTWILSON_HOME/share/scripts/functions.sh"
rm -f "$MTWILSON_HOME/share/scripts/functions"
ln -s "$MTWILSON_HOME/share/scripts/functions.sh" "$MTWILSON_HOME/share/scripts/functions"

# deprecated:  remove when references have been updated to $MTWILSON_HOME/share/scripts/functions.sh
cp functions "$MTWILSON_BIN/functions.sh"

# set permissions
echo "chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME $MTWILSON_HOME" >> $INSTALL_LOG_FILE
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME $MTWILSON_HOME
chmod 755 $MTWILSON_BIN/*

# configure mtwilson TLS policies
echo "Configuring TLS policies..." >>$INSTALL_LOG_FILE
if [ -f "$MTWILSON_CONFIGURATION/mtwilson.properties" ]; then
  #default_mtwilson_tls_policy_id="$MTWILSON_DEFAULT_TLS_POLICY_ID"
  default_mtwilson_tls_policy_id="${MTWILSON_DEFAULT_TLS_POLICY_ID:-$MTW_DEFAULT_TLS_POLICY_ID}"   #`read_property_from_file "mtwilson.default.tls.policy.id" /etc/intel/cloudsecurity/mtwilson.properties`
  if [ "$default_mtwilson_tls_policy_id" == "INSECURE" ] || [ "$default_mtwilson_tls_policy_id" == "TRUST_FIRST_CERTIFICATE" ]; then
    echo_warning "Default TLS policy is insecure; the product guide contains information on enabling secure TLS policies"
  fi
  #fi
  export MTWILSON_TLS_KEYSTORE_PASSWORD="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"   #`read_property_from_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties`
else
  touch "$MTWILSON_CONFIGURATION/mtwilson.properties"
  chmod 600 "$MTWILSON_CONFIGURATION/mtwilson.properties"
  chown $MTWILSON_USERNAME:$MTWILSON_USRENAME "$MTWILSON_CONFIGURATION/mtwilson.properties"
  export MTWILSON_TLS_KEYSTORE_PASSWORD=`generate_password 32`
  echo '#mtwilson.default.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  "$MTWILSON_CONFIGURATION/mtwilson.properties"
  echo '#mtwilson.global.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  "$MTWILSON_CONFIGURATION/mtwilson.properties"
  # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
  # in a future release we will have a UI mechanism to manage this.
fi

#MTWILSON_TLS_POLICY_ALLOW
echo "Available TLS policies: certificate, certificate-digest, public-key, public-key-digest, TRUST_FIRST_CERTIFICATE, INSECURE"
prompt_with_default MTWILSON_TLS_POLICY_ALLOW "Mt Wilson Allowed TLS Policies: " "${MTWILSON_TLS_POLICY_ALLOW:-$MTW_TLS_POLICY_ALLOW}"
MTWILSON_TLS_POLICY_ALLOW=`echo $MTWILSON_TLS_POLICY_ALLOW | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTWILSON_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTWILSON_TLS_POLICY_ALLOW=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "certificate" ] || [ "$i" == "certificate-digest" ] || [ "$i" == "public-key" ] || [ "$i" == "public-key-digest" ] || [ "$i" == "TRUST_FIRST_CERTIFICATE" ] || [ "$i" == "INSECURE" ]; then
    TMP_MTWILSON_TLS_POLICY_ALLOW+="$i,"
  fi
done
MTWILSON_TLS_POLICY_ALLOW=`echo "$TMP_MTWILSON_TLS_POLICY_ALLOW" | sed 's/\(.*\),/\1/'`

if [ -n "$MTWILSON_TLS_POLICY_ALLOW" ]; then
  update_property_in_file "mtwilson.tls.policy.allow" "$MTWILSON_CONFIGURATION/mtwilson.properties" "$MTWILSON_TLS_POLICY_ALLOW"
else
  echo_failure "An allowed TLS policy must be defined."
  exit -1
fi

#MTWILSON_DEFAULT_TLS_POLICY_ID
prompt_with_default MTWILSON_DEFAULT_TLS_POLICY_ID "Mt Wilson Default TLS Policy ID: " "${MTWILSON_DEFAULT_TLS_POLICY_ID:-$MTW_DEFAULT_TLS_POLICY_ID}"
MTWILSON_DEFAULT_TLS_POLICY_ID=`echo $MTWILSON_DEFAULT_TLS_POLICY_ID | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTWILSON_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTWILSON_DEFAULT_TLS_POLICY_ID=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "$MTWILSON_DEFAULT_TLS_POLICY_ID" ]; then
    TMP_MTWILSON_DEFAULT_TLS_POLICY_ID="$i"
  fi
done
MTWILSON_DEFAULT_TLS_POLICY_ID=`echo "$TMP_MTWILSON_DEFAULT_TLS_POLICY_ID"`

if [[ "$MTWILSON_DEFAULT_TLS_POLICY_ID" == "INSECURE" || "$MTWILSON_DEFAULT_TLS_POLICY_ID" == "TRUST_FIRST_CERTIFICATE" ]]; then
  update_property_in_file "mtwilson.default.tls.policy.id" "$MTWILSON_CONFIGURATION/mtwilson.properties" "$MTWILSON_DEFAULT_TLS_POLICY_ID"
else
  echo_warning "Unable to determine default TLS policy."
#  exit -1
fi

export AUTO_UPDATE_ON_UNTRUST=${AUTO_UPDATE_ON_UNTRUST:-false}
update_property_in_file "mtwilson.as.autoUpdateHost" "$MTWILSON_CONFIGURATION/mtwilson.properties" "$AUTO_UPDATE_ON_UNTRUST"
update_property_in_file "mtwilson.locales" "$MTWILSON_CONFIGURATION/mtwilson.properties" "en-US"

#Save variables to properties file
if using_mysql; then   
  mysql_write_connection_properties "$MTWILSON_CONFIGURATION/mtwilson.properties" mtwilson.db
elif using_postgres; then
  postgres_write_connection_properties "$MTWILSON_CONFIGURATION/mtwilson.properties" mtwilson.db
fi

# default connection pool settings
update_property_in_file "dbcp.validation.query" "$MTWILSON_CONFIGURATION/mtwilson.properties" "select 1"
update_property_in_file "dbcp.validation.on.borrow" "$MTWILSON_CONFIGURATION/mtwilson.properties" "true"
update_property_in_file "dbcp.validation.on.return" "$MTWILSON_CONFIGURATION/mtwilson.properties" "false"


echo "Copying logback.xml to $MTWILSON_CONFIGURATION" >> $INSTALL_LOG_FILE
# copy default logging settings to /etc
chmod 600 logback.xml logback-stderr.xml
chown $MTWILSON_USERNAME:$MTWILSON_USERNAME logback.xml logback-stderr.xml
cp logback.xml logback-stderr.xml "$MTWILSON_CONFIGURATION"


# Gather default configuration
MTWILSON_SERVER_IP_ADDRESS=${MTWILSON_SERVER_IP_ADDRESS:-$(hostaddress)}

# Prompt for installation settings
echo "Configuring Mt Wilson Server Name..."
echo "Please enter the IP Address or Hostname that will identify the Mt Wilson server.
This address will be used in the server SSL certificate and in all Mt Wilson URLs.
For example, if you enter '$MTWILSON_SERVER_IP_ADDRESS' then the Mt Wilson URL is 
https://$MTWILSON_SERVER_IP_ADDRESS:8181 (for Glassfish deployments) or 
https://$MTWILSON_SERVER_IP_ADDRESS:8443 (for Tomcat deployments)
Detected the following options on this server:"
for h in $(hostaddress_list); do echo "+ $h"; done; echo "+ "`hostname`
prompt_with_default MTWILSON_SERVER "Mt Wilson Server:" $MTWILSON_SERVER_IP_ADDRESS
export MTWILSON_SERVER
echo

# copy shiro.ini api security file
if [ ! -f "$MTWILSON_CONFIGURATION/shiro.ini" ]; then
  echo "Copying shiro.ini to $MTWILSON_CONFIGURATION" >> $INSTALL_LOG_FILE
  chmod 600 shiro.ini shiro-localhost.ini
  chown $MTWILSON_USERNAME:$MTWILSON_USERNAME shiro.ini shiro-localhost.ini
  cp shiro.ini shiro-localhost.ini "$MTWILSON_CONFIGURATION"
fi

echo "Adding $MTWILSON_SERVER to shiro.ini..." >>$INSTALL_LOG_FILE
# add MTWILSON_SERVER to shiro trust file
# use "hostFilter.allow" when using the access-denying filter (any clients not from that list of ip's will be denied)
# use "iniHostRealm.allow" when using the access-allowing filter (any clients from that list of ip's will be allowed access but clients from other ip's can still try password or x509 authentication) - this is the current default
hostAllowPropertyName=iniHostRealm.allow
sed -i '/'"$hostAllowPropertyName"'/ s/^#//g' "$MTWILSON_CONFIGURATION/shiro.ini"
hostAllow=`read_property_from_file $hostAllowPropertyName "$MTWILSON_CONFIGURATION/shiro.ini"`
if [[ $hostAllow != *$MTWILSON_SERVER* ]]; then
  update_property_in_file "$hostAllowPropertyName" "$MTWILSON_CONFIGURATION/shiro.ini" "$hostAllow,$MTWILSON_SERVER";
fi
hostAllow=`read_property_from_file $hostAllowPropertyName "$MTWILSON_CONFIGURATION/shiro.ini"`
if [[ $hostAllow != *$MTWILSON_SERVER_IP_ADDRESS* ]]; then
  update_property_in_file "$hostAllowPropertyName" "$MTWILSON_CONFIGURATION/shiro.ini" "$hostAllow,$MTWILSON_SERVER_IP_ADDRESS";
fi
sed -i '/'"$hostAllowPropertyName"'/ s/^\([^#]\)/#\1/g' "$MTWILSON_CONFIGURATION/shiro.ini"

# This property is needed by the UpdateSslPort command to determine the port # that should be used in the shiro.ini file
 update_property_in_file "mtwilson.api.url" "$MTWILSON_CONFIGURATION/mtwilson.properties" "$MTWILSON_API_BASEURL"

find_installer() {
  local installer="${1}"
  binfile=`ls -1 $installer-*.bin 2>/dev/null | head -n 1`
  echo $binfile
}

monit_installer=`find_installer monit`
logrotate_installer=`find_installer logrotate`
management_service=`find_installer mtwilson-management-service` #ManagementService`
whitelist_service=`find_installer mtwilson-whitelist-service` #WLMService`
attestation_service=`find_installer mtwilson-attestation-service` #AttestationService`
mtw_portal=`find_installer mtwilson-portal-installer`
glassfish_installer=`find_installer glassfish`
tomcat_installer=`find_installer tomcat`

# Verify the installers we need are present before we start installing
if [ -n "$opt_glassfish" ] && [ ! -e "$glassfish_installer" ]; then
  echo_warning "Glassfish installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_tomcat" ] && [ ! -e "$tomcat_installer" ]; then
  echo_warning "Tomcat installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_attservice" ] && [ ! -e "$attestation_service" ]; then
  echo_warning "Attestation Service installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_mangservice" ] && [ ! -e "$management_service" ]; then
  echo_warning "Management Service installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_wlmservice" ] && [ ! -e "$whitelist_service" ]; then
  echo_warning "WhiteList Service installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_mtwportal" ] && [ ! -e "$mtw_portal" ]; then
  echo_warning "Mtw Combined Portal installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi
if [ -n "$opt_monit" ] && [ ! -e "$monit_installer" ]; then
  echo_warning "Monit installer marked for install but missing. Please verify you are using the right installer"
  exit -1;
fi

# Make sure the nodeploy flag is cleared, so service setup commands will deploy their .war files
export MTWILSON_SETUP_NODEPLOY=

if [[ -z "$opt_postgres" && -z "$opt_mysql" ]]; then
 echo_warning "Relying on an existing database installation"
fi

# before database root portion of executed code
if using_mysql; then
  mysql_userinput_connection_properties
  export MYSQL_HOSTNAME MYSQL_PORTNUM MYSQL_DATABASE MYSQL_USERNAME MYSQL_PASSWORD
elif using_postgres; then
  postgres_installed=1
  touch ${MTWILSON_HOME}/.pgpass
  chmod 0600 ${MTWILSON_HOME}/.pgpass
  chown ${MTWILSON_USERNAME}:${MTWILSON_USERNAME} ${MTWILSON_HOME}/.pgpass
  export POSTGRES_HOSTNAME POSTGRES_PORTNUM POSTGRES_DATABASE POSTGRES_USERNAME POSTGRES_PASSWORD
  if [ "$POSTGRES_HOSTNAME" == "127.0.0.1" ] || [ "$POSTGRES_HOSTNAME" == "localhost" ]; then
    PGPASS_HOSTNAME=localhost
  else
    PGPASS_HOSTNAME="$POSTGRES_HOSTNAME"
  fi
  echo "$POSTGRES_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" > ${MTWILSON_HOME}/.pgpass
  echo "$PGPASS_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" >> ${MTWILSON_HOME}/.pgpass
  if [ $(whoami) == "root" ]; then cp ${MTWILSON_HOME}/.pgpass ~/.pgpass; fi
fi

# database root portion of executed code
if [ "$(whoami)" == "root" ]; then
  if using_mysql; then
    # Install MySQL server (if user selected localhost)
    if [[ "$MYSQL_HOSTNAME" == "127.0.0.1" || "$MYSQL_HOSTNAME" == "localhost" || -n `echo "${hostaddress_list}" | grep "$MYSQL_HOSTNAME"` ]]; then
      if [ ! -z "$opt_mysql" ]; then
        # Place mysql server install code here
        echo "Installing mysql server..."
        aptget_detect; dpkg_detect;
        if [[ -n "$aptget" ]]; then
          echo "mysql-server-5.1 mysql-server/root_password password $DATABASE_ROOT_PASSWORD" | debconf-set-selections
          echo "mysql-server-5.1 mysql-server/root_password_again password $DATABASE_ROOT_PASSWORD" | debconf-set-selections
        fi
        mysql_server_install
		mysql_start >> $INSTALL_LOG_FILE
        echo "Installation of mysql server complete..."
        # End mysql server install code here
      else
        echo_warning "Using existing mysql install"
      fi
      # mysql client install here
      echo "Installing mysql client..."
      mysql_install  
      echo "Installation of mysql client complete"
      # mysql client end

    fi
  elif using_postgres; then
    # Copy the www.postgresql.org PGP public key so add_postgresql_install_packages can add it later if needed
    if [ -d "/etc/apt" ]; then
      mkdir -p /etc/apt/trusted.gpg.d
      chmod 755 /etc/apt/trusted.gpg.d
      cp ACCC4CF8.asc "/etc/apt/trusted.gpg.d"
    fi
    POSTGRES_SERVER_APT_PACKAGES="postgresql-9.3"
    POSTGRES_SERVER_YUM_PACKAGES="postgresql93"
    add_postgresql_install_packages "POSTGRES_SERVER"
    if [ $? -ne 0 ]; then echo_failure "Failed to add postgresql repository to local package manager"; exit -1; fi
    
    postgres_userinput_connection_properties
    if [ -n "$opt_postgres" ]; then
      # Install Postgres server (if user selected localhost)
      if [[ "$POSTGRES_HOSTNAME" == "127.0.0.1" || "$POSTGRES_HOSTNAME" == "localhost" || -n `echo "$(hostaddress_list)" | grep "$POSTGRES_HOSTNAME"` ]]; then
        echo "Installing postgres server..."
        # when we install postgres server on ubuntu it prompts us for root pw
        # we preset it so we can send all output to the log
        aptget_detect; dpkg_detect; yum_detect;
        if [[ -n "$aptget" ]]; then
          echo "postgresql app-pass password $POSTGRES_PASSWORD" | debconf-set-selections 
        fi
        postgres_installed=0 #postgres is being installed
        # don't need to restart postgres server unless the install script says we need to (by returning zero)
        postgres_server_install
        if [ $? -ne 0 ]; then echo_failure "Failed to install postgresql server"; exit -1; fi
        postgres_restart >> $INSTALL_LOG_FILE
        #sleep 10
        # postgres server end
      fi 
      # postgres client install here
      echo "Installing postgres client..."
      postgres_install
      if [ $? -ne 0 ]; then echo_failure "Failed to install postgresql"; exit -1; fi
      # do not need to restart postgres server after installing the client.
      #postgres_restart >> $INSTALL_LOG_FILE
      #sleep 10
      echo "Installation of postgres client complete" 
      # postgres client install end
    else
      echo_warning "Relying on an existing Postgres installation"
    fi
  fi
fi

# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_root_connection() {
  mysql_require
  mysql_root_connect="$mysql --batch --host=${MYSQL_HOSTNAME:-$DEFAULT_MYSQL_HOSTNAME} --port=${MYSQL_PORTNUM:-$DEFAULT_MYSQL_PORTNUM} --user=${MYSQL_ROOT_USERNAME:-$DEFAULT_MYSQL_ROOT_USERNAME} --password=${MYSQL_ROOT_PASSWORD:-$DEFAULT_MYSQL_ROOT_PASSWORD}"
}

# Environment:
# - MYSQL_REQUIRED_VERSION
# sets the is_mysql_available variable to "yes" or ""
# sets the is_MYSQL_DATABASE_created variable to "yes" or ""
mysql_test_root_connection() {
  mysql_root_connection
  is_mysql_available=""
  local mysql_test_result=`$mysql_root_connect -e "show databases" 2>/tmp/intel.mysql.err | grep "^${MYSQL_DATABASE}\$" | wc -l`
  if [ $mysql_test_result -gt 0 ]; then
    is_mysql_available="yes"
  fi
  mysql_connection_error=`cat /tmp/intel.mysql.err`
  rm -f /tmp/intel.mysql.err
}

# requires a mysql connection that can access the existing database, OR (if it doesn't exist)
# requires a mysql connection that can create databases and grant privileges
# call mysql_configure_connection before calling this function
mysql_create_database_and_user() {

  #we first need to find if the user has specified a different port than the once currently configured for mysql
  # find the my.conf location
  mysql_cnf=`find / -name my.cnf 2>/dev/null | head -n 1`
  #echo "MySQL configuration file is located at $mysql_cnf"
  # check the current port that is configured. There should be 2 instances, one for server and one for client. Both of them should be updated
  if [ -f "$mysql_cnf" ]; then
    current_port=`grep -E "port\s+=" $mysql_cnf | head -1 | awk '{print $3}'`
    #echo "MySQL is currently configured with port $current_port"
    # if the required port is already configured. If not, we need to reconfigure
    has_correct_port=`grep $MYSQL_PORTNUM $mysql_cnf | head -1`
    if [ -z "$has_correct_port" ]; then
      echo "Port needs to be reconfigured from $current_port to $MYSQL_PORTNUM"
      sed -i s/$current_port/$MYSQL_PORTNUM/g $mysql_cnf 
      echo "Restarting MySQL for port change update to take effect."
      service mysql restart >> $INSTALL_LOG_FILE
    fi
  else
    echo "warning: my.cnf not found" >> $INSTALL_LOG_FILE
  fi
	
  mysql_test_root_connection
  local create_db="CREATE DATABASE \`${MYSQL_DATABASE}\`;"
  local find_user="SELECT user FROM mysql.user WHERE user='${MYSQL_USERNAME}';"
  local create_user="CREATE USER \`$MYSQL_USERNAME\`@\`$MYSQL_HOSTNAME\` identified by '$MYSQL_PASSWORD';"
  local grant_db="GRANT ALL ON \`${MYSQL_DATABASE}\`.* TO \`${MYSQL_USERNAME}\`@\`$MYSQL_HOSTNAME\` IDENTIFIED BY '${MYSQL_PASSWORD}';"
  if [ -z "$mysql_connection_error" ]; then
    if [ -n "$is_mysql_available" ]; then
      echo_success "Database \`${MYSQL_DATABASE}\` already exists"   >> $INSTALL_LOG_FILE
      return 0
    else
      echo "Creating database..."    >> $INSTALL_LOG_FILE
      $mysql_root_connect -e "${create_db}"
	  user=`$mysql_root_connect -e "${find_user}"`
      if [[ -n "$user" && ${user}=${MYSQL_USERNAME} ]]; then
	    echo "Mysql user '$MYSQL_USERNAME' already exist"
	  else
	    echo "Creating new Mysql user $MYSQL_USERNAME..."
	    $mysql_root_connect -e "${create_user}"
	  fi
	  $mysql_root_connect -e "${grant_db}"
      mysql_test_root_connection
      if [ -z "$is_mysql_available" ]; then
        echo_failure "Failed to create database."  | tee -a $INSTALL_LOG_FILE
        return 1
      fi
    fi
  else
    echo_failure "Cannot connect to database."  | tee -a $INSTALL_LOG_FILE
    echo "Try to execute the following commands on the database:"  >> $INSTALL_LOG_FILE
    echo "${create_sql}" >> $INSTALL_LOG_FILE
	echo "${create_user}" >> $INSTALL_LOG_FILE
    echo "${grant_sql}"  >> $INSTALL_LOG_FILE
    return 1
  fi
}

# after database root portion of executed code
if using_mysql; then
  if [ -z "$SKIP_DATABASE_INIT" ]; then
    # mysql db init here
    if ! mysql_create_database_and_user; then
      mysql_install_db --user=mysql --basedir=/usr --datadir=/var/lib/mysql --defaults-file=/etc/mysql/my.cnf
      mysqladmin -u "root" password "$MYSQL_ROOT_PASSWORD"
      mysql_create_database_and_user
    fi
    # mysql db init end
  else
    echo_warning "Skipping init of database" 
  fi
  export is_mysql_available mysql_connection_error
  if [ -z "$is_mysql_available" ]; then echo_warning "Run 'mtwilson setup' after a database is available"; fi
elif using_postgres; then
  if [ -z "$SKIP_DATABASE_INIT" ]; then
    # postgres db init here
    postgres_create_database
    if [ $? -ne 0 ]; then
      echo_failure "Cannot create database"
      exit 1
    fi
    #postgres_restart >> $INSTALL_LOG_FILE
    #sleep 10
    #export is_postgres_available postgres_connection_error
    if [ -z "$is_postgres_available" ]; then
      echo_warning "Run 'mtwilson setup' after a database is available"; 
    fi
    # postgress db init end
  else
    echo_warning "Skipping init of database"
  fi
  if [ $postgres_installed -eq 0 ]; then
    postgres_server_detect
    has_local_peer=`grep "^local.*all.*all.*peer" $postgres_pghb_conf`
    if [ -n "$has_local_peer" ]; then
      echo "Replacing PostgreSQL local 'peer' authentication with 'password' authentication..."
      sed -i 's/^local.*all.*all.*peer/local all all password/' $postgres_pghb_conf
    fi
    #if [ "$POSTGRESQL_KEEP_PGPASS" != "true" ]; then   # Use this line after 2.0 GA, and verify compatibility with other commands
    if [ "${POSTGRESQL_KEEP_PGPASS:-true}" == "false" ]; then
      if [ -f ${MTWILSON_HOME}/.pgpass ]; then
        echo "Removing .pgpass file to prevent insecure database password storage in plaintext..."
        rm -f ${MTWILSON_HOME}/.pgpass
        if [ $(whoami) == "root" ]; then rm -f ~/.pgpass; fi
      fi
    fi
  fi
fi


# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER

chmod +x *.bin

mkdir -p /opt/mtwilson/logs
touch /opt/mtwilson/logs/mtwilson.log
chown mtwilson:mtwilson /opt/mtwilson/logs/mtwilson.log
touch /opt/mtwilson/logs/mtwilson-audit.log
chown mtwilson:mtwilson /opt/mtwilson/logs/mtwilson-audit.log

# use of "mtwilson config" method will be required when mtwilson setup is 
# revised to use the "mtwilson" command itself for java setup tasks and
# when the "mtwilson" command automatically switches to the "mtwilson" user
# because then it won't have access to the environment variables.
## mtwilson config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-'mtwilson'}" >/dev/null
export MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS=${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-'mtwilson'}
call_tag_setupcommand setup-manager update-extensions-cache-file --force 2> /dev/null

if [[ -z "$opt_glassfish" && -z "$opt_tomcat" ]]; then
 echo_warning "Relying on an existing webservice installation"
fi
if using_glassfish; then
  if [ ! -z "$opt_glassfish" ] && [ -n "$glassfish_installer" ]; then
    if [ ! glassfish_detect >/dev/null ]; then
      portInUse=`netstat -lnput | grep -E "8080|8181"`
      if [ -n "$portInUse" ]; then 
        #glassfish ports in use. exit install
        echo_failure "Glassfish ports in use. Aborting install."
        exit 1
      fi
    fi
    
    echo "Installing Glassfish..." | tee -a  $INSTALL_LOG_FILE
    # glassfish install here
    ./$glassfish_installer
    if [ $? -ne 0 ]; then
      echo_failure "Glassfish installation failed"
      exit 1
    fi
    glassfish_create_ssl_cert_prompt
    echo "Glassfish installation complete..." | tee -a  $INSTALL_LOG_FILE
    # end glassfish installer
  else
    echo_warning "Relying on an existing glassfish installation" 
  fi

  glassfish_detect

  echo "GLASSFISH_HOME=$GLASSFISH_HOME" > $MTWILSON_ENV/glassfish
  echo "glassfish=\"$glassfish\"" >> $MTWILSON_ENV/glassfish
  echo "glassfish_bin=$glassfish_bin" >> $MTWILSON_ENV/glassfish

  
  if [ -e $glassfish_bin ]; then
    echo "Disabling glassfish log rotation in place of system wide log rotation"
      #$glassfish set-log-attributes --target server com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0   ### THIS COMMAND DOES NOT WORK
      gf_logging_properties=$(find "$GLASSFISH_HOME" -name logging.properties | head -1)
      sed -i "s/com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=.*/com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0/g" "$gf_logging_properties"
  else
    echo_warning "Unable to locate asadmin, please run the following command on your system to disable glassfish log rotation: "
    echo_warning "asadmin set-log-attributes --target server com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0"
  fi
elif using_tomcat; then
  if [ ! -z "$opt_tomcat" ] && [ -n "$tomcat_installer" ]; then
    ./$tomcat_installer
    if [ $? -ne 0 ]; then
      echo_failure "Tomcat installation failed"
      exit 1
    fi
    tomcat_create_ssl_cert_prompt
  else
    echo_warning "Relying on an existing Tomcat installation"
  fi
  tomcat_detect

  #set jersey logging filter level to WARNING, instead of default value of INFO
  echo "org.glassfish.jersey.filter.LoggingFilter.level = WARNING" >> $TOMCAT_CONF/logging.properties

  echo "TOMCAT_HOME=$TOMCAT_HOME" > $MTWILSON_ENV/tomcat
  echo "TOMCAT_CONF=$TOMCAT_CONF" >> $MTWILSON_ENV/tomcat
  echo "tomcat=\"$tomcat\"" >> $MTWILSON_ENV/tomcat
  echo "tomcat_bin=$tomcat_bin" >> $MTWILSON_ENV/tomcat
fi

set_owner_for_mtwilson_directories

if [[ -n "opt_attservice"  && -f "$attestation_service" ]]; then
  echo "Installing mtwilson service..." | tee -a  $INSTALL_LOG_FILE
  ./$attestation_service
  if [ $? -ne 0 ]; then echo_failure "Failed to install attestation service"; exit -1; fi
  echo "mtwilson service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_mangservice" && -f "$management_service"  ]]; then
  echo "Installing Management Service..." | tee -a  $INSTALL_LOG_FILE
  ./$management_service
  if [ $? -ne 0 ]; then echo_failure "Failed to install management service"; exit -1; fi
  echo "Management Service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_wlmservice" && -f "$whitelist_service" ]]; then
  echo "Installing Whitelist Service..." | tee -a  $INSTALL_LOG_FILE
  ./$whitelist_service >> $INSTALL_LOG_FILE
  if [ $? -ne 0 ]; then echo_failure "Failed to install whitelist service"; exit -1; fi
  echo "Whitelist Service installed" | tee -a  $INSTALL_LOG_FILE
fi

if [[ -n "$opt_mtwportal" && "$mtw_portal" ]]; then
  echo "Installing Mtw Combined Portal..." | tee -a  $INSTALL_LOG_FILE
  ./$mtw_portal
  if [ $? -ne 0 ]; then echo_failure "Failed to install portal"; exit -1; fi
  echo "Mtw Combined Portal installed" | tee -a  $INSTALL_LOG_FILE
fi

##############################################################################################################################################################################
##tag service installation
CONFIG_DIR=/opt/mtwilson/configuration
prompt_with_default MTWILSON_TAG_SERVER_PRIVATE "Mt Wilson Tag Private Server: " $MTWILSON_SERVER
WEBSERVER_PREFIX=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $1}'`
WEBSERVER_PORT=`echo $MTWILSON_API_BASEURL | awk -F/ '{print $3}' | awk -F: '{print $2}'`
MTWILSON_TAG_URL="$WEBSERVER_PREFIX//$MTWILSON_TAG_SERVER_PRIVATE:$WEBSERVER_PORT/mtwilson/v2"
MTWILSON_API_TAG_URL="$WEBSERVER_PREFIX//$MTWILSON_TAG_SERVER_PRIVATE:$WEBSERVER_PORT/mtwilson/v1/AttestationService/resources/assetTagCert"
update_property_in_file "mtwilson.atag.url" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_URL"
update_property_in_file "mtwilson.atag.mtwilson.baseurl" $CONFIG_DIR/mtwilson.properties "$MTWILSON_API_TAG_URL"

prompt_with_default MTWILSON_TAG_ADMIN_USERNAME "Mt Wilson Asset Tag Admin User: " ${MTWILSON_TAG_ADMIN_USERNAME:-tagadmin}
prompt_with_default_password MTWILSON_TAG_ADMIN_PASSWORD "Mt Wilson Asset Tag Admin Password: " $MTWILSON_TAG_ADMIN_PASSWORD

MTWILSON_TAG_API_USERNAME=${MTWILSON_TAG_API_USERNAME:-"tagservice"}
MTWILSON_TAG_API_PASSWORD=${MTWILSON_TAG_API_PASSWORD:-$(generate_password 16)}
update_property_in_file "mtwilson.tag.api.username" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_API_USERNAME"
update_property_in_file "mtwilson.tag.api.password" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_API_PASSWORD"

if [ ! -z "$opt_portals" ]; then
  DEFAULT_MTWILSON_TAG_HTML5_DIR=`find /opt/mtwilson -name tag`
  prompt_with_default MTWILSON_TAG_HTML5_DIR "Mt Wilson Tag HTML5 Path: " ${MTWILSON_TAG_HTML5_DIR:-$DEFAULT_MTWILSON_TAG_HTML5_DIR}
  echo "MTWILSON_TAG_HTML5_DIR: $MTWILSON_TAG_HTML5_DIR" >> "$INSTALL_LOG_FILE"
  echo "DEFAULT_MTWILSON_TAG_HTML5_DIR: $DEFAULT_MTWILSON_TAG_HTML5_DIR" >> "$INSTALL_LOG_FILE"
  if ! validate_path_executable "$MTWILSON_TAG_HTML5_DIR"; then exit -1; fi
fi

prompt_with_default MTWILSON_TAG_CERT_IMPORT_AUTO "Mt Wilson Tag Certificate Auto Import: " ${MTWILSON_TAG_CERT_IMPORT_AUTO:-true}
update_property_in_file "mtwilson.atag.html5.dir" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_HTML5_DIR"
update_property_in_file "tag.provision.autoimport" $CONFIG_DIR/mtwilson.properties "$MTWILSON_TAG_CERT_IMPORT_AUTO"

# remaining properties
prompt_with_default TAG_PROVISION_EXTERNAL "Use external CA instead of the built-in CA? " ${TAG_PROVISION_EXTERNAL:-false}
prompt_with_default TAG_PROVISION_XML_ENCRYPTION_REQUIRED "XML encryption required? " ${TAG_PROVISION_XML_ENCRYPTION_REQUIRED:-false}
prompt_with_default_password TAG_PROVISION_XML_PASSWORD "XML encryption password: " ${TAG_PROVISION_XML_PASSWORD:-$(generate_password 16)}
#prompt_with_default TAG_PROVISION_SELECTION_DEFAULT "Default tag provisioning selection: " ${TAG_PROVISION_SELECTION_DEFAULT:-default}
prompt_with_default TAG_VALIDITY_SECONDS "Tag certificate validity duration: " ${TAG_VALIDITY_SECONDS:-31536000}
prompt_with_default TAG_ISSUER_DN "Tag issuer distinguished name: " ${TAG_ISSUER_DN:-"CN=mtwilson-tag-ca"}
update_property_in_file "tag.provision.external" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_EXTERNAL"
update_property_in_file "tag.provision.xml.encryption.required" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_XML_ENCRYPTION_REQUIRED"
update_property_in_file "tag.provision.xml.encryption.password" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_XML_PASSWORD"
#update_property_in_file "tag.provision.selection.default" $CONFIG_DIR/mtwilson.properties "$TAG_PROVISION_SELECTION_DEFAULT"
update_property_in_file "tag.validity.seconds" $CONFIG_DIR/mtwilson.properties "$TAG_VALIDITY_SECONDS"
update_property_in_file "tag.issuer.dn" $CONFIG_DIR/mtwilson.properties "$TAG_ISSUER_DN"

#call_setupcommand create-database
#call_tag_setupcommand setup-manager update-extensions-cache-file --force 2> /dev/null
call_tag_setupcommand setup-manager initialize-db --force

call_tag_setupcommand tag-init-database
call_tag_setupcommand tag-create-ca-key "CN=assetTagService"
call_tag_setupcommand tag-export-file cacerts | grep -v ":" >> $CONFIG_DIR/tag-cacerts.pem
call_tag_setupcommand tag-create-mtwilson-client --url="$MTWILSON_TAG_URL" --username="$MTWILSON_TAG_API_USERNAME" --password="$MTWILSON_TAG_API_PASSWORD"
if [ -n "$MTWILSON_TAG_ADMIN_PASSWORD" ]; then
  export MTWILSON_TAG_ADMIN_PASSWORD
  call_tag_setupcommand login-password ${MTWILSON_TAG_ADMIN_USERNAME:-tagadmin} env:MTWILSON_TAG_ADMIN_PASSWORD --permissions tag_certificate_requests:* tag_certificates:* tag_kv_attributes:* tag_selection_kv_attributes:* tag_selections:* tpm_passwords:retrieve hosts:search host_attestations:*
else
  echo_warning "Skipping creation of tag admin user because MTWILSON_TAG_ADMIN_PASSWORD is not set"
fi

#for tag encryption
mkdir -p /opt/mtwilson/features/tag/var
mkdir -p /opt/mtwilson/features/tag/bin
cp encrypt.sh /opt/mtwilson/features/tag/bin
cp decrypt.sh /opt/mtwilson/features/tag/bin
chmod 755 /opt/mtwilson/features/tag/bin/encrypt.sh
chmod 755 /opt/mtwilson/features/tag/bin/decrypt.sh

##############################################################################################################################################################################


if [ ! -z "$opt_logrotate" ]; then
  echo "Installing Log Rotate..." | tee -a  $INSTALL_LOG_FILE
  ./$logrotate_installer
  if [ $? -ne 0 ]; then echo_failure "Failed to install log rotation"; exit -1; fi
  #echo "Log Rotate installed" | tee -a  $INSTALL_LOG_FILE
fi

mkdir -p /etc/logrotate.d

if [ ! -a /etc/logrotate.d/mtwilson ]; then
 echo "/opt/mtwilson/glassfish4/glassfish/domains/domain1/logs/server.log {
    missingok
    notifempty
    rotate $LOG_OLD
    size $LOG_SIZE
    $LOG_ROTATION_PERIOD
    $LOG_COMPRESS
    $LOG_DELAYCOMPRESS
    $LOG_COPYTRUNCATE
}

/opt/mtwilson/share/apache-tomcat-7.0.34/logs/catalina.out {
    missingok
	notifempty
	rotate $LOG_OLD
	size $LOG_SIZE
	$LOG_ROTATION_PERIOD
	$LOG_COMPRESS
	$LOG_DELAYCOMPRESS
	$LOG_COPYTRUNCATE
}" > /etc/logrotate.d/mtwilson
fi

if [ ! -z "$opt_monit" ] && [ -n "$monit_installer" ]; then
  echo "Installing Monit..." | tee -a  $INSTALL_LOG_FILE
  ./$monit_installer  #>> $INSTALL_LOG_FILE
  if [ $? -ne 0 ]; then echo_failure "Failed to install monit"; exit -1; fi
  #echo "Monit installed" | tee -a  $INSTALL_LOG_FILE
fi

mkdir -p /opt/mtwilson/monit/conf.d

# create the monit rc files

#glassfish.mtwilson
if [ -z "$NO_GLASSFISH_MONIT" ]; then 
  if [ ! -a /opt/mtwilson/monit/conf.d/glassfish.mtwilson ]; then
    echo "# Verify glassfish is installed (change path if Glassfish is installed to a different directory)
      check file gf_installed with path "/opt/mtwilson/glassfish4/bin/asadmin"
      group gf_server
      if does not exist then unmonitor

      # MtWilson Glassfish services
      check host mtwilson-version-glassfish with address 127.0.0.1
      group gf_server
      start program = \"/opt/mtwilson/bin/mtwilson start\" with timeout 120 seconds
      stop program = \"/opt/mtwilson/bin/mtwilson stop\" with timeout 120 seconds
      if failed port 8181 TYPE TCPSSL PROTOCOL HTTP
        and request "/mtwilson/v2/version" for 2 cycles
      then restart
      if 3 restarts within 10 cycles then timeout
      depends on gf_installed" > /opt/mtwilson/monit/conf.d/glassfish.mtwilson
  fi
fi

#tomcat.mtwilson
if [ -z "$NO_TOMCAT_MONIT" ]; then 
  if [ ! -a /opt/mtwilson/monit/conf.d/tomcat.mtwilson ]; then
    echo "# Verify tomcat is installed (change path if Tomcat is installed to a different directory)
      check file tc_installed with path \"/opt/mtwilson/share/apache-tomcat-7.0.34/bin/catalina.sh\"
      group tc_server
      if does not exist then unmonitor
    
      # MtWilson Tomcat services
      check host mtwilson-version-tomcat with address 127.0.0.1
      group tc_server
      start program = \"/opt/mtwilson/bin/mtwilson start\" with timeout 120 seconds
      stop program = \"/opt/mtwilson/bin/mtwilson stop\" with timeout 120 seconds
      if failed port 8443 TYPE TCPSSL PROTOCOL HTTP
        and request "/mtwilson/v2/version" for 2 cycles
      then restart
      if 3 restarts within 10 cycles then timeout
      depends on tc_installed" > /opt/mtwilson/monit/conf.d/tomcat.mtwilson
  fi
fi

if [ -z "$NO_POSTGRES_MONIT" ]; then 
  if [ ! -a /opt/mtwilson/monit/conf.d/postgres.mtwilson ]; then 
    echo "check process postgres matching \"postgresql\"
      group pg-db
      start program = \"/usr/sbin/service postgresql start\"
      stop program = \"/usr/sbin/service postgresql stop\"
      if failed unixsocket /var/run/postgresql/.s.PGSQL.${POSTGRES_PORTNUM:-5432} protocol pgsql 
      then restart
      if failed host 127.0.0.1 port ${POSTGRES_PORTNUM:-5432} protocol pgsql then restart
      if 5 restarts within 5 cycles then timeout
      depends on pg_bin
  
      check file pg_bin with path \"/usr/bin/psql\"
      group pg-db
      if does not exist then unmonitor" > /opt/mtwilson/monit/conf.d/postgres.mtwilson
  fi
fi

if [ -z "$NO_MYSQL_MONIT" ]; then 
  if [ ! -a /opt/mtwilson/monit/conf.d/mysql.mtwilson ]; then 
    echo "check process mysql matching \"mysql\"
      group mysql_db
      start program = \"/usr/sbin/service mysql start\"
      stop program = \"/usr/sbin/service mysql stop\"
      if failed host 127.0.0.1 port ${MYSQL_PORTNUM:-3306} protocol mysql then restart
      if 5 restarts within 5 cycles then timeout
      depends on mysql_bin
      depends on mysql_rc

      check file mysql_bin with path /usr/sbin/mysqld
      group mysql_db
      if does not exist then unmonitor

      check file mysql_rc with path /etc/init.d/mysql
      group mysql_db
      if does not exist then unmonitor" > /opt/mtwilson/monit/conf.d/mysql.mtwilson
  fi
fi

echo  -n "Restarting monit service so new configs take effect... "
service monit restart > /dev/null 2>&1
echo "Done"

set_owner_for_mtwilson_directories

# setup mtwilson, unless the NOSETUP variable is defined
if [ -z "$MTWILSON_NOSETUP" ]; then
  # the master password is required
  # if already user provided we assume user will also provide later for restarts
  # otherwise, we generate and store the password
  if [ -z "$MTWILSON_PASSWORD" ] && [ ! -f $MTWILSON_CONFIGURATION/.mtwilson_password ]; then
    touch $MTWILSON_CONFIGURATION/.mtwilson_password
    chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $MTWILSON_CONFIGURATION/.mtwilson_password
    mtwilson generate-password > $MTWILSON_CONFIGURATION/.mtwilson_password
  fi

  if [ "${LOCALHOST_INTEGRATION}" == "yes" ]; then
    mtwilson localhost-integration 127.0.0.1 "$MTWILSON_SERVER_IP_ADDRESS"
  fi

  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/mtwilson.properties" --out="${MTWILSON_CONFIGURATION}/mtwilson.properties" 2>/dev/null
  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/attestation-service.properties" --out="${MTWILSON_CONFIGURATION}/attestation-service.properties" 2>/dev/null
  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/management-service.properties" --out="${MTWILSON_CONFIGURATION}/management-service.properties" 2>/dev/null
  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/audit-handler.properties" --out="${MTWILSON_CONFIGURATION}/audit-handler.properties" 2>/dev/null
  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/mtwilson-portal.properties" --out="${MTWILSON_CONFIGURATION}/mtwilson-portal.properties" 2>/dev/null
  mtwilson import-config --in="${MTWILSON_CONFIGURATION}/wlm-service.properties" --out="${MTWILSON_CONFIGURATION}/wlm-service.properties" 2>/dev/null

  #mtwilson config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,jersey-media-multipart}" >/dev/null
  #mtwilson config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null

  ## dashboard
  #mtwilson config mtwilson.navbar.buttons kms-keys,mtwilson-configuration-settings-ws-v2,mtwilson-core-html5 >/dev/null
  #mtwilson config mtwilson.navbar.hometab keys >/dev/null

  #mtwilson config jetty.port ${JETTY_PORT:-80} >/dev/null
  #mtwilson config jetty.secure.port ${JETTY_SECURE_PORT:-443} >/dev/null

  #mtwilson setup
fi

# store server hostname or ip address (whatever user configured) for server
# to use when constructing self-references
mtwilson config mtwilson.host "$MTWILSON_SERVER"

# delete the temporary setup environment variables file
rm -f $MTWILSON_ENV/mtwilson-setup

## start the server, unless the NOSETUP variable is defined
#if [ -z "$MTWILSON_NOSETUP" ]; then mtwilson start; fi

#Register mtwilson as a startup script
#if [ ! $(/sbin/initctl list | grep mtwilson) ]; then
  if [ "$(whoami)" == "root" ]; then
    register_startup_script /usr/local/bin/mtwilson mtwilson
  else
    echo_warning "You must be root to register mtwilson startup script"
  fi
#fi

if [ "$(whoami)" == "root" ]; then     
  #remove previous service startup scripts if they exist
  remove_startup_script "asctl" >>$INSTALL_LOG_FILE 2>&1
  remove_startup_script "msctl" >>$INSTALL_LOG_FILE 2>&1
  remove_startup_script "mtwilson-portal" >>$INSTALL_LOG_FILE 2>&1
  remove_startup_script "tdctl" >>$INSTALL_LOG_FILE 2>&1
  remove_startup_script "wlmctl" >>$INSTALL_LOG_FILE 2>&1
fi

# last chance to set permissions
chmod 600 "$MTWILSON_CONFIGURATION/*.properties" 2>/dev/null
chmod 700 "/var/opt/intel" "/var/opt/intel/aikverifyhome/bin" "/var/opt/intel/aikverifyhome/data"

echo "Restarting webservice for all changes to take effect"
#Restart webserver
if using_glassfish; then
  mtwilson config "mtwilson.webserver.vendor" "glassfish" >/dev/null
  mtwilson config "glassfish.admin.username" "$WEBSERVICE_MANAGER_USERNAME" >/dev/null
  mtwilson config "glassfish.admin.password" "$WEBSERVICE_MANAGER_PASSWORD" >/dev/null
  glassfish_admin_user
  #glassfish_restart
  /opt/mtwilson/bin/mtwilson restart
elif using_tomcat; then
  mtwilson config "mtwilson.webserver.vendor" "tomcat" >/dev/null
  mtwilson config "tomcat.admin.username" "$WEBSERVICE_MANAGER_USERNAME" >/dev/null
  mtwilson config "tomcat.admin.password" "$WEBSERVICE_MANAGER_PASSWORD" >/dev/null
  #tomcat_restart
  /opt/mtwilson/bin/mtwilson restart
fi

echo "Log file for install is located at $INSTALL_LOG_FILE"
if [ -n "$INSTALLED_MARKER_FILE" ]; then
 touch $INSTALLED_MARKER_FILE
fi
if [ "$(whoami)" != "root" ]; then 
  echo_warning "Please relogin to use mtwilson utilities"
fi
