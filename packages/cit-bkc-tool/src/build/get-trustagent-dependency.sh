#!/bin/bash
TRUSTAGENT_VERSION="3.2-SNAPSHOT"


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

maven_get_trustagent() {
  local _group_id="com.intel.mtwilson.linux"
  local _artifact_id="mtwilson-trustagent"
  local _version="${TRUSTAGENT_VERSION}"
  local _packaging="bin"
  local _classifier=
  local _destination=
  
  # RHEL
  if yum_detect; then
    echo -n "rhel" > ../distro
    _artifact_id+="-rhel"
  # UBUNTU
  elif aptget_detect; then
    echo -n "ubuntu" > ../distro
    _artifact_id+="-ubuntu"
  fi
  _destination="$(pwd)/${_artifact_id}-${_version}.${_packaging}"
  maven_get "${_group_id}" "${_artifact_id}" "${_version}" "${_packaging}" "${_classifier}" "${_destination}"
}

maven_get_trustagent