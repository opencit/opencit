#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# CONFIGURATION:

#setupconsole_dir=/opt/intel/cloudsecurity/setup-console
#conf_dir=/etc/intel/cloudsecurity
DEFAULT_MTWILSON_JAVA_DIR=/opt/mtwilson/java
DEFAULT_MTWILSON_CONF_DIR=/opt/mtwilson/configuration

# TERM_DISPLAY_MODE can be "plain" or "color"
TERM_DISPLAY_MODE=color
TERM_STATUS_COLUMN=60
TERM_COLOR_GREEN="\\033[1;32m"
TERM_COLOR_CYAN="\\033[1;36m"
TERM_COLOR_RED="\\033[1;31m"
TERM_COLOR_YELLOW="\\033[1;33m"
TERM_COLOR_NORMAL="\\033[0;39m"


#DEFAULT_MYSQL_HOSTNAME="127.0.0.1"
#DEFAULT_MYSQL_PORTNUM="3306"
#DEFAULT_MYSQL_USERNAME="root"
#DEFAULT_MYSQL_PASSWORD=""
#DEFAULT_MYSQL_DATABASE="mw_as"

#DEFAULT_POSTGRES_HOSTNAME="127.0.0.1"
#DEFAULT_POSTGRES_PORTNUM="5432"
#DEFAULT_POSTGRES_USERNAME="root"
#DEFAULT_POSTGRES_PASSWORD=""
#DEFAULT_POSTGRES_DATABASE="mw_as"

DEFAULT_JAVA_REQUIRED_VERSION="1.7"
DEFAULT_GLASSFISH_REQUIRED_VERSION="4.0"
DEFAULT_TOMCAT_REQUIRED_VERSION="7.0"
DEFAULT_MYSQL_REQUIRED_VERSION="5.0"
DEFAULT_POSTGRES_REQUIRED_VERSION="9.3"

DEFAULT_MTWILSON_API_BASEURL="http://127.0.0.1:"
DEFAULT_TOMCAT_API_PORT="8443"
DEFAULT_GLASSFISH_API_PORT="8181"
#DEFAULT_API_PORT=$DEFAULT_GLASSFISH_API_PORT

export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}

### FUNCTION LIBRARY: echo and export environment variables

# exports the values of the given variables
# example:
# export_vars VARNAME1 VARNAME2 VARNAME3
# there is no display output from this function
export_vars() {
  local names="$@"
  local name
  local value
  for name in $names
  do
    eval value="\$$name"
    if [ -n "$value" ]; then
      eval export $name=$value
    fi
  done
}

# prints the values of the given variables
# example:
# print_vars VARNAME1 VARNAME2 VARNAME3
# example output:
# VARNAME1=some_value1
# VARNAME2=some_value2
# VARNAME3=some_value3
print_vars() {
  local names="$@"
  local name
  local value
  for name in $names
  do
    eval value="\$$name"
    echo "$name=$value"
  done
}


### FUNCTION LIBRARY: generate random passwords

# generates a random password. default is 32 characters in length.
# you can pass a single parameter that is the desired length if
# you want something other than 32.
# usage examples:
# mypassword32=`generate_password`
# mypassword16=`generate_password 16`
# mypassword32=$(generate_password)
# mypassword16=$(generate_password 16)
generate_password() {
  < /dev/urandom tr -dc _A-Za-z0-9- | head -c${1:-32}
}

### FUNCTION LIBRARY: escape out input strings for passing to sed
# you pass it the string you are about to pass to sed that might 
# contain the following characters ()&#%$+
# usage examples:
# new_string=$(sed_escape $string)
sed_escape() {
 echo $(echo $1 | sed -e 's/[()&#%$+]/\\&/g' -e 's/[/]/\\&/g')
}

### FUNCTION LIBRARY: terminal display functions

# move to column 60:    term_cursor_movex $TERM_STATUS_COLUMN
# Environment:
# - TERM_DISPLAY_MODE
term_cursor_movex() {
  local x="$1"
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then
    echo -en "\\033[${x}G"
  fi
}

# Environment:
# - TERM_DISPLAY_MODE
# - TERM_DISPLAY_GREEN
# - TERM_DISPLAY_NORMAL
echo_success() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_GREEN}"; fi
  echo ${@:-"[  OK  ]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 0
}

# Environment:
# - TERM_DISPLAY_MODE
# - TERM_DISPLAY_RED
# - TERM_DISPLAY_NORMAL
echo_failure() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_RED}"; fi
  echo ${@:-"[FAILED]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 1
}

# Environment:
# - TERM_DISPLAY_MODE
# - TERM_DISPLAY_YELLOW
# - TERM_DISPLAY_NORMAL
echo_warning() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_YELLOW}"; fi
  echo ${@:-"[WARNING]"}
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  return 1
}

function validate_path_configuration() {
  local file_path="${1}"
  if [[ "$file_path" == *..* ]]; then
    echo_warning "Path specified is not absolute: $file_path"
  fi
  file_path=`readlink -f "$file_path"` #make file path absolute

  if [[ "$file_path" != '/etc/'* && "$file_path" != '/opt/'* ]]; then
    echo_failure "Configuration path validation failed. Verify path meets acceptable directory constraints: $file_path"
    return 1
  fi
  
  if [[ -f "$file_path" ]]; then
    chmod 600 "${file_path}"
  fi
  return 0
}

function validate_path_data() {
  local file_path="${1}"
  if [[ "$file_path" == *..* ]]; then
    echo_warning "Path specified is not absolute: $file_path"
  fi
  file_path=`readlink -f "$file_path"` #make file path absolute

  if [[ "$file_path" != '/var/'* && "$file_path" != '/opt/'* ]]; then
    echo_failure "Data path validation failed. Verify path meets acceptable directory constraints: $file_path"
    return 1
  fi
  
  if [[ -f "$file_path" ]]; then
    chmod 600 "${file_path}"
  fi
  return 0
}

function validate_path_executable() {
  local file_path="${1}"
  if [[ "$file_path" == *..* ]]; then
    echo_warning "Path specified is not absolute: $file_path"
  fi
  file_path=`readlink -f "$file_path"` #make file path absolute

  if [[ "$file_path" != '/usr/'* && "$file_path" != '/opt/'* ]]; then
    echo_failure "Executable path validation failed. Verify path meets acceptable directory constraints: $file_path"
    return 1
  fi
  
  if [[ -f "$file_path" ]]; then
    chmod 755 "${file_path}"
  fi
  return 0
}

### SHELL FUNCTIONS

# parameters: space-separated list of files to include (shell functions or configuration)
# example:  shell_include_files /path/to/file1 /path/to/file2 /path/to/file3 ...
# if any file does not exist, it is skipped
shell_include_files() {
  for filename in "$@"
  do
    if [ -f "${filename}" ]; then
      . ${filename}
    fi
  done
}


### FUNCTION LIBRARY: information functions

# Runs its argument and negates the error code: 
# If the argument exits with success (0) then this function exits with error (1).
# If the argument exits with error (1) then this function exits with success (0).
# Note: only works with arguments that are executable; any additional parameters will be passed.
# Example:  if not using_java; then echo "Warning: skipping Java"; fi 
no() { $* ; if [ $? -eq 0 ]; then return 1; else return 0; fi }
not() { $* ; if [ $? -eq 0 ]; then return 1; else return 0; fi }

# extracts the major version number (1) out of a string like 1.2.3_4
version_major() {
  echo "${1}" | awk -F . '{ print $1 }'
}
# extracts the minor version number (2) out of a string like 1.2.3_4
version_minor() {
  echo "${1}" | awk -F . '{ print $2 }'
}
# extracts the second minor version number (3) out of a string like 1.2.3_4
version_extract3() {
  local thirdpart=`echo "${1}" | awk -F . '{ print $3 }'`
  echo "${thirdpart}" | awk -F _ '{ print $1 }'
}
# extracts the fourth minor version number (4) out of a string like 1.2.3_4
version_extract4() {
  local thirdpart=`echo "${1}" | awk -F . '{ print $3 }'`
  echo "${thirdpart}" | awk -F _ '{ print $2 }'
}

# two arguments: actual version number (string), required version number (string)
# example:  `is_version_at_least 4.9 5.0` will return "no" because 4.9 < 5.0
is_version_at_least() {
  local testver="${1}"
  local reqver="${2}"
  local hasmajor=`version_major "${testver}"`
  local hasminor=`version_minor "${testver}"`
  local reqmajor=`version_major "${reqver}"`
  local reqminor=`version_minor "${reqver}"`
  if [[  -n "${reqmajor}" && "${hasmajor}" -gt "${reqmajor}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -z "${reqminor}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}" && "${hasminor}" -ge "${reqminor}" \
     ]]; then
    #echo "yes"
    return 0
  else
    #echo "no"
    return 1
  fi  
}

# like is_version_at_least but works on entire java version string 1.7.0_51
# instead of just a major.minor number
# Parameters:
# - version to test (of installed software)
# - minimum required version
# Return code:  0 (no errors) if the java version given is greater than or equal to the minimum version
is_java_version_at_least() {
  local testver="${1}"
  local reqver="${2}"
  local hasmajor=`version_major "${testver}"`
  local hasminor=`version_minor "${testver}"`
  local hasminor3=`version_extract3 "${testver}"`
  local hasminor4=`version_extract4 "${testver}"`
  local reqmajor=`version_major "${reqver}"`
  local reqminor=`version_minor "${reqver}"`
  local reqminor3=`version_extract3 "${reqver}"`
  local reqminor4=`version_extract4 "${reqver}"`
  if [[  -n "${reqmajor}" && "${hasmajor}" -gt "${reqmajor}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -z "${reqminor}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}" && "${hasminor}" -gt "${reqminor}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}"  && "${hasminor}"  -eq "${reqminor}" \
       && -n "${reqminor3}" && "${hasminor3}" -gt "${reqminor3}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}"  && "${hasminor}"  -eq "${reqminor}" \
       && -z "${reqminor3}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}"  && "${hasminor}"  -eq "${reqminor}" \
       && -n "${reqminor3}" && "${hasminor3}" -eq "${reqminor3}" \
       && -z "${reqminor4}" \
     || \
       -n "${reqmajor}" && "${hasmajor}" -eq "${reqmajor}" \
       && -n "${reqminor}"  && "${hasminor}"  -eq "${reqminor}" \
       && -n "${reqminor3}" && "${hasminor3}" -eq "${reqminor3}" \
       && -n "${reqminor4}" && "${hasminor4}" -ge "${reqminor4}"  \
     ]]; then
#    echo "yes"
    return 0
  else
#    echo "no"
    return 1
  fi  
}

# Parameters:
# - variable name to set with result
# - prompt (string)
# will accept y, Y, or nothing as yes, anything else as no
prompt_yes_no() {
  local resultvarname="${1}"
  local userprompt="${2}"
  # bug #512 add support for answer file
  if [ -n "${!resultvarname}" ]; then
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
    echo "$userprompt [Y/n] ${!resultvarname}"
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
    return
  fi
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
  echo -n "$userprompt [Y/n] "
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  local userinput
  read -n 1 userinput
  echo
  if [[ $userinput == "Y" || $userinput == "y" || $userinput == "" ]]; then
    eval $resultvarname="yes"
  else
    eval $resultvarname="no"
  fi
}

# Parameters:
# - character like 'a' for which to echo the character code
# echos the character code of the specified character
# For example:   ord a     will echo 97
ord() { printf '%d' "'$1"; }

# Parameters:
# - variable name to set with result
# - prompt text (include any punctuation such as ? or : you want to display)
# - default setting (do not include any brackets or punctuation). 
#   If the default setting is omitted, the current value of the output variable name will be used.
# Output:
# - result (input or default) is saved into the specified variable name
#
# Examples:
#   prompt_with_default USERNAME "What is your name?"
#   prompt_with_default USERCOLOR "What is your favorite color?" ${DEFAULT_COLOR}
prompt_with_default() {
  local resultvarname="${1}"
  local userprompt="${2}"
  local default_value
  # here $$
  eval current_value="\$$resultvarname"
  eval default_value="${3:-$current_value}"
  # bug #512 add support for answer file
  if [ -n "${!resultvarname}" ]; then
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
    echo "$userprompt [$default_value] ${!resultvarname:-$default_value}"
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
    return
  fi
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
  echo -n "$userprompt [$default_value] "
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  local userinput
  read userinput
  eval $resultvarname=${userinput:-$default_value}
}

# Same as prompt_with_default, but the default value is hidden by *******,
# and if prompt text is not provided then the default prompt is "Password:"
prompt_with_default_password() {
  local resultvarname="${1}"
  local userprompt="${2:-Password:}"
  local default_value
  # here $$
  eval variable_name="$resultvarname"
  #echo_warning "variable name is $variable_name"
  eval current_value="\$$variable_name"
  #echo_warning "current value = $current_value"
  eval default_value="${3:-'$current_value'}"
  #echo_warning "default value = $default_value"
  local default_value_display="********"
  if [ -z "$default_value" ]; then default_value_display=""; fi;
  # bug #512 add support for answer file
  if [ -n "${!resultvarname}" ]; then
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
    echo "$userprompt [$default_value_display] ${default_value_display}"
    if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
    return
  fi
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_CYAN}"; fi
  echo -n "$userprompt [$default_value_display] "
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
  local userinput=""
  #IFS= read -r -s userinput
  #echo
  local input_counter=0
  local char
  while IFS= read -r -s -n 1 char
  do
    local code=`ord $char`
    if [[ $char == $'\0' ]]; then
      break
    elif [[ "$code" == "8" || "$code" == "127" ]]; then
      if (($input_counter > 0)); then
        echo -n $'\b \b';
        ((input_counter--))
        userinput="${userinput%?}"
      fi
    else
      echo -n '*'
      userinput+="$char";
      ((input_counter++))
    fi
  done
  echo
  if [ ! -z "$userinput" ]; then
   eval $resultvarname='$userinput'
  else
   eval $resultvarname='$default_value'
  fi
  #eval $resultvarname="${userinput:-'$default_value'}"
}

### FUNCTION LIBRARY: environment information functions

# Input: path to file that should exist
wait_until_file_exists() {
  markerfile=$1
  while [ ! -f "$markerfile" ]; do sleep 1; done
}

# Usage example:   if using_glassfish; then echo "Using glassfish"; fi
using_glassfish() {
  if [[ -n "$WEBSERVER_VENDOR" ]]; then
    if [[ "${WEBSERVER_VENDOR}" == "glassfish" ]]; then
      return 0
    else
      return 1
    fi
  else
    glassfish_detect 2>&1 > /dev/null
    tomcat_detect 2>&1 > /dev/null
    if [ -n "$GLASSFISH_HOME" ]; then
      return 0
    else
      return 1
    fi
  fi
}
using_tomcat() {
  if [[ -n "$WEBSERVER_VENDOR" ]]; then
    if [[ "${WEBSERVER_VENDOR}" == "tomcat" ]]; then
      return 0
    else
      return 1
    fi
  else
    glassfish_detect 2>&1 > /dev/null
    tomcat_detect 2>&1 > /dev/null
    if [ -n "$TOMCAT_HOME" ]; then
      return 0
    else
      return 1
    fi
  fi
}
# currently jetty is indicated either by WEBSERVER_VENDOR=jetty or by
# absence of both tomcat and glassfish. there's not an independent
# function for jetty_detect.
using_jetty() {
  if [[ -n "$WEBSERVER_VENDOR" ]]; then
    if [[ "${WEBSERVER_VENDOR}" == "jetty" ]]; then
      return 0
    else
      return 1
    fi
  else
    glassfish_detect 2>&1 > /dev/null
    tomcat_detect 2>&1 > /dev/null
    if [ -z "$GLASSFISH_HOME" ] && [ -z "$TOMCAT_HOME" ]; then
      return 0
    else
      return 1
    fi
  fi
}

using_mysql() { if [[ "${DATABASE_VENDOR}" == "mysql" ]]; then return 0; else return 1; fi }
using_postgres() { if [[ "${DATABASE_VENDOR}" == "postgres" ]]; then return 0; else return 1; fi }
 
### FUNCTION LIBARRY: conditional execution functions

# parameters: condition variable name, status line, code to run
# Will print "status line... " and then "OK" or "FAILED"
action_condition() {
  local condvar="${1}"
  local statusline="${2}"
  local condfn="${3}"
  local cond=$(eval "echo \$${condvar}")
  echo -n "$statusline"
  echo -n "... "
  if [ -n "$cond" ]; then
    echo_success "Skipped"
  else # if [ -z "$cond" ]; then
    eval "$condfn"
    cond=$(eval "echo \$${condvar}")
    if [ -n "$cond" ]; then
      echo_success "OK"
    else
      echo_failure "FAILED"
    fi
  fi
}
# similar to action_condition but reverses the logic: empty is OK, defined is FAILED
inaction_condition() {
  local condvar="${1}"
  local statusline="${2}"
  local condfn="${3}"
  local cond=$(eval "echo \$${condvar}")
  echo -n "$statusline"
  echo -n "... "
  if [ -z "$cond" ]; then
    echo_success "Skipped"
  else # if [ -z "$cond" ]; then
    eval "$condfn"
    cond=$(eval "echo \$${condvar}")
    if [ -z "$cond" ]; then
      echo_success "OK"
    else
      echo_failure "FAILED"
    fi
  fi
}

### FUNCTION LIBRARY: file management

# parameter: one or more paths to check for existence
# output: the first entry found to exist
# example:
# mybinary=`first_existing /usr/local/bin/ahctl /usr/bin/ahctl /opt/local/bin/ahctl /opt/bin/ahctl /opt/intel/cloudsecurity/attestation-service/bin/ahctl`
first_existing() {
    local search_locations="$@"
    local file
    for file in $search_locations
    do
      if [[ -e "$file" ]]; then
        echo "$file"
        return 0
      fi
    done
    return 1
}

# parameters: one or more files/directories to check for existence
# return value: returns 0 if all files are present, 1 if any are missing; displays report on screen
# example:
# report_files_exist /etc/file1 /etc/file2
report_files_exist() {
    local search_locations="$@"
    local report_summary=0
    local file
    for file in $search_locations
    do
      if [[ -e "$file" ]]; then
        echo_success "$file" exists
      else
        echo_failure "$file" missing
        report_summary=1
      fi
    done
    return $report_summary
}

# makes a date-stamped backup copy of a file
backup_file() {
  local filename="${1}"
  local datestr=`date +%Y-%m-%d.%H%M`
  local backup_filename="${filename}.${datestr}"
  if [[ -n "$filename" && -f "$filename" ]]; then
    cp ${filename} ${backup_filename}
    echo "${backup_filename}"
    return 0
  fi
  return 1
}

# read a property from a property file formatted like NAME=VALUE
# parameters: property name, filename
# example: read_property_from_file MYFLAG FILENAME
# Automatically strips Windows carriage returns from the file
read_property_from_file() {
  local property="${1}"
  local filename="${2}"
  if ! validate_path_configuration "$filename"; then exit -1; fi
  if [ -f "$filename" ]; then
    local found=`cat "$filename" | grep "^$property"`
    if [ -n "$found" ]; then
      #echo -n `cat "$filename" | tr -d '\r' | grep "^$property" | tr -d '\n' | awk -F '=' '{ print $2 }'`
      echo `cat "$filename" | tr -d '\r' | grep "^$property" | head -n 1 | awk -F '=' '{ print $2 }'`
    fi
  fi
}

# write a property into a property file, replacing the previous value
# parameters: property name, filename, new value
# example: update_property_in_file MYFLAG FILENAME true
update_property_in_file() {
  local property="${1}"
  local filename="${2}"
  local value="${3}"
  local encrypted="false"

  if ! validate_path_configuration "$filename"; then exit -1; fi
  if [ -f "$filename" ]; then
    # Decrypt if needed
    if file_encrypted "$filename"; then
      encrypted="true"
      decrypt_file "$filename" "$MTWILSON_PASSWORD"
    fi

    local ispresent=`grep "^${property}" "$filename"`
    if [ -n "$ispresent" ]; then
      # first escape the pipes new value so we can use it with replacement command, which uses pipe | as the separator
      local escaped_value=`echo "${value}" | sed 's/|/\\|/g'`
      local sed_escaped_value=$(sed_escape "$escaped_value")
      # replace just that line in the file and save the file
      updatedcontent=`sed -re "s|^(${property})\s*=\s*(.*)|\1=${sed_escaped_value}|" "${filename}"`
      # protect against an error
      if [ -n "$updatedcontent" ]; then
        echo "$updatedcontent" > "${filename}"
      else
        echo_warning "Cannot write $property to $filename with value: $value"
        echo -n 'sed -re "s|^('
        echo -n "${property}"
        echo -n ')=(.*)|\1='
        echo -n "${escaped_value}"
        echo -n '|" "'
        echo -n "${filename}"
        echo -n '"'
        echo
      fi
    else
      # property is not already in file so add it. extra newline in case the last line in the file does not have a newline
      echo "" >> "${filename}"
      echo "${property}=${value}" >> "${filename}"
    fi

  # Return the file to encrypted state, if it was before
  if [ encrypted == "true" ]; then
    encrypt_file "$filename" "$MTWILSON_PASSWORD"
  fi
  # test
  else
    # file does not exist so create it
    echo "${property}=${value}" > "${filename}"
  fi
}

configure_api_baseurl() {
  # setup mtwilson.api.baseurl
  local config_file="${1:-/etc/intel/cloudsecurity/management-service.properties}" 
  
  local input_api_baseurl
  if [ -n "${MTWILSON_API_BASEURL}" ]; then
    mtwilson_api_baseurl="${MTWILSON_API_BASEURL}"
  elif [[ -n "${MTWILSON_SERVER}" && -n "${DEFAULT_API_PORT}" ]]; then
    mtwilson_api_baseurl="https://${MTWILSON_SERVER}:$DEFAULT_API_PORT"
  else
    local configured_api_baseurl="$CONFIGURED_API_BASEURL"   #`read_property_from_file mtwilson.api.baseurl "${config_file}"`
    if [ -z "${configured_api_baseurl}" ]; then
      prompt_with_default input_api_baseurl "Mt Wilson Server (https://[IP]:[PORT]):" "${configured_api_baseurl}"
    else
      input_api_baseurl="$configured_api_baseurl"
    fi
    
    if [[ "$input_api_baseurl" == http* ]]; then
      mtwilson_api_baseurl="$input_api_baseurl"
    else
      mtwilson_api_baseurl="https://${input_api_baseurl}"
    fi
  fi
  export MTWILSON_API_BASEURL=$mtwilson_api_baseurl
  update_property_in_file mtwilson.api.baseurl "${config_file}" "${mtwilson_api_baseurl}"
}

### FUNCTION LIBRARY: package management

# RedHat and CentOS may have yum and rpm

# Output:
# - variable "yum" contains path to yum or empty
yum_detect() {
  yum=`which yum 2>/dev/null`
  if [ -n "$yum" ]; then return 0; else return 1; fi
}
no_yum() {
  if yum_detect; then return 1; else return 0; fi
}

# Output:
# - variable "rpm" contains path to rpm or empty
rpm_detect() {
  rpm=`which rpm 2>/dev/null`
}

# Debian and Ubuntu may have apt-get and dpkg
# Output:
# - variable "aptget" contains path to apt-get or empty
# - variable "aptcache" contains path to apt-cache or empty
aptget_detect() {
  aptget=`which apt-get 2>/dev/null`
  aptcache=`which apt-cache 2>/dev/null`
}
# Output:
# - variable "dpkg" contains path to dpkg or empty
dpkg_detect() {
  dpkg=`which dpkg 2>/dev/null`
}

