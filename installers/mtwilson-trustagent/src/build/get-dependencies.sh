#!/bin/bash
TBOOT_VERSION="1.9.5"
TBOOTXM_VERSION="3.2-SNAPSHOT"


yum_detect() {
  yum=`which yum 2>/dev/null`
  if [ -n "$yum" ]; then return 0; else return 1; fi
}

aptget_detect() {
  aptget=`which apt-get 2>/dev/null`
  aptcache=`which apt-cache 2>/dev/null`
  if [ -n "$aptget" ]; then return 0; else return 1; fi
}

maven_get() {
  local group_id="${1}"
  local artifact_id="${2}"
  local version="${3}"
  local packaging="${4}"
  local classifier="${5}"
  local destination="${6}"
  mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:get -DgroupId="${group_id}" -DartifactId="${artifact_id}" -Dversion="${version}" -Dpackaging="${packaging}" -Dclassifier="${classifier}" -Ddest="${destination}"
}

detect_os() {
  # RHEL
  if yum_detect; then
    echo -n "rhel" > ../distro
  # UBUNTU
  elif aptget_detect; then
    echo -n "ubuntu" > ../distro
  else
    echo "Unsupported OS"
    exit 1
  fi
}

maven_get_tboot() {
  local _group_id="net.sourceforge.tboot"
  local _artifact_id="tboot"
  local _version="${TBOOT_VERSION}"
  local _packaging=
  local _classifier=
  local _destination=
  
  # RHEL
  if yum_detect; then
    _packaging="rpm"
    _classifier="x86_64"
  # UBUNTU
  elif aptget_detect; then
    _packaging="deb"
    _classifier="amd64"
  else
    echo "Unsupported OS: Cannot retrieve tboot package"
    exit 1
  fi
  _destination="$(pwd)/${_artifact_id}-${_version}-${_classifier}.${_packaging}"
  maven_get "${_group_id}" "${_artifact_id}" "${_version}" "${_packaging}" "${_classifier}" "${_destination}"
}

maven_get_tbootxm() {
  local _group_id="com.intel.mtwilson.tbootxm.packages"
  local _artifact_id="tbootxm"
  local _version="${TBOOTXM_VERSION}"
  local _packaging="bin"
  local _classifier=
  local _destination=
  
  # RHEL
  if yum_detect; then
    _classifier="rhel"
  # UBUNTU
  elif aptget_detect; then
    _classifier="ubuntu"
  else
    echo "Unsupported OS: Cannot retrieve tbootxm package"
    exit 1
  fi
  _destination="$(pwd)/${_artifact_id}-${_version}-${_classifier}.${_packaging}"
  maven_get "${_group_id}" "${_artifact_id}" "${_version}" "${_packaging}" "${_classifier}" "${_destination}"
}

detect_os
maven_get_tboot
maven_get_tbootxm
