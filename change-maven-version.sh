#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${version}"
mvnInstallCommand="mvn clean install"

$changeVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi
$changeParentVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions" >&2; exit 3; fi
(cd common && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"common\" folder" >&2; exit 3; fi
(cd database && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"database\" folder" >&2; exit 3; fi
(cd desktop && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"desktop\" folder" >&2; exit 3; fi
(cd features && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"features\" folder" >&2; exit 3; fi
(cd integration && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"integration\" folder" >&2; exit 3; fi
(cd plugins && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"plugins\" folder" >&2; exit 3; fi
(cd portals && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"portals\" folder" >&2; exit 3; fi
(cd services && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"services\" folder" >&2; exit 3; fi
(cd trust-agent && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"trust-agent\" folder" >&2; exit 3; fi
sed -i 's/\(<mtwilson.version>\).*\(<\/mtwilson.version>\)/\1'${version}'\2/g' installers/pom.xml
if [ $? -ne 0 ]; then echo "Failed to change mtwilson.version in \"installers/pom.xml\"" >&2; exit 3; fi
(cd installers && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"installers\" folder" >&2; exit 3; fi
(cd packages && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"packages\" folder" >&2; exit 3; fi
sed -i 's/\-[0-9\.]*\(\-SNAPSHOT\|\(\-\|\.zip$\|\.bin$\|\.jar$\)\)/-'${version}'\2/g' build.targets
if [ $? -ne 0 ]; then echo "Failed to change versions in \"build.targets\" file" >&2; exit 3; fi
