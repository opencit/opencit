#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

#glassfish_required_version=3.0
#java_required_version=1.6.0_29

# detect the packages we have to install
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
if no_java ${JAVA_REQUIRED_VERSION:-1.6}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-1.6} or later"; exit 1; fi
glassfish_install $GLASSFISH_PACKAGE

cp jackson-core-asl.jar ${GLASSFISH_HOME}/modules/
cp jackson-mapper-asl.jar ${GLASSFISH_HOME}/modules/
cp jackson-xc.jar ${GLASSFISH_HOME}/modules/

# on installations configured to use mysql, the customer is responsible for 
# providing the java mysql connector before starting the mt wilson installer.
# due to its GPLv2 license we cannot integrate it in any way with what we
# distribute so it cannot be even considered that our product is "based on"
# or is a "derivative work" of mysql.
# here is what the customer is supposed to execute before installing mt wilson:
# # mkdir -p /opt/intel/cloudsecurity/setup-console
# # cp mysql-connector-java-5.1.x.jar /opt/intel/cloudsecurity/setup-console
# so now we check to see if it's there, and copy it to glassfish so the apps
# can use it:
mysqlconnector_files=`ls -1 /opt/intel/cloudsecurity/setup-console/* | grep -i mysql`
if [[ -n "$mysqlconnector_files" ]]; then
  cp $mysqlconnector_files ${GLASSFISH_HOME}/modules/
fi

glassfish_stop
glassfish_start

# create the monit rc files
mkdir -p /etc/monit/conf.d
if [ ! -a /etc/monit/conf.d/glassfish.monitrc ]; then
 echo "# Monitoring the glassfish java service
	check process glassfish matching \"glassfish.jar\"
	start program = \"/usr/local/bin/mtwilson glassfish-start\"
	stop program = \"/usr/local/bin/mtwilson glassfish-stop\"
	# Glassfish portal
	check host mtwilson-portal with address 127.0.0.1
	start program = \"/usr/local/bin/mpctl start\"
	stop program = \"/usr/local/bin/mpctl stop\"
	if failed port 8181 TYPE TCPSSL PROTOCOL HTTP
		and request \"/mtwilson-portal/home.html\" for 1 cycles
	then restart
	if 3 restarts within 10 cycles then timeout" > /etc/monit/conf.d/glassfish.monitrc
fi

echo