# SUSE has yast
# Output:
# - variable "yast" contains path to yast or empty
yast_detect() {
  yast=`which yast 2>/dev/null`
}

# SUSE has zypper
# Output:
# - variable "zypper" contains path to zypper or empty
zypper_detect() {
  zypper=`which zypper 2>/dev/null`
}

trousers_detect() {
  trousers=`which tcsd 2>/dev/null`
}

# Parameters:
# - absolute path to startup script to register
# - the name to use in registration (one word)
register_startup_script() {
  local absolute_filename="${1}"
  local startup_name="${2}"
  shift; shift;
  
  # try to install it as a startup script
  if [ -d /etc/init.d ]; then
    local prevdir=`pwd`
    cd /etc/init.d
    if [ -f "${startup_name}" ]; then rm -f "${startup_name}"; fi
    ln -s "${absolute_filename}" "${startup_name}"
    cd "$prevdir"
  fi

  # RedHat and SUSE
  chkconfig=`which chkconfig  2>/dev/null`
  if [ -n "$chkconfig" ]; then
    $chkconfig --del "${startup_name}"  2>/dev/null
    $chkconfig --add "${startup_name}"  2>/dev/null
  fi

  # Ubuntu
  updatercd=`which update-rc.d  2>/dev/null`
  if [ -n "$updatercd" ]; then
    $updatercd -f "${startup_name}" remove 2>/dev/null
    $updatercd "${startup_name}" defaults $@ 2>/dev/null
  fi
}

# Parameters:
# - the name of the startup script (one word)
remove_startup_script() {
  local startup_name="${1}"
  shift;

  # RedHat and SUSE
  chkconfig=`which chkconfig  2>/dev/null`
  if [ -n "$chkconfig" ]; then
    $chkconfig --del "${startup_name}"  2>/dev/null
  fi

  # Ubuntu
  updatercd=`which update-rc.d  2>/dev/null`
  if [ -n "$updatercd" ]; then
    $updatercd -f "${startup_name}" remove 2>/dev/null
  fi
  
  # try to install it as a startup script
  if [ -d "/etc/init.d" ]; then
    rm "/etc/init.d/${startup_name}" 2>/dev/null
  fi
}

# Ensure the package actually needs to be installed before calling this function.
# takes arguments: component name (string), package list prefix (string)
auto_install() {
  local component=${1}
  local cprefix=${2}
  local yum_packages=$(eval "echo \$${cprefix}_YUM_PACKAGES")
  local apt_packages=$(eval "echo \$${cprefix}_APT_PACKAGES")
  local yast_packages=$(eval "echo \$${cprefix}_YAST_PACKAGES")
  local zypper_packages=$(eval "echo \$${cprefix}_ZYPPER_PACKAGES")
  # detect available package management tools. start with the less likely ones to differentiate.
  yum_detect; yast_detect; zypper_detect; rpm_detect; aptget_detect; dpkg_detect;
  if [[ -n "$zypper" && -n "$zypper_packages" ]]; then
        zypper install $zypper_packages
  elif [[ -n "$yast" && -n "$yast_packages" ]]; then
        yast -i $yast_packages
  elif [[ -n "$yum" && -n "$yum_packages" ]]; then
        yum -y install $yum_packages
  elif [[ -n "$aptget" && -n "$apt_packages" ]]; then
        apt-get -y install $apt_packages
  fi
}

# this was used in setup.sh when we installed complete rpm or deb packages via the self-extracting installer.
# not currently used, but will be used again when we return to rpm and deb package descriptors
# in conjunction with the self-extracting installer 
my_service_install() {
  auto_install "Application requirements" "APPLICATION"
  if [[ -n "$dpkg" && -n "$aptget" ]]; then
    is_installed=`$dpkg --get-selections | grep "${package_name_deb}" | awk '{ print $1 }'`
    if [ -n "$is_installed" ]; then
      echo "Looks like ${package_name} is already installed. Cleaning..."
      $dpkg -P ${is_installed}
    fi
    echo "Installing $DEB_PACKAGE"
    $dpkg -i $DEB_PACKAGE
    $aptget -f install
  elif [[ -n "$rpm" && -n "$yum" ]]; then
    is_installed=`$rpm -qa | grep "${package_name_rpm}"`
    if [ -n "$is_installed" ]; then
      echo "Looks like ${package_name} is already installed. Cleaning..."
      $rpm -e ${is_installed}
    fi
    echo "Installing $RPM_PACKAGE"
    $rpm -i $RPM_PACKAGE
  fi
  $package_setup_cmd
}

### FUNCTION LIBRARY: NETWORK INFORMATION

# Echo all the localhost's non-loopback IP addresses
# Parameters: None
# Output:
#   The output of "ifconfig" will be scanned for any non-loopback address and all results will be echoed
hostaddress_list() {
  # if you want to exclude certain categories, such as 192.168, add this after the 127.0.0.1 exclusion:  grep -v "^192.168." 
  ifconfig | grep "inet addr" | awk '{ print $2 }' | awk -F : '{ print $2 }' | grep -v "127.0.0.1"
}

# Echo all the localhost's addresses including loopback IP address
# Parameters: none
# output:  10.1.71.56,127.0.0.1
hostaddress_list_csv() {
  ifconfig | grep -E "^\s*inet addr:" | awk '{ print $2 }' | awk -F : '{ print $2 }' | paste -d',' -s
}


# Echo localhost's non-loopback IP address
# Parameters: None
# Output:
#   If the environment variable HOSTADDRESS exists and has a value, its value will be used (careful to make sure it only has one address!).
#   Otherwise If the file /etc/ipaddress exists, the first line of its content will be echoed. This allows a system administrator to "override" the output of this function for the localhost.
#   Otherwise the output of "ifconfig" will be scanned for any non-loopback address and the first one will be used.
hostaddress() {
  if [ -n "$HOSTADDRESS" ]; then
    echo "$HOSTADDRESS"
  elif [ -s /etc/ipaddress ]; then
    cat /etc/ipaddress | head -n 1
  else
    # if you want to exclude certain categories, such as 192.168, add this after the 127.0.0.1 exclusion:  grep -v "^192.168." 
    local HOSTADDRESS=`hostaddress_list | head -n 1`
    echo "$HOSTADDRESS"
  fi
}



### FUNCTION LIBRARY: SSH FUNCTIONS

# Displays the fingerprints of all ssh host keys on this server
ssh_fingerprints() {
  local has_ssh_keygen=`which ssh-keygen 2>/dev/null`
  if [ -z "$has_ssh_keygen" ]; then echo_warning "missing program: ssh-keygen"; return; fi
  local ssh_pubkeys=`find /etc -name ssh_host_*.pub`
  for file in $ssh_pubkeys
  do
    local keybits=`ssh-keygen -lf "$file" | awk '{ print $1 }'`
    local keyhash=`ssh-keygen -lf "$file" | awk '{ print $2 }'`
    local keytype=`ssh-keygen -lf "$file" | awk '{ print $4 }' | tr -d '()'`
    echo "$keyhash ($keytype-$keybits)"
  done
}


### FUNCTION LIBRARY: MYSQL FUNCTIONS


# parameters:
# 1. path to properties file
# 2. properties prefix (for mountwilson.as.db.user etc. the prefix is mountwilson.as.db)
# the default prefix is "mysql" for properties like "mysql.user", etc. The
# prefix must not have any spaces or special shell characters
# ONLY USE IF FILES ARE UNENCRYPTED!!!
mysql_read_connection_properties() {
    local config_file="$1"
    local prefix="${2:-mysql}"
    MYSQL_HOSTNAME=`read_property_from_file ${prefix}.host "${config_file}"`
    MYSQL_PORTNUM=`read_property_from_file ${prefix}.port "${config_file}"`
    MYSQL_USERNAME=`read_property_from_file ${prefix}.user "${config_file}"`
    MYSQL_PASSWORD=`read_property_from_file ${prefix}.password "${config_file}"`
    MYSQL_DATABASE=`read_property_from_file ${prefix}.schema "${config_file}"`
}

# ONLY USE IF FILES ARE UNENCRYPTED!!!
mysql_write_connection_properties() {
    local config_file="$1"
    local prefix="${2:-mysql}"
    local encrypted="false"

    # Decrypt if needed
    if file_encrypted "$config_file"; then
      encrypted="true"
      decrypt_file "$config_file" "$MTWILSON_PASSWORD"
    fi
    update_property_in_file ${prefix}.host "${config_file}" "${MYSQL_HOSTNAME}"
    update_property_in_file ${prefix}.port "${config_file}" "${MYSQL_PORTNUM}"
    update_property_in_file ${prefix}.user "${config_file}" "${MYSQL_USERNAME}"
    update_property_in_file ${prefix}.password "${config_file}" "${MYSQL_PASSWORD}"
    update_property_in_file ${prefix}.schema "${config_file}" "${MYSQL_DATABASE}"
    update_property_in_file ${prefix}.driver "${config_file}" "com.mysql.jdbc.Driver"
    # if you create a .url property then it takes precedence over the .host, .port, and .schema - so let user do that
    
    # Return the file to encrypted state, if it was before
    if [ encrypted == "true" ]; then
      encrypt_file "$config_file" "$MTWILSON_PASSWORD"
    fi
}

# parameters:
# - configuration filename (absolute path)
# - property prefix for settings in the configuration file (java format is assumed, dot will be automatically appended to prefix)
mysql_userinput_connection_properties() {
    echo "Configuring DB Connection..."
    prompt_with_default MYSQL_HOSTNAME "Hostname:" ${DEFAULT_MYSQL_HOSTNAME}
    prompt_with_default MYSQL_PORTNUM "Port Num:" ${DEFAULT_MYSQL_PORTNUM}
    prompt_with_default MYSQL_DATABASE "Database:" ${DEFAULT_MYSQL_DATABASE}
    prompt_with_default MYSQL_USERNAME "Username:" ${DEFAULT_MYSQL_USERNAME}
    prompt_with_default_password MYSQL_PASSWORD "Password:" ${DEFAULT_MYSQL_PASSWORD}
}

mysql_clear() {
  MYSQL_HOME=""
  mysql=""
}

# Environment:
# - MYSQL_REQUIRED_VERSION (or provide it as a parameter)
mysql_version() {
  local min_version="${1:-${MYSQL_REQUIRED_VERSION:-$DEFAULT_MYSQL_REQUIRED_VERSION}}"
  MYSQL_CLIENT_VERSION=""
  MYSQL_CLIENT_VERSION_OK=""
  if [ -n "$mysql" ]; then
    MYSQL_CLIENT_VERSION=`$mysql --version | sed -e 's/^.*Distrib \([0-9.]*\).*$/\1/g;'`
    if is_version_at_least "$MYSQL_CLIENT_VERSION" "${min_version}"; then
      MYSQL_CLIENT_VERSION_OK=yes
    else
      MYSQL_CLIENT_VERSION_OK=no
    fi
  fi
}

# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_version_report() {
  mysql_version
  if [ "$MYSQL_CLIENT_VERSION_OK" == "yes" ]; then
    echo_success "Mysql client version $MYSQL_CLIENT_VERSION is ok"
  else
    echo_warning "Mysql client version $MYSQL_CLIENT_VERSION is not supported, minimum is ${MYSQL_REQUIRED_VERSION:-$DEFAULT_MYSQL_REQUIRED_VERSION}"
  fi
}

# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_detect() {
  local min_version="${1:-${MYSQL_REQUIRED_VERSION:-$DEFAULT_MYSQL_REQUIRED_VERSION}}"
  if [[ -n "$MYSQL_HOME" && -n "$mysql" && -f "$mysql" ]]; then
    return
  fi
  mysql=`which mysql 2>/dev/null`
  if [ -e "$mysql" ]; then
    MYSQL_HOME=`dirname "$mysql"`
    echo "Found mysql client: $mysql"
    mysql_version ${min_version}
    if [ "$MYSQL_CLIENT_VERSION_OK" != "yes" ]; then
  MYSQL_HOME=''
  mysql=""
    fi
  fi
}


mysql_server_detect() {
  if [[ -n "$mysqld" && -f "$mysqld" ]]; then
    return 0
  fi
  mysql_installed=$(which mysql 2>/dev/null)
  if [ -n "$mysql_installed" ]; then
    mysqld="service mysql"
    echo "Found mysql server: $mysqld"
    return 0
  fi
  if [[ -f /usr/bin/mysqld_safe ]]; then
    mysqld="/usr/bin/mysqld_safe"
    echo "Found mysql server: $mysqld"
    return 0
  fi
  mysqld=`which mysqld_safe 2>/dev/null`
  if [[ -f "$mysqld" ]]; then
    echo "Found mysql server: $mysqld"
    return 0
  fi
  return 1
}


# must load from config file or call mysql_detect prior to calling this function
mysql_env_report() {
  echo "mysql=$mysql"
}

# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_require() {
  local min_version="${1:-${MYSQL_REQUIRED_VERSION:-$DEFAULT_MYSQL_REQUIRED_VERSION}}"
  if [[ -z "$MYSQL_HOME" || -z "$mysql" || ! -f "$mysql" ]]; then
    mysql_detect ${min_version} > /dev/null
  fi
  if [[ -z "$MYSQL_HOME" || -z "$mysql" || ! -f "$mysql" ]]; then
    echo "Cannot find MySQL client version $min_version or later"
    exit 1
  fi
}



# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_connection() {
  mysql_require
  mysql_connect="$mysql --batch --host=${MYSQL_HOSTNAME:-$DEFAULT_MYSQL_HOSTNAME} --port=${MYSQL_PORTNUM:-$DEFAULT_MYSQL_PORTNUM} --user=${MYSQL_USERNAME:-$DEFAULT_MYSQL_USERNAME} --password=${MYSQL_PASSWORD:-$DEFAULT_MYSQL_PASSWORD}"
}

# Environment:
# - MYSQL_REQUIRED_VERSION
# sets the is_mysql_available variable to "yes" or ""
# sets the is_MYSQL_DATABASE_created variable to "yes" or ""
mysql_test_connection() {
  mysql_connection
  is_mysql_available=""
  local mysql_test_result=`$mysql_connect -e "show databases" 2>/tmp/intel.mysql.err | grep "^${MYSQL_DATABASE}\$" | wc -l`
  if [ $mysql_test_result -gt 0 ]; then
    is_mysql_available="yes"
  fi
  mysql_connection_error=`cat /tmp/intel.mysql.err`
  rm -f /tmp/intel.mysql.err
}

# Environment:
# - MYSQL_REQUIRED_VERSION
mysql_test_connection_report() {
  echo -n "Testing database connection... "
  mysql_test_connection
  if [ -n "$is_mysql_available" ]; then
    echo "OK"
  else
    echo "FAILED"
    echo_failure "${mysql_connection_error}"
  fi
}


# Environment:
# - MYSQL_REQUIRED_VERSION
# installs mysql client programs (not the server)
# we need the mysql client to create or patch the database, but
# the server can be installed anywhere
mysql_install() {
  MYSQL_CLIENT_YUM_PACKAGES="mysql"
  MYSQL_CLIENT_APT_PACKAGES="mysql-client"
  mysql_detect > /dev/null
  if [[ -z "$MYSQL_HOME" || -z "$mysql" ]]; then
    auto_install "MySQL client" "MYSQL_CLIENT" >> $INSTALL_LOG_FILE
    if [[ -z "$MYSQL_HOME" || -z "$mysql" ]]; then
      echo_failure "Unable to auto-install MySQL client" | tee -a $INSTALL_LOG_FILE
      echo "MySQL download URL:" >> $INSTALL_LOG_FILE
      echo "http://www.mysql.com/downloads/" >> $INSTALL_LOG_FILE
    fi
  else
    echo "MySQL client is already installed" >> $INSTALL_LOG_FILE
  fi
}

# Environment:
# - MYSQL_REQUIRED_VERSION
# installs mysql server 
mysql_server_install() {
  MYSQL_SERVER_YUM_PACKAGES="mysql-server"
  MYSQL_SERVER_APT_PACKAGES="mysql-server"
  mysql_server_detect >> $INSTALL_LOG_FILE
  if [[ -n "$mysqld" ]]; then
    echo "MySQL server is already installed" >> $INSTALL_LOG_FILE
    return;
  fi
  if [[ -z "$mysqld" ]]; then
    auto_install "MySQL server" "MYSQL_SERVER"   >> $INSTALL_LOG_FILE
    mysql_server_detect
  fi
  if [[ -z "$mysqld" ]]; then
    MYSQL_SERVER_YUM_PACKAGES=""
    MYSQL_SERVER_APT_PACKAGES="mysql-server-5.5"
    auto_install "MySQL server" "MYSQL_SERVER"  >> $INSTALL_LOG_FILE
    mysql_server_detect
  fi
  if [[ -z "$mysqld" ]]; then
    MYSQL_SERVER_YUM_PACKAGES=""
    MYSQL_SERVER_APT_PACKAGES="mysql-server-5.1"
    auto_install "MySQL server" "MYSQL_SERVER"  >> $INSTALL_LOG_FILE
    mysql_server_detect
  fi
  if [[ -z "$mysqld" ]]; then
    echo_failure "Unable to auto-install MySQL server" | tee -a $INSTALL_LOG_FILE
    echo "MySQL download URL:"  >> $INSTALL_LOG_FILE
    echo "http://www.mysql.com/downloads/" >> $INSTALL_LOG_FILE
  fi
}

# responsible for ensuring that the connection properties in the config file
# Call this from the control script such as "asctl" before calling the other mysql_* functions
# Parameters:
# - absolute path to configuration file
# - prefix of mysql property file names (java style, dot is added automatically)
# Environment:
# - script_name such as 'asctl' or 'wlmctl'
# - intel_conf_dir (deprecated, just use absolute package_config_filename)
# - package_config_filename  (should be absolute)
mysql_configure_connection() {
    local config_file="${1:-/etc/intel/cloudsecurity/mysql.properties}"
    local prefix="${2:-mysql}"
    mysql_test_connection
    if [ -z "$is_mysql_available" ]; then
      #mysql_read_connection_properties "${config_file}" "${prefix}"
      mysql_test_connection
    fi
    while [ -n "$mysql_connection_error" ]
    do
      echo_warning "Cannot connect to MySQL: $mysql_connection_error"
      prompt_yes_no MYSQL_RETRY_CONFIGURE_AFTER_FAILURE "Do you want to configure it now?"
      if [[ "no" == "$MYSQL_RETRY_CONFIGURE_AFTER_FAILURE" ]]; then
        echo "MySQL settings are in ${package_config_filename}"
        echo "Run '${script_name} setup' after configuring to continue."
        return 1
      fi
      mysql_userinput_connection_properties
      mysql_test_connection
    done
      echo_success "Connected to database \`${MYSQL_DATABASE}\` on ${MYSQL_HOSTNAME}"
#      local should_save
#      prompt_yes_no should_save "Save in ${package_config_filename}?"
#      if [[ "yes" == "${should_save}" ]]; then
      mysql_write_connection_properties "${config_file}" "${prefix}"
#      fi
}

# requires a mysql connection that can access the existing database, OR (if it doesn't exist)
# requires a mysql connection that can create databases and grant privileges
# call mysql_configure_connection before calling this function
mysql_create_database() {

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
	
  mysql_test_connection
  local create_sql="CREATE DATABASE \`${MYSQL_DATABASE}\`;"
  local grant_sql="GRANT ALL ON \`${MYSQL_DATABASE}\`.* TO \`${MYSQL_USERNAME}\` IDENTIFIED BY '${MYSQL_PASSWORD}';"
  if [ -z "$mysql_connection_error" ]; then
    if [ -n "$is_mysql_available" ]; then
      echo_success "Database \`${MYSQL_DATABASE}\` already exists"   >> $INSTALL_LOG_FILE
      return 0
    else
      echo "Creating database..."    >> $INSTALL_LOG_FILE
      $mysql_connect -e "${create_sql}"
      $mysql_connect -e "${grant_sql}"
      mysql_test_connection
      if [ -z "$is_mysql_available" ]; then
        echo_failure "Failed to create database."  | tee -a $INSTALL_LOG_FILE
        return 1
      fi
    fi
  else
    echo_failure "Cannot connect to database."  | tee -a $INSTALL_LOG_FILE
    echo "Try to execute the following commands on the database:"  >> $INSTALL_LOG_FILE
    echo "${create_sql}" >> $INSTALL_LOG_FILE
    echo "${grant_sql}"  >> $INSTALL_LOG_FILE
    return 1
  fi
}

# before using this function, you must first set the connection variables mysql_*
# example:  mysql_run_script /path/to/statements.sql
mysql_run_script() {
  local scriptfile="${1}"
  local datestr=`date +%Y-%m-%d.%H%M`
  echo "##### [${datestr}] Script file: ${scriptfile}" >> ${mysql_setup_log}
  $mysql_connect --force ${MYSQL_DATABASE} < "${scriptfile}" 2>> ${mysql_setup_log}
}

# requires a mysql connection that can create tables and procedures inside an existing database.
# depends on mysql_* variables for the connection information.
# call mysql_configure_connection before calling this function.
# Parameters: a list of sql files to execute (absolute paths)
mysql_install_scripts() {
  local scriptlist="$@"
  mysql_test_connection
  if [ -n "$is_mysql_available" ]; then
    echo "Connected to ${MYSQL_HOSTNAME} as ${MYSQL_USERNAME}. Executing script..."
    for scriptname in $scriptlist
    do
        mysql_run_script $scriptname
    done
    return 0
  else
    echo_failure "Cannot connect to database."
    return 1
  fi
}



mysql_running() {  
  MYSQL_SERVER_RUNNING=''
  if [ -n "$mysqld" ]; then
    local is_running=`$mysqld status | grep running`
    if [ -n "$is_running" ]; then
      MYSQL_SERVER_RUNNING=yes
    fi
  fi
}

mysql_running_report() {
  echo -n "Checking MySQL process... "
  mysql_running
  if [[ "$MYSQL_SERVER_RUNNING" == "yes" ]]; then
    echo_success "Running"
  else
    echo_failure "Not running"
  fi
}
mysql_start() {
  if [ -n "$mysqld" ]; then
      $mysqld start
  fi
}
mysql_stop() {
  if [ -n "$mysqld" ]; then
      $mysqld stop
  fi
}

mysql_configure_ca() {
  export mysql_ssl_ca_dir="${1:-/etc/intel/cloudsecurity/mysql-ca}"
  # derive CA settings
  export mysql_ssl_ca_key="${mysql_ssl_ca_dir}/ca.key.pem"
  export mysql_ssl_ca_cert="${mysql_ssl_ca_dir}/ca.cert.pem"
  export mysql_ssl_ca_index="${mysql_ssl_ca_dir}/index"  
}

mysql_configure_ssl() {
  export mysql_ssl_dir="${1:-/etc/intel/cloudsecurity/mysql-ssl}"
}

# Parameters:
# - CA directory where private key, public key, and index is kept
mysql_create_ca() {
  mysql_configure_ca "${1:-$mysql_ssl_ca_dir}"
  # create CA
  if [ -f "${mysql_ssl_ca_key}" ]; then
    echo_warning "CA key already exists"
  else
    echo "Creating MySQL Certificate Authority..."
    mkdir -p "${mysql_ssl_ca_dir}"
    chmod 700 "${mysql_ssl_ca_dir}"
    touch "${mysql_ssl_ca_key}"
    chmod 600 "${mysql_ssl_ca_key}"
    openssl genrsa 2048 > "${mysql_ssl_ca_key}"
    openssl req -new -x509 -nodes -days 3650 -key "${mysql_ssl_ca_key}" -out "${mysql_ssl_ca_cert}" -subj "/CN=MySQL SSL CA/OU=Mt Wilson/O=Intel/C=US/"
    echo 0 > "${mysql_ssl_ca_index}"
  fi
}

