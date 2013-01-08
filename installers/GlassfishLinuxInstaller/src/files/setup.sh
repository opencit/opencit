#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

glassfish_required_version=3.0
java_required_version=1.6.0_29

# detect the packages we have to install
GLASSFISH_PACKAGE=`ls -1 glassfish*.zip 2>/dev/null | tail -n 1`

# FUNCTION LIBRARY, VERSION INFORMATION, and LOCAL CONFIGURATION
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi

# SCRIPT EXECUTION
java_require
glassfish_install $GLASSFISH_PACKAGE

echo "You must choose an administrator username and password for Glassfish"
echo "The Glassfish control panel is at https://${MTWILSON_SERVER:-127.0.0.1}:4848"
prompt_with_default AS_ADMIN_USER "Glassfish admin username:"
export AS_ADMIN_USER
prompt_with_default_password AS_ADMIN_PASSWORD "Glassfish admin password:"

export AS_ADMIN_PASSWORDFILE=/etc/glassfish/admin.passwd
mkdir -p /etc/glassfish
touch /etc/glassfish/admin.user /etc/glassfish/admin.passwd /etc/glassfish/admin.passwd.old
chmod 600 /etc/glassfish/admin.user /etc/glassfish/admin.passwd /etc/glassfish/admin.passwd.old
echo "AS_ADMIN_USER=${AS_ADMIN_USER}" > /etc/glassfish/admin.user
echo "AS_ADMIN_PASSWORD=${AS_ADMIN_PASSWORD}" > /etc/glassfish/admin.passwd
echo "AS_ADMIN_PASSWORD=" > /etc/glassfish/admin.passwd.old
#echo "AS_ADMIN_MASTERPASSWORD=changeit" >> /etc/glassfish/admin.passwd

glassfish_require
echo "Glassfish will now ask you for the same information:"
# $glassfish is an alias for full path of asadmin
$glassfish --user=admin --passwordfile=/etc/glassfish/admin.passwd.old change-admin-password
# XXX it asks for the password twice ...  can we script with our known value?
$glassfish --user=admin --passwordfile=/etc/glassfish/admin.passwd.old start-domain
$glassfish --user=admin --passwordfile=/etc/glassfish/admin.passwd.old enable-secure-admin

echo
