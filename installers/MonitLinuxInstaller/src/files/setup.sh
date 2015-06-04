#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

monit_required_version=5.5

# detect the packages we have to install
MONIT_PACKAGE=`ls -1 monit-*.tar.gz 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

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
  if [ "$(whoami)" == "root" ]; then
    auto_install "Monit" "MONIT"
    if [ $? -ne 0 ]; then echo_failure "Failed to install monit"; exit -1; fi
  fi
  monit_clear; monit_detect;
    if [[ -z "$monit" ]]; then
      echo_failure "monit is not installed"
      echo "  Monit download URL:"
      echo "  http://www.mmonit.com"
      exit -1
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
  if [ "$(whoami)" == "root" ]; then
    auto_install "Developer tools" "DEVELOPER"
    if [ $? -ne 0 ]; then echo_failure "Failed to install monit prerequisites"; exit -1; fi
  fi
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

#if [ -f /etc/monit/monitrc ]; then
#    echo_warning "Monit configuration already exists in /etc/monit/monitrc; backing up"
#    backup_file /etc/monit/monitrc
#else
#	cp monitrc /etc/monit/monitrc
#fi

monitrc=`cat /etc/init.d/monit | grep CONFIG= | cut -d= -f2 | sed 's/\"//g'`

mkdir -p /opt/mtwilson/monit/conf.d

if [ -n "$monitrc" ]; then
  if [ -w "$monitrc" ]; then 
    if ! grep -q "include /opt/mtwilson/monit/conf.d/*" $monitrc; then
      echo "include /opt/mtwilson/monit/conf.d/*" >> $monitrc
    fi
  else
    echo_warning "Current user does not have write permission to $monitrc"
    echo_warning "Append the following line to file $monitrc: include /opt/mtwilson/monit/conf.d/*"
  fi
fi