# Parameters:
# - SSL request file (input)
# - SSL certificate file (output)
# - SSL CA dir
mysql_ca_sign() {
  local ssl_req="${1}"
  local ssl_cert="${2}"
  mysql_configure_ca "${3:-$mysql_ssl_ca_dir}"
  local prev_index next_index
  if [ -f "${mysql_ssl_ca_index}" ]; then
    prev_index=`cat "${mysql_ssl_ca_index}"`
    ((next_index=prev_index + 1))
  else
    echo_failure "Cannot find MySQL CA"
    return 1
  fi
  openssl x509 -req -in "${ssl_req}" -days 3650 -CA "${mysql_ssl_ca_cert}" -CAkey "${mysql_ssl_ca_key}"  -set_serial "${next_index}" -out "${ssl_cert}"
  echo "${next_index}" > "${mysql_ssl_ca_index}"
}

# Parameters:
# - SSL subject name (goes into the common name field in the certificate)
# - SSL directory where you keep server and client SSL keys and certificates
# - SSL CA directory
# Environment:
# you must have already created the CA key. the CA key information
# should be in the environment variables:
# MTWILSON_CA_KEY=/path/to/file
# MTWILSON_CA_CERT=/path/to/file
# MTWILSON_CA_PASSWORD=password
mysql_create_ssl() {
  local dname="${1}"
  mysql_configure_ssl "${2:-$mysql_ssl_dir}"
  mysql_configure_ca "${3:-$mysql_ssl_ca_dir}"
  echo "Creating MySQL SSL Certificate..."
  mkdir -p "${mysql_ssl_dir}"
  if [ -z "$dname" ]; then
    prompt_with_default MYSQL_SSL_CERT_CN "Common name (username):"
    dname=${MYSQL_SSL_CERT_CN}
  fi
  local filename=`echo "${dname}" | sed "s/[^a-zA-Z0-9-]/_/g"`
  local ssl_key="${mysql_ssl_dir}/${filename}.key.pem"
  local ssl_cert="${mysql_ssl_dir}/${filename}.cert.pem"
  openssl req -newkey rsa:1024 -days 3650 -nodes -keyout "${ssl_key}" -out "${ssl_cert}.req" -subj "/CN=${dname}/OU=Mt Wilson/O=Intel/C=US/"
  openssl rsa -in "${ssl_key}" -out "${ssl_key}"
  mysql_ca_sign "${ssl_cert}.req" "${ssl_cert}" "${mysql_ssl_ca_dir}"
  rm -rf "${ssl_cert}.req"
  # verify the certificate
  echo "Verifying SSL Certificate..."
  openssl verify -CAfile "${mysql_ssl_ca_cert}" "${ssl_cert}"
}

### FUNCTION LIBRARY: postgres


postgres_clear() {
  POSTGRES_HOME=""
  psql=""
  postgres_pghb_conf=""
  postgres_conf=""
  postgres_com=""
}

# Environment:
# - POSTGRES_REQUIRED_VERSION
postgres_version_report() {
  postgres_version
  if [ "$POSTGRES_CLIENT_VERSION_OK" == "yes" ]; then
    echo_success "Postgres client version $POSTGRES_CLIENT_VERSION is ok"
  else
    echo_warning "Postgres client version $POSTGRES_CLIENT_VERSION is not supported, minimum is ${POSTGRES_REQUIRED_VERSION:-$DEFAULT_POSTGRES_REQUIRED_VERSION}"
  fi
}

# Environment:
# - POSTGRES_REQUIRED_VERSION
# installs postgres client programs (not the server)
# we need the postgres client to create or patch the database, but
# the server can be installed anywhere
postgres_install() {
  POSTGRES_CLIENT_YUM_PACKAGES=""
  #POSTGRES_CLIENT_APT_PACKAGES="postgresql-client-common"
  POSTGRES_CLIENT_APT_PACKAGES="postgresql-client-9.3"
  postgres_detect >> $INSTALL_LOG_FILE

  if [[ -z "$POSTGRES_HOME" || -z "$psql" ]]; then
    auto_install "Postgres client" "POSTGRES_CLIENT" >> $INSTALL_LOG_FILE
    postgres_detect >> $INSTALL_LOG_FILE
    if [[ -z "$POSTGRES_HOME" || -z "$psql" ]]; then
      echo_failure "Unable to auto-install Postgres client" | tee -a $INSTALL_LOG_FILE
      echo "Postgres download URL:" >> $INSTALL_LOG_FILE
      echo "http://www.postgresql.org/download/" >> $INSTALL_LOG_FILE
    fi
  else
    echo "Postgres client is already installed" >> $INSTALL_LOG_FILE
    echo "Postgres client is already installed skipping..."
  fi
}

# Checks if postgresql packages need to be added to install application, and adds them
add_postgresql_install_packages() {
  local cprefix=${1}
  local yum_packages=$(eval "echo \$${cprefix}_YUM_PACKAGES")
  local apt_packages=$(eval "echo \$${cprefix}_APT_PACKAGES")
  local yast_packages=$(eval "echo \$${cprefix}_YAST_PACKAGES")
  local zypper_packages=$(eval "echo \$${cprefix}_ZYPPER_PACKAGES")
  
  # detect available package management tools. start with the less likely ones to differentiate.
  yum_detect; yast_detect; zypper_detect; rpm_detect; aptget_detect; dpkg_detect;
  #echo_warning "aptget = $aptget, apt_packages = $apt_packages"
  if [[ -n "$aptget" && -n "$apt_packages" ]]; then
    echo "Checking to see if postgresql package is available for install..."
    pgAddPackRequired=`apt-cache search \`echo $apt_packages | cut -d' ' -f1\``
    #echo_warning "found packages $pgAddPackRequired"
    if [ -z "$pgAddPackRequired" ]; then
      prompt_with_default ADD_POSTGRESQL_REPO "Add \"$apt_packages\" key and packages to local apt repository? " "no"
      if [ "$ADD_POSTGRESQL_REPO" == "no" ]; then
        echo_failure "User declined to add \"$apt_packages\" to local apt repository. Exiting installation..."
        exit -1
      fi
      echo_warning "Adding \"$apt_packages\" package(s) to installer repository..."
      codename=`cat /etc/*-release | grep DISTRIB_CODENAME | sed 's/DISTRIB_CODENAME=//'`
      echo "deb http://apt.postgresql.org/pub/repos/apt/ $codename-pgdg main" >> /etc/apt/sources.list.d/pgdg.list
      # mtwilson-server installer now includes ACCC4CF8.asc and copies it to /etc/apt/trusted.gpg.d
      # so we avoid a download
      #echo "Postgresql apt-key add status: " `wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -`
      apt-key add /etc/apt/trusted.gpg.d/ACCC4CF8.asc
      echo "Running apt-get update; this may take a while..."
      apt-get update >> $INSTALL_LOG_FILE
      #echo "Running apt-get upgrade; this may take a while..."
      #apt-get upgrade -y >> $INSTALL_LOG_FILE
    fi
  #elif [[ -n "$yast" && -n "$yast_packages" ]]; then
    # code goes here
  #elif [[ -n "$yum" && -n "$yum_packages" ]]; then
    # code goes here
  #elif [[ -n "$zypper" && -n "$zypper_packages" ]]; then
    # code goes here
  fi
}

# Environment:
# - POSTGRES_REQUIRED_VERSION
# installs postgres server 
postgres_server_install(){
  POSTGRES_SERVER_YUM_PACKAGES=""
  POSTGRES_SERVER_APT_PACKAGES="postgresql-9.3 pgadmin3 postgresql-contrib-9.3"

  postgres_clear; postgres_server_detect >> $INSTALL_LOG_FILE
  #echo "postgres_server_install postgres_com = $postgres_com"
  if [[ -n "$postgres_com" ]]; then
    echo "Postgres server is already installed" >> $INSTALL_LOG_FILE
    echo "Postgres server is already installed skipping..."
    return;
  fi
  if [[ -z "$postgres_com" ]]; then
    echo "Running postgresql auto install..."
    auto_install "Postgres server" "POSTGRES_SERVER"   >> $INSTALL_LOG_FILE
    postgres_server_detect
  fi
  
  if [[ -z "$postgres_com" ]]; then
    echo_failure "Unable to auto-install Postgres server" | tee -a $INSTALL_LOG_FILE
    echo "Postgres download URL:"  >> $INSTALL_LOG_FILE
    echo "http://www.postgresql.org/download/" >> $INSTALL_LOG_FILE
    return 1
  fi

}

# Environment:
# - POSTGRES_REQUIRED_VERSION
postgres_detect(){
  local min_version="${1:-${POSTGRES_REQUIRED_VERSION:-$DEFAULT_POSTGRES_REQUIRED_VERSION}}"
  if [[ -n "$POSTGRES_HOME" && -n "$psql" && -f "$psql" ]]; then
    echo "postgres detected. returning..."
    return 0
  fi
  psql=`which psql 2>/dev/null`
  export psql
  echo "psql=$psql" >> $INSTALL_LOG_FILE
  
  if [ -e "$psql" ]; then
    POSTGRES_HOME=`dirname "$psql"`
    echo "Found postgres client: $psql" >> $INSTALL_LOG_FILE
    postgres_version ${min_version}
    if [ "$POSTGRES_CLIENT_VERSION_OK" != "yes" ]; then
      echo "postgres client version not ok. resetting psql=''"
      POSTGRES_HOME=''
      psql=""
    fi
  fi
echo "POSTGRES_CLIENT_VERSION_OK: $POSTGRES_CLIENT_VERSION_OK" >> $INSTALL_LOG_FILE
}

# instead of checking separately for pg_hba.conf, postgresql.conf, and /etc/init.d/postgresql
# this is now changed to looking for the postgres server binary and checking its version
# number. then based on its number we look for corresponding pg_hba.conf and postgresql.conf
# files as necessary.  the /etc/init.d/postgresql is always present for all versions.
postgres_server_detect() {
  local min_version="${1:-${POSTGRES_REQUIRED_VERSION:-${DEFAULT_POSTGRES_REQUIRED_VERSION}}}"
  local best_version=""
  local best_version_short=""
  local best_version_bin=""
  # best_version will have a complete version number like 9.1.9
  # best_version_short is the minor version name, for 9.1.9 it would be 9.1
  # best_version_bin is the complete path to the binary like /usr/lib/postgresql/9.1/bin/postgres

  # find candidates like /usr/lib/postgresql/9.1/bin/postgres
  postgres_candidates=`find / -name postgres 2>/dev/null | grep bin`
  for c in $postgres_candidates
  do
    local version_name=`$c --version 2>/dev/null | head -n 1 | awk '{ print $3 }'`
    local bin_dir=`dirname "$c"`
    local version_dir=`dirname "$bin_dir"`
    echo "postgres candidate version=$version_name" >> $INSTALL_LOG_FILE

    if is_version_at_least "$version_name" "$min_version"; then
      echo "Found postgres with version: $version_name" >> $INSTALL_LOG_FILE
      if [[ -z "$best_version" ]]; then
        echo "setting best version $best_version" >> $INSTALL_LOG_FILE
        best_version="$version_name"
        best_version_bin="$c"
        best_version_short=`basename $version_dir`
      elif is_version_at_least "$version_name" "$best_version"; then
        echo "current best version $best_version" >> $INSTALL_LOG_FILE
        best_version="$version_name"
        best_version_bin="$c"
        best_version_short=`basename $version_dir`
      fi
    fi
  done
  if [[ -z "$best_version" ]]; then
    echo_failure "Cannot find postgres version $min_version or later"
    postgres_clear
    return 1
  fi

  # now we have selected a postgres version so set variables accordingly
  echo "Best version of PostgreSQL: $best_version" >> $INSTALL_LOG_FILE
  POSTGRES_SERVER_VERSION="$best_version"
  POSTGRES_SERVER_BIN="$best_version_bin"  
  POSTGRES_SERVER_VERSION_SHORT="$best_version_short"

  echo "server version $POSTGRES_SERVER_VERSION" >> $INSTALL_LOG_FILE
  postgresql_installed=$(which psql 2>/dev/null)
  if [ -n "$postgresql_installed" ]; then
    postgres_com="service postgresql"
  fi

  postgres_pghb_conf=`find / -name pg_hba.conf 2>/dev/null | grep $best_version_short | head -n 1`
  postgres_conf=`find / -name postgresql.conf 2>/dev/null | grep $best_version_short | head -n 1`
  # if we run into a system where postgresql is organized differently we may need to check if these don't exist and try looking without the version number
  echo "postgres_pghb_conf=$postgres_pghb_conf" >> $INSTALL_LOG_FILE
  echo "postgres_conf=$postgres_conf" >> $INSTALL_LOG_FILE
  echo "postgres_com=$postgres_com" >> $INSTALL_LOG_FILE
  return 0
}
postgres_version(){
  local min_version="${1:-${POSTGRES_REQUIRED_VERSION:-$DEFAULT_POSTGRES_REQUIRED_VERSION}}"
  POSTGRES_CLIENT_VERSION=""
  POSTGRES_CLIENT_VERSION_OK=""

  if [ -n "$psql" ]; then
    POSTGRES_CLIENT_VERSION=`$psql --version |  head -n1 | awk '{print $3}'`
    echo "POSTGRES_CLIENT_VERSION: $POSTGRES_CLIENT_VERSION" >> $INSTALL_LOG_FILE
    if is_version_at_least "$POSTGRES_CLIENT_VERSION" "${min_version}"; then
      POSTGRES_CLIENT_VERSION_OK=yes
    else
      POSTGRES_CLIENT_VERSION_OK=no
    fi
  fi
  echo "POSTGRES_CLIENT_VERSION_OK: $POSTGRES_CLIENT_VERSION_OK" >> $INSTALL_LOG_FILE
}

# must load from config file or call postgres_detect prior to calling this function
postgres_env_report() {
  echo "psql=$psql" >> $INSTALL_LOG_FILE
}

# Environment:
# - POSTGRES_REQUIRED_VERSION
postgres_require() {
  local min_version="${1:-${POSTGRES_REQUIRED_VERSION:-$DEFAULT_POSTGRES_REQUIRED_VERSION}}"
  if [[ -z "$POSTGRES_HOME" || -z "$psql" || ! -f "$psql" ]]; then
    postgres_detect ${min_version} > /dev/null
  fi
  if [[ -z "$POSTGRES_HOME" || -z "$psql" || ! -f "$psql" ]]; then
    echo "Cannot find Postgres client version $min_version or later"
    #exit 1
  fi
}


# Environment:
# - POSTGRES_REQUIRED_VERSION\
# format like this -> psql -h 127.0.0.1 -p 5432 -d mw_as -U root -c "\l"
postgres_connection() {
  postgres_require
  postgres_connect="$psql -h ${POSTGRES_HOSTNAME:-$DEFAULT_POSTGRES_HOSTNAME} -p ${POSTGRES_PORTNUM:-$DEFAULT_POSTGRES_PORTNUM} -d ${POSTGRES_DATABASE:-$DEFAULT_POSTGRES_DATABASE} -U ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME}"
  echo "postgres_connect=$postgres_connect" >> $INSTALL_LOG_FILE
}

# Environment:
# - POSTGRES_REQUIRED_VERSION
# sets the is_postgres_available variable to "yes" or ""
postgres_test_connection() {
  postgres_connection
  is_postgres_available=""

  #check if postgres is installed and we can connect with provided credencials

  $psql -h ${POSTGRES_HOSTNAME:-$DEFAULT_POSTGRES_HOSTNAME} -p ${POSTGRES_PORTNUM:-$DEFAULT_POSTGRES_PORTNUM} -d ${POSTGRES_DATABASE:-$DEFAULT_POSTGRES_DATABASE} -U ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME} -w -c "select 1" 2>/tmp/intel.postgres.err >/dev/nulll
   if [ $? -eq 0 ]; then
    is_postgres_available="yes"
    return 0
  fi
  postgres_connection_error=`cat /tmp/intel.postgres.err`
  
  #echo "postgres_connection_error: $postgres_connection_error"
  #rm -f /tmp/intel.postgres.err

  return 1

}

# Environment:
# - POSTGRES_REQUIRED_VERSION
postgres_test_connection_report() {
  echo -n "Testing database connection... "
  postgres_test_connection
  if [ -n "$is_postgres_available" ]; then
    echo "OK"
  else
    echo "FAILED"
    echo_failure "${postgres_connection_error}"
  fi
}

# responsible for ensuring that the connection properties in the config file
# Call this from the control script such as "asctl" before calling the other postgres_* functions
# Parameters:
# - absolute path to configuration file
# - prefix of psql property file names (java style, dot is added automatically)
# Environment:
# - script_name such as 'asctl' or 'wlmctl'
# - intel_conf_dir (deprecated, just use absolute package_config_filename)
# - package_config_filename  (should be absolute)
postgres_configure_connection() {
    local config_file="${1:-/etc/intel/cloudsecurity/postgres.properties}"
    local prefix="${2:-postgres}"
    postgres_test_connection
    if [ -z "$is_postgres_available" ]; then
      #postgres_read_connection_properties "${config_file}" "${prefix}"
      postgres_test_connection
    fi
    while [ -n "$postgres_connection_error" ]
    do
      echo_warning "Cannot connect to Postgres: $postgres_connection_error"
      prompt_yes_no POSTGRES_RETRY_CONFIGURE_AFTER_FAILURE "Do you want to configure it now?"
      if [[ "no" == "$POSTGRES_RETRY_CONFIGURE_AFTER_FAILURE" ]]; then
        echo "Postgres settings are in ${package_config_filename}"
        echo "Run '${script_name} setup' after configuring to continue."
        return 1
      fi
      postgres_userinput_connection_properties
      postgres_test_connection
    done
      echo_success "Connected to database [${POSTGRES_DATABASE}] on ${POSTGRES_HOSTNAME}" >> $INSTALL_LOG_FILE
#      local should_save
#      prompt_yes_no should_save "Save in ${package_config_filename}?"
#      if [[ "yes" == "${should_save}" ]]; then
      postgres_write_connection_properties "${config_file}" "${prefix}"
#      fi
}


# requires a postgres connection that can access the existing database, OR (if it doesn't exist)
# requires a postgres connection that can create databases and grant privileges
# call postgres_configure_connection before calling this function
postgres_create_database() {
if postgres_server_detect ; then
  #we first need to find if the user has specified a different port than the once currently configured for postgres
  if [ -n "$postgres_conf" ]; then
    current_port=`grep "port =" $postgres_conf | awk '{print $3}'`
    has_correct_port=`grep $POSTGRES_PORTNUM $postgres_conf`
    if [ -z "$has_correct_port" ]; then
      echo "Port needs to be reconfigured from $current_port to $POSTGRES_PORTNUM"
      sed -i s/$current_port/$POSTGRES_PORTNUM/g $postgres_conf 
      echo "Restarting PostgreSQL for port change update to take effect."
      postgres_restart >> $INSTALL_LOG_FILE
      sleep 10
    fi
  else
    echo "warning: postgresql.conf not found" >> $INSTALL_LOG_FILE
  fi
	
  postgres_test_connection
  if [ -n "$is_postgres_available" ]; then
    #echo_success "Database [${POSTGRES_DATABASE}] already exists"
    echo_success "Database [${POSTGRES_DATABASE}] already exists"   >> $INSTALL_LOG_FILE
    return 0
  else
    echo "Creating database..."
    echo "Creating database..."    >> $INSTALL_LOG_FILE
    local create_user_sql="CREATE USER ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME} WITH PASSWORD '${POSTGRES_PASSWORD:-$DEFAULT_POSTGRES_PASSWORD}';"
    sudo -u postgres psql postgres -c "${create_user_sql}" 2>/tmp/intel.postgres.err    >> $INSTALL_LOG_FILE
    local superuser_sql="ALTER USER ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME} WITH SUPERUSER;"
    sudo -u postgres psql postgres -c "${superuser_sql}" 2>/tmp/intel.postgres.err    >> $INSTALL_LOG_FILE
    local create_sql="CREATE DATABASE ${POSTGRES_DATABASE:-$DEFAULT_POSTGRES_DATABASE};"
    local grant_sql="GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DATABASE:-$DEFAULT_POSTGRES_DATABASE} TO ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME};"
    sudo -u postgres psql postgres -c "${create_sql}" 2>/tmp/intel.postgres.err    >> $INSTALL_LOG_FILE
    postgres_connection_error=`cat /tmp/intel.postgres.err`
    echo "postgres_connection_error: $postgres_connection_error" >> $INSTALL_LOG_FILE
    sudo -u postgres psql postgres -c "${grant_sql}" 2>/tmp/intel.postgres.err    >> $INSTALL_LOG_FILE
    postgres_connection_error=`cat /tmp/intel.postgres.err`
    echo "postgres_connection_error: $postgres_connection_error" >> $INSTALL_LOG_FILE

	if [ -n "$postgres_pghb_conf" ]; then 
      has_host=`grep "^host" $postgres_pghb_conf | grep "127.0.0.1" | grep -E "password|trust"`
      if [ -z "$has_host" ]; then
        echo host  all  all  127.0.0.1/32  password >> $postgres_pghb_conf
      fi
	else
	  echo "warning: pg_hba.conf not found" >> $INSTALL_LOG_FILE
    fi
	
	if [ -n "$postgres_conf" ]; then
      has_listen_addresses=`grep "^listen_addresses" $postgres_conf`
      if [ -z "$has_listen_addresses" ]; then
         echo listen_addresses=\'127.0.0.1\' >> $postgres_conf
      fi
    else
	  echo "warning: postgresql.conf not found" >> $INSTALL_LOG_FILE
	fi
	
    postgres_restart >> $INSTALL_LOG_FILE
    sleep 10
    postgres_test_connection

    if [ -z "$is_postgres_available" ]; then
      echo_failure "Failed to create database."  | tee -a $INSTALL_LOG_FILE
      echo "Try to execute the following commands on the database:"  >> $INSTALL_LOG_FILE
      echo "${create_sql}" >> $INSTALL_LOG_FILE
      echo "${grant_sql}"  >> $INSTALL_LOG_FILE
      return 1
    fi
  fi
fi
}

# before using this function, you must first set the connection variables postgres_*
# example:  postgres_run_script /path/to/statements.sql
postgres_run_script() {
  local scriptfile="${1}"
  local datestr=`date +%Y-%m-%d.%H%M`
  echo "##### [${datestr}] Script file: ${scriptfile}" >> ${postgres_setup_log}
  $postgres_connect --force ${POSTGRES_DATABASE} < "${scriptfile}" 2>> ${postgres_setup_log}
}

# requires a postgres connection that can create tables and procedures inside an existing database.
# depends on postgres_* variables for the connection information.
# call postgres_configure_connection before calling this function.
# Parameters: a list of sql files to execute (absolute paths)
postgres_install_scripts() {
  local scriptlist="$@"
  postgresd_test_connection
  if [ -n "$is_postgres_available" ]; then
    echo "Connected to ${POSTGRES_HOSTNAME} as ${POSTGRES_USERNAME}. Executing script..."
    for scriptname in $scriptlist
    do
        postgres_run_script $scriptname
    done
    return 0
  else
    echo_failure "Cannot connect to database."
    return 1
  fi
}

