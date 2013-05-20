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
  MONIT_YUM_PACKAGES=""
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

chmod 700 monitrc
if [ -f /etc/monit/monitrc ]; then
    echo_warning "Monit configuration already exists in /etc/monit/monitrc; backing up"
    backup_file /etc/monit/monitrc
fi

cp monitrc /etc/monit/monitrc

if using_mysql; then
 cat >> /etc/monit/monitrc << EOF
	# mysql monitor
	check process mysql matching "mysql"
	group database
	start program = "/usr/sbin/service mysql start"
	stop program = "/usr/sbin/service mysql stop"
	if failed host 127.0.0.1 port 3306 then restart
	if 5 restarts within 5 cycles then timeout
 EOF
else
 cat >> /etc/monit/monitrc << EOF
    # postgres monitor
	check process postgresql matching "postgresql"
	group database
	start program = "/usr/sbin/service postgresql start"
	stop program = "/usr/sbin/service postgresql stop"
	if failed host 127.0.0.1 port 5432 then restart
	if 5 restarts within 5 cycles then timeout
 EOF
fi

if using_glassfish; then
 cat >> /etc/monit/monitrc << EOF
	# Monitoring the glassfish java service
	check process glassfish matching "glassfish.jar"
	start program = "/usr/local/bin/mtwilson glassfish-start"
	stop program = "/usr/local/bin/mtwilson glassfish-stop"
	# Glassfish portal
	check host mtwilson-portal with address 127.0.0.1
	start program = "/usr/local/bin/mtwilson-portal start"
	stop program = "/usr/local/bin/mtwilson-portal stop"
	if failed port 8181 TYPE TCPSSL PROTOCOL HTTP
		and request "/mtwilson-portal/home.html" for 1 cycles
	then restart
	if 3 restarts within 10 cycles then timeout
 EOF
else
 cat >> /etc/monit/monitrc << EOF
	#tomcat monitor
	check host tomcat with address 127.0.0.1
	start program = "/usr/local/bin/mtwilson tomcat-start"
	stop program = "/usr/local/bin/mtwilson tomcat-stop"
	if failed port 8443 TYPE TCP PROTOCOL HTTP
		and request "/" for 3 cycles
	then restart
	if 3 restarts within 10 cycles then timeout
	# tomcat portal
	check host mtwilson-portal with address 127.0.0.1
	start program = "/usr/local/bin/mtwilson-portal start"
	stop program = "/usr/local/bin/mtwilson-portal stop"
	if failed port 8443 TYPE TCPSSL PROTOCOL HTTP
		and request "/mtwilson-portal/home.html" for 1 cycles
	then restart
	if 3 restarts within 10 cycles then timeout
 EOF
fi