postgres_running() {  
  POSTGRES_SERVER_RUNNING=''
  if [ -n "$postgres_com" ]; then
    local is_running=`$postgres_com status | grep online`
    if [ -n "$is_running" ]; then
      POSTGRES_SERVER_RUNNING=yes
    fi
  fi
}

postgres_running_report() {
  echo -n "Checking Postgres process... "
  postgres_running
  if [[ "$POSTGRES_SERVER_RUNNING" == "yes" ]]; then
    echo_success "Running"
  else
    echo_failure "Not running"
  fi
}
postgres_restart() {
  if [ -n "$postgres_com" ]; then
      $postgres_com restart
  fi
}
postgres_start() {
  if [ -n "$postgres_com" ]; then
      $postgres_com start
  fi
}
postgres_stop() {
  if [ -n "$postgres_com" ]; then
      $postgres_com stop
  fi
}

postgres_configure_ca() {
  export postgres_ssl_ca_dir="${1:-/etc/intel/cloudsecurity/postgres-ca}"
  # derive CA settings
  export postgres_ssl_ca_key="${postgres_ssl_ca_dir}/ca.key.pem"
  export postgres_ssl_ca_cert="${postgres_ssl_ca_dir}/ca.cert.pem"
  export postgres_ssl_ca_index="${postgres_ssl_ca_dir}/index"  
}

postgres_configure_ssl() {
  export postgres_ssl_dir="${1:-/etc/intel/cloudsecurity/postgres-ssl}"
}

# Parameters:
# - CA directory where private key, public key, and index is kept
postgres_create_ca() {
  postgres_configure_ca "${1:-$postgres_ssl_ca_dir}"
  # create CA
  if [ -f "${postgres_ssl_ca_key}" ]; then
    echo_warning "CA key already exists"
  else
    echo "Creating Postgres Certificate Authority..."
    mkdir -p "${postgres_ssl_ca_dir}"
    chmod 700 "${postgres_ssl_ca_dir}"
    touch "${postgres_ssl_ca_key}"
    chmod 600 "${postgres_ssl_ca_key}"
    openssl genrsa 2048 > "${postgres_ssl_ca_key}"
    openssl req -new -x509 -nodes -days 3650 -key "${postgres_ssl_ca_key}" -out "${postgres_ssl_ca_cert}" -subj "/CN=Posgres SSL CA/OU=Mt Wilson/O=Intel/C=US/"
    echo 0 > "${postgres_ssl_ca_index}"
  fi
}

# Parameters:
# - SSL request file (input)
# - SSL certificate file (output)
# - SSL CA dir
postgres_ca_sign() {
  local ssl_req="${1}"
  local ssl_cert="${2}"
  postgres_configure_ca "${3:-$postgres_ssl_ca_dir}"
  local prev_index next_index
  if [ -f "${postgres_ssl_ca_index}" ]; then
    prev_index=`cat "${postgres_ssl_ca_index}"`
    ((next_index=prev_index + 1))
  else
    echo_failure "Cannot find Postgres CA"
    return 1
  fi
  openssl x509 -req -in "${ssl_req}" -days 3650 -CA "${postgres_ssl_ca_cert}" -CAkey "${postgres_ssl_ca_key}"  -set_serial "${next_index}" -out "${ssl_cert}"
  echo "${next_index}" > "${postgres_ssl_ca_index}"
}

# Parameters:
# - SSL subject name (goes into the common name field in the certificate)
# - SSL directory where you keep server and client SSL keys and certificates
# - SSL CA directory
# Environment:
# you must have already created the CA key. the CA key information
# should be in the environment variables:
# MTWILSON_CA_KEY=/path/to/file
# MTWILSON_CA_CERT=/path/to/file
# MTWILSON_CA_PASSWORD=password
postgres_create_ssl() {
  local dname="${1}"
  postgres_configure_ssl "${2:-$postgres_ssl_dir}"
  postgres_configure_ca "${3:-$postgres_ssl_ca_dir}"
  echo "Creating Postgres SSL Certificate..."
  mkdir -p "${postgres_ssl_dir}"
  if [ -z "$dname" ]; then
    prompt_with_default POSTGRES_SSL_CERT_CN "Common name (username):"
    dname=${POSTGRES_SSL_CERT_CN}
  fi
  local filename=`echo "${dname}" | sed "s/[^a-zA-Z0-9-]/_/g"`
  local ssl_key="${postgres_ssl_dir}/${filename}.key.pem"
  local ssl_cert="${postgres_ssl_dir}/${filename}.cert.pem"
  openssl req -newkey rsa:1024 -days 3650 -nodes -keyout "${ssl_key}" -out "${ssl_cert}.req" -subj "/CN=${dname}/OU=Mt Wilson/O=Intel/C=US/"
  openssl rsa -in "${ssl_key}" -out "${ssl_key}"
  postgres_ca_sign "${ssl_cert}.req" "${ssl_cert}" "${postgres_ssl_ca_dir}"
  rm -rf "${ssl_cert}.req"
  # verify the certificate
  echo "Verifying SSL Certificate..."
  openssl verify -CAfile "${postgres_ssl_ca_cert}" "${ssl_cert}"
}


### FUNCTION LIBRARY: glassfish

glassfish_clear() {
  GLASSFISH_HOME=""
  glassfish_bin=""
  glassfish=""
}

glassfish_ready_report() {
  if [[ -z "$GLASSFISH_HOME" ]]; then echo_warning "GLASSFISH_HOME variable is not set"; return 1; fi
  if [[ -z "$glassfish_bin" ]]; then echo_warning "Glassfish binary path is not set"; return 1; fi
  if [[ ! -f "$glassfish_bin" ]]; then echo_warning "Cannot find Glassfish binary at $glassfish_bin"; return 1; fi
  if [[ -z "$glassfish" ]]; then echo_warning "Glassfish command is not set"; return 1; fi
  echo_success "Using Glassfish at $GLASSFISH_HOME"
  return 0
}


glassfish_ready() {
  glassfish_ready_report > /dev/null
  return $?
}

# How to use;   GLASSFISH_VERSION=`glassfish_version`
# If you pass a parameter, it is the path to a glassfish "asadmin" binary 
# If you do not pass a parameter, the "glassfish" variable is used as the path to the binary
glassfish_version() {

  if [[ -z $JAVA_HOME && -z $JRE_HOME ]]; then java_detect; fi
  if [[ -z $JAVA_HOME && -z $JRE_HOME ]]; then return 1; fi

  if [[ -n "$glassfish" ]]; then
    # extract the version number from a string like: glassfish version "3.0"
    local current_glassfish_version=`$glassfish version 2>&1 | grep -i glassfish | grep -i version | awk '{ print $8 }'`
    if [ -n "$current_glassfish_version" ]; then
      echo $current_glassfish_version
      return 0
    fi
    return 2
  fi
  return 1
}

# Environment:
# - glassfish_required_version
glassfish_version_report() {
  local min_version="${1:-${GLASSFISH_REQUIRED_VERSION:-$DEFAULT_GLASSFISH_REQUIRED_VERSION}}"
  GLASSFISH_VERSION=`glassfish_version`
  if is_version_at_least "$GLASSFISH_VERSION" "${min_version}"; then
    echo_success "Glassfish version $GLASSFISH_VERSION is ok"
    return 0
  else
    echo_warning "Glassfish version $GLASSFISH_VERSION is not supported, minimum is ${min_version}"
    return 1
  fi
}

# detects possible glassfish installations
# does nothing if GLASSFISH_HOME is already set; unset with glassfish_clear before calling to force detection
# Environment:
# - GLASSFISH_REQUIRED_VERSION (or provide it as a parameter)
# Parameters:
# - minimum required version
glassfish_detect() {
  local min_version="${1:-${GLASSFISH_REQUIRED_VERSION:-${DEFAULT_GLASSFISH_REQUIRED_VERSION}}}"
  if [[ (-z $JAVA_HOME && -z $JRE_HOME) || -z $java ]]; then java_detect; fi
  if [[ (-z $JAVA_HOME && -z $JRE_HOME) || -z $java ]]; then return 1; fi

      if [[ -n "$java" ]]; then    
        local java_bindir=`dirname "$java"`
      fi
  # start with GLASSFISH_HOME if it is already configured
  if [[ -n "$GLASSFISH_HOME" ]]; then
    if [[ -z "$glassfish_bin" ]]; then
      glassfish_bin="$GLASSFISH_HOME/bin/asadmin"
    fi
    if [[ -z "$glassfish" ]]; then
      if [[ -n "$java" ]]; then    
        # the glassfish admin tool read timeout is in milliseconds, so 900,000 is 900 seconds
        glassfish="env PATH=$java_bindir:$PATH AS_ADMIN_READTIMEOUT=900000 $glassfish_bin"
        if [ -f "$GLASSFISH_HOME/config/admin.passwd" ] && [ -f "$GLASSFISH_HOME/config/admin.user" ]; then
          gfuser=`cat $GLASSFISH_HOME/config/admin.user | cut -d'=' -f2`
          if [ -n "$gfuser" ]; then
            glassfish+=" --user=$gfuser --passwordfile=$GLASSFISH_HOME/config/admin.passwd"
          fi
        fi
      else
        glassfish="env AS_ADMIN_READTIMEOUT=900000 $glassfish_bin"
        if [ -f "$GLASSFISH_HOME/config/admin.passwd" ] && [ -f "$GLASSFISH_HOME/config/admin.user" ]; then
          gfuser=`cat $GLASSFISH_HOME/config/admin.user | cut -d'=' -f2`
          if [ -n "$gfuser" ]; then
            glassfish+=" --user=$gfuser --passwordfile=$GLASSFISH_HOME/config/admin.passwd"
          fi
        fi
      fi
    fi
    if [[ -n "$glassfish" ]]; then
      GLASSFISH_VERSION=`glassfish_version`
      if is_version_at_least "$GLASSFISH_VERSION" "${min_version}"; then
        return 0
      fi
    fi
  fi

  GLASSFISH_CANDIDATES=`find / -name domains 2>/dev/null | grep glassfish/domains`
#  echo "Candidates: $GLASSFISH_CANDIDATES"
  for c in $GLASSFISH_CANDIDATES
  do
      local parent=`dirname "$c"`
 #     echo "Checking Glassfish: $parent"
      if [ -f "$parent/bin/asadmin" ]; then
        GLASSFISH_HOME="$parent"
        glassfish_bin="$GLASSFISH_HOME/bin/asadmin"
        # the glassfish admin tool read timeout is in milliseconds, so 900,000 is 900 seconds
        glassfish="env PATH=$java_bindir:$PATH AS_ADMIN_READTIMEOUT=900000 $glassfish_bin"
        if [ -f "$GLASSFISH_HOME/config/admin.passwd" ] && [ -f "$GLASSFISH_HOME/config/admin.user" ]; then
          gfuser=`cat $GLASSFISH_HOME/config/admin.user | cut -d'=' -f2`
          if [ -n "$gfuser" ]; then
            glassfish+=" --user=$gfuser --passwordfile=$GLASSFISH_HOME/config/admin.passwd"
          fi
        fi
        echo "Found Glassfish: $GLASSFISH_HOME"
#        echo "Found Glassfish: $glassfish"
        GLASSFISH_VERSION=`glassfish_version`
        if is_version_at_least "$GLASSFISH_VERSION" "${min_version}"; then
          return 0
        fi
      fi
  done
  echo_failure "Cannot find Glassfish"
  glassfish_clear
  return 1
  # read the admin username and pasword, if present. format of both files is shell  VARIABLE=VALUE
#  if [ -f /etc/glassfish/admin.user ]; then
#    export AS_ADMIN_USER=`read_property_from_file AS_ADMIN_USER /etc/glassfish/admin.user`
#  fi
#  if [ -f /etc/glassfish/admin.passwd ]; then
#    export AS_ADMIN_PASSWORDFILE=/etc/glassfish/admin.passwd
#  fi
}

# must load from config file or call glassfish_detect prior to calling this function
glassfish_env_report() {
  echo "GLASSFISH_HOME=$GLASSFISH_HOME"
  echo "glassfish_bin=$glassfish_bin"
  echo "glassfish=\"$glassfish\""
}


# Environment:
# - glassfish_required_version (or provide it as a parameter)
glassfish_require() {
  local min_version="${1:-${GLASSFISH_REQUIRED_VERSION:-${DEFAULT_GLASSFISH_REQUIRED_VERSION}}}"
  if not glassfish_ready; then
    glassfish_detect ${min_version} > /dev/null
  fi
  if not glassfish_ready; then
    echo_failure "Cannot find Glassfish server version $min_version or later"
    exit 1
  fi
}

# usage:  if no_glassfish 3.0; then echo_failure "Cannot find Glassfish"; exit 1; fi
no_glassfish() {
  if glassfish_require $1; then return 1; else return 0; fi
}

# Run this AFTER glassfish_install
# optional global variables:  
#   glassfish_username (default value glassfish)
#   GLASSFISH_HOME (default value /usr/share/glassfish4)
# works on Debian, Ubuntu, CentOS, RedHat, SUSE
# Username should not contain any spaces or punctuation
# Optional arguments:  one or more directories for glassfish user to own
glassfish_permissions() {
  local chown_locations="$@"
  local username=${glassfish_username:-glassfish}
  local user_exists=`cat /etc/passwd | grep "^${username}"`
  if [ -z "$user_exists" ]; then
    useradd -c "Glassfish" -d "${GLASSFISH_HOME:-/usr/share/glassfish4}" -r -s /bin/bash "$username"
  fi
  local file
  for file in $chown_locations
  do
    if [[ -n "$file" && -e "$file" ]]; then
      chown -R "${username}:${username}" "$file"
    fi
  done
}

# sets a system property for logback configuration file location
# requires a running glassfish
glassfish_logback() {
  # see if it's already set
  local prev_logback=`$glassfish list-system-properties 2>/dev/null | grep "logback.configurationFile"| head -n 1`
  # loop just in case there is more than one defined
#  while [ -n "${prev_logback}" ]
#  do
    echo "Deleting existing system property ${prev_logback}"
    $glassfish delete-system-property "${prev_logback}" 2>/dev/null >/dev/null
#    prev_logback=`$glassfish list-system-properties 2>/dev/null | grep "logback.configurationFile" | head -n 1`
#  done
  $glassfish create-system-properties logback.configurationFile=/etc/intel/cloudsecurity/logback.xml
}

# set the -Xmx and -XX:MaxPermSize memory parameters for the glassfish JVM
glassfish_memory() {
  local jvm_memory="${1:-2048}"
  local jvm_maxperm="${2:-512}"
  # glassfish must be started in order to do this
  
  # first we have to find the current options and remove them
  local prev_jvm_memory=`$glassfish list-jvm-options | grep "\-Xmx" | head -n 1`
  local prev_jvm_maxperm=`$glassfish list-jvm-options | grep "\-XX:MaxPermSize" | head -n 1`
  # loop just in case there is more than one defined
  while [ -n "${prev_jvm_memory}" ]
  do
    echo "Deleting existing option ${prev_jvm_memory}"
    $glassfish delete-jvm-options "${prev_jvm_memory}"
    prev_jvm_memory=`$glassfish list-jvm-options | grep "\-Xmx" | head -n 1`
  done
  # loop just in case there is more than one defined
  while [ -n "${prev_jvm_maxperm}" ]
  do
    # must escape the colon between XX and MaxPermSize
    prev_jvm_maxperm=`echo ${prev_jvm_maxperm} | sed -re "s/:/\\\\\\\\:/"`
    echo "Deleting existing option ${prev_jvm_maxperm}"
    $glassfish delete-jvm-options "${prev_jvm_maxperm}"
    prev_jvm_maxperm=`$glassfish list-jvm-options | grep "\-XX:MaxPermSize" | head -n 1`
  done
  $glassfish create-jvm-options "-Xmx${jvm_memory}m:-XX\\:MaxPermSize=${jvm_maxperm}m"
}

# reset glassfish overall logging handler to turn on logging
# this is required because glassfish 3.1.1 and later...
#  UI has a bug that causes the
# logging handler to be set to OFF whenever a user saves any change to
# other logging levels. 
# references:
# http://java.net/jira/browse/GLASSFISH-17037
# http://stackoverflow.com/questions/9373629/glassfish-3-1-1-suddenly-stopped-writing-to-server-log
glassfish_enable_logging() {
  $glassfish set-log-levels com.sun.enterprise.server.logging.GFFileHandler=ALL
}

# must restart glassfish for enable-secure-admin and memory options to take effect, so call these after calling this function:
#  glassfish_stop
#  glassfish_start
# (they are not done automatically in case the caller has other glassfish setup that would also require a restart)
# Environment:
# - glassfish_required_version
glassfish_install() {
  GLASSFISH_HOME=""
  glassfish=""
  local GLASSFISH_PACKAGE="${1:-glassfish.zip}"
  GLASSFISH_YUM_PACKAGES="unzip"
  GLASSFISH_APT_PACKAGES="unzip"
  GLASSFISH_YAST_PACKAGES="unzip"
  GLASSFISH_ZYPPER_PACKAGES="unzip"
  glassfish_detect

  if glassfish_running; then glassfish_stop; fi

  if [[ -z "$GLASSFISH_HOME" || -z "$glassfish" ]]; then
    if [ -d /usr/share/glassfish4 ]; then
      # we do not remove it automatically in case there are applications or data in there that the user wants to save!!
      echo_warning "Glassfish not detected but /usr/share/glassfish4 exists"
      echo "Remove /usr/share/glassfish4 and try again"
      return 1
    fi
    if [[ -z "$GLASSFISH_PACKAGE" || ! -f "$GLASSFISH_PACKAGE" ]]; then
      echo_failure "Missing Glassfish installer: $GLASSFISH_PACKAGE"
      return 1
    fi
    auto_install "Glassfish requirements" "GLASSFISH"
    echo "Installing $GLASSFISH_PACKAGE"
    unzip $GLASSFISH_PACKAGE 2>&1  >/dev/null
    if [ -d "glassfish4" ]; then
      if [ -d "/usr/share/glassfish4" ]; then
        echo "Glassfish already installed at /usr/share/glassfish4"
        export GLASSFISH_HOME="/usr/share/glassfish4"
      else
        mv glassfish4 /usr/share && export GLASSFISH_HOME="/usr/share/glassfish4"
      fi
    fi


    # Glassfish requires hostname to be mapped to 127.0.0.1 in /etc/hosts
    if [ -f "/etc/hosts" ]; then
        local hostname=`hostname`
        local found=`cat "/etc/hosts" | grep "^127.0.0.1" | grep "$hostname"`
        if [ -z "$found" ]; then
          local datestr=`date +%Y-%m-%d.%H%M`
          cp /etc/hosts /etc/hosts.${datestr}
          local updated=`sed -re "s/^(127.0.0.1\s.*)$/\1 ${hostname}/" /etc/hosts`
          echo "$updated" > /etc/hosts
        fi
    fi
    glassfish_detect
    if [[ -z "$GLASSFISH_HOME" || -z "$glassfish" ]]; then
      echo_failure "Unable to auto-install Glassfish"
      echo "Glassfish download URL:"
      echo "http://glassfish.java.net/"
      return 1
    fi
  else
    echo "Glassfish is already installed in $GLASSFISH_HOME"
  fi

  #if [ -n "${MTWILSON_SERVER}" ]; then
  #  glassfish_create_ssl_cert "${MTWILSON_SERVER}"
  #else
  #  glassfish_create_ssl_cert_prompt
  #fi

  glassfish_permissions "${GLASSFISH_HOME}"
  sleep 5
  glassfish_start
  #glassfish_admin_user
  glassfish_memory 2048 512
  glassfish_logback
  
  # set JAVA_HOME for glassfish
  asenvFile=`find "$GLASSFISH_HOME" -name asenv.conf`
  if [ -n "$asenvFile" ]; then
    if [ -f "$asenvFile" ] && ! grep -q "AS_JAVA=" "$asenvFile"; then
      echo "AS_JAVA=$JAVA_HOME" >> "$asenvFile"
    fi
  else
    echo "warning: asenv.conf not found" >> $INSTALL_LOG_FILE
  fi
  
  echo "Increasing glassfish max thread pool size to 200..."
  $glassfish set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200
}

# glassfish must already be running to execute "enable-secure-domain",  so glassfish_start is required before calling this function
glassfish_admin_user() {  
  #echo "You must choose an administrator username and password for Glassfish"
  echo "The Glassfish control panel is at https://${MTWILSON_SERVER:-127.0.0.1}:4848"
  #prompt_with_default AS_ADMIN_USER "Glassfish admin username:" ${WEBSERVICE_USERNAME}
  #prompt_with_default_password AS_ADMIN_PASSWORD "Glassfish admin password:" ${WEBSERVICE_PASSWORD}

  expect=`which expect`  
  glassfish_detect

  GF_CONFIG_PATH="$GLASSFISH_HOME/config"
  export AS_ADMIN_USER=$WEBSERVICE_USERNAME
  export AS_ADMIN_PASSWORD=$WEBSERVICE_PASSWORD
  export AS_ADMIN_PASSWORD_OLD=`cat $GF_CONFIG_PATH/admin.passwd 2>/dev/null | head -n 1 | cut -d'=' -f2`
  export AS_ADMIN_PASSWORDFILE=$GF_CONFIG_PATH/admin.passwd
  
#  if [ ! -f $GF_CONFIG_PATH/admin.user ]; then
    echo "AS_ADMIN_USER=${AS_ADMIN_USER}" > $GF_CONFIG_PATH/admin.user
#  fi
  
#  if [ ! -f $GF_CONFIG_PATH/admin.passwd ]; then
    echo "AS_ADMIN_PASSWORD=${AS_ADMIN_PASSWORD_OLD}" > $GF_CONFIG_PATH/admin.passwd
    echo "AS_ADMIN_NEWPASSWORD=${AS_ADMIN_PASSWORD}" >> $GF_CONFIG_PATH/admin.passwd
#  fi

#  if [ ! -f $GF_CONFIG_PATH/admin.passwd.old ]; then
    echo "AS_ADMIN_PASSWORD=${AS_ADMIN_PASSWORD_OLD}" > $GF_CONFIG_PATH/admin.passwd.old
#  fi

  chmod 600 $GF_CONFIG_PATH/admin.user $GF_CONFIG_PATH/admin.passwd $GF_CONFIG_PATH/admin.passwd.old
  #echo "AS_ADMIN_MASTERPASSWORD=changeit" >> /etc/glassfish/admin.passwd

  #echo "Glassfish will now ask you for the same information:"
  # $glassfish is an alias for full path of asadmin
  
#(
#$expect << EOD
#spawn $glassfish --user=$WEBSERVICE_USERNAME --passwordfile=$GF_CONFIG_PATH/admin.passwd.old change-admin-password
#expect "Enter the new admin password>"
#send "$WEBSERVICE_PASSWORD\r"
#expect "Enter the new admin password again>"
#send "$WEBSERVICE_PASSWORD\r"
#interact
#expect eof
#EOD
#) > /dev/null 2>&1

  # needed in case glassfish_detect has already added --user and --passwordfile options
  changeAdminPassOptions=
  if [[ "$glassfish" != *"--user="* ]]; then
    changeAdminPassOptions+=" --user=$AS_ADMIN_USER"
  fi
  if [[ "$glassfish" != *"--passwordfile="* ]]; then
    changeAdminPassOptions+=" --passwordfile=$GF_CONFIG_PATH/admin.passwd"
  fi
  $glassfish $changeAdminPassOptions change-admin-password     # no quotes; command doesn't handle well

  # set the password file appropriately for further reads
  echo "AS_ADMIN_PASSWORD=${AS_ADMIN_PASSWORD}" > $GF_CONFIG_PATH/admin.passwd
  glassfish_detect
  $glassfish enable-secure-admin
  $glassfish restart-domain domain1
}

# pre-conditions:   GLASSFISH_HOME  must be set  (find it with glassfish_detect)
# returns success (0) if glassfish is running, error (1) if it is not running
# in order to prevent repetitive calls it also sets the GLASSFISH_RUNNING variable.
# if glassfish is running, it also sets the $GLASSFISH_PID variable to the process id.
# so you can write  if glassfish_running; echo "ok"; fi
# and after that also   if [ "$GLASSFISH_RUNNING" == "yes" ]; then echo "ok"; fi
glassfish_running() {  
  GLASSFISH_RUNNING=''
  if [ -z "$GLASSFISH_HOME" ]; then
    glassfish_detect 2>&1 > /dev/null
  fi
  if [ -n "$GLASSFISH_HOME" ]; then
    GLASSFISH_PID=`ps gauwxx | grep java | grep -v grep | grep "$GLASSFISH_HOME" | awk '{ print $2 }'`
    if [ -n "$GLASSFISH_PID" ]; then
      GLASSFISH_RUNNING=yes
      return 0
    fi
  fi
  return 1
}

# you should call glassfish_clear and glassfish_detect before calling this
# if you don't already have a $glassfish variable with the glassfish admin password
glassfish_running_report() {
  echo -n "Checking Glassfish process... "
  if glassfish_running; then
    echo_success "Running (pid $GLASSFISH_PID)"
  else
    echo_failure "Not running"
  fi
}
glassfish_start() {
  glassfish_require 2>&1 > /dev/null
  if glassfish_running; then
    echo_warning "Glassfish already running [PID: $GLASSFISH_PID]"
  elif [ -n "$glassfish" ]; then
    echo -n "Waiting for Glassfish services to startup..."
    ($glassfish start-domain) 2>&1 > /dev/null #NOT in background, takes some time to start, and will report a running pid in the interim
    while ! glassfish_running; do
      sleep 1
    done
    echo_success " Done"
  fi
}
glassfish_shutdown() {
  glassfish_running
  if [ -n "$GLASSFISH_PID" ]; then
    kill -9 $GLASSFISH_PID
  fi
}
glassfish_stop() {
  glassfish_require 2>&1 > /dev/null
  if ! glassfish_running; then
    echo_warning "Glassfish already stopped"
  elif [ -n "$glassfish" ]; then
    echo -n "Waiting for Glassfish services to shutdown..."
    ($glassfish stop-domain &) 2>&1 > /dev/null
    sleep 5
    while glassfish_running; do
      glassfish_shutdown
      sleep 3
    done
    echo_success " Done"
  fi
}
glassfish_async_stop() {
  glassfish_require 2>&1 > /dev/null
  if ! glassfish_running; then
    echo_warning "Glassfish already stopped"
  elif [ -n "$glassfish" ]; then
    echo -n "Shutting down Glassfish services in the background..."
    ($glassfish stop-domain &) 2>&1 > /dev/null
    echo_success " Done"
  fi
}
glassfish_restart() {
  #if [ -n "$glassfish" ]; then
  #    $glassfish restart-domain
  #fi
  glassfish_stop
  glassfish_start
  glassfish_running_report
}
glassfish_start_report() {
  action_condition GLASSFISH_RUNNING "Starting Glassfish" "glassfish_start > /dev/null; glassfish_running;"
}
glassfish_uninstall() {
  glassfish_require
  echo "Stopping Glassfish..."
  glassfish_shutdown
  # application files
  echo "Removing Glassfish in /usr/share/glassfish4..."
  rm -rf /usr/share/glassfish4
}

# Must call java_require before calling this.
# Parameters:
# - certificate alias to report on (default is s1as, the glassfish default ssl cert alias)
glassfish_sslcert_report() {
  local alias="${1:-s1as}"
  local keystorePassword="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"
  local domain_found=`$glassfish list-domains | head -n 1 | awk '{ print $1 }'`
  local keystore=${GLASSFISH_HOME}/domains/${domain_found}/config/keystore.jks
  java_keystore_cert_report "$keystore" "$keystorePassword" "$alias"
}

# used by attestation_service_install to create a new domain just for attestation service
# parameters:  domain name, domain dir (absolute path)
# example: glassfish_create_domain "intel-as" "${ATTESTATION_SERVICE_HOME}/glassfish/domain"
#Default port 4848 for Admin is in use. Using 39766
#Default port 8080 for HTTP Instance is in use. Using 41112
#Default port 7676 for JMS is in use. Using 52108
#Default port 3700 for IIOP is in use. Using 46322
#Default port 8181 for HTTP_SSL is in use. Using 42364
#
glassfish_create_domain() {
  local domain_name=${1}
  local domain_dir=${2}
  if [ -n "$glassfish" ]; then
    $glassfish create-domain --domaindir "${domain_dir}" "${domain_name}"
    $glassfish start-domain --domaindir "${domain_dir}" "${domain_name}"
  fi
}

glassfish_delete_domain() {
  local domain_name=${1}
  local domain_dir=${2}

  if [ -n "$glassfish" ]; then
    local domain_found=`$glassfish list-domains --domaindir "${domain_dir}" | grep "${domain_name}"`
    if [ -n "$domain_found" ]; then
      $glassfish delete-domain --domaindir "${domain_dir}" "${domain_name}"      
    fi
  fi
}

glassfish_create_ssl_cert_prompt() {
    #echo_warning "This feature has been disabled: glassfish_create_ssl_cert_prompt"
    #return
    # SSL Certificate setup
    #local should_create_sslcert
    prompt_yes_no GLASSFISH_CREATE_SSL_CERT "Do you want to set up an SSL certificate for Glassfish?"
    echo
    if [ "${GLASSFISH_CREATE_SSL_CERT}" == "yes" ]; then
      if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
      glassfish_require
      DEFAULT_GLASSFISH_SSL_CERT_CN=`ifconfig | grep "inet addr" | awk '{ print $2 }' | awk -F : '{ print $2 }' | sed -e ':a;N;$!ba;s/\n/,/g'`
      prompt_with_default GLASSFISH_SSL_CERT_CN "Domain name[s] for SSL Certificate:" ${DEFAULT_GLASSFISH_SSL_CERT_CN:-127.0.0.1}
      glassfish_create_ssl_cert "${GLASSFISH_SSL_CERT_CN}"
    fi
}

function valid_ip() {
    local  ip=$1
    local  stat=1
    if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($ip)
        IFS=$OIFS
        [[ ${ip[0]} -le 255 && ${ip[1]} -le 255 \
            && ${ip[2]} -le 255 && ${ip[3]} -le 255 ]]
        stat=$?
    fi
    return $stat
}

# Parameters:
# - serverName (hostname in the URL, such as 127.0.0.1, 192.168.1.100, my.attestation.com, etc.)
glassfish_create_ssl_cert() {
#  echo_warning "This feature has been disabled: glassfish_create_ssl_cert"
#  return
  if [ "${GLASSFISH_CREATE_SSL_CERT:-yes}" == "yes" ]; then
    if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
    glassfish_require
    local serverName="${1}"

    # Create an array of host ips and dns names from csv list passed into function
    serverName=`echo $serverName | sed -e 's/ //g' | sed -e 's/,$//'`
    OIFS="$IFS"
    IFS=','
    read -a hostArray <<< "${serverName}"
    IFS="$OIFS"
  
    # create common names and sans strings by parsing array
    local cert_cns=""
    local cert_sans=""
    for i in "${hostArray[@]}"; do
      cert_cns+="CN=$i,"
      tmpCN=""
      if valid_ip "$i"; then 
        tmpCN="ip:$i"
      else
        tmpCN="dns:$i"
      fi
      cert_sans+="$tmpCN,"
    done
    cert_cns=`echo $cert_cns | sed -e 's/,$//'`
    cert_sans=`echo $cert_sans | sed -e 's/,$//'`
    cert_cns='CN='`echo $serverName | sed -e 's/ //g' | sed -e 's/,$//' | sed -e 's/,/, CN=/g'`

    local keystorePassword="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"
    local domain_found=`$glassfish list-domains | head -n 1 | awk '{ print $1 }'`
    local keystore=${GLASSFISH_HOME}/domains/${domain_found}/config/keystore.jks
    local cacerts=${GLASSFISH_HOME}/domains/${domain_found}/config/cacerts.jks
    local configDir="/opt/mtwilson/configuration"
    local keytool=${JAVA_HOME}/bin/keytool
    local mtwilson=`which mtwilson 2>/dev/null`
    local tmpHost=`echo $serverName | awk -F ',' '{ print $1 }' | sed -e 's/ //g'`

    # Check if there is already a certificate for this serverName in the Glassfish keystore
    local has_cert=`$keytool -list -v -alias s1as -keystore $keystore -storepass $keystorePassword | grep "^Owner:" | grep "$cert_cns"`
    if [ -n "$has_cert" ]; then
      echo "SSL Certificate for ${serverName} already exists"
    else
      echo "Creating SSL Certificate for ${serverName}..."
      # Delete public insecure certs within keystore.jks and cacerts.jks
      $keytool -delete -alias s1as  -keystore $keystore -storepass $keystorePassword 2>&1 >/dev/null
      $keytool -delete -alias glassfish-instance -keystore $keystore -storepass $keystorePassword 2>&1 >/dev/null
      $keytool -delete -alias s1as -keystore $cacerts -storepass $keystorePassword 2>&1 >/dev/null
      $keytool -delete -alias glassfish-instance -keystore $cacerts -storepass $keystorePassword 2>&1 >/dev/null

      # Update keystore.jks
      $keytool -genkeypair -alias s1as -dname "$cert_cns, OU=Mt Wilson, O=Trusted Data Center, C=US" -ext san="$cert_sans" -keyalg RSA -keysize 2048 -validity 3650 -keystore $keystore -keypass $keystorePassword -storepass $keystorePassword
      $keytool -genkeypair -alias glassfish-instance -dname "$cert_cns, OU=Mt Wilson, O=Trusted Data Center, C=US" -ext san="$cert_sans" -keyalg RSA -keysize 2048 -validity 3650 -keystore $keystore -keypass $keystorePassword -storepass $keystorePassword
    fi

    # Export certificates from keystore.jks
    $keytool -export -alias s1as -file "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.s1as.${tmpHost}.crt" -keystore $keystore -storepass $keystorePassword
    $keytool -export -alias glassfish-instance -file "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.gi.${tmpHost}.crt" -keystore $keystore -storepass $keystorePassword

    # Update cacerts.jks
    $keytool -importcert -noprompt -alias s1as -file "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.s1as.${tmpHost}.crt" -keystore $cacerts -storepass $keystorePassword
    $keytool -importcert -noprompt -alias glassfish-instance -file "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.gi.${tmpHost}.crt" -keystore $cacerts -storepass $keystorePassword

    openssl x509 -in "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.s1as.${tmpHost}.crt" -inform der -out "$configDir/ssl.crt.pem" -outform pem
    cp "${GLASSFISH_HOME}/domains/${domain_found}/config/ssl.s1as.${tmpHost}.crt" "$configDir/ssl.crt"
    cp "$keystore" "$configDir/mtwilson-tls.jks"
    mtwilson_tls_cert_sha1=`openssl sha1 -hex "$configDir/ssl.crt" | awk -F '=' '{ print $2 }' | tr -d ' '`
    update_property_in_file "mtwilson.api.tls.policy.certificate.sha1" "$configDir/mtwilson.properties" "$mtwilson_tls_cert_sha1"
    echo "Restarting Glassfish domain..."
    glassfish_restart
  fi
}

### FUNCTION LIBRARY: tomcat

# tomcat 

tomcat_clear() {
  TOMCAT_CONF=""
  TOMCAT_HOME=""
  tomcat_bin=""
  tomcat=""
}


tomcat_require() {
  local min_version="${1:-${tomcat_required_version:-$DEFAULT_TOMCAT_REQUIRED_VERSION}}"
  if not tomcat_ready; then
    tomcat_detect ${min_version} > /dev/null
  fi
  if not tomcat_ready; then
    echo_failure "Cannot find Tomcat server version $min_version or later"
    exit 1
  fi
}

tomcat_ready_report() {
  if [[ -z "$TOMCAT_HOME" ]]; then echo_warning "TOMCAT_HOME variable is not set"; return 1; fi
  if [[ -z "$tomcat_bin" ]]; then echo_warning "Tomcat binary path is not set"; return 1; fi
  if [[ ! -f "$tomcat_bin" ]]; then echo_warning "Cannot find Tomcat binary at $tomcat_bin"; return 1; fi
  if [[ -z "$tomcat" ]]; then echo_warning "Tomcat command is not set"; return 1; fi
  echo_success "Using Tomcat at $TOMCAT_HOME"
  return 0
}


tomcat_ready() {
  tomcat_ready_report > /dev/null
  return $?
}

# How to use;   GLASSFISH_VERSION   =`glassfish_version`
# If you pass a parameter, it is the path to a glassfish "asadmin" binary
# If you do not pass a parameter, the "glassfish" variable is used as the path to the binary
tomcat_version() {
  # Either the JAVA_HOME or the JRE_HOME environment variable must be defined
  # At least one of these environment variable is needed to run this program
  if [[ -z $JAVA_HOME && -z $JRE_HOME ]]; then java_detect; fi
  if [[ -z $JAVA_HOME && -z $JRE_HOME ]]; then return 1; fi

  if [[ -n "$tomcat" ]]; then
    # extract the version number from a string like: glassfish version "3.0"
    local current_tomcat_version=`$tomcat version 2>&1 | grep -i "^Server version:" | grep -i version | awk -F / '{ print $2 }'`
    if [ -n "$current_tomcat_version" ]; then
      echo "current_tomcat_version: $current_tomcat_version" >> $INSTALL_LOG_FILE
      export TOMCAT_VERSION=$current_tomcat_version
      return 0
    fi
    return 2
  fi
  return 1
}

# sample output from "$tomcat version":
#Using CATALINA_BASE:   /usr/share/apache-tomcat-7.0.34
#Using CATALINA_HOME:   /usr/share/apache-tomcat-7.0.34
#Using CATALINA_TMPDIR: /usr/share/apache-tomcat-7.0.34/temp
#Using JRE_HOME:        /usr/share/jdk1.7.0_51
#Using CLASSPATH:       /usr/share/apache-tomcat-7.0.34/bin/bootstrap.jar
#Server version: Apache Tomcat/7.0.34
#Server built:   July 19 2010 1458
#Server number:  7.0.34
#OS Name:        Linux
#OS Version:     3.0.0-12-server
#Architecture:   amd64
#JVM Version:    1.7.0_51
#JVM Vendor:     Sun Microsystems Inc.


# Environment:
# - TOMCAT_REQUIRED_VERSION  (default is 7.0.34)
tomcat_version_report() {
  local min_version="${1:-${tomcat_required_version:-$DEFAULT_TOMCAT_REQUIRED_VERSION}}"
  #TOMCAT_VERSION=`tomcat_version`
  tomcat_version
  if is_version_at_least "$TOMCAT_VERSION" "${min_version}"; then
    echo_success "Tomcat version $TOMCAT_VERSION is ok"
    return 0
  else
    echo_warning "Tomcat version $TOMCAT_VERSION is not supported, minimum is ${min_version}"
    return 1
  fi
}

# detects possible tomcat installations
# does nothing if TOMCAT_HOME is already set; unset before calling to force detection
tomcat_detect() {
  local min_version="${1:-${tomcat_required_version:-${DEFAULT_TOMCAT_REQUIRED_VERSION}}}"
  echo "min_version for tomcat_detect is $min_version" >>  $INSTALL_LOG_FILE

  if [[ (-z $JAVA_HOME && -z $JRE_HOME) || -z $java ]]; then java_detect; fi
  if [[ (-z $JAVA_HOME && -z $JRE_HOME) || -z $java ]]; then return 1; fi

      if [[ -n "$java" ]]; then    
        local java_bindir=`dirname "$java"`
      fi

  # start with TOMCAT_HOME if it is already configured
  if [[ -n "$TOMCAT_HOME" ]]; then
    if [[ -z "$tomcat_bin" ]]; then
      tomcat_bin="$TOMCAT_HOME/bin/catalina.sh"
    fi
    if [[ -z "$tomcat" ]]; then
      if [[ -n "$java" ]]; then    
        # the glassfish admin tool read timeout is in milliseconds, so 900,000 is 900 seconds
        tomcat="env PATH=$java_bindir:$PATH $tomcat_bin"
      else
        tomcat="$tomcat_bin"
      fi
    fi
    if [ -z "$TOMCAT_CONF" ]; then
      if [ -d "$TOMCAT_HOME/conf" ] && [ -f "$TOMCAT_HOME/conf/tomcat-users.xml" ] && [ -f "$TOMCAT_HOME/conf/server.xml" ]; then
        export TOMCAT_CONF="$TOMCAT_HOME/conf"
      else
        # we think we know TOMCAT_HOME but we can't find TOMCAT_CONF so
        # reset the "tomcat" variable to force a new detection below
        tomcat=""
      fi
    fi
    if [[ -n "$tomcat" ]]; then
      #TOMCAT_VERSION=`tomcat_version`
      tomcat_version
      if is_version_at_least "$TOMCAT_VERSION" "${min_version}"; then
        return 0
      fi
    fi
  fi
  #echo "tomcat variable is $tomcat"
  #echo "TOMCAT_VERSION is $TOMCAT_VERSION"

  # if we get here, then there was NOT already a tomcat configured
  # that meets our minimum version requirement

  TOMCAT_CANDIDATES=`find / -name tomcat-users.xml 2>/dev/null`
  tomcat_clear
  echo "debug TOMCAT_CANDIDATES: ${TOMCAT_CANDIDATES}" >> $INSTALL_LOG_FILE
  for c in $TOMCAT_CANDIDATES
  do
      #echo "debug tomcat candidate: $c"
      local conf_dir=`dirname "$c"`
      local parent=`dirname "$conf_dir"`
      if [ -f "$parent/bin/catalina.sh" ]; then
        export TOMCAT_HOME="$parent"
        export TOMCAT_BASE="$parent"
        export TOMCAT_CONF="$conf_dir"
        tomcat_bin=$parent/bin/catalina.sh
        tomcat="env PATH=$java_bindir:$PATH JAVA_HOME=$JAVA_HOME CATALINA_HOME=$TOMCAT_HOME CATALINA_BASE=$TOMCAT_BASE CATALINA_CONF=$TOMCAT_CONF $tomcat_bin"
        echo "Found Tomcat: $TOMCAT_HOME" >> $INSTALL_LOG_FILE
        echo "tomcat=$tomcat" >> $INSTALL_LOG_FILE
        tomcat_version
        if is_version_at_least "$TOMCAT_VERSION" "${min_version}"; then
          return 0
        fi
      fi
  done
  echo_failure "Cannot find Tomcat"
  tomcat_clear
  return 1
}

tomcat_install() {
  TOMCAT_HOME=""
  tomcat=""
  tomcat_detect
  if [[ -z "$TOMCAT_HOME" || -z "$tomcat" ]]; then
    if [[ -n "$TOMCAT_PACKAGE" && -f "$TOMCAT_PACKAGE" ]]; then
      echo "Installing $TOMCAT_PACKAGE"
      #if [ -d "${tomcat_parent_dir}/${tomcat_name}" ]; then
      #    local datestr=`date +%Y-%m-%d.%H%M`
      #    echo "Renaming existing incomplete ${tomcat_parent_dir}/${tomcat_name} to ${tomcat_parent_dir}/${tomcat_name}.${datestr}"
      #    mv $tomcat_parent_dir/$tomcat_name $tomcat_parent_dir/${tomcat_name}.${datestr}
      #fi
      gunzip -c $TOMCAT_PACKAGE | tar xf - 2>&1  >/dev/null
      local tomcat_folder=`echo $TOMCAT_PACKAGE | awk -F .tar.gz '{ print $1 }'`
      if [ -d "$tomcat_folder" ]; then
        if [ -d "/usr/share/$tomcat_folder" ]; then
          echo "Tomcat already installed at /usr/share/$tomcat_folder"
          export TOMCAT_HOME="/usr/share/$tomcat_folder"
        else
          mv $tomcat_folder /usr/share && export TOMCAT_HOME="/usr/share/$tomcat_folder"
        fi
      fi
      tomcat_detect
    else
      TOMCAT_YUM_PACKAGES="tomcat7"
      TOMCAT_APT_PACKAGES="tomcat7"
      auto_install "Tomcat via package manager" "TOMCAT"
      tomcat_detect
    fi
  fi
  
  if [[ -z "$TOMCAT_HOME" || -z "$tomcat" ]]; then
    echo "Unable to auto-install Tomcat"
    echo "  Tomcat download URL:"
    echo "  http://tomcat.apache.org/"
  fi
}

# Run this AFTER tomcat_install
# optional global variables:  
#   tomcat_username (default value tomcat)
#   TOMCAT_HOME (default value /usr/share/tomcat)
# works on Debian, Ubuntu, CentOS, RedHat, SUSE
# Username should not contain any spaces or punctuation
# Optional arguments:  one or more directories for tomcat user to own
tomcat_permissions() {
  local chown_locations="$@"
  local username=${TOMCAT_USERNAME:-tomcat}
  local user_exists=`cat /etc/passwd | grep "^${username}"`
  if [ -z "$user_exists" ]; then
    useradd -c "tomcat" -d "${TOMCAT_HOME:-/var}" -r -s /bin/bash "$username"
  fi
  local file
  for file in $chown_locations
  do
    if [[ -n "$file" && -e "$file" ]]; then
      chown -R "${username}:${username}" "$file"
    fi
  done
}

tomcat_running() {  
  TOMCAT_RUNNING=''
  if [ -z "$TOMCAT_HOME" ]; then
    tomcat_detect 2>&1 > /dev/null
  fi
  if [ -n "$TOMCAT_HOME" ]; then
    TOMCAT_PID=`ps gauwxx | grep java | grep -v grep | grep "$TOMCAT_HOME" | awk '{ print $2 }'`
    echo TOMCAT_PID: $TOMCAT_PID >> $INSTALL_LOG_FILE
    if [ -n "$TOMCAT_PID" ]; then
      TOMCAT_RUNNING=yes
      echo TOMCAT_RUNNING: $TOMCAT_RUNNING >> $INSTALL_LOG_FILE
      return 0
    fi
  fi
  return 1
}

tomcat_running_report() {
  echo -n "Checking Tomcat process... "
  if tomcat_running; then
    echo_success "Running (pid $TOMCAT_PID)"
  else
    echo_failure "Not running"
  fi
}
tomcat_start() {
  tomcat_require 2>&1 > /dev/null
  if tomcat_running; then
    echo_warning "Tomcat already running [PID: $TOMCAT_PID]"
  elif [ -n "$tomcat" ]; then
    echo -n "Waiting for Tomcat services to startup..."
    ($tomcat start &) 2>&1 > /dev/null
    while ! tomcat_running; do
      sleep 1
    done
    echo_success " Done"
  fi
}
tomcat_shutdown() {
  if tomcat_running; then
    if [ -n "$TOMCAT_PID" ]; then
      kill -9 $TOMCAT_PID
    fi
  fi
}
tomcat_stop() {
  tomcat_require 2>&1 > /dev/null
  if ! tomcat_running; then
    echo_warning "Tomcat already stopped"
  elif [ -n "$tomcat" ]; then
    echo -n "Waiting for Tomcat services to shutdown..."
    $tomcat stop 2>&1 > /dev/null
    while tomcat_running; do
      tomcat_shutdown 2>&1 > /dev/null
      sleep 3
    done
    echo_success " Done"
  fi
}
tomcat_async_stop() {
  tomcat_require 2>&1 > /dev/null
  if ! tomcat_running; then
    echo_warning "Tomcat already stopped"
  elif [ -n "$tomcat" ]; then
    echo -n "Shutting down Tomcat services in the background..."
    ($tomcat stop &) 2>&1 > /dev/null
    echo_success " Done"
  fi
}
tomcat_restart() {
  tomcat_stop
  tomcat_start
  tomcat_running_report
}
tomcat_start_report() {
  action_condition TOMCAT_RUNNING "Starting Tomcat" "tomcat_start > /dev/null; tomcat_running;"
}
tomcat_uninstall() {
  tomcat_require
  echo "Stopping Tomcat..."
  tomcat_shutdown
  # application files
  echo "Removing Tomcat in $TOMCAT_HOME..."
  rm -rf "$TOMCAT_HOME"
}

tomcat_create_ssl_cert_prompt() {
    prompt_yes_no TOMCAT_CREATE_SSL_CERT "Do you want to set up an SSL certificate for Tomcat?"
    echo
    if [ "${TOMCAT_CREATE_SSL_CERT}" == "yes" ]; then
      if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
      tomcat_require
      DEFAULT_TOMCAT_SSL_CERT_CN=`ifconfig | grep "inet addr" | awk '{ print $2 }' | awk -F : '{ print $2 }' | sed -e ':a;N;$!ba;s/\n/,/g'`
      prompt_with_default TOMCAT_SSL_CERT_CN "Domain name[s] for SSL Certificate:" ${DEFAULT_TOMCAT_SSL_CERT_CN:-127.0.0.1}
      tomcat_create_ssl_cert "${TOMCAT_SSL_CERT_CN}"
    fi
}

# Parameters:
# - serverName (hostname in the URL, such as 127.0.0.1, 192.168.1.100, my.attestation.com, etc.)
tomcat_create_ssl_cert() {
#  echo_warning "This feature has been disabled: tomcat_create_ssl_cert"
#  return
  if [ "${TOMCAT_CREATE_SSL_CERT:-yes}" == "yes" ]; then
    if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
    tomcat_require
    local serverName="${1}"
  
    # Create an array of host ips and dns names from csv list passed into function
    serverName=`echo $serverName | sed -e 's/ //g' | sed -e 's/,$//'`
    OIFS="$IFS"
    IFS=','
    read -a hostArray <<< "${serverName}"
    IFS="$OIFS"
  
    # create common names and sans strings by parsing array
    local cert_cns=""
    local cert_sans=""
    for i in "${hostArray[@]}"; do
      cert_cns+="CN=$i,"
   
      tmpCN=""
      if valid_ip "$i"; then 
       tmpCN="ip:$i"
      else
       tmpCN="dns:$i"
      fi
      cert_sans+="$tmpCN,"
    done
    cert_cns=`echo $cert_cns | sed -e 's/,$//'`
    cert_sans=`echo $cert_sans | sed -e 's/,$//'`

    local keystorePassword="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"
    local keystore=${TOMCAT_HOME}/ssl/.keystore
    local configDir="/opt/mtwilson/configuration"
    local keytool=${JAVA_HOME}/bin/keytool
    local mtwilson=`which mtwilson 2>/dev/null`
  
    mkdir -p ${TOMCAT_HOME}/ssl
    # Check if there is already a certificate for this serverName in the Tomcat keystore
    local has_cert=`$keytool -list -v -alias tomcat -keystore $keystore -storepass $keystorePassword | grep "^Owner:" | grep "$cert_cns"`
  
    if [ -n "$has_cert" ]; then
      echo "SSL Certificate for ${serverName} already exists"
    else
      echo "Creating SSL Certificate for ${serverName}..."
      local tmpHost=`echo $serverName | awk -F ',' '{ print $1 }' | sed -e 's/ //g'`
    
      # Delete public insecure certs within keystore.jks and cacerts.jks
      $keytool -delete -alias tomcat -keystore $keystore -storepass $keystorePassword 2>&1 >/dev/null

      # Update keystore.jks
      $keytool -genkeypair -alias tomcat -dname "$cert_cns, OU=Mt Wilson, O=Trusted Data Center, C=US" -ext san="$cert_sans" -keyalg RSA -keysize 2048 -validity 3650 -keystore $keystore -keypass $keystorePassword -storepass $keystorePassword
    fi

    #$mtwilson api CreateSSLCertificate "${serverName}" "ip:${serverName}" $keystore tomcat "$keystorePassword"
    $keytool -export -alias tomcat -file "${TOMCAT_HOME}/ssl/ssl.${tmpHost}.crt" -keystore $keystore -storepass $keystorePassword 
    #$keytool -import -trustcacerts -alias tomcat -file "${TOMCAT_HOME}/ssl/ssl.${tmpHost}.crt" -keystore $keystore -storepass ${keystorePassword}
    openssl x509 -in "${TOMCAT_HOME}/ssl/ssl.${tmpHost}.crt" -inform der -out "$configDir/ssl.crt.pem" -outform pem
    cp "${TOMCAT_HOME}/ssl/ssl.${tmpHost}.crt" "$configDir/ssl.crt"
    cp "$keystore" "$configDir/mtwilson-tls.jks"
    mtwilson_tls_cert_sha1=`openssl sha1 -hex "$configDir/ssl.crt" | awk -F '=' '{ print $2 }' | tr -d ' '`
    update_property_in_file "mtwilson.api.tls.policy.certificate.sha1" "$configDir/mtwilson.properties" "$mtwilson_tls_cert_sha1"
    #sed -i.bak 's/sslProtocol=\"TLS\"/sslProtocol=\"TLS\" SSLCertificateFile=\"${catalina.base}\/ssl\/ssl.${serverName}.crt\" SSLCertificateKeyFile=\"${catalina.base}\/ssl\/ssl.${serverName}.crt.pem\"/g' ${TOMCAT_HOME}/conf/server.xml
    #cp ${keystore} /root/
    #cp ${TOMCAT_HOME}/ssl/ssl.${serverName}.crt.pem "$configDir/ssl.crt.pem"
  fi
}
tomcat_env_report(){
  echo "TOMCAT_HOME=$TOMCAT_HOME"
  echo "tomcat_bin=$tomcat_bin"
  echo "tomcat=\"$tomcat\""
}

# Must call java_require before calling this.
# Parameters:
# - certificate alias to report on (default is tomcat, the tomcat default ssl cert alias)
tomcat_sslcert_report() {
  local alias="${1:-tomcat}"
  local keystorePassword="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"
  local keystore=${TOMCAT_HOME}/ssl/.keystore
  java_keystore_cert_report "$keystore" "$keystorePassword" "$alias"
}

tomcat_init_manager() {
  local config_file=/opt/mtwilson/configuration/mtwilson.properties
  TOMCAT_MANAGER_USER=""
  TOMCAT_MANAGER_PASS=""
  TOMCAT_MANAGER_PORT=""
  if [ -z "$WEBSERVICE_USERNAME" ]; then WEBSERVICE_USERNAME=admin; fi
  if [ -z "$TOMCAT_HOME" ]; then tomcat_detect; fi
  TOMCAT_MANAGER_USER=`read_property_from_file tomcat.admin.username "${config_file}"`
  TOMCAT_MANAGER_PASS=`read_property_from_file tomcat.admin.password "${config_file}"`
  if [[ -z "$TOMCAT_MANAGER_USER" ]]; then
    tomcat_manager_xml=`grep "username=\"$WEBSERVICE_USERNAME\"" $TOMCAT_HOME/conf/tomcat-users.xml | head -n 1`
    
    OIFS="$IFS"
    IFS=' '
    read -a managerArray <<< "${tomcat_manager_xml}"
    IFS="$OIFS"

    for i in "${managerArray[@]}"; do
      if [[ "$i" == *"username"* ]]; then
        TOMCAT_MANAGER_USER=`echo $i|awk -F'=' '{print $2}'|sed 's/^"\(.*\)"$/\1/'`
      fi
  
      if [[ "$i" == *"password"* ]]; then
        TOMCAT_MANAGER_PASS=`echo $i|awk -F'=' '{print $2}'|sed 's/^"\(.*\)"$/\1/'`
      fi
    done
  fi

  # get manager port
  tomcat_managerPort_xml=`cat $TOMCAT_HOME/conf/server.xml|
    awk 'in_comment&&/-->/{sub(/([^-]|-[^-])*--+>/,"");in_comment=0}
    in_comment{next}
    {gsub(/<\!--+([^-]|-[^-])*--+>/,"");
    in_comment=sub(/<\!--+.*/,"");
    print}'|
    grep "<Connector"|grep "port="|head -n1`

  OIFS="$IFS"
  IFS=' '
  read -a managerPortArray <<< "${tomcat_managerPort_xml}"
  IFS="$OIFS"

  for i in "${managerPortArray[@]}"; do
    if [[ "$i" == *"port"* ]]; then
      TOMCAT_MANAGER_PORT=`echo $i|awk -F'=' '{print $2}'|sed 's/^"\(.*\)"$/\1/'`
    fi
  done

  test=`wget http://$TOMCAT_MANAGER_USER:$TOMCAT_MANAGER_PASS@127.0.0.1:$TOMCAT_MANAGER_PORT/manager/text/list -O - -q --no-check-certificate --no-proxy|grep "OK"`

  if [ -n "$test" ]; then
    echo_success "Tomcat manger connection success."
  else
    echo_failure "Tomcat manager connection failed. Incorrect credentials."
  fi
}

### FUNCTION LIBRARY: jetty


jetty_running() {  
  JETTY_RUNNING=''
  if [ -n "$MTWILSON_HOME" ]; then
    JETTY_PID=`ps gauwxx | grep java | grep -v grep | grep "$MTWILSON_HOME" | awk '{ print $2 }'`
    echo JETTY_PID: $JETTY_PID >> $INSTALL_LOG_FILE
    if [ -n "$JETTY_PID" ]; then
      JETTY_RUNNING=yes
      echo JETTY_RUNNING: $JETTY_RUNNING >> $INSTALL_LOG_FILE
      return 0
    fi
  fi
  return 1
}

jetty_running_report() {
  echo -n "Checking Mt Wilson process... "
  if jetty_running; then
    echo_success "Running (pid $JETTY_PID)"
  else
    echo_failure "Not running"
  fi
}
jetty_start() {
  jetty_require 2>&1 > /dev/null
  if jetty_running; then
    echo_warning "Jetty already running [PID: $JETTY_PID]"
  elif [ -n "$jetty" ]; then
    echo -n "Waiting for Mt Wilson services to startup..."
    # default is /opt/mtwilson/java
    local java_lib_dir=${MTWILSON_JAVA_DIR:-$DEFAULT_MTWILSON_JAVA_DIR}
    if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
    local mtwilson_jars=$(JARS=($java_lib_dir/*.jar); IFS=:; echo "${JARS[*]}")
    mainclass=com.intel.mtwilson.launcher.console.Main
    local jvm_memory=2048m
    local jvm_maxperm=512m
    { $java -Xmx${jvm_memory} -XX:MaxPermSize=${jvm_maxperm} -cp "$mtwilson_jars" -Dlogback.configurationFile=${conf_dir:-$DEFAULT_MTWILSON_CONF_DIR}/logback-stderr.xml $mainclass $@ | grep -vE "^\[EL Info\]|^\[EL Warning\]" ; } 2> /var/log/mtwilson.log
    return $?
  fi
}

jetty_shutdown() {
  if jetty_running; then
    if [ -n "$JETTY_PID" ]; then
      kill -9 $JETTY_PID
    fi
  fi
}
jetty_stop() {
  if ! jetty_running; then
    echo_warning "Mt Wilson already stopped"
  else
    echo -n "Waiting for Mt Wilson services to shutdown..."
    while jetty_running; do
      jetty_shutdown 2>&1 > /dev/null
      sleep 3
    done
    echo_success " Done"
  fi
}
jetty_restart() {
  jetty_stop
  jetty_start
  jetty_running_report
}
jetty_start_report() {
  action_condition TOMCAT_RUNNING "Starting Mt Wilson" "jetty_start > /dev/null; jetty_running;"
}

### FUNCTION LIBRARY: java

java_clear() {
  JAVA_HOME=""
  java=""
  JAVA_VERSION=""
}

# Returns success (0) if the JAVA_HOME and java variables are set and if the java binary exists.
# Returns error (1) otherwise and displays the issue as a warning.
# Quick and repeatable. No side effects.
# Example:   if not java_ready; then java_ready_report; fi
# Note: We do NOT check JAVA_VERSION here because if someone has configured a specific Java they want to use,
# we don't care what version it is as long as it is present.  In contrast, the java_detect function sets JAVA_VERSION
java_ready_report() {
  if [[ -z "$JAVA_HOME" ]]; then echo_warning "JAVA_HOME variable is not set"; return 1; fi
  if [[ -z "$java" ]]; then echo_warning "Java binary path is not set"; return 1; fi
  if [[ ! -f "$java" ]]; then echo_warning "Cannot find Java binary at $java"; return 1; fi
  echo_success "Using Java at $java"
  return 0
}

# Returns success (0) if the JAVA_HOME and java variables are set and if the java binary exists.
# Returns error (1) otherwise.
# Quick and repeatable. No side effects.
# Example:   if java_ready; then $java -jar start.jar; fi
java_ready() {
  java_ready_report > /dev/null
  return $?
}


# prints the current java version
# return codes:
# 0 - success
# 1 - java command not found
# 2 - cannot get version number using java command
# Environment:
# - java  (path to java binary) you can get it by calling java_detect 
#    (or if you are calling this from java_detect you set it yourself)
java_version() {
  if [ -n "$java" ]; then
    # extract the version number from a string like: java version "1.7.0_51"
    local current_java_version=`$java -version 2>&1 | head -n 1 | sed -e 's/"//g' | awk '{ print $3 }'`
    if [ -n "$current_java_version" ]; then
      echo $current_java_version
      return 0
    fi
    return 2
  fi
  return 1
}

# Environment:
# - JAVA_REQUIRED_VERSION
java_version_report() {
  local min_version="${1:-${JAVA_REQUIRED_VERSION:-${DEFAULT_JAVA_REQUIRED_VERSION}}}"
  local current_version=`java_version`
  if is_java_version_at_least "$current_version" "${min_version}"; then
    echo_success "Java version $current_version is ok"
    return 0
  else
    echo_warning "Java version $current_version is not supported, minimum is ${min_version}"
    return 1
  fi
}


# detects possible java installations
# does nothing if JAVA_HOME is already set; unset before calling to force detection
# uses the first installation found that meets the version requirement.
# prefers JDK over JRE installations, and prefers JRE over system-provided java
# This is not because JDK is better than JRE is better than system-provided java,
# but because if the system administrator has bothered to install the JDK or JRE
# it's clear he prefers to use that over the system-provided java.
# Environment:
# - JAVA_REQUIRED_VERSION should be set like 1.7 or 1.7.0_51 ; if not set then DEFAULT_JAVA_REQUIRED_VERSION is used
# Return code:  0 if java matching minimum version is found, 1 otherwise
# Postcondition:  on success, JAVA_HOME, java, and JAVA_VERSION are set;  on failure to find java they are cleared
java_detect() {
  local min_version="${1:-${JAVA_REQUIRED_VERSION:-${DEFAULT_JAVA_REQUIRED_VERSION}}}"
  # start with JAVA_HOME if it is already configured
  if [[ -n "$JAVA_HOME" ]]; then
    if [[ -z "$java" ]]; then
      java=${JAVA_HOME}/bin/java
    fi
    JAVA_VERSION=`java_version`
    if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
      return 0
    fi
  fi

    JAVA_JDK_CANDIDATES=`find / -name java 2>/dev/null | grep jdk | grep -v jre | grep bin/java`
    for c in $JAVA_JDK_CANDIDATES
    do
        local java_bindir=`dirname "$c"`
        if [ -f "$java_bindir/java" ]; then
          export JAVA_HOME=`dirname "$java_bindir"`
          java=$c
          JAVA_VERSION=`java_version`
          echo "Found Java: $JAVA_HOME" >> $INSTALL_LOG_FILE 
          if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
            return 0
          fi
        fi
    done
    
    echo "Cannot find JDK"

    JAVA_JRE_CANDIDATES=`find / -name java 2>/dev/null | grep jre | grep bin/java`
    for c in $JAVA_JRE_CANDIDATES
    do
        java_bindir=`dirname "$c"`
        if [ -f "$java_bindir/java" ]; then
          export JAVA_HOME=`dirname "$java_bindir"`
          java=$c
          JAVA_VERSION=`java_version`
          echo "Found Java: $JAVA_HOME" >> $INSTALL_LOG_FILE
          if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
            return 0
          fi
        fi
    done

    echo "Cannot find JRE"

    JAVA_BIN_CANDIDATES=`find / -name java 2>/dev/null | grep bin/java`
    for c in $JAVA_BIN_CANDIDATES
    do
        java_bindir=`dirname "$c"`
        # in non-JDK and non-JRE folders the "java" command may be a symlink:
        if [ -f "$java_bindir/java" ]; then
          export JAVA_HOME=`dirname "$java_bindir"`
          java=$c
          JAVA_VERSION=`java_version`
          echo "Found Java: $c" >> $INSTALL_LOG_FILE
          if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
            return 0
          fi
        elif [ -h "$java_bindir/java" ]; then
          local javatarget=`readlink $c`
          if [ -f "$javatarget" ]; then
            java_bindir=`dirname "$javatarget"`
            export JAVA_HOME=`dirname "$java_bindir"`
            java=$javatarget
            JAVA_VERSION=`java_version`
            echo "Found Java: $java" >> $INSTALL_LOG_FILE
            if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
              return 0
            fi
          else
            echo_warning "Broken link $c -> $javatarget"
          fi
        fi
    done

    echo "Cannot find system Java"

  echo_failure "Cannot find Java"
  java_clear
  return 1
}

# must load from config file or call java_detect prior to calling this function
java_env_report() {
  echo "JAVA_HOME=$JAVA_HOME"
  echo "java_bindir=$java_bindir"
  echo "java=$java"
}


# if java home and java bin are already configured and meet the minimum version, does nothing
# if they are not configured it initiates java detect to find them
# if
# Environment:
# - JAVA_REQUIRED_VERSION in the format "1.7.0_51" (or pass it as a parameter)
java_require() {
  local min_version="${1:-${JAVA_REQUIRED_VERSION:-${DEFAULT_JAVA_REQUIRED_VERSION}}}"
  if [[ -z "$JAVA_HOME" || -z "$java" || ! -f "$java" ]]; then
    java_detect ${min_version} > /dev/null
  fi
  JAVA_VERSION=`java_version`
  if is_java_version_at_least "$JAVA_VERSION" "${min_version}"; then
    return 0
  fi
  echo_failure "Cannot find Java version $min_version or later"
  return 1
}

# usage:  if no_java 1.7; then echo_failure "Cannot find Java"; exit 1; fi
no_java() {
  java_require $1
  if [ $? -eq 0 ]; then return 1; else return 0; fi
}

# Environment:
# - JAVA_REQUIRED_VERSION in the format "1.7.0_51"
java_install_openjdk() {
  JAVA_YUM_PACKAGES="java-1.7.0-openjdk java-1.7.0-openjdk-devel"
  JAVA_APT_PACKAGES="openjdk-7-jre openjdk-7-jdk"
  java_detect
  if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
    auto_install "Java" "JAVA"
    java_detect
    if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
      echo_failure "Cannot install Java"
      echo "Java download URL:"
      echo "http://www.java.com/en/download/"
    fi
  else
    echo "Java is already installed"
  fi
}

java_install() {
  local JAVA_PACKAGE="${1-:jdk-7u51-linux-x64.tar.gz}"
#  JAVA_YUM_PACKAGES="java-1.7.0-openjdk java-1.7.0-openjdk-devel"
#  JAVA_APT_PACKAGES="openjdk-7-jre openjdk-7-jdk"
#  auto_install "Java" "JAVA"
  java_clear; java_detect >> $INSTALL_LOG_FILE
  if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} >> $INSTALL_LOG_FILE; then
    if [[ -z "$JAVA_PACKAGE" || ! -f "$JAVA_PACKAGE" ]]; then
      echo_failure "Missing Java installer: $JAVA_PACKAGE" | tee -a 
      return 1
    fi
    local javafile=$JAVA_PACKAGE
    echo "Installing $javafile"  >> $INSTALL_LOG_FILE
    is_targz=`echo $javafile | grep -E ".tar.gz$|.tgz$"`
    is_gzip=`echo $javafile | grep ".gz$"`
    is_bin=`echo $javafile | grep ".bin$"`
    javaname=`echo $javafile | awk -F . '{ print $1 }'`
    if [ -n "$is_targz" ]; then
      tar xzvf $javafile 2>&1 >> $INSTALL_LOG_FILE
    elif [ -n "$is_gzip" ]; then
      gunzip $javafile 2>&1 >/dev/null  >> $INSTALL_LOG_FILE
      chmod +x $javaname
      ./$javaname | grep -vE "inflating:|creating:|extracting:|linking:|^Creating" 
    elif [ -n "$is_bin" ]; then
      chmod +x $javafile
      ./$javafile | grep -vE "inflating:|creating:|extracting:|linking:|^Creating"  
    fi
    # java gets unpacked in current directory but they cleverly
    # named the folder differently than the archive, so search for it:
    local java_unpacked=`ls -1d jdk* jre* 2>/dev/null`
    for f in $java_unpacked
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
    java_detect  >> $INSTALL_LOG_FILE
    if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
      echo_failure "Unable to auto-install Java" | tee -a $INSTALL_LOG_FILE
      echo "  Java download URL:"                >> $INSTALL_LOG_FILE
      echo "  http://www.java.com/en/download/"  >> $INSTALL_LOG_FILE
    fi
  else
    echo "Java is already installed"              >> $INSTALL_LOG_FILE
  fi
}

java_keystore_cert_report() {
  local keystore="${1:-keystore.jks}"
  local keystorePassword="${2:-changeit}"
  local alias="${3:-s1as}"
  local keytool=${JAVA_HOME}/bin/keytool
  local owner_expires=`$keytool -list -v -alias $alias -keystore $keystore -storepass $keystorePassword | grep -E "^Owner|^Valid"`
  echo "$owner_expires"
  local fingerprints=`$keytool -list -v -alias $alias -keystore $keystore -storepass $keystorePassword | grep -E "MD5:|SHA1:"`
  echo "$fingerprints"
}


### FUNCTION LIBARARY: prerequisites reporting


# environment dependencies report
print_env_summary_report() {
  echo "Requirements summary:"
  local error=0
  if [ -n "$JAVA_HOME" ]; then
    echo "Java: $JAVA_VERSION"
  else
    echo_failure "Java: not found"
    error=1
  fi
  if using_mysql; then
    if [ -n "$MYSQL_HOME" ]; then
      echo "Mysql: $MYSQL_CLIENT_VERSION"
    else
      echo_failure "Mysql: not found"
      error=1
    fi
  fi
  if using_postgres; then
    if [ -n "$POSTGRES_HOME" ]; then
      echo "Postgres: $POSTGRES_CLIENT_VERSION"
    else
      echo_failure "Postgres: not found"
      error=1
    fi
  fi
  if using_glassfish; then
    if [ -n "$GLASSFISH_HOME" ]; then
      GLASSFISH_VERSION=`glassfish_version`
      echo "Glassfish: $GLASSFISH_VERSION"
    else
      echo_failure "Glassfish: not found"
      error=1
    fi
  fi
  if using_tomcat; then
    if [ -n "$TOMCAT_HOME" ]; then
      echo "Tomcat: $TOMCAT_CLIENT_VERSION"
    else
      echo_failure "Tomcat: not found"
      error=1
    fi
  fi
  return $error
}

mtwilson_running() {
  echo "Checking if mtwilson is running." >> $INSTALL_LOG_FILE
  if using_glassfish; then
    MTWILSON_API_BASEURL=${MTWILSON_API_BASEURL:-"https://127.0.0.1:8181/mtwilson/v2"}
  else
    MTWILSON_API_BASEURL=${MTWILSON_API_BASEURL:-"https://127.0.0.1:8443/mtwilson/v2"}
  fi
  MTWILSON_RUNNING=""
  
  MTWILSON_API_BASEURL_V2=`echo $MTWILSON_API_BASEURL | sed 's/\/mtwilson\/v1/\/mtwilson\/v2/'`
  MTWILSON_RUNNING=`wget $MTWILSON_API_BASEURL_V2/version -O - -q --no-check-certificate --no-proxy`
}

mtwilson_running_report() {
  echo -n "Checking if mtwilson is running... "
  mtwilson_running
  if [ -n "$MTWILSON_RUNNING" ]; then
    echo_success "Running"
  else
    echo_failure "Not running"
  fi
}

mtwilson_running_report_wait() {
  echo -n "Checking if mtwilson is running..."
  mtwilson_running
  for (( c=1; c<=10; c++ ))
  do
    if [ -z "$MTWILSON_RUNNING" ]; then
      echo -n "."
      sleep 5
      mtwilson_running
    fi
  done
  if [ -n "$MTWILSON_RUNNING" ]; then
    echo_success "Running"
  else
    echo_failure "Not running"
  fi
}


### FUNCTION LIBRARY: web service on top of web server

# parameters: webservice_application_name such as "AttestationService"
webservice_running() {
  local path=`pwd`
  local webservice_application_name="$1"

  echo "webservice_application_name: $webservice_application_name" >> $INSTALL_LOG_FILE
  MTWILSON_SERVER=${MTWILSON_SERVER:-127.0.0.1}
  WEBSERVICE_RUNNING=""
  WEBSERVICE_DEPLOYED=""

  if using_glassfish; then
    glassfish_running
    if [ -n "$GLASSFISH_RUNNING" ]; then
      WEBSERVICE_DEPLOYED=`$glassfish list-applications | grep "${webservice_application_name}" | head -n 1 | awk '{ print $1 }'`
      if [ -n "$WEBSERVICE_DEPLOYED" ]; then
        WEBSERVICE_RUNNING=`$glassfish show-component-status $WEBSERVICE_DEPLOYED | grep enabled`
      fi
    fi
  elif using_tomcat; then
    tomcat_running
    echo "TOMCAT_RUNNING: $TOMCAT_RUNNING" >> $INSTALL_LOG_FILE
    if [ -z "$TOMCAT_MANAGER_USER" ]; then tomcat_init_manager 2>&1 >/dev/null; fi
    if [ -n "$TOMCAT_RUNNING" ]; then
      WEBSERVICE_DEPLOYED=`wget http://$TOMCAT_MANAGER_USER:$TOMCAT_MANAGER_PASS@$MTWILSON_SERVER:$TOMCAT_MANAGER_PORT/manager/text/list -O - -q --no-check-certificate --no-proxy | grep "${webservice_application_name}"`
      if [ -n "$WEBSERVICE_DEPLOYED" ]; then
        WEBSERVICE_RUNNING=`wget http://$TOMCAT_MANAGER_USER:$TOMCAT_MANAGER_PASS@$MTWILSON_SERVER:$TOMCAT_MANAGER_PORT/manager/text/list -O - -q --no-check-certificate --no-proxy | grep "${webservice_application_name}" | head -n 1 | awk '{ print $1 }' | sed -e 's/:/\n/g' | grep "running"`
      fi
    fi
  fi
  cd $path
}
webservice_running_report() {
  local webservice_application_name="$1"
  echo -n "Checking if ${webservice_application_name} is deployed on webserver... "
  webservice_running "${webservice_application_name}"
  if [ -n "$WEBSERVICE_RUNNING" ]; then
    echo_success "Deployed"
  else
    echo_failure "Not deployed"
  fi
}
webservice_running_report_wait() {
  local webservice_application_name="$1"
  echo -n "Checking if ${webservice_application_name} is deployed on webserver..."
  webservice_running "${webservice_application_name}"
  for (( c=1; c<=10; c++ ))
  do
    if [ -z "$WEBSERVICE_RUNNING" ]; then
      echo -n "."
      sleep 5
      webservice_running "${webservice_application_name}"
    fi
  done
  if [ -n "$WEBSERVICE_RUNNING" ]; then
    echo_success "Deployed"
  else
    echo_failure "Not deployed"
  fi
}

webservice_start() {
  local webservice_application_name="$1"
  webservice_running  "${webservice_application_name}"
  if [ -n "$WEBSERVICE_DEPLOYED" ]; then
    if using_glassfish; then
      $glassfish enable $WEBSERVICE_DEPLOYED
    elif using_tomcat; then
      if [ -z "$TOMCAT_MANAGER_USER" ]; then tomcat_init_manager 2>&1 >/dev/null; fi
      wget http://$TOMCAT_MANAGER_USER:$TOMCAT_MANAGER_PASS@$MTWILSON_SERVER:$TOMCAT_MANAGER_PORT/manager/text/start?path=/${webservice_application_name} -O - -q --no-check-certificate --no-proxy
      #$tomcat start
      #if [ -f $TOMCAT_HOME/${webservice_application_name}/WEB-INF/web.xml.stop ]; then
        #rename $TOMCAT_HOME/${webservice_application_name}/WEB-INF/web.xml.stop $TOMCAT_HOME/${webservice_application_name}/WEB-INF/web.xml
      #fi
      #wget -O - -q --no-check-certificate --no-proxy https://tomcat:tomcat@$MTWILSON_SERVER:$DEFAULT_API_PORT/manager/start?path=${WEBSERVICE_DEPLOYED}  
    fi
  fi
}
webservice_stop() {
  local webservice_application_name="$1"
  webservice_running "${webservice_application_name}"
  if [ -n "$WEBSERVICE_DEPLOYED" ]; then
    if using_glassfish; then
      $glassfish disable $WEBSERVICE_DEPLOYED
    elif using_tomcat; then
      if [ -z "$TOMCAT_MANAGER_USER" ]; then tomcat_init_manager 2>&1 >/dev/null; fi
      wget http://$TOMCAT_MANAGER_USER:$TOMCAT_MANAGER_PASS@$MTWILSON_SERVER:$TOMCAT_MANAGER_PORT/manager/text/stop?path=/${webservice_application_name} -O - -q --no-check-certificate --no-proxy
      #$tomcat stop
      #if [ -f $TOMCAT_HOME/${webservice_application_name}/WEB-INF/web.xml ]; then
        #rename $TOMCAT_HOME/webapps/${webservice_application_name}/WEB-INF/web.xml $TOMCAT_HOME/${webservice_application_name}/WEB-INF/web.xml.stop
      #fi
      #wget -O - -q --no-check-certificate --no-proxy https://tomcat:tomcat@$MTWILSON_SERVER:$DEFAULT_API_PORT/manager/stop?path=${WEBSERVICE_DEPLOYED}
    fi
  fi
}

webservice_start_report() {
    local webservice_application_name="$1"
    webservice_require
    if using_glassfish; then
      glassfish_running
      if [ -z "$GLASSFISH_RUNNING" ]; then
          glassfish_start_report
      fi
    elif using_tomcat; then
      tomcat_running
      if [ -z "$TOMCAT_RUNNING" ]; then
          tomcat_start_report
      fi
    fi

    webservice_running "${webservice_application_name}"
    if [ -z "$WEBSERVICE_RUNNING" ]; then
          action_condition WEBSERVICE_RUNNING "Starting ${webservice_application_name}" "webservice_start ${webservice_application_name} > /dev/null; webservice_running ${webservice_application_name};"
    fi
    if [ -n "$WEBSERVICE_RUNNING" ]; then
          echo_success "${webservice_application_name} is running"
    fi
}
webservice_stop_report() {
    local webservice_application_name="$1"
    webservice_require
    if using_glassfish; then
      glassfish_running
    elif using_tomcat; then
      tomcat_running
    fi
    webservice_running "${webservice_application_name}"
    if [ -n "$WEBSERVICE_RUNNING" ]; then
        inaction_condition WEBSERVICE_RUNNING "Stopping ${webservice_application_name}" "webservice_stop ${webservice_application_name} > /dev/null; webservice_running ${webservice_application_name};"
    fi
    
    if [ -z "$WEBSERVICE_RUNNING" ]; then
      echo_success "${webservice_application_name} is stopped"
    fi
}


# parameters:
# webservice_application_name such as "AttestationService"
# webservice_war_file such as "/path/to/AttestationService-0.5.1.war"
# Environment:
# - glassfish_required_version
webservice_install() {
  local webservice_application_name="$1"
  local webservice_war_file="$2"
  #webservice_require

  webservice_running "${webservice_application_name}"

  local WAR_FILE="${webservice_war_file}"
  local WAR_NAME=${WAR_FILE##*/}

    if [ -n "$WEBSERVICE_DEPLOYED" ]; then
      if using_glassfish; then
        echo "Re-deploying ${WEBSERVICE_DEPLOYED} to Glassfish..."
        $glassfish redeploy --name ${WEBSERVICE_DEPLOYED} ${WAR_FILE}
      elif using_tomcat; then
        echo "Re-deploying ${WEBSERVICE_DEPLOYED} to Tomcat..."
        rm -rf $TOMCAT_HOME/webapps/$WAR_NAME
        cp $WAR_FILE $TOMCAT_HOME/webapps/
        #wget -O - -q --no-check-certificate --no-proxy https://tomcat:tomcat@$MTWILSON_SERVER:$DEFAULT_API_PORT/manager/reload?path=${WEBSERVICE_DEPLOYED}
      fi
    else
      if using_glassfish; then
        glassfish_require
        echo "Deploying ${webservice_application_name} to Glassfish..."
        $glassfish deploy --name ${webservice_application_name} ${WAR_FILE}
      elif using_tomcat; then
        #if [ ! tomcat_running ]; then
        #  tomcat_start
        #fi
        echo "Deploying ${webservice_application_name} to Tomcat..."
        cp $WAR_FILE $TOMCAT_HOME/webapps/
        
        # 2014-02-16 rksavinx removed; unnecessary block of code
        #wget -O - -q --no-check-certificate --no-proxy https://tomcat:tomcat@$MTWILSON_SERVER:$DEFAULT_API_PORT/manager/deploy?path=${webservice_application_name}&war=file:${webservice_war_file} 
        #wait here until the app finishes deploying
        ##webservice_running $webservice_application_name
        ##while [ -z "$WEBSERVICE_RUNNING" ]; do
        ##  webservice_running $webservice_application_name >> $INSTALL_LOG_FILE
        ##  echo -n "." >> $INSTALL_LOG_FILE
        ##  sleep 2
        ##done      
      fi
    fi
}

webservice_uninstall() {
  local webservice_application_name="$1"
  webservice_running "${webservice_application_name}"
  webservice_require
  local WAR_NAME="${webservice_application_name}.war"
  if [ -n "$WEBSERVICE_DEPLOYED" ]; then
    if using_glassfish; then
      echo "Undeploying ${WEBSERVICE_DEPLOYED} from Glassfish..."
      $glassfish undeploy ${WEBSERVICE_DEPLOYED}
    elif using_tomcat; then
      echo "Undeploying ${WEBSERVICE_DEPLOYED} from Tomcat..."
      #wget -O - -q --no-check-certificate --no-proxy https://tomcat:tomcat@$MTWILSON_SERVER:$DEFAULT_API_PORT/manager/undeploy?path=${WEBSERVICE_DEPLOYED}
      rm -rf $TOMCAT_HOME/webapps/$WAR_NAME
    fi
  else
    if using_glassfish; then
      echo "Application is not deployed on Glassfish; skipping undeploy"
    elif using_tomcat; then
      echo "Application is not deployed on Tomcat; skipping undeploy"
    fi
  fi
}
webservice_require(){
  if using_glassfish; then
    glassfish_require
  elif using_tomcat; then
      tomcat_require
  fi
}

### FUNCTION LIBRARY: DATABASE FUNCTIONS

database_restart(){
  if using_glassfish; then
    glassfish_restart
  elif using_tomcat; then
    tomcat_restart
  fi
}

database_shutdown(){
  if using_glassfish; then
    glassfish_shutdown
  elif using_tomcat; then
    tomcat_shutdown
  fi
}
# determine database
which_dbms(){
  echo "Please identify the database which will be used for the Mt Wilson server.
The supported databases are m=MySQL | p=PostgreSQL"
  while true; do
    prompt_with_default DATABASE_CHOICE "Choose Database:" "p";

    if [ "$DATABASE_CHOICE" != 'm' ] && [ "$DATABASE_CHOICE" != 'p' ]; then
      echo "[m]ysql or [p]ostgresql: "
      DATABASE_CHOICE=
    else
      if [ "$DATABASE_CHOICE" = 'm' ]; then 
        export DATABASE_VENDOR="mysql"
      else
        export DATABASE_VENDOR="postgres"
      fi
      break
    fi
  done
  echo "Database Choice: $DATABASE_VENDOR" >> $INSTALL_LOG_FILE
}

# determine web server
which_web_server(){
echo "Please identify the web server which will be used for the Mt Wilson server.
The supported servers are g=Glassfish | t=Tomcat"
  while true; do
    prompt_with_default WEBSERVER_CHOICE "Choose Web Server:" "t";

    if [ "$WEBSERVER_CHOICE" != 't' ] && [ "$WEBSERVER_CHOICE" != 'g' ]; then
      echo "[g]lassfish [t]omcat: "
      WEBSERVER_CHOICE=
    else
      if [ "$WEBSERVER_CHOICE" = 't' ]; then 
        export WEBSERVER_VENDOR="tomcat"
      else
        export WEBSERVER_VENDOR="glassfish"
      fi
      break
    fi
  done
  echo "Web Server Choice: $WEBSERVER_VENDOR" >> $INSTALL_LOG_FILE
}
# parameters:
# 1. path to properties file
# 2. properties prefix (for mountwilson.as.db.user etc. the prefix is mountwilson.as.db)
# the default prefix is "postgres" for properties like "postgres.user", etc. The
# prefix must not have any spaces or special shell characters
# ONLY USE IF FILES ARE UNENCRYPTED!!!
postgres_read_connection_properties() {
    local config_file="$1"
    local prefix="${2:-postgres}"
    POSTGRES_HOSTNAME=`read_property_from_file ${prefix}.host "${config_file}"`
    POSTGRES_PORTNUM=`read_property_from_file ${prefix}.port "${config_file}"`
    POSTGRES_USERNAME=`read_property_from_file ${prefix}.user "${config_file}"`
    POSTGRES_PASSWORD=`read_property_from_file ${prefix}.password "${config_file}"`
    POSTGRES_DATABASE=`read_property_from_file ${prefix}.schema "${config_file}"`
}

# ONLY USE IF FILES ARE UNENCRYPTED!!!
postgres_write_connection_properties() {
    local config_file="$1"
    local prefix="${2:-postgres}"
    local encrypted="false"

    # Decrypt if needed
    if file_encrypted "$config_file"; then
      encrypted="true"
      decrypt_file "$config_file" "$MTWILSON_PASSWORD"
    fi

    update_property_in_file ${prefix}.host "${config_file}" "${POSTGRES_HOSTNAME}"
    update_property_in_file ${prefix}.port "${config_file}" "${POSTGRES_PORTNUM}"
    update_property_in_file ${prefix}.user "${config_file}" "${POSTGRES_USERNAME}"
    update_property_in_file ${prefix}.password "${config_file}" "${POSTGRES_PASSWORD}"
    update_property_in_file ${prefix}.schema "${config_file}" "${POSTGRES_DATABASE}"
    update_property_in_file ${prefix}.driver "${config_file}" "org.postgresql.Driver"
    
    # Return the file to encrypted state, if it was before
    if [ encrypted == "true" ]; then
      encrypt_file "$config_file" "$MTWILSON_PASSWORD"
    fi
}

# parameters:
# - configuration filename (absolute path)
# - property prefix for settings in the configuration file (java format is assumed, dot will be automatically appended to prefix)
postgres_userinput_connection_properties() {
    echo "Configuring DB Connection..."
    prompt_with_default POSTGRES_HOSTNAME "Hostname:" ${DEFAULT_POSTGRES_HOSTNAME}
    prompt_with_default POSTGRES_PORTNUM "Port Num:" ${DEFAULT_POSTGRES_PORTNUM}
    prompt_with_default POSTGRES_DATABASE "Database:" ${DEFAULT_POSTGRES_DATABASE}
    prompt_with_default POSTGRES_USERNAME "Username:" ${DEFAULT_POSTGRES_USERNAME}
    prompt_with_default_password POSTGRES_PASSWORD "Password:" ${DEFAULT_POSTGRES_PASSWORD}
}

# Set config file db properties
set_config_db_properties() {
  local scriptname="$1"
  local packagename="$2"
  intel_conf_dir=/etc/intel/cloudsecurity
  package_dir=/opt/intel/cloudsecurity/${packagename}
  package_config_filename=${intel_conf_dir}/${packagename}.properties
  package_env_filename=${package_dir}/${packagename}.env
  package_install_filename=${package_dir}/${packagename}.install
}

# The EclipseLink persistence framework sends messages to stdout that start with the text [EL Info] or [EL Warning].
# We suppress those because they are not useful for the customer, only for debugging.
# Caller can set setupconsole_dir to the directory where jars are found; default provided by DEFAULT_MTWILSON_JAVA_DIR
# Caller can set conf_dir to the directory where logback-stderr.xml is found; default provided by DEFAULT_MTWILSON_CONF_DIR
call_setupcommand() {
  local java_lib_dir=${setupconsole_dir:-$DEFAULT_MTWILSON_JAVA_DIR}
  if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
  SETUP_CONSOLE_JARS=$(JARS=($java_lib_dir/*.jar); IFS=:; echo "${JARS[*]}")
  mainclass=com.intel.mtwilson.setup.TextConsole
  $java -cp "$SETUP_CONSOLE_JARS" -Dlogback.configurationFile=${conf_dir:-$DEFAULT_MTWILSON_CONF_DIR}/logback-stderr.xml $mainclass $@ | grep -vE "^\[EL Info\]|^\[EL Warning\]" 2> /var/log/mtwilson.log
  return $?
}

# Caller can set setupconsole_dir to the directory where jars are found; default provided by DEFAULT_MTWILSON_JAVA_DIR
call_tag_setupcommand() {
  local java_lib_dir=${setupconsole_dir:-$DEFAULT_MTWILSON_JAVA_DIR}
  if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; return 1; fi
  SETUP_CONSOLE_JARS=$(JARS=($java_lib_dir/*.jar); IFS=:; echo "${JARS[*]}")
  mainclass=com.intel.mtwilson.launcher.console.Main
  local jvm_memory=2048m
  local jvm_maxperm=512m
  $java -Xmx${jvm_memory} -XX:MaxPermSize=${jvm_maxperm} -cp "$SETUP_CONSOLE_JARS" -Dlogback.configurationFile=${conf_dir:-$DEFAULT_MTWILSON_CONF_DIR}/logback-stderr.xml $mainclass $@ | grep -vE "^\[EL Info\]|^\[EL Warning\]" 2> /var/log/mtwilson.log
  return $?
}

file_encrypted() {
  local filename="${1}"
  if [ -n "$filename" ]; then
    if grep -q "ENCRYPTED DATA" "$filename"; then
      return 0 #"File encrypted: $filename"
    else
      return 1 #"File NOT encrypted: $filename"
    fi
  else
    return 2 # FILE NOT FOUND so cannot detect
  fi
}

decrypt_file() {
  local filename="${1}"
  export PASSWORD="${2}"
  if ! validate_path_configuration "$filename" 2>&1>/dev/null && ! validate_path_data "$filename" 2>&1>/dev/null; then
    echo_failure "Path validation failed. Verify path meets acceptable directory constraints: $filename"
    return 1
  fi
  if [ -f "$filename" ]; then
    call_setupcommand ExportConfig "$filename" --env-password="PASSWORD"
    if file_encrypted "$filename"; then
      echo_failure "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
      return 2
    fi
  else
    echo_warning "File not found: $filename"
    return 3
  fi
}

encrypt_file() {
  local filename="${1}"
  export PASSWORD="${2}"
  if ! validate_path_configuration "$filename" 2>&1>/dev/null && ! validate_path_data "$filename" 2>&1>/dev/null; then
    echo_failure "Path validation failed. Verify path meets acceptable directory constraints: $filename"
    return 1
  fi
  if [ -f "$filename" ]; then
    call_setupcommand ImportConfig "$filename" --env-password="PASSWORD"
    if ! file_encrypted "$filename"; then
      echo_failure "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
      return 2
    fi
  else
    echo_warning "File NOT found: $filename"
    return 3
  fi
}

load_conf() {
  local mtw_props_path="/etc/intel/cloudsecurity/mtwilson.properties"
  local as_props_path="/etc/intel/cloudsecurity/attestation-service.properties"
  #local pca_props_path="/etc/intel/cloudsecurity/PrivacyCA.properties"
  local ms_props_path="/etc/intel/cloudsecurity/management-service.properties"
  local mp_props_path="/etc/intel/cloudsecurity/mtwilson-portal.properties"
  local hp_props_path="/etc/intel/cloudsecurity/clientfiles/hisprovisioner.properties"
  local ta_props_path="/etc/intel/cloudsecurity/trustagent.properties"
  
  if [ -n "$DEFALT_ENV_LOADED" ]; then return; fi

  # mtwilson.properties file
  if [ -f "$mtw_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$mtw_props_path"; then
      echo -n "encrypted file [$mtw_props_path]....."
      temp=`call_setupcommand ExportConfig "$mtw_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_DATABASE_HOSTNAME=`echo $temp | awk -F'mtwilson.db.host=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_DATABASE_SCHEMA=`echo $temp | awk -F'mtwilson.db.schema=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_DATABASE_USERNAME=`echo $temp | awk -F'mtwilson.db.user=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_DATABASE_PASSWORD=`echo $temp | awk -F'mtwilson.db.password=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_DATABASE_PORTNUM=`echo $temp | awk -F'mtwilson.db.port=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_DATABASE_DRIVER=`echo $temp | awk -F'mtwilson.db.driver=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_WEBSERVER_VENDOR=`echo $temp | awk -F'mtwilson.webserver.vendor=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_MTWILSON_DEFAULT_TLS_POLICY_ID=`echo $temp | awk -F'mtwilson.default.tls.policy.id=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_MTWILSON_TLS_POLICY_ALLOW=`echo $temp | awk -F'mtwilson.tls.policy.allow=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_MTWILSON_TLS_KEYSTORE_PASSWORD=`echo $temp | awk -F'mtwilson.tls.keystore.password=' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$mtw_props_path]....."
      export CONF_DATABASE_HOSTNAME=`read_property_from_file mtwilson.db.host "$mtw_props_path"`
      export CONF_DATABASE_SCHEMA=`read_property_from_file mtwilson.db.schema "$mtw_props_path"`
      export CONF_DATABASE_USERNAME=`read_property_from_file mtwilson.db.user "$mtw_props_path"`
      export CONF_DATABASE_PASSWORD=`read_property_from_file mtwilson.db.password "$mtw_props_path"`
      export CONF_DATABASE_PORTNUM=`read_property_from_file mtwilson.db.port "$mtw_props_path"`
      export CONF_DATABASE_DRIVER=`read_property_from_file mtwilson.db.driver "$mtw_props_path"`
      export CONF_WEBSERVER_VENDOR=`read_property_from_file mtwilson.webserver.vendor "$mtw_props_path"`
      export CONF_MTWILSON_DEFAULT_TLS_POLICY_ID=`read_property_from_file mtwilson.default.tls.policy.id "$mtw_props_path"`
      export CONF_MTWILSON_TLS_POLICY_ALLOW=`read_property_from_file mtwilson.tls.policy.allow "$mtw_props_path"`
      export CONF_MTWILSON_TLS_KEYSTORE_PASSWORD=`read_property_from_file mtwilson.tls.keystore.password "$mtw_props_path"`
    fi
    echo_success "Done"
  fi
  
  # attestation-service.properties
  if [ -f "$as_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$as_props_path"; then
      echo -n "encrypted file [$as_props_path]....."
      temp=`call_setupcommand ExportConfig "$as_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_SAML_KEYSTORE_FILE=`echo $temp | awk -F'saml.keystore.file=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_SAML_KEYSTORE_PASSWORD=`echo $temp | awk -F'saml.keystore.password=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_SAML_KEY_ALIAS=`echo $temp | awk -F'saml.key.alias=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_SAML_KEY_PASSWORD=`echo $temp | awk -F'saml.key.password=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_SAML_ISSUER=`echo $temp | awk -F'saml.issuer=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_PRIVACYCA_SERVER=`echo $temp | awk -F'privacyca.server=' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$as_props_path]....."
      export CONF_SAML_KEYSTORE_FILE=`read_property_from_file saml.keystore.file "$as_props_path"`
      export CONF_SAML_KEYSTORE_PASSWORD=`read_property_from_file saml.keystore.password "$as_props_path"`
      export CONF_SAML_KEY_ALIAS=`read_property_from_file saml.key.alias "$as_props_path"`
      export CONF_SAML_KEY_PASSWORD=`read_property_from_file saml.key.password "$as_props_path"`
      export CONF_SAML_ISSUER=`read_property_from_file saml.issuer "$as_props_path"`
      export CONF_PRIVACYCA_SERVER=`read_property_from_file privacyca.server "$as_props_path"`
    fi
    echo_success "Done"
  fi

  # management-service.properties
  if [ -f "$ms_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$ms_props_path"; then
      echo -n "encrypted file [$ms_props_path]....."
      temp=`call_setupcommand ExportConfig "$ms_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_MS_KEYSTORE_DIR=`echo $temp | awk -F'mtwilson.ms.keystore.dir=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_API_KEY_ALIAS=`echo $temp | awk -F'mtwilson.api.key.alias=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_API_KEY_PASS=`echo $temp | awk -F'mtwilson.api.key.password=' '{print $2}' | awk -F' ' '{print $1}'`
      export CONF_CONFIGURED_API_BASEURL=`echo $temp | awk -F'mtwilson.api.baseurl=' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$ms_props_path]....."
      export CONF_MS_KEYSTORE_DIR=`read_property_from_file mtwilson.ms.keystore.dir "$ms_props_path"`
      export CONF_API_KEY_ALIAS=`read_property_from_file mtwilson.api.key.alias "$ms_props_path"`
      export CONF_API_KEY_PASS=`read_property_from_file mtwilson.api.key.password "$ms_props_path"`
      export CONF_CONFIGURED_API_BASEURL=`read_property_from_file mtwilson.api.baseurl "$ms_props_path"`
    fi
    echo_success "Done"
  fi


  # mtwilson-portal.properties
  if [ -f "$mp_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$mp_props_path"; then
      echo -n "encrypted file [$mp_props_path]....."
      temp=`call_setupcommand ExportConfig "$mp_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_TDBP_KEYSTORE_DIR=`echo $temp | awk -F'mtwilson.tdbp.keystore.dir=' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$mp_props_path]....."
      export CONF_TDBP_KEYSTORE_DIR=`read_property_from_file mtwilson.tdbp.keystore.dir "$mp_props_path"`
    fi
    echo_success "Done"
  fi
    
  # hisprovisioner.properties
  if [ -f "$hp_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$hp_props_path"; then
      echo -n "encrypted file [$hp_props_path]....."
      temp=`call_setupcommand ExportConfig "$hp_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_ENDORSEMENT_P12_PASS=`echo $temp | awk -F'EndorsementP12Pass = ' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$hp_props_path]....."
      export CONF_ENDORSEMENT_P12_PASS=`read_property_from_file EndorsementP12Pass "$hp_props_path"`
    fi
    echo_success "Done"
  fi

  # trustagent.properties
  if [ -f "$ta_props_path" ]; then
    echo -n "Reading properties from "
    if file_encrypted "$ta_props_path"; then
      echo -n "encrypted file [$ta_props_path]....."
      temp=`call_setupcommand ExportConfig "$ta_props_path" --stdout 2>&1`
      if [[ "$temp" == *"Incorrect password"* ]]; then
        echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."
        return 2
      fi
      export CONF_TRUSTAGENT_KEYSTORE_PASS=`echo $temp | awk -F'trustagent.keystore.password=' '{print $2}' | awk -F' ' '{print $1}'`
    else
      echo -n "file [$ta_props_path]....."
      export CONF_TRUSTAGENT_KEYSTORE_PASS=`read_property_from_file trustagent.keystore.password "$ta_props_path"`
    fi
    echo_success "Done"
  fi

  # Determine DATABASE_VENDOR
  if grep -q "postgres" <<< "$CONF_DATABASE_DRIVER"; then
    export CONF_DATABASE_VENDOR="postgres";
  elif grep -q "mysql" <<< "$CONF_DATABASE_DRIVER"; then
    export CONF_DATABASE_VENDOR="mysql";
  fi

  export DEFAULT_ENV_LOADED=true
  return 0
}

load_defaults() {
  export DEFAULT_MTWILSON_SERVER=""
  export DEFAULT_DATABASE_HOSTNAME=""
  export DEFAULT_DATABASE_SCHEMA=""
  export DEFAULT_DATABASE_USERNAME=""
  export DEFAULT_DATABASE_PASSWORD=""
  export DEFAULT_DATABASE_PORTNUM=""
  export DEFAULT_DATABASE_DRIVER=""
  export DEFAULT_WEBSERVER_VENDOR=""
  export DEFAULT_DATABASE_VENDOR=""
  export DEFAULT_PRIVACYCA_SERVER=""
  export DEFAULT_SAML_KEYSTORE_FILE="SAML.jks"
  export DEFAULT_SAML_KEYSTORE_PASSWORD=""
  export DEFAULT_SAML_KEY_ALIAS="samlkey1"
  export DEFAULT_SAML_KEY_PASSWORD=""
  export DEFAULT_SAML_ISSUER=""
  export DEFAULT_PRIVACYCA_SERVER=""
  export DEFAULT_MS_KEYSTORE_DIR="/var/opt/intel/management-service/users"
  export DEFAULT_API_KEY_ALIAS=""
  export DEFAULT_API_KEY_PASS=""
  export DEFAULT_CONFIGURED_API_BASEURL=""
  export DEFAULT_MTWILSON_DEFAULT_TLS_POLICY_ID=""
  export DEFAULT_MTWILSON_TLS_POLICY_ALLOW=""
  export DEFAULT_MTWILSON_TLS_KEYSTORE_PASSWORD=""
  export DEFAULT_TDBP_KEYSTORE_DIR=""
  export DEFAULT_ENDORSEMENT_P12_PASS=""
  export DEFAULT_TRUSTAGENT_KEYSTORE_PASS=""
  
  export MTWILSON_SERVER=${MTWILSON_SERVER:-${CONF_MTWILSON_SERVER:-$DEFAULT_MTWILSON_SERVER}}
  export DATABASE_HOSTNAME=${DATABASE_HOSTNAME:-${CONF_DATABASE_HOSTNAME:-$DEFAULT_DATABASE_HOSTNAME}}
  export DATABASE_SCHEMA=${DATABASE_SCHEMA:-${CONF_DATABASE_SCHEMA:-$DEFAULT_DATABASE_SCHEMA}}
  export DATABASE_USERNAME=${DATABASE_USERNAME:-${CONF_DATABASE_USERNAME:-$DEFAULT_DATABASE_USERNAME}}
  export DATABASE_PASSWORD=${DATABASE_PASSWORD:-${CONF_DATABASE_PASSWORD:-$DEFAULT_DATABASE_PASSWORD}}
  export DATABASE_PORTNUM=${DATABASE_PORTNUM:-${CONF_DATABASE_PORTNUM:-$DEFAULT_DATABASE_PORTNUM}}
  export DATABASE_DRIVER=${DATABASE_DRIVER:-${CONF_DATABASE_DRIVER:-$DEFAULT_DATABASE_DRIVER}}
  export WEBSERVER_VENDOR=${WEBSERVER_VENDOR:-${CONF_WEBSERVER_VENDOR:-$DEFAULT_WEBSERVER_VENDOR}}
  export DATABASE_VENDOR=${DATABASE_VENDOR:-${CONF_DATABASE_VENDOR:-$DEFAULT_DATABASE_VENDOR}}
  export PRIVACYCA_SERVER=${PRIVACYCA_SERVER:-${CONF_PRIVACYCA_SERVER:-$DEFAULT_PRIVACYCA_SERVER}}
  export SAML_KEYSTORE_FILE=${SAML_KEYSTORE_FILE:-${CONF_SAML_KEYSTORE_FILE:-$DEFAULT_SAML_KEYSTORE_FILE}}
  export SAML_KEYSTORE_PASSWORD=${SAML_KEYSTORE_PASSWORD:-${CONF_SAML_KEYSTORE_PASSWORD:-$DEFAULT_SAML_KEYSTORE_PASSWORD}}
  export SAML_KEY_ALIAS=${SAML_KEY_ALIAS:-${CONF_SAML_KEY_ALIAS:-$DEFAULT_SAML_KEY_ALIAS}}
  export SAML_KEY_PASSWORD=${SAML_KEY_PASSWORD:-${CONF_SAML_KEY_PASSWORD:-$DEFAULT_SAML_KEY_PASSWORD}}
  export SAML_ISSUER=${SAML_ISSUER:-${CONF_SAML_ISSUER:-$DEFAULT_SAML_ISSUER}}
  export PRIVACYCA_SERVER=${PRIVACYCA_SERVER:-${CONF_PRIVACYCA_SERVER:-$DEFAULT_PRIVACYCA_SERVER}}
  export MS_KEYSTORE_DIR=${MS_KEYSTORE_DIR:-${CONF_MS_KEYSTORE_DIR:-$DEFAULT_MS_KEYSTORE_DIR}}
  export API_KEY_ALIAS=${API_KEY_ALIAS:-${CONF_API_KEY_ALIAS:-$DEFAULT_API_KEY_ALIAS}}
  export API_KEY_PASS=${API_KEY_PASS:-${CONF_API_KEY_PASS:-$DEFAULT_API_KEY_PASS}}
  export CONFIGURED_API_BASEURL=${CONFIGURED_API_BASEURL:-${CONF_CONFIGURED_API_BASEURL:-$DEFAULT_CONFIGURED_API_BASEURL}}
  export MTWILSON_DEFAULT_TLS_POLICY_ID=${MTWILSON_DEFAULT_TLS_POLICY_ID:-${CONF_MTWILSON_DEFAULT_TLS_POLICY_ID:-$DEFAULT_MTWILSON_DEFAULT_TLS_POLICY_ID}}
  export MTWILSON_TLS_POLICY_ALLOW=${MTWILSON_TLS_POLICY_ALLOW:-${CONF_MTWILSON_TLS_POLICY_ALLOW:-$DEFAULT_MTWILSON_TLS_POLICY_ALLOW}}
  export MTWILSON_TLS_KEYSTORE_PASSWORD=${MTWILSON_TLS_KEYSTORE_PASSWORD:-${CONF_MTWILSON_TLS_KEYSTORE_PASSWORD:-$DEFAULT_MTWILSON_TLS_KEYSTORE_PASSWORD}}
  export TDBP_KEYSTORE_DIR=${TDBP_KEYSTORE_DIR:-${CONF_TDBP_KEYSTORE_DIR:-$DEFAULT_TDBP_KEYSTORE_DIR}}
  export ENDORSEMENT_P12_PASS=${ENDORSEMENT_P12_PASS:-${CONF_ENDORSEMENT_P12_PASS:-$DEFAULT_ENDORSEMENT_P12_PASS}}
  export TRUSTAGENT_KEYSTORE_PASS=${TRUSTAGENT_KEYSTORE_PASS:-${CONF_TRUSTAGENT_KEYSTORE_PASS:-$DEFAULT_TRUSTAGENT_KEYSTORE_PASS}}

  if using_mysql; then
    export MYSQL_HOSTNAME=${DATABASE_HOSTNAME}
    export MYSQL_PORTNUM=${MYSQL_PORTNUM:-${CONF_DATABASE_PORTNUM:-$DATABASE_PORTNUM}}
    export MYSQL_DATABASE=${DATABASE_SCHEMA}
    export MYSQL_USERNAME=${DATABASE_USERNAME}
    export MYSQL_PASSWORD=${DATABASE_PASSWORD}
  elif using_postgres; then
    export POSTGRES_HOSTNAME=${DATABASE_HOSTNAME}
    export POSTGRES_PORTNUM=${POSTGRES_PORTNUM:-${CONF_DATABASE_PORTNUM:-$DATABASE_PORTNUM}}
    export POSTGRES_DATABASE=${DATABASE_SCHEMA}
    export POSTGRES_USERNAME=${DATABASE_USERNAME}
    export POSTGRES_PASSWORD=${DATABASE_PASSWORD}
  fi
}

change_db_pass() {
  mysqladmin=`which mysqladmin 2>/dev/null`
  psql=`which psql 2>/dev/null`
  mtwilson=`which mtwilson 2>/dev/null`
  cryptopass="$MTWILSON_PASSWORD"
  
  #load_default_env 1>/dev/null
  
  # Do not allow a blank password to be specified
  prompt_with_default_password DATABASE_PASSWORD_NEW "New database password: " "$DATABASE_PASSWORD_NEW"
  new_db_pass="$DATABASE_PASSWORD_NEW"
  sed_escaped_value=$(sed_escape "$new_db_pass")
  
  # Check for encryption, add to array if encrypted
  encrypted_files=()
  count=0
  for i in `ls -1 /etc/intel/cloudsecurity/*.properties`; do
    if file_encrypted "$i"; then
      encrypted_files[count]="$i"
    fi
    let count++
  done
  
  local decryption_error=false
  for i in ${encrypted_files[@]}; do
    decrypt_file "$i" "$cryptopass"
    if [ $? -ne 0 ]; then
      decryption_error=true
    fi
  done
  if $decryption_error; then
    echo_error "Cannot decrypt configuration files; please set MTWILSON_PASSWORD"
    return 1
  fi
  
  load_conf
  load_defaults
  
  # Test DB connection and change password
  if using_mysql; then #MYSQL
    echo_success "using mysql"
    mysql_detect
    mysql_version
    mysql_test_connection_report
    if [ $? -ne 0 ]; then exit; fi
    $mysqladmin -h "$DATABASE_HOSTNAME" -u "$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" password "$new_db_pass"
    if [ $? -ne 0 ]; then echo_failure "Issue building mysql command."; exit; fi
  elif using_postgres; then #POSTGRES
    echo_success "using postgres"
    postgres_detect
    postgres_version
    postgres_test_connection_report
    if [ $? -ne 0 ]; then exit; fi
    temp=$(sudo "$psql" -h "$DATABASE_HOSTNAME" -d "$DATABASE_SCHEMA" -c "ALTER USER $DATABASE_USERNAME WITH PASSWORD '$new_db_pass';")
    echo ""
    if [ $? -ne 0 ]; then echo_failure "Issue building postgres or expect command."; exit; fi
    # Edit postgres password file if it exists
    if [ -f /root/.pgpass ]; then
      echo -n "Updating database password value in .pgpass file...."
      sed -i 's/\(.*\):\(.*\)/\1:'"$new_db_pass"'/' /root/.pgpass
      #temp=`cat /root/.pgpass | cut -f1,2,3,4 -d":"`
      #temp="$temp:$new_db_pass"
      #echo $temp > /root/.pgpass;
    fi
    echo_success "Done"
  fi

  # Edit .properties files
  for i in `ls -1 /etc/intel/cloudsecurity/*.properties`; do
    echo -n "Updating database password value in $i...."
    sed -i -e 's/db.password=[^\n]*/db.password='"$sed_escaped_value"'/g' "$i"
    echo_success "Done"
  done

  # 20140427 commented out the update to mtwilson.env because
  # running system should not depend on it or update it in any way;
  # the mtwilson.env is for install time only and is assumed to be 
  # deleted after install.
  ## Update password in mtwilson.env file
  #if [ -f /root/mtwilson.env ]; then
  #  echo -n "Updating database password value in mtwilson.env file...."
  #  export sed_escaped_value=$(sed_escape "$new_db_pass")
  #  sed -i -e 's/DATABASE_PASSWORD=[^\n]*/DATABASE_PASSWORD='\'"$sed_escaped_value"\''/g' "/root/mtwilson.env"
  #  echo_success "Done"
  #fi

  # Restart
  if using_glassfish; then
    echo_success "using glassfish"
    echo "Restarting mtwilson......"
    $mtwilson glassfish-restart
  elif using_tomcat; then
    echo_success "using tomcat"
    echo "Restarting mtwilson......"
    $mtwilson tomcat-restart
  fi
  echo_success "RESTART COMPLETED"

  # Encrypt files
  for i in ${encrypted_files[@]}; do
    encrypt_file "$i" "$cryptopass"
  done

  echo_success "DB PASSWORD CHANGE FINISHED"
}

#echoerr() { echo_failure "$@" 1>&2; }

function erase_data() {
 mysql=`which mysql 2>/dev/null`
 psql=`which psql 2>/dev/null`

 #load_default_env 1>/dev/null
  
   encrypted_files=()
  count=0
  for i in `ls -1 /etc/intel/cloudsecurity/*.properties`; do
    if file_encrypted "$i"; then
      encrypted_files[count]="$i"
    fi
    let count++
  done
  
  for i in ${encrypted_files[@]}; do
    decrypt_file "$i" "$MTWILSON_PASSWORD"
  done
  
  arr=(mw_measurement_xml mw_tag_certificate mw_tag_certificate_request mw_tag_selection_kvattribute mw_tag_selection mw_tag_kvattribute mw_host_tpm_password mw_asset_tag_certificate mw_audit_log_entry mw_module_manifest_log mw_ta_log mw_saml_assertion mw_host_specific_manifest mw_hosts mw_mle_source mw_module_manifest mw_pcr_manifest mw_mle mw_os mw_oem mw_tls_policy)

  # Test DB connection and change password
  if using_mysql; then #MYSQL
    echo_success "using mysql"
    mysql_detect
    mysql_version
    mysql_test_connection_report
    if [ $? -ne 0 ]; then exit; fi
    for table in ${arr[*]}
    do    
        $mysql -u "$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" -D"$DATABASE_SCHEMA" -e "DELETE from $table;"
    done 
  elif using_postgres; then #POSTGRES
    echo_success "using postgres"
    postgres_detect
    postgres_version
    postgres_test_connection_report
    if [ $? -ne 0 ]; 
     then exit; 
    fi
    for table in ${arr[*]}
    do
     
     temp=$(sudo "$psql" -d "$DATABASE_SCHEMA" -c "DELETE from $table;")
    done
  fi 
}

key_backup() {
  shift
  if ! options=$(getopt -a -n key-backup -l passwd: -o p: -- "$@"); then echo_failure "Usage: $0 key-backup [-p PASSWORD | --passwd PASSWORD]"; return 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      -p|--passwd) eval MTWILSON_PASSWORD="\$$2"; shift;;
      --) shift; args="$@"; shift;;
    esac
    shift
  done

  args=`echo $args | sed -e 's/^ *//' -e 's/ *$//'`
  if [ -n "$args" ]; then echo_failure "Usage: $0 key-backup [-p PASSWORD | --passwd PASSWORD]"; return 2; fi

  export MTWILSON_PASSWORD
  if [ -z "$MTWILSON_PASSWORD" ]; then echo_failure "Encryption password cannot be null."; return 3; fi

  configDir="/opt/mtwilson/configuration"
  keyBackupDir="/var/mtwilson/key-backup"
  datestr=`date +%Y-%m-%d.%H%M%S`
  keyBackupFile="$keyBackupDir/mtwilson-keys_$datestr.enc"
  mkdir -p "$keyBackupDir" 2>/dev/null
  filesToEncrypt="$configDir/*.*"
  if [ -f "$configDir/private/password.txt" ]; then filesToEncrypt="$filesToEncrypt $configDir/private/*.*"; fi
  /opt/mtwilson/bin/encrypt.sh -p MTWILSON_PASSWORD --nopbkdf2 "$keyBackupFile" "$filesToEncrypt" > /dev/null
  find "$configDir/" -name "*.sig" -type f -delete
  shred -uzn 3 "$keyBackupFile.zip"
  echo_success "Keys backed up to: $keyBackupFile"
}

key_restore() {
  shift
  if ! options=$(getopt -a -n key-restore -l passwd: -o p: -- "$@"); then echo_failure "Usage: $0 key-restore [-p PASSWORD | --passwd PASSWORD] file_name"; return 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      -p|--passwd) eval MTWILSON_PASSWORD="\$$2"; shift;;
      --) shift; args="$@"; shift;;
    esac
    shift
  done

  args=`echo $args | sed -e 's/^ *//' -e 's/ *$//'`
  if [[ "$args" == *" "* ]]; then echo_failure "Usage: $0 key-restore [-p PASSWORD | --passwd PASSWORD] file_name"; return 2; fi

  export MTWILSON_PASSWORD
  if [ -z "$MTWILSON_PASSWORD" ]; then echo_failure "Encryption password cannot be null."; return 3; fi

  keyBackupFile="$args"
  keyBackupDir="$keyBackupFile.d"
  configDir="/opt/mtwilson/configuration"
  if [ ! -f "$keyBackupFile" ]; then
    echo_failure "File does not exist"
    return 4
  fi
  /opt/mtwilson/bin/decrypt.sh -p MTWILSON_PASSWORD "$keyBackupFile" > /dev/null
  find "$keyBackupDir/" -name "*.sig" -type f -delete
  cp -R "$keyBackupDir"/* "$configDir"/
  find "$keyBackupDir" -type f -exec shred -uzn 3 {} \;
  rm -rf "$keyBackupDir"
  shred -uzn 3 "$keyBackupFile.zip"

  # password.txt file in private directory
  if [ -f "$configDir/password.txt" ]; then
    mkdir -p "$configDir/private" 2>/dev/null
    cp -R "$configDir/password.txt" "$configDir/private/password.txt"
    shred -uzn 3 "$configDir/password.txt"
  fi

  echo_success "Keys restored from: $keyBackupFile"
}
